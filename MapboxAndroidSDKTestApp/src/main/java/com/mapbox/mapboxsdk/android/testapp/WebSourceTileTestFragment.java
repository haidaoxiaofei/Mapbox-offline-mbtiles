package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

public class WebSourceTileTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_websourcetile, container, false);

        // Setup Map
        MapView mapView = new MapView(getActivity());

        WebSourceTileLayer ws = new WebSourceTileLayer("openstreetmap", "http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        ws.setName("OpenStreetMap")
            .setAttribution("© OpenStreetMap Contributors")
            .setMinimumZoomLevel(1)
            .setMaximumZoomLevel(18);

        mapView.setTileSource(ws);
        mapView.setCenter(new LatLng(34.19997, -118.17163));
        mapView.setZoom(12);

        FrameLayout layout = (FrameLayout) view.findViewById(R.id.webSourceTileFrameLayout);
        layout.addView(mapView);

        return view;
    }
}
