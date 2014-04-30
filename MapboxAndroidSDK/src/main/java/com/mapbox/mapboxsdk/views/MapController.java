package com.mapbox.mapboxsdk.views;

import android.graphics.PointF;
import android.os.Handler;
import android.view.animation.LinearInterpolator;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.TypeEvaluator;

import java.util.ArrayList;
import java.util.List;

public class MapController implements MapViewConstants {

    public class PointEvaluator implements TypeEvaluator<PointF> {

        public PointEvaluator() {
        }

        public PointF evaluate(float fraction, PointF startValue,
                               PointF endValue) {
            return new PointF((fraction * (endValue.x  - startValue.x) + startValue.x), (fraction * (endValue.y  - startValue.y) + startValue.y));
        }

    }

    private static final String TAG = "Mapbox MapController";

    protected final MapView mMapView;

    // Zoom animations
    private ObjectAnimator mCurrentAnimation;

    private ILatLng zoomOnLatLong = null;
    private PointF zoomDeltaScroll = new PointF();
    private ILatLng animateToTargetPoint = null;
    private boolean mCurrentlyUserAction = false;
    private ILatLng mPointToGoTo = null;
    private float mZoomToZoomTo = -1;

    /**
     * Constructor
     *
     * @param mapView MapView to be controlled
     */
    public MapController(MapView mapView) {
        mMapView = mapView;
    }

    public boolean currentlyInUserAction() {
        return mCurrentlyUserAction;
    }

    public void setCurrentlyInUserAction(final boolean value) {
        mCurrentlyUserAction = value;
    }


    protected void aboutToStartAnimation(final ILatLng latlong, final PointF mapCoords) {
        zoomOnLatLong = latlong;
        final Projection projection = mMapView.getProjection();
        mMapView.mMultiTouchScalePoint.set(mapCoords.x, mapCoords.y);
        projection.toPixels(mapCoords, mapCoords);
        zoomDeltaScroll.set((float) (mMapView.getMeasuredWidth() / 2.0 - mapCoords.x), (float) (mMapView.getMeasuredHeight() / 2.0 - mapCoords.y));
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

    /**
     * Start animating the map towards the given point.
     */
    public void animateTo(final ILatLng point, final boolean userAction) {
        setZoomAnimated(point, mMapView.getZoomLevel(), true, userAction);
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
        p.offset(delta.x, delta.y);
        mMapView.scrollTo(p.x, p.y);
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
        setCenter(latlng, null);
    }

    public void setCenter(final ILatLng latlng, final PointF decale) {
        if (latlng == null) {
            return;
        }
        if (!mMapView.isLayedOut()) {
            mPointToGoTo = latlng;
            return;
        }
        PointF p = mMapView.getProjection().toMapPixels(latlng, null);
        if (decale != null) {
            p.offset(decale.x, decale.y);
        }
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
                setCenter(animateToTargetPoint);
            } else {
                stopPanning();
            }
        }

