package com.mapbox.mapboxsdk.overlay;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.util.BitmapUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An Icon provided by the Mapbox marker API, optionally
 * with a symbol from Maki
 */
public class Icon implements MapboxConstants {
    private Marker marker;
    private BitmapDrawable drawable;
    private Resources mResources;

    public enum Size {
        LARGE("l"), MEDIUM("m"), SMALL("s");

        private String apiString;

        Size(String api) {
            this.apiString = api;
        }

        public String getApiString() {
            return apiString;
        }
    }

    /**
     * Initialize an icon with size, symbol, and color, and start a
     * download process to load it from the API.
     *
     * @param resources Android Resources - Used for proper Bitmap Density generation
     * @param size      Size of Icon
     * @param symbol    Maki Symbol
     * @param color     Color of Icon
     */
    public Icon(Resources resources, Size size, String symbol, String color) {
        this.mResources = resources;
        String url = MAPBOX_BASE_URL + "marker/pin-" + size.getApiString();
        if (!symbol.equals("")) {
            url += "-" + symbol + "+" + color + ".png";
        } else {
            url += "+" + color + ".png";
        }
        Log.d(TAG, "Maki url to load = '" + url + "'");
        new BitmapLoader().execute(url);
    }

    public Icon setMarker(Marker marker) {
        this.marker = marker;
        if (drawable != null) {
            this.marker.setMarker(drawable);
        }
        return this;
    }

    class BitmapLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                URL url = new URL(src[0]);
                Log.d(TAG, "Icon url to load = '" + url + "'");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                return BitmapFactory.decodeStream(connection.getInputStream(), null, BitmapUtils.getBitmapOptions(mResources.getDisplayMetrics()));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && marker != null) {
                if (marker != null) {
                    drawable = new BitmapDrawable(mResources, bitmap);
                    marker.setMarker(drawable);
                    Log.w(TAG, "icon loaded");
                }
            }
        }
    }

    private static final String TAG = "Icon";
}