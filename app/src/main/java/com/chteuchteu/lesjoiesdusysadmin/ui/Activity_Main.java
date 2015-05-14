package com.chteuchteu.lesjoiesdusysadmin.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import com.chteuchteu.gifapplicationlibrary.GifApplicationSingleton;
import com.chteuchteu.gifapplicationlibrary.ui.Super_Activity_Main;
import com.chteuchteu.lesjoiesdusysadmin.GifFoo;
import com.chteuchteu.lesjoiesdusysadmin.R;
import com.tjeannin.apprate.AppRate;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Activity_Main extends Super_Activity_Main {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        GifApplicationSingleton.create(this, GifFoo.getApplicationBundle(this));
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());

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
}
