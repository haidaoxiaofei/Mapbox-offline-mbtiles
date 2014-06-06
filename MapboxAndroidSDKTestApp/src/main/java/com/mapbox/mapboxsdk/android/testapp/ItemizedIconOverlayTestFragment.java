package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.ArrayList;

public class ItemizedIconOverlayTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemizedoverlay, container, false);

        // Setup Map
        MapView mapView = (MapView) view.findViewById(R.id.markersMapView);
        mapView.setCenter(new LatLng(50.51171, 4.86325));
        mapView.setZoom(8);

        // Build ItemizedIconOverlay From Markers
        ArrayList<Marker> markers = new ArrayList<Marker>();
        markers.add(new Marker(mapView, "Brasserie de Rochefort", null, new LatLng(50.17811, 5.21940)));
        markers.add(new Marker(mapView, "Brouwerij der Trappisten van Westmalle", null, new LatLng(51.28512, 4.65704)));
        markers.add(new Marker(mapView, "Brouwerij Westvleteren", null, new LatLng(50.89573, 2.72009)));
        markers.add(new Marker(mapView, "Bières de Chimay", null, new LatLng(49.982222, 4.336111)));
        markers.add(new Marker(mapView, "Brasserie d'Orval", null, new LatLng(49.639444, 5.348611)));
        markers.add(new Marker(mapView, "Brouwerij der Sint-Benedictusabdij de Achelse Kluis", null, new LatLng(51.2991402, 5.4902457)));
        markers.add(new Marker(mapView, "Brouwerij de Koningshoeven", null, new LatLng(51.543547, 5.128804)));

        mapView.addItemizedOverlay(new ItemizedIconOverlay(getActivity(), markers, new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
            @Override
            public boolean onItemSingleTapUp(int i, Marker marker) {
                Toast.makeText(getActivity(), "Marker Selected: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }

        @Override
        public boolean onItemLongPress(int i, Marker marker) {
            Toast.makeText(getActivity(), "Marker Selected: " + marker.getTitle(), Toast.LENGTH_LONG).show();
            return true;
        }
        }));

        return view;
    }
}
