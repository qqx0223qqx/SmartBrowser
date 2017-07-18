package com.huarui.life.callback;

import android.graphics.Bitmap;

import com.huarui.life.config.Constant;
import com.huarui.life.thread.UploadImageThread;

import org.xwalk.core.XWalkGetBitmapCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import log.L;

/**
 * Created by HR_Life on 2017/5/10 : 10:07.
 * Package : com.huarui.life.callback
 */

public class XWalkViewBitmapAsycCallback extends XWalkGetBitmapCallback {

    private static final String TAG = "XWalkViewBitmapAsycCallback";

    public XWalkViewBitmapAsycCallback() {
    }

    @Override
    public void onFinishGetBitmap(Bitmap bitmap, int i) {
        String filePath = Constant.getFilePath() + "screen" + File.separator;
        File file = new File(filePath);

        if (!file.exists() && !file.mkdir()) {
            L.printLog2File(TAG, "   Create screen path fail !");
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return;
        }
        String fileName = new SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(new Date()) + ".jpg";
        filePath += fileName;

        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            if (compress) {
                L.printLog2File("   Screen capture success ,and ready upload!");
                new UploadImageThread(fileName, filePath).run();
            }
        } catch (Exception e) {
            L.printLog2File(TAG, "  Screen capture exception :" + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
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
