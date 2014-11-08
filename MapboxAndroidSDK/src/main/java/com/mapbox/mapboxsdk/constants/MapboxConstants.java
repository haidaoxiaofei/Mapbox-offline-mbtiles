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

    public static final String USER_AGENT = "Mapbox Android SDK/0.4.0";

    public enum RasterImageQuality {
        /** Full image quality. */
        MBXRasterImageQualityFull,
        /** 32 color indexed PNG. */
        MBXRasterImageQualityPNG32,
        /** 64 color indexed PNG. */
        MBXRasterImageQualityPNG64,
        /** 128 color indexed PNG. */
        MBXRasterImageQualityPNG128,
        /** 256 color indexed PNG. */
        MBXRasterImageQualityPNG256,
        /** 70% quality JPEG. */
        MBXRasterImageQualityJPEG70,
        /** 80% quality JPEG. */
        MBXRasterImageQualityJPEG80,
        /** 90% quality JPEG. */
        MBXRasterImageQualityJPEG90
    }
}
