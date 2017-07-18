package com.huarui.life.bean;

import android.content.Context;
import android.text.TextUtils;

import com.huarui.life.utils.VersionUtils;

/**
 * Created by HR_Life on 2016/12/7 14:42 15:19.
 * 保存定时任务上传变化数据；
 */

public class DynamicDataBean {

    private static DynamicDataBean dataBean = null;

    public static DynamicDataBean getInstance (){
        if (dataBean == null) {
            dataBean = new DynamicDataBean();
        }
        return dataBean;
    }

    private String deviceId;

    private String version;

    private String ucpu;

    private String umemory;

    private String  ustorage;

    private String bandwidth;

    private String maxbandwidth  ;

    private String nettype;

    public String getNettype() {
        return nettype;
    }

    public void setNettype(String nettype) {
        this.nettype = nettype;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUcpu() {
        return ucpu;
    }

    public void setUcpu(String ucpu) {
        this.ucpu = ucpu;
    }

    public String getUmemory() {
        return umemory;
    }

    public void setUmemory(String umemory) {
        this.umemory = umemory;
    }

    public String getUstorage() {
        return ustorage;
    }

    public void setUstorage(String ustorage) {
        this.ustorage = ustorage;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getMaxbandwidth() {
        return maxbandwidth;
    }

    public void setMaxbandwidth(String maxbandwidth) {
        this.maxbandwidth = maxbandwidth;
    }

    public String getVersion(Context c) {
        if (TextUtils.isEmpty(version)) {
            version = VersionUtils.getVersionName(c);
        }
        return version;
    }

    @Override
    public String toString() {
        return "DynamicDataBean{" +
                "DeviceId='" + deviceId + '\'' +
                ", Version='" + version + '\'' +
                ", Ucpu='" + ucpu + '\'' +
                ", Umemory='" + umemory + '\'' +
                ", Ustorage='" + ustorage + '\'' +
                ", bandwidth='" + bandwidth + '\'' +
                ", maxbandwidth='" + maxbandwidth + '\'' +
                '}';
    }
}
