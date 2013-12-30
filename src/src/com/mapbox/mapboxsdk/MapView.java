package com.mapbox.mapboxsdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapView extends org.osmdroid.views.MapView implements MapEventsReceiver {
    ////////////
    // FIELDS //
    ////////////

    /**
     * The current tile source for the view (to be deprecated soon)
     */
    private ITileSource tileSource;
    /**
     * The default marker Overlay, automatically added to the view to add markers directly
     */
    private ItemizedIconOverlay<OverlayItem> defaultMarkerOverlay;
    /**
     * List linked to the default marker overlay
     */
    private ArrayList<OverlayItem> defaultMarkerList = new ArrayList<OverlayItem>();
    /**
     * Overlay for basic map touch events
     */
    private MapEventsOverlay eventsOverlay;
    /**
     * A copy of the app context
     */
    private Context context;
    /**
     * Whether or not a marker has been placed already
     */
    private boolean firstMarker = true;

    /**
     * Constructor for XML layout calls. Should not be used programmatically
     * @param context A copy of the app context
     * @param attrs An AttributeSet object to get extra info from the XML, such as mapbox id or type of baselayer
     */
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setURL("");
        eventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(eventsOverlay);
        this.setMultiTouchControls(true);
    }

    /**
     * Default constructor for the view
     * @param context A copy of the app context
     * @param URL Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     */
    public MapView(Context context, String URL){
        this(context, (AttributeSet) null);
        setURL(parseURL(URL));
    }

    /**
     * Parses the passed ID string to use the relevant method
     * @param url Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     * @return the standard URL to be used by the library
     **/
    private String parseURL(String url) {
        if(url.contains("json")) return getURLFromTileJSON(url);
        if(!url.contains("http://")) return getURLFromMapBoxID(url);
        if(url.contains(".png")) return getURLFromImageTemplate(url);
        else{
            throw new IllegalArgumentException("You need to enter either a valid URL, a MapBox id, or a tile URL template");
        }
        
    }


    protected MapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
        super(context, tileSizePixels, resourceProxy, aTileProvider);
        init(context);
    }

    public void setURL(String URL){

        if(!URL.equals("")) {
            URL = parseURL(URL);
            tileSource = new XYTileSource(getApplicationName(), ResourceProxy.string.online_mode, 0, 24, 256, ".png", URL);
            this.setTileSource(tileSource);
        }

    }
    public void removeLayer(String identifier){

    }
    public void switchToLayer(String identifier){

    }

    public void addLayer(final String identifier, String URL){
        URL = parseURL(URL);
        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context.getApplicationContext());
        final ITileSource tileSource = new XYTileSource(identifier, null, 1, 16, 256, ".png", URL);
        tileProvider.setTileSource(tileSource);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        this.getOverlays().add(tilesOverlay);
    }



    /////////////////////
    // PRIVATE METHODS //
    /////////////////////

    private void init(Context context) {
        this.context = context;
        setURL("");
        eventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(eventsOverlay);
        this.setMultiTouchControls(true);
    }

    private String getApplicationName(){
        return context.getPackageName();
    }

    private String getURLFromMapBoxID(String mapBoxID){
        if(!mapBoxID.contains(".")){
            throw new IllegalArgumentException("Invalid MapBox ID, entered "+mapBoxID);
        }
        String completeURL = "http://a.tiles.mapbox.com/v3/"+mapBoxID+"/";
        return completeURL;
    }

    private String getURLFromTileJSON(String tileJSONURL){
        return tileJSONURL.replace(".json", "/");
    }
    private String getURLFromTilemill(){
        return null;
    }
    private String getURLFromImageTemplate(String imageTemplateURL){
        return imageTemplateURL.replace("/{z}/{x}/{y}.png", "/");
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
            GeoJSONElement geoJSONElement = new GeoJSONElement(URL);
            return geoJSONElement.getMarkersOverlay();
        }
        @TargetApi(Build.VERSION_CODES.CUPCAKE)
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
