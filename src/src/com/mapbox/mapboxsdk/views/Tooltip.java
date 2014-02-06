package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mapbox.mapboxsdk.geometry.LatLng;


public class Tooltip extends LinearLayout {

    private final MapView parentMapView;
    private String description;
    private String title;
    private Point scrollPoint = new Point();

    public Tooltip(Context context, MapView mapView) {
        super(context);
        parentMapView = mapView;
        TextView tv = new TextView(context);
        tv.setText("hello");
        this.addView(tv);
        mapView.addView(this);
        this.getLayoutParams().width = 300;
        this.getLayoutParams().height = 300;
        this.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(scrollPoint!=null){
            this.setX(scrollPoint.x);
            this.setY(scrollPoint.y);
        }
    }

    public void setText(String title, String description){
        this.title = title;
        this.description = description;
    }

    private void setPosition(LatLng point){
        MapView.Projection projection = parentMapView.getProjection();
        projection.toPixels(point, scrollPoint);
    }
}
