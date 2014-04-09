package com.mapbox.mapboxsdk.events;

import com.mapbox.mapboxsdk.views.MapView;

/**
 * The event generated when a map has finished scrolling to the coordinates (<code>x</code>,<code>y</code>).
 */
public class ScrollEvent implements MapEvent {
    protected MapView source;
    protected int x;
    protected int y;

    public ScrollEvent(final MapView aSource, final int ax, final int ay) {
        this.source = aSource;
        this.x = ax;
        this.y = ay;
    }

    /**
     * Return the map which generated this event.
     */
    public MapView getSource() {
        return source;
    }

    /**
     * Return the x-coordinate scrolled to.
     */
    public int getX() {
        return x;
    }

    /**
     * Return the y-coordinate scrolled to.
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "ScrollEvent [source=" + source + ", x=" + x + ", y=" + y + "]";
    }
}
