package com.mapbox.mapboxsdk.constants;

/**
 * Storing certain attributes of the Mapbox online
 * service as constants to centralize references.
 */
public interface MapboxConstants {
    /**
     * The default base endpoint of Mapbox services.
     * This bakes in a CNAME and version number.
     */
    public static final String MAPBOX_BASE_URL = "https://a.tiles.mapbox.com/v3/";

    public static final String USER_AGENT = "Mapbox Android SDK/0.5.0";
}
