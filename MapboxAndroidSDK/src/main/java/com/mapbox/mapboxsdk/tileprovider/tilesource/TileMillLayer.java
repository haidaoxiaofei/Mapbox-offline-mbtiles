package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.tileprovider.MapTile;

public class TileMillLayer extends WebSourceTileLayer {

    private static final String BASE_URL = "http://%s:20008/tile/%s";

    public TileMillLayer(final String pHost, final String pMap, final float pMinZoom, final float pMaxZoom) {
        super(String.format(BASE_URL, pHost, pMap));
        init(pMinZoom, pMaxZoom);
    }
    public TileMillLayer(final String pHost, final String pMap) {
        super(String.format(BASE_URL, pHost, pMap));
        init(1, 22);

    }

    public TileMillLayer(final String pMap) {
        super(String.format(BASE_URL, "localhost", pMap));
        init(1, 22);

    }

    private void init(final float pMinZoom, final float pMaxZoom) {
        mName = "TileMill";
        mMinimumZoomLevel = pMinZoom;
        mMaximumZoomLevel = pMaxZoom;
    }

    @Override
    public TileLayer setURL(final String aUrl) {
        super.setURL(aUrl + "/%d/%d/%d.png?updated=%d");
        return this;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(mUrl, aTile.getZ(), aTile.getX(), aTile.getY(),  System.currentTimeMillis() / 1000L);
    }
}
