package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Overlay.Snappable;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import java.util.LinkedList;

/**
 * @author Marc Kurtz
 * @author Manuel Stahl
 */
public class UserLocationOverlay extends SafeDrawOverlay implements Snappable {

    protected final SafePaint mPaint = new SafePaint();
    protected final SafePaint mCirclePaint = new SafePaint();

    protected final Bitmap mPersonBitmap;
    protected final Bitmap mDirectionArrowBitmap;

    protected final MapView mMapView;
    protected final Context mContext;

    private final MapController mMapController;
    public GpsLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final PointF mMapCoords = new PointF();

    private Location mLocation;
    private boolean mIsLocationEnabled = false;
    protected boolean mIsFollowing = false; // follow location updates
    protected boolean mDrawAccuracyEnabled = true;

    /**
     * Coordinates the feet of the person are located scaled for display density.
     */
    protected final PointF mPersonHotspot;

    protected final double mDirectionArrowCenterX;
    protected final double mDirectionArrowCenterY;

    public static final int MENU_MY_LOCATION = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    // to avoid allocations during onDraw
    private final float[] mMatrixValues = new float[9];
    private final Matrix mMatrix = new Matrix();
    private final RectF mMyLocationRect = new RectF();
    private final RectF mMyLocationPreviousRect = new RectF();


