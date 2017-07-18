package com.huarui.life.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by HR_Life on 2017/3/24.
 */

public class FileSynReceiver extends BroadcastReceiver {

    public static final String FILE_SYN_ACTION = "com.huarui.life.FILE_SYN_ACTION";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (FILE_SYN_ACTION.equals(action)) {
            Bundle bundleExtra = intent.getBundleExtra("file");
            modifySharesFile(context,bundleExtra);
        }
    }

    private void modifySharesFile(Context context, Bundle bundle){
        if (bundle !=null){
        boolean menu = bundle.getBoolean("menu");
        //PreferenceConfig.setBooleanConfig(context, Constant.FIRST_RUN_INFO_FILENAME,"menu",menu);
        }
    }
}
