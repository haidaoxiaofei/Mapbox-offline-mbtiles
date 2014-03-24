package com.mapbox.mapboxsdk.android.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.*;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

public class MainActivity extends ActionBarActivity {

	private MapController mapController;
	private LatLng startingPoint = new LatLng(51f, 0f);
	private MapView mv;
	private UserLocationOverlay myLocationOverlay;
	private Paint paint;
	private String satellite = "brunosan.map-cyglrrfu";
	private String street = "examples.map-vyofok3q";
	private String terrain = "examples.map-zgrqqx0w";
	private String currentLayer = "terrain";
	private PathOverlay equator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mv = (MapView)findViewById(R.id.mapview);
		mapController = mv.getController();
        replaceMapView("open-streets-dc.mbtiles");

		mv.loadFromGeoJSONURL("https://gist.github.com/fdansv/8541618/raw/09da8aef983c8ffeb814d0a1baa8ecf563555b5d/geojsonpointtest");
		setButtonListeners();
		Marker m = new Marker(mv, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
		m.setIcon(new Icon(getResources(), Icon.Size.SMALL, "marker-stroked", "FF0000"));
		mv.addMarker(m);

		m = new Marker(mv, "Stockholm", "Sweden", new LatLng(59.32995, 18.06461));
		m.setIcon(new Icon(getResources(), Icon.Size.MEDIUM, "city", "FFFF00"));
		mv.addMarker(m);

		m = new Marker(mv, "Prague", "Czech Republic", new LatLng(50.08734, 14.42112));
		m.setIcon(new Icon(getResources(), Icon.Size.LARGE, "land-use", "00FFFF"));
		mv.addMarker(m);

		m = new Marker(mv, "Athens", "Greece", new LatLng(37.97885, 23.71399));
		mv.addMarker(m);

		mv.setOnTilesLoadedListener(new TilesLoadedListener() {
			@Override
			public boolean onTilesLoaded() {
				return false;
			}

			@Override
			public boolean onTilesLoadStarted()
			{
				// TODO Auto-generated method stub
				return false;
			}
		});
		mv.setVisibility(View.VISIBLE);
		equator = new PathOverlay();
		equator.addPoint(0,-89);
		equator.addPoint(0, 89);
		mv.getOverlays().add(equator);
	}

	private void setButtonListeners() {
		Button satBut = changeButtonTypeface((Button)findViewById(R.id.satbut));
		satBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!currentLayer.equals("satellite")) {
					replaceMapView(satellite);
					currentLayer = "satellite";
				}
			}
		});
		Button terBut = changeButtonTypeface((Button)findViewById(R.id.terbut));
		terBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!currentLayer.equals("terrain")) {
					replaceMapView(terrain);
					currentLayer = "terrain";
				}
			}
		});
		Button strBut = changeButtonTypeface((Button)findViewById(R.id.strbut));
		strBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!currentLayer.equals("street")) {
					replaceMapView(street);
					currentLayer = "street";
				}
			}
		});
	}

	protected void replaceMapView(String layer) {
        ITileLayer source;
        if (layer.endsWith("mbtiles")) {
            source = new MBTilesLayer(this, layer);
        }
        else {
            source = new MapboxTileLayer(layer);
        }
        mv.setScrollableAreaLimit(source.getBoundingBox());
        mv.setMinZoomLevel(source.getMinimumZoomLevel());
        mv.setMaxZoomLevel(source.getMaximumZoomLevel());
        mv.setTileSource(source);
        mv.setCenter(source.getCenterCoordinate());
        mv.setZoom(source.getCenterZoom());
        mv.zoomToBoundingBox(source.getBoundingBox());
	}

	private void addLocationOverlay() {
		// Adds an icon that shows location
		myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(this), mv);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.setDrawAccuracyEnabled(true);
		mv.getOverlays().add(myLocationOverlay);
	}
	private void addLine() {
		// Configures a line
		PathOverlay po = new PathOverlay(Color.RED, this);
		Paint linePaint = new Paint();
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setColor(Color.BLUE);
		linePaint.setStrokeWidth(5);
		po.setPaint(linePaint);
		po.addPoint(startingPoint);
		po.addPoint(new LatLng(51.7, 0.3));
		po.addPoint(new LatLng(51.2, 0));

		// Adds line and marker to the overlay
		mv.getOverlays().add(po);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}
	private Button changeButtonTypeface(Button button){
		//Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/semibold.ttf");
		//button.setTypeface(tf);
		return button;
	}

	public LatLng getMapCenter()
	{
		return mv.getCenter();
	}

	public void setMapCenter(ILatLng center)
	{
		mv.setCenter(center);
	}

	/**
	 * Method to show settings  in alert dialog
	 * On pressing Settings button will lauch Settings Options - GPS
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getBaseContext());

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				getBaseContext().startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
}