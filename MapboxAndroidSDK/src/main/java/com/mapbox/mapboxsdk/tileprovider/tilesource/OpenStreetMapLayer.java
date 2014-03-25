package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;

public class OpenStreetMapLayer extends WebSourceTileLayer {
    private static final String BASE_URL = "http://tile.openstreetmap.org/%d/%d/%d.png";

    public OpenStreetMapLayer() {
        super(BASE_URL);
        mName = "Open Street Map";
        mDescription = "Open Street Map, the free wiki world map, provides freely usable map data for all parts of the world, under the Creative Commons Attribution-Share Alike 2.0 license.";
        mShortAttribution = "© OpenStreetMap CC-BY-SA";
        mLongAttribution = "Map data © OpenStreetMap, licensed under Creative Commons Share Alike By Attribution.";
        mMinimumZoomLevel = 1;
        mMaximumZoomLevel = 18;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(BASE_URL, aTile.getZ(), aTile.getX(), aTile.getY());
    }

}
