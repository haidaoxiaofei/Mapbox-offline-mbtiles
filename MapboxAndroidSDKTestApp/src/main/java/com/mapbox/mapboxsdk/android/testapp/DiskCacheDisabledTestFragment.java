package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class DiskCacheDisabledTestFragment extends Fragment
{
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diskcachedisabled, container, false);

        // Setup Map
        mapView = (MapView) view.findViewById(R.id.diskCacheDisableMapView);
        mapView.setCenter(new LatLng(-22.95903, -43.17970));
        mapView.setZoom(14);

        return view;
    }
}
