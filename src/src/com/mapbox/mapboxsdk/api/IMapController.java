package com.mapbox.mapboxsdk.api;

import com.mapbox.mapboxsdk.views.MapController;

/**
 * An interface that resembles the Google Maps API MapController class and is implemented by the
 * osmdroid {@link MapController} class.
 *
 * @author Neil Boyd
 */
public interface IMapController {
    void animateTo(ILatLng geoPoint);

    void scrollBy(int x, int y);

    void setCenter(ILatLng point);

    int setZoom(int zoomLevel);

    void stopAnimation(boolean jumpToFinish);

    void stopPanning();

    boolean zoomIn();

    boolean zoomInFixing(int xPixel, int yPixel);

    boolean zoomOut();

    boolean zoomOutFixing(int xPixel, int yPixel);

    void zoomToSpan(double latSpan, double lonSpan);
}
