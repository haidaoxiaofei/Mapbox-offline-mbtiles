package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileRequestState;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.HttpResponseCache;
import java.net.URL;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.util.StreamUtils;

import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server.
 */
public class MapTileDownloader extends MapTileModuleLayerBase {
    private static final String TAG = "Tile downloader";

    private final AtomicReference<TileLayer> mTileSource = new AtomicReference<TileLayer>();

    private final INetworkAvailabilityCheck mNetworkAvailablityCheck;
    private MapView mapView;
    HttpResponseCache cache;
    boolean hdpi;

    ArrayList<Boolean> threadControl = new ArrayList<Boolean>();

    public MapTileDownloader(final ITileLayer pTileSource,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck,
                             final MapView mapView) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        this.mapView = mapView;

        hdpi = mapView.getContext().getResources().getDisplayMetrics().densityDpi > DisplayMetrics.DENSITY_HIGH;

        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            cache = new HttpResponseCache(cacheDir, 1024);
        } catch(Exception e) {

        }
    }

    public ITileLayer getTileSource() {
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
            threadControl.add(false);
            int threadIndex = threadControl.size() - 1;
            TileLayer tileLayer = mTileSource.get();

            if (tileLayer == null) {
                return null;
            }

            InputStream in = null;
            OutputStream out = null;
            final MapTile tile = aState.getMapTile();

            OkHttpClient client = new OkHttpClient();
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
            } catch (GeneralSecurityException e) {
                throw new AssertionError(); // The system has no TLS. Just give up.
            }
            client.setSslSocketFactory(sslContext.getSocketFactory());

            if (cache != null) {
                client.setResponseCache(cache);
            }
            if (mNetworkAvailablityCheck != null
                    && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                Log.d(TAG, "Skipping " + getName() + " due to NetworkAvailabilityCheck.");
                return null;
            }

            String url = tileLayer.getTileURL(tile, hdpi);

            if (TextUtils.isEmpty(url)) {
                return null;
            }

            try {
                // Log.d(TAG, "getting tile " + tile.getX() + ", " + tile.getY());
                // Log.d(TAG, "Downloading MapTile from url: " + url);
            	TilesLoadedListener listener = mapView.getTilesLoadedListener();
                if (listener != null) {
                    listener.onTilesLoadStarted();
                }
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

                Drawable result = tileLayer.getDrawable(byteStream);
                threadControl.set(threadIndex, true);
                if (checkThreadControl()) {
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
