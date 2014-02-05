package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;


public class Tooltip extends View {

    private final MapView parentMapView;
    private String description;
    private String title;

    public Tooltip(Context context, MapView mapView) {
        super(context);
        parentMapView = mapView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setText(String title, String description){
        this.title = title;
        this.description = description;
    }
}
