package com.mapbox.mapboxsdk;

import android.content.Context;
import android.util.AttributeSet;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapView extends org.osmdroid.views.MapView implements MapEventsReceiver {
    private ITileSource tileSource;
    private MapController controller;
    private ItemizedIconOverlay<OverlayItem> defaultMarkerOverlay;
    private ArrayList<OverlayItem> defaultMarkerList = new ArrayList<OverlayItem>();
    private MapEventsOverlay eventsOverlay;

    private Context context;
    private boolean firstMarker = true;

    public MapView(Context context, AttributeSet attrs) {
        this(context, "");
    }
    public MapView(Context context, String URL){
        super(context, null);
        this.context = context;
        tileSource = new XYTileSource("Test", ResourceProxy.string.online_mode, 0, 24, 256, ".png", URL);
        this.setTileSource(tileSource);
        eventsOverlay = new MapEventsOverlay(context, this);
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
        Marker marker = new Marker(this, title, text, new GeoPoint(lat, lon));
        if(firstMarker){
            defaultMarkerList.add(marker);
            setDefaultItemizedOverlay();
        }
        else{
            defaultMarkerOverlay.addItem(marker);
        }
        this.invalidate();
        firstMarker = false;
        return null;
    }
    private void setDefaultItemizedOverlay() {
        defaultMarkerOverlay = new ItemizedIconOverlay<OverlayItem>(
                defaultMarkerList,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        MapView.this.getOverlays().add(new Tooltip(context, item , item.getTitle()));
                        MapView.this.invalidate();
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, new DefaultResourceProxyImpl(context.getApplicationContext()));
        this.getOverlays().add(defaultMarkerOverlay);
    }
    @Override
    public boolean singleTapUpHelper(IGeoPoint p) {
        this.addMarker(p.getLatitude(), p.getLongitude(), "", "");
        return true;
    }

    @Override
    public boolean longPressHelper(IGeoPoint p) {
        return false;
    }


}
