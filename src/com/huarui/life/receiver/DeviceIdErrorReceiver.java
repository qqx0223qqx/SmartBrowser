package com.huarui.life.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.SharePreFileConfig;
import com.huarui.life.utils.PreferenceConfig;
import com.zhy.http.okhttp.OkHttpUtils;

import log.L;


/**
 * Created by HR_Life on 2017/3/23.
 */

public class DeviceIdErrorReceiver extends BroadcastReceiver {

    public static final String DEVICEID_ERROR_ACTION = "com.huarui.life.ACTION_DEVICE_ID_ERROR";

    private static final String TAG = "DeviceIdErrorReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DEVICEID_ERROR_ACTION.equals(action)) {
            PreferenceConfig.setBooleanConfig(context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_RAN, false);
            PreferenceConfig.setConfig(context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_IMEI, "");
            OkHttpUtils.delete();
            BaseApp.appReboot(context);
            L.printLog2File(TAG, "Device Id changed , App will reboot !");
        }
    }
}
