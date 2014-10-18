package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

public class ProgrammaticTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programmatic, container, false);

        // Setup Map
        MapView mapView = new MapView(getActivity());
        mapView.setTileSource(new MapboxTileLayer(getString(R.string.mapbox_id_terrain)));
        mapView.setCenter(new LatLng(49.31376, -123.14000));
        mapView.setZoom(8);

        FrameLayout layout = (FrameLayout) view.findViewById(R.id.programmaticFrameLayout);
        layout.addView(mapView);

        return view;
    }
}
