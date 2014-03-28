package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;

/**
 * View that can be displayed on an OSMDroid map, associated to a GeoPoint.
 * Typical usage: cartoon-like bubbles displayed when clicking an overlay item.
 * It mimics the InfoWindow class of Google Maps JavaScript API V3.
 * Main differences are:
 * <ul>
 * <li>Structure and content of the view is let to the responsibility of the caller. </li>
 * <li>The same InfoWindow can be associated to many items. </li>
 * </ul>
 * Known issue: the window is displayed "above" the marker, so the queue of the bubble can hide the marker.
 * <p/>
 * This is an abstract class.
 *
 * @author M.Kergall
 */

public class InfoWindow {
    private Marker boundMarker;

    /**
     * @param layoutResId   the id of the view resource.
     * @param mapView       the mapview on which is hooked the view
     */

    private MapView mMapView;
    private boolean mIsVisible;
    private View mView;

    static int mTitleId = 0,
            mDescriptionId = 0,
            mSubDescriptionId = 0,
            mImageId = 0;

    private static void setResIds(Context context) {
        String packageName = context.getPackageName(); //get application package name
        mTitleId = context.getResources().getIdentifier("id/tooltip_title", null, packageName);
        mDescriptionId = context.getResources().getIdentifier("id/tooltip_description", null, packageName);
        mSubDescriptionId = context.getResources().getIdentifier("id/tooltip_subdescription", null, packageName);
        mImageId = context.getResources().getIdentifier("id/tooltip_image", null, packageName);
    }

    public InfoWindow(int layoutResId, MapView mapView) {
        mMapView = mapView;
        mIsVisible = false;
        ViewGroup parent = (ViewGroup) mapView.getParent();
        Context context = mapView.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(layoutResId, parent, false);

        if (mTitleId == 0) {
            setResIds(mapView.getContext());
        }

        // default behavior: close it when clicking on the tooltip:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    close();
                }
                return true; //From Osmdroid 3.0.10, event is properly consumed.
            }
        });
    }

    public InfoWindow(View view, MapView mapView) {
        mMapView = mapView;
        mIsVisible = false;
        ViewGroup parent = (ViewGroup) mapView.getParent();
        Context context = mapView.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = view;

        // default behavior: close it when clicking on the tooltip:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    close();
                }
                return true; //From Osmdroid 3.0.10, event is properly consumed.
            }
        });
    }

    /**
     * open the window at the specified position.
     *
     * @param object   the graphical object on which is hooked the view
     * @param position to place the window on the map
     * @param offsetX  (&offsetY) the offset of the view to the position, in pixels.
     *                 This allows to offset the view from the object position.
     */
    public void open(Object object, LatLng position, int offsetX, int offsetY) {
        onOpen(object);
        MapView.LayoutParams lp = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT,
                position, MapView.LayoutParams.BOTTOM_CENTER,
                offsetX, offsetY);
        close(); //if it was already opened
        mMapView.addView(mView, lp);
        mIsVisible = true;
    }

    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();
        }
    }

    /**
     * Returns the Android view. This allows to set its content.
     *
     * @return the Android view
     */
    public View getView() {
        return (mView);
    }

    public MapView getMapView() {
        return (mMapView);
    }

    public void onOpen(Object item) {
        Marker overlayItem = (Marker) item;
        String title = overlayItem.getTitle();
        if (title == null) {
            title = "";
        }
        ((TextView) mView.findViewById(mTitleId /*R.id.title*/)).setText(title);
        String snippet = overlayItem.getDescription();
        if (snippet == null) {
            snippet = "";
        }
        ((TextView) mView.findViewById(mDescriptionId /*R.id.description*/)).setText(snippet);
        //handle sub-description, hidding or showing the text view:
        TextView subDescText = (TextView) mView.findViewById(mSubDescriptionId);
        String subDesc = overlayItem.getSubDescription();
        if (subDesc != null && !("".equals(subDesc))) {
            subDescText.setText(subDesc);
            subDescText.setVisibility(View.VISIBLE);
        } else {
            subDescText.setVisibility(View.GONE);
        }
    }

    public void onClose() {
        //by default, do nothing
    }

    public void setBoundMarker(Marker boundMarker) {
        this.boundMarker = boundMarker;
    }

    public Marker getBoundMarker() {
        return boundMarker;
    }
}
 
