package com.mapbox.mapboxsdk.overlay;

import com.mapbox.mapboxsdk.DefaultResourceProxyImpl;
import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import com.mapbox.mapboxsdk.views.util.Projection;

public class CoreLocationOverlay extends Overlay {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final int SIMPLE_OVERLAY_STYLE = 1;
    public static final int DIRECTION_OVERLAY_STYLE = 2;

    // ===========================================================
    // Fields
    // ===========================================================

    private int overlayStyle = DIRECTION_OVERLAY_STYLE;

    protected final Paint mPaint = new Paint();
    protected final Paint mAccuracyPaint = new Paint();

    protected Bitmap DIRECTION_ARROW;

    protected LatLng mLocation;
    protected float mBearing;

    private final Matrix directionRotater = new Matrix();
    private final Point screenCoords = new Point();

    private float DIRECTION_ARROW_CENTER_X;
    private float DIRECTION_ARROW_CENTER_Y;
    private int DIRECTION_ARROW_WIDTH;
    private int DIRECTION_ARROW_HEIGHT;

    private int mAccuracy = 0;
    private boolean mShowAccuracy = true;


    protected Bitmap PERSON_ICON;
    /**
     * Coordinates the feet of the person are located.
     */
    protected final android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24, 39);


    // ===========================================================
    // Constructors
    // ===========================================================

    public CoreLocationOverlay(final Context ctx, int overlayStyle) {
        this(ctx, new DefaultResourceProxyImpl(ctx), overlayStyle);
    }

    public CoreLocationOverlay(final Context ctx, final ResourceProxy pResourceProxy, int overlayStyle) {
        super(pResourceProxy);

        this.overlayStyle = overlayStyle;

        switch (this.overlayStyle)
        {
            case SIMPLE_OVERLAY_STYLE:
                this.PERSON_ICON = mResourceProxy.getBitmap(ResourceProxy.bitmap.person);
                break;
            default:
                // Default Style is DIRECTION_OVERLAY_STYLE
                this.DIRECTION_ARROW = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);

                this.DIRECTION_ARROW_CENTER_X = this.DIRECTION_ARROW.getWidth() / 2 - 0.5f;
                this.DIRECTION_ARROW_CENTER_Y = this.DIRECTION_ARROW.getHeight() / 2 - 0.5f;
                this.DIRECTION_ARROW_HEIGHT = this.DIRECTION_ARROW.getHeight();
                this.DIRECTION_ARROW_WIDTH = this.DIRECTION_ARROW.getWidth();
                this.mAccuracyPaint.setStrokeWidth(2);
                this.mAccuracyPaint.setColor(Color.BLUE);
                this.mAccuracyPaint.setAntiAlias(true);
                break;
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setShowAccuracy(final boolean pShowIt) {
        this.mShowAccuracy = pShowIt;
    }

    public void setLocation(final LatLng mp) {
        this.mLocation = mp;
    }

    public LatLng getLocation() {
        return this.mLocation;
    }

    /**
     * @param pAccuracy in Meters
     */
    public void setAccuracy(final int pAccuracy) {
        this.mAccuracy = pAccuracy;
    }

    public void setBearing(final float aHeading) {
        this.mBearing = aHeading;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(final Canvas c, final MapView osmv, final boolean shadow) {

        switch (this.overlayStyle)
        {
            case SIMPLE_OVERLAY_STYLE:
                if (!shadow && this.mLocation != null) {
                    final Projection pj = osmv.getProjection();
                    pj.toMapPixels(this.mLocation, screenCoords);
                    c.drawBitmap(PERSON_ICON, screenCoords.x - PERSON_HOTSPOT.x, screenCoords.y - PERSON_HOTSPOT.y, this.mPaint);
                }
                break;
            default:
                if (shadow)
                {
                    return;
                }

                if (this.mLocation != null)
                {
                    final Projection pj = osmv.getProjection();
                    pj.toMapPixels(this.mLocation, screenCoords);

                    if (this.mShowAccuracy && this.mAccuracy > 10)
                    {
                        final float accuracyRadius = pj.metersToEquatorPixels(this.mAccuracy);
                        /* Only draw if the DirectionArrow doesn't cover it. */
                        if (accuracyRadius > 8)
                        {
                            /* Draw the inner shadow. */
                            this.mAccuracyPaint.setAntiAlias(false);
                            this.mAccuracyPaint.setAlpha(30);
                            this.mAccuracyPaint.setStyle(Style.FILL);
                            c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius,
                                    this.mAccuracyPaint);

					        /* Draw the edge. */
                            this.mAccuracyPaint.setAntiAlias(true);
                            this.mAccuracyPaint.setAlpha(150);
                            this.mAccuracyPaint.setStyle(Style.STROKE);
                            c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius,
                                    this.mAccuracyPaint);
                        }
                    }

			        /*
			         * Rotate the direction-Arrow according to the bearing we are driving. And draw it to the canvas.
			        */
                    this.directionRotater.setRotate(this.mBearing, DIRECTION_ARROW_CENTER_X, DIRECTION_ARROW_CENTER_Y);
                    final Bitmap rotatedDirection = Bitmap.createBitmap(DIRECTION_ARROW, 0, 0,
                            DIRECTION_ARROW_WIDTH, DIRECTION_ARROW_HEIGHT, this.directionRotater, false);
                    c.drawBitmap(rotatedDirection, screenCoords.x - rotatedDirection.getWidth() / 2,
                            screenCoords.y - rotatedDirection.getHeight() / 2, this.mPaint);
                }
                break;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
