package com.mapbox.mapboxsdk.tile;

import android.graphics.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.GeoConstants;
import com.mapbox.mapboxsdk.util.GeometryMath;

public final class TileSystem implements GeoConstants {

    protected static int mTileSize = 256;

    public static void setTileSize(final int tileSize) {
        mTileSize = tileSize;
    }

    public static int getTileSize() {
        return mTileSize;
    }

    /**
     * Clips a number to the specified minimum and maximum values.
     *
     * @param n The number to clip
     * @param minValue Minimum allowable value
     * @param maxValue Maximum allowable value
     * @return The clipped value.
     */
    private static double Clip(final double n, final double minValue, final double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /**
     * Determines the map width and height (in pixels) at a specified level of detail.
     *
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The map width and height in pixels
     */
    public static int MapSize(final float levelOfDetail) {
        return (int)(GeometryMath.leftShift(mTileSize,levelOfDetail));
    }

    /**
     * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
     * detail.
     *
     * @param latitude      Latitude (in degrees) at which to measure the ground resolution
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The ground resolution, in meters per pixel
     */
    public static double GroundResolution(double latitude, final float levelOfDetail) {
        latitude = wrap(latitude, -90, 90, 180);
        latitude = Clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * RADIUS_EARTH_METERS
                / MapSize(levelOfDetail);
    }

    /**
     * Determines the map scale at a specified latitude, level of detail, and screen resolution.
     *
     * @param latitude      Latitude (in degrees) at which to measure the map scale
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param screenDpi     Resolution of the screen, in dots per inch
     * @return The map scale, expressed as the denominator N of the ratio 1 : N
     */
    public static double MapScale(final double latitude, final int levelOfDetail,
                                  final int screenDpi) {
        return GroundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
    }


    /**
     * Converts a point from latitude/longitude WGS-84 coordinates (in degrees) into pixel XY
     * coordinates at a specified level of detail.
     *
     * @param latitude      Latitude of the point, in degrees
     * @param longitude     Longitude of the point, in degrees
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param reuse         An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the X and Y coordinates in pixels
     */
    public static Point LatLongToPixelXY(double latitude, double longitude,
                                         final float levelOfDetail, final Point reuse) {
        latitude = wrap(latitude, -90, 90, 180);
        longitude = wrap(longitude, -180, 180, 360);
        final Point out = (reuse == null ? new Point() : reuse);

        latitude = Clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = Clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        final double x = (longitude + 180) / 360;
        final double sinLatitude = Math.sin(latitude * Math.PI / 180);
        final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        final int mapSize = MapSize(levelOfDetail);
        out.x = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
        out.y = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
        return out;
    }

    /**
     * Converts a pixel from pixel XY coordinates at a specified level of detail into
     * latitude/longitude WGS-84 coordinates (in degrees).
     *
     * @param pixelX        X coordinate of the point, in pixels
     * @param pixelY        Y coordinate of the point, in pixels
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return Output parameter receiving the latitude and longitude in degrees.
     */
    public static LatLng PixelXYToLatLong(int pixelX, int pixelY,
                                          final float levelOfDetail) {
        final int mapSize = MapSize(levelOfDetail);

        pixelX = (int) wrap(pixelX, 0, mapSize - 1, mapSize);
        pixelY = (int) wrap(pixelY, 0, mapSize - 1, mapSize);

        final double x = (Clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        final double y = 0.5 - (Clip(pixelY, 0, mapSize - 1) / mapSize);

        final double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        final double longitude = 360 * x;

        return new LatLng(latitude, longitude);
    }

    /**
     * Converts pixel XY coordinates into tile XY coordinates of the tile containing the specified
     * pixel.
     *
     * @param pixelX Pixel X coordinate
     * @param pixelY Pixel Y coordinate
     * @param reuse  An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the tile X and Y coordinates
     */
    public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = pixelX / mTileSize;
        out.y = pixelY / mTileSize;
        return out;
    }

    /**
     * Converts tile XY coordinates into pixel XY coordinates of the upper-left pixel of the
     * specified tile.
     *
     * @param tileX Tile X coordinate
     * @param tileY Tile X coordinate
     * @param reuse An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the pixel X and Y coordinates
     */
    public static Point TileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = tileX * mTileSize;
        out.y = tileY * mTileSize;
        return out;
    }

    /**
     * Returns a value that lies within <code>minValue</code> and <code>maxValue</code> by
     * subtracting/adding <code>interval</code>.
     *
     * @param n        the input number
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param interval the interval length
     * @return a value that lies within <code>minValue</code> and <code>maxValue</code> by
     *         subtracting/adding <code>interval</code>
     */
    private static double wrap(double n, final double minValue, final double maxValue, final double interval) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue must be smaller than maxValue: "
                    + minValue + ">" + maxValue);
        }
        if (interval > maxValue - minValue + 1) {
            throw new IllegalArgumentException(
                    "interval must be equal or smaller than maxValue-minValue: " + "min: "
                            + minValue + " max:" + maxValue + " int:" + interval);
        }
        while (n < minValue) {
            n += interval;
        }
        while (n > maxValue) {
            n -= interval;
        }
        return n;
    }
}
