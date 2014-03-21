// Created by plusminus on 17:58:57 - 25.09.2008
package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.InputStream;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * @author Nicolas Gramlich
 */
public class MapTileCache implements TileLayerConstants {

    protected final Object mCachedTilesLockObject = new Object();
    protected static BitmapLruCache mCachedTiles = null;
    private Context context;
    private String mDiskCacheKey;
    final static String TAG = "MapTileCache";
    private static final String DISK_CACHE_SUBDIR = "mapbox_tiles_cache";

    public MapTileCache(final Context context) {
        this(context, CACHE_MAPTILECOUNT_DEFAULT);
    }

    /**
     * @param aMaximumCacheSize Maximum amount of MapTiles to be hold within.
     */
    public MapTileCache(final Context context, final int aMaximumCacheSize) {
        this.context = context;
        if(mCachedTiles == null) {
            File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
            if (!cacheDir.exists()) {
                if (cacheDir.mkdirs()) {
                    Log.d(TAG, "creating cacheDir " + cacheDir);
                }
                else {
                    Log.e(TAG, "can't create cacheDir " + cacheDir);
                }
            }
            BitmapLruCache.Builder builder = new BitmapLruCache.Builder(context);
            builder.setMemoryCacheEnabled(true)
                    .setMemoryCacheMaxSizeUsingHeapSize()
                    .setDiskCacheEnabled(true)
                    .setDiskCacheMaxSize(10*1024*1024)
                    .setDiskCacheLocation(cacheDir);
            this.mCachedTiles = builder.build();
        }

    }

    public void ensureCapacity(final int aCapacity) {
//        synchronized (mCachedTilesLockObject) {
//            mCachedTiles.ensureCapacity(aCapacity);
//        }
    }

    public String getCacheKey(MapTile aTile) {
        return mDiskCacheKey + aTile.toString();
    }

    public CacheableBitmapDrawable getMapTile(final MapTile aTile) {
        String key = getCacheKey(aTile);
        CacheableBitmapDrawable result = this.mCachedTiles.getFromMemoryCache(key);
        if (result == null) {
            result = this.mCachedTiles.getFromDiskCache(key, null);
        }
        return result;
    }

    public CacheableBitmapDrawable getMapTileFromMemory(final MapTile aTile) {
        return this.mCachedTiles.getFromMemoryCache(getCacheKey(aTile));
    }

    public CacheableBitmapDrawable getMapTileFromDisk(final MapTile aTile) {
        return this.mCachedTiles.getFromDiskCache(getCacheKey(aTile), null);
    }

    public CacheableBitmapDrawable putTileStream(final MapTile aTile, final InputStream inputStream,
                                                 final BitmapFactory.Options decodeOpts) {
        return this.mCachedTiles.put(getCacheKey(aTile), inputStream, decodeOpts);
    }

    public CacheableBitmapDrawable putTile(final MapTile aTile, final Drawable aDrawable) {
        if (aDrawable != null && aDrawable instanceof BitmapDrawable) {
            String key = getCacheKey(aTile);
            CacheableBitmapDrawable drawable = null;
            if (!this.mCachedTiles.containsInMemoryCache(key)) {
                drawable = this.mCachedTiles.putInMemoryCache(getCacheKey(aTile), ((BitmapDrawable) aDrawable).getBitmap());
            }
            if (!this.mCachedTiles.containsInDiskCache(key)) {
                if (drawable != null) {
                    this.mCachedTiles.putInDiskCache(getCacheKey(aTile), drawable);
                }
                else {
                   this.mCachedTiles.putInDiskCache(getCacheKey(aTile), ((BitmapDrawable) aDrawable).getBitmap());
                }
            }
            return drawable;
        }
        return null;
    }

    public CacheableBitmapDrawable putTileInMemoryCache(final MapTile aTile, final Bitmap aBitmap) {
        if (aBitmap != null) {
            String key = getCacheKey(aTile);
            if (!this.mCachedTiles.containsInMemoryCache(key)) {
                return this.mCachedTiles.putInMemoryCache(getCacheKey(aTile), aBitmap);
            }
        }
        return null;
    }

    public CacheableBitmapDrawable putTileInMemoryCache(final MapTile aTile, final Drawable aDrawable) {
        if (aDrawable != null && aDrawable instanceof BitmapDrawable) {
            String key = getCacheKey(aTile);
            if (!this.mCachedTiles.containsInMemoryCache(key)) {
                if (aDrawable instanceof CacheableBitmapDrawable) {
                    return this.mCachedTiles.putInMemoryCache(getCacheKey(aTile), ((CacheableBitmapDrawable) aDrawable));
                }
                else {
                    return this.mCachedTiles.putInMemoryCache(getCacheKey(aTile), ((BitmapDrawable) aDrawable).getBitmap());
                }
            }
        }
        return null;
    }

    public CacheableBitmapDrawable putTileInDiskCache(final MapTile aTile, final Drawable aDrawable) {
        if (aDrawable != null && aDrawable instanceof BitmapDrawable) {
            String key = getCacheKey(aTile);
            if (!this.mCachedTiles.containsInDiskCache(key)) {
                return this.mCachedTiles.putInDiskCache(getCacheKey(aTile), ((BitmapDrawable) aDrawable).getBitmap());
            }
        }
        return null;
    }

    public boolean containsTile(final MapTile aTile) {
        return this.mCachedTiles.contains(getCacheKey(aTile));
    }

    public boolean containsTileInDiskCache(final MapTile aTile) {
        return this.mCachedTiles.containsInDiskCache(getCacheKey(aTile));
    }

    public void removeTile(final MapTile aTile) {
        this.mCachedTiles.remove(getCacheKey(aTile));
    }

    public void removeTileFromMemory(final MapTile aTile) {
        String key = getCacheKey(aTile);
        this.mCachedTiles.removeFromMemoryCache(key);
    }

    public void clear() {
        this.mCachedTiles.trimMemory();
    }

    public CacheableBitmapDrawable createCacheableBitmapDrawable(Bitmap bitmap, MapTile aTile)
    {
        return this.mCachedTiles.createCacheableBitmapDrawable(bitmap, getCacheKey(aTile), CacheableBitmapDrawable.SOURCE_UNKNOWN);
    }


    Bitmap getBitmapFromRemoved(final int width, final int height) {
        return this.mCachedTiles.getBitmapFromRemoved(width, height);
    }

    public void setDiskCacheKey(final String key)
    {
        mDiskCacheKey = key;
    }
    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? Environment.getExternalStorageDirectory().getPath() :
                        context.getFilesDir().getPath();

        return new File(cachePath, uniqueName);
    }
}
