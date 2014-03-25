package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;


public class MapQuestOpenAerialLayer extends WebSourceTileLayer {

    private static final String BASE_URL = "http://oatile1.mqcdn.com/tiles/1.0.0/sat/%d/%d/%d.png";

    public MapQuestOpenAerialLayer() {
        super(BASE_URL);
        mName = "MapQuest Open Aerial";
        mDescription = "Map tiles courtesy of MapQuest. Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency.";
        mShortAttribution = "Tiles courtesy of MapQuest.";
        mLongAttribution = "Tiles courtesy of MapQuest and OpenStreetMap contributors. Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency.";
        mMinimumZoomLevel = 1;
        mMaximumZoomLevel = 11;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(BASE_URL, aTile.getZ(), aTile.getX(), aTile.getY());
    }
}
