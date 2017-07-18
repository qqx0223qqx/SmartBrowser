package com.huarui.life.thread;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.config.SharePreFileConfig;
import com.huarui.life.bean.entity.VersionEntity;
import com.huarui.life.ui.activity.LaunchActivity;
import com.huarui.life.utils.PreferenceConfig;
import com.huarui.life.utils.VersionUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

import static com.huarui.life.utils.FileUtil.deleteFile;
import static com.huarui.life.utils.SilentInstallUtils.install;


/**
 * Created by HR_Life on 2017/3/16 17:21 : 15:34.
 * Package : ${PACKAGE_NAME}
 * 版本更新线程；
 */

public class VersionUpdateThread implements Runnable {

    public static final String TAG = "CheckVersionThread";
    private final Context mContext;
    private String mVerName;
    private String mFilePath;

    public VersionUpdateThread(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        L.printLog2File(TAG, Thread.currentThread().getName());

        mFilePath = Constant.getFilePath() + "version/";
        File file = new File(mFilePath);
        if (!file.exists() && !file.mkdirs()) {
            L.printLog2File("   Create version update path fail !");
            return;
        }
        mVerName = VersionUtils.getVersionName(mContext);
        sendCheckVersionRequest();
    }

    private void sendCheckVersionRequest() {

        OkHttpUtils.get()
                .url(BuildConfig.BasicUrl.CHECK_VER_URL)
                .addParams("name", "appp")
                .build()
                .execute(new Callback<VersionEntity>() {
                    @Override
                    public VersionEntity parseNetworkResponse(Response response, int id) throws Exception {
                        if (Constant.NET_CODE == response.code()) {
                            String fJson = response.body().string();
                            L.printLog2File("   Version update net callback: " + fJson);
                            return new Gson().fromJson(fJson, VersionEntity.class);
                        }
                        L.printLog2File("   Version update -- net code:" + response.code() + " msg:" + response.message());
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.printLog2File(TAG, "Version update exception :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(VersionEntity response, int id) {
                        if (response == null) {
                            return;
                        }
                        if ("0".equals(response.getStatus())) {
                            String versionName = response.getData().getVersion();
                            if (!TextUtils.isEmpty(versionName) && !mVerName.equals(versionName)) {
                                String downloadUrl = response.getData().getFilename();
                                versionName += ".apk";
                                installApp(versionName, downloadUrl);
                            }
                        }
                    }
                });
    }

    /**
     * 安装app
     */
    private void installApp(String fileName, String downloadUrl) {
        String filePath = mFilePath + fileName;                                                     //文件的具体路径包含名称；
        File file = new File(filePath);

        if (file.exists() && checkApkFileIsGood(fileName)) {
            L.printLog2File("   The file is exist and it's good, try install it ;if no log then ,explain install success");
            setTimerRunApp(filePath);
            boolean install = install(filePath);
            if (!install) {
                cancelTimerRunApp(filePath);
            }
        } else {
            if (file.exists()) {
                deleteFile(file);
                L.printLog2File("   File is bad ,delete it;");
            }
            PreferenceConfig.setBooleanConfig(mContext, SharePreFileConfig.LAUNCH_DATA_FILE_NAME, fileName, false);
            L.printLog2File("   Go download new package!");
            executeDownloadApk(fileName, downloadUrl);
        }
    }

    /**
     * 检查已存在的apk文件是否是完整的包；
     */
    private boolean checkApkFileIsGood(String key) {

        boolean downloadFinished = PreferenceConfig.getBooleanConfig(mContext,
                SharePreFileConfig.LAUNCH_DATA_FILE_NAME, key);
        L.printLog2File("   The file is recorded && available :" + downloadFinished);

        if (downloadFinished) {
            return true;
        }
        return false;
    }

    /**
     * 执行下载apk
     */
    private void executeDownloadApk(final String filename, String downloadUrl) {

        OkHttpUtils.get()
                .url(downloadUrl)
                .build()
                .execute(new FileCallBack(mFilePath, filename) {

                    @Override
                    public void onResponse(File response, int id) {

                        if (response.exists()) {
                            mFilePath += filename;
                            L.printLog2File("   Download finish,and try install it; if no log then,explain install success !");
                            executeInstallApk(mFilePath);
                        }
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        super.inProgress(progress, total, id);
                        if (1.0 == progress) {                                                      //进度完成时保存文件
                            PreferenceConfig.setBooleanConfig(mContext, SharePreFileConfig.LAUNCH_DATA_FILE_NAME, filename, true);
                            L.printLog2File("   File " + filename + "(" + total + " byte)!");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        L.printLog2File("   Version update apk file download error :" + e.getMessage());
                    }
                });
    }

    /**
     * 下载完成去安装；
     */
    private void executeInstallApk(String mFilePath) {
        setTimerRunApp(mFilePath);
        boolean install = install(mFilePath);
        if (!install) {
            cancelTimerRunApp(mFilePath);
        }
    }

    /**
     * 设置默认固定时间后启动app；
     */
    private void setTimerRunApp(String filePath) {

        Intent launchIntent = new Intent(mContext.getApplicationContext(), LaunchActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.putExtra("install", filePath);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 40 * 1000, pendingIntent);
        L.printLog2File("   Will install app, set 40`s run app again!");
    }

    /**
     * 如果安装失败取消定时的启动；
     */
    private void cancelTimerRunApp(String filePath) {

        Intent launchIntent = new Intent(mContext.getApplicationContext(), LaunchActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.putExtra("install", filePath);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
        L.printLog2File("   APK install fail ,cancel 40`s reboot app command!");
    }
}
