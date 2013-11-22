package com.mapbox.mapboxsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Marker extends OverlayItem{
    public Marker(String aTitle, String aSnippet, GeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);
        //Drawable markerDrawable = ;
        //this.setMarker(markerDrawable);
    }

    public Marker(String aUid, String aTitle, String aDescription, GeoPoint aGeoPoint) {
        super(aUid, aTitle, aDescription, aGeoPoint);
    }

    public void fromMaki(String makiString){
        String urlString = "https://raw.github.com/mapbox/maki/gh-pages/renders/"+makiString+"-18@2x.png";
        this.setMarker(new BitmapDrawable());
        new BitmapLoader().execute(urlString);

    }

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
        protected void onPostExecute(Bitmap bitmap){
            bitmap.setDensity(120);
            Marker.this.setMarker(new BitmapDrawable(bitmap));
        }
    }
}
