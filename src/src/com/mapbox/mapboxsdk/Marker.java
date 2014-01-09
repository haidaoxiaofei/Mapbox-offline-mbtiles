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
    private Tooltip tooltip;
    private MapView mapView;

    public Marker(String aTitle, String aDescription, GeoPoint aGeoPoint) {
        this(null, aTitle, aDescription, aGeoPoint);
    }
    public Marker(MapView mv, String aTitle, String aDescription, GeoPoint aGeoPoint) {
        super(aTitle, aDescription, aGeoPoint);
        context = mv.getContext();
        mapView = mv;
        fromMaki("markerstroked");
        attachTooltip();
    }

    private void attachTooltip() {
        tooltip = new Tooltip(context, this, this.getTitle());
        mapView.getOverlays().add(tooltip);
        mapView.invalidate();
    }

    public void fromMaki(String makiString){
        String urlString = makiString+"182x";
        int id = context.getResources().getIdentifier(urlString, "drawable", context.getPackageName());
        this.setMarker(context.getResources().getDrawable(id));
    }
    public void setTooltipVisible(){
        tooltip.setVisible(true);
        mapView.invalidate();
    }
    public void setTooltipInvisible(){
        tooltip.setVisible(false);
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
