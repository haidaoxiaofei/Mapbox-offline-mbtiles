package com.mapbox.mapboxsdk.tileprovider;

import java.util.LinkedHashMap;

import com.mapbox.mapboxsdk.tileprovider.constants.OpenStreetMapTileProviderConstants;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * A least-recently used style cache that stores a set number of
 * tiles at a time.
 */
public class LRUMapTileCache extends LinkedHashMap<MapTile, Drawable>
        implements OpenStreetMapTileProviderConstants {

    public interface TileRemovedListener {
        void onTileRemoved(MapTile mapTile);
    }

    private static final long serialVersionUID = -541142277575493335L;

    private int mCapacity;
    private TileRemovedListener mTileRemovedListener;

    /**
     * Initialize a map tile cache with a desired capacity
     * @param aCapacity desired maximum capacity
     */
    public LRUMapTileCache(final int aCapacity) {
        super(aCapacity + 2, 0.1f, true);
        mCapacity = aCapacity;
    }

    /**
     * Ensure that the capacity is as large as the given argument, and if it isn't,
     * expand it to that argument.
     * @param aCapacity the desired maximum capacity
     */
    public void ensureCapacity(final int aCapacity) {
        if (aCapacity > mCapacity) {
            Log.e(TAG, "Tile cache increased from " + mCapacity + " to " + aCapacity);
            mCapacity = aCapacity;
        }
    }

    @Override
    public Drawable remove(final Object aKey) {
        final Drawable drawable = super.remove(aKey);
        // Only recycle if we are running on a project less than 2.3.3 Gingerbread.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (drawable instanceof BitmapDrawable) {
                final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
        if (getTileRemovedListener() != null && aKey instanceof MapTile)
            getTileRemovedListener().onTileRemoved((MapTile) aKey);
        if (drawable instanceof ReusableBitmapDrawable)
            BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) drawable);
        return drawable;
    }

    @Override
    public void clear() {
        // remove them all individually so that they get recycled
        while (!isEmpty()) {
            remove(keySet().iterator().next());
        }

        // and then clear
        super.clear();
    }

    @Override
    protected boolean removeEldestEntry(final java.util.Map.Entry<MapTile, Drawable> aEldest) {
        if (size() > mCapacity) {
            final MapTile eldest = aEldest.getKey();
            if (DEBUGMODE) {
                Log.i(TAG, "Remove old tile: " + eldest);
            }
            remove(eldest);
            // don't return true because we've already removed it
        }
        return false;
    }

    public TileRemovedListener getTileRemovedListener() {
        return mTileRemovedListener;
    }

    public void setTileRemovedListener(TileRemovedListener tileRemovedListener) {
        mTileRemovedListener = tileRemovedListener;
    }

    private static final String TAG = "LRUMapTileCache";
}
