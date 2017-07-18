package com.huarui.life.callback;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.gson.Gson;
import com.huarui.life.config.Constant;
import com.huarui.life.bean.entity.TimerUploadAttrEntity;
import com.huarui.life.service.ConnectService;
import com.zhy.http.okhttp.callback.Callback;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by WXL on 2016/12/8 14:42 15:19 : 17:02.
 * Project : SmartBrowser
 * Package : com.huarui.life
 */
public class TimerUploadAttrCallBack extends Callback<TimerUploadAttrEntity> {

    private static final String TAG = "TimerUploadAttrCallBack";

    private final Context context;
    /**
     * Service key
     */
    private static final String COMMAND_FLAG = "command";
    private final Handler mHandler;
    /**
     * Gson实例；
     */
    private Gson gson = new Gson();

    public TimerUploadAttrCallBack(Context context, Handler handler) {
        this.context = context.getApplicationContext();
        this.mHandler = handler;
    }

    @Override
    public TimerUploadAttrEntity parseNetworkResponse(Response response, int id) throws Exception {
        if (Constant.NET_CODE == response.code()) {
            String fJson = response.body().string();
            L.e(TAG, "TimerUploadAttrCallBack: " + fJson);
            return gson.fromJson(fJson, TimerUploadAttrEntity.class);
        }
        L.printLog2File(TAG, "TimerUploadAttrCallBack -- net code:" + response.code() + " msg:" + response.message());
        return null;
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        L.printLog2File(TAG, "Request cmd error :" + e.getMessage());
    }

    @Override
    public void onResponse(final TimerUploadAttrEntity response, int id) {
        if (response == null) {
            return;
        }
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String cmd = response.getData().getCmd();
                    String param = response.getData().getParam();
                    sendCmd2Service(cmd, param);
                }
            });
        } else {
            String cmd = response.getData().getCmd();
            String param = response.getData().getParam();
            sendCmd2Service(cmd, param);
        }
    }

    private void sendCmd2Service(String cmd, String param) {

        if (Constant.STATUS_SECCUSS_CODE.equals(cmd)) {
            L.printLog2File(TAG, "Request code = 0;");
        } else if (Constant.CMD_TAKEPHOTO_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "photo"));
        } else if (Constant.CMD_SCREENSHOT_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "screen"));
        } else if (Constant.CMD_REBOOT_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "reboot"));
        } else if (Constant.CMD_VOLUMECONTROL_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "volume").putExtra("value", param));
        } else if (Constant.CMD_SETPOWERTIME_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "power").putExtra("value", param));
        } else if (Constant.CMD_SHUTDOWN_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "shutdown"));
        } else if (Constant.CMD_UPLOAD_LOG_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "log"));
        } else if (Constant.CMD_CHECK_VERSION_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "version"));
        } else if (Constant.CMD_LOCATION_CODE.equals(cmd)) {
            context.startService(new Intent(context, ConnectService.class).putExtra(COMMAND_FLAG, "location"));
        } else {
            L.printLog2File(TAG, "New cmd : " + cmd);
        }
    }
}
