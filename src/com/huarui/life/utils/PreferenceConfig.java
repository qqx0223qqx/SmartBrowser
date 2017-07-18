package com.huarui.life.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by HR_Life on 2017/1/3 14:42 15:19.
 */

public class PreferenceConfig {


	/**
	 * 设置boolean
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 */
	public static void setBooleanConfig(Context context, String fileName, String key, boolean value) {
		context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).edit().putBoolean(key, value).apply();
	}

	/**
	 * 获取boolean类型数据
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static boolean getBooleanConfig(Context context, String fileName, String key) {
		return context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).getBoolean(key, false);
	}

	/**
	 * 获取Long 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 */
	public static void setLongConfig(Context context, String fileName, String key, Long value) {
		context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).edit().putLong(key,value).apply();
	}

	/**
	 * 获取Long 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 */
	public static Long getLongConfig(Context context, String fileName, String key) {
		return context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).getLong(key, -1L);
	}

	/**
	 * 设置String 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 */
	public static void setIntConfig(Context context, String fileName, String key, int value) {
		context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).edit().putInt(key, value).apply();
	}

	/**
	 * 获取String 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 */
	public static int getIntConfig(Context context, String fileName, String key) {
		return context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).getInt(key,-1);
	}

	/**
	 * 设置String 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 */
	public static void setConfig(Context context, String fileName, String key, String value) {
		context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).edit().putString(key, value).apply();
	}

	/**
	 * 获取String 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 */
	public static String getConfig(Context context, String fileName, String key) {
		return context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).getString(key, "");
	}

	/**
	 * 设置String 类型；
	 *
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 */
	public static void setConfigs(Context context, String fileName, String[] key, String[] value) {
		SharedPreferences.Editor editor = context.getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE).edit();
		for (int i = 0; i < key.length; i++) {
			editor.putString(key[i], value[i]);
		}
		editor.apply();
	}

}
