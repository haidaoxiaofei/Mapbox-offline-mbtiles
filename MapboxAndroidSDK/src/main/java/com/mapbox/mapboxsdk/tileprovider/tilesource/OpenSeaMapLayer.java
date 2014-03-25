package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;

public class OpenSeaMapLayer extends WebSourceTileLayer {

    private static final String BASE_URL = "http://tile.openstreetmap.org/%d/%d/%d.png";
    private static final String BASE_URL_SEA = "http://tiles.openseamap.org/seamark/%d/%d/%d.png";

    public OpenSeaMapLayer() {
        super(BASE_URL);
        mName = "Open Sea Map";
        mDescription = "Open Sea Map/Open Street Map, the free wiki world map, provides freely usable map data for all parts of the world, under the Creative Commons Attribution-Share Alike 2.0 license.";
        mShortAttribution = "© OpenStreetMap CC-BY-SA";
        mLongAttribution = "Map data © OpenStreetMap, licensed under Creative Commons Share Alike By Attribution.";
        mMinimumZoomLevel = 1;
        mMaximumZoomLevel = 18;
    }

    @Override
    public String[] getTileURLs(final MapTile aTile, boolean hdpi) {
        final int x = aTile.getX();
        final int y = aTile.getY();
        final int z = aTile.getZ();
        return new String[]{String.format(BASE_URL, z, x, y), String.format(BASE_URL_SEA, z, x, y)};
    }
}
