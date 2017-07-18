package com.huarui.life.ui.activity.other;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.huarui.life.config.Constant;
import com.huarui.life.manager.InfoManager;
import com.huarui.life.ui.activity.ImageActivity;
import com.huarui.life.utils.T;

import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import log.L;

/**
 * Created by HR_Life on 2017/7/10 : 10:22.
 * Package : com.huarui.life.ui.activity.other
 */

public class HrResourceClient extends XWalkResourceClient {

    private static final java.lang.String TAG = "HrResourceClient";
    private Context context;
    /* 是否启用白名单 */
    private boolean isLaunchWhiteList;
    private final InfoManager mInfoManager;
    /* 白名单文件存储集合 */
    private List<String> mWhiteList;
    /* 是否需要验证 */
    private boolean isVerify = false;
    /* 是否是原生url */
    private boolean isOriginalUrl = true;
    /* 是否正在处理加载不合法的数据； */
    private boolean isHandingLoadError = false;

    private String validKeyword = "huaruilife";

    public HrResourceClient(Context context, XWalkView view) {
        super(view);
        this.context = context;
        mInfoManager = InfoManager.getInstance();
        isLaunchWhiteList = mInfoManager.ismIsLaunchWhiteList();
        if (isLaunchWhiteList) {
            initWhiteListData();
        }
    }

    /**
     * 初始化白名单数据；
     * 启动白名单时调用；
     */
    private void initWhiteListData() {

        String filePath = context.getFilesDir() + File.separator + "verifyFile.cfg";

        List<String> verifyList = new ArrayList<>();
        verifyList.add("huaruilife.com");
        verifyList.add("huaruizsh.com");
        verifyList.add("wap.gdbs.gov.cn");

        File file = new File(filePath);
        if (file.exists()) {
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            String readKey;
            try {
                fileReader = new FileReader(filePath);
                bufferedReader = new BufferedReader(fileReader);
                while ((readKey = bufferedReader.readLine()) != null) {
                    verifyList.add(readKey);
                    L.e(readKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileReader != null) {
                        fileReader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mWhiteList = verifyList;
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {

        if (isLaunchWhiteList && isVerify) {
            boolean isInvalidUrl = true;
            if (url.contains(validKeyword) && (url.startsWith("http://") || url.startsWith("https://"))) {
                isInvalidUrl = false;
            } else {
                for (final String keyword : mWhiteList) {
                    if (url.contains(keyword) && (url.startsWith("http://") || url.startsWith("https://"))) {
                        isInvalidUrl = false;
                        break;
                    }
                }
            }

            if (isInvalidUrl) {                                                                     //如果部分图片或者视频的地址没有直接跳过，不去加载改图片
                if (url.endsWith(".jpg") || url.endsWith(".png")
                        || url.endsWith(".mp4") || url.endsWith(".jpeg") || url.endsWith(".bmp")) {
                    L.e("Include unstated image url:" + url);
                } else {
                    loadOriginalUrlOrBackWard(view, "包含不合法数据链接：" + url);
                    L.e(TAG, "unstated url:" + url);
                    return;
                }
            }
        }
        super.onLoadStarted(view, url);
    }

    /**
     * 当有不合法资源时，加载原生资源货返回返回上一url；
     */
    private void loadOriginalUrlOrBackWard(final XWalkView view, String s) {

        if (!isHandingLoadError) {
            synchronized (HrResourceClient.class) {
                if (!isHandingLoadError) {

                    isHandingLoadError = true;
                    T.showToast(context, "错误提示：", s, 1);

                    if (view != null) {
                        view.setClickable(false);
                        //view.stopLoading();                                                                 //如果有不合法地址，停止全部加载；
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (view == null) {
                                return;
                            }
                            view.stopLoading();
                            if ((isLaunchWhiteList = mInfoManager.ismIsLaunchWhiteList())) {
                                isVerify = false;
                                isOriginalUrl = true;
                            }

                            isHandingLoadError = false;

                            XWalkNavigationHistory navigationHistory = view.getNavigationHistory();
                            if (navigationHistory.canGoBack()) {
                                navigationHistory.navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                                L.e("Can go back,and backward!");
                            } else {
                                view.clearCache(false);
                                view.loadUrl(mInfoManager.getmPublicUrl());
                                L.e("Can't back ,load original url!");
                            }
                            view.setClickable(true);
                        }
                    }, 5 * 1000);
                }
            }
        }
    }

    @Override
    public void onLoadFinished(XWalkView view, String url) {
        super.onLoadFinished(view, url);
        if ("about:blank".equals(url)) {
            if (isOriginalUrl) {
                startImageActivity("节目地址无效");
            } else {
                loadOriginalUrlOrBackWard(view, "节目地址无效,返回上一层级！");
            }
            return;
        }
        if (isOriginalUrl) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOriginalUrl = false;                                                          //默认加载完成1s后修改相关属性；
                    if (isLaunchWhiteList) {
                        isVerify = true;
                        L.e("true'''''''''''''''''''''''''''");
                    }
                }
            }, 2500);
        }
    }

    /**
     * 页面跳转；
     */
    private void startImageActivity(String reason) {
        context.startActivity(new Intent(context, ImageActivity.class)
                .putExtra(Constant.IMAGE_REASON, reason));
    }

    @Override
    public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
        super.onReceivedLoadError(view, errorCode, description, failingUrl);
        view.stopLoading();
        loadOriginalUrlOrBackWard(view, mInfoManager.getmPublicUrl());
        L.printLog2File(TAG, "XwalkView load error: error_code = " + errorCode + " description = " + description + " failingUrl = " + failingUrl);
    }

    @Override
    public void onProgressChanged(XWalkView view, int progressInPercent) {
        super.onProgressChanged(view, progressInPercent);
    }


    /**
     * @param isCheck    是否需要验证；
     * @param isLoadData 是否重新加载验证文件数据；
     */
    public void reset(boolean isCheck, boolean isLoadData) {
        if (isLoadData) {
            initWhiteListData();
        }
        isVerify = isCheck;
    }

    /**
     * 通知数据变更；
     */
    public void notifyUrlDataChanged() {

        if (isLaunchWhiteList = mInfoManager.ismIsLaunchWhiteList()) {
            notifyLaunchWhitelist();
        } else {
            closeWhitelist();
        }
    }

    /**
     * 启用白名单验证；
     */
    public void notifyLaunchWhitelist() {
        initWhiteListData();
        isLaunchWhiteList = true;
        isVerify = true;
    }

    /**
     * 关闭白名单；
     */
    public void closeWhitelist() {
        isVerify = false;
        isLaunchWhiteList = false;
        if (mWhiteList != null) {
            mWhiteList.clear();
        }
    }

    /**
     * 清除数据；
     */
    public void onHrClientDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (mWhiteList != null) {
            mWhiteList.clear();
        }
        context = null;
    }

    private Handler mHandler = new Handler();

}
