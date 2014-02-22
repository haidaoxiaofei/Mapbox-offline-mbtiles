package com.mapbox.mapboxsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.util.Projection;


public class Tooltip extends View {

    private final MapView parentMapView;
    private String description;
    private String title;

    private LatLng currentLocation = new LatLng(0, 0);
    private ViewGroup parent;

    public Tooltip(Context context, MapView mapView) {
        super(context);
        System.out.println("creating tooltip" +
                "");
        parentMapView = mapView;
        TextView tv = new TextView(context);
        tv.setText("hello");
        parent = (ViewGroup)mapView.getParent();
        mapView.addView(this);
        this.getLayoutParams().width = 10;
        this.getLayoutParams().height = 10;
        this.setBackgroundColor(Color.RED);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Point location = getScreenPosition(currentLocation);
        this.setScrollX(location.x);
        this.setScrollY(location.y);
    }

    public void setText(String title, String description){
        this.title = title;
        this.description = description;
    }

    public void setPosition(LatLng position){
        this.currentLocation = position;
        this.invalidate();
    }

    private Point getScreenPosition(LatLng point){
        Point scrollPoint = new Point();
        Projection projection = parentMapView.getProjection();
        projection.toPixels(point, scrollPoint);
        return scrollPoint;
    }

    private Point pointFromGeoPoint(LatLng gp){

        Point rtnPoint = new Point();
        Projection projection = parentMapView.getProjection();
        projection.toPixels(gp, rtnPoint);
        // Get the top left GeoPoint
        LatLng geoPointTopLeft = (LatLng) projection.fromPixels(0, 0);
        Point topLeftPoint = new Point();
        // Get the top left Point (includes osmdroid offsets)
        projection.toPixels(geoPointTopLeft, topLeftPoint);
        rtnPoint.x-= topLeftPoint.x; // remove offsets
        rtnPoint.y-= topLeftPoint.y;
        if (rtnPoint.x > parentMapView.getWidth() || rtnPoint.y > parentMapView.getHeight() ||
                rtnPoint.x < 0 || rtnPoint.y < 0){
            return null; // gp must be off the screen
        }
        return rtnPoint;
    }
}
