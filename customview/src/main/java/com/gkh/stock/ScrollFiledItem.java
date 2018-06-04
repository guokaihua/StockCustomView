package com.gkh.stock;

import java.util.ArrayList;
import java.util.List;

/**
 * 左滑listview的item
 *
 * @author:gkh
 * @date: 2017-11-28 16:54
 */

public class ScrollFiledItem {
    public int market;
    //证券类别
    public int zqlb;
    //委买价(买一价)
    public String buy;
    //委卖价(卖一价)
    public String sell;

    //委买量(买一量)
    public String bVollem;
    //委卖价(卖一量)
    public String sVollem;
    public List<StockItemData> filedList = new ArrayList<>();
}
