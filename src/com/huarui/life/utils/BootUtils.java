package com.huarui.life.utils;

import android.content.Intent;
import android.os.Build;

import com.huarui.life.application.BaseApp;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import log.L;

/**
 * Created by HR_Life on 2017/1/16 14:27.
 */

public class BootUtils {

    private static final String TAG = "BootUtils";
    /**
     * 默认开机时间；
     */
    private static final String DEFAULT_POWER_ON_TIME = "07-00-00";                                 //注意时间格式为00-00-00不可为7-1-52;
    /**
     * 默认关机时间；
     */
    private static final String DEFAULT_POWER_OFF_TIME = "23-00-00";
    /**
     * 取消开机监听Action
     */
    private static final String CANCEL_POWER_ON_ACTION = "zysd.alarm.poweron.cancel";
    /**
     * 取消关机监听Action
     */
    private static final String CANCEL_POWER_OFF_ACTION = "zysd.alarm.poweroff.cancel";

    private static final String SET_POWER_ON_ACTION = "zysd.alarm.poweron.time";

    private static final String SET_POWER_OFF_ACTION = "zysd.alarm.poweroff.time";

    private static final String ROOT_NOW_ACTION = "reboot.zysd.now";

    private static final String A90_UPDATEALARM_ACTION = "com.byteflyer.updatealarm";

    /**
     * 取消定时开机功能；
     */
    private static void cancelPoweron() {
        BaseApp.getInstance().sendBroadcast(new Intent(CANCEL_POWER_ON_ACTION));
    }

    /**
     * 取消定时关机功能；
     */
    private static void cancelPoweroff() {
        BaseApp.getInstance().sendBroadcast(new Intent(CANCEL_POWER_OFF_ACTION));
    }

    /**
     * 通过发送广播的方式实现重启；
     */
    public static void reboot() {
        BaseApp.getInstance().sendBroadcast(new Intent(ROOT_NOW_ACTION));
    }

    /**
     * 兼容A90设置开关机；
     * param time PHP时间戳；
     */
    public static void setPowerOn(String timeL) {
        String nextPowerOnDate = DateUtil.specificNumDate(1);
        String nextPowerOnTime;
        if ("-1".equals(timeL)) {                                                                   //没有获取配置文件信息，初始化默认时间；
            nextPowerOnTime = DEFAULT_POWER_ON_TIME;
        } else if ("0".equals(timeL)) {                                                             //后台取消定时开关机功能；
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                BootUtils.cancelPoweron();
                return;
            } else {
                nextPowerOnDate = DateUtil.specificNumDate(5);
                nextPowerOnTime = DEFAULT_POWER_ON_TIME;
            }
        } else {
            nextPowerOnTime = formatDate(timeL);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {                             //A90板子发送监听开机；
            String[] split = nextPowerOnTime.split("-");
            Intent powerIntent = new Intent(A90_UPDATEALARM_ACTION);
            powerIntent.putExtra("flag", "1");
            powerIntent.putExtra("weeks", "127");
            powerIntent.putExtra("hour", split[0]);
            powerIntent.putExtra("minutes", split[1]);
            BaseApp.getInstance().getApplicationContext().sendBroadcast(powerIntent);
            L.printLog2File(TAG, "A90(" + Build.VERSION.SDK_INT + ") power-on： " + nextPowerOnDate + " ：" + nextPowerOnTime);

        } else {                                                                                    //A89板子发送监听开机；
            Intent powerIntent = new Intent(SET_POWER_ON_ACTION);
            powerIntent.putExtra("poweronday", nextPowerOnDate);
            powerIntent.putExtra("powerontime", nextPowerOnTime);
            BaseApp.getInstance().getApplicationContext().sendBroadcast(powerIntent);
            Logger.getLogger(TAG).info("A89(" + Build.VERSION.SDK_INT + ") power-on： " + nextPowerOnDate + " ：" + nextPowerOnTime);
        }
    }

    /**
     * 兼容A90设置默认关机
     * param time 为php 时间戳；
     */
    public static void setPowerOff(String timeL) {
        String powerOffDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String powerOffTime;

        if ("-1".equals(timeL)) {                                                                   //应用第一次运行；
            if (new Date().getHours() >= 23) {                                                      //如果第一次安装在23：00- 24：00,不对关机进行设置；
                return;
            }
            powerOffTime = DEFAULT_POWER_OFF_TIME;

        } else if ("0".equals(timeL)) {                                                             //后台设置为不启用定时开关机功能；
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                BootUtils.cancelPoweroff();
                return;
            } else {
                powerOffDate = DateUtil.specificNumDate(4);
                powerOffTime = DEFAULT_POWER_OFF_TIME;
            }
        } else {                                                                                    //后台修改定时时间；
            Date attrDate = new Date(Long.valueOf(timeL) * 1000);
            /*
            if (attrDate.before(new Date())) {                                                      //如果在默认关机关机后再次启动将不再进行指令；
				Logger.getLogger("setPowerOff").info("setPowerOff: " + " ************ " + attrDate.before(new Date()));
				return;                                                                             //不能采用这种方式，如果主机的日期不准确，有错误；
			}*/
            Date date = new Date();
            if (attrDate.getHours() > date.getHours() || (attrDate.getHours() == date.getHours() && attrDate.getMinutes() > (date.getMinutes() + 5))) {
                powerOffTime = formatDate(timeL);
            } else {
                L.printLog2File(TAG,"待设置的时钟/已发布的时钟位于当前时钟之前，不对设备进行关机设置 => " + attrDate.before(new Date()));
                return;
            }
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            String[] split = powerOffTime.split("-");
            Intent powerIntent = new Intent(A90_UPDATEALARM_ACTION);
            powerIntent.putExtra("flag", "2");
            powerIntent.putExtra("weeks", "127");
            powerIntent.putExtra("hour", split[0]);
            powerIntent.putExtra("minutes", split[1]);
            BaseApp.getInstance().getApplicationContext().sendBroadcast(powerIntent);
            L.printLog2File(TAG, "A90(" + Build.VERSION.SDK_INT + ") power-on ：" + powerOffDate + " ：" + powerOffTime);

        } else {
            Intent off = new Intent(SET_POWER_OFF_ACTION);
            off.putExtra("poweroffday", powerOffDate);
            off.putExtra("powerofftime", powerOffTime);
            BaseApp.getInstance().getApplicationContext().sendBroadcast(off);
            L.printLog2File(TAG, "A89(" + Build.VERSION.SDK_INT + ") power-off ：" + powerOffDate + " ：" + powerOffTime);
        }
    }

    /**
     * 执行线程：执行重新启动；
     */
    public static void executeReboot() {

        Process proc;  //关机
        try {
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (Exception e) {
            Logger.getLogger(TAG).info("Reboot fail : \n " + e.getMessage());
        }
    }

    /**
     * 执行线程：执行关机；
     */
    public static void executeShutDown() {
        if (isRoot()) {
            Process proc;  //关机
            try {
                proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
                proc.waitFor();
            } catch (Exception e) {
                Logger.getLogger(TAG).info("ShutDown fail : \n " + e.getMessage());
            }
        } else {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * LONG数据类型转为String
     */
    private static String formatDate(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("HH-mm-ss", Locale.CHINA);
        long lcc = Long.valueOf(time) * 1000;
        return sdr.format(new Date(lcc));
    }

    /**
     * 检查设备是否root；t
     */
    public static boolean isRoot() {
        boolean root = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                root = false;
            } else {
                root = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }
}
