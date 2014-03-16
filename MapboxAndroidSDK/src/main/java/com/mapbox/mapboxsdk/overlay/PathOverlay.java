package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.DefaultResourceProxyImpl;
import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Viesturs Zarins
 * @author Martin Pearman
 *         <p/>
 *         This class draws a path line in given color.
 */
public class PathOverlay extends Overlay {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    /**
     * Stores points, converted to the map projection.
     */
    private ArrayList<PointF> mPoints;

    /**
     * Number of points that have precomputed values.
     */
    private int mPointsPrecomputed;

    /**
     * Paint settings.
     */
    protected Paint mPaint = new Paint();

    private final Path mPath = new Path();

    private final PointF mTempPoint1 = new PointF();
    private final PointF mTempPoint2 = new PointF();

    // bounding rectangle for the current line segment.
    private final Rect mLineBounds = new Rect();

    // ===========================================================
    // Constructors
    // ===========================================================
    public PathOverlay() {
        super();
        this.mPaint.setColor(Color.BLUE);
        this.mPaint.setStrokeWidth(10.0f);
        this.mPaint.setStyle(Paint.Style.STROKE);

        this.clearPath();
    }

    public PathOverlay(final int color, final Context ctx) {
        this(color, 2.0f, new DefaultResourceProxyImpl(ctx));
    }

    public PathOverlay(final int color, final ResourceProxy resourceProxy) {
        this(color, 2.0f, resourceProxy);
    }

    public PathOverlay(final int color, final float width, final ResourceProxy resourceProxy) {
        super(resourceProxy);
        this.mPaint.setColor(color);
        this.mPaint.setStrokeWidth(width);
        this.mPaint.setStyle(Paint.Style.STROKE);

        this.clearPath();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setColor(final int color) {
        this.mPaint.setColor(color);
    }

    public void setAlpha(final int a) {
        this.mPaint.setAlpha(a);
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(final Paint pPaint) {
        if (pPaint == null) {
            throw new IllegalArgumentException("pPaint argument cannot be null");
        }
        mPaint = pPaint;
    }

    public void clearPath() {
        this.mPoints = new ArrayList<PointF>();
        this.mPointsPrecomputed = 0;
    }

    public void addPoint(final LatLng aPoint) {
        addPoint(aPoint.getLatitude(), aPoint.getLongitude());
    }

    public void addPoint(final double aLatitude, final double aLongitude) {
        mPoints.add(new PointF((float) aLatitude, (float) aLongitude));
    }

    public void addPoints(final LatLng... aPoints) {
        for (final LatLng point : aPoints) {
            addPoint(point);
        }
    }

    public void addPoints(final List<LatLng> aPoints) {
        for (final LatLng point : aPoints) {
            addPoint(point);
        }
    }

    public int getNumberOfPoints() {
        return this.mPoints.size();
    }

    /**
     * This method draws the line. Note - highly optimized to handle long paths, proceed with care.
     * Should be fine up to 10K points.
     */
    @Override
    protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

        if (shadow) {
            return;
        }

        final int size = this.mPoints.size();
        if (size < 2) {
            // nothing to paint
            return;
        }

        final Projection pj = mapView.getProjection();

        // precompute new points to the intermediate projection.
        while (this.mPointsPrecomputed < size) {
            final PointF pt = this.mPoints.get(this.mPointsPrecomputed);
            pj.toMapPixelsProjected((double)pt.x, (double)pt.y, pt);

            this.mPointsPrecomputed++;
        }

        PointF screenPoint0 = null; // points on screen
        PointF screenPoint1;
        PointF projectedPoint0; // points from the points list
        PointF projectedPoint1;

        // clipping rectangle in the intermediate projection, to avoid performing projection.
        final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());

        mPath.rewind();
        projectedPoint0 = this.mPoints.get(size - 1);
        mLineBounds.set((int)projectedPoint0.x, (int)projectedPoint0.y, (int)projectedPoint0.x, (int)projectedPoint0.y);

        for (int i = size - 2; i >= 0; i--) {
            // compute next points
            projectedPoint1 = this.mPoints.get(i);
            mLineBounds.union((int)projectedPoint1.x, (int)projectedPoint1.y);

            if (!Rect.intersects(clipBounds, mLineBounds)) {
                // skip this line, move to next point
                projectedPoint0 = projectedPoint1;
                screenPoint0 = null;
                continue;
            }

            // the starting point may be not calculated, because previous segment was out of clip
            // bounds
            if (screenPoint0 == null) {
                screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
                mPath.moveTo(screenPoint0.x, screenPoint0.y);
            }

            screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

            // skip this point, too close to previous point
            if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
                continue;
            }

            mPath.lineTo(screenPoint1.x, screenPoint1.y);

            // update starting point to next position
            projectedPoint0 = projectedPoint1;
            screenPoint0.x = screenPoint1.x;
            screenPoint0.y = screenPoint1.y;
            mLineBounds.set((int)projectedPoint0.x, (int)projectedPoint0.y, (int)projectedPoint0.x, (int)projectedPoint0.y);
        }

        canvas.drawPath(mPath, this.mPaint);
    }
}