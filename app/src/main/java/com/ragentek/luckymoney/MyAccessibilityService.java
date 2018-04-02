package com.ragentek.luckymoney;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ragentek.luckymoney.Config.BUTTON_CLASS_NAME;
import static com.ragentek.luckymoney.Config.QQ_CLICK_TO_PASTE_PASSWORD;
import static com.ragentek.luckymoney.Config.QQ_HONG_BAO_PASSWORD;
import static com.ragentek.luckymoney.MainActivity.cancelMyNotification;
import static com.ragentek.luckymoney.QhbSettingActivity.CanUse;

public class MyAccessibilityService extends AccessibilityService {
	private static final String TAG="MyAccessibilityService";
	private static MyAccessibilityService sSharedInstance = null;
	public static final int WX_AFTER_GET_GOHOME = 0; //返回桌面
	private static final int WINDOW_NONE = 0;
	private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
	private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
	private static final int WINDOW_LAUNCHER = 3;
	private static final int WINDOW_OTHER = -1;
	private int mCurrentWindow=WINDOW_NONE;
	private boolean isReceivingHongbao;
	private boolean isOpenQhb=false;			//标记是否打开了抢红包功能
	/**下面是qq红包**/
	private AccessibilityNodeInfo rootNodeInfo;
	private List<AccessibilityNodeInfo> mReceiveNode;
	private String lastFetchedHongbaoId = null;

	@Override
	protected void onServiceConnected() {
	    sSharedInstance = this;
	}


	@Override
	public boolean onUnbind(Intent intent) {
	    sSharedInstance = null;
		Log.e(TAG,"unbind");
	    return true;
	}
	
	public static MyAccessibilityService getSharedInstance() {
		if(sSharedInstance!=null)
	    	return sSharedInstance;
		else {
			Log.e("sSharedInstance",null+"");
			return null;
		}
	}
	
	private class AppInfo {
		String id;
		String label;
		Drawable icon;
	}
	
