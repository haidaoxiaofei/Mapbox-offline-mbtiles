package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An Icon provided by the Mapbox marker API, optionally
 * with a symbol from Maki
 */
public class Icon {

    private Marker marker;
    private BitmapDrawable drawable;

    static String BASE_URL = "http://api.tiles.mapbox.com/v3/";

    public enum Size {
        l, m, s
    }

    /**
     * Initialize an icon with size, symbol, and color, and start a
     * download process to load it from the API.
     * @param size
     * @param symbol
     * @param color
     */
    public Icon(Size size, String symbol, String color) {
        String url = BASE_URL + "marker/pin-" + size.toString();
        System.out.println("symbol "+symbol);
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

    class BitmapLoader extends AsyncTask<String, Void,Bitmap> {

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