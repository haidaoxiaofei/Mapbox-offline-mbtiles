package com.mapbox.mapboxsdk.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.format.GeoJSON;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GeoJSONLayer;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.ItemizedOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsReceiver;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.OverlayManager;
import com.mapbox.mapboxsdk.overlay.TilesOverlay;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBasic;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleInvalidationHandler;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.mapbox.mapboxsdk.views.util.constants.MapViewLayouts;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;

/**
 * The MapView class manages all of the content and
 * state of a single map, including layers, markers,
 * and interaction code.
 */
@SuppressLint("WrongCall")
public class MapView extends ViewGroup
        implements MapViewConstants, MapEventsReceiver, MapboxConstants {
    /**
     * The default marker Overlay, automatically added to the view to add markers directly.
     */
    private ItemizedIconOverlay defaultMarkerOverlay;
    /**
     * List linked to the default marker overlay.
     */
    private ArrayList<Marker> defaultMarkerList = new ArrayList<Marker>();
    /**
     * Overlay for basic map touch events.
     */
    private MapEventsOverlay eventsOverlay;
    /**
     * A copy of the app context.
     */
    private Context context;
    /**
     * Whether or not a marker has been placed already.
     */
    private boolean firstMarker = true;

    private static final String TAG = "MapBox MapView";
    private static Method sMotionEventTransformMethod;

    /**
     * Current zoom level for map tiles.
     */
    private float mZoomLevel = 11;
    protected float mRequestedMinimumZoomLevel = 0;
    private float mMinimumZoomLevel = 0;
    private float mMaximumZoomLevel = 22;

    /**
     * The MapView listener
     */
    private MapViewListener mMapViewListener;

    private final OverlayManager mOverlayManager;

    private Projection mProjection;
    private boolean mReadyToComputeProjection;

    private final TilesOverlay mMapOverlay;

    private final GestureDetector mGestureDetector;

    /**
     * Handles map scrolling
     */
    protected final Scroller mScroller;
    protected boolean mIsFlinging;

    protected final AtomicInteger mTargetZoomLevel = new AtomicInteger();
    protected final AtomicBoolean mIsAnimating = new AtomicBoolean(false);

    private final MapController mController;

    protected ScaleGestureDetector mScaleGestureDetector;
    protected float mMultiTouchScale = 1.0f;
    protected PointF mMultiTouchScalePoint = new PointF();

    protected List<MapListener> mListeners = new ArrayList<MapListener>();

    private float mapOrientation = 0;
    private final float[] mRotatePoints = new float[2];
    private final Rect mInvalidateRect = new Rect();

    protected BoundingBox mScrollableAreaBoundingBox = null;
    protected RectF mScrollableAreaLimit = null;
    protected RectF mTempRect = new RectF();

    private BoundingBox mBoundingBoxToZoomOn = null;

    // for speed (avoiding allocations)
    protected final MapTileLayerBase mTileProvider;

    private final Handler mTileRequestCompleteHandler;

    /* a point that will be reused to design added views */
    private final PointF mPoint = new PointF();

    private TilesLoadedListener tilesLoadedListener;
    TileLoadedListener tileLoadedListener;
    private InfoWindow currentTooltip;

    private int mDefaultPinRes = R.drawable.defpin;
    private Drawable mDefaultPinDrawable;
    private PointF mDefaultPinAnchor = DEFAULT_PIN_ANCHOR;

    /**
     * Constructor for XML layout calls. Should not be used programmatically.
     *
     * @param aContext A copy of the app context
     * @param attrs An AttributeSet object to get extra info from the XML, such as mapbox id or
     * type
     * of baselayer
     */
    protected MapView(final Context aContext, final int tileSizePixels,
            MapTileLayerBase tileProvider, final Handler tileRequestCompleteHandler,
            final AttributeSet attrs) {
        super(aContext, attrs);
        setWillNotDraw(false);
        mReadyToComputeProjection = false;
        this.mController = new MapController(this);
        this.mScroller = new Scroller(aContext);
        Projection.setTileSize(tileSizePixels);

        if (tileProvider == null) {
            tileProvider = new MapTileLayerBasic(aContext, null, this);
        }

        mTileRequestCompleteHandler =
                tileRequestCompleteHandler == null ? new SimpleInvalidationHandler(this)
                        : tileRequestCompleteHandler;
        mTileProvider = tileProvider;
        mTileProvider.setTileRequestCompleteHandler(mTileRequestCompleteHandler);

        this.mMapOverlay = new TilesOverlay(mTileProvider);
        mOverlayManager = new OverlayManager(mMapOverlay);

        this.mGestureDetector =
                new GestureDetector(aContext, new MapViewGestureDetectorListener(this));

        mScaleGestureDetector =
                new ScaleGestureDetector(aContext, new MapViewScaleGestureDetectorListener(this));

        this.context = aContext;
        eventsOverlay = new MapEventsOverlay(aContext, this);
        this.getOverlays().add(eventsOverlay);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MapView);
        String mapid = a.getString(R.styleable.MapView_mapid);
        if (mapid != null) {
            setTileSource(new MapboxTileLayer(mapid));
        } else {
            Log.w(MapView.class.getCanonicalName(), "mapid not set.");
        }
        String centerLat = a.getString(R.styleable.MapView_centerLat);
        String centerLng = a.getString(R.styleable.MapView_centerLng);
        if (centerLat != null && centerLng != null) {
            double lat, lng;
            lat = Double.parseDouble(centerLat);
            lng = Double.parseDouble(centerLng);
            this.setCenter(new LatLng(lat, lng));
        } else {
            Log.d(MapView.class.getCanonicalName(), "centerLatLng is not specified in XML.");
        }
        String zoomLvl = a.getString(R.styleable.MapView_zoomLevel);
        if (zoomLvl != null) {
            float lvl = Float.parseFloat(zoomLvl);
            this.setZoom(lvl);
        } else {
            Log.d(MapView.class.getCanonicalName(), "zoomLevel is not specified in XML.");
        }
        a.recycle();
    }

    public MapView(final Context aContext) {
        this(aContext, 256, null, null, null);
    }

    public MapView(final Context aContext, AttributeSet attrs) {
        this(aContext, 256, null, null, attrs);
    }

    protected MapView(Context aContext, int tileSizePixels, MapTileLayerBase aTileProvider) {
        this(aContext, tileSizePixels, aTileProvider, null, null);
    }
    
    public void addListener(final MapListener listener) {
    	if (!mListeners.contains(listener)) {
        	mListeners.add(listener);
    	}
    }
    
    public void removeListener(MapListener listener) {
    	if (!mListeners.contains(listener)) {
        	mListeners.remove(listener);
    	}
    }
    

    public void setTileSource(final ITileLayer[] value) {
        if (mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            LatLng center = getCenter();
            ((MapTileLayerBasic) mTileProvider).setTileSources(value);
            this.setMinZoomLevel(mTileProvider.getMinimumZoomLevel());
            this.setMaxZoomLevel(mTileProvider.getMaximumZoomLevel());
            this.setZoom(mZoomLevel);
            this.setCenter(center);
            postInvalidate();
        }
    }

    public void setTileSource(final ITileLayer aTileSource) {
        mTileProvider.setTileSource(aTileSource);
        Projection.setTileSize(aTileSource.getTileSizePixels());
        this.setZoom(mZoomLevel);
        postInvalidate();
    }

    public void addTileSource(final ITileLayer aTileSource) {
        if (mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).addTileSource(aTileSource);
            this.setZoom(mZoomLevel);
            postInvalidate();
        }
    }

    public void removeTileSource(final ITileLayer aTileSource) {
        if (mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).removeTileSource(aTileSource);
            this.setZoom(mZoomLevel);
            postInvalidate();
        }
    }

    /**
     * Adds a marker to the default marker overlay.
     *
     * @param marker the marker object to be added
     * @return the marker object
     */
    public Marker addMarker(final Marker marker) {
        if (firstMarker) {
            defaultMarkerList.add(marker);
            setDefaultItemizedOverlay();
        } else {
            defaultMarkerOverlay.addItem(marker);
        }
        marker.addTo(this);
        firstMarker = false;
        return marker;
    }

    /**
     * Remove a marker from the map's display.
     */
    public void removeMarker(final Marker marker) {
        defaultMarkerList.remove(marker);
        defaultMarkerOverlay.removeItem(marker);
        this.invalidate();
    }

    /**
     * Select a marker, showing a tooltip if the marker has content that would appear within it.
     */
    public void selectMarker(final Marker marker) {
        InfoWindow toolTip = marker.getToolTip(MapView.this);

        if (mMapViewListener != null) {
            mMapViewListener.onTapMarker(MapView.this, marker);
        }
        closeCurrentTooltip();
        if (toolTip != currentTooltip && marker.hasContent()) {
            if (mMapViewListener != null) {
                mMapViewListener.onShowMarker(MapView.this, marker);
            }
            currentTooltip = toolTip;
            marker.showBubble(currentTooltip, MapView.this, true);
        }
    }

    /**
     * Adds a new ItemizedOverlay to the MapView
     *
     * @param itemizedOverlay the itemized overlay
     */
    public void addItemizedOverlay(ItemizedOverlay itemizedOverlay) {
        this.getOverlays().add(itemizedOverlay);
    }

    public ArrayList<ItemizedIconOverlay> getItemizedOverlays() {
        ArrayList<ItemizedIconOverlay> list = new ArrayList<ItemizedIconOverlay>();
        for (Overlay overlay : getOverlays()) {
            if (overlay instanceof ItemizedOverlay) {
                list.add((ItemizedIconOverlay) overlay);
            }
        }
        return list;
    }

    /**
     * Load and parse a GeoJSON file at a given URL
     *
     * @param URL the URL from which to load the GeoJSON file
     */
    public void loadFromGeoJSONURL(String URL) {
        if (NetworkUtils.isNetworkAvailable(getContext())) {
            new GeoJSONLayer(this).loadURL(URL);
        }
    }

    /**
     * Load and parse a GeoJSON file at a given URL
     *
     * @param geoJSON the GeoJSON string to parse
     */
    public void loadFromGeoJSONString(String geoJSON) throws JSONException {
        GeoJSON.parseString(geoJSON, MapView.this);
    }

    /**
     * Close the currently-displayed tooltip, if any.
     */
    private void closeCurrentTooltip() {
        if (currentTooltip != null) {
            if (mMapViewListener != null) {
                mMapViewListener.onHidemarker(this, currentTooltip.getBoundMarker());
            }
            currentTooltip.close();
            currentTooltip = null;
        }
    }

    /**
     * Sets the default itemized overlay.
     */
    private void setDefaultItemizedOverlay() {
        defaultMarkerOverlay = new ItemizedIconOverlay(getContext(), defaultMarkerList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectMarker(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        if (mMapViewListener != null) {
                            mMapViewListener.onLongPressMarker(MapView.this, item);
                        }
                        return true;
                    }
                }
        );
        this.getOverlays().add(defaultMarkerOverlay);
    }

    /**
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */
    public boolean singleTapUpHelper(final ILatLng p) {
        closeCurrentTooltip();
        onTap(p);
        return true;
    }

    /**
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */
    public boolean longPressHelper(final ILatLng p) {
        onLongPress(p);
        return false;
    }

    public void onLongPress(final ILatLng p) {
        if (mMapViewListener != null) {
            mMapViewListener.onLongPressMap(MapView.this, p);
        }
    }

    public void onTap(final ILatLng p) {
        if (mMapViewListener != null) {
            mMapViewListener.onTapMap(MapView.this, p);
        }
    }

    /**
     * Returns the map's controller
     */
    public MapController getController() {
        return this.mController;
    }

    /**
     * Returns the map's overlay
     */
    public TilesOverlay getMapOverlay() {
        return mMapOverlay;
    }

    /**
     * You can add/remove/reorder your Overlays using the List of {@link Overlay}. The first (index
     * 0) Overlay gets drawn first, the one with the highest as the last one.
     */
    public List<Overlay> getOverlays() {
        return this.getOverlayManager();
    }

    public OverlayManager getOverlayManager() {
        return mOverlayManager;
    }

    public MapTileLayerBase getTileProvider() {
        return mTileProvider;
    }

    /**
     * Returns the map's scroller
     */
    public Scroller getScroller() {
        return mScroller;
    }

    public Handler getTileRequestCompleteHandler() {
        return mTileRequestCompleteHandler;
    }

    public BoundingBox getBoundingBoxInternal() {
        if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
            return null;
        }
        final Rect screenRect = GeometryMath.viewPortRect(getProjection(), null);
        ILatLng neGeoPoint =
                Projection.pixelXYToLatLong(screenRect.right, screenRect.top, mZoomLevel);
        ILatLng swGeoPoint =
                Projection.pixelXYToLatLong(screenRect.left, screenRect.bottom, mZoomLevel);

        return new BoundingBox(neGeoPoint.getLatitude(), neGeoPoint.getLongitude(),
                swGeoPoint.getLatitude(), swGeoPoint.getLongitude());
    }

    /**
     * Returns the current bounding box of the map.
     */
    public BoundingBox getBoundingBox() {
        return getProjection().getBoundingBox();
    }

    /**
     * Get centerpoint of the phone as latitude and longitude.
     *
     * @return centerpoint
     */
    public LatLng getCenter() {
        BoundingBox box = getBoundingBox();
        return (box != null) ? box.getCenter() : null;
    }

    /**
     * Gets the current bounds of the screen in <I>screen coordinates</I>.
     */
    public Rect getScreenRect(final Rect reuse) {
        final Rect out = getIntrinsicScreenRect(reuse);
        if (this.getMapOrientation() % 180 != 0) {
            // Since the canvas is shifted by getWidth/2, we can just return our natural scrollX/Y
            // value since that is the same as the shifted center.
            int centerX = this.getScrollX();
            int centerY = this.getScrollY();
            GeometryMath.getBoundingBoxForRotatedRectangle(out, centerX, centerY,
                    this.getMapOrientation(), out);
        }
        return out;
    }

    public Rect getIntrinsicScreenRect(Rect reuse) {
        if (reuse == null) {
            reuse = new Rect();
        }
        final int width_2 = getMeasuredWidth() >> 1;
        final int height_2 = getMeasuredHeight() >> 1;
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        reuse.set(scrollX - width_2, scrollY - height_2, scrollX + width_2, scrollY + height_2);
        return reuse;
    }

    /**
     * Get a projection for converting between screen-pixel coordinates and latitude/longitude
     * coordinates. You should not hold on to this object for more than one draw, since the
     * projection of the map could change.
     *
     * @return The Projection of the map in its current state. You should not hold on to this object
     * for more than one draw, since the projection of the map could change.
     */
    public Projection getProjection() {
        if (mProjection == null) {
            mProjection = new Projection(this);
        }
        return mProjection;
    }

    /**
     * Set the centerpoint of the map view, given a latitude and
     * longitude position.
     *
     * @return the map view, for chaining
     */
    public MapView setCenter(final ILatLng aCenter) {
        getController().setCenter(aCenter);
        return this;
    }

    /**
     * Pan the map by a given number of pixels in the x and y dimensions.
     */
    public MapView panBy(int x, int y) {
        this.mController.panBy(x, y);
        return this;
    }

    public MapView setScale(float scale) {
        float zoomDelta;
        if (scale < 1) {
            zoomDelta = -2 * (1 - scale);
        } else {
            zoomDelta = scale - 1.0f;
        }
        float newZoom = mZoomLevel + zoomDelta;
        if (newZoom <= mMaximumZoomLevel && newZoom >= mMinimumZoomLevel) {
            mMultiTouchScale = scale;
            invalidate();
        }
        return this;
    }

    public float getScale() {
        return mMultiTouchScale;
    }

    /**
     * @param aZoomLevel the zoom level bound by the tile source
     * @return the map view, for chaining
     */
    public MapView setZoom(final float aZoomLevel) {
        return this.mController.setZoom(aZoomLevel);
    }

    protected MapView setZoomInternal(final float aZoomLevel) {
        final float minZoomLevel = getMinZoomLevel();
        final float maxZoomLevel = getMaxZoomLevel();

        final float newZoomLevel = Math.max(minZoomLevel, Math.min(maxZoomLevel, aZoomLevel));
        final float curZoomLevel = this.mZoomLevel;

        if (newZoomLevel != curZoomLevel) {
            this.mZoomLevel = newZoomLevel;
            mScroller.forceFinished(true);
            mIsFlinging = false;
            updateScrollableAreaLimit();
        }

        if (newZoomLevel > curZoomLevel) {
            // We are going from a lower-resolution plane to a higher-resolution plane, so we have
            // to do it the hard way.
            final int worldSize_current_2 = Projection.mapSize(curZoomLevel) >> 1;
            final int worldSize_new_2 = Projection.mapSize(newZoomLevel) >> 1;
            final ILatLng centerGeoPoint =
                    Projection.pixelXYToLatLong(getScrollX() + worldSize_current_2,
                            getScrollY() + worldSize_current_2, curZoomLevel);
            final PointF centerPoint = Projection.latLongToPixelXY(centerGeoPoint.getLatitude(),
                    centerGeoPoint.getLongitude(), newZoomLevel, null);
            scrollTo((int) centerPoint.x - worldSize_new_2, (int) centerPoint.y - worldSize_new_2);
        } else if (newZoomLevel < curZoomLevel) {
            // We are going from a higher-resolution plane to a lower-resolution plane, so we can do
            // it the easy way.
            scrollTo((int) (GeometryMath.rightShift(getScrollX(), curZoomLevel - newZoomLevel)),
                    (int) (GeometryMath.rightShift(getScrollY(), curZoomLevel - newZoomLevel)));
        }

        mProjection = new Projection(this);
        // snap for all snappables
        final Point snapPoint = new Point();
        if (this.getOverlayManager().onSnapToItem(getScrollX(), getScrollY(), snapPoint, this)) {
            scrollTo(snapPoint.x, snapPoint.y);
        }

        getMapOverlay().rescaleCache(newZoomLevel, curZoomLevel, mProjection);

        // do callback on listener
        if (newZoomLevel != curZoomLevel && mListeners.size() >0) {
            final ZoomEvent event = new ZoomEvent(this, newZoomLevel);
            for (MapListener listener : mListeners) {
            	listener.onZoom(event);
			}
        }

        // Allows any views fixed to a Location in the MapView to adjust
        this.requestLayout();
        return this;
    }

    /**
     * compute the minimum zoom necessary to show a BoundingBox
     *
     * @param boundingBox the box to compute the zoom for
     * @return the minimum zoom necessary to show the bounding box
     */
    private float minimumZoomForBoundingBox(final BoundingBox boundingBox) {
        final RectF rect = Projection.toMapPixels(boundingBox, TileLayerConstants.MAXIMUM_ZOOMLEVEL,
                mTempRect);
        final float requiredLatitudeZoom =
                TileLayerConstants.MAXIMUM_ZOOMLEVEL - (float) ((Math.log(
                        rect.height() / getMeasuredHeight()) / Math.log(2)));
        final float requiredLongitudeZoom =
                TileLayerConstants.MAXIMUM_ZOOMLEVEL - (float) ((Math.log(
                        rect.width() / getMeasuredWidth()) / Math.log(2)));
        return Math.max(requiredLatitudeZoom, requiredLongitudeZoom);
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as possible.
     * Must be called after display layout is complete, or screen dimensions are not known, and
     * will always zoom to center of zoom  level 0.
     * Suggestion: Check getScreenRect(null).getHeight() > 0
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox) {
        BoundingBox inter =
                (mScrollableAreaBoundingBox != null) ? mScrollableAreaBoundingBox.intersect(
                        boundingBox) : boundingBox;
        if (inter == null) {
            return this;
        }
        if (!mReadyToComputeProjection) {
            mBoundingBoxToZoomOn = inter;
            return this;
        }

        // Zoom to boundingBox center, at calculated maximum allowed zoom level
        getController().setZoom(minimumZoomForBoundingBox(inter));

        getController().setCenter(
                new LatLng(inter.getCenter().getLatitude(), inter.getCenter().getLongitude()));

        return this;
    }

    public float getClampedZoomLevel(float zoom) {
        final float minZoomLevel = getMinZoomLevel();
        final float maxZoomLevel = getMaxZoomLevel();

        return Math.max(minZoomLevel, Math.min(maxZoomLevel, zoom));
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @return the current ZoomLevel between 0 (equator) and 18/19(closest), depending on the tile
     * source chosen.
     */
    public float getZoomLevel() {
        return getZoomLevel(true);
    }

    private float getAnimatedZoom() {
        return Float.intBitsToFloat(mTargetZoomLevel.get());
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @param aPending if true and we're animating then return the zoom level that we're animating
     * towards, otherwise return the current zoom level
     * @return the zoom level
     */
    public float getZoomLevel(final boolean aPending) {
        if (aPending && isAnimating()) {
            return getAnimatedZoom();
        } else {
            return mZoomLevel;
        }
    }

    /**
     * Get the minimum allowed zoom level for the maps.
     */
    public float getMinZoomLevel() {
        return Math.max(mMinimumZoomLevel, 0);
    }

    /**
     * Get the maximum allowed zoom level for the maps.
     */
    public float getMaxZoomLevel() {
        return mMaximumZoomLevel;
    }

    /**
     * Set the minimum allowed zoom level, or pass null to use the minimum zoom level from the tile
     * provider.
     */
    public void setMinZoomLevel(float zoomLevel) {
        mRequestedMinimumZoomLevel = mMinimumZoomLevel = zoomLevel;
        updateMinZoomLevel();
    }

    /**
     * Set the maximum allowed zoom level, or pass null to use the maximum zoom level from the tile
     * provider.
     */
    public void setMaxZoomLevel(float zoomLevel) {
        mMaximumZoomLevel = zoomLevel;
    }

    /**
     * Determine whether the map is at its maximum zoom
     *
     * @return whether the map can zoom in
     */
    protected boolean canZoomIn() {
        final float maxZoomLevel = getMaxZoomLevel();
        if ((isAnimating() ? getAnimatedZoom() : mZoomLevel) >= maxZoomLevel) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether the map is at its minimum zoom
     *
     * @return whether the map can zoom out
     */
    protected boolean canZoomOut() {
        final float minZoomLevel = getMinZoomLevel();
        if ((isAnimating() ? getAnimatedZoom() : mZoomLevel) <= minZoomLevel) {
            return false;
        }
        return true;
    }

    /**
     * Zoom in by one zoom level.
     */
    public boolean zoomIn() {
        return getController().zoomIn();
    }

    public boolean zoomInFixing(final ILatLng point) {
        return getController().zoomInAbout(point);
    }

    /**
     * Zoom out by one zoom level.
     */
    public boolean zoomOut() {
        return getController().zoomOut();
    }

    public boolean zoomOutFixing(final ILatLng point) {
        return getController().zoomOutAbout(point);
    }

    /**
     * Set the rotation of the map, in degrees. A value of 0, meaning straight up, is default.
     *
     * @param degrees the angle of the map
     */
    public void setMapOrientation(float degrees) {
        this.mapOrientation = degrees % 360.0f;
        this.mProjection = null;
        this.invalidate();
    }

    /**
     * Gets the current angle of rotation of the map
     *
     * @return the current angle in degrees.
     */
    public float getMapOrientation() {
        return mapOrientation;
    }

    /**
     * Whether to use the network connection if it's available.
     */
    public boolean useDataConnection() {
        return mMapOverlay.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode if true use the network connection if it's available. if false don't use the
     * network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mMapOverlay.setUseDataConnection(aMode);
    }

    private void updateMinZoomLevel() {
        if (mScrollableAreaBoundingBox == null || !mReadyToComputeProjection) {
            return;
        }
        mMinimumZoomLevel = (float) Math.max(mRequestedMinimumZoomLevel,
                minimumZoomForBoundingBox(mScrollableAreaBoundingBox));
        if (mZoomLevel < mMinimumZoomLevel) {
            setZoom(mMinimumZoomLevel);
        }
    }

    /**
     * Everytime we update the zoom or the view size we must re compute the real scrollable area
     * limit in pixels
     */
    public void updateScrollableAreaLimit() {
        if (mScrollableAreaBoundingBox == null) {
            return;
        }
        if (mScrollableAreaLimit == null) {
            mScrollableAreaLimit = new RectF();
        }
        Projection.toMapPixels(mScrollableAreaBoundingBox, getZoomLevel(false),
                mScrollableAreaLimit);
    }

    /**
     * Set the map to limit it's scrollable view to the specified BoundingBox. Note this does not
     * limit zooming so it will be possible for the user to zoom to an area that is larger than the
     * limited area.
     *
     * @param boundingBox A lat/long bounding box to limit scrolling to, or null to remove any
     * scrolling
     * limitations
     */
    public void setScrollableAreaLimit(BoundingBox boundingBox) {

        mScrollableAreaBoundingBox = boundingBox;

        // Clear scrollable area limit if null passed.
        if (mScrollableAreaBoundingBox == null) {
            mMinimumZoomLevel = mRequestedMinimumZoomLevel;
            mScrollableAreaLimit = null;
        } else {
            updateScrollableAreaLimit();
            updateMinZoomLevel();
        }
    }

    /**
     * Returns if the map can go to a specified geo point
     */
    public boolean canGoTo(ILatLng point) {
        return (mScrollableAreaBoundingBox == null || mScrollableAreaBoundingBox.contains(point));
    }

    /**
     * Returns if the map can go to a specified point (in map coordinates)
     */
    public boolean canGoTo(final float x, final float y) {
        return (mScrollableAreaLimit == null || mScrollableAreaLimit.contains(x, y));
    }

    /**
     * Returns the map current scrollable bounding box
     */
    public BoundingBox getScrollableAreaBoundingBox() {
        return mScrollableAreaBoundingBox;
    }

    public void invalidateMapCoordinates(final Rect dirty) {
        mInvalidateRect.set(dirty);
        final int width_2 = this.getWidth() / 2;
        final int height_2 = this.getHeight() / 2;

        // Since the canvas is shifted by getWidth/2, we can just return our natural scrollX/Y value
        // since that is the same as the shifted center.
        int centerX = this.getScrollX();
        int centerY = this.getScrollY();

        if (this.getMapOrientation() != 0) {
            GeometryMath.getBoundingBoxForRotatedRectangle(mInvalidateRect, centerX, centerY,
                    this.getMapOrientation() + 180, mInvalidateRect);
        }
        mInvalidateRect.offset(width_2, height_2);

        super.invalidate(mInvalidateRect);
    }

    public void invalidateMapCoordinates(final RectF dirty) {
        dirty.roundOut(mInvalidateRect);
        final int width_2 = this.getWidth() / 2;
        final int height_2 = this.getHeight() / 2;

        // Since the canvas is shifted by getWidth/2, we can just return our natural scrollX/Y value
        // since that is the same as the shifted center.
        int centerX = this.getScrollX();
        int centerY = this.getScrollY();

        if (this.getMapOrientation() != 0) {
            GeometryMath.getBoundingBoxForRotatedRectangle(mInvalidateRect, centerX, centerY,
                    this.getMapOrientation() + 180, mInvalidateRect);
        }
        mInvalidateRect.offset(width_2, height_2);

        super.invalidate(mInvalidateRect);
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} at the {@link
     * com.mapbox.mapboxsdk.geometry.LatLng} (0, 0) align
     * with {@link MapView.LayoutParams#BOTTOM_CENTER}.
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER, 0,
                0);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new MapView.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        return p instanceof MapView.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        return new MapView.LayoutParams(p);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;

        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        final Projection projection = getProjection();
        // Find rightmost and bottom-most child
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final MapView.LayoutParams lp = (MapView.LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                final int childWidth = child.getMeasuredWidth();
                projection.toMapPixels(lp.geoPoint, mPoint);
                final int x = (int) mPoint.x + getWidth() / 2;
                final int y = (int) mPoint.y + getHeight() / 2;
                int childRight = x;
                int childBottom = y;
                switch (lp.alignment) {
                    case MapView.LayoutParams.TOP_LEFT:
                        childRight = x + childWidth;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.TOP_CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.TOP_RIGHT:
                        childRight = x;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.CENTER_LEFT:
                        childRight = x + childWidth;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER_RIGHT:
                        childRight = x;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.BOTTOM_LEFT:
                        childRight = x + childWidth;
                        childBottom = y + childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y + childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_RIGHT:
                        childRight = x;
                        childBottom = y + childHeight;
                        break;
                }
                childRight += lp.offsetX;
                childBottom += lp.offsetY;

                maxWidth = Math.max(maxWidth, childRight);
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            mProjection = null;
            if (!mReadyToComputeProjection) {
                mReadyToComputeProjection = true;
            }
            updateMinZoomLevel();

            if (mBoundingBoxToZoomOn != null) {
                zoomToBoundingBox(mBoundingBoxToZoomOn);
                mBoundingBoxToZoomOn = null;
            }
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {
        final int count = getChildCount();

        final Projection projection = getProjection();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final MapView.LayoutParams lp = (MapView.LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                final int childWidth = child.getMeasuredWidth();
                projection.toMapPixels(lp.geoPoint, mPoint);
                final int x = (int) mPoint.x + getWidth() / 2;
                final int y = (int) mPoint.y + getHeight() / 2;
                int childLeft = x;
                int childTop = y;
                switch (lp.alignment) {
                    case MapView.LayoutParams.TOP_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.CENTER_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.BOTTOM_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                }
                childLeft += lp.offsetX;
                childTop += lp.offsetY;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }

    public void onDetach() {
        this.getOverlayManager().onDetach(this);
        mTileProvider.detach();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyDown(keyCode, event, this);

        return result || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyUp(keyCode, event, this);

        return result || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {

        if (this.getOverlayManager().onTrackballEvent(event, this)) {
            return true;
        }

        scrollBy((int) (event.getX() * 25), (int) (event.getY() * 25));

        return super.onTrackballEvent(event);
    }

    private boolean canTapTwoFingers = false;
    private int multiTouchDownCount = 0;

    private boolean handleTwoFingersTap(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    multiTouchDownCount = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isAnimating() && canTapTwoFingers) {
                        final ILatLng center =
                                getProjection().fromPixels(event.getX(), event.getY());
                        mController.zoomOutAbout(center);
                        return true;
                    }
                    canTapTwoFingers = false;
                    multiTouchDownCount = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    multiTouchDownCount++;
                    canTapTwoFingers = multiTouchDownCount > 1;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    multiTouchDownCount--;
                    //                    canTapTwoFingers = multiTouchDownCount > 1;
                    break;
                default:
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get rotated event for some touch listeners.
        MotionEvent rotatedEvent = rotateTouchEvent(event);

        try {
            if (this.getOverlayManager().onTouchEvent(rotatedEvent, this)) {
                Log.d(TAG, "OverlayManager handled onTouchEvent");
                return true;
            }
            mScaleGestureDetector.onTouchEvent(rotatedEvent);
            // can't use the scale detector's onTouchEvent() result as it always returns true (Android issue #42591)
            boolean result = mScaleGestureDetector.isInProgress();
            if (!result) {
                result = mGestureDetector.onTouchEvent(rotatedEvent);
            } else {
                //needs to cancel two fingers tap
                canTapTwoFingers = false;
            }
            //handleTwoFingersTap should always be called because it counts pointers up/down
            result |= handleTwoFingersTap(rotatedEvent);

            return result;
        } finally {
            if (rotatedEvent != event) {
                rotatedEvent.recycle();
            }
        }
    }

    private MotionEvent rotateTouchEvent(MotionEvent ev) {
        if (this.getMapOrientation() == 0) {
            return ev;
        }
        MotionEvent rotatedEvent = MotionEvent.obtain(ev);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mRotatePoints[0] = ev.getX();
            mRotatePoints[1] = ev.getY();
            getProjection().rotatePoints(mRotatePoints);
            rotatedEvent.setLocation(mRotatePoints[0], mRotatePoints[1]);
        } else {
            // This method is preferred since it will rotate historical touch events too
            try {
                if (sMotionEventTransformMethod == null) {
                    sMotionEventTransformMethod = MotionEvent.class.getDeclaredMethod("transform",
                            new Class[] { Matrix.class });
                }
                sMotionEventTransformMethod.invoke(rotatedEvent,
                        getProjection().getRotationMatrix());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rotatedEvent;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.isFinished()) {
                // One last scrollTo to get to the final destination
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                // This will facilitate snapping-to any Snappable points.
                setZoom(mZoomLevel);
                mIsFlinging = false;
            } else {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate(); // Keep on drawing until the animation has
            // finished.
        }
    }

    public void updateScrollDuringAnimation() {
        // updateScrollableAreaLimit();
        // scrollTo(getScrollX(), getScrollY());
    }

    @Override
    public void scrollTo(int x, int y) {
        if (mScrollableAreaLimit != null) {
            final float width_2 = this.getMeasuredWidth() / 2;
            final float height_2 = this.getMeasuredHeight() / 2;
            // Adjust if we are outside the scrollable area
            if (mScrollableAreaLimit.width() <= width_2 * 2) {
                if (x - width_2 > mScrollableAreaLimit.left) {
                    x = (int) (mScrollableAreaLimit.left + width_2);
                } else if (x + width_2 < mScrollableAreaLimit.right) {
                    x = (int) (mScrollableAreaLimit.right - width_2);
                }
            } else if (x - width_2 < mScrollableAreaLimit.left) {
                x = (int) (mScrollableAreaLimit.left + width_2);
            } else if (x + width_2 > mScrollableAreaLimit.right) {
                x = (int) (mScrollableAreaLimit.right - width_2);
            }

            if (mScrollableAreaLimit.height() <= height_2 * 2) {
                if (y - height_2 > mScrollableAreaLimit.top) {
                    y = (int) (mScrollableAreaLimit.top + height_2);
                } else if (y + height_2 < mScrollableAreaLimit.bottom) {
                    y = (int) (mScrollableAreaLimit.bottom - height_2);
                }
            } else if (y - height_2 < mScrollableAreaLimit.top) {
                y = (int) (mScrollableAreaLimit.top + height_2);
            } else if (y + height_2 > mScrollableAreaLimit.bottom) {
                y = (int) (mScrollableAreaLimit.bottom - height_2);
            }
        }
        super.scrollTo(x, y);

        // do callback on listener
        if (mListeners.size() > 0) {
            final ScrollEvent event = new ScrollEvent(this, x, y);
            for (MapListener listener : mListeners) {
            	listener.onScroll(event);
    		}
        }
    }

    @Override
    public void setBackgroundColor(final int pColor) {
        mMapOverlay.setLoadingBackgroundColor(pColor);
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas c) {
        super.onDraw(c);
        mProjection = new Projection(this);

        // Save the current canvas matrix
        c.save();

        c.translate(getWidth() / 2, getHeight() / 2);
        c.scale(mMultiTouchScale, mMultiTouchScale, mMultiTouchScalePoint.x,
                mMultiTouchScalePoint.y);

        // rotate Canvas
        c.rotate(mapOrientation, mProjection.getScreenRect().exactCenterX(),
                mProjection.getScreenRect().exactCenterY());

        // Draw all Overlays.
        this.getOverlayManager().onDraw(c, this);

        c.restore();
    }

    /**
     * Returns true if the safe drawing canvas is being used.
     *
     * @see {@link com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas}
     */
    public boolean isUsingSafeCanvas() {
        return this.getOverlayManager().isUsingSafeCanvas();
    }

    /**
     * Sets whether the safe drawing canvas is being used.
     *
     * @see {@link com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas}
     */
    public void setUseSafeCanvas(boolean useSafeCanvas) {
        this.getOverlayManager().setUseSafeCanvas(useSafeCanvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        this.onDetach();
        super.onDetachedFromWindow();
    }

    /**
     * Determines if maps are animating a zoom operation. Useful for overlays to avoid
     * recalculating
     * during an animation sequence.
     *
     * @return boolean indicating whether view is animating.
     */
    public boolean isAnimating() {
        return mIsAnimating.get();
    }

    public TileLoadedListener getTileLoadedListener() {
        return tileLoadedListener;
    }

    /**
     * Per-child layout information associated with OpenStreetMapView.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams implements MapViewLayouts {
        /**
         * The location of the child within the map view.
         */
        public ILatLng geoPoint;

        /**
         * The alignment the alignment of the view compared to the location.
         */
        public int alignment;

        public int offsetX;
        public int offsetY;

        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         *
         * @param width the width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed
         * size
         * in pixels
         * @param height the height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed
         * size
         * in pixels
         * @param aGeoPoint the location of the child within the map view
         * @param aAlignment the alignment of the view compared to the location {@link
         * #BOTTOM_CENTER},
         * {@link #BOTTOM_LEFT}, {@link #BOTTOM_RIGHT} {@link #TOP_CENTER},
         * {@link #TOP_LEFT}, {@link #TOP_RIGHT}
         * @param aOffsetX the additional X offset from the alignment location to draw the child
         * within
         * the map view
         * @param aOffsetY the additional Y offset from the alignment location to draw the child
         * within
         * the map view
         */
        public LayoutParams(final int width, final int height, final ILatLng aGeoPoint,
                final int aAlignment, final int aOffsetX, final int aOffsetY) {
            super(width, height);
            if (aGeoPoint != null) {
                this.geoPoint = aGeoPoint;
            } else {
                this.geoPoint = new LatLng(0, 0);
            }
            this.alignment = aAlignment;
            this.offsetX = aOffsetX;
            this.offsetY = aOffsetY;
        }

        /**
         * Since we cannot use XML files in this project this constructor is useless. Creates a new
         * set of layout parameters. The values are extracted from the supplied attributes set and
         * context.
         *
         * @param c the application environment
         * @param attrs the set of attributes fom which to extract the layout parameters values
         */
        public LayoutParams(final Context c, final AttributeSet attrs) {
            super(c, attrs);
            this.geoPoint = new LatLng(0, 0);
            this.alignment = BOTTOM_CENTER;
        }

        public LayoutParams(final ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public void setMapViewListener(MapViewListener listener) {
        this.mMapViewListener = listener;
    }

    public void setOnTileLoadedListener(TileLoadedListener aTileLoadedListener) {
        this.tileLoadedListener = aTileLoadedListener;
    }

    public void setOnTilesLoadedListener(TilesLoadedListener aTilesLoadedListener) {
        this.tilesLoadedListener = aTilesLoadedListener;
    }

    public TilesLoadedListener getTilesLoadedListener() {
        return tilesLoadedListener;
    }

    @Override
    public String toString() {
        return "MapView {" + getTileProvider() + "}";
    }

    public void setDefaultPinRes(int res) {
        mDefaultPinRes = res;
    }

    public void setDefaultPinDrawable(Drawable drawable) {
        mDefaultPinDrawable = drawable;
    }

    public Drawable getDefaultPinDrawable() {
        if (mDefaultPinDrawable == null && mDefaultPinRes != 0) {
            BitmapFactory.Options opts =
                    BitmapUtils.getBitmapOptions(getResources().getDisplayMetrics());
            mDefaultPinDrawable = new BitmapDrawable(getResources(),
                    BitmapFactory.decodeResource(context.getResources(), mDefaultPinRes, opts));
        }
        return mDefaultPinDrawable;
    }

    public void setDefaultPinAnchor(PointF point) {
        mDefaultPinAnchor = point;
    }

    public PointF getDefaultPinAnchor() {
        return mDefaultPinAnchor;
    }
}