	private enum State {
		IDLE,
		POWER,
		KILL,
		CLEAR,
		DELETE,
	}
	private enum Step {
		INIT,
		POPUP,
		FINISH,
		CLOSE,
	}
	private State state = State.IDLE;
    private Step step = Step.INIT;
	private Context context = null;
	private boolean powerSaveMode = false;
    Iterator<AppInfo> it = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG,"onCreate");
		SharedPreferences sp=getSharedPreferences("qhbSp",Context.MODE_MULTI_PROCESS);
		boolean qhb=sp.getBoolean("isOpenQhb",false);
		Log.i(TAG,"onCreate:qhb:"+qhb);
		MyApplication application= (MyApplication) getApplication();
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
			//如果版本号低于（3。0），那么不显示按钮
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
			remoteViews.setOnClickPendingIntent(R.id.n_content,pendingIntent3);
			Notification notification1 = builder1.build();
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(R.string.app_name, notification1); // 通过通知管理器发送通知
		}
	}

	@Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
		this.rootNodeInfo=event.getSource();
		if(rootNodeInfo==null){
			return;
		}

		String pkn = String.valueOf(event.getPackageName());
		Log.i("nodeInfo", pkn + "");
		if (pkn != null &&(pkn.equals("com.tencent.mm")||pkn.equals("com.tencent.mobileqq"))) {		//判断是否是微信发的event或qq发的event
			SharedPreferences sp= MyApplication.getContext().getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
			boolean qhb=sp.getBoolean("isOpenQhb",false);
			Log.i("isOpenQhb",sp.getBoolean("isOpenQhb",false)+"");
			MyApplication application= (MyApplication) getApplication();
			if(qhb==false||!CanUse(this)){		//如果没有打开抢红包开关或者权限不足,就关掉通知栏
				return;
			}
			final int eventType = event.getEventType();
			Log.i("eventType",eventType+"");
			if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
				Log.i(TAG,"onAccessibilityEvent->打开红包:opneHongbao");
				openHongBao(event);			//打开红包
			} else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
				if(mCurrentWindow != WINDOW_LAUNCHER) { //不在聊天界面或聊天列表，不处理
					return;
				}
				isReceivingHongbao=sp.getBoolean("isReceivingHongbao",false);
				Log.i("isReceivingHongbao",isReceivingHongbao+"");
				if(isReceivingHongbao) {
					Log.i(TAG,"onAccessibilityEvent->处理红包:handleChatListHongBao");
					handleChatListHongBao();			//抢到红包后的处理
				}
			}
		}
    }

	private boolean triggerWorking = false;
	private void triggerWindowSwitch() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					Log.i(TAG,"triggerWindowSwitchSleep");
				} catch (Exception e) {
					e.printStackTrace();
				}
				Context context = MyAccessibilityService.this.getBaseContext();
				Intent intent = new Intent(context, MySingleBlankActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				triggerWorking = true;
				Log.i(TAG, "event ：走到了triggerWindowSwitchStartActivity");
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelMyNotification(this);
		Log.e(TAG,"onDestroy");
	}

	/**
	 * 打开红包
	 * **/
	private void openHongBao(AccessibilityEvent event) {
		Log.i(TAG,"eventClassName"+event.getClassName()+"");
		if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())
				|| (triggerWorking && "com.tencent.mm.ui.LauncherUI".equals(event.getClassName()))) {
			if (triggerWorking) {
				triggerWorking = false;
			}
			mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
			//点中了红包，下一步就是去拆红包
			AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
			Log.i(TAG,"openHongBao:点中了红包下一步是去拆红包->nodeInfo:"+nodeInfo);
			if(nodeInfo == null) {
				Log.i(TAG,"走到了 MyAccessibilityService :rootWindow为空");
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
					triggerWindowSwitch();
					return;
				}
				return;
			}
			AccessibilityNodeInfo targetNode = null;

			//获得微信版本
			PackageInfo mWechatPackageInfo=null;
			try {
				mWechatPackageInfo = MyApplication.getContext().getPackageManager().getPackageInfo("com.tencent.mm", 0);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			Log.i("mWechatPackageInfo",mWechatPackageInfo+"");
			int wechatVersion ;
			if(mWechatPackageInfo==null){
				wechatVersion=0;
			}else {
				wechatVersion=mWechatPackageInfo.versionCode;
			}
			Log.i("wechatVersion",wechatVersion+"");
			if (wechatVersion < Config.USE_ID_MIN_VERSION) {
				Log.i("根据文字拆红包","");
				targetNode =findNodeInfosByText(nodeInfo, "拆红包");
			} else {
				String buttonId = "com.tencent.mm:id/b43";

				if(wechatVersion == 700) {
					buttonId = "com.tencent.mm:id/b2c";
				}

				if(buttonId != null) {
					targetNode = findNodeInfosById(nodeInfo, buttonId);
				}

				if(targetNode == null) {
					//分别对应固定金额的红包 拼手气红包  和三种QQ红包
					AccessibilityNodeInfo textNode = findNodeInfosByTexts(nodeInfo, "发了一个红包", "给你发了一个红包", "发了一个红包，金额随机");

					if(textNode != null) {
						for (int i = 0; i < textNode.getChildCount(); i++) {
							AccessibilityNodeInfo node = textNode.getChild(i);
							Log.i(TAG,node.getClassName()+"");
							if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
								targetNode = node;
								break;
							}
						}
					}
				}

				if(targetNode == null) { //通过组件查找
					targetNode = findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
				}

				if(targetNode != null) {
					final AccessibilityNodeInfo n = targetNode;
					performClick(n);
					triggerWorking = false;
					Log.i("event", "performClick");
				} else {
					if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
						triggerWindowSwitch();
					}
				}
				//事件统计
				eventStatistics(MyApplication.getContext(), "open_hongbao");
			}

		} else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
			mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
			//拆完红包后看详细的纪录界面
			//默认是静静地看，所以不需要回到主界面
		} else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
			mCurrentWindow = WINDOW_LAUNCHER;
			//在聊天界面,去点中红包
			handleChatListHongBao();
		} else {
			mCurrentWindow = WINDOW_OTHER;
		}

		/**qq红包**/
		if("com.tencent.mobileqq.activity.SplashActivity".equals(event.getClassName())){
			mCurrentWindow=WINDOW_LAUNCHER;
			//在聊天界面,去点中红包
			handleChatListHongBao();
		}else if("cooperation.qwallet.plugin.QWalletPluginProxyActivity".equals(event.getClassName())){
			mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
			//拆完红包后看详细的纪录界面
			//默认是静静地看，所以不需要回到主界面
		}
	}

	/**
	 * 收到聊天里的红包
	 */
	private void handleChatListHongBao() {
		Log.i(TAG,"走到了handleChatListHongBao");
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			Log.i("MyAccessibilityService", "rootWindow为空");
			return;
		}

		//List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
		List<AccessibilityNodeInfo> list = findAccessibilityNodeInfosByTexts(nodeInfo,new String[]{"领取红包",Config.QQ_DEFAULT_CLICK_OPEN, Config.QQ_HONG_BAO_PASSWORD, Config.QQ_CLICK_TO_PASTE_PASSWORD, "发送"});
		if (list != null && list.isEmpty()) {
			// 从消息列表查找红包
			AccessibilityNodeInfo node =findNodeInfosByText(nodeInfo, "[微信红包]");
			if (node != null) {
				isReceivingHongbao = true;
				SharedPreferences sp= MyApplication.getContext().getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor se=sp.edit();
				se.putBoolean("isReceivingHongbao",isReceivingHongbao);
				se.commit();
				Log.i("isReceivingHongbao",sp.getBoolean("isReceivingHongbao",false)+"");
				performClick(nodeInfo);		//模拟点击
			}
		} else if (list != null) {
			if (isReceivingHongbao) {
				//最新的红包领起
				AccessibilityNodeInfo node = list.get(list.size() - 1);
				if(node.getText().toString()!=null&&node.getText().toString().equals("口令红包已拆开")) {
					Log.i(TAG,"handleChatListHongBao->口令红包已拆开");
					return;
				}
				performClick(node);

				/**qq红包处理**/
				if(node.getText().toString()!=null&&node.getText().toString().equals(QQ_HONG_BAO_PASSWORD)){
					AccessibilityNodeInfo rowNode = getRootInActiveWindow();
					if (rowNode == null) {
						Log.e(TAG, "noteInfo is　null");
						return;
					} else {
						recycle(rowNode);
					}
				}
				isReceivingHongbao = false;
				SharedPreferences sp= MyApplication.getContext().getSharedPreferences("qhbSp", Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor se=sp.edit();
				se.putBoolean("isReceivingHongbao",isReceivingHongbao);
				se.commit();
				Log.i("isReceivingHongbao",sp.getBoolean("isReceivingHongbao",false)+"");
			}
		}
	}
    
    @Override
    public void onInterrupt() {
		Log.d("MyAccessibilityService", "qianghongbao service interrupt");
		Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }
    
    private Handler maskHandler = null;
    private View maskView = null;
    private TextView titleView = null;
    private ImageView iconView = null;
    private TextView labelView = null;
    private TextView infoView = null;
    
    private final static int WATCH_DOG_INTERVAL = 2000;


	/**
	 * 处理通知栏信息
	 *
	 * 如果是微信红包的提示信息,则模拟点击
	 *
	 * @param event
	 */
	private void handleNotification(AccessibilityEvent event) {
		List<CharSequence> texts = event.getText();
		if (!texts.isEmpty()) {
			for (CharSequence text : texts) {
				String content = text.toString();
				//如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
				if (content.contains("[微信红包]")) {
					if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
						Notification notification = (Notification) event.getParcelableData();
						PendingIntent pendingIntent = notification.contentIntent;
						try {
							pendingIntent.send();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * 关闭红包详情界面,实现自动返回聊天窗口
	 */
	/*@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void close() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			//为了演示,直接查看了关闭按钮的id
			List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId("@id/ez");
			nodeInfo.recycle();
			for (AccessibilityNodeInfo item : infos) {
				item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}
	}*/

	/**
	 * 模拟点击,拆开红包
	 */
	/*@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void openPacket() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			//为了演示,直接查看了红包控件的id
			List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("@id/b9m");
			nodeInfo.recycle();
			for (AccessibilityNodeInfo item : list) {
				item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}
	}*/

	/**
	 * 模拟点击,打开抢红包界面
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	/*private void getPacket() {
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		AccessibilityNodeInfo node = recycle(rootNode);

		node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		AccessibilityNodeInfo parent = node.getParent();
		while (parent != null) {
			if (parent.isClickable()) {
				parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
			parent = parent.getParent();
		}

	}*/

	/**
	 * 递归查找当前聊天窗口中的红包信息
	 *
	 * 聊天窗口中的红包都存在"领取红包"一词,因此可根据该词查找红包
	 *
	 * @param node
	 */
	public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {
		if (node.getChildCount() == 0) {
			if (node.getText() != null) {
				if ("领取红包".equals(node.getText().toString())) {
					return node;
				}
				/*这个if代码的作用是：匹配“点击输入口令的节点，并点击这个节点”*/
				if(node.getText()!=null&&node.getText().toString().equals(QQ_CLICK_TO_PASTE_PASSWORD)) {
					node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			 /*这个if代码的作用是：匹配文本编辑框后面的发送按钮，并点击发送口令*/
				if (node.getClassName().toString().equals("android.widget.Button") && node.getText().toString().equals("发送")) {
					node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			}
		} else {
			for (int i = 0; i < node.getChildCount(); i++) {
				if (node.getChild(i) != null) {
					recycle(node.getChild(i));
				}
			}
		}
		return node;
	}
	/** 通过文本查找*/
	public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	/** 通过id查找*/
	public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
			if(list != null && !list.isEmpty()) {
				return list.get(0);
			}
		}
		return null;
	}
	/** 通过关键字查找*/
	public static AccessibilityNodeInfo findNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
		for(String key : texts) {
			AccessibilityNodeInfo info = findNodeInfosByText(nodeInfo, key);
			if(info != null) {
				return info;
			}
		}
		return null;
	}

	/** 通过组件名字查找*/
	public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
		if(TextUtils.isEmpty(className)) {
			return null;
		}
		for (int i = 0; i < nodeInfo.getChildCount(); i++) {
			AccessibilityNodeInfo node = nodeInfo.getChild(i);
			if(className.equals(node.getClassName())) {
				return node;
			}
		}
		return null;
	}
	/** 点击事件*/
	public static void performClick(AccessibilityNodeInfo nodeInfo) {
		Log.i("模拟点击","");
		if(nodeInfo == null) {
			return;
		}
		if(nodeInfo.isClickable()) {
			nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		} else {
			performClick(nodeInfo.getParent());
		}
	}
	/** 事件统计*/
	public static void eventStatistics(Context context, String event) {

	}

	private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {
		Log.i(TAG,"走到findAccessibilityNodeInfosByTexts");
		for (String text : texts) {
			if (text == null) continue;
			Log.i(TAG,"查找chatlist中的红包里包含"+text);
			List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
			if (!nodes.isEmpty()) {
				if (text.equals(Config.WECHAT_OPEN_EN) && !nodeInfo.findAccessibilityNodeInfosByText(Config.WECHAT_OPENED_EN).isEmpty()) {
					Log.i(TAG,"findAccessibilityNodeInfosByTexts已拆开");
					continue;
				}
				Log.i(TAG,"找到了"+text+"对应的node"+nodes.size()+"个");
				return nodes;
			}
		}
		return new ArrayList<>();
	}

}