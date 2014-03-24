package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;


public class OpenCycleMapLayer extends WebSourceTileLayer {

    private static final String BASE_URL = "http://tile.opencyclemap.org/cycle/%d/%d/%d.png";

    public OpenCycleMapLayer() {
        super(BASE_URL);
        mName = "Open Cycle Map";
        mDescription = "Open Cycle Map, the free wiki world map, provides freely usable map data for all parts of the world, under the Creative Commons Attribution-Share Alike 2.0 license.";
        mShortAttribution = "© OpenCycleMap CC-BY-SA";
        mLongAttribution = "Map data © OpenCycleMap, licensed under Creative Commons Share Alike By Attribution.";
        mMinimumZoomLevel = 1;
        mMaximumZoomLevel = 15;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(BASE_URL, aTile.getZ(), aTile.getX(), aTile.getY());
    }
}
