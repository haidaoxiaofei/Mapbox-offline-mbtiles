// Created by plusminus on 21:28:12 - 25.09.2008
package com.mapbox.mapboxsdk.geometry;

import java.io.Serializable;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.util.constants.GeoConstants;
import com.mapbox.mapboxsdk.views.util.constants.MathConstants;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * An immutable latitude, longitude point. Coordinates are stored as WGS84 degrees.
 */
public final class LatLng implements ILatLng, MathConstants, GeoConstants, Parcelable, Serializable, Cloneable {

    static final long serialVersionUID = 1L;

    private double longitude;
    private double latitude;
    private double altitude;

    public LatLng(final double aLatitude, final double aLongitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
    }

    public LatLng(final double aLatitude, final double aLongitude, final double aAltitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
        this.altitude = aAltitude;
    }

    public LatLng(final Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), aLocation.getAltitude());
    }

    public LatLng(final LatLng aLatLng) {
        this.latitude = aLatLng.latitude;
        this.longitude = aLatLng.longitude;
        this.altitude = aLatLng.altitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getAltitude() {
        return this.altitude;
    }

    @Override
    public LatLng clone() {
        return new LatLng(this.latitude, this.longitude);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(this.latitude)
                .append(",")
                .append(this.longitude)
                .append(",")
                .append(this.altitude)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final LatLng rhs = (LatLng) obj;
        return rhs.latitude == this.latitude && rhs.longitude == this.longitude && rhs.altitude == this.altitude;
    }

    @Override
    public int hashCode() {
        return (int) (37.0 * (17.0 * latitude * 1E6d + longitude * 1E6d) + altitude);
    }

    /**
     * Write LatLng to parcel.
     * @param in
     */
    private LatLng(final Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.altitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(altitude);
    }

    public static final Parcelable.Creator<LatLng> CREATOR = new Parcelable.Creator<LatLng>() {
        @Override
        public LatLng createFromParcel(final Parcel in) {
            return new LatLng(in);
        }

        @Override
        public LatLng[] newArray(final int size) {
            return new LatLng[size];
        }
    };

    /**
     * @return distance in meters
     * @see <a href="http://www.geocities.com/DrChengalva/GPSDistance.html">GPSDistance.html</a>
     */
    public static double distanceTo(final LatLng a, final LatLng b) {

        final double a1 = DEG2RAD * a.latitude;
        final double a2 = DEG2RAD * a.longitude;
        final double b1 = DEG2RAD * b.latitude;
        final double b2 = DEG2RAD * b.longitude;

        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);

        final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);

        final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

        final double t3 = Math.sin(a1) * Math.sin(b1);

        final double tt = Math.acos(t1 + t2 + t3);

        return RADIUS_EARTH_METERS * tt;
    }

    /**
     * @return bearing in degrees
     * @see <a href="http://groups.google.com/group/osmdroid/browse_thread/thread/d22c4efeb9188fe9/bc7f9b3111158dd">discussion</a>
     */
    public static double bearingTo(final LatLng from, final LatLng to) {
        final double lat1 = Math.toRadians(from.latitude);
        final double long1 = Math.toRadians(from.longitude);
        final double lat2 = Math.toRadians(to.latitude);
        final double long2 = Math.toRadians(to.longitude);
        final double deltaLong = long2 - long1;
        final double a = Math.sin(deltaLong) * Math.cos(lat2);
        final double b = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLong);
        return (Math.toDegrees(Math.atan2(a, b)) + 360) % 360;
    }

    /**
     * Calculate a point that is the specified distance and bearing away from this point.
     *
     * @see <a href="http://www.movable-type.co.uk/scripts/latlong.html">latlong.html</a>
     * @see <a href="http://www.movable-type.co.uk/scripts/latlon.js">latlon.js</a>
     */
    public LatLng destinationPoint(final double aDistanceInMeters, final float aBearingInDegrees) {

        // convert distance to angular distance
        final double dist = aDistanceInMeters / RADIUS_EARTH_METERS;

        // convert bearing to radians
        final float brng = DEG2RAD * aBearingInDegrees;

        // get current location in radians
        final double lat1 = DEG2RAD * getLatitude();
        final double lon1 = DEG2RAD * getLongitude();

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
                * Math.sin(dist) * Math.cos(brng));
        final double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist)
                - Math.sin(lat1) * Math.sin(lat2));

        final double lat2deg = lat2 / DEG2RAD;
        final double lon2deg = lon2 / DEG2RAD;

        return new LatLng(lat2deg, lon2deg);
    }

    public static LatLng fromCenterBetween(final LatLng latLngA, final LatLng latLngB) {
        return new LatLng((latLngA.latitude + latLngB.latitude) / 2,
                (latLngA.longitude + latLngB.longitude) / 2);
    }
}
