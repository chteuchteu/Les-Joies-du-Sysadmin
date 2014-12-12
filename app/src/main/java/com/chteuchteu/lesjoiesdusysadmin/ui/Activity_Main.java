package com.chteuchteu.lesjoiesdusysadmin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdusysadmin.NotificationService;
import com.chteuchteu.lesjoiesdusysadmin.R;
import com.chteuchteu.lesjoiesdusysadmin.at.FeedParser;
import com.chteuchteu.lesjoiesdusysadmin.hlpr.Util;
import com.chteuchteu.lesjoiesdusysadmin.obj.Gif;
import com.google.analytics.tracking.android.EasyTracker;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Activity_Main extends ActionBarActivity {
	public static final String tumblrUrl = "lesjoiesdusysadmin.tumblr.com";
	private ArrayList<HashMap<String, String>> list;
	private static Activity activity;
	public static List<Gif> 	gifs;
	public boolean		loaded;
	private MenuItem	    notifs;
	private boolean	    notifsEnabled;
	public static int 	scrollY;
	
	private ListView 	lv_gifs;

	@SuppressLint({ "InlinedApi", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		activity = this;
		list = new ArrayList<>();
		lv_gifs = (ListView) findViewById(R.id.gifs_list);

		toolbar.setTitle(" Les Joies du Sysadmin");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

				SystemBarTintManager tintManager = new SystemBarTintManager(activity);
				tintManager.setStatusBarTintEnabled(true);
				tintManager.setStatusBarTintResource(R.color.statusBarColor);
			}
		}


		if (gifs == null) {
			gifs = new ArrayList<>();
			loaded = false;
			
			Util.createLJDSYDirectory();
			getGifs();
			if (Util.removeUncompleteGifs(activity, gifs))
				getGifs();
			Util.removeOldGifs(gifs);
		}
		
		if (Util.getPref(this, "first_disclaimer").equals("")) {
			Util.setPref(this, "first_disclaimer", "true");
			final LinearLayout l = (LinearLayout) findViewById(R.id.first_disclaimer);
			l.setVisibility(View.VISIBLE);
			findViewById(R.id.disclaimer_valider).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (notifsEnabled)
						enableNotifs();
					else
						disableNotifs();
					AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
					a.setDuration(300);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) {
							l.setVisibility(View.GONE);
							l.setOnClickListener(null);
						}
					});
					l.startAnimation(a);
				}
			});
			final TextView tv1 = (TextView) findViewById(R.id.disclaimer_notifs);
			
			notifsEnabled = true;
			tv1.setText("[X] " + getText(R.string.first_disclaimer_notifs));
			
			tv1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (notifsEnabled)
						tv1.setText("[   ] " + getText(R.string.first_disclaimer_notifs));
					else
						tv1.setText("[X] " + getText(R.string.first_disclaimer_notifs));
					notifsEnabled = !notifsEnabled;
				}
			});
		}
		
		Util.displayAppRateIfNeeded(this);
		
		lv_gifs.post(new Runnable() {
			@Override
			public void run() {
				if (scrollY != 0)
					lv_gifs.setSelectionFromTop(scrollY, 0);
			}
		});
		
		launchUpdateIfNeeded();
	}
	
	private void enableNotifs() {
		Util.setPref(activity, "notifs", "true");
		
		int minutes = 180;
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(Activity_Main.this, NotificationService.class);
		PendingIntent pi = PendingIntent.getService(Activity_Main.this, 0, i, 0);
		am.cancel(pi);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + minutes*60*1000, minutes*60*1000, pi);
		if (notifs != null)
			notifs.setChecked(true);
		try {
			pi.send();
		} catch (CanceledException e) {	e.printStackTrace(); }
	}
	
	private void disableNotifs() {
		Util.setPref(activity, "notifs", "false");
		if (notifs != null)
			notifs.setChecked(false);
	}
	
	public void saveLastViewed() {
		if (gifs != null && gifs.size() > 0)
			Util.setPref(activity, "lastViewed", gifs.get(0).urlArticle);
	}
	
	@Override
	public void onBackPressed() {
		final LinearLayout l = (LinearLayout) findViewById(R.id.about);
		if (l.getVisibility() == View.VISIBLE) {
			AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
			a.setDuration(300);
			a.setAnimationListener(new AnimationListener() {
				@Override public void onAnimationStart(Animation animation) { }
				@Override public void onAnimationRepeat(Animation animation) { }
				@Override public void onAnimationEnd(Animation animation) {
					l.setVisibility(View.GONE);
					l.setOnClickListener(null);
				}
			});
			l.startAnimation(a);
		}
		else
			super.onBackPressed();
	}
	
	private void launchUpdateIfNeeded() {
		boolean letsFetch = false;
		if (!loaded) {
			String lastUpdate = Util.getPref(activity, "lastGifsListUpdate");
			if (lastUpdate.equals("doitnow") || lastUpdate.equals(""))
				letsFetch = true;
			else {
				Date last = Util.stringToDate(Util.getPref(activity, "lastGifsListUpdate"));
				long nbSecs = Util.getSecsDiff(last, new Date());
				long nbHours = nbSecs / 3600;
				letsFetch = nbHours > 12;
			}
		}
		if (!loaded)
			getGifs();
		if (letsFetch)
			new FeedParser(this, gifs, list).execute();
	}

	public void itemClick(View view) {
		TextView label = (TextView) view.findViewById(R.id.line_a);
		Intent intent = new Intent(Activity_Main.this, Activity_Gif.class);
		intent.putExtra("name", label.getText().toString());
		scrollY = ((ListView) findViewById(R.id.gifs_list)).getFirstVisiblePosition();
		Gif g = Util.getGif(gifs, label.getText().toString());
		if (g != null) {
			intent.putExtra("url", g.urlGif);
			startActivity(intent);
			Util.setTransition(this, "rightToLeft");
		}
	}
	
	private void getGifs() {
		if (!Util.getPref(this, "gifs").equals("")) {
			List<Gif> li = Util.getGifs(this);
			
			if (li.size() > 0) {
				gifs = li;
				
				ListView l = (ListView) findViewById(R.id.gifs_list);
				list.clear();
				HashMap<String,String> item;
				for (Gif g : gifs) {
					if (g.isValide()) {
						item = new HashMap<>();
						item.put("line1", g.nom);
						item.put("line2", g.date);
						list.add(item);
					}
				}
				SimpleAdapter sa = new SimpleAdapter(Activity_Main.this, list, R.layout.gifs_list, new String[] { "line1","line2" }, new int[] {R.id.line_a, R.id.line_b});
				l.setAdapter(sa);
				
				l.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
						itemClick(view);
					}
				});
				saveLastViewed();
				
				loaded = true;
			}
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				Util.setPref(this, "lastGifsListUpdate", "doitnow");
				new FeedParser(this, gifs, list).execute();
				return true;
			case R.id.notifications:
				item.setChecked(!item.isChecked());
				if (item.isChecked()) enableNotifs();
				else disableNotifs();
				return true;
			case R.id.menu_about:
				final LinearLayout l = (LinearLayout) findViewById(R.id.about);
				if (l.getVisibility() == View.GONE) {
					Util.Fonts.setFont(this, l, Util.Fonts.CustomFont.Futura);
					l.setVisibility(View.VISIBLE);
					AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
					a.setDuration(500);
					a.setFillAfter(true);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) { }
					});
					l.startAnimation(a);
					l.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
							a.setDuration(300);
							a.setAnimationListener(new AnimationListener() {
								@Override public void onAnimationStart(Animation animation) { }
								@Override public void onAnimationRepeat(Animation animation) { }
								@Override public void onAnimationEnd(Animation animation) {
									l.setVisibility(View.GONE);
									l.setOnClickListener(null);
								}
							});
							l.startAnimation(a);
						}
					});
				} else {
					AlphaAnimation a = new AlphaAnimation(1.0f, 0.0f);
					a.setDuration(300);
					a.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationRepeat(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) {
							l.setVisibility(View.GONE);
							l.setOnClickListener(null);
						}
					});
					l.startAnimation(a);
				}
				return true;
			case R.id.menu_clear_cache:
				Util.clearCache(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		notifs = menu.findItem(R.id.notifications);
		if (Util.getPref(this, "notifs").equals("true"))
			notifs.setChecked(true);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStart(this);
	}
}
