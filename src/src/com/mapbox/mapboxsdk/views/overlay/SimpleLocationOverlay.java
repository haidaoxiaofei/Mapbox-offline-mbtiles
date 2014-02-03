// Created by plusminus on 22:01:11 - 29.09.2008
package com.mapbox.mapboxsdk.views.overlay;

import com.mapbox.mapboxsdk.DefaultResourceProxyImpl;
import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.util.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * @author Nicolas Gramlich
 */
public class SimpleLocationOverlay extends Overlay {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected final Paint mPaint = new Paint();

    protected final Bitmap PERSON_ICON;
    /**
     * Coordinates the feet of the person are located.
     */
    protected final android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24, 39);

    protected LatLng mLocation;
    private final Point screenCoords = new Point();

    // ===========================================================
    // Constructors
    // ===========================================================

    public SimpleLocationOverlay(final Context ctx) {
        this(ctx, new DefaultResourceProxyImpl(ctx));
    }

    public SimpleLocationOverlay(final Context ctx,
                                 final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        this.PERSON_ICON = mResourceProxy.getBitmap(ResourceProxy.bitmap.person);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setLocation(final LatLng mp) {
        this.mLocation = mp;
    }

    public LatLng getMyLocation() {
        return this.mLocation;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
        if (!shadow && this.mLocation != null) {
            final Projection pj = osmv.getProjection();
            pj.toMapPixels(this.mLocation, screenCoords);

            c.drawBitmap(PERSON_ICON, screenCoords.x - PERSON_HOTSPOT.x, screenCoords.y
                    - PERSON_HOTSPOT.y, this.mPaint);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
