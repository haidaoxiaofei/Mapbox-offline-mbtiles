package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ItemizedIconOverlay extends ItemizedOverlay {

    protected final List<Marker> mItemList;
    protected OnItemGestureListener<Marker> mOnItemGestureListener;
    private int mDrawnItemsLimit = Integer.MAX_VALUE;
    private MapView view;
    private Context context;
    private ClusterActions clusterActions;
    private boolean clusteringOn = true;

    public ItemizedIconOverlay(
            final Context pContext,
            final List<Marker> pList,
            final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Marker> pOnItemGestureListener) {
        super();
        this.context = pContext;
        this.mItemList = pList;
        this.mOnItemGestureListener = pOnItemGestureListener;
        populate();
    }

    @Override
    public boolean onSnapToItem(final int pX, final int pY, final Point pSnapPoint, final MapView pMapView) {
        // TODO Implement this!
        return false;
    }

    @Override
    protected Marker createItem(final int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return Math.min(mItemList.size(), mDrawnItemsLimit);
    }

    public boolean addItem(final Marker item) {
        item.setParentHolder(this);
        final boolean result = mItemList.add(item);
        populate();
        return result;
    }

    public void addItem(final int location, final Marker item) {
        item.setParentHolder(this);
        mItemList.add(location, item);
        populate();
    }

    public boolean addItems(final List items) {
        for (Object item : items) {
            if (item instanceof Marker) {
                ((Marker) item).setParentHolder(this);
            }
        }
        final boolean result = mItemList.addAll(items);
        populate();
        return result;
    }

    public void removeAllItems() {
        removeAllItems(true);
    }

    public void removeAllItems(final boolean withPopulate) {
        for (Marker item : mItemList) {
            item.setParentHolder(null);
        }
        mItemList.clear();
        if (withPopulate) {
            populate();
        }
    }

    public boolean removeItem(final Marker item) {
        final boolean result = mItemList.remove(item);
        if (result) {
            item.setParentHolder(null);
        }
        populate();
        return result;
    }

    public Marker removeItem(final int position) {
        final Marker item = mItemList.remove(position);
        if (item != null) {
            item.setParentHolder(null);
        }
        populate();
        return item;
    }

    public void removeItems(final List items) {
        for (Object item : items) {
            if (item instanceof Marker) {
                final boolean result = mItemList.remove(item);
                if (result) {
                    ((Marker) item).setParentHolder(null);
                }
            }

        }
        populate();
    }

    /**
     * Each of these methods performs a item sensitive check. If the item is located its
     * corresponding method is called. The result of the call is returned.
     * <p/>
     * Helper methods are provided so that child classes may more easily override behavior without
     * resorting to overriding the ItemGestureListener methods.
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onSingleTapUpHelper(index, that.mItemList.get(index), mapView);
            }
        }));
    }

    protected boolean onSingleTapUpHelper(final int index, final Marker item, final MapView mapView) {
        if (item instanceof ClusterItem) {
            if (clusterActions != null) {
                clusterActions.onClusterTap((ClusterItem) item);
            } else {
                ArrayList<LatLng> activePoints = getCoordinateList(getGroupElements(mItemList, item.getGroup()));
                view.zoomToBoundingBox(BoundingBox.fromLatLngs(activePoints));
            }
            return false;
        } else {
            return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
        }
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onLongPressHelper(index, getItem(index));
            }
        }));
    }

    protected boolean onLongPressHelper(final int index, final Marker item) {
        return this.mOnItemGestureListener.onItemLongPress(index, item);
    }

    /**
     * When a content sensitive action is performed the content item needs to be identified. This
     * method does that and then performs the assigned task on that item.
     *
     * @param event
     * @param mapView
     * @param task
     * @return true if event is handled false otherwise
     */
    private boolean activateSelectedItems(final MotionEvent event, final MapView mapView,
                                          final ActiveItem task) {
        for (int i = 0; i < this.mItemList.size(); ++i) {
            final Marker item = getItem(i);
            if (item.beingClustered()) {
                continue;
            }
            RectF rect = item.getDrawingBounds(mapView.getProjection(), null);
            if (rect.contains(event.getX(), event.getY())) {
                if (task.run(i)) {
                    this.setFocus(item);
                    return true;
                }
            }
        }
        return false;
    }

    public void setClusterActions(ClusterActions clusterActions) {
        this.clusterActions = clusterActions;
    }

    public void clearCluster() {
        if (clusterList.size() > 0) {
            removeItems(clusterList);
            clusterList.clear();
        }
    }

    public void cluster(MapView view, Context context) {
        clearCluster();
        if (!isClusteringOn()) {
            return;
        }
        this.view = view;
        this.context = context;
        final double CLUSTERING_THRESHOLD = getThreshold();

        for (Marker item : this.mItemList) {
            item.assignGroup(-1);
        }
        int currentGroup = 0;
        for (Marker item : this.mItemList) {
            if (!item.beingClustered()) {
                int counter = 0;
                for (Marker item2 : this.mItemList) {
                    if (item2 != item && !item.beingClustered() && PointF.length(screenX(item) - screenX(item2), screenY(item) - screenY(item2)) <= CLUSTERING_THRESHOLD) {
                        item2.assignGroup(currentGroup);
                        item2.setClustered(true);
                        counter++;
                    }
                }
                if (counter > 0) {
                    item.setClustered(true);
                    item.assignGroup(currentGroup);
                    currentGroup++;
                }
            }
        }
        getGroupSet();
//        view.getOverlays().remove(clusters);
//        if (clusters != null) {
//            clusters.removeAllItems();
//            initClusterOverlay();
//            clusters.addItems(clusterList);
//        } else {
//            initClusterOverlay();
//            clusters.addItems(clusterList);
//        }

//        view.getOverlays().add(clusters);
        view.invalidate();

    }


    private List<ClusterItem> clusterList = new ArrayList<ClusterItem>();


    private double getThreshold() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(size);
            return size.x / 10;
        } else {
            return display.getWidth();
        }
    }

    private HashSet<Integer> getGroupSet() {
        HashSet<Integer> set = new HashSet<Integer>();
        for (Marker element : mItemList) {
            int group = element.getGroup();
            if (group >= 0 && !set.contains(group)) {
                set.add(group);
                generateCenterByGroup((ArrayList<Marker>) mItemList, group);
            }
        }
        addItems(clusterList);
        return set;
    }

    private LatLng getCenter(ArrayList<Marker> list) {
        int total = list.size();

        double X = 0;
        double Y = 0;
        double Z = 0;

        for (Marker i : list) {
            LatLng point = i.getPoint();
            double lat = point.getLatitude() * Math.PI / 180;
            double lon = point.getLongitude() * Math.PI / 180;

            double x = Math.cos(lat) * Math.cos(lon);
            double y = Math.cos(lat) * Math.sin(lon);
            double z = Math.sin(lat);

            X += x;
            Y += y;
            Z += z;
        }

        X = X / total;
        Y = Y / total;
        Z = Z / total;

        double Lon = Math.atan2(Y, X);
        double Hyp = Math.sqrt(X * X + Y * Y);
        double Lat = Math.atan2(Z, Hyp);

        return new LatLng(Lat * 180 / Math.PI, Lon * 180 / Math.PI);
    }

    private LatLng generateCenterByGroup(ArrayList<Marker> list, int group) {
        int sumlon = 0, sumlat = 0, count = 0;
        ArrayList<Marker> tempList = getGroupElements(list, group);
        LatLng result = getCenter(tempList);
        ClusterItem m = new ClusterItem(view, result);
        m.assignGroup(group);
        m.setChildCount(tempList.size());
        if (m.getChildCount() > 1) {
            clusterList.add(m);
        }
        return result;
    }

    private ArrayList<Marker> getGroupElements(List<Marker> list, int group) {
        ArrayList<Marker> tempList = new ArrayList<Marker>();
        for (Marker element : list) {
            if (!(element instanceof ClusterItem) && element.getGroup() == group) {
                tempList.add(element);
            }
        }
        return tempList;
    }

    private ArrayList<LatLng> getCoordinateList(List<Marker> list) {
        ArrayList<LatLng> theList = new ArrayList<LatLng>();
        for (Marker element : list) {
            theList.add(element.getPoint());
        }
        return theList;
    }


    private float screenX(Marker item) {
        return view.getProjection().toMapPixels(item.getPoint(), null).x;
    }

    private float screenY(Marker item) {
        return view.getProjection().toMapPixels(item.getPoint(), null).y;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public int getDrawnItemsLimit() {
        return this.mDrawnItemsLimit;
    }

    public void setDrawnItemsLimit(final int aLimit) {
        this.mDrawnItemsLimit = aLimit;
    }

    public boolean isClusteringOn() {
        return clusteringOn;
    }

    public void setClusteringOn(boolean clusteringOn) {
        this.clusteringOn = clusteringOn;
    }

    public ClusterActions getClusterActions() {
        return clusterActions;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * When the item is touched one of these methods may be invoked depending on the type of touch.
     * <p/>
     * Each of them returns true if the event was completely handled.
     */
    public static interface OnItemGestureListener<T> {
        public boolean onItemSingleTapUp(final int index, final T item);

        public boolean onItemLongPress(final int index, final T item);
    }

    public static interface ActiveItem {
        public boolean run(final int aIndex);
    }

    public static interface ClusterActions {
        public ISafeCanvas onClusterMarkerDraw(ClusterItem item, ISafeCanvas canvas);

        public boolean onClusterTap(ClusterItem item);
    }
}
