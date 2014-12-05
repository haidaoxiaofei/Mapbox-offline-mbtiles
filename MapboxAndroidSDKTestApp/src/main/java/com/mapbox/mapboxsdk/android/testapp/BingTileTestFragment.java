package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.BingTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

public class BingTileTestFragment extends Fragment {

    private static String BING_KEY = ""; // Fill this in.
    private MapView mapView;
    private BingTileLayer bingTileLayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bingtiletest, container, false);

        // Setup Map
        mapView = (MapView)view.findViewById(R.id.mapview);

        if (BING_KEY.isEmpty()) {
            Toast.makeText(inflater.getContext(), "BING_KEY needs to be defined", Toast.LENGTH_SHORT).show();
        }

        bingTileLayer = new BingTileLayer(BING_KEY);
        bingTileLayer.setName("Some sweet Bing action");
        bingTileLayer.setStyle(BingTileLayer.IMAGERYSET_AERIAL);

        mapView.setTileSource(bingTileLayer);
        mapView.setCenter(new LatLng(34.19997, -118.17163));
        mapView.setZoom(12);

        Button satButton = (Button)view.findViewById(R.id.satbut);
        satButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMapStyle(BingTileLayer.IMAGERYSET_AERIAL);
            }
        });

        Button satLabButton = (Button)view.findViewById(R.id.satlabbut);
        satLabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMapStyle(BingTileLayer.IMAGERYSET_AERIALWITHLABELS);
            }
        });

        Button strButton = (Button)view.findViewById(R.id.strbut);
        strButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMapStyle(BingTileLayer.IMAGERYSET_ROAD);
            }
        });

        return view;
    }

    private void changeMapStyle(String imagerySet) {
        bingTileLayer.setStyle(imagerySet);
        mapView.setTileSource(bingTileLayer);
    }
}
