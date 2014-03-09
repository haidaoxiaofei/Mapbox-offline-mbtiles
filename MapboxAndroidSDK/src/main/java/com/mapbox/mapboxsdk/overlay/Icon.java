package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An Icon provided by the Mapbox marker API, optionally
 * with a symbol from Maki
 */
public class Icon implements MapboxConstants
{
    private Marker marker;
    private BitmapDrawable drawable;

    public enum Size {
        LARGE("l"), MEDIUM("m"), SMALL("s");

        private String apiString;

        Size(String api)
        {
            this.apiString = api;
        }

        public String getApiString()
        {
            return apiString;
        }
    }

    /**
     * Initialize an icon with size, symbol, and color, and start a
     * download process to load it from the API.
     * @param size Size of Icon
     * @param symbol Maki Symbol
     * @param color Color of Icon
     */
    public Icon(Size size, String symbol, String color) {
        String url = MAPBOX_BASE_URL + "marker/pin-" + size.getApiString();
        if(!symbol.equals("")){
            url+= "-" + symbol + "+" + color + ".png";
        }
        else{
            url+= "+" + color + ".png";
        }
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
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                return BitmapFactory.decodeStream(connection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            drawable = new BitmapDrawable(bitmap);
            Log.w(TAG, "icon loaded");
            if (marker != null) {
                marker.setMarker(drawable);
            }
        }
    }

    private static final String TAG = "Icon";
}