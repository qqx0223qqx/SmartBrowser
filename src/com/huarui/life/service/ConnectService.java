package com.huarui.life.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.huarui.life.application.BaseApp;
import com.huarui.life.bean.DynamicDataBean;
import com.huarui.life.callback.TimedRequestUrlCallback;
import com.huarui.life.callback.TimerUploadAttrCallBack;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.thread.CaptureScreenThread;
import com.huarui.life.thread.PlayResourceUpdateThread;
import com.huarui.life.thread.TakePhotoThread;
import com.huarui.life.thread.UploadInitLogThread;
import com.huarui.life.thread.VersionUpdateThread;
import com.huarui.life.utils.BootUtil;
import com.huarui.life.utils.DeviceInfoUtils;
import com.huarui.life.utils.NetworkUtils;
import com.huarui.life.utils.VersionUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;

/**
 * Created by HR_Life on 2016/11/8 14:20 14:42 15:19.
 */

public class ConnectService extends Service {

    public static final String TAG = "ConnectService";
    /* 设备ID；*/
    private static int mDeviceId;
    /*定时器Timer;*/
    private Timer mTimer;
    /* 定时器计数;*/
    private int mTimerCount;
    /* 数据请求集合;*/
    private HashMap<String, String> mParams;
    /* 定时请求url返回;*/
    private TimedRequestUrlCallback mUrlCallback;
    /* 定时上传动态参数返回;*/
    private TimerUploadAttrCallBack mAttrCallBack;
    /* 动态参数上传实例*/
    private DynamicDataBean dataBean;
    /* 版本名称；*/
    private String mVerName;
    private LocationServer mLocationServer;
    private ExecutorService mThreadService;

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceId = InfoManager.getInstance().getmDeviceId();
        dataBean = DynamicDataBean.getInstance();
        mVerName = VersionUtils.getVersionName(this);
        mThreadService = BaseApp.getThreadService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String command = intent == null ? "" : intent.getStringExtra("command");

        if (TextUtils.isEmpty(command)) {
            executeTimerTarget();
        } else if ("photo".equals(command)) {
            new TakePhotoThread(this).run();
        } else if ("screen".equals(command)) {
            if (!mThreadService.isShutdown()) {
                mThreadService.submit(new CaptureScreenThread(this));
            }
        } else if ("log".equals(command)) {
            if (!mThreadService.isShutdown()) {
                mThreadService.submit(new UploadInitLogThread());
            }
        } else if ("reboot".equals(command)) {
            BootUtil.commandReboot();
        } else if ("volume".equals(command)) {
            executeSetVolume(intent != null ? intent.getStringExtra("value") : null);
        } else if ("shutdown".equals(command)) {
            BootUtil.commandShutDown();
        } else if ("power".equals(command)) {
            BootUtil.modifyPowerOnAndOff(intent != null ? intent.getStringExtra("value") : null);
        } else if ("version".equals(command)) {
            if (!mThreadService.isShutdown()) {
                mThreadService.submit(new VersionUpdateThread(this));
            }
        } else if ("location".equals(command)) {
            executeLocation();
        }
        L.printLog2File(TAG, "  ==> onStartCommand() command ：  " + (isEmpty(command) ? "No cmd" : command));

