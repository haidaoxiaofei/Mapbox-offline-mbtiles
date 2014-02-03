package com.mapbox.mapboxsdk.api;

/**
 * An interface that resembles the Google Maps API LatLng class.
 */
public interface ILatLng {
    int getLatitudeE6();

    int getLongitudeE6();

    double getLatitude();

    double getLongitude();
}
