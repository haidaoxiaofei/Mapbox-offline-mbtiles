package com.mapbox.mapboxsdk.views;

import android.view.ScaleGestureDetector;
import android.util.Log;

/**
 * https://developer.android.com/training/gestures/scale.html
 * A custom gesture detector that processes gesture events and dispatches them
 * to the map's overlay system.
 */
public class MapViewScaleGestureDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {
    /**
     * This is the active focal point in terms of the viewport. Could be a local
     * variable but kept here to minimize per-frame allocations.
     */

    private float lastFocusX;
    private float lastFocusY;
    private float firstSpan;
    private final MapView mapView;
    private boolean scaling;
    private float currentScale;

    /**
     * Bind a new gesture detector to a map
     * @param mv a map view
     */
    public MapViewScaleGestureDetectorListener(final MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastFocusX = detector.getFocusX();
        lastFocusY = detector.getFocusY();
        firstSpan = detector.getCurrentSpan();
        currentScale = 1.0f;
        if (!this.mapView.isAnimating()) {
            this.mapView.mIsAnimating.set(true);
            this.mapView.getController().aboutToStartAnimation(
                    lastFocusX +  this.mapView.getScrollX() - ( this.mapView.getWidth() / 2),
                    lastFocusY +  this.mapView.getScrollY() - ( this.mapView.getHeight() / 2));
            scaling = true;
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!scaling) return true;
        currentScale = detector.getCurrentSpan() / firstSpan;

        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        this.mapView.getController().panBy((int) (lastFocusX - focusX), (int) (lastFocusY - focusY));
        this.mapView.setScale(currentScale);

        lastFocusX = focusX;
        lastFocusY = focusY;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        if (!scaling) return;
        float preZoom = this.mapView.getZoomLevel(false);
        float newZoom = (float)(Math.log(currentScale) / Math.log(2d) + preZoom);
        this.mapView.mTargetZoomLevel.set(Float.floatToIntBits(newZoom));
    	this.mapView.getController().onAnimationEnd();
        scaling = false;
    }
    private static String TAG = "detector";
}
