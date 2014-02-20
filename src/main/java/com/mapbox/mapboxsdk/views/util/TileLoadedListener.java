/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/5/14 at 8:02 PM
 */

package com.mapbox.mapboxsdk.views.util;

import android.graphics.drawable.Drawable;

public interface TileLoadedListener {
    public Drawable onTileLoaded(Drawable pDrawable);
}
