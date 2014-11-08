package com.mapbox.mapboxsdk.offline;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.BoundingBox;

public class OfflineMapDatabase implements MapboxConstants {

    private String uniqueID;
    private String mapID;
    private boolean includesMetadata;
    private boolean includesMarkers;
    private RasterImageQuality imageQuality;
    private BoundingBox mapRegion;
    private Integer minimumZ;
    private Integer maximumZ;
    private String path;
    private boolean invalid;
    private boolean initializedProperly;

    /**
     * Default Constructor
     */
    public OfflineMapDatabase() {
        super();
    }

}
