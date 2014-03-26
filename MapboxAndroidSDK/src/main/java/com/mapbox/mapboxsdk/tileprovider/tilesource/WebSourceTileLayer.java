package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileCache;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.tileprovider.util.StreamUtils;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * An implementation of {@link TileLayer} that pulls tiles from the internet.
 */
public class WebSourceTileLayer extends TileLayer {
    private static final String TAG = "WebSourceTileLayer";
    HttpResponseCache cache;

    // Tracks the number of threads active in the getBitmapFromURL method.
    private AtomicInteger activeThreads = new AtomicInteger(0);
    protected JSONObject infoJSON;
    protected boolean mEnableSSL = false;

    public WebSourceTileLayer(String url) {
        super(url);
        initialize(url, false);
    }

    public WebSourceTileLayer(String url, boolean enableSSL) {
        super(url);
        initialize(url, enableSSL);
    }

    private boolean checkThreadControl() {
        return activeThreads.get() == 0;
    }

    @Override
    public TileLayer setURL(final String aUrl) {
        if (aUrl.contains(String.format("http%s://", (mEnableSSL ? "" : "s")))) {
            super.setURL(aUrl.replace(String.format("http%s://", (mEnableSSL ? "" : "s")),
                    String.format("http%s://", (mEnableSSL ? "s" : ""))));
        } else {
            super.setURL(aUrl);
        }
        return this;
    }

    protected void initialize(String aUrl, boolean enableSSL) {
        mEnableSSL = enableSSL;
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            cache = new HttpResponseCache(cacheDir, 1024);
        } catch (Exception e) {

        }

