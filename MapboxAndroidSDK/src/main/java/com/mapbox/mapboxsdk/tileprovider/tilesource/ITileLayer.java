package com.mapbox.mapboxsdk.tileprovider.tilesource;

import java.io.InputStream;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.tileprovider.util.LowMemoryException;

import android.graphics.drawable.Drawable;

public interface ITileLayer {

    public void detach();
    /**
     * Get a rendered Drawable from the specified Tile.
     *
     * @param downloader reference to the downloader asking for the tile
     * @param aTile the tile requested
     * @param hdpi is hdpi requested?
     * @return the rendered Drawable
     */
    public Drawable getDrawableFromTile(final MapTileDownloader downloader, final MapTile aTile, boolean hdpi);

    /**
     * Set the current tile url template used in this layer
     *
     * @return the tile layer
     */
    public TileLayer setURL(final String aUrl);

    /**
     * Get the current tile url template used in this layer
     *
     * @return tile url string as a template string
     */
    public String getTileURL(final MapTile aTile, boolean hdpi);

    /**
     * Get the minimum zoom level this tile source can provide.
     *
     * @return the minimum zoom level
     */
    public float getMinimumZoomLevel();

    /**
     * Get the maximum zoom level this tile source can provide.
     *
     * @return the maximum zoom level
     */
    public float getMaximumZoomLevel();

    /**
     * Get the tile size in pixels this tile source provides.
     *
     * @return the tile size in pixels
     */
    public int getTileSizePixels();
    
    /**
     * Get the tile source bounding box.
     *
     * @return the tile source bounding box
     */
	public BoundingBox getBoundingBox();

    /**
     * Get the tile source center.
     *
     * @return the tile source center
     */
    public LatLng getCenterCoordinate();

    /**
     * Get the tile source suggested starting zoom.
     *
     * @return the tile suggested starting zoom
     */
    public float getCenterZoom();

    /**
     * Get the tile source short name
     *
     * @return the short name
     */
    public String getShortName();

    /**
     * Get the tile source description
     *
     * @return the description
     */
    public String getLongDescription();

    /**
     * Get the tile source short attribution
     *
     * @return the short attribution
     */
    public String getShortAttribution();

    /**
     * Get the tile source long attribution
     *
     * @return the long attribution
     */
    public String getLongAttribution();

    /**
     * Get the tile source legend
     *
     * @return the legend
     */
    public String getLegend();

}
