package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileRequestState;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.HttpResponseCache;
import java.net.URL;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.util.UUID;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileSource;
import com.mapbox.mapboxsdk.tileprovider.tilesource.OnlineTileSourceBase;
import com.mapbox.mapboxsdk.tileprovider.util.StreamUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server.
 */
public class MapTileDownloader extends MapTileModuleProviderBase {
    private static final String TAG = "Tile downloader";

    private final AtomicReference<OnlineTileSourceBase> mTileSource = new AtomicReference<OnlineTileSourceBase>();

    private final INetworkAvailabilityCheck mNetworkAvailablityCheck;
    private MapView mapView;
    HttpResponseCache cache;
    boolean hdpi;

    ArrayList<Boolean> threadControl = new ArrayList<Boolean>();

    public MapTileDownloader(final ITileSource pTileSource,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck,
                             final MapView mapView) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        this.mapView = mapView;

        hdpi = mapView.getContext().getResources().getDisplayMetrics().densityDpi > 300;

        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            HttpResponseCache cache = new HttpResponseCache(cacheDir, 1024);
        } catch(Exception e) {

        }
    }

    public ITileSource getTileSource() {
        return mTileSource.get();
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
    public int getMinimumZoomLevel() {
        OnlineTileSourceBase tileSource = mTileSource.get();
        return (tileSource != null ? tileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
    }

    @Override
    public int getMaximumZoomLevel() {
        OnlineTileSourceBase tileSource = mTileSource.get();
        return (tileSource != null ? tileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
    }

    @Override
    public void setTileSource(final ITileSource tileSource) {
        // We are only interested in OnlineTileSourceBase tile sources
        if (tileSource instanceof OnlineTileSourceBase) {
            mTileSource.set((OnlineTileSourceBase) tileSource);
        } else {
            // Otherwise shut down the tile downloader
            mTileSource.set(null);
        }
    }

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {
            threadControl.add(false);
            int threadIndex = threadControl.size() - 1;
            OnlineTileSourceBase tileSource = mTileSource.get();

            if (tileSource == null) {
                return null;
            }

            InputStream in = null;
            OutputStream out = null;
            final MapTile tile = aState.getMapTile();
            OkHttpClient client = new OkHttpClient();
            if (cache != null) {
                client.setResponseCache(cache);
            }
            if (mNetworkAvailablityCheck != null
                    && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                Log.d(TAG, "Skipping " + getName() + " due to NetworkAvailabilityCheck.");
                return null;
            }

            String url = tileSource.getTileURLString(tile, hdpi);

            if (TextUtils.isEmpty(url)) {
                return null;
            }

            try {
                // Log.d(TAG, "getting tile " + tile.getX() + ", " + tile.getY());
                // Log.d(TAG, "Downloading MapTile from url: " + url);

                HttpURLConnection connection = client.open(new URL(url));
                in = connection.getInputStream();

                if (in == null) {
                    Log.d(TAG, "No content downloading MapTile: " + tile);
                    return null;
                }

                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
                StreamUtils.copy(in, out);
                out.flush();
                final byte[] data = dataStream.toByteArray();
                final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

                Drawable result = tileSource.getDrawable(byteStream);
                threadControl.set(threadIndex, true);
                if (checkThreadControl()) {
                    TilesLoadedListener listener = mapView.getTilesLoadedListener();
                    if (listener != null) {
                        listener.onTilesLoaded();
                    }
                }
                result = mapView.getTileLoadedListener() != null ? onTileLoaded(result) : result;
                return result;
            } catch (final Throwable e) {
                Log.d(TAG, "Error downloading MapTile: " + tile + ":" + e);
            } finally {
                StreamUtils.closeStream(in);
                StreamUtils.closeStream(out);
            }

            return null;
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

    private boolean checkThreadControl() {
        for (boolean done: threadControl) {
            if (!done) {
                return false;
            }
        }
        threadControl = new ArrayList<Boolean>();
        return true;
    }
}
