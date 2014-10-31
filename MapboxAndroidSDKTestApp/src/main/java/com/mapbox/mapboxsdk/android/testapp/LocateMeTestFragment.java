package com.mapbox.mapboxsdk.android.testapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

public class LocateMeTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_locate_me_test, container, false);
        MapView mv = (MapView) view.findViewById(R.id.locateMeMapView);
        mv.setZoom(14);

        // Adds an icon that shows location
        UserLocationOverlay myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(getActivity()), mv);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mv.getOverlays().add(myLocationOverlay);

        return view;
    }

}
