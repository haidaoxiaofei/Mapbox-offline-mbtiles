package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import com.mapbox.mapboxsdk.tileprovider.MapTileRequestState;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server.
 */
public class MapTileDownloader extends MapTileModuleLayerBase {
    private static final String TAG = "Tile downloader";

    private final AtomicReference<TileLayer> mTileSource = new AtomicReference<TileLayer>();

    private final INetworkAvailabilityCheck mNetworkAvailablityCheck;
    private MapView mapView;
    boolean hdpi;


    public MapTileDownloader(final ITileLayer pTileSource,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck,
                             final MapView mapView) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        this.mapView = mapView;

        hdpi = mapView.getContext().getResources().getDisplayMetrics().densityDpi > DisplayMetrics.DENSITY_HIGH;

        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
    }

    public ITileLayer getTileSource() {
        return mTileSource.get();
    }

    public boolean isNetworkAvailable() {
        return (mNetworkAvailablityCheck == null || mNetworkAvailablityCheck.getNetworkAvailable());
    }

    public TilesLoadedListener getTilesLoadedListener() {
        return mapView.getTilesLoadedListener();
    }

    public TileLoadedListener getTileLoadedListener() {
        return mapView.getTileLoadedListener();
    }

    @Override
    public boolean getUsesDataConnection() {
        return true;
    }

    @Override
    protected String getName() {
        return "Online Tile Download Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "downloader";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public float getMinimumZoomLevel() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
    }

    @Override
    public float getMaximumZoomLevel() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
    }

    @Override
    public void setTileSource(final ITileLayer tileSource) {
        // We are only interested in TileLayer tile sources
        if (tileSource instanceof TileLayer) {
            mTileSource.set((TileLayer) tileSource);
        } else {
            // Otherwise shut down the tile downloader
            mTileSource.set(null);
        }
    }


    protected class TileLoader extends MapTileModuleLayerBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {

            TileLayer tileLayer = mTileSource.get();

            if (tileLayer == null) {
                return null;
            }
            return tileLayer.getDrawableFromTile(MapTileDownloader.this, aState.getMapTile(), hdpi);
        }

        @Override
        protected void tileLoaded(final MapTileRequestState pState, Drawable pDrawable) {
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestCompleted(pState, pDrawable);
        }
    }

    private Drawable onTileLoaded(Drawable pDrawable) {
        Log.i(TAG, "tile loaded on downloader");
        return mapView.getTileLoadedListener().onTileLoaded(pDrawable);
    }

}
