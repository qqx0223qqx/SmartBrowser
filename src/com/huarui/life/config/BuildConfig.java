package com.huarui.life.config;

/**
 * Created by wxLife on 2017/2/9 14:46.
 * 声明应用处于Debug模式还是Release模式；
 */
public class BuildConfig {

    public static final boolean isDebug = true;                                                     //开发环境或生产环境；

    private static final String[] IPs = {
            "http://192.168.3.114/laravel5.1/public/",
            "http://114.55.244.35:8800/laravel5.1/public/",
            "http://info.huaruizsh.com:8800/",
            "http://info.huaruizsh.com:8800/"};

    /**
     * 获取当前模式对应的ip地址；
     */
    private static String getBasicIp() {
        if (isDebug) {
            return IPs[2];
        }
        return IPs[3];
    }

    /**
     * 基础url前缀；
     */
    public static final class BasicUrl {

        /**
         * 第一次运行app时；
         */
        public static String FIRST_RUN_REQUEST_URL = getBasicIp() + "applogin";

        /**
         * 定时获取cmd请求指令url
         */
        public static String TIMER_REQUST_URL = getBasicIp() + "appgetcmd";

        /**
         * 定时上传动态参数，并获取是否含有后台指令如截屏，拍照等；
         */
        public static String TIMER_UPLOAD_DYNAMICATTR_URL = getBasicIp() + "appstatus";

        /**
         * 上传截屏图片资源地址；
         */
        public static String UPLOAD_SCREENSHOT_URL = getBasicIp() + "appfile";

        /**
         * 请求有没有新的默认资源url下载
         */
        public static String DEFAULT_IMG_DOWN_URL = getBasicIp() + "acquiesce";

        /**
         * 上传用户操作
         */
        public static String MONITOR_USER_STATE_URL = getBasicIp() + "userlog";

        /**
         * 上传日志文件url；
         */
        public static String UPLOAD_LOG_URL = getBasicIp() + "logfile";

        /**
         * 检查版本url
         */
        public static String CHECK_VER_URL = getBasicIp() + "appgetver";

        /**
         * 上传位置信息url;
         */
        public static String LOCATION_INFO_URL = getBasicIp() + "deviceaddr/";
    }
}