        return START_STICKY_COMPATIBILITY;
    }

    /**
     * 指令执行定位；
     */
    private void executeLocation() {
        mLocationServer = LocationServer.createInstance(this, new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                String longitude = bdLocation.getLongitude() + "";
                String latitude = bdLocation.getLatitude() + "";
                String addrInfo = bdLocation.getAddrStr();

                if (TextUtils.isEmpty(addrInfo)) {
                    addrInfo = "广东省深圳市";
                }
                uploadAddrAttr(addrInfo, longitude, latitude);

                L.printLog2File(TAG, " New info：" + addrInfo + " longitude : " + longitude + " latitude ：" + latitude);
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        });
        if (mLocationServer != null) {
            mLocationServer.startLocate();
        }
    }

    /**
     * 执行设置音量；
     */
    private void executeSetVolume(String value) {
        if (TextUtils.isEmpty(value)) {
            value = "3";
        }
        try {
            AudioManager manager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
            Integer volume = Math.abs(Integer.valueOf(value));
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            L.printLog2File(TAG, "Set system volume finish :" + volume);
        } catch (Exception e) {
            L.printLog2File(TAG, "Set system volume exception :" + e.getMessage());
        }
    }

    /**
     * 启用定时任务！
     */
    private void executeTimerTarget() {

        if (mUrlCallback != null) {                                                                 //如果实例存在先滞空，加快回收；
            mUrlCallback = null;
        }
        if (mAttrCallBack != null) {
            mAttrCallBack = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mUrlCallback = new TimedRequestUrlCallback(this, mHandler);
        mAttrCallBack = new TimerUploadAttrCallBack(this, mHandler);
        mTimer = new Timer();
        mTimer.schedule(timerTask, 1, Constant.TIMER_REQUEST_CMD);
        L.printLog2File(TAG, "Execute timer task ;");
    }

    /**
     * 执行请求url-cmd状态;
     */
    private void executeRequestcmd() {
        OkHttpUtils.get()
                .url(BuildConfig.BasicUrl.TIMER_REQUST_URL)
                .addParams("deviceid", String.valueOf(mDeviceId))
                .build()
                .execute(mUrlCallback);
    }

    /**
     * 启用定时任务获取相关参数；
     */
    private void executeResetAttr() {

        dataBean.setUstorage(DeviceInfoUtils.getAvailableExternalMemorySize());
        dataBean.setUcpu(DeviceInfoUtils.readCpuUsage());
        dataBean.setUmemory(DeviceInfoUtils.getAvailMemory(ConnectService.this));
        //dataBean.setBandwidth(TrafficNetUtils.getInstance().getCurrentNetData());
        dataBean.setBandwidth("10kb");
        //dataBean.setMaxbandwidth(TrafficNetUtils.getInstance().getMaxBandWight());
        dataBean.setMaxbandwidth("868kb");
        dataBean.setNettype(NetworkUtils.getNetworkState(ConnectService.this));
    }

    /**
     * 启用定时任务定时上传动态参数；
     */
    private void executeUploadAttr() {
        if (mParams == null) {
            mParams = new HashMap<>();
        }

        mParams.clear();
        mParams.put("deviceid", mDeviceId + "");
        mParams.put("version", mVerName);
        mParams.put("nettype", dataBean.getNettype());
        mParams.put("ucpu", dataBean.getUcpu());                                             //当前cpu使用率
        mParams.put("umemory", dataBean.getUmemory());                                       //当前易用内存
        mParams.put("ustorage", dataBean.getUstorage());                                     //当前已用外部存储
        mParams.put("bandwidth", dataBean.getBandwidth());                                   //当前平均带宽
        mParams.put("maxbandwidth", dataBean.getMaxbandwidth());                             //最高带宽

        OkHttpUtils.post()
                .url(BuildConfig.BasicUrl.TIMER_UPLOAD_DYNAMICATTR_URL)
                .params(mParams)
                .build()
                .execute(mAttrCallBack);
    }

    /**
     * 上传定位参数；
     */
    private void uploadAddrAttr(String addrInfo, String longitude, String latitude) {

        if (mLocationServer != null) {
            mLocationServer.releaseLocate();
        }

        OkHttpUtils.post()
                .addParams("addr", addrInfo)
                .addParams("longitude", longitude)
                .addParams("latitude", latitude)
                .url(BuildConfig.BasicUrl.LOCATION_INFO_URL + mDeviceId)
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        Logger.getLogger("==> uploadAddrAttr()").info("Upload location attr finish , net callback :" + response.body().string());
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Logger.getLogger("==> uploadAddrAttr()").info("Upload location attr error:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Object response, int id) {

                    }
                });
    }

    /**
     * 当前每12s 获取一次url是否变化；
     * 每4.5min上传一次动态参数；
     */
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mTimerCount == 0 || (mTimerCount % 3 == 0)) {
                executeRequestcmd();
            } else if (mTimerCount == 7) {
                if (!mThreadService.isShutdown()) {
                    mThreadService.submit(new PlayResourceUpdateThread());
                }
            } else if (mTimerCount == 17) {
                executeResetAttr();
            } else if (mTimerCount == 20) {
                executeUploadAttr();
                mParams.clear();
            } else if (mTimerCount > 30) {
                mTimerCount = 9;
            }
            mTimerCount += 1;

            L.e(TAG, "TimerTask count : " + mTimerCount);
        }
    };

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUrlCallback = null;
        mAttrCallBack = null;

        timerTask.cancel();
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimerCount = 0;
        this.startService(new Intent(this, ConnectService.class));
    }
}
