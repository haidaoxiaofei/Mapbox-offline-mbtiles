// Created by plusminus on 00:02:58 - 03.10.2008
package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.util.Log;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Immutable class describing a LatLng with a Title and a Description.
 *
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public class Marker {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;

    protected static final Point DEFAULT_MARKER_SIZE = new Point(26, 94);
    private int group = 0;
    private boolean clustered;


    public int getGroup() {
        return group;
    }

    public void assignGroup(int currentGroup) {
        if (currentGroup == 0) {
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

    private String mTitle, mDescription; // now, they are modifiable
    private String mSubDescription; //a third field that can be displayed in the infowindow, on a third line
    private Drawable mImage; //that will be shown in the infowindow.
    //private GeoPoint mGeoPoint //unfortunately, this is not so simple...
    private Object mRelatedObject; //reference to an object (of any kind) linked to this item.
    private boolean bubbleShowing;

    // ===========================================================
    // Constructors
    // ===========================================================

    public Marker(final String aUid, final String aTitle, final String aDescription,
                  final LatLng aLatLng) {
        this.mTitle = aTitle;
        this.mSnippet = aDescription;
        this.mLatLng = aLatLng;
        this.mUid = aUid;
    }

    public Marker(String title, String s, LatLng aLatLng) {
        this((MapView) null, title, s, aLatLng);
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
            this.setMarker(context.getResources().getDrawable(R.drawable.defpin));
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
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

//        mapView.invalidateMapCoordinates(marker.getBounds());

/*
        // Determine bounding box of drawable
        Point point = mapView.getProjection().toMapPixels(latLng, null);
        int widthBuffer = getWidth() / 2;
        int heightBuffer = getHeight() / 2;

        // l, t, r, b
        Rect rect = new Rect(point.x - widthBuffer, point.y + heightBuffer, point.x + widthBuffer, point.y - heightBuffer);

        mapView.invalidateMapCoordinates(rect);
*/

        mapView.invalidate();

    }

    public void setMarkerHotspot(final HotspotPlace place) {
        this.mHotspotPlace = (place == null) ? HotspotPlace.BOTTOM_CENTER : place;
    }

    public HotspotPlace getMarkerHotspot() {
        return this.mHotspotPlace;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
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
        return this.mMarker.getIntrinsicHeight();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


    /**
     * From a HotspotPlace and drawable dimensions (width, height), return the hotspot position.
     * Could be a public method of HotspotPlace or OverlayItem...
     */
    public Point getHotspot(HotspotPlace place, int w, int h) {
        Point hp = new Point();
        if (place == null) {
            place = HotspotPlace.BOTTOM_CENTER; //use same default than in osmdroid.
        }
        switch (place) {
            case NONE:
                hp.set(0, 0);
                break;
            case BOTTOM_CENTER:
                hp.set(w / 2, 0);
                break;
            case LOWER_LEFT_CORNER:
                hp.set(0, 0);
                break;
            case LOWER_RIGHT_CORNER:
                hp.set(w, 0);
                break;
            case CENTER:
                hp.set(w / 2, -h / 2);
                break;
            case LEFT_CENTER:
                hp.set(0, -h / 2);
                break;
            case RIGHT_CENTER:
                hp.set(w, -h / 2);
                break;
            case TOP_CENTER:
                hp.set(w / 2, -h);
                break;
            case UPPER_LEFT_CORNER:
                hp.set(0, -h);
                break;
            case UPPER_RIGHT_CORNER:
                hp.set(w, -h);
                break;
        }
        return hp;
    }

    /**
     * Populates this tooltip with all item info:
     * <ul>title and description in any case, </ul>
     * <ul>image and sub-description if any.</ul>
     * and centers the map view on the item if panIntoView is true. <br>
     */
    public void showBubble(InfoWindow tooltip, MapView aMapView, boolean panIntoView) {
        //offset the tooltip to be top-centered on the marker:
        Drawable marker = getMarker(0 /*OverlayItem.ITEM_STATE_FOCUSED_MASK*/);
        int markerWidth = 0, markerHeight = 0;
        if (marker != null) {
            markerWidth = marker.getIntrinsicWidth();
            markerHeight = marker.getIntrinsicHeight();
        } //else... we don't have the default marker size => don't user default markers!!!
        Point markerH = getHotspot(getMarkerHotspot(), markerWidth, markerHeight);
        Point tooltipH = getHotspot(HotspotPlace.TOP_CENTER, markerWidth, markerHeight);
        tooltipH.offset(-markerH.x, -markerH.y);
        tooltip.open(this, this.getPoint(), tooltipH.x, tooltipH.y);
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
            // Set default icon
            setIcon(new Icon(mv.getResources(), Icon.Size.LARGE, "", "000"));
        }
        return this;
    }

    public Marker setIcon(Icon aIcon) {
        this.icon = aIcon;
        this.icon.setMarker(this);
        this.setMarkerHotspot(HotspotPlace.CENTER);
        return this;
    }
}
