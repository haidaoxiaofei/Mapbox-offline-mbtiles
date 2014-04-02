package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
import com.mapbox.mapboxsdk.views.util.Projection;

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
    protected static Paint mDebugPaint = null;
    private final Rect mTileRect = new Rect();
    private final Rect mViewPort = new Rect();
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
                mDebugPaint = new Paint();
                mDebugPaint.setColor(Color.RED);
                mDebugPaint.setStyle(Style.STROKE);
                mDebugPaint.setStrokeWidth(2);
            }
        }
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
        final float zoomLevel = pj.getZoomLevel();
        mWorldSize_2 = pj.getHalfWorldSize();
        GeometryMath.viewPortRectForTileDrawing(pj, mViewPort);

        // Draw the tiles!
        drawTiles(c.getSafeCanvas(), zoomLevel, Projection.getTileSize(), mViewPort);
    }

    /**
     * This is meant to be a "pure" tile drawing function that doesn't take into account
     * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
     * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
     * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
     */
    public void drawTiles(final Canvas c, final float zoomLevel, final int tileSizePx,
                          final Rect viewPort) {

        mTileLooper.loop(c, zoomLevel, tileSizePx, viewPort);

        // draw a cross at center in debug mode
        if (UtilConstants.DEBUGMODE) {
            final Point centerPoint = new Point(viewPort.centerX() - mWorldSize_2,
                    viewPort.centerY() - mWorldSize_2);
            c.drawLine(centerPoint.x, centerPoint.y - 9,
                    centerPoint.x, centerPoint.y + 9, mDebugPaint);
            c.drawLine(centerPoint.x - 9, centerPoint.y,
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
                               final int pTileSizePx,
                               final MapTile pTile,
                               final int pX,
                               final int pY) {
            Drawable currentMapTile = mTileProvider.getMapTile(pTile);
            boolean isReusable = currentMapTile instanceof CacheableBitmapDrawable;
            if (currentMapTile == null) {
                currentMapTile = getLoadingTile();
            }

            if (currentMapTile != null) {
                mTileRect.set(
                        (int) (pX * pTileSizePx * mCurrentZoomFactor),
                        (int) (pY * pTileSizePx * mCurrentZoomFactor),
                        (int) ((pX * pTileSizePx + pTileSizePx) * mCurrentZoomFactor),
                        (int) ((pY * pTileSizePx + pTileSizePx) * mCurrentZoomFactor));
                if (isReusable) {
                    ((CacheableBitmapDrawable) currentMapTile).setBeingUsed(true);
                }
                try {
                    if (isReusable && !((CacheableBitmapDrawable) currentMapTile).isBitmapValid()) {
                        currentMapTile = getLoadingTile();
                        isReusable = false;
                    }
                    mTileRect.offset(-mWorldSize_2, -mWorldSize_2);
                    currentMapTile.setBounds(mTileRect);
                    currentMapTile.draw(pCanvas);
                } finally {
                    if (isReusable) {
                        ((CacheableBitmapDrawable) currentMapTile).setBeingUsed(false);
                    }
                }
                if (UtilConstants.DEBUGMODE) {
                    pCanvas.drawText(pTile.toString(), mTileRect.left + 1,
                            mTileRect.top + mDebugPaint.getTextSize(), mDebugPaint);
                    pCanvas.drawRect(mTileRect, mDebugPaint);
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
                final Paint paint = new Paint();
                canvas.drawColor(mLoadingBackgroundColor);
                paint.setColor(mLoadingLineColor);
                paint.setStrokeWidth(0);
                final int lineSize = tileSize / 16;
                for (int a = 0; a < tileSize; a += lineSize) {
                    canvas.drawLine(0, a, tileSize, a, paint);
                    canvas.drawLine(a, 0, a, tileSize, paint);
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

    private static final String TAG = "TilesOverlay";
}
