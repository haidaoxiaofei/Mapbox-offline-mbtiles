package com.mapbox.mapboxsdk.views.overlay.compass;


public interface IOrientationProvider {
    boolean startOrientationProvider(IOrientationConsumer orientationConsumer);

    void stopOrientationProvider();

    float getLastKnownOrientation();
}
