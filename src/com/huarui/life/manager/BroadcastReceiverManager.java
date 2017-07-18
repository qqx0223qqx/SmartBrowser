package com.huarui.life.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by wx_Life on 2017/2/9 15:48.
 * LocalBroadcastManager 只能应用内部发送广播，效率更高；
 */

public class BroadcastReceiverManager {

	private static LocalBroadcastManager broadcastManager;

	/**
	 * 初始化
	 */
	public static void init(Context context) {
		broadcastManager = LocalBroadcastManager.getInstance(context);
	}

	/**
	 * 注册广播；
	 */
	public static void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		broadcastManager.registerReceiver(receiver, filter);
	}

	/**
	 * 发送广播；
	 */
	public static void sendBroadcast(Intent intent) {
		broadcastManager.sendBroadcast(intent);
	}

	/**
	 * 发送同步广播；
	 *
	 * @param intent
	 */
	public static void sendBroadcastSync(Intent intent) {
		broadcastManager.sendBroadcastSync(intent);
	}

	/**
	 * 取消注册；
	 *
	 * @param receiver
	 */
	public static void unregisterReceiver(BroadcastReceiver receiver) {
		broadcastManager.unregisterReceiver(receiver);
	}
}
