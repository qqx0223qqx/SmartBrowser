package com.huarui.life.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by HR_Life on 2016/12/8 14:42 15:19.
 */

@SuppressWarnings("FieldCanBeLocal")
public class TrafficNetUtils {

    private TrafficNetUtils() {
    }

    private static TrafficNetUtils instance = null;

    public static TrafficNetUtils getInstance() {
        if (instance == null) {
            instance = new TrafficNetUtils();
        }
        return instance;
    }

    private int maxBandWight = 0;

    // 流量数据
    String[] ethData = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0"};
    String[] gprsData = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0"};
    String[] wifiData = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0"};

    // 用来存储前一个时间点的数据
    String[] data = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0"};

    // 以太网
    final String ETHLINE = "eth0";
    // wifi
    final String WIFILINE = "wlan0";
    // gprs
    final String GPRSLINE = "rmnet0";

    /**
     * 读取系统流量文件
     */
    private void readDev() {
        FileReader fr = null;
        try {
            String DEV_FILE = "/proc/self/net/dev";
            fr = new FileReader(DEV_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert fr != null;
        BufferedReader bufr = new BufferedReader(fr, 500);
        String line;
        String[] data_temp;
        String[] netData;
        int k;
        int j;
        // 读取文件，并对读取到的文件进行操作
        try {
            while ((line = bufr.readLine()) != null) {
                data_temp = line.trim().split(":");
                if (line.contains(ETHLINE)) {
                    netData = data_temp[1].trim().split(" ");
                    for (k = 0, j = 0; k < netData.length; k++) {
                        if (netData[k].length() > 0) {
                            ethData[j] = netData[k];
                            j++;
                        }
                    }
                } else if (line.contains(GPRSLINE)) {
                    netData = data_temp[1].trim().split(" ");
                    for (k = 0, j = 0; k < netData.length; k++) {
                        if (netData[k].length() > 0) {
                            gprsData[j] = netData[k];
                            j++;
                        }
                    }
                } else if (line.contains(WIFILINE)) {
                    netData = data_temp[1].trim().split(" ");
                    /*for (k = 0, j = 0; k < netData.length; k++) {
                        //wifiData[j] = netData[k];
                        j++;
                    }*/
                }
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 实时读取系统流量文件，更新
     */
    public String getCurrentNetData() {
        // 读取系统流量文件

        readDev();

        // 计算增量
        int[] delta = new int[12];
        delta[0] = Integer.parseInt(ethData[0]) - Integer.parseInt(data[0]);
        delta[1] = Integer.parseInt(ethData[1]) - Integer.parseInt(data[1]);
        delta[2] = Integer.parseInt(ethData[8]) - Integer.parseInt(data[2]);
        delta[3] = Integer.parseInt(ethData[9]) - Integer.parseInt(data[3]);
        delta[4] = Integer.parseInt(gprsData[0]) - Integer.parseInt(data[4]);
        delta[5] = Integer.parseInt(gprsData[1]) - Integer.parseInt(data[5]);
        delta[6] = Integer.parseInt(gprsData[8]) - Integer.parseInt(data[6]);
        delta[7] = Integer.parseInt(gprsData[9]) - Integer.parseInt(data[7]);
        delta[8] = Integer.parseInt(wifiData[0]) - Integer.parseInt(data[8]);
        delta[9] = Integer.parseInt(wifiData[1]) - Integer.parseInt(data[9]);
        delta[10] = Integer.parseInt(wifiData[8]) - Integer.parseInt(data[10]);
        delta[11] = Integer.parseInt(wifiData[9]) - Integer.parseInt(data[11]);

        data[0] = ethData[0];
        data[1] = ethData[1];
        data[2] = ethData[8];
        data[3] = ethData[9];
        data[4] = gprsData[0];
        data[5] = gprsData[1];
        data[6] = gprsData[8];
        data[7] = gprsData[9];
        data[8] = wifiData[0];
        data[9] = wifiData[1];
        data[10] = wifiData[8];
        data[11] = wifiData[9];

        // 每秒下载的字节数kb/s
        int traffic_data = (delta[0] + delta[4] + delta[8]) / (1024 * 3);

        maxBandWight = traffic_data > maxBandWight ? traffic_data : maxBandWight;
        return traffic_data + "kb/s";
    }

    public String getMaxBandWight() {
        return maxBandWight + "b/s";
    }
}
