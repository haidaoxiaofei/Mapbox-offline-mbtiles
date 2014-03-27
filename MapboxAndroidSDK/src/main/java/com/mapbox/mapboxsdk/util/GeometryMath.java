package com.mapbox.mapboxsdk.util;

import android.graphics.Rect;

import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * @author Marc Kurtz
 */
public class GeometryMath {
    public static final double DEG2RAD = (Math.PI / 180.0);
    public static final double RAD2DEG = (180.0 / Math.PI);

    public static final Rect getBoundingBoxForRotatedRectangle(Rect rect, int centerX,
                                                               int centerY, float angle, Rect reuse) {
        if (reuse == null) {
            reuse = new Rect();
        }

        double theta = angle * DEG2RAD;
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double dx1 = rect.left - centerX;
        double dy1 = rect.top - centerY;
        double newX1 = centerX - dx1 * cosTheta + dy1 * sinTheta;
        double newY1 = centerY - dx1 * sinTheta - dy1 * cosTheta;
        double dx2 = rect.right - centerX;
        double dy2 = rect.top - centerY;
        double newX2 = centerX - dx2 * cosTheta + dy2 * sinTheta;
        double newY2 = centerY - dx2 * sinTheta - dy2 * cosTheta;
        double dx3 = rect.left - centerX;
        double dy3 = rect.bottom - centerY;
        double newX3 = centerX - dx3 * cosTheta + dy3 * sinTheta;
        double newY3 = centerY - dx3 * sinTheta - dy3 * cosTheta;
        double dx4 = rect.right - centerX;
        double dy4 = rect.bottom - centerY;
        double newX4 = centerX - dx4 * cosTheta + dy4 * sinTheta;
        double newY4 = centerY - dx4 * sinTheta - dy4 * cosTheta;
        reuse.set((int) Min4(newX1, newX2, newX3, newX4), (int) Min4(newY1, newY2, newY3, newY4),
                (int) Max4(newX1, newX2, newX3, newX4), (int) Max4(newY1, newY2, newY3, newY4));

        return reuse;
    }

    private static double Min4(double a, double b, double c, double d) {
        return Math.floor(Math.min(Math.min(a, b), Math.min(c, d)));
    }

    private static double Max4(double a, double b, double c, double d) {
        return Math.ceil(Math.max(Math.max(a, b), Math.max(c, d)));
    }

    /**
     * Calculates i.e. the increase of zoomlevel needed when the visible latitude needs to be bigger
     * by <code>factor</code>.
     * <p/>
     * Assert.assertEquals(1, getNextSquareNumberAbove(1.1f)); Assert.assertEquals(2,
     * getNextSquareNumberAbove(2.1f)); Assert.assertEquals(2, getNextSquareNumberAbove(3.9f));
     * Assert.assertEquals(3, getNextSquareNumberAbove(4.1f)); Assert.assertEquals(3,
     * getNextSquareNumberAbove(7.9f)); Assert.assertEquals(4, getNextSquareNumberAbove(8.1f));
     * Assert.assertEquals(5, getNextSquareNumberAbove(16.1f));
     * <p/>
     * Assert.assertEquals(-1, - getNextSquareNumberAbove(1 / 0.4f) + 1); Assert.assertEquals(-2, -
     * getNextSquareNumberAbove(1 / 0.24f) + 1);
     *
     * @param factor
     * @return
     */
    public static int getNextSquareNumberAbove(final float factor) {
        int out = 0;
        int cur = 1;
        int i = 1;
        while (true) {
            if (cur > factor) {
                return out;
            }

            out = i;
            cur *= 2;
            i++;
        }
    }

    public static int mod(int number, final int modulus) {
        if (number > 0) {
            return number % modulus;
        }

        while (number < 0) {
            number += modulus;
        }

        return number;
    }

    public static float leftShift(float value, float multiplier) {
        return (float) (value * Math.pow(2, multiplier));
    }

    public static float rightShift(float value, float multiplier) {
        return (float) (value / Math.pow(2, multiplier));
    }

    public static Rect viewPortRect(float zoomLevel, Projection projection, Rect reuse) {
        if (reuse == null) {
            reuse = new Rect();
        }
        // Get the area we are drawing to
        final Rect screenRect = projection.getScreenRect();
        final int worldSize_2 = projection.mapSize(zoomLevel) >> 1;

        //when using float zoom, the view port should be the one of the floored value
        //this is because MapTiles are indexed around int values
        int roundWorldSize_2 = projection.mapSize((float) Math.floor(zoomLevel)) >> 1;
        float scale = (float) roundWorldSize_2 / worldSize_2;
        reuse.set((int) (scale * screenRect.left),
                (int) (scale * screenRect.top),
                (int) (scale * screenRect.right),
                (int) (scale * screenRect.bottom));

        // Translate the Canvas coordinates into Mercator coordinates
        reuse.offset(roundWorldSize_2, roundWorldSize_2);
        return reuse;
    }

    public static Rect viewPortRect(Projection projection, Rect reuse) {
        return viewPortRect(projection.getZoomLevel(), projection, reuse);
    }
}
