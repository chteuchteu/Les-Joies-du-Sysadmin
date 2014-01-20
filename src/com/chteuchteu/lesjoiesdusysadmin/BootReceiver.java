package com.chteuchteu.lesjoiesdusysadmin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent) {
		
		// in our case intent will always be BOOT_COMPLETED, so we can just set
		// the alarm
		// Note that a BroadcastReceiver is *NOT* a Context. Thus, we can't use
		// "this" whenever we need to pass a reference to the current context.
		// Thankfully, Android will supply a valid Context as the first parameter
		
		if (Util.getPref(context, "notifs").equals("true")) {
			int min = 180;
			//if (!getPref("notifs_refreshRate", context).equals(""))
			//	min = Integer.parseInt(getPref("notifs_refreshRate", context));
			
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, NotificationService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
			am.cancel(pi);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + min*60*1000,
					min*60*1000, pi);
		}
	}
}