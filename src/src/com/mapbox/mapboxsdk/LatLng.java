/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 1/28/14 at 8:27 PM
 */
package com.mapbox.mapboxsdk;

import android.location.Location;
import org.osmdroid.util.GeoPoint;

public class LatLng extends GeoPoint
{
	public LatLng(int aLatitudeE6, int aLongitudeE6)
	{
		super(aLatitudeE6, aLongitudeE6);
	}

	public LatLng(int aLatitudeE6, int aLongitudeE6, int aAltitude)
	{
		super(aLatitudeE6, aLongitudeE6, aAltitude);
	}

	public LatLng(double aLatitude, double aLongitude)
	{
		super(aLatitude, aLongitude);
	}

	public LatLng(double aLatitude, double aLongitude, double aAltitude)
	{
		super(aLatitude, aLongitude, aAltitude);
	}

	public LatLng(Location aLocation)
	{
		super(aLocation);
	}

	public LatLng(GeoPoint aGeopoint)
	{
		super(aGeopoint);
	}
}
