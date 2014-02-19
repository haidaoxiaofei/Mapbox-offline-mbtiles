package com.mapbox.mapboxsdk.tileprovider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleProviderBase;

/**
 * Track the status of a single map tile given a list of providers that could
 * change its state by loading, caching, or disposing it.
 */
public class MapTileRequestState {

    private final Queue<MapTileModuleProviderBase> mProviderQueue;
    private final MapTile mMapTile;
    private final IMapTileProviderCallback mCallback;
    private MapTileModuleProviderBase mCurrentProvider;

    /**
     * Initialize a new state to keep track of a map tile
     * @param mapTile
     * @param providers
     * @param callback
     */
    public MapTileRequestState(final MapTile mapTile,
                               final MapTileModuleProviderBase[] providers,
                               final IMapTileProviderCallback callback) {
        mProviderQueue = new LinkedList<MapTileModuleProviderBase>();
        Collections.addAll(mProviderQueue, providers);
        mMapTile = mapTile;
        mCallback = callback;
    }

    /**
     * Get the map tile this class owns
     * @return this map tile
     */
    public MapTile getMapTile() {
        return mMapTile;
    }

    /**
     * Get the assigned callback
     * @return the assigned callback
     */
    public IMapTileProviderCallback getCallback() {
        return mCallback;
    }

    public MapTileModuleProviderBase getNextProvider() {
        mCurrentProvider = mProviderQueue.poll();
        return mCurrentProvider;
    }
}
