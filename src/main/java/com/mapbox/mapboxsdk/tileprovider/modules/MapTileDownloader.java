package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.squareup.okhttp.OkHttpClient;
import java.net.URL;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.*;
import com.mapbox.mapboxsdk.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileSource;
import com.mapbox.mapboxsdk.tileprovider.tilesource.OnlineTileSourceBase;
import com.mapbox.mapboxsdk.tileprovider.util.StreamUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server. It saves downloaded tiles to an
 * IFilesystemCache if available.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */
public class MapTileDownloader extends MapTileModuleProviderBase {
    private static final String TAG = "Tile downloader";

    private final IFilesystemCache mFilesystemCache;

    private final AtomicReference<OnlineTileSourceBase> mTileSource = new AtomicReference<OnlineTileSourceBase>();

    private final INetworkAvailabilityCheck mNetworkAvailablityCheck;
    private MapView mapView;
    private boolean highDensity = false;

    private int threadCount = 0;
    ArrayList<Boolean> threadControl = new ArrayList<Boolean>();

    public MapTileDownloader(final ITileSource pTileSource,
                             final IFilesystemCache pFilesystemCache,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck,
                             final MapView mapView) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        this.mapView = mapView;
        mFilesystemCache = pFilesystemCache;
        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
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

    public void setHighDensity(boolean highDensity) {
        this.highDensity = highDensity;
    }

    public boolean isHighDensity() {
        return highDensity;
    }

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {
        private int attempts = 0;
        protected String[] domainLetters = {"a", "b", "c", "d"};

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {
            threadControl.add(false);
            int threadIndex = threadControl.size() - 1;
            System.out.println(threadIndex + " set");
            OnlineTileSourceBase tileSource = mTileSource.get();

            if (tileSource == null) {
                return null;
            }

            InputStream in = null;
            OutputStream out = null;
            final MapTile tile = aState.getMapTile();
            OkHttpClient client = new OkHttpClient();
            if (mNetworkAvailablityCheck != null
                    && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                Log.d(TAG, "Skipping " + getName() + " due to NetworkAvailabilityCheck.");
                return null;
            }
            String url = tileSource.getTileURLString(tile);
            if (MapTileDownloader.this.isHighDensity() && url.contains("mapbox.com")) {
                url = url.replace(".png", "@2x.png");
            }
            if (TextUtils.isEmpty(url)) {
                return null;
            }

            try {
                Log.d(TAG, "getting tile " + tile.getX() + ", " + tile.getY());
                Log.d(TAG, "Downloading MapTile from url: " + url);

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

                // Save the data to the filesystem cache
                if (mFilesystemCache != null) {
                    mFilesystemCache.saveFile(tileSource, tile, byteStream);
                    byteStream.reset();
                }
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

            } catch (final UnknownHostException e) {
                // no network connection so empty the queue
                Log.d(TAG, "UnknownHostException downloading MapTile: " + tile + " : " + e);
                throw new CantContinueException(e);
            } catch (final LowMemoryException e) {
                // low memory so empty the queue
                Log.d(TAG, "LowMemoryException downloading MapTile: " + tile + " : " + e);
                throw new CantContinueException(e);
            } catch (final FileNotFoundException e) {
                Log.d(TAG, "Tile not found: " + tile + " : " + e);
            } catch (final IOException e) {
                Log.d(TAG, "IOException downloading MapTile: " + tile + " : " + e);
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

            // don't return the tile because we'll wait for the fs provider to ask for it
            // this prevent flickering when a load of delayed downloads complete for tiles
            // that we might not even be interested in any more
            pState.getCallback().mapTileRequestCompleted(pState, null);
            // We want to return the Bitmap to the BitmapPool if applicable
            if (pDrawable instanceof ReusableBitmapDrawable) {
                BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
            }
        }
    }

    private Drawable onTileLoaded(Drawable pDrawable) {
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
