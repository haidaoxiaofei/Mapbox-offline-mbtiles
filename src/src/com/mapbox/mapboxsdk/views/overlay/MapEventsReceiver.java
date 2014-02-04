package com.mapbox.mapboxsdk.views.overlay;

import com.mapbox.mapboxsdk.api.ILatLng;

/**
 * Interface for objects that need to handle map events thrown by a MapEventsOverlay. 
 * @see com.mapbox.mapboxsdk.views.overlay.MapEventsOverlay
 * @author M.Kergall
 */
public interface MapEventsReceiver {

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    boolean singleTapUpHelper(ILatLng p);

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    boolean longPressHelper(ILatLng p);
}