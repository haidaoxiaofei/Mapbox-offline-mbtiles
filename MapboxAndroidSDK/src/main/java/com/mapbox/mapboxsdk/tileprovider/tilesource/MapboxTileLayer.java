package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

/**
 * A convenience class to initialize tile layers that use Mapbox services.
 * Underneath, this initializes a WebSourceTileLayer, but provides conveniences
 * for retina tiles, initialization by ID, and loading over SSL.
 */
public class MapboxTileLayer extends WebSourceTileLayer implements MapViewConstants,
        MapboxConstants {
    private static final String TAG = "MapboxTileLayer";
    private String mId;

    /**
     * Initialize a new tile layer, directed at a hosted Mapbox tilesource.
     *
     * @param pId a valid mapid, of the form account.map
     */
    public MapboxTileLayer(String pId) {
        this(pId, false);
    }

    public MapboxTileLayer(String pId, boolean enableSSL) {
        super(pId, pId, enableSSL);
    }
    @Override
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        mId = pId;
        super.initialize(pId, aUrl, enableSSL);
    }
    @Override
    public TileLayer setURL(final String aUrl) {
        if (!aUrl.contains("http://") && !aUrl.contains("https://")
                && aUrl.contains("")) {
            super.setURL(MAPBOX_BASE_URL + aUrl + "/{z}/{x}/{y}{2x}.png");
        } else {
            super.setURL(aUrl);
        }
        return this;
    }

    @Override
    protected String getBrandedJSONURL() {
        return String.format("http%s://api.tiles.mapbox.com/v3/%s.json%s",
                (mEnableSSL ? "s" : ""), mId, (mEnableSSL ? "?secure" : ""));
    }

    public String getCacheKey() {
        return mId;
    }
}