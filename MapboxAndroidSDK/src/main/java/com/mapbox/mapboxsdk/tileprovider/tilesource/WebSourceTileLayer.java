package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.tileprovider.util.StreamUtils;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.UUID;

import javax.net.ssl.SSLContext;

public class WebSourceTileLayer extends TileLayer {
    private static final String TAG = "WebSourceTileLayer";
    HttpResponseCache cache;
    ArrayList<Boolean> threadControl = new ArrayList<Boolean>();

    public WebSourceTileLayer(final String aUrl) {
        super(aUrl);
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            cache = new HttpResponseCache(cacheDir, 1024);
        } catch(Exception e) {

        }
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

    @Override
    public Drawable getDrawableFromTile(final MapTileDownloader downloader, final MapTile aTile, boolean hdpi) {
        if (downloader.isNetworkAvailable()) {
            return getDrawableFromURL(downloader, getTileURL(aTile, hdpi));
        }
        else {
            Log.d(TAG, "Skipping tile " + aTile.toString() + " due to NetworkAvailabilityCheck.");
        }
        return null;
    }

    public Drawable getDrawableFromURL(final MapTileDownloader downloader, final String url) {
        threadControl.add(false);
        int threadIndex = threadControl.size() - 1;
        InputStream in = null;
        OutputStream out = null;

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

        if (TextUtils.isEmpty(url)) {
            return null;
        }

        try {
            // Log.d(TAG, "getting tile " + tile.getX() + ", " + tile.getY());
            // Log.d(TAG, "Downloading MapTile from url: " + url);
            TilesLoadedListener listener = downloader.getTilesLoadedListener();
            if (listener != null) {
                listener.onTilesLoadStarted();
            }
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
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

            Drawable result = getDrawable(byteStream);
            threadControl.set(threadIndex, true);
            if (checkThreadControl()) {
                if (listener != null) {
                    listener.onTilesLoaded();
                }
            }

            TileLoadedListener listener2 = downloader.getTileLoadedListener();
            result = listener2 != null ? listener2.onTileLoaded(result) : result;
            return result;
        } catch (final Throwable e) {
            Log.d(TAG, "Error downloading MapTile: " + url + ":" + e);
        } finally {
            StreamUtils.closeStream(in);
            StreamUtils.closeStream(out);
        }
        return null;
    }
}
