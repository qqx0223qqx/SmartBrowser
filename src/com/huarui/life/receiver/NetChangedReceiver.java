package com.huarui.life.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by HR_Life on 2017/5/16 : 18:35.
 * Package : com.hr.life.trnfa.receiver
 */

public class NetChangedReceiver extends BroadcastReceiver {

    public static final String TAG = "NetChangedReceiver";

    public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION.equalsIgnoreCase(intent.getAction())) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo == null || (!activeNetworkInfo.isConnected()) || (!activeNetworkInfo.isAvailable())) {
               /* Intent turnIntent = new Intent(ConsAction.ACTION_TURN_RESOURCE_ACTIVITY);
                turnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                turnIntent.putExtra("reason", "网络连接异常");
                context.startActivity(turnIntent);*/
                return;
            }

            if (activeNetworkInfo.isConnected()) {
                Log.e(TAG, "onReceive: " + activeNetworkInfo.isConnected());

                /*Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.putExtra("net",true);
                context.startActivity(mainIntent);*/
            }
        }
    }
}
