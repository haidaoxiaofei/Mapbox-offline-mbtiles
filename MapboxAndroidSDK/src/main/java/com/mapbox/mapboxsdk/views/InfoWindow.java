package com.mapbox.mapboxsdk.views;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ExtendedOverlayItem;

public class InfoWindow {

    private MapView mMapView;
    private boolean mIsVisible;
    private View mView;

    static int mTitleId = 0,
            mDescriptionId = 0,
            mSubDescriptionId = 0,
            mImageId = 0;

    private static void setResIds(Context context){
        String packageName = context.getPackageName(); //get application package name
        mTitleId = context.getResources().getIdentifier("id/tooltip_title", null, packageName);
        mDescriptionId = context.getResources().getIdentifier("id/tooltip_description", null, packageName);
        mSubDescriptionId = context.getResources().getIdentifier("id/tooltip_subdescription", null, packageName);
        mImageId = context.getResources().getIdentifier("id/tooltip_image", null, packageName);
    }

    public InfoWindow(int layoutResId, MapView mapView) {
        mMapView = mapView;
        mIsVisible = false;
        ViewGroup parent = (ViewGroup) mapView.getParent();
        Context context = mapView.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(layoutResId, parent, false);

        if (mTitleId == 0) {
            setResIds(mapView.getContext());
        }

        // default behavior: close it when clicking on the tooltip:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    close();
                return true; //From Osmdroid 3.0.10, event is properly consumed.
            }
        });
    }

    /**
     * open the window at the specified position.
     * @param object the graphical object on which is hooked the view
     * @param position to place the window on the map
     * @param offsetX (&offsetY) the offset of the view to the position, in pixels.
     * This allows to offset the view from the object position.
     */
    public void open(Object object, LatLng position, int offsetX, int offsetY) {
        onOpen(object);
        MapView.LayoutParams lp = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT,
                position, MapView.LayoutParams.BOTTOM_CENTER,
                offsetX, offsetY);
        close(); //if it was already opened
        mMapView.addView(mView, lp);
        mIsVisible = true;
    }

    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup)mView.getParent()).removeView(mView);
            onClose();
        }
    }

    /**
     * Returns the Android view. This allows to set its content.
     * @return the Android view
     */
    public View getView() {
        return(mView);
    }

    public void onOpen(Object item) {
        ExtendedOverlayItem extendedOverlayItem = (ExtendedOverlayItem) item;
        String title = extendedOverlayItem.getTitle();
        if (title == null) {
            title = "";
        }
        ((TextView) mView.findViewById(mTitleId /*R.id.title*/)).setText(title);
        String snippet = extendedOverlayItem.getDescription();
        if (snippet == null) {
            snippet = "";
        }
        ((TextView) mView.findViewById(mDescriptionId /*R.id.description*/)).setText(snippet);
        //handle sub-description, hidding or showing the text view:
        TextView subDescText = (TextView)mView.findViewById(mSubDescriptionId);
        String subDesc = extendedOverlayItem.getSubDescription();
        if (subDesc != null && !("".equals(subDesc))){
            subDescText.setText(subDesc);
            subDescText.setVisibility(View.VISIBLE);
        } else {
            subDescText.setVisibility(View.GONE);
        }
    }

    public void onClose() {
        //by default, do nothing
    }
}
 
