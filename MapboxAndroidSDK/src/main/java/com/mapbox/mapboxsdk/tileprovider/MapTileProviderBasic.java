package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.modules.*;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;

public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {
    Context context;

    /**
     *
     * @param pContext
     * @param pTileSource
     * @param mapView
     */
    public MapTileProviderBasic(final Context pContext,
                                final ITileLayer pTileSource,
                                MapView mapView) {
        super(pTileSource, new SimpleRegisterReceiver(pContext));
        this.context = pContext;

        final MapTileDownloader downloaderProvider = new MapTileDownloader(
                pTileSource,
                new NetworkAvailabilityCheck(pContext),
                mapView);

        for (MapTileModuleProviderBase provider: mTileProviderList) {
            if (provider.getClass().isInstance(MapTileDownloader.class)) {
                mTileProviderList.remove(provider);
            }
        }

        mTileProviderList.add(downloaderProvider);
    }
}