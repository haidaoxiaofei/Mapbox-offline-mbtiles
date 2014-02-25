/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/23/14 at 11:36 AM
 */

package com.mapbox.mapboxsdk.android.testapp.test;

import android.test.ActivityInstrumentationTestCase2;
import com.mapbox.mapboxsdk.android.testapp.MainActivity;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import junit.framework.Assert;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testSetMapCenter() throws Exception {
        LatLng center = new LatLng(43.07472, -89.38421);
        MainActivity activity = getActivity();
        activity.setMapCenter(center);
        // Assert.assertEquals(center, activity.getMapCenter());
    }

    public void testLatLng() throws Exception {
        LatLng ll = new LatLng(10d, -20d);
        LatLng llb = new LatLng(10d, -20d);
        LatLng llc = new LatLng(10d, -21d, 100d);

        Assert.assertEquals(ll.getLatitude(), 10d);
        Assert.assertEquals(ll.getLongitude(), -20d);
        Assert.assertEquals(ll.getAltitude(), 0d);

        Assert.assertEquals(ll.toString(), "10.0,-20.0,0.0");
        Assert.assertEquals(llc.toString(), "10.0,-21.0,100.0");

        Assert.assertTrue(ll.equals(llb));
        Assert.assertFalse(ll.equals(llc));
        Assert.assertTrue(ll.equals(ll.clone()));
        Assert.assertTrue(ll.equals(new LatLng(ll)));
    }

    public void testBoundingBox() throws Exception {
        BoundingBox bb = new BoundingBox(10d, 10d, 0d, 0d);

        Assert.assertEquals(bb.getLatNorth(), 10d);
        Assert.assertEquals(bb.getLatSouth(), 0d);
        Assert.assertEquals(bb.getLonEast(), 10d);
        Assert.assertEquals(bb.getLonWest(), 0d);

        Assert.assertEquals(bb.toString(), "N:10.0; E:10.0; S:0.0; W:0.0");

        Assert.assertEquals(bb.getCenter().getLongitude(), 5.0d);
        Assert.assertEquals(bb.getLongitudeSpan(), 10.0d);
        Assert.assertEquals(bb.getLatitudeSpan(), 10.0d);
    }

    public void testTileSystem() throws Exception {
        TileSystem.setTileSize(256);
        Assert.assertEquals(TileSystem.getTileSize(), 256);
        Assert.assertEquals(TileSystem.MapSize(5), 8192);
    }

    public void testMapTile() throws Exception {
        MapTile tile = new MapTile(1, 2, 3);
        MapTile tileB = new MapTile(1, 2, 3);
        MapTile tileC = new MapTile(1, 2, 4);

        Assert.assertEquals(tile.getZ(), 1);
        Assert.assertEquals(tile.getX(), 2);
        Assert.assertEquals(tile.getY(), 3);
        Assert.assertEquals(tile.toString(), "/1/2/3");

        Assert.assertTrue(tile.equals(tileB));
        Assert.assertFalse(tile.equals(tileC));
    }

    public void testTileLayer() throws Exception {
        TileLayer tl = new TileLayer("http://hi.com/{z}/{x}/{y}{2x}.png");
        MapTile tile = new MapTile(0, 0, 0);
        Assert.assertEquals(tl.getTileURL(tile, true), "http://hi.com/0/0/0@2x.png");
        Assert.assertEquals(tl.setURL("http://hello.com/{z}/{x}/{y}{2x}.png"), tl);
        Assert.assertEquals(tl.getTileURL(tile, true), "http://hello.com/0/0/0@2x.png");
    }
}
