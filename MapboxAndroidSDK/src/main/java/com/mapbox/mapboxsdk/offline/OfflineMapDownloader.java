package com.mapbox.mapboxsdk.offline;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.CoordinateRegion;

public class OfflineMapDownloader implements MapboxConstants {

    private static final String TAG = "OfflineMapDownloader";

    private static OfflineMapDownloader offlineMapDownloader;

    /**
     * The possible states of the offline map downloader.
     */
    enum MBXOfflineMapDownloaderState {
        /**
         * An offline map download job is in progress.
         */
        MBXOfflineMapDownloaderStateRunning,
        /**
         * An offline map download job is suspended and can be either resumed or canceled.
         */
        MBXOfflineMapDownloaderStateSuspended,
        /**
         * An offline map download job is being canceled.
         */
        MBXOfflineMapDownloaderStateCanceling,
        /**
         * The offline map downloader is ready to begin a new offline map download job.
         */
        MBXOfflineMapDownloaderStateAvailable
    }

    private String uniqueID;
    private String mapID;
    private boolean includesMetadata;
    private boolean includesMarkers;
    private RasterImageQuality imageQuality;
    private CoordinateRegion mapRegion;
    private int minimumZ;
    private int maximumZ;
    private MBXOfflineMapDownloaderState state;
    private int totalFilesWritten;
    private int totalFilesExpectedToWrite;


/*
    // Don't appear to be needed as there's one database per app for offline maps
    @property (nonatomic) NSMutableArray *mutableOfflineMapDatabases;
    @property (nonatomic) NSString *partialDatabasePath;
    @property (nonatomic) NSURL *offlineMapDirectory;

    // Don't appear to be needed as as Android and Mapbox Android SDK provide these
    @property (nonatomic) NSOperationQueue *backgroundWorkQueue;
    @property (nonatomic) NSOperationQueue *sqliteQueue;
    @property (nonatomic) NSURLSession *dataSession;
    @property (nonatomic) NSInteger activeDataSessionTasks;
*/


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
