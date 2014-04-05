/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 4/4/14 at 2:07 PM
 */

package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class AlternateMapTestActivity extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternatemap);
        MapView mv = (MapView) findViewById(R.id.altMapView);
        mv.setCenter(new LatLng(43.07472, -89.38421));
        mv.setZoom(14);
    }
}
