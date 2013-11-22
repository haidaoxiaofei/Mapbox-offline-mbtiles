package com.mapbox.mapboxsdk;

import android.content.Context;
import android.util.AttributeSet;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapView extends org.osmdroid.views.MapView{
    private ITileSource tileSource;
    private MapController controller;
    private ItemizedIconOverlay<OverlayItem> defaultMarkerOverlay;
    private ArrayList<OverlayItem> defaultMarkerList = new ArrayList<OverlayItem>();
    private Context context;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public MapView(Context context, String URL){
        super(context, null);
        this.context = context;
        tileSource = new XYTileSource("Test", ResourceProxy.string.online_mode, 0, 24, 256, ".png", URL);
        this.setTileSource(tileSource);
    }
    /**
     * Adds a marker to the default marker overlay
     * @param lat latitude of the marker
     * @param lon longitude of the marker
     * @param title title of the marker
     * @param text body of the marker's tooltip
     * @return the marker object
     */

    public Marker addMarker(double lat, double lon, String title, String text){
        defaultMarkerList.add(new Marker(title, text, new GeoPoint(lat, lon)));
        setDefaultItemizedOverlay();
        this.getOverlays().add(defaultMarkerOverlay);
        this.invalidate();
        return null;
    }
    private void setDefaultItemizedOverlay() {
        System.out.println(defaultMarkerList);
        System.out.println(context.getApplicationContext()==null);
        defaultMarkerOverlay = new ItemizedIconOverlay<OverlayItem>(
                defaultMarkerList,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        MapView.this.getOverlays().add(new Tooltip(context, item));
                        MapView.this.invalidate();

                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, new DefaultResourceProxyImpl(context.getApplicationContext()));
    }



}
