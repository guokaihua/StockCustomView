package com.gkh.stock;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:sf
 * @date: 2016-10-24 15:20
 */
public  class HVAdapter extends BaseAdapter {
    private WeakReference<Context> mContextRef;

    private HVListView mListView;

    private List<ScrollFiledItem> mDatas = new ArrayList<>();

    public HVAdapter(Context context, HVListView listview){
        mContextRef = new WeakReference<>(context);
        mListView = listview;
    }
    @Override
    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<ScrollFiledItem> data){
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    private int curExpandPos = -1;

    public void reset(){
        if (curExpandPos == -1)
            return;
        curExpandPos = -1;
        notifyDataSetChanged();
    }

    private final class ViewHolder{
        public HVItemView itemView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = new HVItemView(mContextRef.get(), mDatas.get(position).filedList.size());
            holder.itemView = (HVItemView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScrollFiledItem mScrollItem = mDatas.get(position);
        holder.itemView.setData(mDatas.get(position),mScrollItem.filedList,true);

        //校正（处理同时上下和左右滚动出现错位情况）
        View child = convertView.findViewById(R.id.ll_group);
        if (child!=null && mListView != null) {
            int head = mListView.getHeadScrollX();
            if (child.getScrollX() != head) {
                child.scrollTo(mListView.getHeadScrollX(), 0);
            }
        }

        return convertView;
    }
}
