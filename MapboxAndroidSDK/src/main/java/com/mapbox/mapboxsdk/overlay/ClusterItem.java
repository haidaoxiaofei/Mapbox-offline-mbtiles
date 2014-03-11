package com.mapbox.mapboxsdk.overlay;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by Francisco Dans on 06/03/14.
 */
public class ClusterItem extends Marker{
    private int childCount = 0;

    public ClusterItem(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(mv, aTitle, aDescription, aLatLng);
    }

    public ClusterItem(String aTitle, String aSnippet, LatLng aLatLng) {
        super(aTitle, aSnippet, aLatLng);
    }

    public ClusterItem(MapView view, LatLng result) {
        super(view, "", "", result);
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}
