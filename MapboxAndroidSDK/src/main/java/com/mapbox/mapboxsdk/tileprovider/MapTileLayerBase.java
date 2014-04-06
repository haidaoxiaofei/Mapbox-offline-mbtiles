// Created by plusminus on 21:46:22 - 25.09.2008
package com.mapbox.mapboxsdk.tileprovider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.TileLooper;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.HashMap;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * This is an abstract class. The tile provider is responsible for:
 * <ul>
 * <li>determining if a map tile is available,</li>
 * <li>notifying the client, via a callback handler</li>
 * </ul>
 * see {@link MapTile} for an overview of how tiles are served by this provider.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 */
public abstract class MapTileLayerBase implements IMapTileProviderCallback,
        TileLayerConstants {
    protected Context context;
    protected final MapTileCache mTileCache;
    private Handler mTileRequestCompleteHandler;
    private boolean mUseDataConnection = true;

    private ITileLayer mTileSource;
    protected String mCacheKey = "";

    /**
     * Attempts to get a Drawable that represents a {@link MapTile}. If the tile is not immediately
     * available this will return null and attempt to get the tile from known tile sources for
     * subsequent future requests. Note that this may return a {@link CacheableBitmapDrawable} in
     * which case you should follow proper handling procedures for using that Drawable or it may
     * reused while you are working with it.
     *
     * @see CacheableBitmapDrawable
     */
    public abstract Drawable getMapTile(MapTile pTile);

    public abstract void detach();

    /**
     * Gets the minimum zoom level this tile provider can provide
     *
     * @return the minimum zoom level
     */
    public float getMinimumZoomLevel() {
        return mTileSource.getMinimumZoomLevel();
    }

    /**
     * Get the maximum zoom level this tile provider can provide.
     *
     * @return the maximum zoom level
     */
    public float getMaximumZoomLevel() {
        return mTileSource.getMaximumZoomLevel();
    }

    /**
     * Get the tile size in pixels this tile provider provides.
     *
     * @return the tile size in pixels
     */
    public int getTileSizePixels() {
        return mTileSource.getTileSizePixels();
    }

    /**
     * Get the tile provider bounding box.
     *
     * @return the tile source bounding box
     */
    public BoundingBox getBoundingBox() {
        return mTileSource.getBoundingBox();
    }

    /**
     * Get the tile provider center.
     *
     * @return the tile source center
     */
    public LatLng getCenterCoordinate() {
        return mTileSource.getCenterCoordinate();
    }

    /**
     * Get the tile provider suggested starting zoom.
     *
     * @return the tile suggested starting zoom
     */
    public float getCenterZoom() {
        return mTileSource.getCenterZoom();
    }

    /**
     * Sets the tile source for this tile provider.
     *
     * @param pTileSource the tile source
     */
    public void setTileSource(final ITileLayer pTileSource) {
        if (mTileSource != null) {
            mTileSource.detach();
        }
        mTileSource = pTileSource;
        if (mTileSource != null) {
            mCacheKey = mTileSource.getCacheKey();
        }
    }

    /**
     * Gets the tile source for this tile provider.
     *
     * @return the tile source
     */
    public ITileLayer getTileSource() {
        return mTileSource;
    }

    /**
     * Gets the cache key for that layer
     *
     * @return the cache key
     */
    public String getCacheKey() {
        return mCacheKey;
    }

    /**
     * Creates a {@link MapTileCache} to be used to cache tiles in memory.
     */
    public MapTileCache createTileCache(final Context context) {
        return new MapTileCache(context);
    }

    public MapTileLayerBase(final Context context, final ITileLayer pTileSource) {
        this(context, pTileSource, null);
    }

    public MapTileLayerBase(final Context context,
                            final ITileLayer pTileSource,
                            final Handler pDownloadFinishedListener) {
        this.context = context;
        mTileRequestCompleteHandler = pDownloadFinishedListener;
        mTileSource = pTileSource;
        mTileCache = this.createTileCache(context);
    }

    /**
     * Called by implementation class methods indicating that they have completed the request as
     * best it can. The tile is added to the cache, and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState    the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @Override
    public void mapTileRequestCompleted(final MapTileRequestState pState, final Drawable pDrawable) {
        // tell our caller we've finished and it should update its view
        if (mTileRequestCompleteHandler != null) {
            Message msg = new Message();
            msg.obj = pState.getMapTile().getTileRect();
            msg.what = MapTile.MAPTILE_SUCCESS_ID;
            mTileRequestCompleteHandler.sendMessage(msg);
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "MapTileLayerBase.mapTileRequestCompleted(): " + pState.getMapTile());
        }
    }

    /**
     * Called by implementation class methods indicating that they have failed to retrieve the
     * requested map tile. a MAPTILE_FAIL_ID message is sent.
     *
     * @param pState the map tile request state object
     */
    @Override
    public void mapTileRequestFailed(final MapTileRequestState pState) {
        if (mTileRequestCompleteHandler != null) {
            mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_FAIL_ID);
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "MapTileLayerBase.mapTileRequestFailed(): " + pState.getMapTile());
        }
    }

    /**
     * Called by implementation class methods indicating that they have produced an expired result
     * that can be used but better results may be delivered later. The tile is added to the cache,
     * and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState    the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @Override
    public void mapTileRequestExpiredTile(MapTileRequestState pState, CacheableBitmapDrawable pDrawable) {
        // Put the expired tile into the cache
        putExpiredTileIntoCache(pState.getMapTile(), pDrawable);

        // tell our caller we've finished and it should update its view
        if (mTileRequestCompleteHandler != null) {
            mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.i(TAG, "MapTileLayerBase.mapTileRequestExpiredTile(): " + pState.getMapTile());
        }
    }

    private void putTileIntoCacheInternal(final MapTile pTile, final Drawable pDrawable) {
        mTileCache.putTile(pTile, pDrawable);
    }

    private class CacheTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            putTileIntoCacheInternal((MapTile) params[0], (Drawable) params[1]);
            return null;
        }
    }

    private void putTileIntoCache(final MapTile pTile, final Drawable pDrawable) {
//        mTileCache.putTileInMemoryCache(pTile, pDrawable);
        if (pDrawable != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                (new CacheTask()).execute(pTile, pDrawable);
            } else {
                putTileIntoCacheInternal(pTile, pDrawable);
            }
        }
    }

    protected void putTileIntoCache(final MapTileRequestState pState, final Drawable pDrawable) {
        putTileIntoCache(pState.getMapTile(), pDrawable);
    }

    protected void removeTileFromCache(final MapTileRequestState pState) {
        mTileCache.removeTileFromMemory(pState.getMapTile());
    }

    protected CacheableBitmapDrawable putExpiredTileIntoCache(final MapTile pTile, final CacheableBitmapDrawable drawable) {
        CacheableBitmapDrawable currentDrawable = mTileCache.getMapTileFromMemory(pTile);
        if (drawable != null && currentDrawable == null) {
            currentDrawable = mTileCache.putTileInMemoryCache(pTile, drawable);
        }
        return currentDrawable;
    }

    public void setTileRequestCompleteHandler(final Handler handler) {
        mTileRequestCompleteHandler = handler;
    }

    public void clearTileMemoryCache() {
        mTileCache.purgeMemoryCache();
    }

    public void clearTileDiskCache() {
        mTileCache.purgeDiskCache();
    }

    /**
     * Whether to use the network connection if it's available.
     */
    @Override
    public boolean useDataConnection() {
        return mUseDataConnection;
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param pMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean pMode) {
        mUseDataConnection = pMode;
    }


    public boolean hasNoSource() {
        return mTileSource == null;
    }

    /**
     * Recreate the cache using scaled versions of the tiles currently in it
     *
     * @param pNewZoomLevel the zoom level that we need now
     * @param pOldZoomLevel the previous zoom level that we should get the tiles to rescale
     * @param projection    the projection to compute view port
     */
    public void rescaleCache(final float pNewZoomLevel, final float pOldZoomLevel, final Projection projection) {

        if (hasNoSource() || Math.floor(pNewZoomLevel) == Math.floor(pOldZoomLevel)) {
            return;
        }

        final long startMs = System.currentTimeMillis();

        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "rescale tile cache from " + pOldZoomLevel + " to " + pNewZoomLevel);
        }

        final int tileSize = getTileSizePixels();
        final Rect viewPort = GeometryMath.viewPortRectForTileDrawing(pNewZoomLevel, projection, null);

        final ScaleTileLooper tileLooper = pNewZoomLevel > pOldZoomLevel
                ? new ZoomInTileLooper(pOldZoomLevel)
                : new ZoomOutTileLooper(pOldZoomLevel);
        tileLooper.loop(null, this, pNewZoomLevel, tileSize, viewPort);

        final long endMs = System.currentTimeMillis();
        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "Finished rescale in " + (endMs - startMs) + "ms");
        }
    }

    private abstract class ScaleTileLooper extends TileLooper {

        /**
         * new (scaled) tiles to add to cache
         * NB first generate all and then put all in cache,
         * otherwise the ones we need will be pushed out
         */
        protected final HashMap<MapTile, Bitmap> mNewTiles;

        protected final float mOldZoomLevel;
        protected float mDiff;
        protected int mTileSize_2;
        protected Rect mSrcRect;
        protected Rect mDestRect;
        protected Paint mDebugPaint;

        public ScaleTileLooper(final float pOldZoomLevel) {
            mOldZoomLevel = pOldZoomLevel;
            mNewTiles = new HashMap<MapTile, Bitmap>();
            mSrcRect = new Rect();
            mDestRect = new Rect();
            mDebugPaint = new Paint();
        }

        @Override
        public void initializeLoop(final float pZoomLevel, final int pTileSizePx) {
            mDiff = (float) Math.abs(Math.floor(pZoomLevel) - Math.floor(mOldZoomLevel));
            mTileSize_2 = (int) GeometryMath.rightShift(pTileSizePx, mDiff);
        }

        @Override
        public void handleTile(final Canvas pCanvas, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

            // Get tile from cache.
            // If it's found then no need to created scaled version.
            // If not found (null) them we've initiated a new request for it,
            // and now we'll create a scaled version until the request completes.
            final Drawable requestedTile = getMapTileFromMemory(pTile);
            if (requestedTile == null) {
                try {
                    handleTile(pTileSizePx, pTile, pX, pY);
                } catch (final OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError rescaling cache");
                }
            }
        }

        @Override
        public void finalizeLoop() {
            // now add the new ones, pushing out the old ones
            while (!mNewTiles.isEmpty()) {

                final MapTile tile = mNewTiles.keySet().iterator().next();
                final Bitmap bitmap = mNewTiles.remove(tile);


                CacheableBitmapDrawable existingTileDrawable = mTileCache.getMapTileFromMemory(tile);
                if (existingTileDrawable == null || BitmapUtils.isCacheDrawableExpired(existingTileDrawable)) {
                    final CacheableBitmapDrawable drawable = mTileCache.createCacheableBitmapDrawable(bitmap, tile);
                    BitmapUtils.setCacheDrawableExpired(drawable);
                    putExpiredTileIntoCache(tile, drawable);
                }

            }
        }

        protected abstract void handleTile(int pTileSizePx, MapTile pTile, int pX, int pY);
    }

    private class ZoomInTileLooper extends ScaleTileLooper {
        public ZoomInTileLooper(final float pOldZoomLevel) {
            super(pOldZoomLevel);
        }

        @Override
        public void handleTile(final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

            // get the correct fraction of the tile from cache and scale up
            final MapTile oldTile = new MapTile(MapTileLayerBase.this.getCacheKey(),
                    (int) Math.floor(mOldZoomLevel),
                    (int) GeometryMath.rightShift(pX, mDiff),
                    (int) GeometryMath.rightShift(pY, mDiff));
            final Drawable oldDrawable = getMapTileFromMemory(oldTile);

            if (oldDrawable instanceof BitmapDrawable) {
                final int xx = (pX % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                final int yy = (pY % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
                mDestRect.set(0, 0, pTileSizePx, pTileSizePx);

                // Try to get a bitmap from the pool, otherwise allocate a new one
                Bitmap bitmap = mTileCache.getBitmapFromRemoved(pTileSizePx,
                        pTileSizePx);

                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx,
                            Bitmap.Config.ARGB_8888);
                }

                final Canvas canvas = new Canvas(bitmap);
                final boolean isReusable = oldDrawable instanceof CacheableBitmapDrawable;
                boolean success = false;
                if (isReusable) {
                    ((CacheableBitmapDrawable) oldDrawable).setBeingUsed(true);
                }
                try {
                    if (!isReusable || ((CacheableBitmapDrawable) oldDrawable).isBitmapValid()) {
                        final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                        canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);
                        success = true;
                        /*
                            Log.i(TAG, "Created scaled tile: " + pTile);
                            mDebugPaint.setTextSize(40);
                            canvas.drawText("scaled", 50, 50, mDebugPaint);
                        */
                    }
                } finally {
                    if (isReusable) {
                        ((CacheableBitmapDrawable) oldDrawable).setBeingUsed(false);
                    }
                }
                if (success) {
                    mNewTiles.put(pTile, bitmap);
                }
            }
        }
    }

    private class ZoomOutTileLooper extends ScaleTileLooper {
        private static final int MAX_ZOOM_OUT_DIFF = 4;

        public ZoomOutTileLooper(final float pOldZoomLevel) {
            super(pOldZoomLevel);
        }

        @Override
        protected void handleTile(final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

            if (mDiff >= MAX_ZOOM_OUT_DIFF) {
                return;
            }

            // get many tiles from cache and make one tile from them
            final int xx = (int) GeometryMath.leftShift(pX, mDiff);
            final int yy = (int) GeometryMath.leftShift(pY, mDiff);
            final int numTiles = (int) GeometryMath.leftShift(1, mDiff);
            Bitmap bitmap = null;
            Canvas canvas = null;
            for (int x = 0; x < numTiles; x++) {
                for (int y = 0; y < numTiles; y++) {
                    final MapTile oldTile = new MapTile(MapTileLayerBase.this.getCacheKey(),
                            (int) Math.floor(mOldZoomLevel),
                            xx + x,
                            yy + y);
                    final Drawable oldDrawable = mTileCache.getMapTileFromMemory(oldTile);
                    if (oldDrawable instanceof BitmapDrawable) {
                        final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                        if (oldBitmap != null) {
                            if (bitmap == null) {
                                // Try to get a bitmap from the pool, otherwise allocate a new one
                                bitmap = mTileCache.getBitmapFromRemoved(
                                        pTileSizePx, pTileSizePx);
                                if (bitmap == null) {
                                    bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx,
                                            Bitmap.Config.ARGB_8888);
                                }
                                canvas = new Canvas(bitmap);
                                canvas.drawColor(Color.LTGRAY);
                            }
                            mDestRect.set(
                                    x * mTileSize_2, y * mTileSize_2,
                                    (x + 1) * mTileSize_2, (y + 1) * mTileSize_2);
                            canvas.drawBitmap(oldBitmap, null, mDestRect, null);
                            mTileCache.removeTileFromMemory(oldTile);
                        }
                    }
                }
            }

            if (bitmap != null) {
                mNewTiles.put(pTile, bitmap);
                /*
                    Log.i(TAG, "Created scaled tile: " + pTile);
                    mDebugPaint.setTextSize(40);
                    canvas.drawText("scaled", 50, 50, mDebugPaint);
                */
            }
        }
    }

    public CacheableBitmapDrawable getMapTileFromMemory(MapTile pTile) {
        return (mTileCache != null) ? mTileCache.getMapTileFromMemory(pTile) : null;
    }

    private static final String TAG = "MapTileLayerBase";

}
