package com.huarui.life.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huarui.life.R;
import com.huarui.life.application.BaseApp;
import com.huarui.life.callback.XWalkViewBitmapAsycCallback;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.BroadcastReceiverManager;
import com.huarui.life.thread.UploadImageThread;
import com.huarui.life.ui.activity.base.BaseActivity;
import com.huarui.life.utils.ScreenUtils;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import log.L;

public class ImageActivity extends BaseActivity {

    private static final String TAG = "ImageActivity";
    private Banner mBanner;
    private List<String> mImagePaths = new ArrayList<>();
    private TextView mTvPrompt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mTvPrompt = (TextView) findViewById(R.id.image_tv_error_prompt);
        initPromptData();
        initData();
        mBanner = (Banner) findViewById(R.id.image_banner);
        mBanner.isAutoPlay(true);
        mBanner.setDelayTime(10 * 1000);
        mBanner.setIndicatorGravity(BannerConfig.RIGHT | BannerConfig.NUM_INDICATOR);
        mBanner.setImages(mImagePaths);
        mBanner.setImageLoader(new ImageLoader() {

            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                String imagePath = String.valueOf(path);

                if (imagePath.length() <= 20) {
                    loadDefaultResource(imageView, imagePath);
                } else {
                    loadSDCardResource(imageView, imagePath);
                }
                L.e(imagePath);
            }
        });
        mBanner.start();
    }

    /**
     * 初始化错误提示；
     */
    private void initPromptData() {
        if (getIntent() != null) {
            String reason = getIntent().getStringExtra(Constant.IMAGE_REASON);
            if (!TextUtils.isEmpty(reason)) {
                mTvPrompt.setVisibility(View.VISIBLE);
                mTvPrompt.setText(reason);
            }
        }
    }

    /**
     * 加载下载的图片资源；
     */
    private void loadSDCardResource(ImageView imageView, String imagePath) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
    }

    /**
     * 加载默认资源图片；th
     */
    private void loadDefaultResource(ImageView imageView, String imagePath) {

        AssetManager manager = getAssets();
        InputStream openAssetStream = null;
        try {
            openAssetStream = manager.open(imagePath);
            imageView.setImageBitmap(BitmapFactory.decodeStream(openAssetStream));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (openAssetStream != null) {
                try {
                    openAssetStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化资源地址；
     */
    private void initData() {
        String imagePath = Constant.getFilePath() + "image/";
        File imageFile = new File(imagePath);
        if (imageFile.exists() && imageFile.isDirectory() && imageFile.canRead() && imageFile.list().length > 0) {
            File[] listFiles = imageFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".png") || name.endsWith(".jpg")
                            || name.endsWith(".bmp") || name.endsWith(".jpeg"));
                }
            });
            Arrays.sort(listFiles);                                                                 //按照字典顺序排序；
            for (File file : listFiles) {
                mImagePaths.add(file.getAbsolutePath());
                L.e(file.getAbsolutePath());
            }
        } else {
            mImagePaths.add("no_res.jpg");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        BroadcastReceiverManager.registerReceiver(mReceiver,
                new IntentFilter(Constant.ACTION_DATA_CHANGE));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String reason = intent.getStringExtra(Constant.IMAGE_REASON);
            if (!TextUtils.isEmpty(reason)) {
                mTvPrompt.setVisibility(View.VISIBLE);
                mTvPrompt.setText(reason);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBanner.isAutoPlay(false);
        BroadcastReceiverManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBanner.releaseBanner();
        mBanner = null;

        mImagePaths.clear();
        mImagePaths = null;
    }

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
                L.printLog2File(TAG, "  Receiver broadcast : screenshot!");
                BaseApp.getThreadService().submit(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = ScreenUtils.snapShotWithoutStatusBar(ImageActivity.this);
                        if (bitmap == null) {
                            bitmap = ScreenUtils.snapShotWithStatusBar(ImageActivity.this);
                        }
                        if (bitmap != null) {
                            XWalkViewBitmapAsycCallback callback = new XWalkViewBitmapAsycCallback();
                            callback.onFinishGetBitmap(bitmap, 0);
                            return;
                        }
                        L.printLog2File(TAG, "   Screen capture fail !");
                    }
                });
            }
        }
    };

    /**
     * 捕获屏幕；
     */
    private void captureScreenAndUpload() {

        String filePath = Constant.getFilePath() + "screen/";
        File file = new File(filePath);
        if (!file.exists() && !file.mkdirs()) {
            L.printLog2File(TAG, "   Create screen path fail !");
            return;
        }
        String fileName = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        filePath += fileName;

        FileOutputStream outputStream = null;
        Bitmap bitmap = null;
        try {
            outputStream = new FileOutputStream(filePath);
            bitmap = ScreenUtils.snapShotWithoutStatusBar(ImageActivity.this);
            boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            if (compress) {
                L.printLog2File(TAG, "   Screen capture success !");
                new UploadImageThread("screen", filePath).run();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
