package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * A Marker object is a visible representation of a point on a Map that has a geographical place.
 */
public class Marker extends ExtendedOverlayItem {
    private Context context;
    private Tooltip tooltip;
    private MapView mapView;

    /**
     * Initialize a new marker object, adding it to a MapView and attaching a tooltip
     * @param mv a mapview
     * @param aTitle the title of the marker, in a potential tooltip
     * @param aDescription the description of the marker, in a tooltip
     * @param aLatLng the location of the marker
     */
    public Marker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(aTitle, aDescription, aLatLng);
        if (mv != null) {
            context = mv.getContext();
            mapView = mv;
            fromMaki("markerstroked");
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
        fromMaki("markerstroked");
        return this;
    }

    private void attachTooltip() {
        tooltip = new Tooltip(context, this, this.getTitle());
        mapView.getOverlays().add(tooltip);
        mapView.invalidate();
    }

    /**
     * Set this marker's icon to a marker from the Maki icon set.
     *
     * @param makiString the name of a Maki icon symbol
     */
    public void fromMaki(String makiString) {
        String urlString = makiString+"182x";
        int id = context.getResources().getIdentifier(urlString, "drawable", context.getPackageName());
        this.setMarker(context.getResources().getDrawable(id));
    }

    public Marker setIcon(Icon icon) {
        icon.setMarker(this);
        return this;
    }

    public void setTooltipVisible() {
        //tooltip.setVisible(true);
        //mapView.invalidate();
    }

    public void setTooltipInvisible() {
        tooltip.setVisible(false);
    }
}
