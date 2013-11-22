package com.mapbox.mapboxsdk;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class Marker extends OverlayItem{
    public Marker(String aTitle, String aSnippet, GeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);
//        Drawable markerDrawable = this.getResources().getDrawable(R.drawable.pin);
//        this.setMarker(markerDrawable);
    }

    public Marker(String aUid, String aTitle, String aDescription, GeoPoint aGeoPoint) {
        super(aUid, aTitle, aDescription, aGeoPoint);
    }
}
