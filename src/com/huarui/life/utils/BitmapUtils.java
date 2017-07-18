package com.huarui.life.utils;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;

/**
 * Created by HR_Life on 2017/2/10 11:47.
 */

public class BitmapUtils {

	/**
	 * 清除指定的view的background;
	 *
	 * @param view
	 */
	public static void recycleBackground(View view) {
		if (view == null) {
			return;
		}
		try {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getBackground();
			view.setBackgroundResource(0);
			bitmapDrawable.setCallback(null);
			bitmapDrawable.getBitmap().recycle();
		} catch (Exception e) {

		}
	}
}
