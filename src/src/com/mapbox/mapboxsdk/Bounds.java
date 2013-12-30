package com.mapbox.mapboxsdk;

/**
 * Created by Francisco on 30/12/13.
 */
public class Bounds {
    private double left;
    private double bottom;
    private double right;
    private double top;

    public Bounds(double left, double bottom, double right, double top){
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    public double getTop() {
        return top;
    }

    public double getLeft() {
        return left;
    }

    public double getBottom() {
        return bottom;
    }

    public double getRight() {
        return right;
    }


}
