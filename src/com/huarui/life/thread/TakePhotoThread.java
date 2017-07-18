package com.huarui.life.thread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.huarui.life.application.BaseApp;
import com.huarui.life.config.Constant;
import com.huarui.life.manager.InfoManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import log.L;

/**
 * Created by HR_Life on 2017/6/12 : 9:39.
 * Package : com.huaruilife.trnfa.service.thread
 */

public class TakePhotoThread implements Runnable {

    private static final String TAG = "TakephotosThread";
    private Context context;
    private String mFilePath;
    private Camera mFrontCamera;
    private WindowManager mWindowManager;
    private SurfaceView mSurfaceView;

    public TakePhotoThread(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void run() {                                                                             //拍照是在子线程中实现！
        L.printLog2File(TAG, Thread.currentThread().getName());
        initFileData();
        startPreview();
    }

    /**
     * 创建预览；
     */
    private void startPreview() {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mSurfaceView = new SurfaceView(context);
        SurfaceHolder surfaceViewHolder = mSurfaceView.getHolder();
        surfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                L.printLog2File("   SurfaceView created !");
                initCameraAndCapture(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                L.printLog2File("   SurfaceView destroyed !");
                releaseCamera();
            }
        });
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.alpha = 0;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.addView(mSurfaceView, layoutParams);
    }

    /**
     * 打开相机然后拍照；
     */
    private void initCameraAndCapture(SurfaceHolder holder) {

        if (openCamera()) {
            try {
                mFrontCamera.setPreviewDisplay(holder);
                mFrontCamera.startPreview();
                L.printLog2File("  Open camera and we can start preview!");
                Thread.sleep(600);
                takePhoto();
            } catch (Exception e) {
                L.printLog2File(TAG, "  Preview exception:" + e.getMessage());
                releaseSurfaceView();                                                               //不能在finally release !
            }
        } else {
            releaseSurfaceView();
        }
    }

    /**
     * 检查设备摄像机是否有效；
     */
    private boolean openCamera() {
        mFrontCamera = openFacingFrontCamera();
        L.printLog2File("  Camera is opened :" + (mFrontCamera != null));
        return mFrontCamera != null;
    }

    /**
     * 获取前置摄像头
     */
    private Camera openFacingFrontCamera() {
        Camera myCamera = null;
        int cameras = Camera.getNumberOfCameras();
        if (cameras < 1) {
            L.printLog2File("  Device no camera !");
            return null;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();                                     //尝试开启前置摄像头

        for (int camIdx = 0; camIdx < cameras; camIdx++) {

            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    myCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    releaseCamera();
                    L.printLog2File("  Open front camera exception:" + e.getMessage());
                }
            }
        }

        //如果开启前置失败（无前置）则开启后置
        if (myCamera == null) {
            for (int camIdx = 0; camIdx < cameras; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        releaseCamera();
                        L.printLog2File("  Open behind camera exception:" + e.getMessage());
                    }
                }
            }
        }
        return myCamera;
    }

    /**
     * 释放资源
     */
    private void releaseCamera() {

        if (mFrontCamera != null) {
            mFrontCamera.stopPreview();
            mFrontCamera.release();
            mFrontCamera = null;
            L.printLog2File("   release camera !");
        }
        context = null;
        interrupt();
    }

    /**
     * 移除surfaceView;
     */
    private void releaseSurfaceView() {

        if (mWindowManager != null && mSurfaceView != null) {
            mWindowManager.removeViewImmediate(mSurfaceView);
            mWindowManager = null;
            mSurfaceView = null;
            L.printLog2File("   release surface view !");
        }
    }

    /**
     * 开始拍照；
     */
    private void takePhoto() {
        if (mFrontCamera != null) {
            L.printLog2File("   take picture !");
            mFrontCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    matrix.preRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);//创建bitmap资源
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(new File(mFilePath));
                        fos.flush();
                        boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                        if (compress) {
                            if (InfoManager.getInstance().ismRan()) {
                                L.printLog2File("  Normal takePhoto success！");
                                BaseApp.getThreadService().submit(new UploadImageThread("photo", mFilePath));
                            } else {
                                L.printLog2File("   Debug takePhoto success！");
                            }
                        } else {
                            L.printLog2File("   Photo compress false ! ");
                        }
                    } catch (Exception e) {
                        L.printLog2File(TAG, "   Take photo take exception: " + e.getMessage());
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        releaseSurfaceView();
                    }
                }
            });
        } else {
            L.printLog2File("   check camera == null !");
        }
    }

    /**
     * 初始化文件路径及文件名；
     */
    private void initFileData() {
        mFilePath = Constant.getFilePath() + "photo/";
        File file = new File(mFilePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                L.printLog2File("  Create photo file path fail !");
                interrupt();
                return;
            }
        }
        String fileName = new SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(new Date()) + ".jpg";
        mFilePath += fileName;
    }

    /**
     * 终端当前线程；
     */
    private void interrupt() {
        try {
            Thread currentThread = Thread.currentThread();
            if (currentThread.isAlive() && !currentThread.isInterrupted()) {
                currentThread.interrupt();
                L.e("--interrupt()" + currentThread.getName());
            }
        } catch (Exception c) {
            L.e(c.getMessage());
        }
    }
}
