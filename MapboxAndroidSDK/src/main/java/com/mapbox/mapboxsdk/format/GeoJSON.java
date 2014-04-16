package com.mapbox.mapboxsdk.format;

import android.graphics.Paint;
import android.util.Log;
import com.google.common.base.Strings;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


/**
 * A GeoJSON parser.
 */
public class GeoJSON {

    /**
     * Parse a string of GeoJSON data, returning an array of Overlay objects.
     *
     * @param jsonString GeoJSON string
     * @throws JSONException
     */
    public static List<Object> parseString(String jsonString, MapView mv) throws JSONException {
        return parse(new JSONObject(jsonString), mv);
    }

    /**
     * Parse a GeoJSON object into an array of overlays.
     *
     * @param json GeoJSON
     * @param mv a mapview for the overlays to be added to
     * @throws JSONException
     */
    public static List<Object> parse(JSONObject json, MapView mv) throws JSONException {
        ArrayList<Object> uiObjects = new ArrayList<Object>();
        String type = json.optString("type");
        if (Strings.isNullOrEmpty(type)) {
            Log.w(GeoJSON.class.getCanonicalName(), "type is null, so returning.");
            return uiObjects;
        }
        if (type.equalsIgnoreCase("FeatureCollection")) {
            uiObjects.addAll(featureCollectionToLayers(json, mv));
        } else if (type.equalsIgnoreCase("Feature")) {
            uiObjects.addAll(featureToLayer(json, mv));
        }
/*
        // Refresh Map Once To Update Changes From Parsing and Adding
        mv.invalidate();
*/
        return uiObjects;
    }

    /**
     * Given a <a href='http://geojson.org/geojson-spec.html#feature-collection-objects'>GeoJSON
     * FeatureCollection</a>,
     * parse each feature and create layers on the given map
     *
     * @param featureCollection a json object representing a featurecollection
     * @param mv a mapview
     * @throws JSONException
     */
    public static ArrayList<Object> featureCollectionToLayers(JSONObject featureCollection, MapView mv) throws JSONException {
        ArrayList<Object> uiObjects = new ArrayList<Object>();

        JSONArray features = (JSONArray) featureCollection.get("features");
        // foreach is not usable for JSONArray, so longform
        for (int i = 0; i < features.length(); i++) {
            uiObjects.addAll(featureToLayer((JSONObject) features.get(i), mv));
        }
        return uiObjects;
    }

