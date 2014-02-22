package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.modules.*;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileSource;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloader} (downloads map tiles via tile source).
 */
public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {
    Context context;

    /**
     *
     * @param pContext
     * @param pTileSource
     * @param mapView
     */
    public MapTileProviderBasic(final Context pContext,
                                final ITileSource pTileSource,
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