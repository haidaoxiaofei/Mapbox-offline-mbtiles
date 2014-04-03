package com.mapbox.mapboxsdk.overlay;

import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by Francisco Dans on 06/03/14.
 */
public class ClusterItem extends Marker {
    private int childCount = 0;
    private static Drawable mDrawable;

    public ClusterItem(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(mv, aTitle, aDescription, aLatLng);
        setHotspot(HotspotPlace.CENTER);
        if (mv != null) {
            if (mDrawable == null) {
                mDrawable = mv.getResources().getDrawable(R.drawable.clusteri);
            }
            setMarker(mDrawable);
        }

    }

    @Override
    public int getHeight() {
        //we are not a marker image make sure we report the correct height
        return this.mMarker.getIntrinsicHeight();
    }

//    public ClusterItem(String aTitle, String aSnippet, LatLng aLatLng) {
//        this(null, aTitle, aSnippet, aLatLng);
//    }

    public ClusterItem(MapView view, LatLng result) {
        this(view, "", "", result);
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}
