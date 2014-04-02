package com.mapbox.mapboxsdk.util;

import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.tileprovider.MapTile;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

    protected final Point mUpperLeft = new Point();
    protected final Point mLowerRight = new Point();
    protected final Point center = new Point();

    public final void loop(final Canvas pCanvas, final float pZoomLevel, final int pTileSizePx, final Rect pViewPort) {
        // Calculate the amount of tiles needed for each side around the center one.
        Projection.pixelXYToTileXY(pViewPort.left, pViewPort.top, mUpperLeft);
        mUpperLeft.offset(-1, -1);

        Projection.pixelXYToTileXY(pViewPort.right, pViewPort.bottom, mLowerRight);
        mLowerRight.offset(1, 1);

        center.set((mUpperLeft.x + mLowerRight.x) / 2, (mUpperLeft.y + mLowerRight.y) / 2);

        final int roundedZoom = (int) Math.floor(pZoomLevel);
        final int mapTileUpperBound = 1 << roundedZoom;
        initializeLoop(pZoomLevel, pTileSizePx);

        int tileX, tileY;

        for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
            for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
                tileY = GeometryMath.mod(y, mapTileUpperBound);
                tileX = GeometryMath.mod(x, mapTileUpperBound);
                final MapTile tile = new MapTile(roundedZoom, tileX, tileY);
                handleTile(pCanvas, pTileSizePx, tile, x, y);
            }
        }
    }

    public abstract void initializeLoop(float pZoomLevel, int pTileSizePx);

    public abstract void handleTile(Canvas pCanvas, int pTileSizePx, MapTile pTile, int pX, int pY);
}
