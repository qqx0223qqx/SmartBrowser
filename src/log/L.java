package log;


import android.util.Log;

import com.huarui.life.config.BuildConfig;

import org.apache.log4j.Logger;


/**
 * Created by HR_Life on 2017/4/12 : 14:39.
 * Package : ${PACKAGE_NAME}
 */

public class L {

    private L(String logFlag, String msg) {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    private static final String TAG = "L";

    public static void i(String msg) {
        if (BuildConfig.isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (BuildConfig.isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (BuildConfig.isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (BuildConfig.isDebug)
            Log.v(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.isDebug)
            Log.v(tag, msg);
    }

    /**
     * 输出日志到文件中
     */
    public static void printLog2File(String msg) {
        printLog2File(TAG, msg);
    }

    /**
     * 输出日志到文件中
     */
    public static void printLog2File(String flag, String msg) {
        Logger.getLogger(flag).info(msg);
    }
}
