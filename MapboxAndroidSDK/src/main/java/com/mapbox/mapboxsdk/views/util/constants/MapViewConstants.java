// Created by plusminus on 18:00:24 - 25.09.2008
package com.mapbox.mapboxsdk.views.util.constants;

/**
 * This class contains constants used by the map view.
 *
 * @author Nicolas Gramlich
 */
public interface MapViewConstants {
    // ===========================================================
    // Final Fields
    // ===========================================================

    public static final boolean DEBUGMODE = false;

    public static final int NOT_SET = Integer.MIN_VALUE;

    public static final int ANIMATION_SMOOTHNESS_LOW = 4;
    public static final int ANIMATION_SMOOTHNESS_DEFAULT = 10;
    public static final int ANIMATION_SMOOTHNESS_HIGH = 20;

    public static final int ANIMATION_DURATION_SHORT = 250;
    public static final int ANIMATION_DURATION_DEFAULT = 500;
    public static final int ANIMATION_DURATION_LONG = 2000;

    public static final double ZOOM_SENSITIVITY = 1.0;
    public static final double ZOOM_LOG_BASE_INV = 1.0 / Math.log(2.0 / ZOOM_SENSITIVITY);
}
