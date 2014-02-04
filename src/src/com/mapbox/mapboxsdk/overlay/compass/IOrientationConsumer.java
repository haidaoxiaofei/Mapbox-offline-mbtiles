package com.mapbox.mapboxsdk.overlay.compass;


public interface IOrientationConsumer {
    void onOrientationChanged(float orientation, IOrientationProvider source);
}
