package com.mapbox.mapboxsdk.tileprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleLayerBase;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import android.util.Log;

import android.graphics.drawable.Drawable;

/**
 * This top-level tile provider allows a consumer to provide an array of modular asynchronous tile
 * providers to be used to obtain map tiles. When a tile is requested, the
 * {@link MapTileLayerArray} first checks the {@link MapTileCache} (synchronously) and returns
 * the tile if available. If not, then the {@link MapTileLayerArray} returns null and sends the
 * tile request through the asynchronous tile request chain. Each asynchronous tile provider returns
 * success/failure to the {@link MapTileLayerArray}. If successful, the
 * {@link MapTileLayerArray} passes the result to the base class. If failed, then the next
 * asynchronous tile provider is called in the chain. If there are no more asynchronous tile
 * providers in the chain, then the failure result is passed to the base class. The
 * {@link MapTileLayerArray} provides a mechanism so that only one unique tile-request can be in
 * the map tile request chain at a time.
 *
 * @author Marc Kurtz
 */
public class MapTileLayerArray extends MapTileLayerBase {

    protected final HashMap<MapTile, MapTileRequestState> mWorking;

    protected final List<MapTileModuleLayerBase> mTileProviderList;

    /**
     * Creates an {@link MapTileLayerArray} with no tile providers.
     *
     * @param pRegisterReceiver a {@link IRegisterReceiver}
     */
    protected MapTileLayerArray(final ITileLayer pTileSource,
                                final IRegisterReceiver pRegisterReceiver) {
        this(pTileSource, pRegisterReceiver, null);
    }

    /**
     * Creates an {@link MapTileLayerArray} with the specified tile providers.
     *
     * @param aRegisterReceiver  a {@link IRegisterReceiver}
     * @param pTileProviderArray an array of {@link com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleLayerBase}
     */
    public MapTileLayerArray(final ITileLayer pTileSource,
                             final IRegisterReceiver aRegisterReceiver,
                             final MapTileModuleLayerBase[] pTileProviderArray) {
        super(pTileSource);

        mWorking = new HashMap<MapTile, MapTileRequestState>();

        mTileProviderList = new ArrayList<MapTileModuleLayerBase>();
        if (pTileProviderArray != null) {
            Collections.addAll(mTileProviderList, pTileProviderArray);
        }
    }

