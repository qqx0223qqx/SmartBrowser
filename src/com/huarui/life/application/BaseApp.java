package com.huarui.life.application;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.crash.CrashHandler;
import com.huarui.life.manager.BroadcastReceiverManager;
import com.huarui.life.ui.activity.LaunchActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.PersistentCookieStore;
import com.zhy.http.okhttp.https.HttpsUtils;

import org.apache.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import log.L;
import okhttp3.OkHttpClient;

/**
 * Created by HR_Life on 2017/6/13 : 12:03.
 * Package : com.huarui.life.application
 */

public class BaseApp extends Application {

    public static final String TAG = "BaseApplication";
    /**
     * 保存所有Activity！
     */
    private static HashMap<String, Activity> mActivityMap = new HashMap<>();
    private static BaseApp application = null;
    public static String mLogFileName;
    public static final String TOP_ACTIVITY = "topActivity";
    private static ExecutorService mService;

    public static BaseApp getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (application == null) {
            application = this;
        }
        initData();
        registerActivityLifecycleCallbacks(callback);
    }

    private void initData() {
        initHttpUtils();
        initLogger();
        BroadcastReceiverManager.init(this);
        CrashHandler.getInstance().init(this);
    }

    /**
     * @return 应用线程池管理器；
     */
    public static ExecutorService getThreadService() {
        if (mService == null || mService.isShutdown()) {
            mService = Executors.newFixedThreadPool(5);
        }
        return mService;
    }

    /**
     * 初始化OkHttpClient数据！
     */
    private void initHttpUtils() {

        CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                // .addInterceptor(new LoggerInterceptor("TAG"))
                .cookieJar(cookieJar)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    /**
     * 初始化日志工具；
     */
    private void initLogger() {

        String format = new SimpleDateFormat("yyMMdd", Locale.CHINA).format(new Date());
        mLogFileName = Constant.getFilePath() + "log" + File.separator + "i-" + format + ".log";

        LogConfigurator logConfig = new LogConfigurator(mLogFileName);
        logConfig.setRootLevel(Level.DEBUG);
        logConfig.setUseFileAppender(true);                                                         //设置文件内容替换还是延续；
        logConfig.setImmediateFlush(true);                                                          //设置是否立即打印结果
        logConfig.setMaxFileSize(1024 * 1024 * 2);
        logConfig.setMaxBackupSize(2);

        if (BuildConfig.isDebug) {
            logConfig.setUseLogCatAppender(true);                                                   //是否显示本地控制台输出；
        }
        logConfig.configure();
    }

    /**
     * 3s后应用重核心启动；
     */
    public static void appReboot(Context context) {
        Intent intent = new Intent(context, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis() + 2000, pendingIntent);
        finishAll();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        L.printLog2File(TAG, "Progress will exit , AlarmManager send cmd : reboot and 2`s ！");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Runtime.getRuntime().gc();
    }

    /**
     * Activity 生命周期回调！
     */
    private ActivityLifecycleCallbacks callback = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            L.e("onActivityCreated:" + activity.getLocalClassName());
            addActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.e(TAG, "onActivityStarted: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityResumed(Activity activity) {
            L.e("onActivityResumed ==> " + activity.getLocalClassName());
            if (mActivityMap != null) {
                mActivityMap.remove(TOP_ACTIVITY);
                mActivityMap.put(TOP_ACTIVITY, activity);

                //L.e("topActivity =: " + mActivityMap.get(TOP_ACTIVITY).getLocalClassName());
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            L.e("onActivityPaused ==> " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            L.e("onActivityStopped ==> " + activity.getLocalClassName());

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            L.e("onActivityDestroyed:" + activity.getLocalClassName());
            Activity topActivity = getTopActivity();
            if (topActivity != null && activity.getLocalClassName().equals(topActivity.getLocalClassName())) {
                mActivityMap.remove(TOP_ACTIVITY);
            }
            removeActivity(activity);
        }
    };

    /**
     * 保存实例引用
     */
    public void addActivity(Activity activity) {

        if (mActivityMap != null && activity != null) {
            mActivityMap.put(activity.getLocalClassName(), activity);
            L.e("add activity :" + activity.getLocalClassName());
        }
    }

    /**
     * 移除引用;
     */
    public void removeActivity(Activity activity) {

        if (mActivityMap != null && activity != null) {
            mActivityMap.remove(activity.getLocalClassName());
            L.e(" remove activity : " + activity.getLocalClassName());
        }
    }

    /**
     * 结束指定activity;
     */
    public static void finishActivity(String localClassName) {

        if (mActivityMap != null && mActivityMap.containsKey(localClassName)) {
            mActivityMap.get(localClassName).finish();
            L.e("finish activity:" + localClassName);
        }
    }

    /**
     * 结束除MainActivity以外其他activity;
     */
    public static void finishOtherAll(Activity activity) {
        if (mActivityMap != null && activity != null) {
            for (String key : mActivityMap.keySet()) {
                if (!TOP_ACTIVITY.equals(key) && !activity.getLocalClassName().equals(key)) {
                    mActivityMap.get(key).finish();
                }
            }
        }
    }

    /**
     * 结束除MainActivity以外其他activity;
     */
    public static void finishOtherAll(String localClassName) {
        if (mActivityMap != null) {
            for (String key : mActivityMap.keySet()) {
                if (!TOP_ACTIVITY.equals(key) && !localClassName.equals(key)) {
                    mActivityMap.get(key).finish();
                }
            }
        }
    }

    /**
     * 结束所有Activity；
     */
    public static void finishAll() {
        if (mActivityMap != null) {
           /* for (String key : mActivityMap.keySet()) {
                mActivityMap.get(key).finish();
            }*/
            for (Map.Entry<String, Activity> entry : mActivityMap.entrySet()) {                     //效率相对于上者更高；
                entry.getValue().finish();
                L.e("finishAll " + entry.getKey());
            }
        }
    }

    /**
     * @return 当前顶层Activity的实例引用；
     */
    public static Activity getTopActivity() {
        if (mActivityMap != null) {
            return mActivityMap.get(TOP_ACTIVITY);
        }
        return null;
    }

}
