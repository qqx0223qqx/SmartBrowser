package com.huarui.life.callback;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.huarui.life.config.Constant;
import com.huarui.life.bean.entity.TimerUrlStatusEntity;
import com.huarui.life.manager.BroadcastReceiverManager;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.ui.activity.ImageActivity;
import com.huarui.life.ui.activity.MainActivity;
import com.huarui.life.utils.NetworkUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;
import static com.huarui.life.manager.BroadcastReceiverManager.sendBroadcast;

/**
 * Created by HR_Life on 2016/11/10 14:42 15:19.
 */

public class TimedRequestUrlCallback extends Callback<TimerUrlStatusEntity> {

    public static final String DEVICEID_ERROR_ACTION = "com.huarui.life.ACTION_DEVICE_ID_ERROR";

    private static final String TAG = "TimedRequestUrlCallback";
    private Handler mHandler;
    private Context context;
    /* Gson 唯一实例*/
    private Gson mGson = new Gson();
    /* 上一次节目version; */
    private int mPublicUrlVersion;
    /* 上一次节目url：*/
    private String mLastPublicUrl;
    /* 是否已经在运行ImageActivity*/
    private boolean mLaunchImagePlay;
    /* 记录上一次报名单启用状态 */
    private boolean mLastWhitelistStatus = false;
    /* 错误统计；如果连续超过3次跳转页面；*/
    private int errorCount_NetCode = 0;

    private int errorCount_Request = 0;
    private final InfoManager mInfoManager;

    public TimedRequestUrlCallback(Context context, Handler handler) {
        this.context = context.getApplicationContext();
        this.mHandler = handler;
        mInfoManager = InfoManager.getInstance();
        mPublicUrlVersion = mInfoManager.getmPublicUrlVersion();
    }

    /**
     * 请求serverCmd指令的返回！
     */
    @Override
    public TimerUrlStatusEntity parseNetworkResponse(Response response, int id) throws Exception {

        if (Constant.NET_CODE == response.code()) {
            String fJson = response.body().string();
            //L.e(TAG, "TimedRequestUrlCallback :" + fJson);
            errorCount_NetCode = 0;
            return mGson.fromJson(fJson, TimerUrlStatusEntity.class);
        }

        errorCount_NetCode += 1;                                                                    //有网络，但是连接服务器失败；
        if (errorCount_NetCode >= 4) {
            launchImageActivity("连接服务器错误,error_code:" + response.code());
            errorCount_NetCode = -3;
        }
        L.printLog2File(TAG, "net_code: " + response.code() + "msg: " + response.message());
        return null;
    }

    @Override
    public void onError(Call call, Exception e, int id) {

        if (mLaunchImagePlay) {                                                                      //前三次时间不跳转 launchWheelPlay = false,相当于前9s内3次请求都失败在跳转;
            return;
        }
        errorCount_Request += 1;

        if (errorCount_Request < 3) {                                                               //连续请求3次都失败页面跳转；
            return;
        }
        errorCount_Request = 0;

        String reason;                                                                             //连接服务器时出错误基本是没有连接到服务器；
        String exception = e != null ? "(Net request fail)" + e.getMessage() : "null";

        if (!NetworkUtils.isNetworkAvailables(context)) {
            reason = " ！ 网络未连接";
        } else if ((exception.contains("timeout")) || (exception.contains("time out"))) {
            reason = " ！ 网络连接超时  ";
        } else {
            reason = " ！ 连接服务器错误  ";
        }
        launchImageActivity(reason);

        L.printLog2File(TAG, "Request url state error :" + exception);
    }