    @Override
    public void detach() {
        if (getTileSource() != null) {
            getTileSource().detach();
        }
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                tileProvider.detach();
            }
        }

        synchronized (mWorking) {
            mWorking.clear();
        }
    }

    @Override
    public Drawable getMapTile(final MapTile pTile) {
        final Drawable tile = mTileCache.getMapTile(pTile);
        if (tile != null && !ExpirableBitmapDrawable.isDrawableExpired(tile)) {
            return tile;
        } else {
            boolean alreadyInProgress = false;
            synchronized (mWorking) {
                alreadyInProgress = mWorking.containsKey(pTile);
            }

            if (alreadyInProgress) {
                //
            } else {
                if (DEBUG_TILE_PROVIDERS) {
                    Log.i(TAG, "MapTileLayerArray.getMapTile() requested but not in cache, trying from async providers: "
                            + pTile);
                }

                final MapTileRequestState state;
                synchronized (mTileProviderList) {
                    final MapTileModuleLayerBase[] providerArray =
                            new MapTileModuleLayerBase[mTileProviderList.size()];
                    state = new MapTileRequestState(pTile,
                            mTileProviderList.toArray(providerArray), this);
                }

                synchronized (mWorking) {
                    // Check again
                    alreadyInProgress = mWorking.containsKey(pTile);
                    if (alreadyInProgress) {
                        return null;
                    }

                    mWorking.put(pTile, state);
                }

                final MapTileModuleLayerBase provider = findNextAppropriateProvider(state);
                if (provider != null) {
                    provider.loadMapTileAsync(state);
                } else {
                    mapTileRequestFailed(state);
                }
            }
            return tile;
        }
    }

    @Override
    public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
        synchronized (mWorking) {
            mWorking.remove(aState.getMapTile());
        }
        super.mapTileRequestCompleted(aState, aDrawable);
    }

    @Override
    public void mapTileRequestFailed(final MapTileRequestState aState) {
        final MapTileModuleLayerBase nextProvider = findNextAppropriateProvider(aState);
        if (nextProvider != null) {
            nextProvider.loadMapTileAsync(aState);
        } else {
            synchronized (mWorking) {
                mWorking.remove(aState.getMapTile());
            }
            super.mapTileRequestFailed(aState);
        }
    }

    @Override
    public void mapTileRequestExpiredTile(MapTileRequestState aState, Drawable aDrawable) {
        // Call through to the super first so aState.getCurrentProvider() still contains the proper
        // provider.
        super.mapTileRequestExpiredTile(aState, aDrawable);

        // Continue through the provider chain
        final MapTileModuleLayerBase nextProvider = findNextAppropriateProvider(aState);
        if (nextProvider != null) {
            nextProvider.loadMapTileAsync(aState);
        } else {
            synchronized (mWorking) {
                mWorking.remove(aState.getMapTile());
            }
        }
    }

    /**
     * We want to not use a provider that doesn't exist anymore in the chain, and we want to not use
     * a provider that requires a data connection when one is not available.
     */
    protected MapTileModuleLayerBase findNextAppropriateProvider(final MapTileRequestState aState) {
        MapTileModuleLayerBase provider = null;
        boolean providerDoesntExist = false, providerCantGetDataConnection = false, providerCantServiceZoomlevel = false;
        // The logic of the while statement is
        // "Keep looping until you get null, or a provider that still exists
        // and has a data connection if it needs one and can service the zoom level,"
        do {
            provider = aState.getNextProvider();
            // Perform some checks to see if we can use this provider
            // If any of these are true, then that disqualifies the provider for this tile request.
            if (provider != null) {
                providerDoesntExist = !this.getProviderExists(provider);
                providerCantGetDataConnection = !useDataConnection()
                        && provider.getUsesDataConnection();
                int zoomLevel = aState.getMapTile().getZ();
                providerCantServiceZoomlevel = zoomLevel > provider.getMaximumZoomLevel()
                        || zoomLevel < provider.getMinimumZoomLevel();
            }
        } while ((provider != null)
                && (providerDoesntExist || providerCantGetDataConnection || providerCantServiceZoomlevel));
        return provider;
    }

    public boolean getProviderExists(final MapTileModuleLayerBase provider) {
        synchronized (mTileProviderList) {
            return mTileProviderList.contains(provider);
        }
    }

    @Override
    public float getMinimumZoomLevel() {
    	float result = MINIMUM_ZOOMLEVEL;
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                result = Math.max(result, tileProvider.getMinimumZoomLevel());
            }
        }
        return result;
    }

    @Override
    public float getMaximumZoomLevel() {
    	float result = MAXIMUM_ZOOMLEVEL;
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                result = Math.min(result, tileProvider.getMaximumZoomLevel());
            }
        }
        return result;
    }

    @Override
    public void setTileSource(final ITileLayer aTileSource) {

        super.setTileSource(aTileSource);

        synchronized (mTileProviderList) {
        	if (mTileProviderList.size() != 0) {
        		mTileProviderList.get(0).setTileSource(aTileSource);
                clearTileCache();
        	}
            
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox result = null;
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                BoundingBox providerBox = tileProvider.getBoundingBox();
                if (result == null) {
                    result = providerBox;
                } else {
                    result = result.union(providerBox);
                }
            }
        }
        return result;
    }

    @Override
    public LatLng getCenterCoordinate() {
        float latitude = 0;
        float longitude = 0;
        int nb = 0;
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                LatLng providerCenter = tileProvider.getCenterCoordinate();
                if (providerCenter != null) {
                    latitude += providerCenter.getLatitude();
                    longitude += providerCenter.getLongitude();
                    nb ++;
                }
            }
        }
        if (nb > 0 ) {
            latitude /= nb;
            longitude /= nb;
            return new LatLng(latitude, longitude);
        }

        return null;
    }


    @Override
    public float getCenterZoom() {
        float centerZoom = 0;
        int nb = 0;
        synchronized (mTileProviderList) {
            for (final MapTileModuleLayerBase tileProvider : mTileProviderList) {
                centerZoom += tileProvider.getCenterZoom();
                nb ++;
            }
        }
        if (centerZoom > 0 ) {
            return centerZoom / nb;
        }

        return (getMaximumZoomLevel() + getMinimumZoomLevel()) / 2;
    }

    private static final String TAG = "MapTileLayerArray";
}
