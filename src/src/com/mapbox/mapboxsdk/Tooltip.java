package com.mapbox.mapboxsdk;

import android.content.Context;
import android.graphics.*;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

public class Tooltip extends Overlay{
    private OverlayItem item;
    private Paint paint = new Paint();
    private String text;

    private Point point = new Point();
    private MapView mapView;
    private Canvas canvas;

    public Tooltip(Context ctx) {
        super(ctx);
    }
    public Tooltip(Context ctx, OverlayItem ot){
        super(ctx);
        this.item = ot;
    }

    private Tooltip(ResourceProxy pResourceProxy) {
        super(pResourceProxy);
    }

    public void setText(String text){
        this.text = text;

    }

    @Override
    protected void draw(Canvas canvas, org.osmdroid.views.MapView mapView, boolean shadow) {
        this.mapView = (MapView)mapView;
        this.calculatePoint();
        this.canvas = canvas;
        paint.setColor(Color.WHITE);
        this.setTooltipShape();
        this.setText("whatevs");
        paint.setColor(Color.rgb(50, 50, 50));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40f);
        System.out.println("Point: " + point.x);
        canvas.drawText(text, point.x, point.y-140, paint);
    }
    private void setTooltipShape(){
        canvas.drawRect(point.x - 240, point.y - 200, point.x + 240, point.y - 100, paint);
        canvas.save();
        canvas.rotate((float) 45, point.x, point.y - 100);
        canvas.drawRect(point.x - 20, point.y - 120, point.x + 20, point.y - 80, paint);
        canvas.restore();
    }
    private void calculatePoint(){
        GeoPoint markerCoords = item.getPoint();
        MapView.Projection projection = mapView.getProjection();
        Point pointy = new Point();
        projection.toPixels(markerCoords, pointy);
        this.point = pointy;
    }
}
