package com.mapbox.mapboxsdk.api;

import com.mapbox.mapboxsdk.views.MapController;

/**
 * An interface that resembles the Google Maps API MapView class
 * and is implemented by the osmdroid {@link MapView} class.
 *
 * @author Neil Boyd
 */
public interface IMapView {

    MapController getController();

    IProjection getProjection();

    int getZoomLevel();

    int getMaxZoomLevel();

    double getLatitudeSpan();

    double getLongitudeSpan();

    ILatLng getCenter();

    // some methods from View
    // (well, just one for now)
    void setBackgroundColor(int color);

}
