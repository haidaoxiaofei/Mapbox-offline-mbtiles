// Created by plusminus on 00:02:58 - 03.10.2008
package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * Immutable class describing a LatLng with a Title and a Description.
 *
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public class Marker {
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;

    private int group = 0;
    private boolean clustered;

    private final RectF mMyLocationRect = new RectF(0, 0, 0, 0);
    private final RectF mMyLocationPreviousRect = new RectF(0, 0, 0, 0);

    public int getGroup() {
        return group;
    }

    public void assignGroup(int currentGroup) {
        if (currentGroup == -1) {
            this.setClustered(false);
        }
        group = currentGroup;
    }

    public boolean beingClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }


    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.tootip, mv);
    }

    private InfoWindow mToolTip;

    public InfoWindow getToolTip(MapView mv) {
        if (mToolTip == null || mToolTip.getMapView() != mv) {
            mToolTip = createTooltip(mv);
        }
        return mToolTip;
    }

    public void blur() {
        if (mParentHolder != null) {
            mParentHolder.blurItem(this);
        }
    }


    /**
     * Indicates a hotspot for an area. This is where the origin (0,0) of a point will be located
     * relative to the area. In otherwords this acts as an offset. NONE indicates that no adjustment
     * should be made.
     */
    public enum HotspotPlace {
        NONE, CENTER, BOTTOM_CENTER, TOP_CENTER, RIGHT_CENTER, LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER, UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
    }

    // ===========================================================
    // Fields
    // ===========================================================

    protected String mUid;
    protected String mSnippet;
    protected LatLng mLatLng;
    protected Drawable mMarker;
    protected HotspotPlace mHotspotPlace;
    protected Point mAnchor = null;

    private String mTitle, mDescription; // now, they are modifiable
    private String mSubDescription; //a third field that can be displayed in the infowindow, on a third line
    private Drawable mImage; //that will be shown in the infowindow.
    //private GeoPoint mGeoPoint //unfortunately, this is not so simple...
    private Object mRelatedObject; //reference to an object (of any kind) linked to this item.
    private boolean bubbleShowing;
    private ItemizedOverlay mParentHolder;

    static Drawable defaultPinDrawable;

    public Marker(String title, String description, LatLng latLng) {
        this(null, title, description, latLng);
    }

    /**
     * Initialize a new marker object, adding it to a MapView and attaching a tooltip
     *
     * @param mv           a mapview
     * @param aTitle       the title of the marker, in a potential tooltip
     * @param aDescription the description of the marker, in a tooltip
     * @param aLatLng      the location of the marker
     */
    public Marker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        this.setTitle(aTitle);
        this.setDescription(aDescription);
        this.mLatLng = aLatLng;
        Log.d(getClass().getCanonicalName(), "markerconst" + mv + aTitle + aDescription + aLatLng);
        if (mv != null) {
            context = mv.getContext();
            mapView = mv;
            if (defaultPinDrawable == null) {
                BitmapFactory.Options opts = BitmapUtils.getBitmapOptions(context.getResources().getDisplayMetrics());
                defaultPinDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.defpin, opts));
            }
        }
        this.setMarker(defaultPinDrawable);
        mHotspotPlace = HotspotPlace.BOTTOM_CENTER;
        mParentHolder = null;
    }

    public String getUid() {
        return mUid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public LatLng getPoint() {
        return mLatLng;
    }

    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public void setDescription(String aDescription) {
        mDescription = aDescription;
    }

    public void setSubDescription(String aSubDescription) {
        mSubDescription = aSubDescription;
    }

    public void setImage(Drawable anImage) {
        mImage = anImage;
    }

    public void setRelatedObject(Object o) {
        mRelatedObject = o;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getSubDescription() {
        return mSubDescription;
    }

    public Drawable getImage() {
        return mImage;
    }

    public Object getRelatedObject() {
        return mRelatedObject;
    }

    public ItemizedOverlay getParentHolder() {
        return mParentHolder;
    }

    public void setParentHolder(ItemizedOverlay o) {
        mParentHolder = o;
    }

    /*
     * (copied from Google API docs) Returns the marker that should be used when drawing this item
     * on the map. A null value means that the default marker should be drawn. Different markers can
     * be returned for different states. The different markers can have different bounds. The
     * default behavior is to call {@link setState(android.graphics.drawable.Drawable, int)} on the
     * overlay item's marker, if it exists, and then return it.
     *
     * @param stateBitset The current state.
     *
     * @return The marker for the current state, or null if the default marker for the overlay
     * should be used.
     */
    public Drawable getMarker(final int stateBitset) {
        // marker not specified
        if (mMarker == null) {
            return null;
        }

        // set marker state appropriately
        setState(mMarker, stateBitset);
        return mMarker;
    }

    public void setMarker(final Drawable marker) {
        this.mMarker = marker;
        if (marker != null) {
            marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
        }
        invalidate();
    }

    public void setMarkerHotspot(final HotspotPlace place) {
        this.mHotspotPlace = (place == null) ? HotspotPlace.BOTTOM_CENTER : place;
        invalidate();
    }

    public HotspotPlace getMarkerHotspot() {
        return this.mHotspotPlace;
    }

    public Point getMarkerAnchor() {
        if (mAnchor != null) {
            int markerWidth = getWidth(), markerHeight = getHeight();
            return new Point(mAnchor.x * markerWidth, mAnchor.y * markerHeight);
        }
        return getMarkerAnchor(getMarkerHotspot());
    }

    public Point getMarkerAnchor(HotspotPlace place) {
        int markerWidth = getWidth(), markerHeight = getHeight();
        return getHotspot(place, markerWidth, markerHeight);
    }

    public void setMarkerAnchorPoint(final Point anchor) {
        this.mAnchor = anchor;
    }

    public static void setState(final Drawable drawable, final int stateBitset) {
        final int[] states = new int[3];
        int index = 0;
        if ((stateBitset & ITEM_STATE_PRESSED_MASK) > 0) {
            states[index++] = android.R.attr.state_pressed;
        }
        if ((stateBitset & ITEM_STATE_SELECTED_MASK) > 0) {
            states[index++] = android.R.attr.state_selected;
        }
        if ((stateBitset & ITEM_STATE_FOCUSED_MASK) > 0) {
            states[index++] = android.R.attr.state_focused;
        }

        drawable.setState(states);
    }

    public Drawable getDrawable() {
        return this.mMarker;
    }

    public int getWidth() {
        return this.mMarker.getIntrinsicWidth();
    }

    public int getHeight() {
        return this.mMarker.getIntrinsicHeight() / 2;
    }

    public PointF getPositionOnScreen(final Projection projection, PointF reuse) {
        if (reuse == null) {
            reuse = new PointF();
        }
        projection.toPixels(mLatLng, reuse);
        return reuse;
    }

    public PointF getDrawingPositionOnScreen(final Projection projection, PointF reuse) {
        reuse = getPositionOnScreen(projection, reuse);
        Point point = getMarkerAnchor();
        reuse.offset(point.x, point.y);
        return reuse;
    }

    protected RectF getDrawingBounds(final Projection projection, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }
        final PointF scale = getHotspotScale(getMarkerHotspot());
        final PointF position = getPositionOnScreen(projection, null);
        final int w = getWidth();
        final int h = getHeight();
        final float x = position.x - scale.x * w;
        final float y = position.y - scale.y * h;
        reuse.set(x, y, x + w, y + h);
        return reuse;
    }

    protected RectF getMapDrawingBounds(final Projection projection, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }
        final PointF scale = getHotspotScale(getMarkerHotspot());
        final PointF position = projection.toMapPixels(mLatLng, null);
        final int w = getWidth();
        final int h = getHeight();
        final float x = position.x - scale.x * w;
        final float y = position.y - scale.y * h;
        reuse.set(x, y, x + w, y + h);
        return reuse;
    }

    public PointF getHotspotScale(HotspotPlace place) {
        PointF hp = new PointF(0, 0);
        if (place == null) {
            place = HotspotPlace.BOTTOM_CENTER; //use same default than in osmdroid.
        }
        switch (place) {
            case NONE:
            case UPPER_LEFT_CORNER:
                break;
            case BOTTOM_CENTER:
                hp.set(0.5f, 1f);
                break;
            case LOWER_LEFT_CORNER:
                hp.set(0, 1);
                break;
            case LOWER_RIGHT_CORNER:
                hp.set(1, 1);
                break;
            case CENTER:
                hp.set(0.5f, 0.5f);
                break;
            case LEFT_CENTER:
                hp.set(0, 0.5f);
                break;
            case RIGHT_CENTER:
                hp.set(1, 0.5f);
                break;
            case TOP_CENTER:
                hp.set(0.5f, 0);
                break;
            case UPPER_RIGHT_CORNER:
                hp.set(1, 0);
                break;
        }
        return hp;
    }

    /**
     * From a HotspotPlace and drawable dimensions (width, height), return the hotspot position.
     * Could be a public method of HotspotPlace or OverlayItem...
     */
    public Point getHotspot(HotspotPlace place, int w, int h) {
        PointF scale = getHotspotScale(place);
        return new Point((int) (-w * scale.x), (int) (-h * scale.y));
    }

    /**
     * Populates this tooltip with all item info:
     * <ul>title and description in any case, </ul>
     * <ul>image and sub-description if any.</ul>
     * and centers the map view on the item if panIntoView is true. <br>
     */
    public void showBubble(InfoWindow tooltip, MapView aMapView, boolean panIntoView) {
        //offset the tooltip to be top-centered on the marker:
//        Drawable marker = getMarker(0 /*OverlayItem.ITEM_STATE_FOCUSED_MASK*/);
        Point markerH = getMarkerAnchor();
        Point tooltipH = getMarkerAnchor(HotspotPlace.TOP_CENTER);
        markerH.offset(-tooltipH.x, tooltipH.y);
        tooltip.open(this, this.getPoint(), markerH.x, markerH.y);
        if (panIntoView) {
            aMapView.getController().animateTo(getPoint());
        }

        bubbleShowing = true;
        tooltip.setBoundMarker(this);
    }

    private Context context;
    private Tooltip tooltip;
    private MapView mapView;
    private LatLng latLng;
    private Icon icon;


    public Marker addTo(MapView mv) {
        mapView = mv;
        context = mv.getContext();
        if (icon == null) {
            setIcon(new Icon(mv.getContext(), Icon.Size.LARGE, "", "000"));
        }
        return this;
    }

    public Marker setIcon(Icon icon) {
        this.icon = icon;
        icon.setMarker(this);
        return this;
    }

    private void updateDrawingPositionRect() {
        getMapDrawingBounds(mapView.getProjection(), mMyLocationRect);
    }

    private void invalidate() {
        if (mapView == null) {
            return; //not on map yet
        }
        // Get new drawing bounds
        mMyLocationPreviousRect.set(mMyLocationRect);
        updateDrawingPositionRect();
        final RectF newRect = new RectF(mMyLocationRect);
        // If we had a previous location, merge in those bounds too
        newRect.union(mMyLocationPreviousRect);
        // Invalidate the bounds
        mapView.post(new Runnable() {
            @Override
            public void run() {
                mapView.invalidateMapCoordinates(newRect);
            }
        });
    }
}
