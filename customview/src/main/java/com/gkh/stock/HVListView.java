package com.gkh.stock;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
/**
 * 自定义支持横向滚动的ListView
 *
 * @author guokaihua
 */
public class HVListView extends ListView {
    public static int MODE_DOWN = 1;
    public static int MODE_LONGDOWN = 2;
    public static int MODE_HDRAG = 3;
    public static String TAG = HVListView.class.getSimpleName();
    /**
     * 手势
     */
    private GestureDetector mGesture;
    /**
     * 列头
     */
    public LinearLayout mListHead;
    /**
     * 偏移坐标
     */
    private int mOffset = 0;
    /**
     * 屏幕宽度
     */
    private int screenWidth;

    public int mode = MODE_DOWN;

    private int m_ScrollWidth;
    float down_x = 0, down_y = 0;
    private boolean isVerticalScroll = false;
    private boolean isHorizontalScroll = false;
    // Scroll
    private final int SCROLL_MIN_DISTANCE = 25;    // Unit:pixel/s

    // Fling
    private final int FLING_MIN_DISTANCE = 50;    // Unit:pixel/s
    private final int FLING_MIN_VELOCITY = 50;  // Unit:pixel/s

    private final float FLING_VELOCITY_UP_CRITICAL = -3000.0f;        // Unit:pixel/s
    private final float FLING_VELOCITY_DOWN_CRITICAL = 3000.0f;    // Unit:pixel/s

    private final float VELOCITY_FACTOR = 1.05f;
    private float mVx = 0.0f, mVx_last = 0.0f;
    private MotionEvent me1, me2;    // record the motion event in the fling function
    private FlingThread flingThread;
    private boolean mInterrupted = false;

    private long mStartTime;
    private float mDeceleration;
    private int ll_group;

    Handler mHandler;
    private OnHVScrollListener mOnHVScrollListener;

