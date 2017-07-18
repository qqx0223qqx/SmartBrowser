package com.huarui.life.callback;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.huarui.life.manager.InfoManager;
import com.huarui.life.service.MonitorService;

import java.util.ArrayList;

/**
 * Created by HR_Life on 2017/7/10 : 11:20.
 * Package : com.huarui.life.callback
 */

public class OnHrTouchListener implements View.OnTouchListener {

    private final Context context;
    private final int getmDeviceId;
    private ArrayList<CharSequence> mEventList;

    /* 开始事件计时上传数据； */
    private boolean mIsCountDownTime = false;
    private final HrCountDownTimer mHrCountDownTimer;


    public OnHrTouchListener(Context context) {
        this.context = context;
        if (mEventList == null) {
            mEventList = new ArrayList<>();
        }
        getmDeviceId = InfoManager.getInstance().getmDeviceId();
        mHrCountDownTimer = new HrCountDownTimer(20, 20);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (!mIsCountDownTime) {
                    mIsCountDownTime = true;
                    mHrCountDownTimer.start();
                }
                break;

            case MotionEvent.ACTION_UP:
                String touchTime = SystemClock.currentThreadTimeMillis() + "";
                mEventList.add(touchTime);
                break;
        }

        return false;
    }

    /**
     * @param list 发送数据给MonitorService ；
     */
    private void startMonitorServiceUploadData(ArrayList<CharSequence> list) {
        Intent analyticIntent = new Intent(context, MonitorService.class);
        analyticIntent.putExtra("deviceId", +getmDeviceId);
        analyticIntent.putCharSequenceArrayListExtra("analytic", list);
        context.startService(analyticIntent);
    }

    private class HrCountDownTimer extends CountDownTimer {
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public HrCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            startMonitorServiceUploadData(mEventList);
            mEventList.clear();
            mIsCountDownTime = false;
        }
    }
}
