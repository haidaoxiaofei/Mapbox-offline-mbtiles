package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * A Marker object is a visible representation of a point on a Map that has a geographical place.
 */
public class Marker extends ExtendedOverlayItem {
    private Context context;
    private Tooltip tooltip;
    private MapView mapView;
    private LatLng latLng;
    private Icon icon;

    /**
     * Initialize a new marker object, adding it to a MapView and attaching a tooltip
     * @param mv a mapview
     * @param aTitle the title of the marker, in a potential tooltip
     * @param aDescription the description of the marker, in a tooltip
     * @param aLatLng the location of the marker
     */
    public Marker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(aTitle, aDescription, aLatLng);
        this.latLng = aLatLng;
        if (mv != null) {
            context = mv.getContext();
            mapView = mv;
            setIcon(new Icon(mv.getResources(), Icon.Size.LARGE, "", "000"));
//            attachTooltip();
        }
    }

    /**
     * Initialize a new marker object
     *
     * @param aTitle the title of the marker, in a potential tooltip
     * @param aDescription the description of the marker, in a tooltip
     * @param aLatLng the location of the marker
     */
    public Marker(String aTitle, String aDescription, LatLng aLatLng) {
        this(null, aTitle, aDescription, aLatLng);
    }

    public Marker addTo(MapView mv) {
        mapView = mv;
        context = mv.getContext();
        if (icon == null)
        {
            // Set default icon
            setIcon(new Icon(mv.getResources(), Icon.Size.LARGE, "", "000"));
        }
        return this;
    }

    /**
     * Sets Marker, then redraws map to display it.
     * @param marker Marker
     */
    @Override
    public void setMarker(Drawable marker)
    {
        super.setMarker(marker);

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

    private void attachTooltip() {
        tooltip = new Tooltip(context, this, this.getTitle(), this.getDescription());
        mapView.getOverlays().add(tooltip);
        mapView.invalidate();
    }

    public Marker setIcon(Icon icon) {
        this.icon = icon;
        icon.setMarker(this);
        this.setMarkerHotspot(HotspotPlace.CENTER);
        return this;
    }

    public void setTooltipVisible() {
        tooltip.setVisible(true);
    }

    public void setTooltipInvisible() {
        tooltip.setVisible(false);
    }
}
