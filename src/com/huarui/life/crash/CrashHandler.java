package com.huarui.life.crash;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.ui.activity.LaunchActivity;
import com.huarui.life.utils.T;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import log.L;

import static android.content.Context.ALARM_SERVICE;

public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    @SuppressLint("StaticFieldLeak")
    private static CrashHandler instance;

    private Context mContext;

    private UncaughtExceptionHandler mDefaultHandler;

    /**
     * 用于保存异常信息；
     */
    private Map<String, String> infos = new HashMap<>();

    private static final String VERSION_NAME = "versionName";

    private static final String VERSION_CODE = "versionCode";

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null)
            instance = new CrashHandler();
        return instance;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     */
    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);                                          // 如果用户没有处理则让系统默认的异常处理器来处理
        } else {
            try {
                Thread.sleep(3000);
                BaseApp.finishAll();
                Runtime.getRuntime().gc();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(final Throwable ex) {

        if (ex == null) {
            return true;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();                                                                   // Toast 显示需要出现在一个线程的消息队列中
                T.showToast(mContext, "友情提示：", "系统数据更新，程序即将重新启动，请稍后...", 1);
                Intent intent = new Intent(mContext, LaunchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                ((AlarmManager) mContext.getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis() + 5000, pendingIntent);
                Looper.loop();
            }
        }.start();

        collectCrashDeviceInfo(mContext);                                                           // 收集设备信息
        final File file = saveCrashInfoToFile(ex);                                                  // 保存错误报告文件
        if (file != null && file.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendFile2Server(file);
                }
            }).start();
        }
        return true;
    }

    /**
     * 收集程序崩溃的设备信息
     */
    private void collectCrashDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                infos.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
                infos.put(VERSION_CODE, pi.versionCode + "");
            }
        } catch (NameNotFoundException e) {
            L.e(TAG, e.getMessage());
        }
        Field[] fields = Build.class.getDeclaredFields();                                           // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        for (Field field : fields) {                                                                // 返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段
            try {
                field.setAccessible(true);                                                          // 通过设置Accessible属性为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异常
                infos.put(field.getName(), field.get(null) + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        L.e("Collect device info finish !");
    }

    /**
     * 保存错误信息到文件中
     */
    private File saveCrashInfoToFile(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        FileOutputStream fos = null;
        String filePath = "";
        try {
            DateFormat formatter = new SimpleDateFormat("yyMMdd HH-mm-ss", Locale.getDefault());
            String time = formatter.format(new Date());
            String fileName = "c-" + time + ".log";

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                filePath = Constant.getFilePath() + "crash" + File.separator;
                File dir = new File(filePath);
                if (!dir.exists() && !dir.mkdir()) {
                    L.printLog2File(TAG, "Crash file path make fail !");
                    return null;
                }
                filePath += fileName;
                L.e(TAG, "Crash File :" + filePath);
                fos = new FileOutputStream(filePath);
                fos.write(sb.toString().getBytes());
                fos.flush();
                L.e(TAG, "Crash info save to file finish :" + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert fos != null;
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(filePath);
    }

    /**
     * 上传崩溃日志到server
     */
    private void sendFile2Server(File file) {

        int deviceId = InfoManager.getInstance().getmDeviceId();

        String url = BuildConfig.BasicUrl.UPLOAD_LOG_URL + "/" + 2 + "/" + deviceId;
        OkHttpUtils.post()
                .url(url)
                .addFile("filename", "file", file)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(okhttp3.Call call, Exception e, int id) {
                        L.printLog2File(TAG, "Upload crash log error !" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        L.printLog2File(TAG, "Upload crash log finish :" + response);
                    }
                });
    }
}
