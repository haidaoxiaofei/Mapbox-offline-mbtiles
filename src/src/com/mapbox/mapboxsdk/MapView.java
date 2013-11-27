package com.mapbox.mapboxsdk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        this.getOverlays().add(eventsOverlay);
        this.setMultiTouchControls(true);
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

    public void addItemizedOverlay(ItemizedOverlay<Marker> itemizedOverlay){
        this.getOverlays().add(itemizedOverlay);
    }

    public class MarkerFactory {
        public ItemizedOverlay<Marker> fromGeoJSON(String URL){

            return null;
        }
        private class JSONBodyGetter extends AsyncTask<String, Void, JSONObject> {
            @Override
            protected JSONObject doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);
                    return new JSONObject(responseStrBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {

            }
        }
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
        onTap(p);
        return true;
    }

    @Override
    public boolean longPressHelper(IGeoPoint p) {
        onLongPress(p);
        return false;
    }

    public void onLongPress(IGeoPoint p){
    }
    public void onTap(IGeoPoint p){
    }


}
