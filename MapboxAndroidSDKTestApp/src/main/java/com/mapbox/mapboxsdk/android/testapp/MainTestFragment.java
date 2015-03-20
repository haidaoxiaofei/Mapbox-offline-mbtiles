package com.mapbox.mapboxsdk.android.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import java.util.List;

public class MainTestFragment extends Fragment {
    private LatLng startingPoint = new LatLng(51f, 0f);
    private MapView mv;
    private String satellite = "brunosan.map-cyglrrfu";
    private String street = "examples.map-i87786ca";
    private String terrain = "examples.map-zgrqqx0w";
    private final String mbTile = "zhhqw.mbtiles";
    private String currentLayer = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_maintest, container, false);

        mv = (MapView) view.findViewById(R.id.mapview);
        // Set Default Map Type
        replaceMapView(terrain);
        currentLayer = "terrain";
        mv.setUserLocationEnabled(true)
            .setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.FOLLOW);
        /*
        // Original GeoJSON Test that caus es crash when Hardware Acceleration when enabled in TestApp
        mv.loadFromGeoJSONURL("https://gist.githubusercontent.com/tmcw/4a6f5fa40ab9a6b2f163/raw/b1ee1e445225fc0a397e2605feda7da74c36161b/map.geojson");
        */
        // Smaller GeoJSON Test
        mv.loadFromGeoJSONURL("https://gist.githubusercontent.com/bleege/133920f60eb7a334430f/raw/5392bad4e09015d3995d6153db21869b02f34d27/map.geojson");
        Marker m = new Marker(mv, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
        m.setIcon(new Icon(getActivity(), Icon.Size.SMALL, "marker-stroked", "FF0000"));
        mv.addMarker(m);

        m = new Marker(mv, "Stockholm", "Sweden", new LatLng(59.32995, 18.06461));
        m.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "city", "FFFF00"));
        mv.addMarker(m);

        m = new Marker(mv, "Prague", "Czech Republic", new LatLng(50.08734, 14.42112));
        m.setIcon(new Icon(getActivity(), Icon.Size.LARGE, "land-use", "00FFFF"));
        mv.addMarker(m);

        m = new Marker(mv, "Athens", "Greece", new LatLng(37.97885, 23.71399));
        mv.addMarker(m);

        /*
           m = new Marker(mv, "Prague2", "Czech Republic", new LatLng(50.0875, 14.42112));
           m.setIcon(new Icon(getBaseContext(), Icon.Size.LARGE, "land-use", "00FF00"));
           mv.addMarker(m);
           */
        // Set Button Listeners
        Button satBut = changeButtonTypeface((Button) view.findViewById(R.id.satbut));
        satBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("satellite")) {
                    replaceMapView(satellite);
                    currentLayer = "satellite";
                }
            }
        });
        Button terBut = changeButtonTypeface((Button) view.findViewById(R.id.terbut));
        terBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("terrain")) {
                    replaceMapView(terrain);
                    currentLayer = "terrain";
                }
            }
        });
        Button strBut = changeButtonTypeface((Button) view.findViewById(R.id.strbut));
        strBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals("street")) {
                    replaceMapView(street);
                    currentLayer = "street";
                }
            }
        });
        Button mbTileBut = changeButtonTypeface((Button) view.findViewById(R.id.mbTilesBut));
        mbTileBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentLayer.equals(mbTile)) {
                    replaceMapView(mbTile);
                    currentLayer = mbTile;
                }
            }
        });


        /*
           Button altBut = changeButtonTypeface((Button) view.findViewById(R.id.strAltMap));
           altBut.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
           Intent altMapActivity =
           new Intent(getActivity(), AlternateMapTestActivity.class);
           startActivity(altMapActivity);
           }
           });

           Button pinsButton = changeButtonTypeface((Button) view.findViewById(R.id.markersButton));
           pinsButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
           Intent altMapActivity =
           new Intent(getActivity(), MarkersTestActivity.class);
           startActivity(altMapActivity);
           }
           });
           */

        Button spinButton = changeButtonTypeface((Button) view.findViewById(R.id.spinButton));
        spinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mv.setMapOrientation(mv.getMapOrientation() + 45f);
            }
        });

        /*
           Button selectBut = changeButtonTypeface((Button) view.findViewById(R.id.layerselect));
           selectBut.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
           AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
           ab.setTitle("Select Layer");
           ab.setItems(availableLayers, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface d, int choice) {
           replaceMapView(availableLayers[choice]);
           }
           });
           ab.show();
           }
           });
           */

        mv.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

        @Override
        public boolean onTilesLoadStarted() {
            // TODO Auto-generated method stub
            return false;
        }
        });
        mv.setVisibility(View.VISIBLE);

        PathOverlay equator = new PathOverlay();
        equator.addPoint(0, -89);
        equator.addPoint(0, 89);
        mv.getOverlays().add(equator);

        return view;
    }

    final String[] availableLayers = {
        "OpenStreetMap", "OpenSeaMap", "mapquest", "open-streets-dc.mbtiles", "test.MBTiles"
    };

    protected void replaceMapView(String layer) {
        ITileLayer source;
        BoundingBox box;
        if (layer.toLowerCase().endsWith("mbtiles")) {
            TileLayer mbTileLayer = new MBTilesLayer(getActivity(), layer);
            //            mv.setTileSource(mbTileLayer);
            displayMarkersAndRoutes();
            mv.setTileSource(new ITileLayer[] {
                mbTileLayer, new WebSourceTileLayer("mapquest",
                    "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png").setName(
                        "MapQuest Open Aerial")
                        .setAttribution("Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                .setMinimumZoomLevel(1)
                .setMaximumZoomLevel(18)
            });
            box = mbTileLayer.getBoundingBox();
        } else {
            if (layer.equalsIgnoreCase("OpenStreetMap")) {
                source = new WebSourceTileLayer("openstreetmap",
                        "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(1)
                    .setMaximumZoomLevel(18);
            } else if (layer.equalsIgnoreCase("OpenSeaMap")) {
                source = new WebSourceTileLayer("openstreetmap",
                        "http://tile.openstreetmap.org/seamark/{z}/{x}/{y}.png").setName(
                            "OpenStreetMap")
                            .setAttribution("© OpenStreetMap Contributors")
                            .setMinimumZoomLevel(1)
                            .setMaximumZoomLevel(18);
            } else if (layer.equalsIgnoreCase("mapquest")) {
                source = new WebSourceTileLayer("mapquest",
                        "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png").setName(
                            "MapQuest Open Aerial")
                            .setAttribution(
                                    "Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                            .setMinimumZoomLevel(1)
                            .setMaximumZoomLevel(18);
            } else {
                source = new MapboxTileLayer(layer);
            }
            mv.setTileSource(source);
            box = source.getBoundingBox();
        }
        //        mv.setScrollableAreaLimit(mv.getTileProvider().getBoundingBox());
        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(mv.getTileProvider().getCenterCoordinate());
        mv.setZoom(0);
        Log.d("MainActivity", "zoomToBoundingBox " + box.toString());
        //        mv.zoomToBoundingBox(box);
    }

    private void addLine() {
        // Configures a line
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(5);

        PathOverlay po = new PathOverlay().setPaint(linePaint);

        po.addPoint(startingPoint);
        po.addPoint(new LatLng(51.7, 0.3));
        po.addPoint(new LatLng(51.2, 0));

        // Adds line and marker to the overlay
        mv.getOverlays().add(po);
    }

    private Button changeButtonTypeface(Button button) {
        //Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/semibold.ttf");
        //button.setTypeface(tf);
        return button;
    }

    public LatLng getMapCenter() {
        return mv.getCenter();
    }

    public void setMapCenter(ILatLng center) {
        mv.setCenter(center);
    }

    /**
     * Method to show settings  in alert dialog
     * On pressing Settings button will lauch Settings Options - GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
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


    public void displayMarkersAndRoutes(){
        addAnchorMarkers();
//        drawRoutes();
//        matrixMarkers();
    }

    public void matrixMarkers(){
        double xStart = 113.11965;
        double xEnd = 113.127427;
        double yStart = 22.072323;
        double yEnd = 22.072744;

        double xSpan = xEnd - xStart;
        double ySpan = yEnd - yStart;

        int density = 20;
        double xStep = xSpan / density;
        double yStep = ySpan / density;

        double currentX = xStart;


        while (currentX <= xEnd) {
            double currentY = yStart;
            while (currentY <= yEnd){
                Point testP = new Point(currentX,currentY);
                Point screenP = gpsPointMappingToScreen(testP);
                Marker m = new Marker(mv, "cX:"+currentX, "cY:"+currentY, screenP.getLatLng());
                m.setIcon(new Icon(getActivity(), Icon.Size.SMALL, "marker-stroked", "FF0000"));
                mv.addMarker(m);
                currentY += yStep;
            }
            currentX += xStep;

        }

    }

    public void addAnchorMarkers(){
//        Point testP = new Point(113.120449,22.065053);
//        Point screenP = gpsPointMappingToScreen(testP);
//        Marker m = new Marker(mv, "左下", "河与路交界", screenP.getLatLng());

        String[] markerInfos = {"113.122001,22.07028,大喷泉,左侧环路中心",
                "113.122886, 22.07374, 神秘岛, 中心",
                "113.119647,22.072738,中上,内凹丁字路口",
                "113.120449,22.065053,左下,河与路交界",
                "113.122814,22.073299, 神秘岛,下方环路",
                "113.123852,22.073437,神秘岛,右侧环路",
                "113.12283,22.07280, anchor, anchor",
                "113.118026,22.073799, sea, seafront"
        };

        for(int i = 0; i < markerInfos.length; i++){
            String[] markerAreas = markerInfos[i].split(",");
            Point testP = new Point(Double.valueOf(markerAreas[0]),Double.valueOf(markerAreas[1]));
            Point screenP = gpsPointMappingToScreen(testP);
            Marker m = new Marker(mv, markerAreas[2], markerAreas[3], screenP.getLatLng());
            m.setIcon(new Icon(getActivity(), Icon.Size.SMALL, "marker-stroked", "FF0000"));
            mv.addMarker(m);
        }

    }


    public void drawRoutes(){
        List<Segment> routesList = RoadRouteRecordManager.getRoutesList();


        for (Segment s : routesList){
            PathOverlay route = new PathOverlay();
            Point sPoint = ocnPointToScreen(s.getsPoint().x, s.getsPoint().y);
            Point ePoint = ocnPointToScreen(s.getePoint().x, s.getePoint().y);
            route.addPoint(sPoint.getLatitude(), sPoint.getLongitude());
            route.addPoint(ePoint.getLatitude(), ePoint.getLongitude());
            mv.getOverlays().add(route);
        }

    }

    /////mapping functions

    public Point gpsPointMappingToScreen(Point originGpsPoint) {
        Point ocnPoint = gpsPointMappingToOcn(originGpsPoint);
        return ocnPointToScreen(ocnPoint.x, ocnPoint.y);
    }

    public Point gpsPointMappingToOcn(Point originGpsPoint) {
        Matrix m = new Matrix();

        /*
        Axis Converter for HQW
        Baidu GPS : 113.119647D, 22.072738D;
        OCN result: 239068.1D, 95352.18D
        */


        double x = originGpsPoint.getX() - 113.119647D;
        double y = originGpsPoint.getY() - 22.072738D;


        m.postRotate(45.0F);
        m.postScale(2.8452876F, 2.8452876F * (float)Math.sin(0.7853982D));
        float[] ret = new float[2];
        m.mapPoints(ret, new float[] { (float)(239068.1D + x * 186989.8154655078D), (float)(95352.18D + y * -194167.6530986923D) });


        return new Point(ret[0], ret[1]);
    }

    public Point ocnPointToScreen(float x, float y) {
        // convert OCN to display LatLon

        /*
        OCN:
        left_bottom(x,y) = (1009132, 1123244)
        right_top(x,y) = (1013760, 1118720)
        Target LatLon:
        left_bottom(x,y) = (-1,-1)
        right_top(x,y) = (1,1)
         */

        double left_bottom_x = 286208;
        double left_bottom_y = 478696;
        double right_top_x = 293828;
        double right_top_y = 473552;

        double ocn_h = left_bottom_y - right_top_y;
        double ocn_w = right_top_x - left_bottom_x;

        double left_bottom_latlon_x = -1;
        double left_bottom_latlon_y = -0.67849723;
        double right_top_latlon_x = 1;
        double right_top_latlon_y = 0.67849723;


        double latlon_h = right_top_latlon_y - left_bottom_latlon_y;
        double latlon_w = right_top_latlon_x - left_bottom_latlon_x;

        double latlon_x = (x - left_bottom_x)/ocn_w * latlon_w + left_bottom_latlon_x - 0.01;
        double latlon_y = -((y - left_bottom_y)/ocn_h * latlon_h - left_bottom_latlon_y) + 0.025;


        return new Point(latlon_x, latlon_y);
    }

    public Point ocnPointToScreen(Point op) {
        return ocnPointToScreen((float)op.getX(), (float)op.getY());
    }
    public Point ocnPointMappingToGps(Point op){
        Matrix m = new Matrix();
        m.postRotate(45.0F);
        m.postScale(1.0F, (float)Math.sin(0.7853982D));
        m.postScale(2.8452876F, 2.8452876F);
        m.invert(m);
        float[] ret = new float[2];
        m.mapPoints(ret, new float[] { (float)op.getX(), (float)op.getY() });
        double x = (ret[0]-645537.3D)/214293.74333333335D+113.98037D;
        double y = (ret[1]-143025.36D)/(-232736.01333333334D)+22.539246D;
        return new Point(x,y);
    }
    public Point screenPointToOcn(float x,float y){
        double ocn_h = 1123575 - 1118469;
        double ocn_w = 1014264 - 1009155;

        double latlon_h = 1 - (-1);
        double latlon_w = 1 - (-1);

        double ocn_x = (x-0.006-(-1))/latlon_w*ocn_w+1009155;
        double ocn_y = (-y-1)/latlon_h*ocn_h+1123575;
        return new Point(ocn_x,ocn_y);
    }
    public Point screenPointToOcn(Point sp){
        return screenPointToOcn((float) sp.getX(), (float) sp.getY());
    }
    public Point screenPointToGps(Point sp){
        return ocnPointMappingToGps(screenPointToOcn(sp));
    }

}
