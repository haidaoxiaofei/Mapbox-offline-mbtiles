package com.mapbox.mapboxsdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

public class Tooltip extends Overlay{

    private OverlayItem item;
    private Paint paint = new Paint();
    private Point point = new Point();
    private String text;
    private MapView mapView;
    private Canvas canvas;

    public Tooltip(Context ctx) {
        this(ctx, null);
    }
    public Tooltip(Context ctx, OverlayItem ot){
        this(ctx, ot, "");
    }
    public Tooltip(Context ctx, OverlayItem ot, String text) {
        super(ctx);
        setItem(ot);
        setText(text);
    }
    public void setText(String text){
        this.text = text;
    }
    public void setItem(OverlayItem item) {
        this.item = item;
    }

    @Override
    protected void draw(Canvas canvas, org.osmdroid.views.MapView mapView, boolean shadow) {
        this.mapView = (MapView)mapView;
        this.calculatePoint();
        this.canvas = canvas;
        paint.setColor(Color.WHITE);
        this.setTooltipShape();
        paint.setColor(Color.rgb(50, 50, 50));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40f);
        System.out.println("Point: " + point.x);
        canvas.drawText(text, point.x, point.y - 140, paint);
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
        projection.toPixels(markerCoords, point);
    }
}
