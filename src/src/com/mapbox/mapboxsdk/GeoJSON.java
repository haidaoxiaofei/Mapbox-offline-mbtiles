package com.mapbox.mapboxsdk;

import android.graphics.Paint;
import com.mapbox.mapboxsdk.util.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mapbox.mapboxsdk.views.overlay.*;

/**
 * A GeoJSON parser.
 */
public class GeoJSON {

    /**
     * Parse a string of GeoJSON data, returning an array of Overlay objects.
     *
     * @param jsonString
     * @return
     * @throws JSONException
     */
    static void parseString(String jsonString, MapView mv) throws JSONException {
        parse(new JSONObject(jsonString), mv);
    }

    /**
     * Parse a GeoJSON object into an array of overlays.
     *
     * @param json
     * @param mv a mapview for the overlays to be added to
     * @throws JSONException
     */
    static void parse(JSONObject json, MapView mv) throws JSONException {
        String type = json.optString("type");
        if (type.equals("FeatureCollection")) {
            featureCollectionToLayers(json, mv);
        } else if (type.equals("Feature")) {
            featureToLayer(json, mv);
        }
    }

    static void featureCollectionToLayers(JSONObject featureCollection, MapView mv) throws JSONException {
        JSONArray features = (JSONArray) featureCollection.get("features");
        for (int i = 0; i < features.length(); i++) {
            featureToLayer((JSONObject) features.get(i), mv);
        }
    }

    /**
     * Parse a GeoJSON feature object into some number of overlays, adding them to the overlays
     * array.
     *
     * @param feature
     * @param overlays
     * @throws JSONException
     */
    static void featureToLayer(JSONObject feature, MapView mv) throws JSONException {

        JSONObject properties = (JSONObject) feature.get("properties");
        String title = "";
        title = properties.optString("title");

        JSONObject geometry = (JSONObject) feature.get("geometry");
        String type = geometry.optString("type");

        int j;

        if (type.equals("Point")) {
            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
            double lon = (Double) coordinates.get(0);
            double lat = (Double) coordinates.get(1);
            mv.createMarker(lat, lon, title, "");
        } else if (type.equals("MultiPoint")) {
            JSONArray points = (JSONArray) geometry.get("coordinates");
            for (j = 0; j < points.length(); j++) {
                JSONArray coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                mv.createMarker(lat, lon, title, "");
            }
        } else if (type.equals("LineString")) {
            PathOverlay path = new PathOverlay();
            JSONArray points = (JSONArray) geometry.get("coordinates");
            JSONArray coordinates;
            for (j = 0; j < points.length(); j++) {
                coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                path.addPoint(new LatLng(lat, lon));
            }
            mv.getOverlays().add(path);
        } else if (type.equals("MultiLineString")) {
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
                mv.getOverlays().add(path);
            }
        } else if (type.equals("Polygon")) {
            PathOverlay path = new PathOverlay();
            path.getPaint().setStyle(Paint.Style.FILL);
            JSONArray points = (JSONArray) geometry.get("coordinates");
            JSONArray outerRing = (JSONArray) points.get(0);
            JSONArray coordinates;
            for (j = 0; j < outerRing.length(); j++) {
                coordinates = (JSONArray) outerRing.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                path.addPoint(new LatLng(lat, lon));
            }
            mv.getOverlays().add(path);
        }
    }
}