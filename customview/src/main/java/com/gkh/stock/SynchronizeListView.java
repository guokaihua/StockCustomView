package com.gkh.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.List;

/**
 *
 * @author guokaihua
 */
public class SynchronizeListView extends ListView implements AbsListView.OnScrollListener{
    private static final String TAG = SynchronizeListView.class.getSimpleName();
    private List<SynchronizeListView> mSynchronizeListViews;
    public boolean isSynchronizeScroll = false;

    public SynchronizeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SynchronizeListView(Context context, AttributeSet attrs) {
       this(context,attrs,0);
    }

    public SynchronizeListView(Context context) {
        this(context,null);
    }

    private void init() {
        setOnScrollListener(this);
    }

    public void setSynchronizationListViews(List<SynchronizeListView> synchronizeListViews){
        this.mSynchronizeListViews = synchronizeListViews;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mSynchronizeListViews == null || !isSynchronizeScroll)
            return;

        int firstVisibleTop = 0;
        if (view.getChildAt(0) != null) {
            firstVisibleTop = view.getChildAt(0).getTop();
        }

        for(SynchronizeListView synchronizeListView : mSynchronizeListViews){
            if(synchronizeListView != view)
                synchronizeListView.setSelectionFromTop(firstVisibleItem, firstVisibleTop);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if(!isSynchronizeScroll){
                isSynchronizeScroll = true;
                if(mSynchronizeListViews != null ) {
                    for (SynchronizeListView synchronizeListView : mSynchronizeListViews) {
                        if (synchronizeListView != SynchronizeListView.this)
                            synchronizeListView.isSynchronizeScroll = false;
                    }
                }
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_CANCEL && ev.getHistorySize() == 0) {
            if(!isSynchronizeScroll){
                isSynchronizeScroll = true;
                if(mSynchronizeListViews != null ) {
                    for (SynchronizeListView synchronizeListView : mSynchronizeListViews) {
                        if (synchronizeListView != SynchronizeListView.this)
                            synchronizeListView.isSynchronizeScroll = false;
                    }
                }
            }
        }

        return super.onTouchEvent(ev);
    }
}
