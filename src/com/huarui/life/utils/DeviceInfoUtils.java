package com.huarui.life.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.LauncherActivity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by HR_Life on 2016/11/9 14:20 14:42 15:19.
 */

public class DeviceInfoUtils {

    public static final String TAG = DeviceInfoUtils.class.getSimpleName();
    /**
     * 设置cpu最大频率生成文件路径！
     */
    private final static String kCpuInfoMaxFreqFilePath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    private static ActivityManager am;

    /**
     * 获取最大cpu频率******************
     *
     * @return
     */
    public static int getMaxCpuFreq() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kCpuInfoMaxFreqFilePath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        return result;
    }

    /**
     * 设置cpu最小频率生成文件路径！
     */
    private final static String kCpuInfoMinFreqFilePath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";

    /**
     * 获取CPU最小频率（单位KHZ）
     *
     * @return
     */
    public int getMinCpuFreq() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kCpuInfoMinFreqFilePath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return result;
    }

    /**
     * 获取CPU名字
     */
    public String getCpuName() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader("/proc/cpuinfo");
            br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);

            return array[1];
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return "";
    }


    /**
     * 实时获取使用cpu 使用百分比！******************
     */
    public static String readCpuUsage() {
        long total = 0;
        long idle = 0;
        double f1 = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);

            double usage = (currTotal - total) * 100.0f / (currTotal - total + currIdle - idle);

            BigDecimal b = new BigDecimal(usage);
            f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return f1 + "%";
    }

    /**
     * 获取设备ip地址******************
     */
    public static String getIpAddress(Context mContext) {
        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            return "";
        }

        return ipAddress;
    }

    /**
     * 获取设备IMEI号码标识
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds")
        String iMei = telephonyManager.getDeviceId();

        String mac = getMacadd(context);
        if (TextUtils.isEmpty(mac)) {
            mac = Arrays.toString(getMacAddreses());
        }
        iMei += mac;
        return iMei;
    }

    /**
     * 获取手机IMSI号码标识
     *
     * @param context
     * @return
     */
    public static String getIMSI(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds") String iMsi = mTelephonyMgr.getSubscriberId();
        return iMsi;
    }

    /**
     * 获取总内存,与cpu相似读取文件******************
     */
    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件

        String str2;

        String[] arrayOfString;

        long initial_memory = 0;

        try {

            FileReader localFileReader = new FileReader(str1);

            BufferedReader localBufferedReader = new BufferedReader(

                    localFileReader, 8192);

            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");

            /*for (String num : arrayOfString) {

                Log.i(str2, num + "\t");

            }*/

            initial_memory = Integer.valueOf(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte

            localBufferedReader.close();

        } catch (IOException ignored) {

        }

        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化

    }

    /**
     * 获取可用内存(闪存)******************
     */
    public static String getAvailMemory(Context context) {// 获取当前android可用内存大小

        if (am == null) {
            am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //long availMem = mi.availMem; //当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 获取当前系统版本信息
     *
     * @param c
     * @return
     */
    public String getCurrSysVersion(Context c) {
        return Build.VERSION.RELEASE;
    }

    /**
     * SDCARD是否存
     */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatFileSize(availableBlocks * blockSize, false);
    }

    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return formatFileSize(totalBlocks * blockSize, false);
    }

    /**
     * 获取SDCARD剩余存储空间******************
     */
    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return formatFileSize(availableBlocks * blockSize, false);
        } else {
            return "错误";
        }
    }

    /**
     * 获取SDCARD总的存储空间******************
     */
    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return formatFileSize(totalBlocks * blockSize, false);
        } else {
            return "错误";
        }
    }

    /**
     * 获取系统总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    public static String getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return formatFileSize(Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024L, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static String getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return formatFileSize(memoryInfo.availMem, false);
    }

    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");

    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

    /**
     * 单位换算
     *
     * @param size      单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString;
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }

    /**
     * 获取屏幕像素参数
     *
     * @param c
     * @return
     */
    public static String getDisplayInfo(LauncherActivity c) {
        int ver = Build.VERSION.SDK_INT;
        DisplayMetrics metric = new DisplayMetrics();
        Display display = c.getWindowManager().getDefaultDisplay();
        display.getMetrics(metric);
        int width = metric.widthPixels;                                                             // 屏幕宽度（像素）
        int height = metric.heightPixels;                                                           // 屏幕高度（像素）
        if (ver < 13) {
            height = metric.heightPixels;
        } else if (ver == 13) {
            try {
                Method mt = display.getClass().getMethod("getRealHeight");
                height = (Integer) mt.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ver > 13) {
            try {
                Method mt = display.getClass().getMethod("getRawHeight");
                height = (Integer) mt.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return width + "*" + height;
    }


    public static String getMacadd(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String result = "";
        if (manager != null) {
            result = manager.getConnectionInfo().getMacAddress();
        }
        return result;
    }

    public static byte[] getMacAddreses() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        while (interfaces.hasMoreElements()) {
            final NetworkInterface ni = interfaces.nextElement();
            try {
                if (ni.isLoopback() || ni.isPointToPoint() || ni.isVirtual())
                    continue;
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            byte[] macAddress = null;
            try {
                macAddress = ni.getHardwareAddress();
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (macAddress != null && macAddress.length > 0)
                return macAddress;
        }
        return null;
    }
}
