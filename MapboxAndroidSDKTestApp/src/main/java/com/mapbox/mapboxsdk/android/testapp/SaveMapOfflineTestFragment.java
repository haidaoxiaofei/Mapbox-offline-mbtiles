package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class SaveMapOfflineTestFragment extends Fragment {

    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savemapoffline, container, false);

        // Setup Map
        mapView = (MapView) view.findViewById(R.id.saveMapOfflineMapView);
        mapView.setCenter(new LatLng(29.94423, -90.09201));
        mapView.setZoom(12);

        return view;
    }
}
