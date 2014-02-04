package com.mapbox.mapboxsdk;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by tmcw on 1/28/14.
 */
public class DistanceGrid {
    private int cellSize;

    public void addObject(Marker marker, LatLng point) {
        int x = getCoord(point.getLongitude());
        int y = getCoord(point.getLatitude());
    }

    private int getCoord(double x) {
        return (int) (x / cellSize);
    }
}