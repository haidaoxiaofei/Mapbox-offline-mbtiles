package com.mapbox.mapboxsdk;

import android.content.Context;
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
    private Context context;


    public Marker(String aTitle, String aDescription, GeoPoint aGeoPoint) {
        super(aTitle, aDescription, aGeoPoint);
        fromMaki("markerstroked");
    }
    public Marker(MapView mv, String aTitle, String aDescription, GeoPoint aGeoPoint) {
        super(aTitle, aDescription, aGeoPoint);
        context = mv.getContext();
        fromMaki("markerstroked");
    }

    public void fromMaki(String makiString){
        String urlString = makiString+"182x";
        System.out.println(urlString);
        int id = context.getResources().getIdentifier(urlString, "drawable", context.getPackageName());
        System.out.println(""+id);
        this.setMarker(context.getResources().getDrawable(id));
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
