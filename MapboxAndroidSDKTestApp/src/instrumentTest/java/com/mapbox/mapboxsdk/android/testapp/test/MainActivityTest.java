/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/23/14 at 11:36 AM
 */

package com.mapbox.mapboxsdk.android.testapp.test;

import android.test.ActivityInstrumentationTestCase2;
import com.mapbox.mapboxsdk.android.testapp.MainActivity;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.views.util.Projection;
import java.util.ArrayList;
import junit.framework.Assert;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testSetMapCenter() throws Exception {
        LatLng center = new LatLng(43.07472, -89.38421);
        MainActivity activity = getActivity();
        activity.setMapCenter(center);
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
        Assert.assertTrue(bb.contains(new LatLng(5f, 5f)));
        Assert.assertFalse(bb.contains(new LatLng(15f, 5f)));

        ArrayList pts = new ArrayList();
        pts.add(new LatLng(0f, 0f));
        pts.add(new LatLng(10f, 10f));
        Assert.assertTrue(bb.equals(BoundingBox.fromLatLngs(pts)));

        BoundingBox bb2 = new BoundingBox(0d, 0d, -10d, -10d);

        Assert.assertTrue(bb.union(bb2).equals(new BoundingBox(10d, 10d, -10d, -10d)));

        Assert.assertEquals(new BoundingBox(0d, 0d, 0d, 0d).toString(),
                bb.intersect(bb2).toString());
    }

    public void testMapTile() throws Exception {
        MapTile tile = new MapTile(1, 2, 3);
        MapTile tileB = new MapTile(1, 2, 3);
        MapTile tileC = new MapTile(1, 2, 4);

        Assert.assertEquals(tile.getZ(), 1);
        Assert.assertEquals(tile.getX(), 2);
        Assert.assertEquals(tile.getY(), 3);
        Assert.assertEquals(tile.toString(), "1/2/3");

        Assert.assertTrue(tile.equals(tileB));
        Assert.assertFalse(tile.equals(tileC));
    }

    public void testProjection() throws Exception {
        Assert.assertEquals(256, Projection.mapSize(0f));
        Assert.assertEquals(512, Projection.mapSize(1f));
        Assert.assertEquals(256, Projection.getTileSize());
    }
}
