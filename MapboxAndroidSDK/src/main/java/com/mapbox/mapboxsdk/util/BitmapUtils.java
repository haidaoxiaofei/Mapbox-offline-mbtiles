/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 3/9/14 at 2:50 PM
 */

package com.mapbox.mapboxsdk.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

import java.lang.reflect.Field;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class BitmapUtils {
    public static final int[] EXPIRED = new int[]{-1};

    public static BitmapFactory.Options getBitmapOptions(DisplayMetrics mDisplayMetrics) {
        try {
            // TODO I think this can all be done without reflection now because all these properties are SDK 4
            final Field density = DisplayMetrics.class.getDeclaredField("DENSITY_DEFAULT");
            final Field inDensity = BitmapFactory.Options.class.getDeclaredField("inDensity");
            final Field inTargetDensity = BitmapFactory.Options.class.getDeclaredField("inTargetDensity");
            final Field targetDensity = DisplayMetrics.class.getDeclaredField("densityDpi");
            final BitmapFactory.Options options = new BitmapFactory.Options();
            inDensity.setInt(options, density.getInt(null));
            inTargetDensity.setInt(options, targetDensity.getInt(mDisplayMetrics));
            return options;
        } catch (final IllegalAccessException ex) {
            // ignore
        } catch (final NoSuchFieldException ex) {
            // ignore
        }
        return null;
    }

    public static boolean isCacheDrawableExpired(Drawable drawable) {
        if (drawable != null &&
                drawable.getState() == EXPIRED) {
            return true;
        }
        return false;
    }

    public static void setCacheDrawableExpired(CacheableBitmapDrawable drawable) {
        if (drawable != null) {
            drawable.setState(EXPIRED);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    public static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        // Target ~15% of the available heap.
        return 1024 * 1024 * memoryClass / 7;
    }

}
