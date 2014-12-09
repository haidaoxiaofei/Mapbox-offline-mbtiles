package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.mapbox.mapboxsdk.exceptions.OfflineDatabaseException;
import com.mapbox.mapboxsdk.offline.OfflineMapDatabase;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import java.io.ByteArrayInputStream;

public class OfflineMapTileProvider extends MapTileLayerBase {

    private static final String TAG = "OfflineMapTileProvider";

    private OfflineMapDatabase offlineMapDatabase = null;

    public OfflineMapTileProvider(Context context, OfflineMapDatabase offlineMapDatabase) {
        super(context, null);
        this.offlineMapDatabase = offlineMapDatabase;
    }

    @Override
    public Drawable getMapTile(MapTile pTile, boolean allowRemote) {
        Log.i(TAG, String.format("getMapTile() with maptile path = '%s'", pTile.getPath()));
        try {
            // TODO - Build URL to match url in database
//            String.format(MAPBOX_BASE_URL + "%s/%d/%d/%d%s.%s%s", this.mapID, zoom, x, y, "@2x", MapboxUtils.qualityExtensionForImageQuality(this.imageQuality), "");

            byte[] data = offlineMapDatabase.dataForURL(pTile.getPath());
            BitmapDrawable bd = new BitmapDrawable(context.getResources(), new ByteArrayInputStream(data));
            return bd;
        } catch (OfflineDatabaseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void detach() {

    }
}
