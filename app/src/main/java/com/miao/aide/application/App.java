package com.miao.aide.application;

import adrt.ADRTLogCatReader;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.aide.ui.AIDEApplication;
import com.aide.uidesigner.XmlLayoutDesignActivity;
import com.baidu.mobstat.StatService;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.LogUtils.Config;
import com.blankj.utilcode.util.Utils;
import com.miao.app.base.BaseActivity;
import com.miao.util.CrashUtils;
import com.miao.util.CrashUtils.CrashActiviy;
import com.miao.util.CrashUtils.OnCrashListener;
import com.miao.util.Toasty;
import com.tencent.mm.util.ExtDataInput;
import java.io.File;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import org.xutils.x.Ext;
import com.aide.ui.mod.Logger;

public class App extends AIDEApplication {
	private static App sApp;

	private class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
		private final App app;

		public MyActivityLifecycleCallbacks(App app) {
			this.app = app;
		}

		@Override
		public void onActivityCreated(Activity activity, Bundle bundle) {
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
		}

		@Override
		public void onActivityPaused(Activity activity) {
		}

		@Override
		public void onActivityResumed(Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
		}

		@Override
		public void onActivityStarted(Activity activity) {
			if (!(activity instanceof CrashActiviy) && !(activity instanceof XmlLayoutDesignActivity)) {
				BaseActivity.setStatusBar(activity);
			}
		}

		@Override
		public void onActivityStopped(Activity activity) {
		}
	}

	public static App getApp() {
		return sApp;
	}

	private void initMtj() {
		StatService.setAppKey("550e1bf115");
		StatService.setDebugOn(false);
		StatService.autoTrace(this, true, false);
	}

	private void initUtils() {
		Utils.init(this);
		File file = new File(getExternalCacheDir(), "log");
		Config config = LogUtils.getConfig();
		config.setLogSwitch(true);
		config.setLog2FileSwitch(true);
		config.setSaveDays(2);
		config.setDir(file);
		CrashUtils.init(new File(file, "crash"), new CrashUtils.OnCrashListener() {
			@Override
			public void onCrash(String str, Throwable th) {
				try {
					Intent intent = new Intent(App.getApp(), Class.forName("com.miao.util.CrashUtils$CrashActiviy"));
					intent.addFlags(268435456);
					intent.putExtra("crashInfo", str);
					getApp().startActivity(intent);
					AppUtils.exitApp();
				} catch (Throwable e) {
					throw new NoClassDefFoundError(e.getMessage());
				}
			}

		});
	}

	private void initX() {
		Ext.init(this);
		Ext.setDebug(false);
		Ext.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String str, SSLSession sSLSession) {
				return true;
			}
		});
	}

	private boolean useMtj() {
		return FileUtils.isFileExists(new File(com.miao.util.Utils.SDCARD_DATA_DIR, "disable_mtj")) ^ true;
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		if (VERSION.SDK_INT >= 29) {
			PreferenceManager.getDefaultSharedPreferences(this).getBoolean("adjust_actionbar", false);
		}
	}

	protected void init() {
		try {
			com.miao.util.Utils.setApp(this);
			AppConfigured.init();
			initUtils();
			initX();
			registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks(this));
		} catch (Throwable e) {
			Toasty.toast(e.toString()).show();
		}

		try {
			if (useMtj()) {
				initMtj();
				return;
			}
			LogUtils.i(new Object[] { "disable_mtj" });
		} catch (Throwable e) {
			Toasty.toast(e.toString()).show();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		
		Logger.initialize(this);

		ADRTLogCatReader.onContext(this, "com.aide.ui.mod");
		sApp = this;
		init();
	}
}
