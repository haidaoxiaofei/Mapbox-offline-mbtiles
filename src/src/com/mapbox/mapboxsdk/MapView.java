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
import org.json.JSONArray;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
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

    public final static String EXAMPLE_MAP_ID = "examples.map-z2effxa8";

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    /**
     * Constructor for XML layout calls. Should not be used programmatically
     * @param context A copy of the app context
     * @param attrs An AttributeSet object to get extra info from the XML, such as mapbox id or type of baselayer
     */
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setURL(EXAMPLE_MAP_ID);
        eventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(eventsOverlay);
        this.setMultiTouchControls(true);
        final String mapboxID = attrs.getAttributeValue(null, "mapboxID");
        if(mapboxID!=null){
            setURL(mapboxID);
        }
    }

    /**
     * Default constructor for the view
     * @param context A copy of the app context
     * @param URL Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     */
    public MapView(Context context, String URL){
        this(context, (AttributeSet) null);
        setURL(URL);
    }

    protected MapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
        super(context, tileSizePixels, resourceProxy, aTileProvider);
        init(context);
    }

    ////////////////////
    // PUBLIC METHODS //
    ////////////////////


    /**
     * Sets the MapView to use the specified URL
     * @param URL Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     */

    public void setURL(String URL){
        if(!URL.equals("")) {
            URL = parseURL(URL);
            tileSource = new XYTileSource(URL, ResourceProxy.string.online_mode, 0, 24, 256, ".png", URL);
            this.setTileSource(tileSource);
        }

    }

    /**
     * Removes a layer from the list in the MapView
     * @param identifier layer name
     */
    public void removeLayer(String identifier){

    }
    @Deprecated
    public void addLayer(String name){
        this.switchToLayer(name);
    }

    /**
     * Switches the MapView to a layer (tile overlay)
     * @param name Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     */
    public void switchToLayer(String name){
        String URL = parseURL(name);
        final MapTileProviderBasic tileProvider = (MapTileProviderBasic) this.getTileProvider();
        final ITileSource tileSource = new XYTileSource(name, null, 1, 16, 256, ".png", URL);
        tileProvider.setTileSource(tileSource);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        this.getOverlays().clear();
        this.getOverlays().add(tilesOverlay);
        this.getController().animateTo(this.getMapCenter()); // This clears tiles (for some reason)
        this.invalidate();
    }



    /////////////////////
    // PRIVATE METHODS //
    /////////////////////


    /**
     * Parses the passed ID string to use the relevant method
     * @param url Valid MapBox ID, URL of tileJSON file or URL of z/x/y image template
     * @return the standard URL to be used by the library
     **/
    private String parseURL(String url) {
        if(url.contains(".json")) return getURLFromTileJSON(url);
        if(!url.contains("http://")) return getURLFromMapBoxID(url);
        if(url.contains(".png")) return getURLFromImageTemplate(url);
        else{
            throw new IllegalArgumentException("You need to enter either a valid URL, a MapBox id, or a tile URL template");
        }

    }

    /**
     * Method that constructs the view. used in lieu of a constructor
     * @param context a copy of the app context
     */
    private void init(Context context) {
        this.context = context;
        setURL("");
        eventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(eventsOverlay);
        this.setMultiTouchControls(true);
    }

    /**
     * Obtains the name of the application to identify the maps in the filesystem
     * @return the name of the app
     */
    private String getApplicationName(){
        return context.getPackageName();
    }

    /**
     * Turns a Mapbox ID into a standard URL
     * @param mapBoxID the Mapbox ID
     * @return a standard url that will be used by the MapView
     */
    private String getURLFromMapBoxID(String mapBoxID){
        if(!mapBoxID.contains(".")){
            throw new IllegalArgumentException("Invalid MapBox ID, entered "+mapBoxID);
        }
        String completeURL = "http://a.tiles.mapbox.com/v3/"+mapBoxID+"/";
        return completeURL;
    }

    /**
     * Turns a URL TileJSON path to the standard URL format used by the MapView
     * @param tileJSONURL the tileJSON URL
     * @return a standard url that will be used by the MapView
     */
    private String getURLFromTileJSON(String tileJSONURL){
        return tileJSONURL.replace(".json", "/");
    }

    /**
     * Gets a local TileMill address and turns it into a URL for the MapView (not yet implemented)
     * @return a standard url that will be used by the MapView
     */
    private String getURLFromTilemill(){
        return null;
    }

    /**
     * Gets a {xyz} image template URL and turns it into a standard URL for the MapView
     * @param imageTemplateURL the template URL
     * @return a standard url that will be used by the MapView
     */
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

    /**
     * Adds a new ItemizedOverlay to the MapView
     * @param itemizedOverlay the itemized overlay
     */

    public void addItemizedOverlay(ItemizedOverlay<Marker> itemizedOverlay){
        this.getOverlays().add(itemizedOverlay);

    }

    public void parseFromGeoJSON(String URL) {
        new JSONBodyGetter().execute(URL);
    }

    /**
     * Class that generates markers from formats such as GeoJSON
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public class JSONBodyGetter extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            String jsonText = null;
            try {
                is = new URL(params[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

                jsonText = readAll(rd);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonText;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            try {
                parseGeoJSON(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        private void parseGeoJSON(String jsonString) throws JSONException {
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                throw new IllegalArgumentException("Invalid JSON");
            }
            JSONArray features = null;
            try {
                features = (JSONArray)json.get("features");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for(int i = 0; i< (features != null ? features.length() : 0); i++){
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject properties = (JSONObject) feature.get("properties");
                String title = "";
                if(properties.has("title")){
                    title = properties.getString("title");
                }

                JSONObject geometry = null;
                geometry = (JSONObject) feature.get("geometry");
                JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                double lon = (Double)coordinates.get(0);
                double lat = (Double)coordinates.get(1);
                MapView.this.addMarker(lat, lon, title, "");

            }

        }
    }

    /**
     * Sets the default itemized overlay
     */
    private void setDefaultItemizedOverlay() {
        defaultMarkerOverlay = new ItemizedIconOverlay<OverlayItem>(
                defaultMarkerList,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    Marker currentMarker;
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        ((Marker)(item)).setTooltipVisible();

                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, new DefaultResourceProxyImpl(context.getApplicationContext()));
        this.getOverlays().add(defaultMarkerOverlay);
    }

    /////////////////////////
    // IMPLEMENTED METHODS //
    /////////////////////////

    /**
     * Method coming from OSMDroid's tap handler
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */
    @Override
    public boolean singleTapUpHelper(IGeoPoint p) {
        onTap(p);
        return true;
    }

    /**
     * Method coming from OSMDroid's long tap handler
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */

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
