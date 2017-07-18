package com.huarui.life.thread;


import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.huarui.life.config.Constant;
import com.huarui.life.bean.entity.ResourseCheckEntity;
import com.huarui.life.manager.InfoManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.Callback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

import static com.huarui.life.config.BuildConfig.BasicUrl.DEFAULT_IMG_DOWN_URL;
import static com.huarui.life.config.Constant.NET_CODE;

/**
 * Created by HR_Life on 2017/1/11 17:19 : 14:46.
 * Package : ${PACKAGE_NAME}
 */

public class PlayResourceUpdateThread implements Runnable {

    private static final String TAG = "CheckResStatusThread";
    private String mDeviceId;
    private int mResVersion;
    private InfoManager mInfoManager;

    @Override
    public void run() {
        L.printLog2File(TAG, Thread.currentThread().getName());
        mInfoManager = InfoManager.getInstance();
        mDeviceId = mInfoManager.getmDeviceId() + "";
        mResVersion = mInfoManager.getmResVersion();
        sendCheckResStatusRequest();
    }

    /**
     * 请求默认播放图片资源数据
     */
    private void sendCheckResStatusRequest() {

        OkHttpUtils.get()
                .url(DEFAULT_IMG_DOWN_URL)
                .addParams("deviceid", mDeviceId)
                .build()
                .execute(new Callback<ResourseCheckEntity>() {

                    @Override
                    public ResourseCheckEntity parseNetworkResponse(Response response, int id) throws Exception {
                        if (NET_CODE == response.code()) {
                            String fJson = response.body().string();
                            L.printLog2File(TAG, "  Resource update net callback: " + fJson);
                            return new Gson().fromJson(fJson, ResourseCheckEntity.class);
                        }
                        L.printLog2File("   Resource update -- net code:" + response.code() + " msg: " + response.message());
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.printLog2File(TAG, "Resource update net error: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(ResourseCheckEntity response, int id) {
                        if (response == null) {
                            return;
                        }
                        if ("0".equals(response.getStatus())) {
                            int resourceVer = response.getData().getId();
                            L.e("last res version :" + mResVersion + " net version :" + resourceVer);
                            if (mResVersion != resourceVer) {
                                L.printLog2File("   Net has new res ,and ready download !");
                                String thumb = response.getData().getThumb();
                                downloadResource(thumb, resourceVer);
                            }
                        } else if ("10009".equals(response.getStatus())) {                              //如果当前返回无资源，必须删除原有资源
                            clearFiles(new File(Constant.getFilePath() + "image/"));
                        }
                    }
                });
    }

    /**
     * 执行下载图片资源！
     */
    private void downloadResource(String thumb, final int resourceVer) {

        if (TextUtils.isEmpty(thumb)) {
            return;
        }

        final String filePath = Constant.getFilePath() + "image/";
        File file = new File(filePath);
        clearFiles(file);
        String[] strings = thumb.split(",");

        final boolean[] results = new boolean[strings.length];

        for (int i = 0; i < strings.length; i++) {
            final String fileName = filePath + (i + ".jpg");
            OkHttpUtils.get()
                    .url(strings[i])
                    .id(i)
                    .build()
                    .execute(new BitmapCallback() {

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            L.printLog2File("  No：" + id + "pcs resource net error :" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Bitmap response, int id) {

                            FileOutputStream out = null;
                            try {
                                File file = new File(fileName);
                                out = new FileOutputStream(file);
                                boolean compress = response.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                if (compress) {
                                    results[id] = true;
                                    isDownFinish(results, resourceVer);
                                   // L.e(TAG, "Id :" + id + " res download success!");
                                }
                            } catch (IOException e) {
                                L.printLog2File("   Save Bitmap error :" + e.getMessage());
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (response != null && !response.isRecycled()) {
                                    response.recycle();
                                }
                            }
                        }

                        /**
                         * 验证是否所有资源都下载完成，如果所有都完成保存资源版本！
                         */
                        private void isDownFinish(boolean[] results, int resourceVer) {
                            boolean allFinish = false;
                            for (boolean b : results) {
                                allFinish = b;
                            }
                            if (allFinish) {
                                mInfoManager.setmResVersion(resourceVer);
                                L.printLog2File("   Resource update finish : " + results.length + " pcs res;");
                            }
                        }
                    });
        }
    }

    /**
     * 如果文件看路径存在则删除原有文件；如果不存在创建路径；
     */
    private void clearFiles(File file) {

        if (file.exists() && file.list().length > 0) {
            File[] files = file.listFiles();
            for (File file1 : files) {                                                              //如果文件存在 删除源文件下所有；
                if (file1.delete()) {
                    L.e(TAG, "delete old image files ! ");
                }
            }
            L.printLog2File("   Delete old image files finish !");
        } else {
            if (!file.mkdir()) {
                L.printLog2File("   Create res file dir fail !");
                interrupt();
            }
        }
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
