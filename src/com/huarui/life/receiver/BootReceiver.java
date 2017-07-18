package com.huarui.life.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huarui.life.ui.activity.LaunchActivity;

import org.apache.log4j.Logger;


/**
 * Created by HR_Life on 2016/11/7 14:19 14:43 15:19.
 */

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "BootReceiver";

	/**
     * 对开机进行监听启动MainActivity!
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent launcher = new Intent(context, LaunchActivity.class);
            launcher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launcher);
            Logger.getLogger(TAG).info("Device power-on , app will launch ;");
        }
    }
}
