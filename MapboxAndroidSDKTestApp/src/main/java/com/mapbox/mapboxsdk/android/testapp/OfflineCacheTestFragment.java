package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class OfflineCacheTestFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offlinecache, container, false);

        // Setup Map
        final MapView mapView = (MapView) view.findViewById(R.id.offlineCacheMapView);
        mapView.setDiskCacheEnabled(true);
        mapView.setCenter(new LatLng(46.85268, -121.75907));
        mapView.setZoom(10);

        final Button dataButton = (Button) view.findViewById(R.id.dataOnOffButton);
        if (!mapView.useDataConnection()) {
            dataButton.setText(R.string.dataOff);
        }
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataButton.getText().equals(getString(R.string.dataOn))) {
                    mapView.setUseDataConnection(false);
                    mapView.getTileProvider().clearTileMemoryCache();
                    dataButton.setText(R.string.dataOff);
                } else {
                    mapView.setUseDataConnection(true);
                    dataButton.setText(R.string.dataOn);
                }
            }
        });


        return view;
    }

}
