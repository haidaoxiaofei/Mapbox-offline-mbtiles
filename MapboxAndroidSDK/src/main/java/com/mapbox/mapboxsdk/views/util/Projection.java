/**
 * A Projection serves to translate between the coordinate system of x/y on-screen pixel
 * coordinates and that of latitude/longitude points on the surface of the earth. You obtain a
 * Projection from MapView.getProjection(). You should not hold on to this object for more than
 * one draw, since the projection of the map could change. <br />
 * <br />
 * <I>Screen coordinates</I> are in the coordinate system of the screen's Canvas. The origin is
 * in the center of the plane. <I>Screen coordinates</I> are appropriate for using to draw to
 * the screen.<br />
 * <br />
 * <I>Map coordinates</I> are in the coordinate system of the standard Mercator projection. The
 * origin is in the upper-left corner of the plane. <I>Map coordinates</I> are appropriate for
 * use in the Projection class.<br />
 * <br />
 * <I>Intermediate coordinates</I> are used to cache the computationally heavy part of the
 * projection. They aren't suitable for use until translated into <I>screen coordinates</I> or
 * <I>map coordinates</I>.
 *
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */

package com.mapbox.mapboxsdk.views.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.GeoConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.views.MapView;

public class Projection implements GeoConstants {
    private MapView mapView = null;

    private int viewWidth2;
    private int viewHeight2;
    private int worldSize2;
    private final int offsetX;
    private final int offsetY;

    private BoundingBox mBoundingBoxProjection;
    private final float mZoomLevelProjection;
    private final Rect mScreenRectProjection;
    private final Rect mIntrinsicScreenRectProjection;
    private final float mMapOrientation;
    protected static int mTileSize = 256;

    public Projection(final MapView mv) {
        super();
        this.mapView = mv;

        viewWidth2 = mapView.getMeasuredWidth() >> 1;
        viewHeight2 = mapView.getMeasuredHeight() >> 1;
        mZoomLevelProjection = mapView.getZoomLevel(false);
        worldSize2 = this.mapSize(mZoomLevelProjection) >> 1;

        offsetX = -worldSize2;
        offsetY = -worldSize2;

        //TODO: optimize because right now each line re-compute the previous value
        mIntrinsicScreenRectProjection = mapView.getIntrinsicScreenRect(null);
        mScreenRectProjection = mapView.getScreenRect(null);
        mMapOrientation = mapView.getMapOrientation();
    }

    public float getZoomLevel() {
        return mZoomLevelProjection;
    }

    public int getHalfWorldSize() {
        return worldSize2;
    }

    public BoundingBox getBoundingBox() {
    	if (mBoundingBoxProjection == null) {
    		mBoundingBoxProjection = mapView.getBoundingBoxInternal();
    	}
        return mBoundingBoxProjection;
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public Rect getIntrinsicScreenRect() {
        return mIntrinsicScreenRectProjection;
    }

    public float getMapOrientation() {
        return mMapOrientation;
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @param x
     * @param y
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final float x, final float y) {
        final Rect screenRect = getIntrinsicScreenRect();
        return this.pixelXYToLatLong(screenRect.left + (int) x + worldSize2,
                screenRect.top + (int) y + worldSize2, mZoomLevelProjection);
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @param x
     * @param y
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final int x, final int y) {
        return fromPixels((float) x, (float) y);
    }

    /**
     * Converts from map pixels to a Point value. Optionally reuses an existing Point.
     *
     * @param x
     * @param y
     * @param reuse
     * @return
     */
    public Point fromMapPixels(final int x, final int y, final Point reuse) {
        final Point out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new Point();
        }
        out.set(x - viewWidth2, y - viewHeight2);
        out.offset(mapView.getScrollX(), mapView.getScrollY());
        return out;
    }

    /**
     * Converts a LatLng to its <I>screen coordinates</I>.
     *
     * @param in    the LatLng you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>screen coordinates</I> of the LatLng passed.
     */
    public PointF toPixels(final ILatLng in, final PointF reuse) {
        PointF result = toMapPixels(in, reuse);
        result.offset(-mScreenRectProjection.left, (-mScreenRectProjection.top));
        return result;
    }

    /**
     * Converts a LatLng to its <I>Map coordinates</I> in pixels for the current zoom.
     *
     * @param in    the LatLng you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Map coordinates</I> of the LatLng passed.
     */
    public PointF toMapPixels(final ILatLng in, final PointF reuse) {
        return toMapPixels(in.getLatitude(), in.getLongitude(), reuse);
    }

