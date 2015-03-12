package com.chteuchteu.lesjoiesdusysadmin.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;

import com.chteuchteu.gifapplicationlibrary.GifApplicationSingleton;
import com.chteuchteu.gifapplicationlibrary.hlpr.MainUtil;
import com.chteuchteu.gifapplicationlibrary.ui.Super_Activity_Main;
import com.chteuchteu.lesjoiesdusysadmin.GifFoo;
import com.chteuchteu.lesjoiesdusysadmin.NotificationService;
import com.chteuchteu.lesjoiesdusysadmin.R;
import com.tjeannin.apprate.AppRate;

public class Activity_Main extends Super_Activity_Main {
    private MenuItem menu_notifs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        GifApplicationSingleton.create(this, GifFoo.getApplicationBundle(this));
		super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.vote_title))
                .setIcon(R.drawable.ic_launcher)
                .setMessage(getString(R.string.vote))
                .setPositiveButton(getString(R.string.vote_yes), null)
                .setNegativeButton(getString(R.string.vote_no), null)
                .setNeutralButton(getString(R.string.vote_notnow), null);
        new AppRate(this)
                .setCustomDialog(builder)
                .setMinDaysUntilPrompt(10)
                .setMinLaunchesUntilPrompt(10)
                .init();
	}
	
	private void enableNotifs() {
		MainUtil.Prefs.setPref(this, "notifs", "true");
		
		int minutes = 180;
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(Activity_Main.this, NotificationService.class);
		PendingIntent pi = PendingIntent.getService(Activity_Main.this, 0, i, 0);
		am.cancel(pi);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + minutes*60*1000, minutes*60*1000, pi);
		if (menu_notifs != null)
            menu_notifs.setChecked(true);
		try {
			pi.send();
		} catch (CanceledException e) {	e.printStackTrace(); }
	}
	
	private void disableNotifs() {
		MainUtil.Prefs.setPref(this, "notifs", "false");
		if (menu_notifs != null)
            menu_notifs.setChecked(false);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == com.chteuchteu.gifapplicationlibrary.R.id.menu_list_notifications) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) enableNotifs();
            else disableNotifs();
            return true;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu_notifs = menu.findItem(com.chteuchteu.gifapplicationlibrary.R.id.menu_list_notifications);
        menu_notifs.setChecked(MainUtil.Prefs.getPref(this, "notifs").equals("true"));
        return true;
    }
}
