package com.mapbox.mapboxsdk.format;

import android.graphics.Paint;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static void parseString(String jsonString, MapView mv) throws JSONException {
        parse(new JSONObject(jsonString), mv);
    }

    /**
     * Parse a GeoJSON object into an array of overlays.
     *
     * @param json GeoJSON
     * @param mv   a mapview for the overlays to be added to
     * @throws JSONException
     */
    public static void parse(JSONObject json, MapView mv) throws JSONException {
        String type = json.optString("type");
        if (type.equals("FeatureCollection")) {
            featureCollectionToLayers(json, mv);
        } else if (type.equals("Feature")) {
            featureToLayer(json, mv);
        }
    }

    public static void featureCollectionToLayers(JSONObject featureCollection, MapView mv) throws JSONException {
        JSONArray features = (JSONArray) featureCollection.get("features");
        for (int i = 0; i < features.length(); i++) {
            featureToLayer((JSONObject) features.get(i), mv);
        }
    }

    /**
     * Parse a GeoJSON feature object into some number of overlays, adding them to the overlays
     * array.
     *
     * @param feature GeoJSON
     * @throws JSONException
     */
    public static void featureToLayer(JSONObject feature, MapView mv) throws JSONException {

        JSONObject properties = (JSONObject) feature.get("properties");
        String title = properties.optString("title");
        JSONObject geometry = (JSONObject) feature.get("geometry");
        String type = geometry.optString("type");


        int j;

        // Extract the marker style properties from the GeoJSON
        // (See: https://www.mapbox.com/developers/simplestyle/)
        Icon markerIcon = null;
        String markerColor = properties.optString("marker-color");
        String markerSize = properties.optString("marker-size");
        String markerSymbol = properties.optString("marker-symbol");

        if (!"".equals(markerColor) || !"".equals(markerSize) || !"".equals(markerSymbol)) {
            // Who knows what kind of stuff we are getting in
            Icon.Size size = Icon.Size.LARGE;
            try {
                size = Icon.Size.valueOf(markerSize.toUpperCase());
            } catch (IllegalArgumentException iae) {
                // Fine, we will just assume you meant large..
            }

            markerIcon = new Icon(mv.getContext(), size, markerSymbol, markerColor);
        }

        if (type.equals("Point")) {
            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
            double lon = (Double) coordinates.get(0);
            double lat = (Double) coordinates.get(1);
            Marker marker = new Marker(mv, title, "", new LatLng(lat, lon));
            if (markerIcon != null)
                marker.setIcon(markerIcon);

            mv.addMarker(marker);
        } else if (type.equals("MultiPoint")) {
            JSONArray points = (JSONArray) geometry.get("coordinates");
            for (j = 0; j < points.length(); j++) {
                JSONArray coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                Marker marker = new Marker(mv, title, "", new LatLng(lat, lon));
                if (markerIcon != null)
                    marker.setIcon(markerIcon);

                mv.addMarker(marker);
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
        mv.invalidate();
    }
}