package com.ragentek.luckymoney;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/12/22.
 */

public class Config {
    private SharedPreferences preferences;
    private Context mContext;
    private static Config current;

    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_CONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_DISCONNECT";
    /** 红包消息的关键字*/
    public static final String HONGBAO_TEXT_KEY = "[微信红包]";
    /** 不能再使用文字匹配的最小版本号 */
    public static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700
    public static final String BUTTON_CLASS_NAME = "android.widget.Button";

    public static final String WECHAT_OPEN_EN = "Open";
    public static final String WECHAT_OPENED_EN = "You've opened";
    /**QQ红包**/
    public final static String QQ_DEFAULT_CLICK_OPEN = "点击拆开";
    public final static String QQ_HONG_BAO_PASSWORD = "口令红包";
    public final static String QQ_CLICK_TO_PASTE_PASSWORD = "点击输入口令";
    public static final String QQ_HONGBAO_TEXT_KEY = "[QQ红包]";


    private Config(Context context){
        mContext=context;
        preferences=mContext.getSharedPreferences("qhbSp", Context.MODE_PRIVATE);
    }
    public static synchronized Config getConfig(Context context) {
        if(current == null) {
            current = new Config(context.getApplicationContext());
        }
        return current;
    }
    /** 免责声明*/
    public boolean isAgreement() {
        return preferences.getBoolean("agreement", false);
    }

    /** 设置是否同意*/
    public void setAgreement(boolean agreement) {
        preferences.edit().putBoolean("agreement", agreement).apply();
    }

}
