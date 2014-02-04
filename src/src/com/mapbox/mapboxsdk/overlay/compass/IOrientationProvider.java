package com.mapbox.mapboxsdk.overlay.compass;


public interface IOrientationProvider {
    boolean startOrientationProvider(IOrientationConsumer orientationConsumer);

    void stopOrientationProvider();

    float getLastKnownOrientation();
}
