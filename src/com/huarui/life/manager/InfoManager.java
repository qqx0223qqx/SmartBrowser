package com.huarui.life.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.SharePreFileConfig;
import com.huarui.life.utils.PreferenceConfig;

import static com.huarui.life.config.SharePreFileConfig.LAUNCH_DATA_FILE_NAME;


/**
 * Created by HR_Life on 2016/11/7 14:42 15:19.
 * 保存当前数据信息
 */

public class InfoManager {

    private final Context context;

    private InfoManager() {
        context = BaseApp.getInstance().getApplicationContext();
    }

    @SuppressLint("StaticFieldLeak")
    private static InfoManager instance = new InfoManager();

    public static InfoManager getInstance() {
        return instance;
    }

    /* 设备标识id */
    private int mDeviceId = -1;
    /* 已经发布的url地址 */
    private String mPublicUrl;
    /* 已经发布的url版本 */
    private int mPublicUrlVersion = -1;
    /* 是否启用白名单 */
    private boolean mIsLaunchWhiteList = false;
    /* 默认请求资源的版本 */
    private int mResVersion = -1;
    /* 定时开关机设置值 */
    private String mPowerValue;
    /* 应用是否启动过 */
    private boolean mRan;
    /* 当前发布的url地址 */
    private String mCurrentUrl;
    /* 当前发布的url 版本 */
    // private int mCurrentUrlVersion;


    public int getmDeviceId() {
        if (-1 == mDeviceId) {
            mDeviceId = PreferenceConfig.getIntConfig(
                    context, LAUNCH_DATA_FILE_NAME,
                    SharePreFileConfig.KEY_DEVICE_ID);

        }
        return mDeviceId;
    }

    public void setmDeviceId(int deviceId) {
        this.mDeviceId = deviceId;
        PreferenceConfig.setIntConfig(
                context, LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_DEVICE_ID, deviceId);
    }

    public String getmPublicUrl() {
        if (TextUtils.isEmpty(mPublicUrl)) {
            mPublicUrl = PreferenceConfig.getConfig(
                    context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                    SharePreFileConfig.KEY_PUBLIC_URL);
        }
        return mPublicUrl;
    }

    public void setmPublicUrl(String url) {
        this.mPublicUrl = url;
        PreferenceConfig.setConfig(context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_PUBLIC_URL, url);
    }

    public int getmPublicUrlVersion() {
        if (-1 == mPublicUrlVersion) {
            mPublicUrlVersion = PreferenceConfig.getIntConfig(
                    context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                    SharePreFileConfig.KEY_PUBLIC_URL_VERSION);
        }
        return mPublicUrlVersion;
    }

    public void setmPublicUrlVersion(int version) {
        this.mPublicUrlVersion = version;
        PreferenceConfig.setIntConfig(context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_PUBLIC_URL_VERSION, version);
    }

    public boolean ismIsLaunchWhiteList() {
        if (!mIsLaunchWhiteList) {
            mIsLaunchWhiteList = PreferenceConfig.getBooleanConfig(
                    context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                    SharePreFileConfig.KEY_IS_LAUNCH_WHITELIST);
        }
        return mIsLaunchWhiteList;
    }

    public void setmIsLaunchWhiteList(boolean mIsLaunchWhiteList) {
        this.mIsLaunchWhiteList = mIsLaunchWhiteList;
        PreferenceConfig.setBooleanConfig(context, LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_IS_LAUNCH_WHITELIST, mIsLaunchWhiteList);
    }

    public int getmResVersion() {
        if (-1 == mResVersion) {
            mResVersion = PreferenceConfig.getIntConfig(
                    context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                    SharePreFileConfig.KEY_PUBLIC_RES_VERSION);
        }
        return mResVersion;
    }

    public void setmResVersion(int version) {
        this.mPublicUrlVersion = version;
        PreferenceConfig.setIntConfig(context, LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_PUBLIC_RES_VERSION, version);
    }

    public String getmPowerValue() {
        if (TextUtils.isEmpty(mPowerValue)) {
            mPowerValue = PreferenceConfig.getConfig(context,
                    LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_POWER);
            if (TextUtils.isEmpty(mPowerValue)) {
                mPowerValue = "-1,-1";
            }
        }
        return mPowerValue;
    }

    public boolean ismRan() {
        if (!mRan) {
            mRan = PreferenceConfig.getBooleanConfig(context, LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_RAN);
        }
        return mRan;
    }

    public void setmRan(boolean mRan) {
        this.mRan = mRan;
        PreferenceConfig.setBooleanConfig(context, LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_RAN, mRan);
    }

    public void setmPowerValue(String mPowerValue) {
        this.mPowerValue = mPowerValue;
        PreferenceConfig.setConfig(context, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_POWER, mPowerValue);
    }

    public String getmCurrentUrl() {
        return mCurrentUrl;
    }

    public void setmCurrentUrl(String mCurrentUrl) {
        this.mCurrentUrl = mCurrentUrl;
    }

   /* public int getmCurrentUrlVersion() {
        return mCurrentUrlVersion;
    }

    public void setmCurrentUrlVersion(int mCurrentUrlVersion) {
        this.mCurrentUrlVersion = mCurrentUrlVersion;
    }*/

}