        // We ignore the jumpToTarget for zoom levels since it doesn't make sense to stop
        // the animation in the middle. Maybe we could have it cancel the zoom operation and jump
        // back to original zoom level?
        if (mMapView.mIsAnimating.get()) {
            mCurrentAnimation.cancel();
            mMapView.setZoomInternal(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()));
            if (jumpToTarget && zoomOnLatLong != null) {
                goTo(zoomOnLatLong, zoomDeltaScroll);
            }
            mMapView.mIsAnimating.set(false);
        }
    }

    public boolean setZoomAnimated(final ILatLng latlong, final float zoomlevel, final boolean move, final boolean userAction) {
        if (!mMapView.isLayedOut()) {
            mPointToGoTo = latlong;
            mZoomToZoomTo = zoomlevel;
            return false;
        }

        stopAnimation(true);
        mCurrentlyUserAction = userAction;
        mMapView.mIsFlinging = false;

        float currentZoom = mMapView.getZoomLevel(false);

        final PointF dCurrentScroll = mMapView.getScrollPoint();
        PointF p = Projection.toMapPixels(latlong.getLatitude(), latlong.getLongitude(), currentZoom, dCurrentScroll.x, dCurrentScroll.y, null);

        mMapView.mMultiTouchScalePoint.set(p.x, p.y);
        List<PropertyValuesHolder> propertiesList = new ArrayList<PropertyValuesHolder>();
        float targetZoom = mMapView.getClampedZoomLevel(zoomlevel);
        boolean zoomAnimating = (targetZoom != currentZoom);
        zoomDeltaScroll.set(0, 0);
        if (zoomAnimating) {
            zoomOnLatLong = latlong;
            mMapView.mTargetZoomLevel.set(Float.floatToIntBits(targetZoom));

            float factor = (float) Math.pow(2, targetZoom - currentZoom);
            float delta = (targetZoom - currentZoom);
            if (delta > 0) {
                propertiesList.add(PropertyValuesHolder.ofFloat("scale", 1.0f, factor));
            } else {
                propertiesList.add(PropertyValuesHolder.ofFloat("scale", 1.0f, factor));
            }
        }
        boolean zoomAndMove = move && !p.equals(dCurrentScroll);
        if (zoomAndMove) {
            PointEvaluator evaluator = new PointEvaluator();
            propertiesList.add(PropertyValuesHolder.ofObject(
                    "scrollPoint", evaluator,
                    p));
        } else {
            mMapView.getProjection().toPixels(p, p);
            zoomDeltaScroll.set((float) (mMapView.getMeasuredWidth() / 2.0 - p.x), (float) (mMapView.getMeasuredHeight() / 2.0 - p.y));
        }

        if (propertiesList.size() > 0) {
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, propertiesList.toArray(new PropertyValuesHolder[0]));

            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(zoomAndMove ? ANIMATION_DURATION_DEFAULT : ANIMATION_DURATION_SHORT);
            anim.setTarget(mMapView);
            anim.addListener(new AnimatorListenerAdapter() {
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
            });
            mCurrentAnimation = anim;
            anim.start();
            return true;
        }

        return false;
    }

    public MapView setZoom(final float zoomlevel) {
        return setZoom(zoomlevel, false);
    }

    public MapView setZoom(final float zoomlevel, final boolean userAction) {
        mCurrentlyUserAction = userAction;
        stopAnimation(true);
        mMapView.setZoomInternal(zoomlevel);
        mCurrentlyUserAction = false;
        return mMapView;
    }

    public MapView setZoomAnimated(final float zoomlevel) {
        setZoomAnimated(zoomlevel, mMapView.getCenter(), false);
        return mMapView;
    }

    public MapView setZoomAnimated(final float zoomlevel, final ILatLng latlong, final boolean userAction) {
        setZoomAnimated(latlong, zoomlevel, false, userAction);
        return mMapView;
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
        float currentZoom = mMapView.getZoomLevel(false);
        float targetZoom = (float) (Math.ceil(currentZoom) + 1);
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 2.25) {
            targetZoom = (float) Math.ceil(currentZoom);
        }
        return setZoomAnimated(latlong, targetZoom, false, userAction);
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
        float currentZoom = mMapView.getZoomLevel(false);
        float targetZoom = (float) (Math.floor(currentZoom));
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 0.75) {
            targetZoom = (float) (Math.floor(currentZoom) - 1);
        }

        return setZoomAnimated(latlong, targetZoom, false, userAction);
    }

    public boolean zoomOutAbout(final ILatLng latlong) {
        return zoomOutAbout(latlong, false);
    }

    protected void onAnimationStart() {
        mMapView.mIsAnimating.set(true);
    }

    public void onAnimationEnd() {
        stopPanning();
        mMapView.setZoomInternal(Float.intBitsToFloat(mMapView.mTargetZoomLevel.get()), zoomOnLatLong, zoomDeltaScroll);
        zoomOnLatLong = null;
        mCurrentlyUserAction = false;
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapView.mIsAnimating.set(false);
            }
        }, 100);
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
