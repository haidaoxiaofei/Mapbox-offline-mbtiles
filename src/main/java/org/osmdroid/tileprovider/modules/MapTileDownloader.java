package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.tileprovider.*;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.views.MapView;

import java.io.*;
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

    // ===========================================================
    // Constants
    // ===========================================================


    // ===========================================================
    // Fields
    // ===========================================================

    private final IFilesystemCache mFilesystemCache;

    private final AtomicReference<OnlineTileSourceBase> mTileSource = new AtomicReference<OnlineTileSourceBase>();

    private final INetworkAvailabilityCheck mNetworkAvailablityCheck;
    private org.osmdroid.views.MapView mapView;
    private boolean highDensity = false;

    private int threadCount = 0;
    ArrayList<Boolean> threadControl = new ArrayList<Boolean>();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileDownloader(final ITileSource pTileSource) {
        this(pTileSource, null);
    }

    public MapTileDownloader(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache) {
        this(pTileSource, pFilesystemCache, null, null);
    }

    public MapTileDownloader(final ITileSource pTileSource,
                             final IFilesystemCache pFilesystemCache,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck,
                             final org.osmdroid.views.MapView mapView) {
        this(pTileSource, pFilesystemCache, pNetworkAvailablityCheck,
                NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        System.out.println(mapView);
        this.mapView = mapView;
    }

    public MapTileDownloader(final ITileSource pTileSource,
                             final IFilesystemCache pFilesystemCache,
                             final INetworkAvailabilityCheck pNetworkAvailablityCheck, int pThreadPoolSize,
                             int pPendingQueueSize) {
        super(pThreadPoolSize, pPendingQueueSize);

        mFilesystemCache = pFilesystemCache;
        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
    }


    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public ITileSource getTileSource() {
        return mTileSource.get();
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

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

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {
        private int attempts = 0;
        protected String[] domainLetters = {"a", "b", "c", "d"};

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {
            threadControl.add(false);
            int threadIndex = threadControl.size()-1;
            System.out.println(threadIndex+" set");
            OnlineTileSourceBase tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }

            InputStream in = null;
            OutputStream out = null;
            final MapTile tile = aState.getMapTile();

            try {
                Log.d(TAG, "getting tile " + tile.getX() + ", " + tile.getY());
                if (mNetworkAvailablityCheck != null
                        && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                    if (DEBUGMODE) {
                        Log.d(TAG, "Skipping " + getName() + " due to NetworkAvailabilityCheck.");
                    }
                    return null;
                }

                String tileURLString = tileSource.getTileURLString(tile);
                if(MapTileDownloader.this.isHighDensity() && isMapBox(tileURLString)){
                    tileURLString = tileURLString.replace(".png","@2x.png");

                }
                if(isMapBox(tileURLString) && tileURLString.contains("https://")){
                    tileURLString = tileURLString.replace(".png", ".png?secure");
                }
                if (DEBUGMODE) {
                    Log.d(TAG, "Downloading Maptile from url: " + tileURLString);
                }

                if (TextUtils.isEmpty(tileURLString)) {
                    return null;
                }
                HttpResponse response = this.makeRequest(tileURLString);

                // Check to see if we got success
                org.apache.http.StatusLine line = response.getStatusLine();

                while (line.getStatusCode() != 200 && attempts<5) {
                    Log.d(TAG, "Retrying MapTile: " + tile + " HTTP response: " + line);

                    if(tileURLString.contains("mapbox.com")){

                        tileURLString = changeMapBoxSubdomain(tileURLString, attempts);
                    }

                    response = this.makeRequest(tileURLString);
                    line = response.getStatusLine();
                    attempts++;
                }
                if(line.getStatusCode() != 200) return null;

                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    Log.d(TAG, "No content downloading MapTile: " + tile);
                    return null;
                }
                in = entity.getContent();

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
                if(checkThreadControl()) {
                    MapView.TilesLoadedListener listener = mapView.getTilesLoadedListener();
                    if (listener != null){
                        listener.onTilesLoaded();
                    }
                }
                result = mapView.hasTileLoadedListener()? onTileLoaded(result): result;
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
                Log.d(TAG, "Error downloading MapTile: " + tile);
            } finally {
                StreamUtils.closeStream(in);
                StreamUtils.closeStream(out);
            }


            return null;
        }

        private boolean isMapBox(String URL) {
            return URL.contains("mapbox.com");
        }

        private String changeMapBoxSubdomain(String url, int attempts) {
            String tileURL = url.replace(url.substring(0, 8), "http://"+domainLetters[attempts%(domainLetters.length-1)]);
            return tileURL;
        }

        private HttpResponse makeRequest(String tileURLString) throws IOException {
            final HttpClient client = HttpClientFactory.createHttpClient();
            final HttpUriRequest head = new HttpGet(tileURLString);
            return client.execute(head);
        }

        @Override
        protected void tileLoaded(final MapTileRequestState pState, Drawable pDrawable) {
            removeTileFromQueues(pState.getMapTile());

            // don't return the tile because we'll wait for the fs provider to ask for it
            // this prevent flickering when a load of delayed downloads complete for tiles
            // that we might not even be interested in any more
            pState.getCallback().mapTileRequestCompleted(pState, null);
            // We want to return the Bitmap to the BitmapPool if applicable
            if (pDrawable instanceof ReusableBitmapDrawable)
                BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
        }
    }

    private Drawable onTileLoaded(Drawable pDrawable) {
        return mapView.getTileLoadedListener().onTileLoaded(pDrawable);
    }

    private boolean checkThreadControl(){
        for(boolean done: threadControl){
            if(!done) return false;
        }
        threadControl = new ArrayList<Boolean>();
        return true;
    }
}
