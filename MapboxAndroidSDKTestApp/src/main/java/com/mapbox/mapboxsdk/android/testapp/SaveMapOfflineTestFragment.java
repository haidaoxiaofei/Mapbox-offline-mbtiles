package com.mapbox.mapboxsdk.android.testapp;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.CoordinateRegion;
import com.mapbox.mapboxsdk.geometry.CoordinateSpan;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineMapDatabase;
import com.mapbox.mapboxsdk.offline.OfflineMapDownloader;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;

public class SaveMapOfflineTestFragment extends Fragment {

    private static final String TAG = "SaveMapOfflineTestFragment";

    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savemapoffline, container, false);

        // Setup Map
        mapView = (MapView) view.findViewById(R.id.saveMapOfflineMapView);
        mapView.setCenter(new LatLng(29.94423, -90.09201));
        mapView.setZoom(12);

        Button saveMapButton = (Button) view.findViewById(R.id.saveMapButton);
        saveMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveMapButton(v);
            }
        });

        Button loadMapButton = (Button) view.findViewById(R.id.loadMapButton);
        loadMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoadMapButton(v);
            }
        });

        return view;
    }

    public void handleSaveMapButton(View view) {
        Log.i(TAG, "handleSaveMapButton() called");
        OfflineMapDownloader offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(getActivity());

        BoundingBox boundingBox = mapView.getBoundingBox();
        CoordinateSpan span = new CoordinateSpan(boundingBox.getLatitudeSpan(), boundingBox.getLongitudeSpan());
        CoordinateRegion coordinateRegion = new CoordinateRegion(mapView.getCenter(), span);
        offlineMapDownloader.beginDownloadingMapID(getString(R.string.mapbox_id_street), coordinateRegion, (int) mapView.getZoomLevel(), (int) mapView.getZoomLevel());
    }

    public void handleLoadMapButton(View view) {
        Log.i(TAG, "handleLoadMapButton()");
        OfflineMapDownloader offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(getActivity());

        ArrayList<OfflineMapDatabase> offlineMapDatabases = offlineMapDownloader.getMutableOfflineMapDatabases();
        if (offlineMapDatabases != null && offlineMapDatabases.size() > 0) {
            OfflineMapDatabase db = offlineMapDatabases.get(0);
            Toast.makeText(getActivity(), String.format("Will load MapID = '%s'", db.getMapID()), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getActivity(), "No Offline Maps available.", Toast.LENGTH_LONG).show();
        }
    }
}
