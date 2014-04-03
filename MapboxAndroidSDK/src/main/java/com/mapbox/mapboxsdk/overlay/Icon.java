package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.squareup.okhttp.OkHttpClient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * An Icon provided by the Mapbox marker API, optionally
 * with a symbol from Maki
 */
public class Icon implements MapboxConstants {

    private Marker marker;
    private BitmapDrawable drawable;

    protected static BitmapLruCache sIconCache;
    private static final String DISK_CACHE_SUBDIR = "mapbox_icon_cache";

    // Well, we only want to download the same URL once. If we request the same url rapidly
    // We place it in this queue..
    private static ConcurrentHashMap<String, ArrayList<Icon>> downloadQueue = new ConcurrentHashMap<String, ArrayList<Icon>>();

    public enum Size {
        LARGE("l"), MEDIUM("m"), SMALL("s");

        private String apiString;

        Size(String api) {
            this.apiString = api;
        }

        public String getApiString() {
            return apiString;
        }
    }

    // TODO: This is common code from MapTileCache, ideally this would be extracted
    // and used by both classes.
    protected BitmapLruCache getCache(Context context) {
        if (sIconCache == null) {
            File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
            if (!cacheDir.exists()) {
                if (cacheDir.mkdirs()) {
                    Log.d(TAG, "creating cacheDir " + cacheDir);
                } else {
                    Log.e(TAG, "can't create cacheDir " + cacheDir);
                }
            }
            BitmapLruCache.Builder builder = new BitmapLruCache.Builder(context);
            builder.setMemoryCacheEnabled(true)
                    .setMemoryCacheMaxSizeUsingHeapSize()
                    .setDiskCacheEnabled(true)
                    // 1 MB (a marker image is around 1kb)
                    .setDiskCacheMaxSize(1024 * 1024)
                    .setDiskCacheLocation(cacheDir);
            sIconCache = builder.build();
        }
        return sIconCache;
    }

    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external
     * but if not mounted, falls back on internal storage.
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || (!Environment.isExternalStorageRemovable())
                        ? Environment.getExternalStorageDirectory().getPath() : context.getFilesDir().getPath();
        return new File(cachePath, uniqueName);
    }

    /**
     * Initialize an icon with size, symbol, and color, and start a
     * download process to load it from the API.
     *
     * @param context Android context - Used for proper Bitmap Density generation
     * @param size    Size of Icon
     * @param symbol  Maki Symbol
     * @param color   Color of Icon
     */
    public Icon(Context context, Size size, String symbol, String color) {
        String url = MAPBOX_BASE_URL + "marker/pin-" + size.getApiString();
        String colr = color;
        if (color.startsWith("#")) {
            colr = color.substring(1);
        }
        if (!symbol.equals("")) {
            url += "-" + symbol + "+" + colr + ".png";
        } else {
            url += "+" + colr + ".png";
        }
        downloadBitmap(context, url);
    }

    private void downloadBitmap(Context context, String url) {
        CacheableBitmapDrawable bitmap = getCache(context).get(url);

        // Cache hit! We're done..
        if (bitmap != null) {
        	drawable = bitmap;
            if (marker != null) marker.setMarker(drawable);
            return;
        }

        // Ok, we want to download a url only once. So if we have multiple requests coming in in
        // a short period of time we will batch them up..

        // The idea is simple. We have a map url->[list of icons wanting that image]
        // The first icon in the list will kick of a downloader on the background.
        // any incoming requests that resulted in a cache miss with the downloader active
        // will be added to the list.
        // Once the downloader finishes, it will notify every icon that the image is there.
        if (Icon.downloadQueue.putIfAbsent(url, new ArrayList<Icon>()) == null) {
            // We just placed a new list in the queue, so we will be responsible for
            // kicking off the downloader..
            ArrayList<Icon> list = Icon.downloadQueue.get(url);
            synchronized (list) {
                list.add(this);
                new BitmapLoader().execute(url);
            }
        } else {
            // Okay, another downloader for this url is active, and the bitmap is not
            // yet retrieved..
            ArrayList<Icon> list = Icon.downloadQueue.get(url);

            // Case 1:
            // The download thread just finished up, the list is now removed from the
            // hashmap
            if (list == null) {
                // Note, there is an extremely unlikely chance we are immediately kicked
                // out of the cache...
            	drawable = sIconCache.get(url);
                if (marker != null) marker.setMarker(drawable);
                return;
            }

            synchronized (list) {
                // Case 2:
                // The downloader thread just released the lock, the list is empty.
                // The cache has our icon..
                if (list.isEmpty()) {
                	drawable = sIconCache.get(url);
                    if (marker != null) marker.setMarker(drawable);
                    return;
                }

                // Case 3: The background thread is busy, or waiting to get the lock..
                // We can safely add ourselves to the list to be notified of the retrieved bitmap.
                list.add(this);
            }
        }
    }

    public Icon setMarker(Marker marker) {
        this.marker = marker;
        if (drawable != null) {
            this.marker.setMarker(drawable);
        }
        return this;
    }

    class BitmapLoader extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private String url;

        @Override
        protected CacheableBitmapDrawable doInBackground(String... src) {
            this.url = src[0];
            OkHttpClient client = new OkHttpClient();
            InputStream in = null;
            try {
                try {
                    Log.d(TAG, "Maki url to load = '" + url + "'");
                    HttpURLConnection connection = client.open(new URL(url));
                    // Note, sIconCache cannot be null..
                    return sIconCache.put(src[0], connection.getInputStream());
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: Unable to fetch icon from: " + src[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable bitmap) {
            if (bitmap != null && marker != null) {
                ArrayList<Icon> list = Icon.downloadQueue.get(url);
                synchronized (list) {
                    for (Icon icon : list)
                        if (icon.marker != null) {
                            icon.marker.setMarker(bitmap);
                        }
                    Log.w(TAG, "Loaded:" + url);
                    Icon.downloadQueue.remove(url);
                }
            }
        }

    }

    private static final String TAG = "Icon";
}