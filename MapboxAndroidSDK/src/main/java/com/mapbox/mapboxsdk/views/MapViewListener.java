package com.mapbox.mapboxsdk.views;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.overlay.Marker;


public class MapViewListener {
    public void willShowMarker(final MapView pMapView, final Marker pMarker){};
    public void willHideMarker(final MapView pMapView, final Marker pMarker){};
    public void tapOnMarker(final MapView pMapView, final Marker pMarker){};
    public void longpressOnMarker(final MapView pMapView, final Marker pMarker){};
    public void tapOnMap(final MapView pMapView, final ILatLng pPosition){};
    public void longpressOnMap(final MapView pMapView, final ILatLng pPosition){};
}
