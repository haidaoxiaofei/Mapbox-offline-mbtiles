package com.mapbox.mapboxsdk.util;

import android.util.Log;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class DataLoadingUtils {

    /**
     * Load GeoJSON from URL (in synchronous manner) and return GeoJSON FeatureCollection
     * @param url URL of GeoJSON data
     * @return Remote GeoJSON parsed into Library objects
     * @throws IOException
     * @throws JSONException
     */
    public static FeatureCollection loadGeoJSONFromUrl(final String url) throws IOException, JSONException {
        if (UtilConstants.DEBUGMODE) {
            Log.d(DataLoadingUtils.class.getCanonicalName(), "Mapbox SDK downloading GeoJSON URL: " + url);
        }
        InputStream is = new URL(url).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String jsonText = readAll(rd);

        FeatureCollection parsed = (FeatureCollection) GeoJSON.parse(jsonText);
        if (UtilConstants.DEBUGMODE) {
            Log.d(DataLoadingUtils.class.getCanonicalName(), "Parsed GeoJSON with " + parsed.getFeatures().size() + " features.");
        }

        return parsed;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
