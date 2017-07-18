package com.huarui.life.ui.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.google.gson.Gson;
import com.huarui.life.R;
import com.huarui.life.application.BaseApp;
import com.huarui.life.config.BuildConfig;
import com.huarui.life.config.Constant;
import com.huarui.life.config.SharePreFileConfig;
import com.huarui.life.entity.FirstRunEntity;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.service.ConnectService;
import com.huarui.life.service.LocationServer;
import com.huarui.life.service.MonitorService;
import com.huarui.life.thread.TakePhotoThread;
import com.huarui.life.utils.AppUtil;
import com.huarui.life.utils.BootUtil;
import com.huarui.life.utils.BootUtils;
import com.huarui.life.utils.DeviceInfoUtils;
import com.huarui.life.utils.NetworkUtils;
import com.huarui.life.utils.PreferenceConfig;
import com.huarui.life.utils.T;
import com.huarui.life.utils.VersionUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import log.L;
import okhttp3.Call;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;
import static com.huarui.life.config.Constant.NET_CODE;
import static com.huarui.life.config.SharePreFileConfig.KEY_RAN;
import static com.huarui.life.config.SharePreFileConfig.LAUNCH_DATA_FILE_NAME;


public class LaunchActivity extends AppCompatActivity {

    private static String TAG = "LaunchActivity";

