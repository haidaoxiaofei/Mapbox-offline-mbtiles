package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileCache;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.util.LowMemoryException;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

import java.io.InputStream;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class TileLayer implements ITileLayer, TileLayerConstants, MapViewConstants {

	protected String mUrl;
    protected float mMinimumZoomLevel = 1;
    protected float mMaximumZoomLevel = 16;
    protected BoundingBox mBoundingBox = null;
    protected LatLng mCenter = new LatLng(0,0);
    private final int mTileSizePixels = DEFAULT_TILE_SIZE;

    public TileLayer(final String aUrl) {
        mUrl = aUrl;
    }

    public TileLayer setURL(final String aUrl) {
        mUrl = aUrl;
        return this;
    }

    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return mUrl
                .replace("{z}", String.valueOf(aTile.getZ()))
                .replace("{x}", String.valueOf(aTile.getX()))
                .replace("{y}", String.valueOf(aTile.getY()))
                .replace("{2x}", hdpi ? "@2x" : "");
    }

    @Override
    public float getMinimumZoomLevel() {
        return mMinimumZoomLevel;
    }

    @Override
    public float getMaximumZoomLevel() {
        return mMaximumZoomLevel;
    }

    @Override
    public int getTileSizePixels() {
        return mTileSizePixels;
    }

    @Override
    public Drawable getDrawable(final MapTile aTile, final MapTileCache aCache, final Resources resources, final InputStream aFileInputStream) throws LowMemoryException {
        try {
            // default implementation will load the file as a bitmap and create
            // a CacheableBitmapDrawable from it
            //also caching is handled here to be optimized
            return aCache.putTileStream(aTile, aFileInputStream, new BitmapFactory.Options());

        } catch (final OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError loading bitmap");
            System.gc();
            throw new LowMemoryException(e);
        }
    }

    final private String TAG = "OnlineTileSource";

	@Override
	public BoundingBox getBoundingBox() {
		return mBoundingBox;
}

    @Override
    public String getCacheKey() { return "";}
}
