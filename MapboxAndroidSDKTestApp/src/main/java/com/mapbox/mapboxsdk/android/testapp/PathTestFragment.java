package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

public class PathTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_path, container, false);

        // Setup Map
        MapView mapView = (MapView) view.findViewById(R.id.pathMapView);
        mapView.setCenter(new LatLng(44.84029, -89.78027));
        mapView.setZoom(7);

        PathOverlay pathOverlay = new PathOverlay();
        pathOverlay.addPoint(47.05515, -93.18604);
        pathOverlay.addPoint(47.05515, -86.55029);
        pathOverlay.addPoint(42.34231, -86.55029);
        pathOverlay.addPoint(42.34231, -93.18604);
        pathOverlay.addPoint(47.05515, -93.18604);

        mapView.addOverlay(pathOverlay);

        return view;
    }

}
