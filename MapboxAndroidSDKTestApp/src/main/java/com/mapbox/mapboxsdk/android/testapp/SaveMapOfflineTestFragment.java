package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.CoordinateRegion;
import com.mapbox.mapboxsdk.geometry.CoordinateSpan;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineMapDatabase;
import com.mapbox.mapboxsdk.offline.OfflineMapDownloader;
import com.mapbox.mapboxsdk.offline.OfflineMapDownloaderListener;
import com.mapbox.mapboxsdk.overlay.OfflineMapTileProvider;
import com.mapbox.mapboxsdk.overlay.TilesOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.ArrayList;

public class SaveMapOfflineTestFragment extends Fragment implements OfflineMapDownloaderListener {

    private static final String TAG = "SaveMapOfflineTestFragment";

    private MapView mapView;
    private TilesOverlay offlineMapOverlay;
    private ProgressBar progressBar;

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

        Button deleteMapButton = (Button) view.findViewById(R.id.deleteMapsButton);
        deleteMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDeleteMapButton(v);
            }
        });

        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetButton(v);
            }
        });

        OfflineMapDownloader offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(getActivity());
        offlineMapDownloader.addOfflineMapDownloaderListener(this);

        progressBar = (ProgressBar)view.findViewById(R.id.downloadProgress);

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

            OfflineMapTileProvider tp = new OfflineMapTileProvider(getActivity(), db);
            offlineMapOverlay = new TilesOverlay(tp);
            mapView.addOverlay(offlineMapOverlay);
        } else {
            Toast.makeText(getActivity(), "No Offline Maps available.", Toast.LENGTH_LONG).show();
        }
    }

    public void handleDeleteMapButton(View view) {
        Log.i(TAG, "handleDeleteMapButton()");
        OfflineMapDownloader offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(getActivity());
        if (offlineMapDownloader.isMapIdAlreadyAnOfflineMapDatabase(getString(R.string.mapbox_id_street))) {
            boolean result = offlineMapDownloader.removeOfflineMapDatabaseWithID(getString(R.string.mapbox_id_street));
            Toast.makeText(getActivity(), String.format("Result of deletion attempt: '%s'", result), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "It's not an offline database yet.", Toast.LENGTH_LONG).show();
        }
    }

    public void handleResetButton(View view) {
        Log.i(TAG, "handleResetButton()");

        mapView.removeOverlay(offlineMapOverlay);
        mapView.setCenter(new LatLng(29.94423, -90.09201));
        mapView.setZoom(12);
    }

    @Override
    public void stateChanged(OfflineMapDownloader.MBXOfflineMapDownloaderState newState) {
        Log.i(TAG, String.format("stateChanged to %s", newState));
    }

    @Override
    public void initialCountOfFiles(Integer numberOfFiles) {
        Log.i(TAG, "stateChanged");
    }

    @Override
    public void progressUpdate(final Integer numberOfFilesWritten, final Integer numberOfFilesExpected) {
        Log.i(TAG, String.format("progressUpdate: files written = %d, files expected = %d", numberOfFilesWritten, numberOfFilesExpected));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setMax(numberOfFilesExpected);
                progressBar.setProgress(numberOfFilesWritten);

                if (numberOfFilesExpected == numberOfFilesWritten) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void networkConnectivityError(Throwable error) {
        Log.i(TAG, "networkConnectivityError");
    }

    @Override
    public void sqlLiteError(Throwable error) {
        Log.i(TAG, "sqlLiteError");
    }

    @Override
    public void httpStatusError(Throwable error) {
        Log.i(TAG, "httpStatusError");
    }

    @Override
    public void completionOfOfflineDatabaseMap(OfflineMapDatabase offlineMapDatabase) {
        Log.i(TAG, "completionOfOfflineDatabaseMap");
        // Need to run on UI Thread if going to display on UI
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "Finished Saving Database", Toast.LENGTH_LONG).show();
            }
        });
    }
}
