package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

public class mapboxTileLayer extends OnlineTileSourceBase implements MapViewConstants, MapboxConstants {

    private String baseUrl;

    public mapboxTileLayer(String id) {
        super(id, null, 1, 16, DEFAULT_TILE_SIZE, ".png", id);

        if (!id.contains("http://") && !id.contains("https://") && id.contains("")) {
            baseUrl =  MAPBOX_BASE_URL + id + "/";
        } else {
            baseUrl = id;
        }
    }


    @Override
    public String getTileURLString(final MapTile aTile) {
        return baseUrl + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY()
                + mImageFilenameEnding;
    }
}
