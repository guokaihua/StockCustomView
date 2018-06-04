package com.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gkh.stock.HVScrollView;
import com.gkh.stock.HvListData;
import com.gkh.stock.ScrollFiledItem;
import com.gkh.stock.StockItemData;

import java.util.ArrayList;
import java.util.List;

public class SampleActivity extends AppCompatActivity {
    private HVScrollView mHvScrollView;
    private List<StockItemData> mMoveHead = new ArrayList<>();
    private HvListData hvListData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        mHvScrollView = findViewById(R.id.hvScrollView);

        initHeadGroupData();
        initData();
        init();
    }

    private void init() {
        mHvScrollView.setScrollFiledNum(3);
        //mHvScrollView.setIsShowFixTextView(true);
        mHvScrollView.setHeadGroupData(mMoveHead);
        mHvScrollView.loadListData(hvListData);
    }

    private static final  int filedNum = 10;
    private void initHeadGroupData() {
        for (int i = 0; i < filedNum; i++) {
            StockItemData item = new StockItemData();
            item.stockItem = "item" + i;
            mMoveHead.add(item);
        }
    }

    private void initData() {
        hvListData = new HvListData();
        hvListData.listData.clear();
        for (int i = 0; i < 30; i++) {
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

            for (int j = 0; j < filedNum; j++){
                StockItemData childItem = new StockItemData();
                childItem.stockItem = "行" + i + "列" + j;
                childItem.colorId = getResources().getColor(R.color.qlColorTextmain);
                if (j % 2 == 0){
                    childItem.bgColorId = getResources().getColor(com.gkh.stock.R.color.holo_green_light);
                } else {
                    childItem.bgColorId = getResources().getColor(com.gkh.stock.R.color.qlColorTextRed);
                }
                item.filedList.add(childItem);
            }
            hvListData.listData.add(item);
        }
        hvListData.totalNum = 60;
        hvListData.startPos = 0;
    }
}
