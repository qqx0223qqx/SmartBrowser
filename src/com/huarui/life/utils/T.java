package com.huarui.life.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huarui.life.R;
import com.huarui.life.config.BuildConfig;




/**
 * Created by HR_Life on 2017/4/12.
 * Toast管理类；
 */

public class T<T> {

    private T() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isShow = BuildConfig.isDebug;

    /**
     * 短时间显示Toast
     */
    public static void showShort(Context context, CharSequence message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     */
    public static void showShort(Context context, int message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(Context context, CharSequence message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     */
    public static void showLong(Context context, int message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(Context context, CharSequence message, int duration) {
        if (isShow)
            Toast.makeText(context, message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     */
    public static void show(Context context, int message, int duration) {
        if (isShow)
            Toast.makeText(context, message, duration).show();
    }

    /**
     * 自定义显示；
     */
    public static void showToast(Context context, String title, String msg, int time) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.custom_toast_tv_title);
        tvTitle.setText(TextUtils.isEmpty(title) ? "系统提示：" : title);
        TextView tvMsg = (TextView) view.findViewById(R.id.custom_toast_tv_msg);
        tvMsg.setText(msg);
        Toast toast = new Toast(context);

        toast.setDuration(time);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        toast.setGravity(Gravity.TOP, 0, (height * 2) / 9);
        toast.setMargin(0,0);
        toast.setView(view);
        toast.show();
    }
}
