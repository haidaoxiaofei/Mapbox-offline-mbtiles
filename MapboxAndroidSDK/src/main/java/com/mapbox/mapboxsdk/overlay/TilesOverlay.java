package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.TileLooper;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.HashMap;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * These objects are the principle consumer of map tiles.
 * <p/>
 * see {@link MapTile} for an overview of how tiles are acquired by this overlay.
 */

public class TilesOverlay
        extends SafeDrawOverlay {

    public static final int MENU_OFFLINE = getSafeMenuId();

    /**
     * Current tile source
     */
    protected final MapTileLayerBase mTileProvider;

    /* to avoid allocations during draw */
    protected static SafePaint mDebugPaint = null;
    protected Paint mLoadingPaint = null;
    private final Rect mTileRect = new Rect();
    private final Rect mViewPort = new Rect();
    private final Rect mClipRect = new Rect();
    float mCurrentZoomFactor = 1;

    private boolean mOptionsMenuEnabled = true;

    private int mWorldSize_2;

    /**
     * A drawable loading tile *
     */
    private BitmapDrawable mLoadingTile = null;
    private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
    private int mLoadingLineColor = Color.rgb(200, 192, 192);

    public TilesOverlay(final MapTileLayerBase aTileProvider) {
        super();
        if (aTileProvider == null) {
            throw new IllegalArgumentException(
                    "You must pass a valid tile provider to the tiles overlay.");
        }
        this.mTileProvider = aTileProvider;
        if (UtilConstants.DEBUGMODE) {
            if (mDebugPaint == null) {
                mDebugPaint = new SafePaint();
                mDebugPaint.setAntiAlias(true);
                mDebugPaint.setFilterBitmap(true);
                mDebugPaint.setColor(Color.RED);
                mDebugPaint.setStyle(Paint.Style.STROKE);
            }
        }

        mLoadingPaint = new Paint();
        mLoadingPaint.setAntiAlias(true);
        mLoadingPaint.setFilterBitmap(true);
        mLoadingPaint.setColor(mLoadingLineColor);
        mLoadingPaint.setStrokeWidth(0);
    }

    @Override
    public void onDetach(final MapView pMapView) {
        this.mTileProvider.detach();
    }

    public float getMinimumZoomLevel() {
        return mTileProvider.getMinimumZoomLevel();
    }

    public float getMaximumZoomLevel() {
        return mTileProvider.getMaximumZoomLevel();
    }

    /**
     * Whether to use the network connection if it's available.
     */
    public boolean useDataConnection() {
        return mTileProvider.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mTileProvider.setUseDataConnection(aMode);
    }

    @Override
    protected void drawSafe(final ISafeCanvas c, final MapView mapView, final boolean shadow) {

        if (shadow) {
            return;
        }

        // Calculate the half-world size
        final Projection pj = mapView.getProjection();
        c.getClipBounds(mClipRect);
        final float zoomLevel = pj.getZoomLevel();
        mWorldSize_2 = pj.getHalfWorldSize();
        GeometryMath.viewPortRectForTileDrawing(pj, mViewPort);

        // Draw the tiles!
        drawTiles(c.getSafeCanvas(), zoomLevel, Projection.getTileSize(), mViewPort, mClipRect);
    }

    /**
     * This is meant to be a "pure" tile drawing function that doesn't take into account
     * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
     * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
     * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
     */
    public void drawTiles(final Canvas c, final float zoomLevel, final int tileSizePx,
                          final Rect viewPort, final Rect pClipRect) {

        Log.d(TAG, "drawTiles  " + pClipRect.toString());
        mTileLooper.loop(c, mTileProvider.getCacheKey(), zoomLevel, tileSizePx, viewPort, pClipRect);

        // draw a cross at center in debug mode
        if (UtilConstants.DEBUGMODE) {
            ISafeCanvas canvas = (ISafeCanvas) c;
            final Point centerPoint = new Point(viewPort.centerX() - mWorldSize_2,
                    viewPort.centerY() - mWorldSize_2);
            canvas.drawLine(centerPoint.x, centerPoint.y - 9,
                    centerPoint.x, centerPoint.y + 9, mDebugPaint);
            canvas.drawLine(centerPoint.x - 9, centerPoint.y,
                    centerPoint.x + 9, centerPoint.y, mDebugPaint);
        }

    }

    private final TileLooper mTileLooper = new TileLooper() {
        @Override
        public void initializeLoop(final float pZoomLevel, final int pTileSizePx) {

            final int roundedZoom = (int) Math.floor(pZoomLevel);
            if (roundedZoom != pZoomLevel) {
                final int mapTileUpperBound = 1 << roundedZoom;
                mCurrentZoomFactor = (float) Projection.mapSize(pZoomLevel) / mapTileUpperBound / pTileSizePx;
            } else {
                mCurrentZoomFactor = 1.0f;
            }
        }

        @Override
        public void handleTile(final Canvas pCanvas,
                               final String pCacheKey,
                               final int pTileSizePx,
                               final MapTile pTile,
                               final int pX,
                               final int pY,
                               final Rect pClipRect) {
            final float x = pX * pTileSizePx * mCurrentZoomFactor - mWorldSize_2;
            final float y = pY * pTileSizePx * mCurrentZoomFactor - mWorldSize_2;
            final float w = pTileSizePx * mCurrentZoomFactor;
            mTileRect.set((int) x, (int) y, (int) (x + w), (int) (y + w));
            if (!Rect.intersects(mTileRect, pClipRect)) {
                Log.d(TAG, "not drawing  " + pTile.toString() + "//" + mTileRect.toString());
                return;
            }
            Log.d(TAG, "handleTile " + pTile.toString());
            pTile.setTileRect(mTileRect);
            Drawable drawable = mTileProvider.getMapTile(pTile);
            if (drawable == null) {
                drawable = getLoadingTile();
            }
            boolean isReusable = drawable instanceof CacheableBitmapDrawable;

            if (drawable != null) {
                if (isReusable) {
                    Log.d(TAG, "about to draw " + ((CacheableBitmapDrawable) drawable).getUrl());
                    mBeeingUsedDrawables.add((CacheableBitmapDrawable) drawable);
                }
                drawable.setBounds(mTileRect);
                drawable.draw(pCanvas);
                if (UtilConstants.DEBUGMODE) {
                    ISafeCanvas canvas = (ISafeCanvas) pCanvas;
                    canvas.drawText(pTile.toString(), mTileRect.left + 1,
                            mTileRect.top + mDebugPaint.getTextSize(), mDebugPaint);
                    canvas.drawRect(mTileRect, mDebugPaint);
                }
            }
        }
    };

    public int getLoadingBackgroundColor() {
        return mLoadingBackgroundColor;
    }

    /**
     * Set the color to use to draw the background while we're waiting for the tile to load.
     *
     * @param pLoadingBackgroundColor the color to use. If the value is {@link Color#TRANSPARENT} then there will be no
     *                                loading tile.
     */
    public void setLoadingBackgroundColor(final int pLoadingBackgroundColor) {
        if (mLoadingBackgroundColor != pLoadingBackgroundColor) {
            mLoadingBackgroundColor = pLoadingBackgroundColor;
            clearLoadingTile();
        }
    }

    public int getLoadingLineColor() {
        return mLoadingLineColor;
    }

    public void setLoadingLineColor(final int pLoadingLineColor) {
        if (mLoadingLineColor != pLoadingLineColor) {
            mLoadingLineColor = pLoadingLineColor;
            mLoadingPaint.setColor(mLoadingLineColor);
            clearLoadingTile();
        }
    }

    /**
     * Draw a 'loading' placeholder with a canvas.
     *
     * @return
     */
    private Drawable getLoadingTile() {
        if (mLoadingTile == null && mLoadingBackgroundColor != Color.TRANSPARENT) {
            try {
                final int tileSize = mTileProvider.getTileSource() != null ?
                        mTileProvider
                                .getTileSource().getTileSizePixels() : 256;
                final Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize,
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(mLoadingBackgroundColor);
                final int lineSize = tileSize / 16;
                for (int a = 0; a < tileSize; a += lineSize) {
                    canvas.drawLine(0, a, tileSize, a, mLoadingPaint);
                    canvas.drawLine(a, 0, a, tileSize, mLoadingPaint);
                }
                mLoadingTile = new BitmapDrawable(bitmap);
            } catch (final OutOfMemoryError e) {
                Log.e(TAG, "OutOfMemoryError getting loading tile");
                System.gc();
            }
        }
        return mLoadingTile;
    }

    private void clearLoadingTile() {
        final BitmapDrawable bitmapDrawable = mLoadingTile;
        mLoadingTile = null;
        // Only recycle if we are running on a project less than 2.3.3 Gingerbread.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (bitmapDrawable != null) {
                bitmapDrawable.getBitmap().recycle();
            }
        }
    }

    /**
     * Recreate the cache using scaled versions of the tiles currently in it
     *
     * @param pNewZoomLevel the zoom level that we need now
     * @param pOldZoomLevel the previous zoom level that we should get the tiles to rescale
     * @param projection    the projection to compute view port
     */
    public void rescaleCache(final float pNewZoomLevel, final float pOldZoomLevel, final Projection projection) {

        if (mTileProvider.hasNoSource() || Math.floor(pNewZoomLevel) == Math.floor(pOldZoomLevel)) {
            return;
        }

        final long startMs = System.currentTimeMillis();

        if (UtilConstants.DEBUGMODE) {
            Log.d(TAG, "rescale tile cache from " + pOldZoomLevel + " to " + pNewZoomLevel);
        }

        final int tileSize = Projection.getTileSize();
        final Rect viewPort = GeometryMath.viewPortRectForTileDrawing(pNewZoomLevel, projection, null);

        final ScaleTileLooper tileLooper = pNewZoomLevel > pOldZoomLevel
                ? new ZoomInTileLooper(pOldZoomLevel)
                : new ZoomOutTileLooper(pOldZoomLevel);
        tileLooper.loop(null, mTileProvider.getCacheKey(), pNewZoomLevel, tileSize, viewPort, null);

        final long endMs = System.currentTimeMillis();
        if (UtilConstants.DEBUGMODE) {
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
        public void handleTile(final Canvas pCanvas, final String pCacheKey, final int pTileSizePx, final MapTile pTile, final int pX, final int pY, final Rect pClipRect) {

            // Get tile from cache.
            // If it's found then no need to created scaled version.
            // If not found (null) them we've initiated a new request for it,
            // and now we'll create a scaled version until the request completes.
            final Drawable requestedTile = mTileProvider.getMapTileFromMemory(pTile);
            if (requestedTile == null) {
                try {
                    handleScaleTile(pCacheKey, pTileSizePx, pTile, pX, pY);
                } catch (final OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError rescaling cache");
                }
            }
        }

        @Override
        public void finalizeLoop() {
            super.finalizeLoop();
            // now add the new ones, pushing out the old ones
            while (!mNewTiles.isEmpty()) {

                final MapTile tile = mNewTiles.keySet().iterator().next();
                final Bitmap bitmap = mNewTiles.remove(tile);
                mTileProvider.putExpiredTileIntoCache(tile, bitmap);
            }
        }

        protected abstract void handleScaleTile(final String pCacheKey, final int pTileSizePx, final MapTile pTile, final int pX, final int pY);
    }

    private class ZoomInTileLooper extends ScaleTileLooper {
        public ZoomInTileLooper(final float pOldZoomLevel) {
            super(pOldZoomLevel);
        }

        @Override
        public void handleScaleTile(final String pCacheKey, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

            // get the correct fraction of the tile from cache and scale up
            final MapTile oldTile = new MapTile(pCacheKey,
                    (int) Math.floor(mOldZoomLevel),
                    (int) GeometryMath.rightShift(pX, mDiff),
                    (int) GeometryMath.rightShift(pY, mDiff));
            final Drawable oldDrawable = mTileProvider.getMapTileFromMemory(oldTile);

            if (oldDrawable instanceof BitmapDrawable) {
                final int xx = (pX % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                final int yy = (pY % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
                mDestRect.set(0, 0, pTileSizePx, pTileSizePx);

                // Try to get a bitmap from the pool, otherwise allocate a new one
                Bitmap bitmap = mTileProvider.getBitmapFromRemoved(pTileSizePx,
                        pTileSizePx);

                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx,
                            Bitmap.Config.ARGB_8888);
                }

                final Canvas canvas = new Canvas(bitmap);
                final boolean isReusable = oldDrawable instanceof CacheableBitmapDrawable;
                boolean success = false;
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
        protected void handleScaleTile(final String pCacheKey, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {

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
                    final MapTile oldTile = new MapTile(mTileProvider.getCacheKey(),
                            (int) Math.floor(mOldZoomLevel),
                            xx + x,
                            yy + y);
                    Drawable oldDrawable = mTileProvider.getMapTileFromMemory(oldTile);
                    if (oldDrawable == null) {
                        oldDrawable = getLoadingTile();
                    }

                    if (oldDrawable instanceof BitmapDrawable) {
                        final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                        if (oldBitmap != null) {
                            if (bitmap == null) {
                                // Try to get a bitmap from the pool, otherwise allocate a new one
                                bitmap = mTileProvider.getBitmapFromRemoved(
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
                            mTileProvider.removeTileFromMemory(oldTile);
                        }
                    }
                    if (oldDrawable instanceof CacheableBitmapDrawable) {
                        ((CacheableBitmapDrawable) oldDrawable).setBeingUsed(false);
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

    private static final String TAG = "TilesOverlay";
}
