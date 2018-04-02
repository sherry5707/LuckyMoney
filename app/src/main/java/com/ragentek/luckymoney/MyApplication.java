package com.ragentek.luckymoney;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyApplication extends Application {
	private static final String TAG = "MyApplication";
	public static Context mContext = null;
	public AlertDialog mDialog;
	private Handler maskHandler = null;

	public void setAlertDialogText(final String fromWho, final PendingIntent pendingIntent, final int flag){
		try {
			if (maskHandler == null) {
				HandlerThread handlerThread = new HandlerThread("Mask");
				handlerThread.start();
				maskHandler = new Handler(handlerThread.getLooper());
			}
			maskHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mDialog!=null){
						mDialog.dismiss();
						Log.e(TAG,"新dialog来了，之前的消失并置空");
						mDialog=null;
					}
					//String txt=fromWho;
					LayoutInflater inflater = LayoutInflater.from(MyApplication.getContext());
					LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.lock_popup_dialog, null);
					mDialog = new AlertDialog.Builder(mContext).create();
					mDialog.setCancelable(true);
					mDialog.setCanceledOnTouchOutside(true);
					mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告，关键
					mDialog.show();
					mDialog.getWindow().setContentView(layout);

					//按钮
					LinearLayout qhb = (LinearLayout) mDialog.findViewById(R.id.qhb);
					LinearLayout bql = (LinearLayout) mDialog.findViewById(R.id.bql);
					TextView from = (TextView) mDialog.findViewById(R.id.fromwho);
					from.setText(fromWho);
					qhb.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								pendingIntent.send();
								mDialog.dismiss();
							} catch (PendingIntent.CanceledException e) {
								//Log.e("e", e.getMessage());
								e.printStackTrace();
							}
						}
					});
					bql.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
						}
					});

					//微信/QQ icon
					ImageView icon=(ImageView)mDialog.findViewById(R.id.dialog_icon);
					if(flag==2){
						icon.setImageResource(R.drawable.lock_popup_dialog_qq);
					}
				}
			});
		} catch (Exception e) {
			Log.e("e", e.getMessage()+"没有权限");
			e.getStackTrace();
		}
	}

	public static Context getContext() {
		return MyApplication.mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("MyApplication", "onCreate");

		MyApplication.mContext = getApplicationContext();
	}

}
