package com.huarui.life.config;

import android.os.Environment;

import java.io.File;

/**
 * Created by HR_Life on 2016/11/7 14:20 14:42 15:19.
 */

public class Constant {

    /**
     * 获取默认文件存储路径
     */
    public static String getFilePath() {
        String dataPath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dataPath = Environment.getExternalStorageDirectory() + File.separator + "hrlife" + File.separator;
        } else {
            dataPath = Environment.getDataDirectory() + File.separator + "hrlife" + File.separator;
        }
        return dataPath;
    }

    public static final int NET_CODE = 200;
    //----------------------------------------------------------------------------------------------

    /**
     * 默认URL!
     */
    public static String DEFAULT_URL = "http://www.huaruilife.com";

    //----------------------------------------------------------------------------------------------

    /**
     * 设备类型参数：1为横屏 2为竖屏！
     */
    public static final String DEVICE_TYPE = "2";

    //----------------------------------------------------------------------------------------------

    /**
     * 账号或密码错误
     */
    public static final String STATUS_VERNAME_ERROR_CODE = "10001";

    /**
     * 账号或密码错误
     */
    public static final String STATUS_ACCPWS_ERROR_CODE = "10002";

    /**
     * 参数错误
     */
    public static final String STATUS_ATTR_ERROR_CODE = "10003";

    /**
     * 设备id错误
     */
    public static final String STATUS_DEVICEID_ERROR_CODE = "10004";

    /**
     * 设备解绑
     */
    public static final String STATUS_NOSHOW_CODE = "10005";

    /**
     * 当设备更换组后标识code；
     */
    public static final String STATUS_NO_RES_CODE = "10009";

    /**
     * 当设备更换组后标识code；
     */
    public static final String STATUS_GROUP_CHANGE_CODE = "100010";

    //----------------------------------------------------------------------------------------------

    /**
     * 请求成功码
     */
    public static final String STATUS_SECCUSS_CODE = "0";

    /**
     * 上传动态参数返回拍照指令
     */
    public static final String STATUS_DEVICEID_ERROR = "1";

    /**
     * 上传动态参数返回拍照指令
     */
    public static final String CMD_TAKEPHOTO_CODE = "1";

    /**
     * 上传动态参数返回截屏指令
     */
    public static final String CMD_SCREENSHOT_CODE = "2";

    /**
     * 上传动态参数返回截屏指令
     */
    public static final String CMD_REBOOT_CODE = "3";

    /**
     * 上传动态参数返回设置音量指令
     */
    public static final String CMD_VOLUMECONTROL_CODE = "4";

    /**
     * 上传动态参数返回设置音量指令
     */
    public static final String CMD_SETPOWERTIME_CODE = "5";

    /**
     * 上传动态参数返回截屏指令
     */
    public static final String CMD_SHUTDOWN_CODE = "6";

    /**
     * 上传动态参数返回:上传日志指令；
     */
    public static final String CMD_UPLOAD_LOG_CODE = "7";

    /**
     * 上传动态参数返回：J
     */
    public static final String CMD_CHECK_VERSION_CODE = "8";

    /**
     * 上传动态参数返回：执行录像
     */
    public static final String CMD_LOCATION_CODE = "9";

    //----------------------------------------------------------------------------------------------

    /**
     * 页面加载启动后第一次请求cmd的间隔时间~30s
     */
    public static final long TIMER_FIRST_DELAY_CMD = 1 * 20 * 1000;

    /**
     * 定时请求Server的时间间隔
     */
    public static final long TIMER_REQUEST_CMD = 3 * 1000;

    /**
     * 定时上传动态数据在定时请求cmd后延迟时间
     */
    public static final long TIMER_UPLOAD_DYNIMIC_DELAYED = 3 * 1000;

    /**
     * 定时检测倒计时；
     */
    public static final long TIMER_MONITOR = 3 * 60 * 1000;


    //----------------------------------------------------------------------------------------------
    /**
     * IM阿甘ACTIVITY Key reason；
     */
    public static final String IMAGE_REASON = "reason";
    /**
     * 数据变化  Key screen_shot；
     */
    public static final String DATA_CHANGE_SCREENSHOT = "screen_shot";
    /**
     * 数据变化  Key only_white_list；
     */
    public static final String DATA_CHANGE_ONLY_WHITELIST = "only_white_list";
    /**
     * 数据变化  Key white_list；
     */
    public static final String DATA_CHANGE_WHITELIST = "white_list";


    public static final String ACTION_DATA_CHANGE = "com.huarui.life.ACTION_DATA_CHANGE_MAIN";

}
