package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.modules.*;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;

public class MapTileLayerBasic extends MapTileLayerArray implements IMapTileProviderCallback {
    Context mContext;
    MapView mMapView;

    /**
     * @param pContext
     * @param pTileSource
     * @param mapView
     */
    public MapTileLayerBasic(final Context pContext,
                             final ITileLayer pTileSource,
                             MapView mapView) {
        super(pContext, pTileSource, new SimpleRegisterReceiver(pContext));
        this.mContext = pContext;
        this.mMapView = mapView;

        final MapTileDownloader downloaderProvider = new MapTileDownloader(
                pTileSource,
                mTileCache,
                mNetworkAvailabilityCheck,
                mMapView);

        for (MapTileModuleLayerBase provider : mTileProviderList) {
            if (provider.getClass().isInstance(MapTileDownloader.class)) {
                mTileProviderList.remove(provider);
            }
        }

        mTileProviderList.add(downloaderProvider);
    }


    public void setTileSources(final ITileLayer[] aTileSources) {
        super.setTileSource(null);
        synchronized (mTileProviderList) {
            mTileProviderList.clear();
            for (ITileLayer source : aTileSources) {
                addTileSource(source);
            }
        }
    }

    public void addTileSource(final ITileLayer pTileSource) {
        final MapTileDownloader downloaderProvider = new MapTileDownloader(
                pTileSource,
                mTileCache,
                mNetworkAvailabilityCheck,
                mMapView);
        mTileProviderList.add(downloaderProvider);
    }

    public void removeTileSource(final ITileLayer pTileSource) {
        for (MapTileModuleLayerBase provider : mTileProviderList) {
            if (provider.getTileSource() == pTileSource) {
                mTileProviderList.remove(provider);
                return;
            }
        }
    }
}