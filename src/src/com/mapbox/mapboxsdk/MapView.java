package com.mapbox.mapboxsdk;

import android.content.Context;
import android.util.AttributeSet;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapController;

public class MapView extends org.osmdroid.views.MapView{
    private MapTileProviderBasic tileProvider;
    private ITileSource tileSource;
    private MapController controller;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MapView(Context context, String URL){
        super(context, null);
        tileSource = new XYTileSource("Test", ResourceProxy.string.online_mode, 0, 24, 256, ".png", URL);
        this.setTileSource(tileSource);
    }
}