    public UserLocationOverlay(GpsLocationProvider myLocationProvider, MapView mapView) {
        super();

        mMapView = mapView;
        mMapController = mapView.getController();
        mContext = mapView.getContext();
        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        mPersonBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person);
        mDirectionArrowBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.direction_arrow);

        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0 - 0.5;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0 - 0.5;

        // Calculate position of person icon's feet, scaled to screen density
        mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);

        setMyLocationProvider(myLocationProvider);
    }

    @Override
    public void onDetach(MapView mapView) {
        this.disableMyLocation();
        super.onDetach(mapView);
    }

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @param drawAccuracyEnabled whether the accuracy circle will be enabled
     */
    public void setDrawAccuracyEnabled(final boolean drawAccuracyEnabled) {
        mDrawAccuracyEnabled = drawAccuracyEnabled;
    }

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isDrawAccuracyEnabled() {
        return mDrawAccuracyEnabled;
    }

    public GpsLocationProvider getMyLocationProvider() {
        return mMyLocationProvider;
    }

    protected void setMyLocationProvider(GpsLocationProvider myLocationProvider) {

        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }

        mMyLocationProvider = myLocationProvider;
    }

    public void setPersonHotspot(float x, float y) {
        mPersonHotspot.set(x, y);
    }

    protected void drawMyLocation(final ISafeCanvas canvas, final MapView mapView,
                                  final Location lastFix) {
        final Projection pj = mapView.getProjection();
        final float zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - pj.getZoomLevel();

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    mapView.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Style.FILL);
            canvas.drawCircle(GeometryMath.rightShift(mMapCoords.x, zoomDiff) , GeometryMath.rightShift(mMapCoords.y, zoomDiff), radius,
                    mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Style.STROKE);
            canvas.drawCircle(GeometryMath.rightShift(mMapCoords.x, zoomDiff), GeometryMath.rightShift(mMapCoords.y, zoomDiff), radius,
                    mCirclePaint);
        }

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        if (UtilConstants.DEBUGMODE) {
            final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
                    / mMatrixValues[Matrix.MSCALE_X];
            final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
                    / mMatrixValues[Matrix.MSCALE_Y];
            canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
            canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
            canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
            canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
        }

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);
        final float x = GeometryMath.rightShift(mMapCoords.x, zoomDiff);
        final float y = GeometryMath.rightShift(mMapCoords.y, zoomDiff);
        if (lastFix.hasBearing()) {
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(mDirectionArrowBitmap, x - mDirectionArrowCenterX, y
                    - mDirectionArrowCenterY, mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-mMapView.getMapOrientation(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(mPersonBitmap, x - mPersonHotspot.x, y - mPersonHotspot.y, mPaint);
            canvas.restore();
        }
    }

    protected RectF getMyLocationDrawingBounds(float zoomLevel, Location lastFix, RectF reuse) {
        if (reuse == null)
            reuse = new RectF();

        final float zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - zoomLevel;
        final float posX = GeometryMath.rightShift(mMapCoords.x, zoomDiff);
        final float posY = GeometryMath.rightShift(mMapCoords.y, zoomDiff);

        // Start with the bitmap bounds
        if (lastFix.hasBearing()) {
            // Get a square bounding box around the object, and expand by the length of the diagonal
            // so as to allow for extra space for rotating
            int widestEdge = (int) Math.ceil(Math.max(mDirectionArrowBitmap.getWidth(),
                    mDirectionArrowBitmap.getHeight()) * Math.sqrt(2));
            reuse.set(posX, posY, posX + widestEdge, posY + widestEdge);
            reuse.offset(-widestEdge / 2, -widestEdge / 2);
        } else {
            reuse.set(posX, posY, posX + mPersonBitmap.getWidth(), posY + mPersonBitmap.getHeight());
            reuse.offset((int) (-mPersonHotspot.x + 0.5f), (int) (-mPersonHotspot.y + 0.5f));
        }

        // Add in the accuracy circle if enabled
        if (mDrawAccuracyEnabled) {
            final float radius = (float) Math.ceil(lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(), zoomLevel));
            reuse.union(posX - radius, posY - radius, posX + radius, posY + radius);
            final float strokeWidth = (float) Math.ceil(mCirclePaint.getStrokeWidth() == 0 ? 1
                    : mCirclePaint.getStrokeWidth());
            reuse.inset(-strokeWidth, -strokeWidth);
        }

        return reuse;
    }

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
        if (shadow)
            return;

        if (mLocation != null && isMyLocationEnabled()) {
            drawMyLocation(canvas, mapView, mLocation);
        }
    }

    @Override
    public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
                                final MapView mapView) {
        if (this.mLocation != null) {
            snapPoint.x = (int)mMapCoords.x;
            snapPoint.y = (int)mMapCoords.y;
            final double xDiff = x - mMapCoords.x;
            final double yDiff = y - mMapCoords.y;
            final boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
            if (UtilConstants.DEBUGMODE) {
                Log.i(TAG, "snap=" + snap);
            }
            return snap;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.disableFollowLocation();
        }

        return super.onTouchEvent(event, mapView);
    }

    /**
     * Return a LatLng of the last known location, or null if not known.
     */
    public LatLng getMyLocation() {
        if (mLocation == null) {
            return null;
        } else {
            return new LatLng(mLocation);
        }
    }

    public Location getLastFix() {
        return mLocation;
    }

    /**
     * Enables "follow" functionality. The map will center on your current location and
     * automatically scroll as you move. Scrolling the map in the UI will disable.
     */
    public void enableFollowLocation() {
        mIsFollowing = true;

        // set initial location when enabled
        if (isMyLocationEnabled()) {
            mLocation = mMyLocationProvider.getLastKnownLocation();
            if (mLocation != null) {
                TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                        MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
                final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
                mMapCoords.offset(-worldSize_2, -worldSize_2);
                mMapController.animateTo(new LatLng(mLocation));
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * Disables "follow" functionality.
     */
    public void disableFollowLocation() {
        mIsFollowing = false;
    }

    /**
     * If enabled, the map will center on your current location and automatically scroll as you
     * move. Scrolling the map in the UI will disable.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isFollowLocationEnabled() {
        return mIsFollowing;
    }

    public void onLocationChanged(Location location, GpsLocationProvider source) {
        // If we had a previous location, let's get those bounds
        Location oldLocation = mLocation;
        if (oldLocation != null) {
            this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), oldLocation,
                    mMyLocationPreviousRect);
        }

        mLocation = location;
        mMapCoords.set(0, 0);

        if (mLocation != null) {
            TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                    MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
            final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
            mMapCoords.offset(-worldSize_2, -worldSize_2);

            if (mIsFollowing) {
                mMapController.animateTo(new LatLng(mLocation));
            } else {
                // Get new drawing bounds
                this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

                // If we had a previous location, merge in those bounds too
                if (oldLocation != null) {
                    mMyLocationRect.union(mMyLocationPreviousRect);
                }

                final Rect invalidateRect = new Rect();
                mMyLocationRect.round(invalidateRect);
                // Invalidate the bounds
                mMapView.post(new Runnable() {
                    @Override
                    public void run() {
                        mMapView.invalidateMapCoordinates(invalidateRect);
                    }
                });
            }
        }

        for (final Runnable runnable : mRunOnFirstFix) {
            new Thread(runnable).start();
        }
        mRunOnFirstFix.clear();
    }

    public boolean enableMyLocation(GpsLocationProvider myLocationProvider) {
        this.setMyLocationProvider(myLocationProvider);
        mIsLocationEnabled = false;
        return enableMyLocation();
    }

    /**
     * Enable receiving location updates from the provided GpsLocationProvider and show your
     * location on the maps. You will likely want to call enableMyLocation() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableMyLocation() {
        if (mIsLocationEnabled)
            mMyLocationProvider.stopLocationProvider();

        boolean result = mMyLocationProvider.startLocationProvider(this);
        mIsLocationEnabled = result;

        // set initial location when enabled
        if (result && isFollowLocationEnabled()) {
            mLocation = mMyLocationProvider.getLastKnownLocation();
            if (mLocation != null) {
                TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                        MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
                final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
                mMapCoords.offset(-worldSize_2, -worldSize_2);
                mMapController.animateTo(new LatLng(mLocation));
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }

        return result;
    }

    /**
     * Disable location updates
     */
    public void disableMyLocation() {
        mIsLocationEnabled = false;

        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * If enabled, the map is receiving location updates and drawing your location on the map.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isMyLocationEnabled() {
        return mIsLocationEnabled;
    }

    public boolean runOnFirstFix(final Runnable runnable) {
        if (mMyLocationProvider != null && mLocation != null) {
            new Thread(runnable).start();
            return true;
        } else {
            mRunOnFirstFix.addLast(runnable);
            return false;
        }
    }

    private static final String TAG = "UserLocationOverlay";
}
