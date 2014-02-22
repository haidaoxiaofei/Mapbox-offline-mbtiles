package com.mapbox.mapboxsdk.views;

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.api.ILatLng;

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
        if (this.mapView.getOverlayManager().onDoubleTapEvent(e, this.mapView)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
        if (this.mapView.getOverlayManager().onSingleTapConfirmed(e, this.mapView)) {
            return true;
        }

        return false;
    }
}