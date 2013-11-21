package com.mapbox.osmdroidtests;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import com.testflightapp.lib.TestFlight;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;

import java.util.ArrayList;

public class MainActivity extends Activity {
	private IMapController mapController;
	private GeoPoint startingPoint = new GeoPoint(51.5, 0);
	private MapTileProviderBasic tileProvider;
	private MapView mv;
	private MyLocationOverlay myLocationOverlay;
    Paint paint;
	
	private final String mapURL = "http://a.tiles.mapbox.com/v3/czana.map-e6nd3na3/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TestFlight.takeOff(getApplication(), "e4fe404b-2edc-4a2d-8083-3d708168e4c4");
        setContentView(R.layout.activity_main);
        tileProvider = new MapTileProviderBasic(this);
		
		// Defines source, indicating tag, resource id (if any), min zoom level, max zoom level,
		// tile size in pixels, image format, and map url.
		ITileSource tileSource = new XYTileSource("Test", ResourceProxy.string.online_mode, 3, 20, 256, ".png", mapURL);
		tileProvider.setTileSource(tileSource);
		
		// Initializes the view
        mv = (MapView) findViewById(R.id.mapview);
        mv.setTileSource(tileSource);
		// Sets initial position of the map camera
		mapController = mv.getController();
		mapController.setCenter(startingPoint);
		mapController.setZoom(7);
		
		// Activates pan & zoom controls
		mv.setMultiTouchControls(true);


		
		// Adds an icon that shows location
		myLocationOverlay = new MyLocationOverlay(this, mv);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.setDrawAccuracyEnabled(true);

		
		// Configures a marker
		OverlayItem myLocationOverlayItem = new OverlayItem("Hello", "Marker test", new GeoPoint(52f,0f));
        Drawable markerDrawable = this.getResources().getDrawable(R.drawable.pin);
        myLocationOverlayItem.setMarker(markerDrawable);
        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);
        
        //Sets marker actions
        ItemizedIconOverlay<OverlayItem> markerOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        mv.getOverlays().add(new Overlay(MainActivity.this) {
                            @Override
                            protected void draw(Canvas canvas, MapView mapView, boolean b) {
                                System.out.println("this is being called");
                                GeoPoint markerCoords = item.getPoint();
                                MapView.Projection projection = mapView.getProjection();
                                Point point = new Point();
                                projection.toPixels(markerCoords, point);

                                paint = new Paint();
                                paint.setColor(Color.WHITE);
                                canvas.drawRect(point.x - 240, point.y - 200, point.x + 240, point.y - 100, paint);
                                canvas.save();
                                canvas.rotate((float) 45, point.x, point.y - 100);
                                canvas.drawRect(point.x - 20, point.y - 120, point.x + 20, point.y - 80, paint);
                                canvas.restore();
                                paint.setColor(Color.rgb(50, 50, 50));
                                paint.setTextAlign(Paint.Align.CENTER);
                                paint.setTextSize(40f);
                                canvas.drawText("Helloooo this is a tooltip!", point.x, point.y-140, paint);

                            }
                        });
                        mv.invalidate();

                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, new DefaultResourceProxyImpl(getApplicationContext()));
        
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
        mv.getOverlays().add(markerOverlay);
        mv.getOverlays().add(po);
        mv.getOverlays().add(myLocationOverlay);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		return true;
	}

}
