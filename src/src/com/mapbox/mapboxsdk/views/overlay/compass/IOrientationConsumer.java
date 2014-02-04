package com.mapbox.mapboxsdk.views.overlay.compass;


public interface IOrientationConsumer {
    void onOrientationChanged(float orientation, IOrientationProvider source);
}
