package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.Random;

public class MarkersTestActivity extends ActionBarActivity {

	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_markers);

		// Setup Map
		mapView = (MapView) findViewById(R.id.markersMapView);
		mapView.setCenter(new LatLng(38.11493876707079, 13.3647260069847));
		mapView.setZoom(14);

		// Dynamically create 100 markers
		Random r = new Random();
		for(int i = 0; i < 100; i++) {
			LatLng position = new LatLng(new LatLng(38.11493876707079f + r.nextFloat() / 100,
					13.3647260069847f  + r.nextFloat() / 100));
			addMarker(position);
		}
	}

	public void addMarker(LatLng position) {
		Marker marker = new Marker(mapView, "", "", position);
		marker.setIcon(new Icon(this, Icon.Size.SMALL, "marker-stroked", "FF0000"));
		mapView.addMarker(marker);
	}
}
