package com.huarui.life.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import log.L;

/**
 * Created by qqx02 on 2016-11-15 : 15:26.
 * Package : ${PACKAGE_NAME}
 */

public class SilentInstallUtils {

    private static final java.lang.String TAG = "SilentInstallUtil";

    /**
     * 执行具体的静默安装逻辑，需要手机ROOT。
     *
     * @param apkPath 要安装的apk文件的路径
     * @return 安装成功返回true，安装失败返回false。
     */
    public static boolean install(String apkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {

            Process process = Runtime.getRuntime().exec("su");                                      // 申请su权限
            dataOutputStream = new DataOutputStream(process.getOutputStream());

            String command = "pm install -r " + apkPath + "\n";                                      // 执行pm install命令
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();

            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();

            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;

            while ((line = errorStream.readLine()) != null) {                                       // 读取命令的执行结果
                msg += line;
            }
            L.e(TAG, "====install msg is ==== :" + msg);
            L.printLog2File(TAG, "Silent install :" + msg);

            if (!msg.contains("Failure")) {                                                         // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}