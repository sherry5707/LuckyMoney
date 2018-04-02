package com.ragentek.luckymoney;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.tencent.bugly.crashreport.CrashReport;

import static com.ragentek.luckymoney.MainActivity.cancelMyNotification;
import static com.ragentek.luckymoney.MainActivity.updateUI;
import static com.ragentek.luckymoney.QhbSettingActivity.CanUse;
import static com.ragentek.luckymoney.QhbSettingActivity.CanUse2;

/**
 * Created by Administrator on 2016/12/21.
 */

public class NotificationService extends NotificationListenerService {
    private static final String TAG = "QHBNotificationService";
    private static NotificationService service;
    private Context context;
    private String fromWho="有人";
    private MediaPlayer mp;
    private int flag;       //用来标记是qq红包还是微信红包       微信1,QQ2
    private boolean canuse=false;
    private boolean isFinishCanuse=false;     //用来标记handler里的canuse是否判断完毕

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG,"Unbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "4b4c6b28e2", false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
        try {
            context = getApplicationContext();
            mp = MediaPlayer.create(MyApplication.getContext(), R.raw.coins);
        } catch (Exception e) {
            Log.e(TAG,"onCreate走到crash了");
            e.getStackTrace();
        }

        SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
        boolean qhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"onCreate:qhb:"+qhb);
        if(CanUse(this)&&qhb) {
            //常驻通知栏
            NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this);
            builder1.setSmallIcon(R.mipmap.qhb_icon); //设置图标
            builder1.setContentTitle("宝橙红包助手"); //设置标题
            builder1.setContentText("后台自动抢红包中"); //消息内容
            builder1.setTicker("宝橙红包助手帮你后台抢红包");
            builder1.setWhen(System.currentTimeMillis()); //发送时间
            builder1.setDefaults(Notification.DEFAULT_LIGHTS);
            builder1.setOngoing(true);
            //按关闭按钮发送关闭抢红包广播
            Intent intent2=new Intent();
            intent2.setAction("com.ragentek.SendCloseQhbBroadCast");
            PendingIntent pendingIntent=PendingIntent.getBroadcast(this,2,intent2,PendingIntent.FLAG_UPDATE_CURRENT);
            //按整个通知栏进入MainActivity
            Intent intent3=new Intent(this,MainActivity.class);
            PendingIntent pendingIntent3=PendingIntent.getActivity(this,0,intent3,PendingIntent.FLAG_UPDATE_CURRENT);
            builder1.setContentIntent(pendingIntent3);

            RemoteViews remoteViews=new RemoteViews(getPackageName(),R.layout.mynotification);
            //remoteViews.setInt(R.id.notif_root,"setTextColor", isDarkNotificationTheme(getApplicationContext())==true? Color.WHITE:Color.BLACK);
            //如果版本号低于（3.0），那么不显示按钮
            if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.CUPCAKE){
                remoteViews.setViewVisibility(R.id.n_close, View.GONE);
                Log.i(TAG,"onResume:remoteViesButtonGone");
            }else{
                remoteViews.setViewVisibility(R.id.n_close, View.VISIBLE);
                Log.i(TAG,"onResume:remoteViesButtonVisible");
            }

            remoteViews.setOnClickPendingIntent(R.id.n_close,pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.n_content,pendingIntent3);
            //builder1.setCustomBigContentView(remoteViews);
            builder1.setContent(remoteViews);
            Notification notification1 = builder1.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(R.string.app_name, notification1); // 通过通知管理器发送通知
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        cancelMyNotification(this);
        service = null;
    }


    @Override
    public void onListenerConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        Log.i(TAG, "onListenerConnected");
        service = this;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        Log.i(TAG,"StatusBarNotification"+ sbn.getNotification()+",");
        String pkn = String.valueOf(sbn.getPackageName());
        Log.i(TAG,"包名:"+pkn);
        if(pkn==null||!(pkn.equals("com.tencent.mm")||pkn.equals("com.tencent.mobileqq"))){
            Log.i(TAG,"不是qq或微信红包");
            return;
        }
        if(pkn.equals("com.tencent.mm")){
            flag=1;
        }else {
            flag=2;
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onNotificationRemoved");
        }
        SharedPreferences sp = getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean qhb=sp.getBoolean("isOpenQhb", false);
        Log.i(TAG, "isOpenQhb:"+sp.getBoolean("isOpenQhb", false) + "");
        final Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                canuse=CanUse(NotificationService.this);
                Log.e(TAG,"onNotificationPosted--->canuse:"+canuse);
                isFinishCanuse=true;
            }
        });

        if (qhb == false||(!canuse&&isFinishCanuse)||isFinishCanuse&&!CanUse2(this)){
            cancelMyNotification(this);
            return;
        }
        handeNotificationPosted(new IStatusBarNotification() {
            @Override
            public String getPackageName() {

                return sbn.getPackageName();
            }

            @Override
            public Notification getNotification() {
                return sbn.getNotification();
            }
        });
    }

    /**
     * 接收通知栏事件
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void handeNotificationPosted(IStatusBarNotification notificationService) {
        Log.i(TAG,"走到handeNotificationPosted");
        Log.i("handeNotificationPost形参", notificationService + "");
        if (notificationService == null) {
            return;
        }
        String pack = notificationService.getPackageName();
        Notification nf = notificationService.getNotification();
        String text = String.valueOf(notificationService.getNotification().tickerText);
        //notificationIcon=notificationService.getNotification().extras.getParcelable(Notification.EXTRA_LARGE_ICON);
        notificationEvent(text, nf);
    }

    /**
     * 通知栏事件
     */
    private void notificationEvent(String ticker, Notification nf) {
        Log.i(TAG,"走到notificationEvent");
        String text = ticker;
        Log.i("ticker",text);
        int index = text.indexOf(":");
        if (index != -1) {
            if(text.substring(0,index)!=null&&text.substring(0,index)!=""){
                fromWho=text.substring(0,index);
            }
            text = text.substring(index + 1);
        }
        text = text.trim();
        Log.i("通知栏事件关键字", text);
        if(text.contains(Config.HONGBAO_TEXT_KEY)||text.contains(Config.QQ_HONGBAO_TEXT_KEY)) { //红包消息
            Log.i("newHongBaoNotification","进入");
            newHongBaoNotification(nf);
        }
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void newHongBaoNotification(Notification notification) {
        SharedPreferences sp = getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor se = sp.edit();
        se.putBoolean("isReceivingHongbao", true);
        se.commit();
        Log.i("isReceivingHongbao", sp.getBoolean("isReceivingHongbao", false) + "");
        //以下是精华，将微信的通知栏消息打开
        final PendingIntent pendingIntent = notification.contentIntent;
        KeyguardManager km = (KeyguardManager) MyApplication.getContext().getSystemService(Context.KEYGUARD_SERVICE);

        boolean isScreenOn;
        PowerManager pm = (PowerManager) MyApplication.getContext().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        Log.i("KeyguardRestInputM", km.inKeyguardRestrictedInputMode() + "");      //用于判断是否处于锁屏状态
        Log.i("isScreenOn", isScreenOn + "");
        boolean lock = (km.inKeyguardRestrictedInputMode() || !isScreenOn);


        if (!lock) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else {
            /** 显示通知*/
        }

        if (lock) {  /** 播放效果、声音与震动*/
            /** 播放声音*/
            try {
                if(mp==null){
                    mp = MediaPlayer.create(MyApplication.getContext(), R.raw.coins);
                }
                mp.start();
            } catch (Exception e) {
                Log.e("音频播放失败", "");
                Log.e("e", e.getMessage());
                e.printStackTrace();
            }
            /** 振动*/
            Vibrator sVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            sVibrator.vibrate(new long[]{100, 10, 100, 1000}, -1);

            MyApplication application = (MyApplication) this.getApplication();
            application.setAlertDialogText(fromWho,pendingIntent,flag);

        }

    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("SevenNLS", "移除的packegeName是:"+sbn.getPackageName());
        if(sbn.getPackageName().equals("com.ragentek.luckymoney")){
            this.sendBroadcast(new Intent(updateUI));
        }
    }

    public static void toggleNotificationListenerService(Context context) {
        Log.e(TAG,"toggleNLS");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, NotificationService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, NotificationService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}
