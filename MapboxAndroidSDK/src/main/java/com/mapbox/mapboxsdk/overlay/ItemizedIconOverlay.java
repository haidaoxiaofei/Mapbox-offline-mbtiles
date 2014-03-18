package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.Display;
import com.mapbox.mapboxsdk.R;
import android.view.MotionEvent;

import android.view.WindowManager;
import com.mapbox.mapboxsdk.DefaultResourceProxyImpl;
import com.mapbox.mapboxsdk.ResourceProxy;
import com.mapbox.mapboxsdk.ResourceProxy.bitmap;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ItemizedIconOverlay<Item extends OverlayItem> extends ItemizedOverlay<Item> {

    protected final List<Item> mItemList;
    protected OnItemGestureListener<Item> mOnItemGestureListener;
    private int mDrawnItemsLimit = Integer.MAX_VALUE;
    private final Point mTouchScreenPoint = new Point();
    private final PointF mItemPoint = new PointF();

    private ItemizedIconOverlay<ClusterItem> clusters;
    private MapView view;
    private Context context;
    private boolean isClusterOverlay;
    
    private ClusterActions clusterActions;



    private boolean clusteringOn = true;

    public ItemizedIconOverlay(
            final List<Item> pList,
            final Drawable pDefaultMarker,
            final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener,
            final ResourceProxy pResourceProxy)
    {
        super(pDefaultMarker, pResourceProxy);

        this.mItemList = pList;
        this.mOnItemGestureListener = pOnItemGestureListener;
        populate();
    }

    public ItemizedIconOverlay(
            final List<Item> pList,
            final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener,
            final ResourceProxy pResourceProxy)
    {
        this(pList, pResourceProxy.getDrawable(bitmap.marker_default), pOnItemGestureListener, pResourceProxy);
    }

    public ItemizedIconOverlay(
            final Context pContext,
            final List<Item> pList,
            final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener)
    {
        this(pList, new DefaultResourceProxyImpl(pContext).getDrawable(bitmap.marker_default), pOnItemGestureListener, new DefaultResourceProxyImpl(pContext));
    }

    @Override
    public boolean onSnapToItem(final int pX, final int pY, final Point pSnapPoint, final MapView pMapView) {
        // TODO Implement this!
        return false;
    }

    @Override
    protected Item createItem(final int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return Math.min(mItemList.size(), mDrawnItemsLimit);
    }

    public boolean addItem(final Item item) {
        final boolean result = mItemList.add(item);
        populate();
        return result;
    }

    public void addItem(final int location, final Item item) {
        mItemList.add(location, item);
        populate();
    }

    public boolean addItems(final List<Item> items) {
        final boolean result = mItemList.addAll(items);
        populate();
        return result;
    }

    public void removeAllItems() {
        removeAllItems(true);
    }

    public void removeAllItems(final boolean withPopulate) {
        mItemList.clear();
        if (withPopulate) {
            populate();
        }
    }

    public boolean removeItem(final Item item) {
        final boolean result = mItemList.remove(item);
        populate();
        return result;
    }

    public Item removeItem(final int position) {
        final Item result = mItemList.remove(position);
        populate();
        return result;
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
                final ItemizedIconOverlay<Item> that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onSingleTapUpHelper(index, that.mItemList.get(index), mapView);
            }
        })) ? true : super.onSingleTapConfirmed(event, mapView);
    }

    protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
        return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay<Item> that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onLongPressHelper(index, getItem(index));
            }
        })) ? true : super.onLongPress(event, mapView);
    }

    protected boolean onLongPressHelper(final int index, final Item item) {
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
        final Projection pj = mapView.getProjection();
        final int eventX = (int) event.getX();
        final int eventY = (int) event.getY();

		/* These objects are created to avoid construct new ones every cycle. */
        pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);

        for (int i = 0; i < this.mItemList.size(); ++i) {
            final Item item = getItem(i);
            final Drawable marker = (item.getMarker(0) == null) ? this.mDefaultMarker : item
                    .getMarker(0);

            pj.toPixels(item.getPoint(), mItemPoint);

            if (hitTest(item, marker,(int) (mTouchScreenPoint.x - mItemPoint.x), (int)(mTouchScreenPoint.y
                    - mItemPoint.y))) {
                if (task.run(i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setClusterActions(ClusterActions clusterActions){
        this.clusterActions = clusterActions;
    }

    public void cluster(MapView view, Context context){
        if(!isClusteringOn()) return;
        this.view = view;
        this.context = context;
        int currentGroup = 0;
        final double CLUSTERING_THRESHOLD = getThreshold();
        clusterList = new ArrayList<ClusterItem>();
        for(OverlayItem item: this.mItemList){
            item.setClustered(false);
            item.assignGroup(0);
        }
        currentGroup++;
        for (OverlayItem item: this.mItemList) {
            if (item.getGroup() == 0) {
                item.assignGroup(currentGroup);
                item.setClustered(true);
                int counter = 0;
                for (OverlayItem item2: this.mItemList) {
                    if (item2.getGroup() == 0 && PointF.length(screenX(item) - screenX(item2), screenY(item) - screenY(item2)) <= CLUSTERING_THRESHOLD) {
                        item2.assignGroup(currentGroup);
                        item2.setClustered(true);
                        counter++;
                    }
                }
                if (counter == 0) { // If the item has no markers near it there is no sense in clustering it
                    item.setClustered(false);
                    item.assignGroup(0);
                }
            }
            currentGroup++;
        }
        getGroupSet();
        view.getOverlays().remove(clusters);
        if (clusters != null) {
            clusters.removeAllItems();
            initClusterOverlay();
            clusters.addItems(clusterList);
        } else {
            initClusterOverlay();
            clusters.addItems(clusterList);
        }

        view.getOverlays().add(clusters);
        view.invalidate();

    }


    private ArrayList<ClusterItem> clusterList = new ArrayList<ClusterItem>();


    private double getThreshold() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (android.os.Build.VERSION.SDK_INT >=13){
            display.getSize(size);
            return size.x/10;
        }
        else{
            return display.getWidth();
        }
    }

    private HashSet<Integer> getGroupSet(){
        HashSet<Integer> set = new HashSet<Integer>();
        for(OverlayItem element: mItemList){
            if(!set.contains(element.getGroup())){
                set.add(element.getGroup());
                generateCenterByGroup((ArrayList<OverlayItem>) mItemList, element.getGroup());
            }
        }
        return set;
    }

    private LatLng getCenter(ArrayList<OverlayItem> list){
        int total = list.size();

        double X = 0;
        double Y = 0;
        double Z = 0;

        for (OverlayItem i: list) {
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



    private void initClusterOverlay() {

        clusters = new ItemizedIconOverlay<ClusterItem>(clusterList, new ItemizedIconOverlay.OnItemGestureListener<ClusterItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, ClusterItem item) {
                if(clusterActions!=null){
                    clusterActions.onClusterTap(item);
                } else {
                    ArrayList<LatLng> activePoints = getCoordinateList(getGroupElements((List<OverlayItem>) mItemList, item.getGroup()));
                    view.zoomToBoundingBox(BoundingBox.fromGeoPoints(activePoints));
                }
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, ClusterItem item) {
                return false;
            }
        }, mResourceProxy);
        clusters.setCluster(true);
    }

    private LatLng generateCenterByGroup(ArrayList<OverlayItem> list, int group) {
        int sumlon = 0, sumlat = 0, count = 0;
        ArrayList<OverlayItem> tempList = getGroupElements(list, group);
        LatLng result = getCenter(tempList);
        ClusterItem m = new ClusterItem(view, result);
        m.setMarker(context.getResources().getDrawable(R.drawable.clusteri));
        m.assignGroup(group);
        m.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        m.setChildCount(tempList.size());
        if(m.getChildCount()>1){
            clusterList.add(m);
        }
        return result;
    }

    private ArrayList<OverlayItem> getGroupElements(List<OverlayItem> list, int group){
        ArrayList<OverlayItem> tempList = new ArrayList<OverlayItem>();
        for (OverlayItem element : list) {
            if (element.getGroup() == group) {
                tempList.add(element);
            }
        }
        return tempList;
    }

    private ArrayList<LatLng> getCoordinateList(List<OverlayItem> list){
        ArrayList<LatLng> theList = new ArrayList<LatLng>();
        for(OverlayItem element: list){
            theList.add(element.getPoint());
        }
        return theList;
    }



    private float screenX(OverlayItem item){
        return view.getProjection().toPixels(item.getPoint(), null).x;
    }

    private float screenY(OverlayItem item){
        return view.getProjection().toPixels(item.getPoint(), null).y;
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

    public boolean isClusterOverlay() {
        return isClusterOverlay;
    }

    public void setCluster(boolean cluster) {
        this.isClusterOverlay = cluster;
    }

    public boolean isClusteringOn(){
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

    public static interface ClusterActions{
        public ISafeCanvas onClusterMarkerDraw(ClusterItem item, ISafeCanvas canvas);
        public boolean onClusterTap(ClusterItem item);
    }
}
