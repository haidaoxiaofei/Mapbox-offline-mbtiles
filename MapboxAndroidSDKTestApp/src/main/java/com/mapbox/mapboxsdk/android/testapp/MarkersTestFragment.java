package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.Random;

public class MarkersTestFragment extends Fragment {

    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markers, container, false);

        // Setup Map
        mapView = (MapView) view.findViewById(R.id.markersMapView);
        mapView.setCenter(new LatLng(38.11493876707079, 13.3647260069847));
        mapView.setZoom(14);

        // Dynamically create 100 markers
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            LatLng position = new LatLng(new LatLng(38.11493876707079f + r.nextFloat() / 100,
                        13.3647260069847f  + r.nextFloat() / 100));
            addMarker(position);
        }

        return view;
    }

    public void addMarker(LatLng position) {
        Marker marker = new Marker(mapView, "", "", position);
        marker.setIcon(new Icon(getActivity(), Icon.Size.SMALL, "marker-stroked", "FF0000"));
        mapView.addMarker(marker);
    }
}
