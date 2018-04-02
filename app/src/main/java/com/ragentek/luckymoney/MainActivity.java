package com.ragentek.luckymoney;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.LinkedList;

import static com.ragentek.luckymoney.QhbSettingActivity.CanUse;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    private TextView setting;
    private LinearLayout go_setting;
    private ImageView settingBtn;
    public static final String updateUI = "com.ragentek.updateUI";
    //private LinearLayout tips;
    //private SwitchButton qhb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tips=(LinearLayout)findViewById(tips);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window Mywindow = this.getWindow();
            Mywindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Mywindow.setStatusBarColor(getResources().getColor(R.color.qhb_main_red));
        }

        MyApplication application= (MyApplication) getApplication();

        setting=(TextView)findViewById(R.id.setting);
        go_setting=(LinearLayout)findViewById(R.id.go_setting);
        settingBtn=(ImageView)findViewById(R.id.setting_btn);

        SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnCreate:isOpenQhb"+isOpenQhb);
        if(isOpenQhb&&CanUse(this)){
            go_setting.setVisibility(View.GONE);
        }else {
            go_setting.setVisibility(View.VISIBLE);
            Log.i(TAG,"开关未打开");
        }

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,QhbSettingActivity.class);
                startActivityForResult(intent,2);
                //startActivity(intent);
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,QhbSettingActivity.class);
                startActivity(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(updateUI);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"走到onResume");
        SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnResume:isOpenQhb:"+isOpenQhb);
        if(isOpenQhb&&CanUse(this)){
            go_setting.setVisibility(View.GONE);
            //tips.setVisibility(View.VISIBLE);

            //常驻通知栏
            NotificationCompat.Builder builder1 = new NotificationCompat.Builder(MainActivity.this);
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
            //remoteViews.setInt(R.id.notif_root,"setTextColor", isDarkNotificationTheme(MainActivity.this)==true?Color.WHITE:Color.BLACK);
            //如果版本号低于（3.0），那么不显示按钮
            if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.CUPCAKE){
                remoteViews.setViewVisibility(R.id.n_close, View.GONE);
                Log.i(TAG,"onResume:remoteViesButtonGone");
            }else{
                remoteViews.setViewVisibility(R.id.n_close, View.VISIBLE);
                Log.i(TAG,"onResume:remoteViesButtonVisible");
            }

            remoteViews.setOnClickPendingIntent(R.id.n_close,pendingIntent);
            //builder1.setCustomBigContentView(remoteViews);
            builder1.setContent(remoteViews);
            Notification notification1 = builder1.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(R.string.app_name, notification1); // 通过通知管理器发送通知
        }else {
            go_setting.setVisibility(View.VISIBLE);
            Log.i(TAG,"OnResume开关未打开");
            cancelMyNotification(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==2){
            Log.i(TAG,"走到onBackPressed");
            SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
            boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
            Log.i(TAG,"onActivityResult:isOpenQhb:"+isOpenQhb);
            if(CanUse(this)&&isOpenQhb){
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG,"走到onBackPressed");
        SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnBack:isOpenQhb:"+isOpenQhb);
        if(isOpenQhb&&CanUse(this)){
            go_setting.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    // 取消通知
    public static void cancelMyNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }

    //用来更新UI
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"更新UI");
            SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
            boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
            Log.i(TAG,"OnCreate:isOpenQhb"+isOpenQhb);
            if(isOpenQhb&&CanUse(MainActivity.this)){
                if(go_setting!=null)
                    go_setting.setVisibility(View.GONE);
            }else {
                if(go_setting!=null)
                    go_setting.setVisibility(View.VISIBLE);
                Log.i(TAG,"开关未打开");
            }
        }
    };

    public static boolean isDarkNotificationTheme(Context context) {
        return !isSimilarColor(Color.BLACK, getNotificationColor(context));
    }
    /**
     * 获取通知栏颜色
     * @param context
     * @return
     */
    public static int getNotificationColor(Context context) {
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
        Notification notification=builder.build();
        int layoutId=notification.contentView.getLayoutId();
        ViewGroup viewGroup= (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null, false);
        if (viewGroup.findViewById(android.R.id.title)!=null) {
            return ((TextView) viewGroup.findViewById(android.R.id.title)).getCurrentTextColor();
        }
        return findColor(viewGroup);
    }

    private static boolean isSimilarColor(int baseColor, int color) {
        int simpleBaseColor=baseColor|0xff000000;
        int simpleColor=color|0xff000000;
        int baseRed=Color.red(simpleBaseColor)-Color.red(simpleColor);
        int baseGreen=Color.green(simpleBaseColor)-Color.green(simpleColor);
        int baseBlue=Color.blue(simpleBaseColor)-Color.blue(simpleColor);
        double value=Math.sqrt(baseRed*baseRed+baseGreen*baseGreen+baseBlue*baseBlue);
        if (value<180.0) {
            return true;
        }
        return false;
    }
    private static int findColor(ViewGroup viewGroupSource) {
        int color=Color.TRANSPARENT;
        LinkedList<ViewGroup> viewGroups=new LinkedList<>();
        viewGroups.add(viewGroupSource);
        while (viewGroups.size()>0) {
            ViewGroup viewGroup1=viewGroups.getFirst();
            for (int i = 0; i < viewGroup1.getChildCount(); i++) {
                if (viewGroup1.getChildAt(i) instanceof ViewGroup) {
                    viewGroups.add((ViewGroup) viewGroup1.getChildAt(i));
                }
                else if (viewGroup1.getChildAt(i) instanceof TextView) {
                    if (((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor()!=-1) {
                        color=((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor();
                    }
                }
            }
            viewGroups.remove(viewGroup1);
        }
        return color;
    }
}
