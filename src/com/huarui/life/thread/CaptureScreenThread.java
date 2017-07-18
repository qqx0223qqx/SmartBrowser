package com.huarui.life.thread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.BroadcastReceiverManager;
import com.huarui.life.ui.activity.LaunchActivity;
import com.huarui.life.utils.BootUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import log.L;

import static com.huarui.life.config.Constant.DATA_CHANGE_SCREENSHOT;

/**
 * Created by HR_Life on 2017/6/19 : 10:59.
 * Package : com.huaruilife.trnfa.service.thread
 */

public class CaptureScreenThread implements Runnable {

    private static final String TAG = "CaptureScreenThread";
    private final Context context;
    private String mFilePath;
    private String fileName;

    public CaptureScreenThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        L.printLog2File(TAG, "   " + Thread.currentThread().getName());
        initFileData();
        try {
            useActivityFeatureCapture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用Activity属性捕获屏幕；
     */
    private void useActivityFeatureCapture() {

        final String LAUNCH_ACTIVITY = "ui.activity.LaunchActivity";
        Activity topActivity = BaseApp.getTopActivity();
        String topActivityClassName = topActivity != null ? topActivity.getLocalClassName() : "No Activity !";

        L.printLog2File("   topActivity name :" + topActivityClassName);

        if (LAUNCH_ACTIVITY.equals(topActivityClassName)) {
            try {
                Thread.sleep(8 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BroadcastReceiverManager.sendBroadcast(new Intent(Constant.ACTION_DATA_CHANGE)
                    .putExtra(DATA_CHANGE_SCREENSHOT, true));
            L.printLog2File("   Thread.sleep(8s) and send broadcast !");
        } else if (TextUtils.isEmpty(topActivityClassName)) {

            if (BootUtil.isRoot()) {
                try {
                    L.printLog2File("   ADB (root) capture handle ");
                    captureScreenUseADB();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                activityHandle();
            }

        } else {
            BroadcastReceiverManager.sendBroadcast(new Intent(Constant.ACTION_DATA_CHANGE)
                    .putExtra(DATA_CHANGE_SCREENSHOT, true));
            L.e("Normal send broadcast !");
        }
        interrupt();
    }

    /**
     * 启动Activity and 定时处理；
     */
    private void activityHandle() {
        context.startActivity(new Intent(context, LaunchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(16 * 1000);

                    BroadcastReceiverManager.sendBroadcast(new Intent(Constant.ACTION_DATA_CHANGE)
                            .putExtra(DATA_CHANGE_SCREENSHOT, true));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                interrupt();
            }
        }).start();
    }

    /**
     * 开始捕获屏幕；
     */
    private void captureScreenUseADB() throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String command = "su -c 'screencap " + mFilePath + "' && adb pull " + mFilePath;
                boolean result = RootCommand(command);                                             //process 会导致当前线程阻塞，提前创建子线程延迟检查；
                L.e("-------------- Is capture success :" + result + "-----------------");
                interrupt();
            }
        }).start();

        Thread.sleep(3 * 1000);

        File file = new File(mFilePath);

        if (file.exists() && file.length() > 0) {
            L.printLog2File("   (ADB capture) LENGTH:" + file.length() + "  NAME:" + file.getName());
            new UploadImageThread(fileName, mFilePath).run();
        }
    }

    /**
     * 初始化文件数据；
     */
    private void initFileData() {
        mFilePath = Constant.getFilePath() + "screen/";
        File file = new File(mFilePath);
        if (!file.exists() && !file.mkdirs()) {
            L.printLog2File("   Create photo file path fail !");
            interrupt();
            return;
        }
        fileName = new SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(new Date()) + ".png";
        mFilePath += fileName;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @param command 命令：String apkRoot="chmod 777 "+getPackageCodePath();
     *                RootCommand(apkRoot);
     * @return 应用程序是/否获取Root权限
     */
    private boolean RootCommand(String command) {
        java.lang.Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            L.printLog2File(TAG, "Root shotscreen fail :" + e.getMessage());
            return false;
        } finally {

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return true;
    }

    /**
     * 终端当前线程；
     */
    private void interrupt() {
        try {
            Thread currentThread = Thread.currentThread();
            if (currentThread.isAlive() && !currentThread.isInterrupted()) {
                currentThread.interrupt();
                L.e("-----------" + "interrupt(): " + Thread.currentThread().getName() + "-----------");
            }
        } catch (Exception c) {
            L.e(c.getMessage());
        }
    }
}
