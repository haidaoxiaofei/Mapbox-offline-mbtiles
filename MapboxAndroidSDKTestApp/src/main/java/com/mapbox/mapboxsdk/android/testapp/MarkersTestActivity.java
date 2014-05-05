/**
 * @author Brad Leege <leege@doit.wisc.edu>
 * Created on 5/4/14 at 6:42 PM
 */
package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class MarkersTestActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_markers);
		MapView mv = (MapView) findViewById(R.id.markersMapView);
		mv.setCenter(new LatLng(38.11493876707079, 13.3647260069847));
		mv.setZoom(14);
	}
}
