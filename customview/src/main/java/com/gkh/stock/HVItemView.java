package com.gkh.stock;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HVItemView extends RelativeLayout {
    private final static String TAG = HVItemView.class.getSimpleName();

    private List<TextView> colTexts = new ArrayList<>();
    private WeakReference<Context> mContextRef;

    private TextView tv_zqmc, tv_zqdm;

    public HVItemView(Context context, int num) {
        super(context);
        mContextRef = new WeakReference<>(context);
        int defalutColor = getResources().getColor(R.color.qlColorTextmain);
        colTexts.clear();
        int screenW = ScreenUtils.getScreenWidth(context);
        int tvWidth = (screenW - DensityUtils.dip2px(context, 100)) / 3;
        View layout = LayoutInflater.from(context).inflate(R.layout.ql_item_hvlist, this, true);
        LinearLayout layout_group = layout.findViewById(R.id.ll_group);
        tv_zqmc = layout.findViewById(R.id.tv_zqmc);
        colTexts.add(tv_zqmc);
        tv_zqdm = layout.findViewById(R.id.tv_zqdm);
        colTexts.add(tv_zqdm);

        int spacing = DensityUtils.dip2px(mContextRef.get(), 5);
        for (int i = 0; i < num; i++) {
            TextView field = new TextView(context);
            LayoutParams lp = new LayoutParams(tvWidth, LayoutParams.WRAP_CONTENT);
            field.setTextSize(18);
            field.setPadding(0, spacing, 0, spacing);
            field.setGravity(Gravity.CENTER);
            field.setTextColor(defalutColor);
            layout_group.addView(field, lp);
            colTexts.add(field);
        }
    }


    public void setData(ScrollFiledItem stockItemData, List<StockItemData> itemData, boolean isShowBgColor) {
        try {
            int i = 0;
            for (TextView field : colTexts) {
                if (field != null) {
                    field.setText(itemData.get(i).stockItem);
                    if (itemData.get(i).colorId != 0) {
                        field.setTextColor(itemData.get(i).colorId);
                    }

                    if (isShowBgColor) {
                        field.setBackgroundColor(itemData.get(i).bgColorId);
                    }

                    i++;
                }
            }
        } catch (Exception e) {
        }
    }

    private onExpandTrendListener mListener = null;

    public interface onExpandTrendListener {
        void onExpand(boolean isExpand);
    }

    public void setOnExpandTrendListener(onExpandTrendListener listener) {
        this.mListener = listener;
    }
}