    public PointF toMapPixels(final double latitude, final double longitude, final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }
        final float zoom = getZoomLevel();
        final int mapSize = this.mapSize(zoom);
        final float scrollX = mapView.getScrollX();
        final float scrollY = mapView.getScrollY();
        this.latLongToPixelXY(
                latitude,
                longitude,
                zoom, out);
        out.offset(offsetX, offsetY);
        if (Math.abs(out.x - scrollX)
                > Math.abs(out.x - mapSize - scrollX)) {
            out.x -= mapSize;
        }
        if (Math.abs(out.x - scrollX)
                > Math.abs(out.x + mapSize - scrollX)) {
            out.x += mapSize;
        }
        if (Math.abs(out.y - scrollY)
                > Math.abs(out.y - mapSize - scrollY)) {
            out.y -= mapSize;
        }
        if (Math.abs(out.y - scrollY)
                > Math.abs(out.y + mapSize - scrollY)) {
            out.y += mapSize;
        }
        return out;
    }

    public static RectF toMapPixels(final BoundingBox box, final float zoom, final RectF reuse) {
        final RectF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new RectF();
        }
        final int mapSize_2 = mapSize(zoom)/2;
        PointF nw = latLongToPixelXY(
                box.getLatNorth(),
                box.getLonWest(),
                zoom, null);
        PointF se = latLongToPixelXY(
                box.getLatSouth(),
                box.getLonEast(),
                zoom, null);
        out.set(nw.x, nw.y, se.x, se.y);
        out.offset(-mapSize_2, -mapSize_2);
        return out;
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * toMapPixelsTranslated to get the final position.
     *
     * @param latitude  the latitude of the point
     * @param longitude the longitude of the point
     * @param reuse     just pass null if you do not have a Point to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public PointF toMapPixelsProjected(final double latitude, final double longitude,
                                       final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }
        this.latLongToPixelXY(latitude, longitude, TileLayerConstants.MAXIMUM_ZOOMLEVEL, out);
        return out;
    }

    /**
     * Performs the second computationally light part of the projection. Returns results in
     * <I>screen coordinates</I>.
     *
     * @param in    the Point calculated by the toMapPixelsProjected
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Screen coordinates</I> of the initial LatLng passed
     * to the toMapPixelsProjected.
     */
    public PointF toMapPixelsTranslated(final PointF in, final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();
        out.set((int) (GeometryMath.rightShift(in.x, zoomDifference) + offsetX),
                (int) (GeometryMath.rightShift(in.y, zoomDifference) + offsetY));
        return out;
    }

    /**
     * Translates a rectangle from <I>screen coordinates</I> to <I>intermediate coordinates</I>.
     *
     * @param in the rectangle in <I>screen coordinates</I>
     * @return a rectangle in </I>intermediate coordindates</I>.
     */
    public Rect fromPixelsToProjected(final Rect in) {
        final Rect result = new Rect();

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();

        final int x0 = (int) GeometryMath.leftShift(in.left - offsetX, zoomDifference);
        final int x1 = (int) GeometryMath.leftShift(in.right - offsetX, zoomDifference);
        final int y0 = (int) GeometryMath.leftShift(in.bottom - offsetY, zoomDifference);
        final int y1 = (int) GeometryMath.leftShift(in.top - offsetY, zoomDifference);

        result.set(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
        return result;
    }

    public static void setTileSize(final int tileSize) {
        mTileSize = tileSize;
    }

    public static int getTileSize() {
        return mTileSize;
    }

    /**
     * Clips a number to the specified minimum and maximum values.
     *
     * @param n        The number to clip
     * @param minValue Minimum allowable value
     * @param maxValue Maximum allowable value
     * @return The clipped value.
     */
    private static double clip(final double n, final double minValue, final double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /**
     * Determines the map width and height (in pixels) at a specified level of detail.
     *
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The map width and height in pixels
     */
    public static int mapSize(final float levelOfDetail) {
        return (int) (GeometryMath.leftShift(mTileSize, levelOfDetail));
    }

    /**
     * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
     * detail.
     *
     * @param latitude      Latitude (in degrees) at which to measure the ground resolution
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The ground resolution, in meters per pixel
     */
    public static double groundResolution(double latitude, final float levelOfDetail) {
        latitude = wrap(latitude, -90, 90, 180);
        latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * RADIUS_EARTH_METERS
                / mapSize(levelOfDetail);
    }

    /**
     * Determines the map scale at a specified latitude, level of detail, and screen resolution.
     *
     * @param latitude      Latitude (in degrees) at which to measure the map scale
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param screenDpi     Resolution of the screen, in dots per inch
     * @return The map scale, expressed as the denominator N of the ratio 1 : N
     */
    public static double mapScale(final double latitude, final int levelOfDetail,
                                  final int screenDpi) {
        return groundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
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
    public static PointF latLongToPixelXY(double latitude, double longitude,
                                          final float levelOfDetail, final PointF reuse) {
        latitude = wrap(latitude, -90, 90, 180);
        longitude = wrap(longitude, -180, 180, 360);
        final PointF out = (reuse == null ? new PointF() : reuse);

        latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        final double x = (longitude + 180) / 360;
        final double sinLatitude = Math.sin(latitude * Math.PI / 180);
        final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        final int mapSize = mapSize(levelOfDetail);
        out.x = (float) clip(x * mapSize + 0.5, 0, mapSize - 1);
        out.y = (float) clip(y * mapSize + 0.5, 0, mapSize - 1);
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
    public static LatLng pixelXYToLatLong(int pixelX, int pixelY,
                                          final float levelOfDetail) {
        final int mapSize = mapSize(levelOfDetail);

        pixelX = (int) wrap(pixelX, 0, mapSize - 1, mapSize);
        pixelY = (int) wrap(pixelY, 0, mapSize - 1, mapSize);

        final double x = (clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        final double y = 0.5 - (clip(pixelY, 0, mapSize - 1) / mapSize);

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
    public static Point pixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
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
    public static Point tileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
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
     * subtracting/adding <code>interval</code>
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



    private static final String TAG = "Projection";
}
