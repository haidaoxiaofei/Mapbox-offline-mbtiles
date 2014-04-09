package com.mapbox.mapboxsdk.overlay;

import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.format.GeoJSON;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class GeoJSONLayer {

    private MapView mapView;

    public GeoJSONLayer(final MapView mapView) {
        this.mapView = mapView;
    }

    public void loadURL(final String url) {
        new Getter().execute(url);
    }

    /**
     * Class that generates markers from formats such as GeoJSON
     */
    public class Getter extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            InputStream is;
            String jsonText = null;
            try {
                is = new URL(params[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                        Charset.forName("UTF-8")));

                jsonText = readAll(rd);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonText;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            try {
                GeoJSON.parseString(jsonString, mapView);
            } catch (JSONException e) {
                Log.w(TAG, "JSON parsed was invalid. Continuing without it");
            } catch (Exception e) {
                Log.e(TAG, "Other exception returned from async geojson load " + e.getMessage());
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
    }

    static final String TAG = "GeoJSONLayer";
}
