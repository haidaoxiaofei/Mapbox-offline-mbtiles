package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class OfflineCacheTestFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offlinecache, container, false);

        // Setup Map
        MapView mapView = (MapView) view.findViewById(R.id.offlineCacheMapView);
        mapView.setDiskCacheEnabled(true);
        mapView.setCenter(new LatLng(46.85268, -121.75907));
        mapView.setZoom(10);

        return view;
    }

}
