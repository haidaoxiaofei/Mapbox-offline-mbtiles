package com.mapbox.mapboxsdk.views;

import android.graphics.PointF;
import android.os.Handler;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class MapController implements MapViewConstants {

    protected final MapView mMapView;

    // Zoom animations
    private ValueAnimator mZoomAnimation;

    private ILatLng zoomOnLatLong = null;
    private PointF zoomDeltaScroll = new PointF();
    
    private boolean mCurrentlyUserAction = false;
    
    private ILatLng mPointToGoTo = null;
    private float mZoomToZoomTo = -1;
    
    private boolean zoomAndMoveAnimation = false;

    /**
     * Constructor
     *
     * @param mapView MapView to be controlled
     */
    public MapController(MapView mapView) {
        mMapView = mapView;
        mZoomAnimation = ValueAnimator.ofFloat(0f, 1f);
        mZoomAnimation.addListener(new MyZoomAnimatorListener());
        mZoomAnimation.addUpdateListener(new MyZoomAnimatorUpdateListener());
        mZoomAnimation.setDuration(ANIMATION_DURATION_SHORT);
    }
    
    public boolean currentlyInUserAction()
    {
    	return mCurrentlyUserAction;
    }
    
    public void setCurrentlyInUserAction(final boolean value)
    {
    	 mCurrentlyUserAction = value;
    }

    /**
     * Start animating the map towards the given point.
     */
    public void animateTo(final ILatLng point, final boolean userAction) {
        if (!mMapView.canGoTo(point)) {
            return;
        }
        if (!mMapView.isLayedOut()) {
    		mPointToGoTo = point;
            return;
        }

        PointF p = mMapView.getProjection().toMapPixels(point, null);
        animateTo((int) p.x, (int) p.y, userAction);
    }
    public void animateTo(final ILatLng point) {
    	animateTo(point, false);
    }

    /**
     * Go to a given point (not animated)
     */
    public void goTo(final ILatLng point, PointF delta) {
    	
        final Projection projection = mMapView.getProjection();
        PointF p = projection.toMapPixels(point, null);
        mMapView.scrollTo(p.x - delta.x, p.y - delta.y);
    }

    /**
     * Start animating the map towards the given point.
     */
    public void animateTo(final int x, final int y, final boolean userAction) {
    	stopAnimation(false);
    	mCurrentlyUserAction = userAction;
        mMapView.mIsFlinging = false;
        final int xStart = mMapView.getScrollX();
        final int yStart = mMapView.getScrollY();
        mMapView.getScroller()
                .startScroll(xStart, yStart, x - xStart, y - yStart,
                        ANIMATION_DURATION_DEFAULT);
        mMapView.postInvalidate();
    }
    public void animateTo(final int x, final int y) {
    	animateTo(x, y, false);
    }

    public void panBy(int x, int y, final boolean userAction) {
    	mCurrentlyUserAction = userAction;
        zoomDeltaScroll.offset(x, y);
        this.mMapView.scrollBy(x, y);
        mCurrentlyUserAction = false;
    }
    public void panBy(int x, int y) {
    	panBy(x, y, false);
    }

    /**
     * Set the map view to the given center. There will be no animation.
     */
    public void setCenter(final ILatLng latlng) {
        if (latlng == null) {
            return;
        }
        if (!mMapView.isLayedOut()) {
            mPointToGoTo = latlng;
            return;
        }
        PointF p = mMapView.getProjection().toMapPixels(latlng, null);
        this.mMapView.scrollTo(p.x, p.y);
    }

    public void stopPanning() {
        mMapView.mIsFlinging = false;
        mMapView.getScroller().forceFinished(true);
    }

    /**
     * Stops a running animation.
     */
    public void stopAnimation(final boolean jumpToTarget) {

        if (!mMapView.getScroller().isFinished()) {
            if (jumpToTarget) {
                mMapView.mIsFlinging = false;
                mMapView.getScroller().abortAnimation();
            } else {
                stopPanning();
            }
        }

        // We ignore the jumpToTarget for zoom levels since it doesn't make sense to stop
        // the animation in the middle. Maybe we could have it cancel the zoom operation and jump
        // back to original zoom level?
        if (mMapView.mIsAnimating.get()) {
            mZoomAnimation.cancel();
            setZoom(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()), false);
            if (jumpToTarget) {
                goTo(zoomOnLatLong, zoomDeltaScroll);
            }
            mMapView.mIsAnimating.set(false);
        }
    }
    
    public MapView zoomAndMoveAnimated(final ILatLng latlong, final float zoomlevel) {
    	if (!mMapView.isLayedOut()) {
            mPointToGoTo = latlong;
            mZoomToZoomTo = zoomlevel;
            return mMapView;
        }
        if (mMapView.isAnimating()) {
            stopAnimation(true);
        }
//        aboutToStartAnimation(mMapView.getCenter());
        zoomOnLatLong = latlong;
        zoomDeltaScroll.set(0, 0);
        zoomAndMoveAnimation = true;
        mMapView.mIsFlinging = false;
        
        float currentZoom = mMapView.getZoomLevel(false);
        float targetZoom = mMapView.getClampedZoomLevel(zoomlevel);
        mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));
        
        float delta = Math.abs(targetZoom - currentZoom);
        if (delta != 0) {
        	 if (targetZoom > currentZoom) {
                 mZoomAnimation.setFloatValues(1.0f, 1.0f + delta);
             }
             else {
                 mZoomAnimation.setFloatValues(1.0f, 1.0f / (1.0f + delta));
             }
             mZoomAnimation.start();
        }
        mZoomAnimation.setDuration(ANIMATION_DURATION_DEFAULT);
       

    	final int xStart = mMapView.getScrollX();
        final int yStart = mMapView.getScrollY();
        PointF p = mMapView.getProjection().toMapPixels(latlong.getLatitude(), latlong.getLongitude(), currentZoom, xStart, yStart, null);
        
        mMapView.getScroller()
                .startScroll(xStart, yStart, (int)p.x - xStart, (int)p.y - yStart,
                		ANIMATION_DURATION_DEFAULT);
        mMapView.postInvalidate();
        return mMapView;
    }
    
    public MapView setZoom(final float zoomlevel) {
        return setZoom(zoomlevel, true);
    }
    
    public MapView setZoom(final float zoomlevel, final boolean shouldStopAnimation) {
    	
    	if (shouldStopAnimation) stopAnimation(true);
        mMapView.setZoomInternal(zoomlevel);
        mMapView.setScale(1.0f);
        return mMapView;
    }
    
    public MapView setZoomAnimated(final float zoomlevel) {
        return zoomAndMoveAnimated(mMapView.getCenter(), zoomlevel);
    }

    /**
     * Zoom in by one zoom level.
     */
    public boolean zoomIn(final boolean userAction) {
        return zoomInAbout(mMapView.getCenter(), userAction);
    }
    
    public boolean zoomIn() {
        return zoomIn(false);
    }

    public boolean zoomInAbout(final ILatLng latlong, final boolean userAction) {

        if (!mMapView.canZoomIn()) {
            return false;
        }

        if (mMapView.isAnimating()) {
            // TODO extend zoom (and return true)
            return false;
        } else {
            aboutToStartAnimation(latlong);
        	mCurrentlyUserAction = userAction;
            float currentZoom = mMapView.getZoomLevel(false);
            float targetZoom = (float) (Math.ceil(currentZoom) + 1);
            float factor = (float) Math.pow(2, targetZoom - currentZoom);

            if (factor > 2.25) {
                targetZoom = (float) Math.ceil(currentZoom);
            }
            targetZoom = mMapView.getClampedZoomLevel(targetZoom);
            mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));

            float delta = Math.abs(targetZoom - currentZoom);
            mZoomAnimation.setFloatValues(1.0f, 1.0f + delta);
            mZoomAnimation.setDuration(ANIMATION_DURATION_SHORT);
            mZoomAnimation.start();
            return true;
        }
    }
    public boolean zoomInAbout(final ILatLng latlong) {
    	return zoomInAbout(latlong, false);
    }

    /**
     * Zoom out by one zoom level.
     */
    public boolean zoomOut(final boolean userAction) {
        return zoomOutAbout(mMapView.getCenter(), userAction);
    }
    
    public boolean zoomOut() {
        return zoomOut(false);
    }

    public boolean zoomOutAbout(final ILatLng latlong, final boolean userAction) {
        if (mMapView.canZoomOut()) {
            if (mMapView.isAnimating()) {
                // TODO extend zoom (and return true)
                return false;
            } else {
                aboutToStartAnimation(latlong);
            	mCurrentlyUserAction = userAction;
            	float currentZoom = mMapView.getZoomLevel(false);
                float targetZoom = (float) (Math.floor(currentZoom));
                targetZoom = mMapView.getClampedZoomLevel(targetZoom);
                float factor = (float) Math.pow(2, targetZoom - currentZoom);

                if (factor > 0.75) {
                    targetZoom = mMapView.getClampedZoomLevel((float) Math.floor(currentZoom) - 1);
                }
                mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));
                float delta = Math.abs(targetZoom - currentZoom);
                mZoomAnimation.setFloatValues(1.0f, 1.0f / (1.0f + delta));
                mZoomAnimation.setDuration(ANIMATION_DURATION_SHORT);
                mZoomAnimation.start();
                return true;
            }
        } else {
            return false;
        }
    }
    
    public boolean zoomOutAbout(final ILatLng latlong) {
    	return zoomOutAbout(latlong, false);
    }

    protected void onAnimationStart() {
        mMapView.mIsAnimating.set(true);
    }

    protected void aboutToStartAnimation(final ILatLng latlong, final PointF mapCoords) {
        zoomOnLatLong = latlong;
        final Projection projection = mMapView.getProjection();
        mMapView.mMultiTouchScalePoint.set(mapCoords.x, mapCoords.y);
        projection.toPixels(mapCoords, mapCoords);
        zoomDeltaScroll.set((float)(mapCoords.x - mMapView.getMeasuredWidth() / 2.0), (float)(mapCoords.y - mMapView.getMeasuredHeight() / 2.0));
    }

    protected void aboutToStartAnimation(final ILatLng latlong) {
        PointF mapCoords = mMapView.getProjection().toMapPixels(latlong, null);
        aboutToStartAnimation(latlong, mapCoords);
    }

    protected void aboutToStartAnimation(final PointF mapCoords) {
        final float zoom = mMapView.getZoomLevel(false);
        final int worldSize_2 = mMapView.getProjection().mapSize(zoom) >> 1;
        final ILatLng latlong = mMapView.getProjection()
                .pixelXYToLatLong((int) (mapCoords.x + worldSize_2),
                        (int) (mapCoords.y + worldSize_2), zoom);
        aboutToStartAnimation(latlong, mapCoords);
    }

    protected void aboutToStartAnimation(final float x, final float y) {
        aboutToStartAnimation(new PointF(x, y));
    }

    public void onAnimationEnd() {
        setZoom(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()), false);
        goTo(zoomOnLatLong, zoomDeltaScroll);
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
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
            float currentAnimFactor = (Float) animation.getAnimatedValue();
            mMapView.updateScrollDuringAnimation();
            if (zoomAndMoveAnimation) {
                mMapView.mMultiTouchScalePoint.set(mMapView.getScrollX(), mMapView.getScrollY());
            }
            mMapView.setScale(currentAnimFactor);
        }
    }
    
    /**
     * Called when the mapView is layed out for the first time
     * if action were triggered before we had to wait because 
     * we didn't have any projection
     */
	public void mapViewLayedOut() {
		if (mPointToGoTo != null) {
			setCenter(mPointToGoTo);
             mPointToGoTo = null;
		}
		if (mZoomToZoomTo != -1) {
			setZoom(mZoomToZoomTo);
            mZoomToZoomTo = -1;
        }
		
	}
}
