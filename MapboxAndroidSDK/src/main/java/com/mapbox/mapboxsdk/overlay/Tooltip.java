package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Color;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

public class Tooltip extends Overlay {

    private OverlayItem item;
    private Paint paint = new Paint();
    private Point point = new Point();
    private Context context;
    private String title;
    private String description;
    private MapView mapView;
    private Canvas canvas;

    /**
     * Is this tooltip currently visible.
     */
    private boolean visible;

    /**
     * A paint style used for tooltip contents.
     */
    private TextPaint textPaint;

    /**
     * Initialize a tooltip without title or description
     * @param ctx a Context object on which this tooltip is drawn.
     * @param ot an overlay item.
     */
    public Tooltip(Context ctx, OverlayItem ot) {
        this(ctx, ot, "", "");
    }

    /**
     * Initialize a tooltip.
     *
     * @param ctx a Context object on which this tooltip is drawn.
     * @param ot an overlay item.
     * @param title the title in the tooltip.
     * @param description the description text in the tooltip
     */
    public Tooltip(Context ctx, OverlayItem ot, String title, String description) {
        super(ctx);
        this.context = ctx;
        setItem(ot);
        initTextPaint();
        setTitle(title);
        setDescription(description);
        setVisible(true);
    }

    private void initTextPaint() {
        textPaint = new TextPaint();
        textPaint.setColor(context.getResources().getColor(R.color.toolTipText));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (this.isVisible()) {
            StaticLayout sl = new StaticLayout(title, textPaint, 400, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
            sl.draw(canvas);
            if (description != null) {
                StaticLayout sd = new StaticLayout(description, textPaint, 400, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
                sd.draw(canvas);
            }
            this.mapView = mapView;
            this.canvas = canvas;
            paint.setColor(Color.WHITE);
            this.setTooltipShape();
        }
    }

    private void setTooltipShape() {
        canvas.drawRect(getRect(), paint);
        canvas.save();
        canvas.rotate((float) 45, point.x, point.y - 100);
        canvas.drawRect(point.x - 20,
                point.y - 120,
                point.x + 20,
                point.y - 80,
                paint);
        canvas.restore();
    }

    private void calculatePoint() {
        LatLng markerCoords = item.getPoint();
        Projection projection = mapView.getProjection();
        projection.toPixels(markerCoords, point);
    }

    // Getters/setters


    /**
     * Sets description to be displayed in the tooltip
     * @param description the description text
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * Sets title to be displayed in the tooltip
     * @param title the title
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setTitle(String title) {
       this.title = title;
       return this;
    }

    /**
     * Sets associated overlay of the tooltip
     * @param item the overlay (normally a Marker object)
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setItem(OverlayItem item) {
        this.item = item;
        return this;
    }

    /**
     * Is the tooltip visible?
     * @return true if it's visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets visibility of the tooltip
     * @param visible whether it's visible or not
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Get the on-screen drawn area of this tooltip.
     * @return the on-screen dimensions of this tooltip as a Rect
     */
    public Rect getRect() {
        return new Rect(point.x - TOOLTIP_WIDTH / 2,
                point.y - 200,
                point.x + TOOLTIP_WIDTH / 2,
                point.y - 100);
    }

    /**
     * The default tooltip width, in pixels.
     */
    public static final int TOOLTIP_WIDTH = 480;
    /**
     * Default title size, in points.
     */
    public static final float DEFAULT_TEXT_SIZE = 40f;
}
