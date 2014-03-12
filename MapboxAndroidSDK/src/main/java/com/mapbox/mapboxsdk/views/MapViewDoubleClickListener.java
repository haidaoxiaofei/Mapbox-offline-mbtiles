package com.mapbox.mapboxsdk.views;

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.api.ILatLng;
import android.util.Log;

/**
 * A custom listener for double-tap gestures that zooms in on
 * the right location.
 */
class MapViewDoubleClickListener implements GestureDetector.OnDoubleTapListener {

    private final MapView mapView;
    public MapViewDoubleClickListener(MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (this.mapView.getOverlayManager().onDoubleTap(e, this.mapView)) {
            return true;
        }
        final ILatLng center = this.mapView.getProjection().fromPixels(e.getX(), e.getY());
        return this.mapView.zoomInFixing(center);
    }

    @Override
    public boolean onDoubleTapEvent(final MotionEvent e) {
        return this.mapView.getOverlayManager().onDoubleTapEvent(e, this.mapView);
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
        return this.mapView.getOverlayManager().onSingleTapConfirmed(e, this.mapView);
    }

    private final String TAG = "MapViewDoubleClickListener";
}