package com.mapbox.mapboxsdk.tileprovider.tilesource;

import java.io.File;
import java.io.InputStream;
import java.util.Random;

import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.ResourceProxy.string;
import com.mapbox.mapboxsdk.tileprovider.BitmapPool;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.ReusableBitmapDrawable;
import com.mapbox.mapboxsdk.tileprovider.constants.OpenStreetMapTileProviderConstants;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public abstract class BitmapTileSourceBase implements ITileSource,
        OpenStreetMapTileProviderConstants {

    private static int globalOrdinal = 0;

    private final int mMinimumZoomLevel;
    private final int mMaximumZoomLevel;

    private final int mOrdinal;
    protected final String mName;
    protected final String mImageFilenameEnding;
    protected final Random random = new Random();

    private final int mTileSizePixels;

    private final string mResourceId;

    public BitmapTileSourceBase(final String aName,
                                final string aResourceId,
                                final int aZoomMinLevel,
                                final int aZoomMaxLevel,
                                final int aTileSizePixels,
                                final String aImageFilenameEnding) {
        mResourceId = aResourceId;
        mOrdinal = globalOrdinal++;
        mName = aName;
        mMinimumZoomLevel = aZoomMinLevel;
        mMaximumZoomLevel = aZoomMaxLevel;
        mTileSizePixels = aTileSizePixels;
        mImageFilenameEnding = aImageFilenameEnding;
    }

    @Override
    public int ordinal() {
        return mOrdinal;
    }

    @Override
    public String name() {
        return mName;
    }

    @Override
    public int getMinimumZoomLevel() {
        return mMinimumZoomLevel;
    }

    @Override
    public int getMaximumZoomLevel() {
        return mMaximumZoomLevel;
    }

    @Override
    public int getTileSizePixels() {
        return mTileSizePixels;
    }

    @Override
    public String localizedName(final ResourceProxy proxy) {
        return proxy.getString(mResourceId);
    }

    @Override
    public Drawable getDrawable(final InputStream aFileInputStream) throws LowMemoryException {
        try {
            // default implementation will load the file as a bitmap and create
            // a BitmapDrawable from it
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            BitmapPool.getInstance().applyReusableOptions(bitmapOptions);
            final Bitmap bitmap = BitmapFactory.decodeStream(aFileInputStream, null, bitmapOptions);
            if (bitmap != null) {
                return new ReusableBitmapDrawable(bitmap);
            }
        } catch (final OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError loading bitmap");
            System.gc();
            throw new LowMemoryException(e);
        }
        return null;
    }

    public final class LowMemoryException extends Exception {
        private static final long serialVersionUID = 146526524087765134L;

        public LowMemoryException(final String pDetailMessage) {
            super(pDetailMessage);
        }

        public LowMemoryException(final Throwable pThrowable) {
            super(pThrowable);
        }
    }

    private static final String TAG = "BitmapTileSourceBase";
}
