package com.huarui.life.service;

import android.app.ActivityManager;
import android.content.Context;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.List;

import log.L;

/**
 * Created by HR_Life on 2017/4/14 : 14:41.
 * Package : com.huarui.life.services
 */

public class LocationServer {

    public static final String TAG = "LocationServer";
    public static final String LOCATION_REMOTE_PRO = "com.huarui.life:remote";

    private Context context;

    private LocationClient mLocationClient = null;

    private BDLocationListener mLocationListener = null;

    public static final double DEFAULT_LONGITUDE = 113.95300;

    public static final double DEFAULT_LATITUDE = 22.541600;

    public static final double LOCATION_ERROR = 4.9E-324;


    private LocationServer(Context context, BDLocationListener listener) throws Exception {
        this.context = context.getApplicationContext();
        if (listener == null) {
            throw new NullPointerException("定位监听回调 == null");
        }
        this.mLocationListener = listener;
        init(listener);
    }

    public static LocationServer createInstance(Context context, BDLocationListener listener) {       //如果后面没有listener传null即可；
        try {
            return new LocationServer(context, listener);
        } catch (Exception e) {
            L.printLog2File(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * 初始化定位参数及数据；
     */
    private void init(BDLocationListener listener) {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(context);
        }
        initLocation();
        if (listener != null) {
            mLocationClient.registerLocationListener(listener);
        }
    }

    /**
     * 开始定位；
     */
    public void startLocate() {
        if (mLocationClient != null) {
            mLocationClient.start();
        }
    }

    /**
     * 停止定位；
     */
    public void stopLocate() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    public LocationClient getLocationClient() {
        return mLocationClient;
    }

    /**
     * 结束定位资源；
     */
    public void releaseLocate() {
        if (mLocationClient != null) {
            stopLocate();
            if (mLocationListener != null) {
                mLocationClient.unRegisterLocationListener(mLocationListener);
                mLocationListener = null;
            }
            mLocationClient = null;
            context = null;
        }
        L.e("Release location !");
    }

    /**
     * 初始化定位数据需求；
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span = 0;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(false);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(false);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    /**
     * kill定位进程；
     */
    public void killLocationProcesses() {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {

            if (LOCATION_REMOTE_PRO.equals(info.processName)) {
                android.os.Process.killProcess(info.pid);
                //activityManager.killBackgroundProcesses(processName);
                L.e("Kill location :remote");
                break;
            }
        }
        context = null;
    }
}
