package com.mapbox.mapboxsdk;

import com.mapbox.mapboxsdk.util.GeoPoint;

/**
 * Created by tmcw on 1/28/14.
 */
public class DistanceGrid {
    private int cellSize;

    public void addObject(Marker marker, GeoPoint point) {
        int x = getCoord(point.getLongitude());
        int y = getCoord(point.getLatitude());
    }

    private int getCoord(double x) {
        return (int) (x / cellSize);
    }
}