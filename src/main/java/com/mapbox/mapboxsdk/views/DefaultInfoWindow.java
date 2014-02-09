package com.mapbox.mapboxsdk.views;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.mapbox.mapboxsdk.overlay.ExtendedOverlayItem;

public class DefaultInfoWindow extends InfoWindow {
    static int mTitleId=0, mDescriptionId=0, mSubDescriptionId=0, mImageId=0; //resource ids
    private static void setResIds(Context context){
        String packageName = context.getPackageName(); //get application package name
        mTitleId = context.getResources().getIdentifier("id/tooltip_title", null, packageName);
        mDescriptionId = context.getResources().getIdentifier("id/tooltip_description", null, packageName);
        mSubDescriptionId = context.getResources().getIdentifier("id/tooltip_subdescription", null, packageName);
        mImageId = context.getResources().getIdentifier("id/tooltip_image", null, packageName);
    }
    public DefaultInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        if (mTitleId == 0)
            setResIds(mapView.getContext());
        //default behavior: close it when clicking on the tooltip:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    close();
                return true; //From Osmdroid 3.0.10, event is properly consumed.
            }
        });
    }
    @Override public void onOpen(Object item) {
        ExtendedOverlayItem extendedOverlayItem = (ExtendedOverlayItem)item;
        String title = extendedOverlayItem.getTitle();
        if (title == null)
            title = "";
        ((TextView)mView.findViewById(mTitleId /*R.id.title*/)).setText(title);
        String snippet = extendedOverlayItem.getDescription();
        if (snippet == null)
            snippet = "";
        ((TextView)mView.findViewById(mDescriptionId /*R.id.description*/)).setText(snippet);
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
    @Override public void onClose() {
        //by default, do nothing
    }
}
 
