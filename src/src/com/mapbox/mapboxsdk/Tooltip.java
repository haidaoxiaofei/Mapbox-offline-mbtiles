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

    @Override
    protected void draw(Canvas canvas, org.osmdroid.views.MapView mapView, boolean shadow) {
        Rect bounds = new Rect();
        String text = "Helloooo this is a tooltip!";
        paint.getTextBounds(text, 0, text.length()-1, bounds);
        int innerBoxHeight = bounds.height();
        GeoPoint markerCoords = item.getPoint();
        MapView.Projection projection = mapView.getProjection();
        Point point = new Point();
        projection.toPixels(markerCoords, point);
        paint.setColor(Color.WHITE);
        canvas.drawRect(point.x - 240, point.y - 200, point.x + 240, point.y - 100, paint);
        canvas.save();
        canvas.rotate((float) 45, point.x, point.y - 100);
        canvas.drawRect(point.x - 20, point.y - 120, point.x + 20, point.y - 80, paint);
        canvas.restore();
        paint.setColor(Color.rgb(50, 50, 50));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40f);
        canvas.drawText(text, point.x, point.y-140, paint);
    }
}
