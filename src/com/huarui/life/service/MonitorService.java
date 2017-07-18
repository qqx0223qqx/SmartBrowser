package com.huarui.life.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.ui.activity.LaunchActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by HR_Life on 2016/11/17.
 */

@SuppressWarnings("ALL")
public class MonitorService extends Service {

    private static final String TAG = "MonitorService";
    private static final long ALARM_TRAGGER_TIME = 30 * 1000;                                       //开始执行时间；
    private static final long ALARM_INTERVAL_TIME = 3 * 60 * 1000;                                  //间隔时间；
    private String mPackageName;

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageName = this.getPackageName();
        //setAlarmTimer2check();
    }

    /**
     * 启用系统时钟进行循环检查；
     */
    private void setAlarmTimer2check() {

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent serviceIntent = new Intent(this, MonitorService.class);
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        serviceIntent.putExtra("checkstatus", true);
        PendingIntent pendingIntent = PendingIntent.getService(this.getBaseContext(), 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + ALARM_TRAGGER_TIME, ALARM_INTERVAL_TIME, pendingIntent);
        L.printLog2File(TAG, " onCreate()  ==> Use system alarmclock tob timer call :  online/offline ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getBooleanExtra("checkstatus", false)) {
            checkAppRunState();
            L.e(TAG, "  Check app online/offine ");
        } else if (intent != null) {
            //String deviceId = intent.getStringExtra("deviceId");
            ArrayList<CharSequence> analytic = intent.getCharSequenceArrayListExtra("analytic");
            if (analytic != null && analytic.size() > 0) {
                //uploadTouchEvent(deviceId, analytic);
            }
        } else {
            L.e(TAG, "MonitorService onStartCommand !");
        }
        return START_STICKY_COMPATIBILITY;
    }

    /**
     * 上传手势结果到服务器；
     */
    private void uploadTouchEvent(String deviceId, ArrayList list) {

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String tJson = new Gson().toJson(list, type);

        OkHttpUtils.post()
                .url(BuildConfig.BasicUrl.MONITOR_USER_STATE_URL)
                .addParams("deviceid", deviceId)
                .addParams("timestamp", tJson)
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        L.e(" Upload user monitoring state finish :" + response.body().string());
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.e("Upload user monitoring state error :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                    }
                });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 检查应用是否置于前台；
     */
    private boolean checkAppIsForeground() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {        //前台程序
                    for (String activeProcess : processInfo.pkgList) {
                        if (mPackageName.equals(activeProcess)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (mPackageName.equals(componentInfo.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查应用是否在前台运行后的执行业务！
     */
    public void checkAppRunState() {

        boolean runForeground = checkAppIsForeground();                                               //判断是否在后台运行,如果在后台就拉起；
        if (!runForeground) {
            L.printLog2File(TAG, "=== App is backgroung run ! === ");
            try {
                Intent startActivitys = new Intent(this, LaunchActivity.class);
                startActivitys.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(startActivitys);
            } catch (Exception e) {
                L.e(e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.printLog2File(TAG, "MonitorService onDestroy !");
        startService(new Intent(this, MonitorService.class));
    }
}
