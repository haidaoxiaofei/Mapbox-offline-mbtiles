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
    private float lastSpanX;
    private float lastSpanY;

    private float lastFocusX;
    private float lastFocusY;
    private float firstSpan;
    private final MapView mapView;

    /**
     * Bind a new gesture detector to a map
     * @param mv a map view
     */
    public MapViewScaleGestureDetectorListener(final MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastSpanX = detector.getCurrentSpanX();
        lastSpanY = detector.getCurrentSpanY();
        lastFocusX = detector.getFocusX();
        lastFocusY = detector.getFocusY();
        firstSpan = detector.getCurrentSpan();
        this.mapView.getController().aboutToStartAnimation(
        		lastFocusX +  this.mapView.getScrollX() - ( this.mapView.getWidth() / 2),
        		lastFocusY +  this.mapView.getScrollY() - ( this.mapView.getHeight() / 2));
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float spanX = scaleGestureDetector.getCurrentSpanX();
        float spanY = scaleGestureDetector.getCurrentSpanY();

        float focusX = scaleGestureDetector.getFocusX();
        float focusY = scaleGestureDetector.getFocusY();

        this.mapView.getController().panBy((int) (lastFocusX - focusX), (int) (lastFocusY - focusY));
        this.mapView.setScale(scaleGestureDetector.getCurrentSpan() / firstSpan);

        lastSpanX = spanX;
        lastSpanY = spanY;
        lastFocusX = focusX;
        lastFocusY = focusY;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        float scale = scaleGestureDetector.getCurrentSpan() / firstSpan;
       float preZoom = this.mapView.getZoomLevel(false);
        float newZoom = (float)(Math.log(scale) / Math.log(2d) + preZoom);
        this.mapView.setScale(scale);
        this.mapView.mTargetZoomLevel.set(Float.floatToIntBits(newZoom));
    	this.mapView.getController().onAnimationEnd();
    }
    private static String TAG = "detector";
}
