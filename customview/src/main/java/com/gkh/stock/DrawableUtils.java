package com.gkh.stock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.RadioButton;
import android.widget.TextView;
/**
 * @author:gkh
 * @date: 2015-12-22 13:48
 */
public class DrawableUtils {
    public static void setAroundDrawable(Context context, TextView mView, int left, int top, int right, int bottom){
        if (mView == null)
            return;
        if  (Build.VERSION.SDK_INT >= 17)
            mView.setCompoundDrawablesRelativeWithIntrinsicBounds(left, top, right, bottom);
        else {
            Drawable leftdrawable = left == 0 ? null : ContextCompat.getDrawable(context,left);
            Drawable topDrawable = top == 0 ? null : ContextCompat.getDrawable(context,top);
            Drawable rightDrawable = right == 0 ? null : ContextCompat.getDrawable(context,right);
            Drawable bottomDrawable = bottom == 0 ? null : ContextCompat.getDrawable(context,bottom);
            if (leftdrawable != null)
                setAroundDrawableBounds(leftdrawable);

            if (topDrawable != null)
                setAroundDrawableBounds(topDrawable);

            if (rightDrawable != null)
                setAroundDrawableBounds(rightDrawable);

            if (bottomDrawable != null)
                setAroundDrawableBounds(bottomDrawable);

            mView.setCompoundDrawables(leftdrawable,topDrawable,rightDrawable,bottomDrawable);
        }
    }

    public static void setRigthDrawable(Context context, TextView mView, int right){
        if (mView == null)
            return;
        setAroundDrawable(context,mView, 0, 0 , right, 0);
    }

    public static void setAroundDrawable(Context context, RadioButton mView, int left, int top, int right, int bottom){
        if (mView == null)
            return;
        if  (Build.VERSION.SDK_INT >= 17)
            mView.setCompoundDrawablesRelativeWithIntrinsicBounds(left, top, right, bottom);
        else {
            Drawable leftdrawable = left == 0 ? null : ContextCompat.getDrawable(context,left);
            Drawable topDrawable = top == 0 ? null : ContextCompat.getDrawable(context,top);
            Drawable rightDrawable = right == 0 ? null : ContextCompat.getDrawable(context,right);
            Drawable bottomDrawable = bottom == 0 ? null : ContextCompat.getDrawable(context,bottom);
            if (leftdrawable != null)
                setAroundDrawableBounds(leftdrawable);

            if (topDrawable != null)
                setAroundDrawableBounds(topDrawable);

            if (rightDrawable != null)
                setAroundDrawableBounds(rightDrawable);

            if (bottomDrawable != null)
                setAroundDrawableBounds(bottomDrawable);

            mView.setCompoundDrawables(leftdrawable,topDrawable,rightDrawable,bottomDrawable);
        }
    }

    private static void setAroundDrawableBounds(Drawable drawable){
        drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
    }
}
