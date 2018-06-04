package com.gkh.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HVScrollView extends FrameLayout {
    private static final String TAG = HVScrollView.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mInflate;
    private int filedWidth = 0;
    private HVListView listview;
    private LinearLayout ll_fixhead;
    private LinearLayout ll_head_group;
    private HVAdapter mAdapter;
    private OnHeaderClickedListener mHeaderClicked;
    private boolean isSupportSort = false, isOnRefresh = true;

    private boolean isShowFixTextView = false;
    private int mSortState = 0;//0:无排序，1:升序 2：降序

    private TextView fixTextView;
    private TextView lastClickTextView;//上一次点击的TextView
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int requestNum = 30, responseNum = 0;
    private int mStartPos = 0;
    private OnRefreshListener mOnRefreshListener;
    private int sortFiled = 0;           //排序字段
    private int mVisiblePos = 0;        //屏幕可见起始
    private int mLoadStartPos = 0;
    private View mFooterView, mImageView;
    private boolean mLoading;
    private int mTotalItemCount;
    private boolean isRest = false;

    private static final int REQUEST_COUNT = 30;

    /**
     * 横向是否超出一屏可滑动
     */
    private boolean isCanScroll = false;
    private ImageView iv_loading;

    private List<ScrollFiledItem> mListData = new ArrayList<>();

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    private TextView textProgress;

    private int touchSlop;

    public HVScrollView(Context context) {
        super(context);
        init(context);

    }

    public HVScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mContext = context;
        mInflate = LayoutInflater.from(context);
        mFooterView = mInflate.inflate(R.layout.ql_view_list_loading, null);
        buidScrollListView();
    }

    /**
     * 绑定视图
     */
    private void buidScrollListView() {
        mInflate.inflate(R.layout.ql_layout_hvscroll_view, this, true);
        fixTextView = findViewById(R.id.tv_fixhead);
        listview = findViewById(R.id.listview);
        ll_fixhead = findViewById(R.id.ll_fixhead);
        ll_head_group = findViewById(R.id.ll_head_group);
        mImageView = findViewById(R.id.imageView);

        LinearLayout llHeader = findViewById(R.id.ll_header);

        mAdapter = new HVAdapter(mContext, listview);
        listview.setHeadLinearLayout(ll_head_group);
        listview.setAdapter(mAdapter);

        iv_loading = findViewById(R.id.iv_loading);
        textProgress = findViewById(R.id.textProgress);
    }

    public void addFootView(View footView) {
        if (listview == null)
            return;
        listview.addFooterView(footView);
    }

    /**
     * 设置可变的列表头信息
     *
     * @param moveHead 列表头显示的名称
     */
    public void setHeadGroupData(List<StockItemData> moveHead) {
        if (moveHead == null || moveHead.size() == 0)
            return;

        ll_head_group.removeAllViews();
        for (StockItemData itemData : moveHead) {
            LinearLayout llItem = new LinearLayout(mContext);
            llItem.setLayoutParams(new LinearLayout.LayoutParams(filedWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            llItem.setGravity(Gravity.CENTER);

            TextView tvItem = new TextView(mContext);
            DrawableUtils.setAroundDrawable(mContext,tvItem,0,0,R.mipmap.list_sort_normal,0);
            tvItem.setCompoundDrawablePadding(5);

            tvItem.setText(itemData.stockItem);
            tvItem.setTextColor(itemData.colorId != 0 ? itemData.colorId : getResources().getColor(R.color.qlColorTextGray));

            llItem.addView(tvItem);

            ll_head_group.addView(llItem);
        }
        listview.setScrollWidth(filedWidth * moveHead.size());

        isCanScroll = moveHead.size() <= 3 ? false : true;
    }

    /**
     * 设置不可变的列表头信息
     *
     * @param fixHead 列表头显示的名称
     */
    public void setFixHead(String fixHead) {
        fixTextView.setText(fixHead);
    }

    /**
     * 恢复到最初的位置
     */
    public void resetToDefaultPos() {
        listview.resetToDefaultPos();
    }

    /**
     * listView Item点击
     *
     * @param listener Item点击事件
     */
    public void setOnItemClickedListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * listView Item长按
     *
     * @param listener Item长按事件
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * 列头点击事件
     *
     * @param listener
     */
    public void setOnHeaderClickedListener(HVScrollView.OnHeaderClickedListener listener) {
        mHeaderClicked = listener;
    }

    /**
     * 是否显示固定头部
     *
     * @param showFixTextView
     */
    public void setIsShowFixTextView(boolean showFixTextView) {
        isShowFixTextView = showFixTextView;
        if (fixTextView != null) {
            fixTextView.setVisibility(showFixTextView ? View.VISIBLE : View.GONE);
        }
    }

    public void resetHVScrollView() {
        mSortState = 0;
        mStartPos = 0;
        mListData.clear();
        mAdapter.notifyDataSetChanged();
        isOnRefresh = false;
        sortFiled = 0;
        mVisiblePos = 0;
        isRest = true;
    }


    /**
     * 加载列表数据
     */
    public void loadListData(HvListData data) {
        if (data == null || mAdapter == null)
            return;
        //停止加载数据
        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            showLoading(false);
            int filedCount = data.listData.get(0).filedList.size();
            mListData.clear();
            for (int i = 0; i < data.totalNum; i++) {
                ScrollFiledItem item = initEmptyItemData(i,filedCount);
                mListData.add(item);
            }

            int i = 0;
            for (ScrollFiledItem item : data.listData) {
                mListData.set(data.startPos + i, item);
                i++;
            }
            mAdapter.setData(mListData);
            mLoadStartPos = data.startPos;
            if (isRest) {
                listview.setSelection(data.startPos);
                isRest = false;
            }
        }
    }

    private ScrollFiledItem initEmptyItemData(int i,int filedCount) {
        int defaultColorId = getResources().getColor(R.color.qlColorTextmain);
        ScrollFiledItem item = new ScrollFiledItem();
        item.market = 1;
        StockItemData mcItem = new StockItemData();
        mcItem.stockItem = "名称" + i;
        mcItem.colorId = getResources().getColor(R.color.qlColorTextWhite);
        mcItem.bgColorId = getResources().getColor(R.color.holo_blue_light);
        item.filedList.add(mcItem);

        StockItemData dmItem = new StockItemData();
        dmItem.stockItem = "代码" + i;
        dmItem.colorId = getResources().getColor(R.color.qlColorTextWhite);
        dmItem.bgColorId = getResources().getColor(R.color.holo_blue_light);
        item.filedList.add(dmItem);

        for (int j = 0; j < filedCount; j++) {
            StockItemData childItem = new StockItemData();
            childItem.stockItem = "行" + i + "列" + j;
            childItem.colorId = defaultColorId;
            if (j % 2 == 0){
                childItem.bgColorId = getResources().getColor(R.color.holo_green_light);
            }else {
                childItem.bgColorId = getResources().getColor(com.gkh.stock.R.color.qlColorTextRed);
            }
            item.filedList.add(childItem);
        }

        return item;
    }

    /**
     * 加载列表数据
     */

    public synchronized void loadListData(List<ScrollFiledItem> listData) {
        if (listData == null || mAdapter == null)
            return;
        responseNum = listData.size();
        showLoading(false);
        try {
            for (int i = 0; i < responseNum; i++) {
                if (mListData.size() > mVisiblePos + i) {
                    mListData.set(mVisiblePos + i, listData.get(i));
                } else {
                    mListData.add(listData.get(i));
                }
            }
        } catch (Exception e) {
        }
        mAdapter.setData(mListData);

        if (isRest) {
            listview.setSelection(0);
            isRest = false;
        }
    }

    public void setTextViewWidth(int filedWidth) {
        this.filedWidth = filedWidth;
    }

    /**
     * 设置可滑动部分，一屏幕显示的数据
     *
     * @param filedNum
     */
    public void setScrollFiledNum(int filedNum) {
        int screenW = ScreenUtils.getScreenWidth(mContext);
        this.filedWidth = (screenW - DensityUtils.dip2px(mContext, 100)) / filedNum;
    }

    private int mLastVisiblePos;

    private AbsListView.OnScrollListener mLoadMoreListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mScrollState = scrollState;
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (mLastVisiblePos >= (mLoadStartPos + REQUEST_COUNT) || mVisiblePos <= mLoadStartPos) {
                    if (mOnRefreshListener != null) {
                        requestNum = REQUEST_COUNT;
                        showLoading(true);

                        //最后一页小于30时 请求30条
                        if (mTotalItemCount <= REQUEST_COUNT) {
                            mVisiblePos = 0;
                        } else if (mTotalItemCount > REQUEST_COUNT && (mTotalItemCount - mVisiblePos < REQUEST_COUNT)) {
                            mVisiblePos = mTotalItemCount - REQUEST_COUNT;
                        }

                        mOnRefreshListener.onLoadMore(mVisiblePos, requestNum, sortFiled, mSortState);
                    }
                }
            }
            updateTextProgress(scrollState);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mLastVisiblePos = firstVisibleItem + visibleItemCount;
            mVisiblePos = firstVisibleItem;
            mTotalItemCount = totalItemCount;

            //L.i(TAG, "onScroll--->mVisiblePos:" + mVisiblePos + " mLastVisiblePos:" + mLastVisiblePos);
            int footViewCounts = listview.getFooterViewsCount();
            textProgress.setText((footViewCounts > 0 ? mLastVisiblePos - footViewCounts : mLastVisiblePos) + "/" + mListData.size());
        }
    };

    private void updateTextProgress(int scrollState) {
        textProgress.setVisibility(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ? GONE : VISIBLE);
    }

    /**
     * 显示正在加载
     */
    public void showLoading(boolean show) {
        if (mLoading == show)
            return;
        mLoading = show;
    }

    /**
     * 列头点击事件
     */
    public interface OnHeaderClickedListener {
        void onHeaderGroupClick(int headerID, int sortState, TextView textView);

        void onFixHeaderClick(View v);

    }

    /**
     * list itme点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long id);

    }

    /**
     * list itme 长按事件
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);

    }

    private boolean isScroll = false;
    private float downX, downY;
    /**
     * 事件分发冲突
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (Math.abs(downY - y) <  Math.abs(downX - x)
                        && Math.abs(downX - x) > touchSlop) {
                    isScroll = true;
                } else {
                    isScroll = false;
                }

                if (!isCanScroll && isScroll)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isScroll = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

   /* @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (Math.abs(downY - y) <  Math.abs(downX - x)
                        && Math.abs(downX - x) > touchSlop) {
                    isScroll = true;
                } else {
                    isScroll = false;
                }

                if (!isCanScroll && isScroll)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isScroll = false;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 加载更多数据事件
     */
    public interface OnRefreshListener {
        void onLoadMore(int start, int requestNum, int headerID, int sortState);
    }

    /**
     * 设置加载更多数据事件
     */
    public void setOnRefreshListener(OnRefreshListener mOnRefreshListener) {
        this.mOnRefreshListener = mOnRefreshListener;
    }

}