    /**
     * Parse a GeoJSON feature object into some number of overlays, adding them to the overlays
     * array.
     *
     * @param feature GeoJSON
     * @throws JSONException
     */
    public static List<Object> featureToLayer(JSONObject feature, MapView mv) throws JSONException {

        ArrayList<Object> uiObjects = new ArrayList<Object>();

        JSONObject properties = (JSONObject) feature.get("properties");
        String title = properties.optString("title");
        JSONObject geometry = (JSONObject) feature.get("geometry");
        String type = geometry.optString("type");

        if (Strings.isNullOrEmpty(type)) {
            Log.w(GeoJSON.class.getCanonicalName(), "type is null, so can't parse anything.");
            return uiObjects;
        }

        int j;

        // Extract the marker style properties from the GeoJSON
        // (See: https://www.mapbox.com/developers/simplestyle/)
        Icon markerIcon = null;
        String markerColor = properties.optString("marker-color");
        String markerSize = properties.optString("marker-size");
        String markerSymbol = properties.optString("marker-symbol");

        if (!Strings.isNullOrEmpty(markerColor) || !Strings.isNullOrEmpty(markerSize) || !Strings.isNullOrEmpty(markerSymbol)) {
            // Who knows what kind of stuff we are getting in
            Icon.Size size;

            try {
                size = Icon.Size.valueOf(markerSize.toUpperCase(Locale.US));
            } catch (IllegalArgumentException iae) {
                // Fine, we will just assume you meant large..
                size = Icon.Size.LARGE;
            }

            markerIcon = new Icon(mv.getContext(), size, markerSymbol, markerColor);
        }

        if (type.equalsIgnoreCase("Point")) {
            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
            double lon = (Double) coordinates.get(0);
            double lat = (Double) coordinates.get(1);
            Marker marker = new Marker(mv, title, "", new LatLng(lat, lon));
            if (markerIcon != null) {
                marker.setIcon(markerIcon);
            }

            uiObjects.add(marker);
//          mv.addMarker(marker);
        } else if (type.equalsIgnoreCase("MultiPoint")) {
            JSONArray points = (JSONArray) geometry.get("coordinates");
            for (j = 0; j < points.length(); j++) {
                JSONArray coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                Marker marker = new Marker(mv, title, "", new LatLng(lat, lon));
                if (markerIcon != null) {
                    marker.setIcon(markerIcon);
                }
                uiObjects.add(marker);
            }
        } else if (type.equalsIgnoreCase("LineString")) {
            PathOverlay path = new PathOverlay();
            JSONArray points = (JSONArray) geometry.get("coordinates");
            JSONArray coordinates;
            for (j = 0; j < points.length(); j++) {
                coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                path.addPoint(new LatLng(lat, lon));
            }
            uiObjects.add(path);
        } else if (type.equalsIgnoreCase("MultiLineString")) {
            JSONArray lines = (JSONArray) geometry.get("coordinates");
            for (int k = 0; k < lines.length(); k++) {
                PathOverlay path = new PathOverlay();
                JSONArray points = (JSONArray) lines.get(k);
                JSONArray coordinates;
                for (j = 0; j < points.length(); j++) {
                    coordinates = (JSONArray) points.get(j);
                    double lon = (Double) coordinates.get(0);
                    double lat = (Double) coordinates.get(1);
                    path.addPoint(new LatLng(lat, lon));
                }
                uiObjects.add(path);
            }
        } else if (type.equalsIgnoreCase("Polygon")) {
            PathOverlay path = new PathOverlay();
            path.getPaint().setStyle(Paint.Style.FILL);
            JSONArray points = (JSONArray) geometry.get("coordinates");

            for (int r = 0; r < points.length(); r++) {
                JSONArray ring = (JSONArray) points.get(r);
                JSONArray coordinates;

                // we re-wind inner rings of GeoJSON polygons in order
                // to render them as transparent in the canvas layer.

                // first ring should have windingOrder = true,
                // all others should have winding order == false
                if ((r == 0 && !windingOrder(ring)) || (r != 0 && windingOrder(ring))) {
                    for (j = 0; j < ring.length(); j++) {
                        coordinates = (JSONArray) ring.get(j);
                        double lon = (Double) coordinates.get(0);
                        double lat = (Double) coordinates.get(1);
                        path.addPoint(new LatLng(lat, lon));
                    }
                } else {
                    for (j = ring.length() - 1; j >= 0; j--) {
                        coordinates = (JSONArray) ring.get(j);
                        double lon = (Double) coordinates.get(0);
                        double lat = (Double) coordinates.get(1);
                        path.addPoint(new LatLng(lat, lon));
                    }
                }
                uiObjects.add(path);
            }
        }
        Log.i(GeoJSON.class.getCanonicalName(), "Returning " + uiObjects.size() + " UI objects");
        return uiObjects;
    }

    private static boolean windingOrder(JSONArray ring) throws JSONException {
        float area = 0;

        if (ring.length() > 2) {
            for (int i = 0; i < ring.length() - 1; i++) {
                JSONArray p1 = (JSONArray) ring.get(i);
                JSONArray p2 = (JSONArray) ring.get(i + 1);
                area += rad((Double) p2.get(0) - (Double) p1.get(0)) * (2 + Math.sin(
                        rad((Double) p1.get(1))) + Math.sin(rad((Double) p2.get(1))));
            }
        }

        return area > 0;
    }

    private static double rad(double _) {
        return _ * Math.PI / 180f;
    }
}