        this.setURL(aUrl);
        Log.d(TAG, "initialize " + aUrl);
        String jsonURL = getBrandedJSONURL();
        if (jsonURL != null) {
            initWithTileJSON(getBrandedJSON(jsonURL));
        }
    }

    private float getJSONFloat(JSONObject JSON, String key) {
        float defaultValue = 0;
        if (JSON.has(key)) {
            try {
                return (float) JSON.getDouble(key);
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double[] getJSONDoubleArray(JSONObject JSON, String key, int length) {
        double[] defaultValue = null;
        if (JSON.has(key)) {
            try {
                boolean valid = false;
                double[] result = new double[length];
                Object value = JSON.get(key);
                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    if (array.length() == length) {
                        for (int i = 0; i < array.length(); i++) {
                            result[i] = array.getDouble(i);
                        }
                        valid = true;
                    }
                } else {
                    String[] array = JSON.getString(key).split(",");
                    if (array.length == length) {
                        for (int i = 0; i < array.length; i++) {
                            result[i] = Double.parseDouble(array[i]);
                        }
                        valid = true;
                    }
                }
                if (valid) {
                    return result;
                }
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private void initWithTileJSON(JSONObject tileJSON) {
        infoJSON = (tileJSON != null) ? tileJSON : new JSONObject();
        if (infoJSON != null) {
            if (infoJSON.has("tiles")) {
                try {
                    this.setURL(infoJSON.getJSONArray("tiles").getString(0).replace(".png", "{2x}.png"));
                } catch (JSONException e) {
                }
            }
            mMinimumZoomLevel = getJSONFloat(infoJSON, "minzoom");
            mMaximumZoomLevel = getJSONFloat(infoJSON, "maxzoom");
            mName = infoJSON.optString("name");
            mDescription = infoJSON.optString("description");
            mAttribution = infoJSON.optString("attribution");
            mLegend = infoJSON.optString("legend");

            double[] center = getJSONDoubleArray(infoJSON, "center", 3);
            if (center != null) {
                mCenter = new LatLng(center[0], center[1], center[2]);
            }
            double[] bounds = getJSONDoubleArray(infoJSON, "bounds", 4);
            if (bounds != null) {
                mBoundingBox = new BoundingBox(bounds[3], bounds[2], bounds[1], bounds[0]);
            }
        }
        Log.d(TAG, "infoJSON " + infoJSON.toString());
    }

    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    class RetrieveJSONTask extends AsyncTask<String, Void, JSONObject> {
        protected JSONObject doInBackground(String... urls) {
            OkHttpClient client = new OkHttpClient();
            client.setResponseCache(null);
            InputStream in = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = client.open(url);
                in = connection.getInputStream();
                byte[] response = readFully(in);
                String result = new String(response, "UTF-8");
                return new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private JSONObject getBrandedJSON(String url) {
        try {
            return new RetrieveJSONTask().execute(url).get(10000,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getBrandedJSONURL() {
        return null;
    }

    /**
     * Gets a list of Tile URLs used by this layer for a specific tile.
     *
     * @param aTile a map tile
     * @param hdpi a boolean that indicates whether the tile should be at 2x or retina size
     * @return a list of tile URLS
     */
    public String[] getTileURLs(final MapTile aTile, boolean hdpi) {
        String url = getTileURL(aTile, hdpi);
        if (url != null) {
            return new String[]{url};
        }
        return null;
    }

    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return null;
    }

    private static final Paint compositePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private Bitmap compositeBitmaps(final Bitmap source, Bitmap dest) {
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, 0, 0, compositePaint);
        return dest;
    }

    @Override
    public CacheableBitmapDrawable getDrawableFromTile(final MapTileDownloader downloader, final MapTile aTile, boolean hdpi) {
        if (downloader.isNetworkAvailable()) {
            TilesLoadedListener listener = downloader.getTilesLoadedListener();

            String[] urls = getTileURLs(aTile, hdpi);
            CacheableBitmapDrawable result = null;
            Bitmap resultBitmap = null;
            if (urls != null) {
                MapTileCache cache = downloader.getCache();
                if (listener != null) {
                    listener.onTilesLoadStarted();
                }
                for (final String url : urls) {
                    Bitmap bitmap = getBitmapFromURL(url, cache);
                    if (bitmap == null) {
                        continue;
                    }
                    if (resultBitmap == null) {
                        resultBitmap = bitmap;
                    } else {
                        resultBitmap = compositeBitmaps(bitmap, resultBitmap);
                    }
                }
                if (resultBitmap != null) {
                    //get drawable by putting it into cache (memory and disk)
                    result = cache.putTileBitmap(aTile, resultBitmap);
                }
                if (checkThreadControl()) {
                    if (listener != null) {
                        listener.onTilesLoaded();
                    }
                }
            }

            if (result != null) {
                TileLoadedListener listener2 = downloader.getTileLoadedListener();
                result = listener2 != null ? listener2.onTileLoaded(result) : result;
            }

            return result;
        } else {
            Log.d(TAG, "Skipping tile " + aTile.toString() + " due to NetworkAvailabilityCheck.");
        }
        return null;
    }

    /**
     * Requests and returns a bitmap object from a given URL, using aCache to decode it.
     *
     * @param url the map tile url. should refer to a valid bitmap resource.
     * @param aCache a cache, an instance of MapTileCache
     * @return the tile if valid, otherwise null
     */
    public Bitmap getBitmapFromURL(final String url, final MapTileCache aCache) {
        // We track the active threads here, every exit point should decrement this value.
        activeThreads.incrementAndGet();
        InputStream in = null;
        OutputStream out = null;

        OkHttpClient client = new OkHttpClient();
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (GeneralSecurityException e) {
            activeThreads.decrementAndGet();
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
        client.setSslSocketFactory(sslContext.getSocketFactory());

        if (cache != null) {
            client.setResponseCache(cache);
        }

        if (TextUtils.isEmpty(url)) {
            activeThreads.decrementAndGet();
            return null;
        }

        try {
            HttpURLConnection connection = client.open(new URL(url));
            in = connection.getInputStream();

            if (in == null) {
                Log.d(TAG, "No content downloading MapTile: " + url);
                return null;
            }

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
            StreamUtils.copy(in, out);
            out.flush();
            final byte[] data = dataStream.toByteArray();
            return aCache.decodeBitmap(data, null);
        } catch (final Throwable e) {
            Log.d(TAG, "Error downloading MapTile: " + url + ":" + e);
        } finally {
            StreamUtils.closeStream(in);
            StreamUtils.closeStream(out);
            activeThreads.decrementAndGet();
        }
        return null;
    }
}