    /* 定位错误返回值；*/
    private final String LOCATION_ERROR = "4.9E-324";
    /* 数据管理器;*/
    private InfoManager infoManager;
    /* 网络错误计算器，设备唯一标识; */
    private int mCountNetError, mDeviceId;
    /* 初始化数据;*/
    private String mCpuMaxFreq = "", mTotalMemory = "", mLatitude = "22.541600", mLongitude = "113.95300", mSysVersion = "", iMEI = "", mAddress = "", mTotalExternalStorage = "";
    /* DeviceId显示控件；*/
    private TextView mTvDeviceId;
    /* permission grade */
    private Button mBtnGrante;
    /* Location server instance! */
    private LocationServer mLocationServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        infoManager = InfoManager.getInstance();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseApp.getThreadService().submit(new InitDataThread());
            }
        }, 3000);

        String versionName = VersionUtils.getVersionName(this);
        String version = "VER : " + versionName;
        ((TextView) findViewById(R.id.version_show)).setText(version);
        mTvDeviceId = (TextView) findViewById(R.id.device_id_show);                                  //设备ID显示控件
    }

    /**
     * 设备id显示内容转换；
     */
    private String visibleDeviceId(int deviceId) {

        String visibleId;
        int idLength = String.valueOf(Math.abs(deviceId)).length();
        switch (idLength) {
            case 1:
                visibleId = "00000" + deviceId;
                break;
            case 2:
                visibleId = "0000" + deviceId;
                break;
            case 3:
                visibleId = "000" + deviceId;
                break;
            case 4:
                visibleId = "00" + deviceId;
                break;
            case 5:
                visibleId = "0" + deviceId;
                break;
            default:
                visibleId = String.valueOf(deviceId);
        }
        L.e(TAG, "No: " + visibleId);
        return "NO: " + visibleId;
    }

    /**
     * 初始化权限界面；
     */
    final boolean[] choices = {false, false, false};                                                // 设置默认选中的选项，全为false默认均未选中

    private void initPermissions() {

        ArrayList<String> list = new ArrayList();
        list.add(0, LaunchActivity.this.getString(R.string.permission_location));
        list.add(1, LaunchActivity.this.getString(R.string.permission_camera));
        list.add(2, LaunchActivity.this.getString(R.string.permission_root));

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.title);
        builder.setCancelable(false);
        builder.setTitle(LaunchActivity.this.getString(R.string.dailog_tile_prompt));
        View view = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
        ListView listView = (ListView) view.findViewById(R.id.custom_dialog_list_view);
        mBtnGrante = (Button) view.findViewById(R.id.custom_dialog_btn);
        TextView tvPrompt = (TextView) view.findViewById(R.id.custom_dialog_tv);
        Adapter adapter = new Adapter(this, list, choices, tvPrompt);
        listView.setAdapter(adapter);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        mBtnGrante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canFinish = true;
                for (boolean b : choices) {
                    if (!b) {
                        canFinish = false;
                        break;
                    }
                }
                if (canFinish) {

                    PreferenceConfig.setBooleanConfig(LaunchActivity.this,
                            SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                            SharePreFileConfig.KEY_PROMPT, true);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            initFirstRunData();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 查看设备是否已经root
     */
    private boolean initRoot() {
        if (BootUtils.isRoot()) {
            try {
                Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            Toast.makeText(this, LaunchActivity.this.getString(R.string.root_fail_prompt),
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 如果已经运行过或者第一次运行拿到deviceId后直接启动Service；
     */
    private void checkNetAndPageSkip() {

        if (NetworkUtils.isNetworkAvailables(this)) {
            this.startService(new Intent(this, ConnectService.class));
            return;
        }
        if (mCountNetError >= 5) {
            T.showToast(this, "Net error :", "网络连接异常，16s后重新请求！", 1);
            this.startService(new Intent(this, ConnectService.class));
        } else {
            T.showToast(this, "Net error :", "网络连接异常，8s后重新请求（" + mCountNetError + "）！", 1);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkNetAndPageSkip();
                }
            }, 8000);
            mCountNetError += 1;
        }
    }

    /**
     * 初始化第一次运行时的参数：都为不可变固定参数；
     */
    private void initFirstRunData() {

        if (!NetworkUtils.isNetworkAvailables(LaunchActivity.this)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    T.showToast(LaunchActivity.this, "Net error :", "-- 当前网络连接不可用，请检查后重新打开应用！--", 1);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LaunchActivity.this.finish();
                }
            }, 3000);
            return;
        }

        iMEI = PreferenceConfig.getConfig(this, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_IMEI);                                                       //初始化时先查找已有设备iMEI,避免已经安装设备删除ID后创建新的iMEI，产生新的DeviceId；
        if (isEmpty(iMEI)) {
            iMEI = DeviceInfoUtils.getIMEI(this);
            if (isEmpty(iMEI)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LaunchActivity.this, LaunchActivity.this.getString(
                                R.string.no_Imei), Toast.LENGTH_LONG).show();
                    }
                });
                L.printLog2File(TAG, "IMEI is null ! Don`t get it!");
                this.finish();
                System.exit(0);
                return;
            }
        }
        mCpuMaxFreq = AppUtil.getMaxCpuFreq() + "";
        mTotalMemory = AppUtil.getTotalMemorySize(this);
        mTotalExternalStorage = AppUtil.getTotalExternalMemorySize();
        mSysVersion = Build.VERSION.RELEASE;

        sendFirstRunRequest();
        L.printLog2File(TAG, "iMEI : " + iMEI);
    }

    /**
     * KEY_PROMPT
     * 首次运行初始化定位参数；
     */
    private void initLocations() {

        mLocationServer = LocationServer.createInstance(LaunchActivity.this, new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {

                String longitude = bdLocation.getLongitude() + "";
                String latitude = bdLocation.getLatitude() + "";
                String addr = bdLocation.getAddrStr();

                mAddress = TextUtils.isEmpty(addr) ? "广东省深圳市" : addr;

                if (!LOCATION_ERROR.equalsIgnoreCase(longitude)) {
                    mLongitude = longitude;
                }
                if (!LOCATION_ERROR.equalsIgnoreCase(latitude)) {
                    mLatitude = latitude;
                }
                L.printLog2File(TAG, "Get location :" + mAddress + " longitude : " + mLongitude + " latitude ：" + mLatitude);
                Toast.makeText(LaunchActivity.this, mAddress, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        });

        if (mLocationServer != null) {
            mLocationServer.startLocate();
        }
    }

    /**
     * 第一次启动，获取DeviceId Flag；
     */
    private void sendFirstRunRequest() {

        HashMap<String, String> params = new HashMap<>();
        params.put("imei", iMEI);
        params.put("name", mAddress);
        params.put("mode", Constant.DEVICE_TYPE);
        params.put("system", mSysVersion);
        params.put("longitude", mLongitude);
        params.put("latitude", mLatitude);
        params.put("cpu", mCpuMaxFreq);
        params.put("memory", mTotalMemory);
        params.put("storage", mTotalExternalStorage);

        OkHttpUtils.post()
                .url(BuildConfig.BasicUrl.FIRST_RUN_REQUEST_URL)
                .params(params)
                .build()
                .execute(new Callback<FirstRunEntity>() {
                    @Override
                    public FirstRunEntity parseNetworkResponse(Response response, int i) throws Exception {
                        if (NET_CODE == response.code()) {
                            String fJson = response.body().string();
                            L.printLog2File(TAG, "Request get deviceId => Net callback :" + fJson);
                            return new Gson().fromJson(fJson, FirstRunEntity.class);
                        }
                        L.printLog2File(TAG, "First run net-code:" + response.code() + "msg:" + response.message());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTvDeviceId.setText(LaunchActivity.this.getString(R.string.net_error_server));
                            }
                        });
                        return null;
                    }

                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String reason = LaunchActivity.this.getString(R.string.net_error) + ":" + e.getMessage();
                                Toast.makeText(LaunchActivity.this, reason, Toast.LENGTH_LONG).show();
                            }
                        });
                        L.printLog2File(TAG, "First request DeviceId error ：" + e.getMessage());
                        LaunchActivity.this.finish();
                    }

                    @Override
                    public void onResponse(FirstRunEntity loginEntity, int id) {
                        if (Constant.STATUS_SECCUSS_CODE.equals(loginEntity.getStatus())) {

                            mDeviceId = loginEntity.getData().getDeviceid();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mTvDeviceId.setText(visibleDeviceId(mDeviceId));
                                }
                            });
                            saveInfo2Config();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkNetAndPageSkip();
                                }
                            }, 2600);
                            L.printLog2File(TAG, "New device id :" + mDeviceId);
                        } else if (Constant.STATUS_ATTR_ERROR_CODE.equals(loginEntity.getStatus())) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LaunchActivity.this, LaunchActivity.this.getString(
                                            R.string.launch_attr_error), Toast.LENGTH_LONG).show();
                                }
                            });
                            LaunchActivity.this.finish();
                        }
                    }
                });
    }

    /**
     * 登录模块,网络返回数据接收!根据具体业务区执行！
     */
    private void saveInfo2Config() {
        infoManager.setmDeviceId(mDeviceId);
        PreferenceConfig.setBooleanConfig(this, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_RAN, true);
        PreferenceConfig.setConfig(this, SharePreFileConfig.LAUNCH_DATA_FILE_NAME,
                SharePreFileConfig.KEY_IMEI, iMEI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLocationServer != null) {
            mLocationServer.releaseLocate();
            mLocationServer = null;
        }

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private Handler mHandler = new Handler();

    private class Adapter extends BaseAdapter {

        private final ArrayList<String> list;
        private final boolean[] chioses;
        private final Context context;

        Adapter(Context context, ArrayList<String> arrayList, boolean[] choices, TextView view) {
            this.context = context;
            this.list = arrayList;
            this.chioses = choices;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String content = list.get(position);
            boolean choice = chioses[position];
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.lv_item_dialog, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.tv_item_dialog);
                holder.radioBtn = (RadioButton) convertView.findViewById(R.id.rbtn_item_dialog);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(content);
            holder.radioBtn.setChecked(choice);

            holder.radioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.choice = !holder.choice;                                                 //取反
                    choices[position] = holder.choice;                                              //修改数组成员值
                    holder.radioBtn.setChecked(holder.choice);                                      //修改UI
                    if (holder.choice) {                                                            //只有在选择为true时，才去请求相关业务
                        executeActive(position);
                    } else {
                        mBtnGrante.setEnabled(false);
                    }
                }
            });
            return convertView;
        }

        private void executeActive(int position) {
            if (0 == position) {
                initLocations();
            } else if (1 == position) {
                new TakePhotoThread(LaunchActivity.this).run();
            } else if (2 == position) {
                initRoot();
            }
            boolean noItems = false;

            for (boolean b : chioses) {
                if (!b) {
                    noItems = true;
                    break;
                }
            }
            if (!noItems) {
                mBtnGrante.setEnabled(true);
            }
        }
    }

    private static class ViewHolder {
        TextView textView;
        RadioButton radioBtn;
        boolean choice;
    }

    /**
     * 初始化数据线程；
     */
    private class InitDataThread implements Runnable {

        final String CONNECT_SERVICE_LOCAL_NAME = "com.huarui.life.service.ConnectService";

        @Override
        public void run() {

            boolean isRun = serviceIsRunning(CONNECT_SERVICE_LOCAL_NAME);                           //如果ConnectService有在运行直接打开主界面！
            L.e("ConnectService is run :" + isRun);

            if (isRun) {
                mDeviceId = infoManager.getmDeviceId();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvDeviceId.setText(visibleDeviceId(mDeviceId));
                    }
                });

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.this.startActivity(
                                new Intent(LaunchActivity.this, MainActivity.class));
                    }
                }, 3000);
                return;
            }

            LaunchActivity.this.startService(new Intent(LaunchActivity.this, MonitorService.class));//启动监听进程；

            boolean isRan = PreferenceConfig.getBooleanConfig(LaunchActivity.this,
                    LAUNCH_DATA_FILE_NAME, KEY_RAN);                                                //为防止后期迭代出bug参数ran不可变；
            L.e("App is ran :" + isRan);
            String powerValue = infoManager.getmPowerValue();

            if (isRan) {                                                                            //如果启动过直接到获取该设备DeviceID
                mDeviceId = infoManager.getmDeviceId();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvDeviceId.setText(visibleDeviceId(mDeviceId));
                    }
                });

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkNetAndPageSkip();
                    }
                }, 3000);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {                        //如果运行过，5.0以下启动就发广播；否则没有运行过 第一次发广播；
                    BootUtil.modifyPowerOnAndOff(powerValue);
                }
                clearApkFile();
                return;
            }

            boolean isPrompted = PreferenceConfig.getBooleanConfig(LaunchActivity.this,
                    SharePreFileConfig.LAUNCH_DATA_FILE_NAME, SharePreFileConfig.KEY_PROMPT);
            L.e("App first run, is prompted :" + isPrompted);

            if (isPrompted) {
                if (NetworkUtils.isNetworkAvailables(LaunchActivity.this)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            initLocations();
                        }
                    });
                    initFirstRunData();
                } else {
                    for (int i = 0; i <= 5; i++) {
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                T.showToast(LaunchActivity.this, "Net error:", "网络连接异常，6s后再次请求(" + finalI + ")", 1);
                            }
                        });
                        try {
                            Thread.sleep(8000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (NetworkUtils.isNetworkAvailables(LaunchActivity.this)) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    initLocations();
                                }
                            });
                            initFirstRunData();
                            return;
                        }
                        if (finalI == 5) {
                            startActivity(new Intent(LaunchActivity.this, ImageActivity.class).putExtra(Constant.IMAGE_REASON, "Net Error,请检查网络后重新启动应用程序"));
                            LaunchActivity.this.finish();
                            return;
                        }
                    }

                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        initPermissions();
                        //LaunchActivity.this.finish();
                    }
                });
            }
        }

        /**
         * 判断ConnectService是否存在；
         */
        private boolean serviceIsRunning(String className) {

            ActivityManager activityManager =
                    (ActivityManager) LaunchActivity.this.getSystemService(ACTIVITY_SERVICE);

            List<ActivityManager.RunningServiceInfo> serviceList =
                    activityManager.getRunningServices(Integer.MAX_VALUE);

            if (!(serviceList.size() > 0)) {
                return false;
            }
            for (int i = 0; i < serviceList.size(); i++) {
                ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
                ComponentName serviceName = serviceInfo.service;
                if (serviceName.getClassName().equals(className)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 检查是否存在安装包；
         */
        private void clearApkFile() {

            if (LaunchActivity.this.getIntent() != null) {                                          //如果安装成功后启动，删除安装文件夹；
                String filePath = LaunchActivity.this.getIntent().getStringExtra("install");

                if (!TextUtils.isEmpty(filePath)) {
                    File apkFile = new File(filePath);

                    if (apkFile.exists()) {
                        if (apkFile.delete()) {
                            L.printLog2File(TAG, "Delete apk file '" + filePath + " ' finish !");
                        }
                    }
                    L.e(TAG, "clearApkFile ：" + filePath);
                }
            }
        }

    }
}
