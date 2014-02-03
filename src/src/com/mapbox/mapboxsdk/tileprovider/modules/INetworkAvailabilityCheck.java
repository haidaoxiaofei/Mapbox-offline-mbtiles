package com.mapbox.mapboxsdk.tileprovider.modules;

public interface INetworkAvailabilityCheck {

    boolean getNetworkAvailable();

    boolean getWiFiNetworkAvailable();

    boolean getCellularDataNetworkAvailable();

    boolean getRouteToPathExists(int hostAddress);
}
