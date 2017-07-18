package com.huarui.life.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ProgressBar;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.huarui.life.R;
import com.huarui.life.application.BaseApp;
import com.huarui.life.callback.OnHrTouchListener;
import com.huarui.life.callback.XWalkViewBitmapAsycCallback;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.BroadcastReceiverManager;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.service.HrNanoHTTPD;
import com.huarui.life.ui.activity.other.HrResourceClient;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import log.L;


public class MainActivity extends XWalkActivity {

    private static final String TAG = "MainActivity";
    private static int mDeviceId = -1;
    private XWalkView mXwalkView;
    /* 数据管理实例；*/
    private InfoManager infoManager;
    /* 进度条过度；*/
    private ProgressBar mProgressBar;
    /*保存用户操作时间戳；*/
    private ArrayList<CharSequence> mTimeList = new ArrayList<CharSequence>();
    /* android后台视频流支持服务； */
    private HrNanoHTTPD mNanoHttpD;
    /* 页面缓存视频服务； */
    private HttpProxyCacheServer mHttpProxy = null;
    /* 发布的节目url */
    private String mPublicUrl;
    /* 页面资源加载器 */
    private HrResourceClient mResourceClient;

    private boolean isXWalkReady = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoManager = InfoManager.getInstance();
        mDeviceId = infoManager.getmDeviceId();
        mPublicUrl = infoManager.getmPublicUrl();

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        if (mNanoHttpD == null) {
            mNanoHttpD = new HrNanoHTTPD();
        }
        try {
            if (!mNanoHttpD.isAlive()) {
                mNanoHttpD.start(1, true);
                L.printLog2File(TAG, "NanoHttp service start !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mXwalkView = (XWalkView) findViewById(R.id.xwalkview);
        mResourceClient = new HrResourceClient(this, mXwalkView);
        L.printLog2File(TAG, "url:" + mPublicUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onXWalkReady() {
        //mXwalkView.setXWalkViewInternalVisibility(View.INVISIBLE);
        isXWalkReady = true;

        XWalkSettings settings = mXwalkView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);

        mXwalkView.setLongClickable(false);
        mXwalkView.setResourceClient(mResourceClient);
        mXwalkView.loadUrl(mPublicUrl);
        mXwalkView.addJavascriptInterface(new JsToAndroid(), "js2android");
        mXwalkView.setOnTouchListener(new OnHrTouchListener(this));
        mXwalkView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onXWalkFailed() {
        super.onXWalkFailed();
        this.startActivity(new Intent(this, ImageActivity.class).
                putExtra(Constant.IMAGE_REASON, "XwalkView load fail;"));
        this.finish();
    }

    @Override
    protected void onStart() {
        BroadcastReceiverManager.registerReceiver(mReceiver, new IntentFilter(Constant.ACTION_DATA_CHANGE));                          //注册监听；
        L.e("registerReceiver(mReceiver)");
        mXwalkView.setEnabled(true);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXwalkView != null && mXwalkView.isEnabled() && isXWalkReady) {
            mXwalkView.resumeTimers();
            L.e("mXwalkView.resumeTimers()");
        }
        BaseApp.finishOtherAll(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (mXwalkView != null && mXwalkView.isEnabled()) {
            mXwalkView.loadUrl(infoManager.getmPublicUrl());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean isLoad = intent.getBooleanExtra("load", false);                                     // false 允许读取已有缓存数据 true 绕过缓存重新加载；
        if (isLoad) {
            String currentUrl = intent.getStringExtra("currentUrl");
            if (!TextUtils.isEmpty(currentUrl) && mXwalkView.isEnabled()) {
                mXwalkView.stopLoading();
                mResourceClient.notifyUrlDataChanged();
                mXwalkView.loadUrl(currentUrl);
                L.e(TAG, "New cmd : load url =>" + currentUrl);
            }
        } else {
            mXwalkView.reload(XWalkView.RELOAD_IGNORE_CACHE);                                       //刷新数据绕过缓存完全网络加载；
            L.e(TAG, "New cmd : reload url");
        }
        mXwalkView.clearCache(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXwalkView != null && mXwalkView.isEnabled() && isXWalkReady) {
            mXwalkView.pauseTimers();
            L.e("mXwalkView.pauseTimers()");
        }
    }

    @Override
    protected void onStop() {
        BroadcastReceiverManager.unregisterReceiver(mReceiver);                                                              //反注册监听；
        L.e("unregisterReceiver(mReceiver)");
        if (mXwalkView != null && mXwalkView.isEnabled() && isXWalkReady) {
            mXwalkView.stopLoading();
            mXwalkView.setEnabled(false);
            L.e("mXwalkView.stopLoading();");
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mXwalkView != null && mXwalkView.isEnabled()) {
            mXwalkView.clearCache(false);
            mXwalkView.onDestroy();
        }
        if (mResourceClient != null) {
            mResourceClient.onHrClientDestroy();
        }

        try {
            if (mHttpProxy != null) {
                mHttpProxy.unregisterCacheListener(mCacheListener);
                mHttpProxy.shutdown();
                L.printLog2File(TAG, "Http proxy service is shutdown !");
            }

            if (mNanoHttpD != null && mNanoHttpD.isAlive()) {
                mNanoHttpD.stop();
                L.printLog2File(TAG, "NanoHttp service stop !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mReceiver = null;

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        L.printLog2File(TAG, " ==> onDestroy()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mXwalkView != null && mXwalkView.isEnabled()) {
            mXwalkView.clearCache(false);
        }
    }

    @Override
    public void onBackPressed() {
        //屏蔽单击右键返回功能，使用长按右键执行Home键功能；
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseApp.getInstance().finishAll();
        }
        L.printLog2File(TAG, "WebView page => onKeyLongPress() -- go home !");
        return super.onKeyLongPress(keyCode, event);
    }

    private Handler mHandler = new Handler();

    /**
     * js调用android
     */
    private class JsToAndroid {

        private HttpProxyCacheServer newProxy() {
            return new HttpProxyCacheServer.Builder(MainActivity.this).
                    maxCacheSize(1024 * 1024 * 1024).maxCacheFilesCount(12).build();
        }

        @JavascriptInterface
        public String cache(String videoUrl) {

            if (mHttpProxy == null) {
                mHttpProxy = newProxy();
                mHttpProxy.registerCacheListener(mCacheListener, videoUrl);
            }

            String cacheUrl = mHttpProxy.getProxyUrl(videoUrl);
            if (mHttpProxy.isCached(videoUrl)) {
                cacheUrl = "http://127.0.0.1:5556/v?path=" + cacheUrl;
            }
            L.printLog2File("===Js2android===", cacheUrl);
            return cacheUrl;
        }
    }

    /**
     * 缓存文件回调监听；
     */
    private CacheListener mCacheListener = new CacheListener() {
        @Override
        public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
            if (percentsAvailable == 0) {
                L.printLog2File(TAG, "Web video url :" + url + " == cache file :" + (cacheFile.exists() ? cacheFile.getAbsolutePath() : "no file !"));
            } else if (cacheFile.exists() && ((percentsAvailable % 20) == 0)) {
                L.printLog2File(TAG, " PercentsAvailable:" + percentsAvailable + "%");
            }
        }
    };


    /**
     * 数据变化监听器；
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) {
                return;
            }

            if (intent.getBooleanExtra(Constant.DATA_CHANGE_SCREENSHOT, false)) {
                L.printLog2File(TAG, "   RReceiver broadcast : screenshot!!");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mXwalkView.captureBitmapAsync(new XWalkViewBitmapAsycCallback());
                    }
                });
            } else if (intent.getBooleanExtra(Constant.DATA_CHANGE_ONLY_WHITELIST, false)) {

                boolean launchWhitelist = intent.getBooleanExtra(Constant.DATA_CHANGE_WHITELIST, false);
                if (mXwalkView.isEnabled() && mResourceClient != null) {
                    if (!launchWhitelist) {
                        mResourceClient.closeWhitelist();
                        infoManager.setmIsLaunchWhiteList(false);
                        L.e("Close whitelist verify!");
                    } else {
                        if (!infoManager.ismIsLaunchWhiteList()) {
                            infoManager.setmIsLaunchWhiteList(true);
                            mResourceClient.notifyLaunchWhitelist();
                            L.e("Launch whitelist verify!");
                        }
                    }
                } else {
                    infoManager.setmIsLaunchWhiteList(launchWhitelist);
                }
            }
        }
    };
}