    /**
     * 构造函数
     */
    public HVListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGesture = new GestureDetector(context, mOnGesture);
        float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = SensorManager.GRAVITY_EARTH   // g (m/s^2)
                * 39.37f                        // inch/meter
                * ppi                           // pixels per inch
                * ViewConfiguration.getScrollFriction();
        initHandler();
    }

    /**
     * 分发触摸事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGesture.onTouchEvent(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                down_x = ev.getX();
                down_y = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float move_x = ev.getX() - down_x;
                float move_y = ev.getY() - down_y;
//                down_x = ev.getX();
//                down_y = ev.getY();
                if(Math.abs(move_y) > 25 && Math.abs(move_y) >= Math.abs(move_x)){
                    if (!isHorizontalScroll) {
                        isVerticalScroll = true;
                    }
                }else if(Math.abs(move_x) > 25 && Math.abs(move_y) <= Math.abs(move_x)){
                    if (!isVerticalScroll) {
                        isHorizontalScroll = true;
                    }
                }

                if (isHorizontalScroll)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
                isVerticalScroll = false;
                isHorizontalScroll = false;
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // process incoming messages here
                switch (msg.what) {
                    case 200: {
                        if (mVx_last == msg.arg1)
                            break;

                        int distanceX = -msg.arg1;

                        procGestureMove(distanceX);
                        mVx_last = msg.arg1;
                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /**
     * 手势
     */
    private GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            mode = MODE_DOWN;
            mInterrupted = true;
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mode = MODE_LONGDOWN;
            mInterrupted = true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            mStartTime = AnimationUtils.currentAnimationTimeMillis();

            //计算滑动的距离
            int dx = (int) (e2.getX() - e1.getX());

            //降噪处理，必须有较大的动作才识别
            if (Math.abs(dx) > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                mInterrupted = false;
                mVx = velocityX;
                if (mVx > FLING_VELOCITY_DOWN_CRITICAL)
                    mVx = FLING_VELOCITY_DOWN_CRITICAL;
                else if (mVx < FLING_VELOCITY_UP_CRITICAL)
                    mVx = FLING_VELOCITY_UP_CRITICAL;

                if (flingThread == null && !isVerticalScroll && isHorizontalScroll) {
                    mVx_last = 0;
                    flingThread = new FlingThread();
                    flingThread.start();
                }
            }
            return true;
        }

        /** 滚动 */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float distance_x = Math.abs(e2.getX() - e1.getX());
            if (distance_x > 25 && !isVerticalScroll && isHorizontalScroll) {
                procGestureMove((int) distanceX);
            }
            return true;
        }
    };


    private void procGestureMove(int distanceX) {
//        L.d("HVListView", "procGestureMove--->this.getChildCount() = " + this.getChildCount());
        try {
            if (getFooterViewsCount() > 0 && getChildCount() == 1) {
                Log.e(TAG, "procGestureMove--->getFooterViewsCount() = " + getFooterViewsCount());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "procGestureMove--->Exception 1");
            return;
        }

        try {
            if (((ViewGroup) getChildAt(0)) == null) {
                Log.e(TAG, "procGestureMove--->item1 = null");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "procGestureMove--->Exception 2");
            return;
        }
        synchronized (HVListView.this) {

            View itmeView = getChildAt(0);
            if (itmeView != null) {
                ViewGroup temp_group = (ViewGroup) (itmeView.findViewById(ll_group));
                int temp_child_count = temp_group.getChildCount();
                if (temp_child_count <= 3) {
                    return;
                }
            }
            int moveX = (int) distanceX;
            int curX = mListHead.getScrollX();
            int scrollWidth = m_ScrollWidth;
            int dx = moveX;
            //L.i(TAG, "curX = " + curX);
            //控制越界问题
            if (curX + moveX < 0) {
                dx = -curX;
            }

            if (curX + moveX + getScreenWidth() > scrollWidth) {
                dx = scrollWidth - getScreenWidth() - curX;
//                L.i(TAG, "2 curX = " + curX + ", moveX = " + moveX + ", getScreenWidth() = " + getScreenWidth() + ", scrollWidth = " + scrollWidth);
            } else {
//                L.i(TAG, "3 curX = " + curX + ", moveX = " + moveX + ", getScreenWidth() = " + getScreenWidth() + ", scrollWidth = " + scrollWidth);
            }

//			bScrolling	 = dx>10;
//			L.i("HVList", "bScrolling = " + bScrolling);
            mode = MODE_HDRAG;

            mOffset += dx;
            //L.i(TAG, "mOffset = " + mOffset);
            if(mOnHVScrollListener != null){
                mOnHVScrollListener.onHVScroll(mOffset);
            }
            //根据手势滚动Item视图
            for (int i = 0, j = getChildCount(); i < j; i++) {
                View child = getChildAt(i).findViewById(ll_group);
                if (child != null) {
                    if (child.getScrollX() != mOffset)
                        child.scrollTo(mOffset, 0);
                }
            }
            mListHead.scrollBy(dx, 0);
            //L.i(TAG, "dx = " + dx);

        }
        //requestLayout();
    }

    /**
     * 获取屏幕可见范围内最大屏幕
     *
     * @return
     */
    public int getScreenWidth() {
        if (screenWidth == 0) {
            screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            if (getChildAt(0) != null) {
                //screenWidth -= ((ViewGroup) getChildAt(0)).findViewById(R.id.ll_head).getMeasuredWidth();
                screenWidth -= 100;
            } else if (mListHead != null) {
                //减去固定第一列
                screenWidth -= mListHead.getChildAt(0).getMeasuredWidth();
            }
        }
        return screenWidth;
    }

    /**
     * 获取列头偏移量
     */
    public int getHeadScrollX() {
        return mListHead.getScrollX();
    }

    /**
     * 设置头部联动布局
     */
    public void setHeadLinearLayout(LinearLayout mListHead) {
        this.mListHead = mListHead;
    }

    /**
     * 设置头部可滑动的宽度
     */
    public void setScrollWidth(int width) {
        this.m_ScrollWidth = width;
    }

    public void resetToDefaultPos() {
        for (int i = 0, j = getChildCount(); i < j; i++) {
            View child = ((ViewGroup) getChildAt(i)).findViewById(ll_group);
            if (child != null) {
                child.scrollTo(0, 0);
                child.scrollBy(0, 1);
            }
        }
        mListHead.scrollTo(0, 0);
        mOffset = 0;
    }

    private class FlingThread extends Thread {
        public FlingThread() {
        }

        public void resetValue() {
            mVx = 0;
            flingThread = null;
        }

        public void run() {
            while (true) {
                if (mInterrupted) {
                    resetValue();
                    break;
                }

                try {
                    sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);

                float timePassedSeconds = timePassed / 1000.0f;
                //速度*时间差=距离
                //加速度*时间差**时间差/2=距离
                float distance = (mVx * timePassedSeconds) - (mDeceleration * timePassedSeconds * timePassedSeconds / 2.0f);
                if (mVx > 0) {
                    mVx = mVx - mDeceleration * timePassedSeconds;
                    if (mVx < -0.1f) {
                        resetValue();
                        break;
                    }
                } else {
                    mVx = mVx + mDeceleration * timePassedSeconds;
                    if (mVx > 0.1f) {
                        resetValue();
                        break;
                    }
                }
                if (mHandler != null) {
                    mHandler.obtainMessage(200, (int) (distance), 0).sendToTarget();
                }
            }
        }
    }

    public void setOnHVScrollListener(OnHVScrollListener mOnHVScrollListener) {
        this.mOnHVScrollListener = mOnHVScrollListener;
    }

    public interface OnHVScrollListener{
        void onHVScroll(int mOffset);
    }
}
