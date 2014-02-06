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
 * use in the TileSystem class.<br />
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
import android.graphics.Rect;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.api.IProjection;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.GeoConstants;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.views.MapView;

public class Projection implements IProjection, GeoConstants {
    private MapView mapView = null;

    private int viewWidth_2;
    private int viewHeight_2;
    private int worldSize_2;
    private final int offsetX = -worldSize_2;
    private final int offsetY = -worldSize_2;

    private final BoundingBox mBoundingBoxProjection;
    private final int mZoomLevelProjection;
    private final Rect mScreenRectProjection;
    private final Rect mIntrinsicScreenRectProjection;

    public Projection(MapView mv) {
        super();
        this.mapView = mv;

	    /*
         * Do some calculations and drag attributes to local variables to save some performance.
		 */
        mZoomLevelProjection = mapView.getZoomLevel();
        mBoundingBoxProjection = mapView.getBoundingBox();
        mScreenRectProjection = mapView.getScreenRect(null);
        mIntrinsicScreenRectProjection = mapView.getIntrinsicScreenRect(null);
    }

    private int worldSize_2() {
        return TileSystem.MapSize(mapView.getZoomLevel() / 2);
    }

    private int viewHeight_2() {
        return mapView.getHeight() / 2;
    }

    private int viewWidth_2() {
        return mapView.getWidth() / 2;
    }

    public int getZoomLevel() {
        return mZoomLevelProjection;
    }

    public BoundingBox getBoundingBox() {
        return mBoundingBoxProjection;
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public Rect getIntrinsicScreenRect() {
        return mIntrinsicScreenRectProjection;
    }

    public float getMapOrientation() {
        return mapView.getMapOrientation();
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
        return TileSystem.PixelXYToLatLong(screenRect.left + (int) x + worldSize_2(),
                screenRect.top + (int) y + worldSize_2(), mZoomLevelProjection);
    }

    public Point fromMapPixels(final int x, final int y, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        out.set(x - viewWidth_2(), y - viewHeight_2());
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
    public Point toMapPixels(final ILatLng in, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        TileSystem.LatLongToPixelXY(
                in.getLatitude(),
                in.getLongitude(),
                getZoomLevel(), out);
        out.offset(offsetX, offsetY);
        if (Math.abs(out.x - mapView.getScrollX())
                > Math.abs(out.x - TileSystem.MapSize(getZoomLevel()) - mapView.getScrollX())) {
            out.x -= TileSystem.MapSize(getZoomLevel());
        }
        if (Math.abs(out.x - mapView.getScrollX())
                > Math.abs(out.x + TileSystem.MapSize(getZoomLevel()) - mapView.getScrollX())) {
            out.x += TileSystem.MapSize(getZoomLevel());
        }
        if (Math.abs(out.y - mapView.getScrollY())
                > Math.abs(out.y - TileSystem.MapSize(getZoomLevel()) - mapView.getScrollY())) {
            out.y -= TileSystem.MapSize(getZoomLevel());
        }
        if (Math.abs(out.y - mapView.getScrollY())
                > Math.abs(out.y + TileSystem.MapSize(getZoomLevel()) - mapView.getScrollY())) {
            out.y += TileSystem.MapSize(getZoomLevel());
        }
        return out;
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * toMapPixelsTranslated to get the final position.
     *
     * @param latitude  the latitude of the point
     * @param longitude the longitude of the point
     * @param reuse       just pass null if you do not have a Point to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public Point toMapPixelsProjected(final double latitude, final double longitude,
                                      final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();

        TileSystem
                .LatLongToPixelXY(latitude, longitude, MapView.MAXIMUM_ZOOMLEVEL, out);
        return out;
    }

    /**
     * Performs the second computationally light part of the projection. Returns results in
     * <I>screen coordinates</I>.
     *
     * @param in    the Point calculated by the toMapPixelsProjected
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Screen coordinates</I> of the initial LatLng passed
     *         to the toMapPixelsProjected.
     */
    public Point toMapPixelsTranslated(final Point in, final Point reuse) {
        final Point out = reuse != null ? reuse : new Point();

        final int zoomDifference = MapView.MAXIMUM_ZOOMLEVEL - getZoomLevel();
        out.set((in.x >> zoomDifference) + offsetX, (in.y >> zoomDifference) + offsetY);
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

        final int zoomDifference = MapView.MAXIMUM_ZOOMLEVEL - getZoomLevel();

        final int x0 = in.left - offsetX << zoomDifference;
        final int x1 = in.right - offsetX << zoomDifference;
        final int y0 = in.bottom - offsetY << zoomDifference;
        final int y1 = in.top - offsetY << zoomDifference;

        result.set(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
        return result;
    }

    @Override
    public float metersToEquatorPixels(final float meters) {
        return meters / (float) TileSystem.GroundResolution(0, mZoomLevelProjection);
    }

    @Override
    public ILatLng getNorthEast() {
        return fromPixels(mapView.getWidth(), 0);
    }

    @Override
    public ILatLng getSouthWest() {
        return fromPixels(0, mapView.getHeight());
    }

    @Override
    public Point toPixels(final ILatLng in, final Point out) {
        return toMapPixels(in, out);
    }

    @Override
    public ILatLng fromPixels(final int x, final int y) {
        return fromPixels((float) x, (float) y);
    }
}
