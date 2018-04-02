package com.ragentek.luckymoney;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static com.ragentek.luckymoney.MainActivity.cancelMyNotification;
import static com.ragentek.luckymoney.MyApplication.getContext;
import static com.ragentek.luckymoney.MyApplication.mContext;
import static com.ragentek.luckymoney.NotificationService.toggleNotificationListenerService;
import static com.ragentek.luckymoney.SuspensionUtil.applyPermission;

public class QhbSettingActivity extends AppCompatActivity {
    private static String TAG="QhbSettingActivity";
    private Switch openService;
    private Switch openNotification;
    private Switch openOverlay;
    private CheckBox agreement;         //免责声明
    private boolean isAllOpen=false;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private LinearLayout xuanfukuang;       //悬浮框的布局
    private View diliver3;              //悬浮窗下的分割线

    private Switch qhb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qhb_setting);

        wuzhangaiText=(TextView)findViewById(R.id.wuzhangaitext);
        tongzhiText=(TextView)findViewById(R.id.tongzhilantext);
        xuanfuText=(TextView)findViewById(R.id.xuanfukuangtext);
        mianzeText=(TextView)findViewById(R.id.mianzetext);
        diliver1=(View)findViewById(R.id.diliver1);
        diliver2=(View)findViewById(R.id.diliver2);
        diliver4=(View)findViewById(R.id.diliver4);

        setTitle("抢红包");
        qhb=(Switch)findViewById(R.id.qhb);
        openService= (Switch) findViewById(R.id.open_service);
        openNotification= (Switch) findViewById(R.id.open_notification);
        openOverlay = (Switch) findViewById(R.id.open_overlay);
        diliver3=(View)findViewById(R.id.diliver3);
        xuanfukuang=(LinearLayout)findViewById(R.id.xuanfukuang);
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            xuanfukuang.setVisibility(View.GONE);
            diliver3.setVisibility(View.GONE);
        }
        agreement=(CheckBox)findViewById(R.id.agreement);

        SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
        final boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnCreate:isOpenQhb"+isOpenQhb);
        qhb.setChecked(isOpenQhb);
        if(isOpenQhb){
            setAuthority(true);
        }else {
            setAuthority(false);
        }

        qhb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(qhb.isChecked()){        //总开关是关，要打开
                    qhb.setChecked(true);
                    SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putBoolean("isOpenQhb",true);
                    editor.commit();
                    Log.i(TAG,"click关"+sp.getBoolean("isOpenQhb",false)+"");
                    setAuthority(true);
                }else {         //总开关是开，要关闭
                    qhb.setChecked(false);
                    SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor=sp.edit();
                    editor.putBoolean("isOpenQhb",false);
                    editor.commit();
                    Log.i(TAG,"click关"+sp.getBoolean("isOpenQhb",false)+"");
                    setAuthority(false);
                    cancelMyNotification(QhbSettingActivity.this);
                }
            }
        });

        openService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG,"openService.isChecked():"+openService.isChecked());
                //如果switch是关闭状态且没有权限也是关闭
                if (!isAccessibilitySettingsOn(QhbSettingActivity.this)&&openService.isChecked()) {
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]的无障碍服务权限,然后打开权限即可", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    return;
                }
                //如果switch是开启状态，点击即是想要关闭
                if(!openService.isChecked()){//  点击事件发生在switch状态改变后？此时如果想要关,checked已经变成关的状态
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]的无障碍服务权限,然后关闭权限即可", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isNotificationEnabled(QhbSettingActivity.this)&&openNotification.isChecked()) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后开启服务即可", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!openNotification.isChecked()){
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后关闭服务即可", Toast.LENGTH_LONG).show();
                }
            }
        });
        openOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSuspensionWindOpen(QhbSettingActivity.this)&&openOverlay.isChecked()) {
                    if(SuspensionUtil.checkIsMeizuRom()){       //如果是魅族
                        applyPermission(QhbSettingActivity.this);
                        Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后开启服务即可", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,  Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后开启服务即可", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!openOverlay.isChecked()){
                    if(SuspensionUtil.checkIsMeizuRom()){
                        applyPermission(QhbSettingActivity.this);
                        Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后关闭服务即可", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,  Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Toast.makeText(QhbSettingActivity.this, "找到[宝橙红包助手]，然后关闭服务即可", Toast.LENGTH_LONG).show();
                }
            }
        });
        sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isAgree=sp.getBoolean("qhbAgreement",false);
        if(isAgree){
            agreement.setChecked(true);
        }else{
            agreement.setChecked(false);
        }
        agreement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor se=sp.edit();
                    se.putBoolean("qhbAgreement",true);
                    se.commit();
                }else {
                    SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor se=sp.edit();
                    se.putBoolean("qhbAgreement",false);
                    se.commit();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
        boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnCreate:isOpenQhb"+isOpenQhb);
        qhb.setChecked(isOpenQhb);
        if(isOpenQhb){
            setAuthority(true);
        }else {
            setAuthority(false);
        }

        //MyAccessibilityService accessibilityService = MyAccessibilityService.getSharedInstance();
        if(!isAccessibilitySettingsOn(QhbSettingActivity.this)){
            openService.setChecked(false);
        }else {
            openService.setChecked(true);
        }

        if(!isNotificationEnabled(QhbSettingActivity.this)){
            openNotification.setChecked(false);
        }else {
            openNotification.setChecked(true);
        }

        if(!isSuspensionWindOpen(QhbSettingActivity.this)) {
            openOverlay.setChecked(false);
        }else {
            openOverlay.setChecked(true);
        }

        toggleNotificationListenerService(this);
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent();
        //MyAccessibilityService accessibilityService = MyAccessibilityService.getSharedInstance();
        SharedPreferences sp=getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isOpenQhb=sp.getBoolean("isOpenQhb",false);
        Log.i(TAG,"OnBack:isOpenQhb:"+isOpenQhb);
        if(CanUse(this)&&isOpenQhb){
            isAllOpen=true;
            intent.putExtra("isAllOpen", isAllOpen);
            setResult(2,intent);

            finish();
        }else {
            isAllOpen=false;
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("权限没有全部给哦，还不能使用抢红包功能，要离开么?");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //return;
                }
            });
            builder.setPositiveButton("狠心离开", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    intent.putExtra("isAllOpen", isAllOpen);
                    setResult(2,intent);
                    finish();
                }
            });
            AlertDialog dialog=builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
      /*  SharedPreferences sp2=mContext.getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        if(isAccessibilitySettingsOn(this)&&isNotificationEnabled(this)&&isSuspensionWindOpen(this)&&isAgree(this)){
            SharedPreferences.Editor edit = sp2.edit();
            edit.putBoolean("isOpenQhb", true);
            edit.commit();
            Log.i("isOpenQhb",sp2.getBoolean("isOpenQhb",false)+"");
        } else {
            SharedPreferences.Editor edit = sp2.edit();
            edit.putBoolean("isOpenQhb", false);
            edit.commit();
            Log.i("isOpenQhb",sp2.getBoolean("isOpenQhb",false)+"");
        }*/
    }

    public static boolean isNotificationEnabled(Context context) {
        if(context==null){
            context=getContext();
        }
        if(context==null){
            return false;
        }
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = "com.ragentek.luckymoney/com.ragentek.luckymoney.MyAccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

    public static boolean isSuspensionWindOpen(Context context){
        //6.0以上需要请求悬浮窗权限，6.0以下不需要  context必须是activity
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            /*Log.i(TAG,"这里是isSuspensionWindOpen,context是:"+context+"");
            boolean result=false;
            SharedPreferences sp=context.getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
            int b=sp.getInt("susW",3);
            Log.i(TAG,"这里是isSuspensionWindOpen,b:"+b);
            if(b==3){   //表示是第一次进入这个函数(context应该是mainactivity,那么需要写入sp并返回结果)
                result=Settings.canDrawOverlays(context);
                SharedPreferences.Editor editor=sp.edit();
                editor.putInt("susW",result==true?1:0);
                editor.commit();
                return result;
            }else {     //返回之前获得的结果
                if(b==0){
                    return false;
                }
                if(b==1){
                    return true;
                }
            }*/
            Log.i(TAG,"CanUse->context:"+context.getApplicationContext());
            boolean b=false;
            try{
                b=Settings.canDrawOverlays(context.getApplicationContext());
            }catch (Exception e){
                e.printStackTrace();
            }
            return b;
        }
        return true;
    }

    public static boolean isAgree(Context context){
        SharedPreferences sp=context.getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
        boolean isAgree=sp.getBoolean("qhbAgreement",false);
        return isAgree;
    }

    public static boolean CanUse(Context context){
        if(isNotificationEnabled(context)&&isAccessibilitySettingsOn(context)&&isAgree(context)&&isSuspensionWindOpen(context)){
            return true;
        }else {
            return false;
        }
    }

    public static boolean CanUse2(Context context){
        Log.i(TAG,"CanUse2:context"+context);
        if(isNotificationEnabled(context)&&isAccessibilitySettingsOn(context)&&isAgree(context)){
            return true;
        }else {
            return false;
        }
    }

    private TextView wuzhangaiText,tongzhiText,xuanfuText,mianzeText;
    private View diliver1,diliver2,diliver4;
    private void setAuthority(boolean flag){     //设置权限不可设置
        Log.i(TAG,"setAutority:flag："+flag);
        openOverlay.setEnabled(flag);
        openNotification.setEnabled(flag);
        openService.setEnabled(flag);
        agreement.setEnabled(flag);

        if(flag==true){
            wuzhangaiText.setTextColor(this.getResources().getColor(R.color.black));
            tongzhiText.setTextColor(this.getResources().getColor(R.color.black));
            xuanfuText.setTextColor(this.getResources().getColor(R.color.black));
            mianzeText.setTextColor(this.getResources().getColor(R.color.black));
            diliver1.setBackgroundColor(this.getResources().getColor(R.color.black));
            diliver2.setBackgroundColor(this.getResources().getColor(R.color.black));
            diliver3.setBackgroundColor(this.getResources().getColor(R.color.black));
            diliver4.setBackgroundColor(this.getResources().getColor(R.color.black));
        }else {
            wuzhangaiText.setTextColor(this.getResources().getColor(R.color.gray));
            tongzhiText.setTextColor(this.getResources().getColor(R.color.gray));
            xuanfuText.setTextColor(this.getResources().getColor(R.color.gray));
            mianzeText.setTextColor(this.getResources().getColor(R.color.gray));
            diliver1.setBackgroundColor(this.getResources().getColor(R.color.gray));
            diliver2.setBackgroundColor(this.getResources().getColor(R.color.gray));
            diliver3.setBackgroundColor(this.getResources().getColor(R.color.gray));
            diliver4.setBackgroundColor(this.getResources().getColor(R.color.gray));
        }
    }
}

