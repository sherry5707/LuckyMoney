package com.ragentek.luckymoney;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class LockPopupActivity extends AppCompatActivity {
    private Button qhbBtn;
    private qhbBtnClickListner listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win=getWindow();
        //锁屏状态下显示，解锁，保持屏幕长亮，打开屏幕
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_lock_popup);
        qhbBtn=(Button)findViewById(R.id.qhb_btn);
        qhbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnqhbBtnClickListner();
            }
        });
    }

    public void setOnqhbBtnClickListner(qhbBtnClickListner listener){
        this.listener=listener;
    }

    /**
     * 再次亮起屏幕：
     * @param intent
     * 如果该Activity并未退出，但是被手动按了锁屏键，当前面的广播接收器再次去启动它的时候，屏幕并不会被唤起，
     * 所以我们需要在activity当中添加唤醒屏幕的代码，这里用的是电源锁。
     */
    @Override
    protected void onNewIntent(Intent intent) {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
    }

    public interface qhbBtnClickListner{
        public void OnqhbBtnClickListner();
    }
}
