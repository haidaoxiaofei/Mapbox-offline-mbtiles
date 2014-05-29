package com.mapbox.mapboxsdk.overlay;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.google.common.base.Strings;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.MapView;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class GeoJSONLayer {

    private final MapView mapView;
    private final Icon markerIcon;

    public GeoJSONLayer(final MapView mapView, final Icon markerIcon) {
        super();
        this.mapView = mapView;
        this.markerIcon = markerIcon;
    }

    public void loadURL(final String url) {
        if (Strings.isNullOrEmpty(url)) {
            return;
        }
        new Getter().execute(url);
    }

    /**
     * Class that generates markers from formats such as GeoJSON
     */
    private class Getter extends AsyncTask<String, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            InputStream is;
            String jsonText;
            ArrayList<Object> uiObjects = new ArrayList<Object>();

            try {
                if (UtilConstants.DEBUGMODE) {
                    Log.d(TAG, "Mapbox SDK downloading GeoJSON URL: " + params[0]);
                }
                is = new URL(params[0]).openStream();
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                jsonText = readAll(rd);

                FeatureCollection parsed = (FeatureCollection) GeoJSON.parse(jsonText);
                if (UtilConstants.DEBUGMODE) {
                    Log.d(TAG, "Parsed GeoJSON with " + parsed.getFeatures().size() + " features.");
                }

                for (Feature f : parsed.getFeatures()) {
                    // Parse Into UI Objections
                    String type = f.getType();
                    int j;

                    if (type.equals("Point")) {
                        JSONArray coordinates = (JSONArray) f.toJSON().get("coordinates");
                        double lon = (Double) coordinates.get(0);
                        double lat = (Double) coordinates.get(1);
                        Marker marker = new Marker(mapView, "", "", new LatLng(lat, lon));
                        if (markerIcon != null) {
                            marker.setIcon(markerIcon);
                        }
                        uiObjects.add(marker);
                    } else if (type.equals("MultiPoint")) {
                        JSONArray points = (JSONArray) f.toJSON().get("coordinates");
                        for (j = 0; j < points.length(); j++) {
                            JSONArray coordinates = (JSONArray) points.get(j);
                            double lon = (Double) coordinates.get(0);
                            double lat = (Double) coordinates.get(1);
                            Marker marker = new Marker(mapView, "", "", new LatLng(lat, lon));
                            if (markerIcon != null) {
                                marker.setIcon(markerIcon);
                            }
                            uiObjects.add(marker);
                        }
                    } else if (type.equals("LineString")) {
                        PathOverlay path = new PathOverlay();
                        JSONArray points = (JSONArray) f.toJSON().get("coordinates");
                        JSONArray coordinates;
                        for (j = 0; j < points.length(); j++) {
                            coordinates = (JSONArray) points.get(j);
                            double lon = (Double) coordinates.get(0);
                            double lat = (Double) coordinates.get(1);
                            path.addPoint(new LatLng(lat, lon));
                        }
                        uiObjects.add(path);
                    } else if (type.equals("MultiLineString")) {
                        JSONArray lines = (JSONArray) f.toJSON().get("coordinates");
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
                    } else if (type.equals("Polygon")) {
                        PathOverlay path = new PathOverlay();
                        path.getPaint().setStyle(Paint.Style.FILL);
                        JSONArray points = (JSONArray) f.toJSON().get("coordinates");

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
                    } else if (type.equals("MultiPolygon")) {
                        PathOverlay path = new PathOverlay();
                        path.getPaint().setStyle(Paint.Style.FILL);
                        JSONArray polygons = (JSONArray) f.toJSON().get("coordinates");

                        for (int p = 0; p < polygons.length(); p++) {
                            JSONArray points = (JSONArray) polygons.get(p);
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
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading / parsing GeoJSON: " + e.toString());
                e.printStackTrace();
            }
            return uiObjects;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            // Back on the Main Thread so add new UI Objects and refresh map
            for (Object obj : objects) {
                if (obj instanceof Marker) {
                    mapView.addMarker((Marker) obj);
                } else if (obj instanceof PathOverlay) {
                    mapView.getOverlays().add((PathOverlay) obj);
                }
            }
            if (objects.size() > 0) {
                mapView.invalidate();
            }
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

    static final String TAG = "GeoJSONLayer";
}
