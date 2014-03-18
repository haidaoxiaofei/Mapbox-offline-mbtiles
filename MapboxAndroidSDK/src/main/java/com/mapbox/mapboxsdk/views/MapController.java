package com.mapbox.mapboxsdk.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;

public class MapController implements MapViewConstants {

    protected final MapView mMapView;

    // Zoom animations
    private ValueAnimator mZoomInAnimation;
    private ValueAnimator mZoomOutAnimation;
    private ScaleAnimation mZoomInAnimationOld;
    private ScaleAnimation mZoomOutAnimationOld;

    private Animator mCurrentAnimator;
    private float mAnimationFactor = 1.0f;
    private ILatLng zoomOnLatLong = null;
    private PointF zoomDeltaScroll = new PointF();

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
        PointF p = mMapView.getProjection().toMapPixels(point, null);
        animateTo((int)p.x, (int)p.y);
    }

    /**
     * Start animating the map towards the given point.
     */
    public void goTo(final ILatLng point, PointF delta) {
    	PointF p = mMapView.getProjection().toMapPixels(point, null);
        mMapView.scrollTo((int)(p.x + delta.x), (int)(p.y + delta.y));
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
    	zoomDeltaScroll.offset(x, y);
        this.mMapView.scrollBy(x, y);
    }

    /**
     * Set the map view to the given center. There will be no animation.
     */
    public void setCenter(final ILatLng latlng) {
        PointF p = mMapView.getProjection().toMapPixels(latlng, null);
        this.mMapView.scrollTo((int)p.x, (int)p.y);
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
        return zoomInAbout(mMapView.getCenter());
    }

    public boolean zoomInAbout(final ILatLng latlong) {

        if (!mMapView.canZoomIn()) return false;


        if (mMapView.mIsAnimating.getAndSet(true)) {
            // TODO extend zoom (and return true)
            return false;
        } else {
        	aboutToStartAnimation(latlong);
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
        return zoomOutAbout(mMapView.getCenter());
    }

    public boolean zoomOutAbout(final ILatLng latlong) {
        if (mMapView.canZoomOut()) {
            if (mMapView.mIsAnimating.getAndSet(true)) {
                // TODO extend zoom (and return true)
                return false;
            } else {
            	aboutToStartAnimation(latlong);
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
    
    protected void aboutToStartAnimation(final ILatLng latlong) {
        PointF coords = mMapView.getProjection().toMapPixels(latlong, null);
        aboutToStartAnimation(latlong, coords.x, coords.y);
    }
    
    protected void aboutToStartAnimation(final PointF coords) {
        aboutToStartAnimation(coords.x, coords.y);
    }
    
    protected void aboutToStartAnimation(final float x, final float y) {
    	final float zoom = mMapView.getZoomLevel();
    	final int worldSize_2 = TileSystem.MapSize(zoom) / 2;
        final ILatLng latlong = TileSystem.PixelXYToLatLong((int)(x
                + worldSize_2), (int)(y + worldSize_2), zoom);
        aboutToStartAnimation(latlong, x, y);
    }
    
    protected void aboutToStartAnimation(final ILatLng latlong, final float x, final float y) {
        zoomOnLatLong = latlong;
        zoomDeltaScroll.set(mMapView.getScrollX() - x, mMapView.getScrollY() - y);
        mMapView.mMultiTouchScalePoint.set(x, y);
    }

    public void onAnimationEnd() {
        setZoom(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()));
        goTo(zoomOnLatLong, zoomDeltaScroll);
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
            mMapView.updateScrollDuringAnimation(currentAnimFactor);
            mMapView.setScale(1.0f + currentAnimFactor *(mAnimationFactor - 1.0f));
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
