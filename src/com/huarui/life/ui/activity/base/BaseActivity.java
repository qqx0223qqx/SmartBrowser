package com.huarui.life.ui.activity.base;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.huarui.life.application.BaseApp;


/**
 * Created by HR_Life on 2016/11/7.
 * 项目中所创建Activity优先继承BaseActivity；
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private boolean isCompress;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {
        //屏蔽返回键
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {                                                        //这里不生效；
            BaseApp.getInstance().finishAll();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseApp.getInstance().finishAll();                                                      //支持长按返回；
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