    @Override
    public void onResponse(final TimerUrlStatusEntity infoEntity, int id) {
        if (infoEntity == null) {
            return;
        }
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleMsg(infoEntity);
                }
            });
        } else {
            handleMsg(infoEntity);
        }
    }

    /**
     * 处理你网络返回；
     */
    private void handleMsg(TimerUrlStatusEntity infoEntity) {

        if (Constant.STATUS_SECCUSS_CODE.equals(infoEntity.getStatus())) {                          //正常返回，一定包含数据url；

            String currUrl = infoEntity.getData().getUrl();
            String txtUrl = infoEntity.getData().getTxt();
            int currVersion = infoEntity.getData().getVersion();

            if (isEmpty(mLastPublicUrl) || mLaunchImagePlay) {

                mLastPublicUrl = currUrl;
                mLastWhitelistStatus = !TextUtils.isEmpty(txtUrl);

                startMainActivity(currVersion, currUrl, txtUrl);                                    //刚刚启动调用，启动MainActivity
            } else if (mPublicUrlVersion != currVersion) {

                mPublicUrlVersion = currVersion;
                mInfoManager.setmPublicUrlVersion(currVersion);
                mLastWhitelistStatus = !TextUtils.isEmpty(txtUrl);

                updateMainActivity(currUrl, txtUrl);                                   //如果节目版本有变化一定要执行逻辑，创建Intent，后比较url是否有变化；
            } else {
                boolean whitelist = !TextUtils.isEmpty(txtUrl);
                if (mLastWhitelistStatus != whitelist) {

                    mLastWhitelistStatus = !TextUtils.isEmpty(txtUrl);
                    mInfoManager.setmIsLaunchWhiteList(whitelist);

                    updateActivityCheck(whitelist, txtUrl);                                            //排除由于加载页面错误因素引起播放图片时启动无效MainActivity更新白名单设置；
                } else {
                    L.e(TAG, "No");
                }
            }
        } else if (Constant.STATUS_DEVICEID_ERROR.equals(infoEntity.getStatus())) {
            sendBroadcast(new Intent(DEVICEID_ERROR_ACTION));
            L.printLog2File(TAG, "Send broad-cast-receiver");
        }
    }

    /**
     * 启动ImageActivity
     */
    private void launchImageActivity(String reason) {
        context.startActivity(new Intent(context, ImageActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Constant.IMAGE_REASON, reason));
        mLaunchImagePlay = true;
    }

    /**
     * 传数据给MainActivity开启或关闭白名单功能；
     */
    private void updateActivityCheck(boolean whitelist, String txtUrl) {

        Intent menuIntent = new Intent(Constant.ACTION_DATA_CHANGE);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menuIntent.putExtra(Constant.DATA_CHANGE_WHITELIST, whitelist);
        menuIntent.putExtra(Constant.DATA_CHANGE_ONLY_WHITELIST, true);

        if (whitelist) {                                                                           //如果启用白名单，那就先下载，否则就直接发送广播通知取消白名单功能；
            downloadVerifyFile(menuIntent, txtUrl, false);
            L.e(TAG, "Modify whitelist of MainActivity :" + true);

        } else {
            sendBroadcast(menuIntent);
            L.e(TAG, "Modify whitelist of MainActivity :" + false);
        }
    }

    /**
     * 当app在运行时，更改节目调用；
     */
    private void updateMainActivity(String currUrl, String txtUrl) {

        Intent updateIntent = new Intent(context, MainActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!mLastPublicUrl.equals(currUrl)) {
            updateIntent.putExtra("load", true);
            updateIntent.putExtra("currentUrl", currUrl);
            mLastPublicUrl = currUrl;                                                                      //先保存及更新数据；
            mInfoManager.setmPublicUrl(currUrl);
            if (!TextUtils.isEmpty(txtUrl)) {                                                                           //如果txtUrl为空，不去下载白名单文件；同时启动新的页面加载url；
                downloadVerifyFile(updateIntent, txtUrl, true);
            } else {
                context.startActivity(updateIntent);
            }
            L.e(TAG, "Update MainActivity ,and different of last url !");
        } else {
            updateIntent.putExtra("load", false);
            context.startActivity(updateIntent);
            L.e(TAG, "Update MainActivity ,the same of last url !");
        }
    }

    /**
     * 由刚启动或完全停止状态下运行时！
     */
    private void startMainActivity(int currVersion, String currUrl, String txtUrl) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("load", true);
        intent.putExtra("currentUrl", currUrl);

        if (mPublicUrlVersion != currVersion) {                                                           //如果与文件中的数据不相同，保存更新数据！
            mPublicUrlVersion = currVersion;
            mInfoManager.setmPublicUrlVersion(currVersion);
            mInfoManager.setmPublicUrl(currUrl);
            L.e(TAG, "Start MainActivity ,and different of last url version !");
        }

        if (!TextUtils.isEmpty(txtUrl)) {                                                           //如果txtUrl为空，不去下载白名单文件；同时启动页面加载新的数据；
            downloadVerifyFile(intent, txtUrl, true);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * 执行下载白名单文件；
     */
    private void downloadVerifyFile(final Intent intent, String txtUrl, final boolean isStartNoSendReceiver) {

        String filePath = context.getFilesDir().getAbsolutePath();
        String fileName = "verifyFile.cfg";
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {                                                                   //A90机器存在不能创建文件夹的情况；
                filePath = Constant.getFilePath() + "verify" + File.separator;
                L.printLog2File(TAG, "Make verifyFile.cfg default dir error !");
            }
        }

        OkHttpUtils.get()
                .url(txtUrl)
                .build()
                .execute(new FileCallBack(filePath, fileName) {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (isStartNoSendReceiver) {
                            context.startActivity(intent);
                        } else {
                            BroadcastReceiverManager.sendBroadcast(intent);
                        }
                        L.printLog2File(TAG, "Download verify file error : " + e.getMessage() + " launch model:" + isStartNoSendReceiver);
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        if (isStartNoSendReceiver) {
                            context.startActivity(intent);
                        } else {
                            BroadcastReceiverManager.sendBroadcast(intent);
                        }
                        L.printLog2File(TAG, "Download verify file finish,and start/update MainActivity !" + " launch model:" + isStartNoSendReceiver);
                    }
                });
    }
}
