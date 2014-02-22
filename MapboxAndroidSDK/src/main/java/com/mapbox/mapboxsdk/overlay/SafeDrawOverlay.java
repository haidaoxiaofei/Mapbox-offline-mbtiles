package com.mapbox.mapboxsdk.overlay;

import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafeTranslatedCanvas;
import com.mapbox.mapboxsdk.views.MapView;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;

/**
 * An overlay class that uses the safe drawing canvas to draw itself and can be zoomed in to high
 * levels without drawing issues.
 *
 * @see {@link ISafeCanvas}
 */
public abstract class SafeDrawOverlay extends Overlay {

    private static final SafeTranslatedCanvas sSafeCanvas = new SafeTranslatedCanvas();
    private static final Matrix sMatrix = new Matrix();
    private boolean mUseSafeCanvas = true;

    protected abstract void drawSafe(final ISafeCanvas c, final MapView mapView, final boolean shadow);

    public SafeDrawOverlay(ResourceProxy pResourceProxy) {
        super(pResourceProxy);
    }

    @Override
    protected void draw(final Canvas c, final MapView mapView, final boolean shadow) {

        sSafeCanvas.setCanvas(c);

        if (this.isUsingSafeCanvas()) {

            // Find the screen offset
            Rect screenRect = mapView.getProjection().getScreenRect();
            sSafeCanvas.xOffset = -screenRect.left;
            sSafeCanvas.yOffset = -screenRect.top;

            // Save the canvas state
            c.save();

            if (mapView.getMapOrientation() != 0) {
                // Un-rotate the maps so we can rotate them accurately using the safe canvas
                c.rotate(-mapView.getMapOrientation(), screenRect.exactCenterX(),
                        screenRect.exactCenterY());
            }

            // Since the translate calls still take a float, there can be rounding errors
            // Let's calculate the error, and adjust for it.
            final int floatErrorX = screenRect.left - (int) (float) screenRect.left;
            final int floatErrorY = screenRect.top - (int) (float) screenRect.top;

            // Translate the coordinates
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                final float scaleX = mapView.getScaleX();
                final float scaleY = mapView.getScaleY();
                c.translate(screenRect.left * scaleX, screenRect.top * scaleY);
                c.translate(floatErrorX, floatErrorY);
            } else {
                c.getMatrix(sMatrix);
                sMatrix.preTranslate(screenRect.left, screenRect.top);
                sMatrix.preTranslate(floatErrorX, floatErrorY);
                c.setMatrix(sMatrix);
            }

            if (mapView.getMapOrientation() != 0) {
                // Safely re-rotate the maps
                sSafeCanvas.rotate(mapView.getMapOrientation(), (double) screenRect.exactCenterX(),
                        (double) screenRect.exactCenterY());
            }

        } else {
            sSafeCanvas.xOffset = 0;
            sSafeCanvas.yOffset = 0;
        }
        this.drawSafe(sSafeCanvas, mapView, shadow);

        if (this.isUsingSafeCanvas()) {
            c.restore();
        }
    }

    public boolean isUsingSafeCanvas() {
        return mUseSafeCanvas;
    }

    public void setUseSafeCanvas(boolean useSafeCanvas) {
        mUseSafeCanvas = useSafeCanvas;
    }
}
