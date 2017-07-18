package com.huarui.life.thread;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.utils.FileUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import log.L;
import okhttp3.Call;

/**
 * Created by WXL on 2017/3/14 14:12 : 15:03 : 15:06.
 * Project : SmartBrowser
 * Package : com.huarui.life
 */

public class UploadInitLogThread implements Runnable {

    private static final String TAG = "UploadLogThread";

    @Override
    public void run() {

        L.printLog2File(TAG, Thread.currentThread().getName());

        String deviceId = InfoManager.getInstance().getmDeviceId() + "";
        String fileName = Constant.getFilePath() + "log/" + "i-" +
                new SimpleDateFormat("yyMMdd", Locale.getDefault()).format(new Date()) + ".log";

        File logFile = new File(fileName);

        if (!logFile.exists()) {
            fileName = BaseApp.mLogFileName;
            logFile = new File(fileName);
        }
        L.printLog2File(    "File name :" + fileName);

        BufferedReader buffreader = null;
        BufferedWriter bufferwrite = null;
        File tempFile = null;

        if (logFile.exists()) {
            try {
                buffreader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
                tempFile = new File(Constant.getFilePath() + "log/temp-log.txt");
                bufferwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)));

                String buffread;
                while ((buffread = buffreader.readLine()) != null) {
                    bufferwrite.write(buffread);
                    bufferwrite.flush();
                    bufferwrite.newLine();
                }
                L.printLog2File(    "Log file copy finish,and will upload !");
            } catch (IOException e) {
                L.printLog2File("   Copy log file exception :" + e.getMessage());
            } finally {
                try {
                    if (buffreader != null) {
                        buffreader.close();
                    }
                    if (bufferwrite != null) {
                        bufferwrite.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                uploadLogFile(tempFile, deviceId);
                L.e("   Temp file path :" + (tempFile != null ?
                        tempFile.getAbsolutePath() : null) + ",temp file length :" +
                        (tempFile != null ? tempFile.length() : 0) + ", log file length :" + logFile.length());
            }
        }
    }

    /**
     * 执行上传日志；
     */
    private void uploadLogFile(final File tempFile, String deviceId) {

        if (!tempFile.exists()) {
            L.printLog2File("   Log file not exist !");
            return;
        }
        String url = BuildConfig.BasicUrl.UPLOAD_LOG_URL + "/" + 1 + "/" + deviceId;

        OkHttpUtils.post()
                .url(url)
                .addParams("deviceid", deviceId)
                .addFile("filename", "file", tempFile)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.printLog2File("   Upload log file error:" + e.getMessage());
                        FileUtil.deleteFile(tempFile);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        L.printLog2File("   Upload log file finish :" + response);
                        FileUtil.deleteFile(tempFile);
                    }
                });
    }
}

