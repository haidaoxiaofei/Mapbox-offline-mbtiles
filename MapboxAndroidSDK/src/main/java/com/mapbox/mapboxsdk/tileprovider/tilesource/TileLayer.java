package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileCache;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

public class TileLayer implements ITileLayer, TileLayerConstants, MapViewConstants {

    protected String mUrl;
    protected String mName;
    protected String mDescription;
    protected String mShortAttribution;
    protected String mLongAttribution;
    protected String mLegend;

    protected float mMinimumZoomLevel = TileLayerConstants.MINIMUM_ZOOMLEVEL;
    protected float mMaximumZoomLevel = TileLayerConstants.MAXIMUM_ZOOMLEVEL;
    protected BoundingBox mBoundingBox = null;
    protected LatLng mCenter = new LatLng(0, 0);
    private final int mTileSizePixels = DEFAULT_TILE_SIZE;

    public TileLayer(final String aUrl) {
        mUrl = aUrl;
    }

    public TileLayer setURL(final String aUrl) {
        mUrl = aUrl;
        return this;
    }

    public Drawable getDrawableFromTile(final MapTileDownloader downloader, final MapTile aTile, boolean hdpi) {
        return null;
    }

    @Override
    public void detach() {

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

    final private String TAG = "OnlineTileSource";

    @Override
    public String getCacheKey() {
        return "";
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public LatLng getCenterCoordinate() {
        return mCenter;
    }


    @Override
    public float getCenterZoom() {
        if (mCenter != null) {
            return (float) mCenter.getAltitude();
        }
        return Math.round(mMaximumZoomLevel + mMinimumZoomLevel) / 2;
    }

    @Override
    public String getShortName() {
        return mName;
    }

    @Override
    public String getLongDescription() {
        return mDescription;
    }

    @Override
    public String getShortAttribution() {
        return mShortAttribution;
    }

    @Override
    public String getLongAttribution() {
        return mLongAttribution;
    }

    @Override
    public String getLegend() {
        return mLegend;
    }
}
