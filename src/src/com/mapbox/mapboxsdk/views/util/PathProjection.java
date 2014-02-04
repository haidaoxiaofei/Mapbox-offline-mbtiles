package com.mapbox.mapboxsdk.views.util;

import java.util.List;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.TileSystem;
import com.mapbox.mapboxsdk.views.MapView.Projection;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class PathProjection {

    public static Path toPixels(Projection projection, final List<? extends LatLng> in,
                                final Path reuse) {
        return toPixels(projection, in, reuse, true);
    }

    public static Path toPixels(Projection projection, final List<? extends LatLng> in,
                                final Path reuse, final boolean doGudermann) throws IllegalArgumentException {
        if (in.size() < 2) {
            throw new IllegalArgumentException("List of GeoPoints needs to be at least 2.");
        }

        final Path out = (reuse != null) ? reuse : new Path();
        out.incReserve(in.size());

        boolean first = true;
        for (final LatLng gp : in) {
            final Point underGeopointTileCoords = TileSystem.LatLongToPixelXY(
                    gp.getLatitude(), gp.getLongitude(), projection.getZoomLevel(),
                    null);
            TileSystem.PixelXYToTileXY(underGeopointTileCoords.x, underGeopointTileCoords.y,
                    underGeopointTileCoords);

			/*
             * Calculate the Latitude/Longitude on the left-upper ScreenCoords of the MapTile.
			 */
            final Point upperRight = TileSystem.TileXYToPixelXY(underGeopointTileCoords.x,
                    underGeopointTileCoords.y, null);
            final Point lowerLeft = TileSystem.TileXYToPixelXY(underGeopointTileCoords.x
                    + TileSystem.getTileSize(),
                    underGeopointTileCoords.y + TileSystem.getTileSize(), null);
            final LatLng neLatLng = TileSystem.PixelXYToLatLong(upperRight.x, upperRight.y,
                    projection.getZoomLevel());
            final LatLng swLatLng = TileSystem.PixelXYToLatLong(lowerLeft.x, lowerLeft.y,
                    projection.getZoomLevel());
            final BoundingBox bb = new BoundingBox(neLatLng.getLatitude(),
                    neLatLng.getLongitude(), swLatLng.getLatitude(),
                    swLatLng.getLongitude());

            final PointF relativePositionInCenterMapTile;
            if (doGudermann && (projection.getZoomLevel() < 7)) {
                relativePositionInCenterMapTile = bb
                        .getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
                                gp.getLatitude(), gp.getLongitude(), null);
            } else {
                relativePositionInCenterMapTile = bb
                        .getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
                                gp.getLatitude(), gp.getLongitude(), null);
            }

            final Rect screenRect = projection.getScreenRect();
            Point centerMapTileCoords = TileSystem.PixelXYToTileXY(screenRect.centerX(),
                    screenRect.centerY(), null);
            final Point upperLeftCornerOfCenterMapTile = TileSystem.TileXYToPixelXY(
                    centerMapTileCoords.x, centerMapTileCoords.y, null);
            final int tileDiffX = centerMapTileCoords.x - underGeopointTileCoords.x;
            final int tileDiffY = centerMapTileCoords.y - underGeopointTileCoords.y;
            final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
                    - (TileSystem.getTileSize() * tileDiffX);
            final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
                    - (TileSystem.getTileSize() * tileDiffY);

            final int x = underGeopointTileScreenLeft
                    + (int) (relativePositionInCenterMapTile.x * TileSystem.getTileSize());
            final int y = underGeopointTileScreenTop
                    + (int) (relativePositionInCenterMapTile.y * TileSystem.getTileSize());

			/* Add up the offset caused by touch. */
            if (first) {
                out.moveTo(x, y);
                // out.moveTo(x + MapView.this.mTouchMapOffsetX, y +
                // MapView.this.mTouchMapOffsetY);
            } else {
                out.lineTo(x, y);
            }
            first = false;
        }

        return out;
    }
}
