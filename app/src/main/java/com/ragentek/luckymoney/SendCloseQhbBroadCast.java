package com.ragentek.luckymoney;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static com.ragentek.luckymoney.MainActivity.cancelMyNotification;

/**
 * Created by Administrator on 2017/1/20.
 */

public class SendCloseQhbBroadCast extends BroadcastReceiver {
    private static final String TAG="SendCloseQhbBroadCast";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"得到关闭抢红包的广播,context:"+context);
        Context ma=context.getApplicationContext();
        Log.i(TAG,"getAplicationcontext"+ma);
        SharedPreferences sp=context.getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean("isOpenQhb",false);
        editor.commit();
        cancelMyNotification(context);
    }
}
