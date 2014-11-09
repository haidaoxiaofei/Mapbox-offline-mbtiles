package com.mapbox.mapboxsdk.offline;

public class OfflineMapDownloader {

    private static final String TAG = "OfflineMapDownloader";

    private static OfflineMapDownloader offlineMapDownloader;

    private OfflineMapDownloader()
    {
        super();
    }

    public static OfflineMapDownloader getOfflineMapDownloader() {
        if (offlineMapDownloader == null)
        {
            offlineMapDownloader = new OfflineMapDownloader();
        }
        return offlineMapDownloader;
    }

}
