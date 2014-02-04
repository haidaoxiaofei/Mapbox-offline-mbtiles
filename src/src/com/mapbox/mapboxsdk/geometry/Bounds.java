package com.mapbox.mapboxsdk.geometry;

/**
 * Created by Francisco on 30/12/13.
 */
public class Bounds {
    public double left;
    public double bottom;
    public double right;
    public double top;

    /**
     * Initialize geographic bounds, with left, bottom, right,
     * and top dimensions.
     *
     * @param left the left, west side
     * @param bottom the bottom, south side
     * @param right the right, east side
     * @param top the top, north side
     */
    public Bounds(double left, double bottom, double right, double top){
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }
}