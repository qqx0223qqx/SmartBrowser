package com.huarui.life.thread;

import com.huarui.life.config.BuildConfig;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.utils.FileUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import log.L;
import okhttp3.Call;

/**
 * Created by HR_Life on 2017/5/16 : 13:53.
 * Package : com.hr.life.trnfa.service.thread
 */

public class UploadImageThread implements Runnable {

    public static final String TAG = "UploadImageThread";
    /*需要上传的文件绝对路径；*/
    private final String mFilePath;
    /* 可能会被其他调用，只传入的类型标识； */
    private final String mFlag;

    public UploadImageThread(String flag, String filePath) {
        this.mFlag = flag;
        this.mFilePath = filePath;
    }

    @Override
    public void run() {
        L.e("=========name ：" + Thread.currentThread().getName() + "   id :" + Thread.currentThread().getId());

        L.printLog2File(TAG, "  " + Thread.currentThread().getName());

        File file = new File(mFilePath);
        L.printLog2File("Upload file length :" + file.length() + "  byte !");
        if (!file.exists()) {
            L.printLog2File("   Upload image file isn`t exist !");
            interrupt();
            return;
        }
        String deviceId = InfoManager.getInstance().getmDeviceId() + "";
        String uploadUrl = BuildConfig.BasicUrl.UPLOAD_SCREENSHOT_URL;
        L.e("   " + mFilePath);
        uploadImage2server(deviceId, uploadUrl, file);
    }

    /**
     * 上传File到server；
     */
    private void uploadImage2server(String deviceId, String uploadUrl, final File file) {
        OkHttpUtils.post()
                .url(uploadUrl)
                .addParams("deviceid", deviceId)
                .addFile("image", mFlag, file)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.printLog2File("   Upload image fail net callback:" + e.getMessage());
                        FileUtil.deleteFile(file);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        L.printLog2File("   Upload image finish net callback:" + response + "   delete file !");
                        FileUtil.deleteFile(file);
                    }
                });
    }

    /**
     * 终端当前线程；
     */
    private void interrupt() {
        try {
            Thread currentThread = Thread.currentThread();
            if (currentThread.isAlive() && !currentThread.isInterrupted()) {
                currentThread.interrupt();
            }
        } catch (Exception c) {
            L.e(c.getMessage());
        }
    }
}
