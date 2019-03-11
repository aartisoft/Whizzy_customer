package com.sprvtec.whizzy.util;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.sprvtec.whizzy.ui.MapDragEditLocationActivity;

public class MapWrapperLayout extends FrameLayout {

    public interface OnDragListener {
//        void onWindowFocusChanged(boolean hasFocus);

        void onDrag(MotionEvent motionEvent);
    }

    private OnDragListener mOnDragListener;

    public MapWrapperLayout(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(ev);
            //override scroll with map scroll
            this.getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                MapDragEditLocationActivity.mMapIsTouched = true;
                break;

            case MotionEvent.ACTION_UP:
//                    MapDragEditLocationActivity.mMapIsTouched = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }
}