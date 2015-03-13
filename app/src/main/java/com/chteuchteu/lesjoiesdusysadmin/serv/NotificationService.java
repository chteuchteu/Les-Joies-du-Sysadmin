package com.chteuchteu.lesjoiesdusysadmin.serv;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.chteuchteu.gifapplicationlibrary.async.NotificationsPollTask;
import com.chteuchteu.lesjoiesdusysadmin.GifFoo;

public class NotificationService extends Service {
	private WakeLock mWakeLock;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void handleIntent() {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.chteuchteu.lesjoiesdusysadmin");
		mWakeLock.acquire();
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getBackgroundDataSetting()) {
			stopSelf();
			return;
		}

        new NotificationsPollTask(this, GifFoo.getApplicationBundle(this)).execute();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent();
		return START_NOT_STICKY;
	}
	
	public void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
	}
}
