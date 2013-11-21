package com.mapbox.mapboxsdk;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;


public class MapView extends org.osmdroid.views.MapView {
    public MapView(Context context){
        super(context);
    }
    private MapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs) {
        super(context, tileSizePixels, resourceProxy, tileProvider, tileRequestCompleteHandler, attrs);
    }
}
