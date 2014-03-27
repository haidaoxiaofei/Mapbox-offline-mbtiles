// Created by plusminus on 23:18:23 - 02.10.2008
package com.mapbox.mapboxsdk.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas.UnsafeCanvasHandler;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;

/**
 * Draws a list of {@link Marker} as markers to a map. The item with the lowest index is drawn
 * as last and therefore the 'topmost' marker. It also gets checked for onTap first. This class is
 * generic, because you then you get your custom item-class passed back in onTap().
 *
 * @param <Item>
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public abstract class ItemizedOverlay<Item extends Marker> extends SafeDrawOverlay implements
        Overlay.Snappable {

    private final ArrayList<Item> mInternalItemList;
//    private final Rect mRect = new Rect();
    protected final PointF mCurScreenCoords = new PointF();
    protected boolean mDrawFocusedItem = true;
    private Item mFocusedItem;
    private boolean mPendingFocusChangedEvent = false;
    private OnFocusChangeListener mOnFocusChangeListener;

    private static SafePaint mClusterTextPaint;

    /**
     * Method by which subclasses create the actual Items. This will only be called from populate()
     * we'll cache them for later use.
     */
    protected abstract Item createItem(int i);

    /**
     * The number of items in this overlay.
     */
    public abstract int size();

    public ItemizedOverlay() {

        super();

        if (mClusterTextPaint == null) {
            mClusterTextPaint = new SafePaint();

            mClusterTextPaint.setTextAlign(Paint.Align.CENTER);
            mClusterTextPaint.setTextSize(30);
            mClusterTextPaint.setFakeBoldText(true);
        }


        mInternalItemList = new ArrayList<Item>();
    }

    private Rect getBounds(View view) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();
        return new Rect(x, y, x + w, y + h);
    }

    /**
     * Draw a marker on each of our items. populate() must have been called first.<br/>
     * <br/>
     * The marker will be drawn twice for each Item in the Overlay--once in the shadow phase, skewed
     * and darkened, then again in the non-shadow phase. The bottom-center of the marker will be
     * aligned with the geographical coordinates of the Item.<br/>
     * <br/>
     * The order of drawing may be changed by overriding the getIndexToDraw(int) method. An item may
     * provide an alternate marker via its Marker.getMarker(int) method. If that method returns
     * null, the default marker is used.<br/>
     * <br/>
     * The focused item is always drawn last, which puts it visually on top of the other items.<br/>
     *
     * @param canvas  the Canvas upon which to draw. Note that this may already have a transformation
     *                applied, so be sure to leave it the way you found it
     * @param mapView the MapView that requested the draw. Use MapView.getProjection() to convert
     *                between on-screen pixels and latitude/longitude pairs
     * @param shadow  if true, draw the shadow layer. If false, draw the overlay contents.
     */
    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {

        if (shadow) {
            return;
        }

        if (mPendingFocusChangedEvent && mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChanged(this, mFocusedItem);
        }
        mPendingFocusChangedEvent = false;

        final Projection pj = mapView.getProjection();
        final int size = this.mInternalItemList.size() - 1;

        final Rect bounds = getBounds(mapView);

		/* Draw in backward cycle, so the items with the least index are on the front. */
        for (int i = size; i >= 0; i--) {
            final Item item = getItem(i);
            pj.toMapPixels(item.getPoint(), mCurScreenCoords);
            Point roundedCoords = new Point((int)mCurScreenCoords.x, (int)mCurScreenCoords.y);
            PointF onScreen = pj.toPixels(item.getPoint(), null);
            Point anchor = item.getMarkerAnchor();
            onScreen.offset(anchor.x, anchor.y);
            if (!bounds.contains((int)onScreen.x, (int)onScreen.y)) {
                //dont draw item if offscreen
                continue;
            }
            canvas.save();

            canvas.scale(1/mapView.getScale(), 1/mapView.getScale(), mCurScreenCoords.x,
            		mCurScreenCoords.y);

            onDrawItem(canvas, item, roundedCoords, mapView.getMapOrientation());
            canvas.restore();
       }
    }

    /**
     * Utility method to perform all processing on a new ItemizedOverlay. Subclasses provide Items
     * through the createItem(int) method. The subclass should call this as soon as it has data,
     * before anything else gets called.
     */
    protected final void populate() {
        final int size = size();
        mInternalItemList.clear();
        mInternalItemList.ensureCapacity(size);
        for (int a = 0; a < size; a++) {
            mInternalItemList.add(createItem(a));
        }
    }

    /**
     * Returns the Item at the given index.
     *
     * @param position the position of the item to return
     * @return the Item of the given index.
     */
    public final Item getItem(final int position) {
        return mInternalItemList.get(position);
    }

    /**
     * Draws an item located at the provided screen coordinates to the canvas.
     *
     * @param canvas          what the item is drawn upon
     * @param item            the item to be drawn
     * @param curScreenCoords
     * @param aMapOrientation
     */
    protected void onDrawItem(ISafeCanvas canvas, final Item item, final Point curScreenCoords, final float aMapOrientation) {
        if(item.beingClustered()){
            return;
        }
        final int state = (mDrawFocusedItem && (mFocusedItem == item) ? Marker.ITEM_STATE_FOCUSED_MASK
                : 0);
        final Drawable marker = item.getMarker(state);
        if (marker == null) return;
        final Point point = item.getMarkerAnchor();

        // draw it
        if (this.isUsingSafeCanvas()) {
            Overlay.drawAt(canvas.getSafeCanvas(), marker, curScreenCoords, point, false, aMapOrientation);
        } else {
            canvas.getUnsafeCanvas(new UnsafeCanvasHandler() {
                @Override
                public void onUnsafeCanvas(Canvas canvas) {
                    Overlay.drawAt(canvas, marker, curScreenCoords, point, false, aMapOrientation);
                }
            });
        }

        if(item instanceof ClusterItem){

            if(((ItemizedIconOverlay)this).getClusterActions()!=null){
                canvas = ((ItemizedIconOverlay)this)
                        .getClusterActions()
                        .onClusterMarkerDraw((ClusterItem) item, canvas);
            }
            else{
                String text = String.valueOf(((ClusterItem)item).getChildCount());
                Rect rectText = new Rect();
                mClusterTextPaint.getTextBounds(text, 0, text.length(), rectText);
                canvas.drawText(text,curScreenCoords.x - rectText.left, curScreenCoords.y - rectText.top - rectText.height()/2, mClusterTextPaint);
            }
        }

    }

    /**
     * See if a given hit point is within the bounds of an item's marker. Override to modify the way
     * an item is hit tested. The hit point is relative to the marker's bounds. The default
     * implementation just checks to see if the hit point is within the touchable bounds of the
     * marker.
     *
     * @param item   the item to hit test
     * @param x  x coordinates of the point to check
     * @param y  y coordinates of the point to check
     * @return true if the hit point is within the marker
     */
    protected boolean hitTest(final Item item, final float x, final float y) {
        return x > mCurScreenCoords.x &&
                x < mCurScreenCoords.x + item.getWidth() &&
                y > mCurScreenCoords.y &&
                y < mCurScreenCoords.y + item.getHeight();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        final int size = this.size();

        for (int i = 0; i < size; i++) {
            final Item item = getItem(i);
            item.getPositionOnScreen(mapView, mCurScreenCoords);
            if (hitTest(item, e.getX(), e.getY())) {
                // We have a hit, do we get a response from onTap?
                if (onTap(i)) {
                    // We got a response so consume the event
                    return true;
                }
            }
        }

        return super.onSingleTapConfirmed(e, mapView);
    }

    /**
     * Override this method to handle a "tap" on an item. This could be from a touchscreen tap on an
     * onscreen Item, or from a trackball click on a centered, selected Item. By default, does
     * nothing and returns false.
     *
     * @return true if you handled the tap, false if you want the event that generated it to pass to
     *         other overlays.
     */
    protected boolean onTap(int index) {
        return false;
    }

    /**
     * Set whether or not to draw the focused item. The default is to draw it, but some clients may
     * prefer to draw the focused item themselves.
     */
    public void setDrawFocusedItem(final boolean drawFocusedItem) {
        mDrawFocusedItem = drawFocusedItem;
    }

    /**
     * If the given Item is found in the overlay, force it to be the current focus-bearer. Any
     * registered {@link ItemizedOverlay} will be notified. This does not move
     * the map, so if the Item isn't already centered, the user may get confused. If the Item is not
     * found, this is a no-op. You can also pass null to remove focus.
     */
    public void setFocus(final Item item) {
        mPendingFocusChangedEvent = item != mFocusedItem;
        mFocusedItem = item;
    }

    /**
     * @return the currently-focused item, or null if no item is currently focused.
     */
    public Item getFocus() {
        return mFocusedItem;
    }

//    /**
//     * Adjusts a drawable's bounds so that (0,0) is a pixel in the location described by the anchor
//     * parameter. Useful for "pin"-like graphics. For convenience, returns the same drawable that
//     * was passed in.
//     *
//     * @param marker  the drawable to adjust
//     * @param anchor the anchor for the drawable (float between 0 and 1)
//     * @return the same drawable that was passed in.
//     */
//    protected synchronized Drawable boundToHotspot(final Drawable marker, Point anchor) {
//        final int markerWidth = marker.getIntrinsicWidth();
//        final int markerHeight = marker.getIntrinsicHeight();
//
//        mRect.set(0, 0, markerWidth, markerHeight);
//        mRect.offset(anchor.x, anchor.y);
//        marker.setBounds(mRect);
//        return marker;
//    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    public static interface OnFocusChangeListener {
        void onFocusChanged(ItemizedOverlay<?> overlay, Marker newFocus);
    }
}
