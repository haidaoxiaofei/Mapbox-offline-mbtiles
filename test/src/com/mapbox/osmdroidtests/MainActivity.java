package com.mapbox.osmdroidtests;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import com.mapbox.mapboxsdk.MapView;
import com.testflightapp.lib.TestFlight;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends Activity {
	private IMapController mapController;
	private GeoPoint startingPoint = new GeoPoint(37.7905, -122.4062);
	private MapTileProviderBasic tileProvider;
	private MapView mv;
	private MyLocationNewOverlay myLocationOverlay;
    private Paint paint;
    private final String mapURL = "examples.map-9ijuk24y";
    private final String otherURL = "fdansv.maphome";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestFlight.takeOff(getApplication(), "e4fe404b-2edc-4a2d-8083-3d708168e4c4");
        setContentView(R.layout.activity_main);
        mv = (MapView) findViewById(R.id.mapview);
        mv.setURL(otherURL);
        mapController = mv.getController();
        mapController.setCenter(startingPoint);
        mapController.setZoom(3);

        // Configures a marker
        mv.addMarker(52.5, 0f, "Hello", "Marker test");
        Button button = (Button) findViewById(R.id.layerbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Adding layer");
                
                mv.addLayer("sf", mapURL);
                mv.invalidate();
            }
        });

    }
    private void addLocationOverlay(){
        // Adds an icon that shows location
        myLocationOverlay = new MyLocationNewOverlay(this, mv);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mv.getOverlays().add(myLocationOverlay);
    }
    private void addLine(){
        // Configures a line
        PathOverlay po = new PathOverlay(Color.RED, this);
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(5);
        po.setPaint(linePaint);
        po.addPoint(startingPoint);
        po.addPoint(new GeoPoint(51.7, 0.3));
        po.addPoint(new GeoPoint(51.2, 0));

        // Adds line and marker to the overlay
        mv.getOverlays().add(po);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		return true;
	}


}
