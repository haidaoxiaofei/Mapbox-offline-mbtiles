// Created by plusminus on 21:28:12 - 25.09.2008
package com.mapbox.mapboxsdk.util;

import java.io.Serializable;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.util.constants.GeoConstants;
import com.mapbox.mapboxsdk.views.util.constants.MathConstants;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Gramlich
 * @author Theodore Hong
 */
public class LatLng implements ILatLng, MathConstants, GeoConstants, Parcelable, Serializable, Cloneable {

    static final long serialVersionUID = 1L;

    public double longitude;
    public double latitude;
    public double mAltitude;

    public LatLng(final double aLatitude, final double aLongitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
    }

    public LatLng(final double aLatitude, final double aLongitude, final double aAltitude) {
        this.latitude = aLatitude;
        this.longitude = aLongitude;
        this.mAltitude = aAltitude;
    }

    public LatLng(final Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), aLocation.getAltitude());
    }

    public LatLng(final LatLng aLatLng) {
        this.latitude = aLatLng.latitude;
        this.longitude = aLatLng.longitude;
        this.mAltitude = aLatLng.mAltitude;
    }

    public static LatLng fromDoubleString(final String s, final char spacer) {
        final int spacerPos1 = s.indexOf(spacer);
        final int spacerPos2 = s.indexOf(spacer, spacerPos1 + 1);

        if (spacerPos2 == -1) {
            return new LatLng(
                    (int) (Double.parseDouble(s.substring(0, spacerPos1)) * 1E6),
                    (int) (Double.parseDouble(s.substring(spacerPos1 + 1, s.length())) * 1E6));
        } else {
            return new LatLng(
                    (int) (Double.parseDouble(s.substring(0, spacerPos1)) * 1E6),
                    (int) (Double.parseDouble(s.substring(spacerPos1 + 1, spacerPos2)) * 1E6),
                    (int) Double.parseDouble(s.substring(spacerPos2 + 1, s.length())));
        }
    }

    public static LatLng fromInvertedDoubleString(final String s, final char spacer) {
        final int spacerPos1 = s.indexOf(spacer);
        final int spacerPos2 = s.indexOf(spacer, spacerPos1 + 1);

        if (spacerPos2 == -1) {
            return new LatLng(
                    (int) (Double.parseDouble(s.substring(spacerPos1 + 1, s.length())) * 1E6),
                    (int) (Double.parseDouble(s.substring(0, spacerPos1)) * 1E6));
        } else {
            return new LatLng(
                    (int) (Double.parseDouble(s.substring(spacerPos1 + 1, spacerPos2)) * 1E6),
                    (int) (Double.parseDouble(s.substring(0, spacerPos1)) * 1E6),
                    (int) Double.parseDouble(s.substring(spacerPos2 + 1, s.length())));

        }
    }

    public static LatLng fromIntString(final String s) {
        final int commaPos1 = s.indexOf(',');
        final int commaPos2 = s.indexOf(',', commaPos1 + 1);

        if (commaPos2 == -1) {
            return new LatLng(
                    Integer.parseInt(s.substring(0, commaPos1)),
                    Integer.parseInt(s.substring(commaPos1 + 1, s.length())));
        } else {
            return new LatLng(
                    Integer.parseInt(s.substring(0, commaPos1)),
                    Integer.parseInt(s.substring(commaPos1 + 1, commaPos2)),
                    Integer.parseInt(s.substring(commaPos2 + 1, s.length()))
            );
        }
    }


    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    public double getAltitude() {
        return this.mAltitude;
    }

    public void setLongitude(final double aLongitude) {
        this.longitude = aLongitude;
    }

    public void setLatitude(final double aLatitude) {
        this.latitude = aLatitude;
    }

    public void setAltitude(final double aAltitude) {
        this.mAltitude = aAltitude;
    }

    @Override
    public Object clone() {
        return new LatLng(this.latitude, this.longitude);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(this.latitude)
                .append(",")
                .append(this.longitude)
                .append(",")
                .append(this.mAltitude)
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
        return rhs.latitude == this.latitude && rhs.longitude == this.longitude && rhs.mAltitude == this.mAltitude;
    }

    @Override
    public int hashCode() {
        return (int) (37.0 * (17.0 * latitude * 1E6d + longitude * 1E6d) + mAltitude);
    }

    // ===========================================================
    // Parcelable
    // ===========================================================
    private LatLng(final Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.mAltitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(mAltitude);
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
    public int distanceTo(final LatLng other) {

        final double a1 = DEG2RAD * this.latitude;
        final double a2 = DEG2RAD * this.longitude;
        final double b1 = DEG2RAD * other.latitude;
        final double b2 = DEG2RAD * other.longitude;

        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);

        final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);

        final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

        final double t3 = Math.sin(a1) * Math.sin(b1);

        final double tt = Math.acos(t1 + t2 + t3);

        return (int) (RADIUS_EARTH_METERS * tt);
    }

    /**
     * @return bearing in degrees
     * @see <a href="http://groups.google.com/group/osmdroid/browse_thread/thread/d22c4efeb9188fe9/bc7f9b3111158dd">discussion</a>
     */
    public double bearingTo(final LatLng other) {
        final double lat1 = Math.toRadians(this.latitude);
        final double long1 = Math.toRadians(this.longitude);
        final double lat2 = Math.toRadians(other.latitude);
        final double long2 = Math.toRadians(other.longitude);
        final double delta_long = long2 - long1;
        final double a = Math.sin(delta_long) * Math.cos(lat2);
        final double b = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(delta_long);
        final double bearing = Math.toDegrees(Math.atan2(a, b));
        final double bearing_normalized = (bearing + 360) % 360;
        return bearing_normalized;
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

    public String toDoubleString() {
        return new StringBuilder().append(this.latitude / 1E6).append(",")
                .append(this.longitude / 1E6).append(",").append(this.mAltitude).toString();
    }

    public String toInvertedDoubleString() {
        return new StringBuilder().append(this.longitude / 1E6).append(",")
                .append(this.latitude / 1E6).append(",").append(this.mAltitude).toString();
    }
}
