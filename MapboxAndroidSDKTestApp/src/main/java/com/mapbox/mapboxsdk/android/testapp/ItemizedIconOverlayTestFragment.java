package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class ItemizedIconOverlayTestFragment extends Fragment {
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itemizedoverlay, container, false);

		// Setup Map
		mapView = (MapView) view.findViewById(R.id.markersMapView);
		mapView.setCenter(new LatLng(50.51171, 4.86325));
		mapView.setZoom(8);

		return view;
	}
}
