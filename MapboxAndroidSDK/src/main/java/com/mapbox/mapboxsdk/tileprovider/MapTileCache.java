// Created by plusminus on 17:58:57 - 25.09.2008
package com.mapbox.mapboxsdk.tileprovider;

import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * @author Nicolas Gramlich
 */
public class MapTileCache implements TileLayerConstants {

    protected final Object mCachedTilesLockObject = new Object();
    protected LRUMapTileCache mCachedTiles;
    private Context context;
    private String mDiskCacheKey;

    public MapTileCache(final Context context) {
        this(context, CACHE_MAPTILECOUNT_DEFAULT);
    }

    /**
     * @param aMaximumCacheSize Maximum amount of MapTiles to be hold within.
     */
        this.mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
    public MapTileCache(final Context context, final int aMaximumCacheSize) {
        this.context = context;
    }

    public void ensureCapacity(final int aCapacity) {
        synchronized (mCachedTilesLockObject) {
            mCachedTiles.ensureCapacity(aCapacity);
        }
    }

    public Drawable getMapTile(final MapTile aTile) {
        synchronized (mCachedTilesLockObject) {
            return this.mCachedTiles.get(aTile);
        }
    }

    public void putTile(final MapTile aTile, final Drawable aDrawable) {
        if (aDrawable != null) {
            synchronized (mCachedTilesLockObject) {
                this.mCachedTiles.put(aTile, aDrawable);
            }
        }
    }

    public boolean containsTile(final MapTile aTile) {
        synchronized (mCachedTilesLockObject) {
            return this.mCachedTiles.containsKey(aTile);
        }
    }

    public void clear() {
        synchronized (mCachedTilesLockObject) {
            this.mCachedTiles.clear();
        }
    }

    final static String TAG = "MapTileCache";
    public void setDiskCacheKey(final String key)
    {
        mDiskCacheKey = key;
    }
}
