package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

public class Tooltip extends Overlay {

    private Marker mItem;
    private Paint mPaint = new Paint();
    private PointF mPoint = new PointF();
    private Context mContext;
    private String mTitle;
    private String mDescription;
    private MapView mapView;
    private Canvas mCanvas;

    /**
     * Is this tooltip currently visible.
     */
    private boolean visible;

    /**
     * A mPaint style used for tooltip contents.
     */
    private TextPaint textPaint;

    /**
     * Initialize a tooltip without mTitle or mDescription
     *
     * @param ctx a Context object on which this tooltip is drawn.
     * @param ot  an overlay mItem.
     */
    public Tooltip(Context ctx, Marker ot) {
        this(ctx, ot, "", "");
    }

    /**
     * Initialize a tooltip.
     *
     * @param ctx         a Context object on which this tooltip is drawn.
     * @param ot          an overlay mItem.
     * @param title       the mTitle in the tooltip.
     * @param description the mDescription text in the tooltip
     */
    public Tooltip(Context ctx, Marker ot, String title, String description) {
        super(ctx);
        this.mContext = ctx;
        setItem(ot);
        initTextPaint();
        setTitle(title);
        setDescription(description);
        setVisible(true);
    }

    private void initTextPaint() {
        textPaint = new TextPaint();
        textPaint.setColor(mContext.getResources().getColor(R.color.toolTipText));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (this.isVisible()) {
            StaticLayout sl = new StaticLayout(mTitle, textPaint, 400, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
            sl.draw(canvas);
            if (mDescription != null) {
                StaticLayout sd = new StaticLayout(mDescription, textPaint, 400, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
                sd.draw(canvas);
            }
            this.mapView = mapView;
            this.mCanvas = canvas;
            mPaint.setColor(Color.WHITE);
            this.setTooltipShape();
        }
    }

    private void setTooltipShape() {
        mCanvas.drawRect(getRect(), mPaint);
        mCanvas.save();
        mCanvas.rotate((float) 45, mPoint.x, mPoint.y - 100);
        mCanvas.drawRect(mPoint.x - 20,
                mPoint.y - 120,
                mPoint.x + 20,
                mPoint.y - 80,
                mPaint);
        mCanvas.restore();
    }

    private void calculatePoint() {
        LatLng markerCoords = mItem.getPoint();
        Projection projection = mapView.getProjection();
        projection.toMapPixels(markerCoords, mPoint);
    }

    // Getters/setters


    /**
     * Sets mDescription to be displayed in the tooltip
     *
     * @param description the mDescription text
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setDescription(String description) {
        this.mDescription = description;
        return this;
    }

    /**
     * Sets mTitle to be displayed in the tooltip
     *
     * @param title the mTitle
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    /**
     * Sets associated overlay of the tooltip
     *
     * @param item the overlay (normally a Marker object)
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setItem(Marker item) {
        this.mItem = item;
        return this;
    }

    /**
     * Is the tooltip visible?
     *
     * @return true if it's visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets visibility of the tooltip
     *
     * @param visible whether it's visible or not
     * @return Tooltip the tooltip, for chaining.
     */
    public Tooltip setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Get the on-screen drawn area of this tooltip.
     *
     * @return the on-screen dimensions of this tooltip as a Rect
     */
    public Rect getRect() {
        return new Rect((int) mPoint.x - TOOLTIP_WIDTH / 2,
                (int) mPoint.y - 200,
                (int) mPoint.x + TOOLTIP_WIDTH / 2,
                (int) mPoint.y - 100);
    }

    /**
     * The default tooltip width, in pixels.
     */
    public static final int TOOLTIP_WIDTH = 480;
    /**
     * Default mTitle size, in points.
     */
    public static final float DEFAULT_TEXT_SIZE = 40f;
}
