/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/23/14 at 11:36 AM
 */

package com.mapbox.mapboxsdk.android.testapp.test;

import android.test.ActivityInstrumentationTestCase2;
import com.mapbox.mapboxsdk.android.testapp.MainActivity;
import com.mapbox.mapboxsdk.geometry.LatLng;
import junit.framework.Assert;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>
{
    public MainActivityTest()
    {
        super(MainActivity.class);
    }

    public void testSetMapCenter() throws Exception
    {
        LatLng center = new LatLng(43.07472, -89.38421);
        MainActivity activity = getActivity();
        activity.setMapCenter(center);
        Assert.assertEquals(center, activity.getMapCenter());
    }
}
