package com.mapbox.mapboxsdk.views;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class MapController implements MapViewConstants {

    protected final MapView mMapView;

    // Zoom animations
    private ValueAnimator mZoomInAnimation;
    private ValueAnimator mZoomOutAnimation;
    private ScaleAnimation mZoomInAnimationOld;
    private ScaleAnimation mZoomOutAnimationOld;

    private Animator mCurrentAnimator;
    private float mAnimationFactor = 1.0f;

    public MapController(MapView mapView) {
        mMapView = mapView;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mZoomInAnimation = ValueAnimator.ofFloat(0f, 1f);
            mZoomInAnimation.addListener(new MyZoomAnimatorListener());
            mZoomInAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
            mZoomInAnimation.setDuration(ANIMATION_DURATION_SHORT);

            mZoomOutAnimation = ValueAnimator.ofFloat(0f, 1f);
            mZoomOutAnimation.addListener(new MyZoomAnimatorListener());
            mZoomOutAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
            mZoomOutAnimation.setDuration(ANIMATION_DURATION_SHORT);
        } else {
            mZoomInAnimationOld = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mZoomOutAnimationOld = new ScaleAnimation(1, 0.5f, 1, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mZoomInAnimationOld.setDuration(ANIMATION_DURATION_SHORT);
            mZoomOutAnimationOld.setDuration(ANIMATION_DURATION_SHORT);
            mZoomInAnimationOld.setAnimationListener(new MyZoomAnimationListener());
            mZoomOutAnimationOld.setAnimationListener(new MyZoomAnimationListener());
        }
    }

    /**
     * Start animating the map towards the given point.
     */
    public void animateTo(final ILatLng point) {
        Point p = mMapView.getProjection().toMapPixels(point, null);
        animateTo(p.x, p.y);
    }

    /**
     * Start animating the map towards the given point.
     */
    public void animateTo(int x, int y) {
        if (!mMapView.isAnimating()) {
            mMapView.mIsFlinging = false;
            final int xStart = mMapView.getScrollX();
            final int yStart = mMapView.getScrollY();
            mMapView.getScroller().startScroll(xStart, yStart, x - xStart, y - yStart,
                    ANIMATION_DURATION_DEFAULT);
            mMapView.postInvalidate();
        }
    }

    public void panBy(int x, int y) {
        this.mMapView.scrollBy(x, y);
    }

    /**
     * Set the map view to the given center. There will be no animation.
     */
    public void setCenter(final ILatLng latlng) {
        Point p = mMapView.getProjection().toMapPixels(latlng, null);
        this.mMapView.scrollTo(p.x, p.y);
    }

    public void stopPanning() {
        mMapView.mIsFlinging = false;
        mMapView.getScroller().forceFinished(true);
    }

    /**
     * Stops a running animation.
     *
     * @param jumpToTarget
     */
    public void stopAnimation(final boolean jumpToTarget) {

        if (!mMapView.getScroller().isFinished()) {
            if (jumpToTarget) {
                mMapView.mIsFlinging = false;
                mMapView.getScroller().abortAnimation();
            } else
                stopPanning();
        }

        // We ignore the jumpToTarget for zoom levels since it doesn't make sense to stop
        // the animation in the middle. Maybe we could have it cancel the zoom operation and jump
        // back to original zoom level?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final Animator currentAnimator = this.mCurrentAnimator;
            if (mMapView.mIsAnimating.get()) {
                currentAnimator.end();
            }
        } else {
            if (mMapView.mIsAnimating.get()) {
                mMapView.clearAnimation();
            }
        }
    }


    public MapView setZoom(final float zoomlevel) {
        return mMapView.setZoom(zoomlevel);
    }

    /**
     * Zoom in by one zoom level.
     */
    public boolean zoomIn() {
        Point coords = mMapView.getProjection().toMapPixels(mMapView.getCenter(), null);
        return zoomInAbout(coords.x, coords.y);
    }

    public boolean zoomInAbout(final int xPixel, final int yPixel) {
        mMapView.mMultiTouchScalePoint.set(xPixel, yPixel);
        if (!mMapView.canZoomIn()) return false;

        if (mMapView.mIsAnimating.getAndSet(true)) {
            // TODO extend zoom (and return true)
            return false;
        } else {
        	float currentZoom = mMapView.getZoomLevel(false);
        	float targetZoom =(float) (Math.ceil(currentZoom) + 1);
        	float factor = (float) Math.pow(2, targetZoom - currentZoom);

            if (factor > 2.25)
            {
            	targetZoom = (float) Math.ceil(currentZoom);
            }
            targetZoom = mMapView.getClampedZoomLevel(targetZoom);
        	mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));
        	mAnimationFactor = (float)(currentZoom - 1.0f + Math.ceil(targetZoom))/currentZoom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mCurrentAnimator = mZoomInAnimation;
                mZoomInAnimation.start();
            } else {
                mMapView.startAnimation(mZoomInAnimationOld);
            }
            return true;
        }
    }

    /**
     * Zoom out by one zoom level.
     */
    public boolean zoomOut() {
        Point coords = mMapView.getProjection().toMapPixels(mMapView.getCenter(), null);
        return zoomOutAbout(coords.x, coords.y);
    }

    public boolean zoomOutAbout(final int xPixel, final int yPixel) {
        mMapView.mMultiTouchScalePoint.set(xPixel, yPixel);
        if (mMapView.canZoomOut()) {
            if (mMapView.mIsAnimating.getAndSet(true)) {
                // TODO extend zoom (and return true)
                return false;
            } else {
            	float currentZoom = mMapView.getZoomLevel(false);
            	float targetZoom =(float) (Math.floor(currentZoom));
                targetZoom = mMapView.getClampedZoomLevel(targetZoom);
                float factor = (float) Math.pow(2, targetZoom - currentZoom);

                if (factor > 0.75)
                {
                	targetZoom = mMapView.getClampedZoomLevel((float) Math.floor(currentZoom) - 1);
                }
                mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));
            	mAnimationFactor = targetZoom/(float)(currentZoom - 1.0f + Math.floor(targetZoom));
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mCurrentAnimator = mZoomOutAnimation;
                    mZoomOutAnimation.start();
                } else {
                    mMapView.startAnimation(mZoomOutAnimationOld);
                }
                return true;
            }
        } else {
            return false;
        }
    }

    protected void onAnimationStart() {
        mMapView.mIsAnimating.set(true);
    }

    public void onAnimationEnd() {
        final Rect screenRect = mMapView.getProjection().getScreenRect();
        final Matrix m = new Matrix();
        m.setScale(1 / mMapView.mMultiTouchScale, 1 / mMapView.mMultiTouchScale,
            mMapView.mMultiTouchScalePoint.x, mMapView.mMultiTouchScalePoint.y);
        m.postRotate(
                -mMapView.getMapOrientation(),
                screenRect.exactCenterX(),
                screenRect.exactCenterY());
        float[] pts = new float[2];
        pts[0] = mMapView.getScrollX();
        pts[1] = mMapView.getScrollY();
        m.mapPoints(pts);
        mMapView.scrollTo((int) pts[0], (int) pts[1]);
        setZoom(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()));
        mMapView.mMultiTouchScale = 1f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCurrentAnimator = null;
        }
        mMapView.mIsAnimating.set(false);
    }

    protected class MyZoomAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            MapController.this.onAnimationStart();
            super.onAnimationStart(animation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            MapController.this.onAnimationEnd();
            super.onAnimationEnd(animation);
        }
    }

    protected class MyZoomAnimatorUpdateListener implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
        	float currentAnimFactor = (Float)animation.getAnimatedValue();
        	mMapView.mMultiTouchScale = 1.0f + currentAnimFactor *(mAnimationFactor - 1.0f);
            mMapView.updateScrollDuringAnimation(currentAnimFactor);
            mMapView.invalidate();
        }
    }

    protected class MyZoomAnimationListener implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            MapController.this.onAnimationStart();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            MapController.this.onAnimationEnd();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // Nothing to do here...
        }
    }

    private static String TAG = "MapController";
}
