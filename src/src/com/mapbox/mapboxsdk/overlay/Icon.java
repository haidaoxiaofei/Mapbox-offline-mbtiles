package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tmcw on 1/30/14.
 */
public class Icon {

    private Marker marker;
    private BitmapDrawable drawable;

    static String BASE_URL = "http://api.tiles.mapbox.com/v3/";

    public enum Size {
        l, m, s
    }

    public Icon(Size size, String symbol, String color) {
        String url = BASE_URL + "marker/pin-" +
            size.toString() + "-" + symbol + "+" + color + ".png";
        new BitmapLoader().execute(url);
    }

    public Icon setMarker(Marker marker) {
        this.marker = marker;
        if (drawable != null) {
            this.marker.setMarker(drawable);
        }
        return this;
    }

    // marker/pin-m-bus+48a.png"

    class BitmapLoader extends AsyncTask<String, Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                URL url = new URL(src[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bitmap.setDensity(120);
            drawable = new BitmapDrawable(bitmap);
            Log.w(TAG, "icon loaded");
            if (marker != null) {
                marker.setMarker(drawable);
            }
        }
    }

    private static final String TAG = "Icon";
}