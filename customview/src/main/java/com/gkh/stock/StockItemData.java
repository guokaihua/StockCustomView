package com.gkh.stock;

/***
 * 股票 数据 元素
 * @author gkh
 *
 */
public class StockItemData {
    /**
     * 股票子元素 数据值
     */
    public String stockItem;
    /**
     * 股票子元素  字体颜色资源ID
     */
    public int colorId;
    /**
     * 股票子元素  背景颜色资源ID
     */
    public int bgColorId = -2;
    /**
     * 与昨收比较  =：-1  >:0 <:-2
     */
    public int compareFlag = 0;

    public boolean isLight = false;
    public int leftType = -1;//0:灰  1:红 2：红买 3:绿 4：绿卖 -1:隐藏
    public int rightType = -1;
    public int requestFiled;

    public StockItemData() {

    }

    public StockItemData(String value, int color) {
        stockItem = value;
        colorId = color;
    }
}