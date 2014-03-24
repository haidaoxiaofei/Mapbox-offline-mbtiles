package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;


public class MapQuestOSMLayer extends WebSourceTileLayer {

    private static final String BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/%d/%d/%d.png";

    public MapQuestOSMLayer() {
        super(BASE_URL);
        mName = "MapQuest Open Aerial";
        mDescription = "Map tiles courtesy of MapQuest.";
        mShortAttribution = "Tiles courtesy of MapQuest.";
        mLongAttribution = "Tiles courtesy of MapQuest and OpenStreetMap contributors.";
        mMinimumZoomLevel = 1;
        mMaximumZoomLevel = 18;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(BASE_URL, aTile.getZ(), aTile.getX(), aTile.getY());
    }
}