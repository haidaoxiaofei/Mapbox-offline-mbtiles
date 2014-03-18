package com.mapbox.mapboxsdk.api;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * An interface that resembles the Google Maps API Projection interface and is implemented by the
 * osmdroid {@link Projection} class.
 *
 * @author Neil Boyd
 */
public interface IProjection {

    /**
     * Converts the given LatLng to onscreen pixel coordinates, relative to the top-left of the
     * MapView that provided this Projection.
     *
     * @param in  The latitude/longitude pair to convert.
     * @param out A pre-existing object to use for the output; if null, a new Point will be
     *            allocated and returned.
     */
    PointF toPixels(ILatLng in, PointF out);

    /**
     * Create a new LatLng from pixel coordinates relative to the top-left of the MapView that
     * provided this PixelConverter.
     */
    ILatLng fromPixels(int x, int y);

    /**
     * Converts a distance in meters (along the equator) to one in (horizontal) pixels at the
     * current zoomlevel. In the default Mercator projection, the actual number of pixels for a
     * given distance will get higher as you move away from the equator.
     *
     * @param meters the distance in meters
     * @return The number of pixels corresponding to the distance, if measured along the equator, at
     *         the current zoom level. The return value may only be approximate.
     */
    float metersToEquatorPixels(float meters);

    /**
     * Get the coordinates of the most north-easterly visible point of the map.
     */
    ILatLng getNorthEast();

    /**
     * Get the coordinates of the most south-westerly visible point of the map.
     */
    ILatLng getSouthWest();

}
