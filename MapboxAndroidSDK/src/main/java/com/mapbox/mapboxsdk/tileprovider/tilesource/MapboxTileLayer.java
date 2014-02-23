package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

/**
 * A convenience class to initialize tile layers that use Mapbox.
 */
public class MapboxTileLayer extends OnlineTileSourceBase implements MapViewConstants, MapboxConstants {

    private String baseUrl;

    /**
     * Initialize a new tile layer, directed at a hosted Mapbox tilesource.
     * @param id a valid mapid, of the form account.map
     */
    public MapboxTileLayer(String id) {
        super(id, null, 1, 16, DEFAULT_TILE_SIZE, ".png", id);
        if (!id.contains("http://") && !id.contains("https://") && id.contains("")) {
            baseUrl =  MAPBOX_BASE_URL + id + "/";
        } else {
            baseUrl = id;
        }
    }

    @Override
    public String getTileURLString(final MapTile aTile, boolean hdpi) {
        String url = baseUrl + aTile.getZ() + "/" + aTile.getX() + "/" + aTile.getY()
                + mImageFilenameEnding;
        url = url.replace(".png", "@2x.png");
        return url;
    }
}
