package com.mapbox.mapboxsdk.views;

import android.graphics.PointF;
import android.os.Handler;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class MapController implements MapViewConstants {

    protected final MapView mMapView;

    // Zoom animations
    private ValueAnimator mZoomAnimation;

    private float mAnimationFactor = 1.0f;
    private ILatLng zoomOnLatLong = null;
    private PointF zoomDeltaScroll = new PointF();

	/**
	 * Constructor
	 * @param mapView MapView to be controlled
	 */
	public MapController(MapView mapView)
	{
		mMapView = mapView;
		mZoomAnimation = ValueAnimator.ofFloat(0f, 1f);
		mZoomAnimation.addListener(new MyZoomAnimatorListener());
		mZoomAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
		mZoomAnimation.setDuration(ANIMATION_DURATION_SHORT);
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
        if (mMapView.mIsAnimating.get()) {
            mZoomAnimation.end();
        }
    }


    public MapView setZoom(final float zoomlevel) {
        mMapView.setScale(1.0f);
        mMapView.setZoomInternal(zoomlevel);
        return mMapView;
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
            
        	float delta = Math.abs(targetZoom - currentZoom);
        	mZoomAnimation.setFloatValues(1.0f, 1.0f + delta);
            mZoomAnimation.start();
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
            	float delta = Math.abs(targetZoom - currentZoom);
            	mZoomAnimation.setFloatValues(1.0f, 1.0f/(1.0f + delta));
               mZoomAnimation.start();
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
        final ILatLng latlong = TileSystem.PixelXYToLatLong((int)(x + worldSize_2), (int)(y + worldSize_2), zoom);
        aboutToStartAnimation(latlong, x, y);
    }
    
    protected void aboutToStartAnimation(final ILatLng latlong, final float x, final float y) {
        mMapView.mIsAnimating.set(true);
        zoomOnLatLong = latlong;
        zoomDeltaScroll.set(mMapView.getScrollX() - x, mMapView.getScrollY() - y);
        mMapView.mMultiTouchScalePoint.set(x, y);
    }

    public void onAnimationEnd() {
        setZoom(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()));
        goTo(zoomOnLatLong, zoomDeltaScroll);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
        @Override
        	public void run(){
        		mMapView.mIsAnimating.set(false);
           }
        }, 100);
        
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
            mMapView.updateScrollDuringAnimation();
            mMapView.setScale(currentAnimFactor);
        }
    }

    private static String TAG = "MapController";
}
