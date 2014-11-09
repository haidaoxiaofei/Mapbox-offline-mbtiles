package com.mapbox.mapboxsdk.util;

import android.text.TextUtils;
import com.mapbox.mapboxsdk.constants.MapboxConstants;

public class MapboxUtils {

    public static String markerIconURL(String size, String symbol, String color)
    {
        // Make a string which follows the MapBox Core API spec for stand-alone markers. This relies on the MapBox API
        // for error checking.
        //
        StringBuffer marker = new StringBuffer("pin-");

        if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 'l')
        {
            marker.append("l"); // large
        }
        else if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 's')
        {
            marker.append("s"); // small
        }
        else
        {
            marker.append("m"); // default to medium
        }

        if (!TextUtils.isEmpty(symbol))
        {
            marker.append(String.format("-%s+", symbol));
        }
        else
        {
            marker.append("+");
        }

        marker.append(color.replaceAll("#", ""));

        // Get hi res version by default
        marker.append("@2x.png");

        // Using API 3 for now
        return String.format(MapboxConstants.MAPBOX_BASE_URL + "/marker/%s", marker);
    }
}
