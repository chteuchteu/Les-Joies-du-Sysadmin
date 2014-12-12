package com.chteuchteu.lesjoiesdusysadmin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.lesjoiesdusysadmin.R;
import com.chteuchteu.lesjoiesdusysadmin.at.GifDownloader;
import com.chteuchteu.lesjoiesdusysadmin.hlpr.Util;
import com.chteuchteu.lesjoiesdusysadmin.obj.Gif;
import com.google.analytics.tracking.android.EasyTracker;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;

public class Activity_Gif extends ActionBarActivity {
	public static int pos = -1;
	private static Activity activity;
	public static Gif gif;
	public static Gif			old_gif;
	private GifDownloader gifDownloader;
	public static WebView webView;
	public boolean				textsShown = true;
	public float				deltaY;
	
	public boolean		finishedDownload = true;
	public boolean		loaded = false;
	
	public static int			SWITCH_NEXT = 1;
	public static int			SWITCH_PREVIOUS = 0;
	
	public static boolean		fromWeb;
	
	@SuppressLint({ "SetJavaScriptEnabled", "InlinedApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gif);
		activity = this;

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		fromWeb = false;
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (action != null && action.equals(Intent.ACTION_VIEW)) {
			fromWeb = true;
			String uri = intent.getDataString();
			if (Activity_Main.gifs == null || Activity_Main.gifs.size() == 0)
				Activity_Main.gifs = Util.getGifs(this);
			if (Activity_Main.gifs.size() == 0) {
				Activity_Main.gifs = null;
				// First launch while trying to open the app from web = no gif cached
				startActivity(new Intent(Activity_Gif.this, Activity_Main.class));
			}
			gif = Util.getGifFromWebUrl(Activity_Main.gifs, uri);
			if (gif == null) {
				fromWeb = false;
				Toast.makeText(this, "Impossible d'afficher ce gif depuis le web...", Toast.LENGTH_LONG).show();
			}
		}

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

		Intent thisIntent = getIntent();
		String url = "";
		if (Activity_Main.gifs != null && Activity_Main.gifs.size() > 0)
			url = Activity_Main.gifs.get(0).urlGif;
		if (thisIntent != null && thisIntent.getExtras() != null
				&& thisIntent.getExtras().containsKey("url"))
			url = thisIntent.getExtras().getString("url");
		
		if (!fromWeb || gif == null)
			gif = Util.getGifFromGifUrl(Activity_Main.gifs, url);
		
		pos = Util.getGifPos(gif, Activity_Main.gifs);
		
		old_gif = gif;
		
		if (url != null)
			restoreActivity();
		
		TextView header_nom = (TextView) findViewById(R.id.header_nom);
		header_nom.setText(gif.nom);
		if (header_nom.getText().toString().length() / 32 > 4) // nb lines
			header_nom.setLineSpacing(-10, 1);
		else if (header_nom.getText().toString().length() / 32 > 6)
			header_nom.setLineSpacing(-25, 1);
		
		webView = (WebView) findViewById(R.id.wv);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setVerticalScrollBarEnabled(false);
		webView.setVerticalFadingEdgeEnabled(false);
		webView.setHorizontalFadingEdgeEnabled(false);
		webView.setBackgroundColor(0x00000000);

		
		TextView gif_precedent = (TextView) findViewById(R.id.gif_precedent);
		TextView gif_suivant = (TextView) findViewById(R.id.gif_suivant);
		int pos = Util.getGifPos(gif, Activity_Main.gifs);
		if (pos == 0)
			gif_precedent.setVisibility(View.GONE);
		if (pos == Activity_Main.gifs.size()-1)
			gif_suivant.setVisibility(View.GONE);
		gif_precedent.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { switchGif(SWITCH_PREVIOUS); } });
		gif_suivant.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { switchGif(SWITCH_NEXT); } });
		
		findViewById(R.id.actions_container).post(new Runnable(){
			public void run() {
				deltaY = findViewById(R.id.actions_container).getHeight()/2;
			}
		});
		findViewById(R.id.onclick_catcher).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { toggleTexts(); } });
		
		Util.Fonts.setFont(this, findViewById(R.id.header_nom), Util.Fonts.CustomFont.Roboto_Regular);
		Util.Fonts.setFont(this, findViewById(R.id.gif_precedent), Util.Fonts.CustomFont.Roboto_Regular);
		Util.Fonts.setFont(this, findViewById(R.id.gif_suivant), Util.Fonts.CustomFont.Roboto_Regular);
		
		if (gif == null && old_gif != null)
			gif = old_gif;
	}
	
	private void restoreActivity() {
		textsShown = true;
		finishedDownload = true;
		loaded = false;
		stopThread();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (gif == null && Activity_Main.gifs != null && pos != -1)
			gif = Activity_Main.gifs.get(pos);
		
		if (gif != null)
			loadGif();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		gif = null;
	}
	
	private void loadGif() {
		if (!loaded) {
			File photo = new File(Util.getEntiereFileName(gif, false));
			stopThread();
			webView.setVisibility(View.GONE);
			if (!photo.exists()) {
				gifDownloader = new GifDownloader(this, gif, pos, webView);
				gifDownloader.execute();
			} else {
				if (gif.state != Gif.ST_COMPLETE) {
					gif.state = Gif.ST_COMPLETE;
					Util.saveGifs(activity, Activity_Main.gifs);
				}
				String imagePath = Util.getEntiereFileName(gif, true);
				webView.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html", "utf-8", "");
				
				int pos = Util.getGifPos(gif, Activity_Main.gifs);
				if (pos == 0)	findViewById(R.id.gif_precedent).setVisibility(View.GONE);
				else			findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
				if (pos == Activity_Main.gifs.size()-1)		findViewById(R.id.gif_suivant).setVisibility(View.GONE);
				else			findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
				
				((TextView) findViewById(R.id.header_nom)).setText(gif.nom);
				
				webView.setWebViewClient(new WebViewClient() {
					public void onPageFinished(WebView v, String u) {
						webView.setVisibility(View.VISIBLE);
						AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
						a.setStartOffset(250);
						a.setDuration(350);
						a.setFillEnabled(true);
						a.setFillAfter(true);
						webView.startAnimation(a);
					}
				});
			}
		}
	}
	
	private void switchGif(int which) {
		if (textsShown) {
			if (gif != null) {
				stopThread();
				int pos = Util.getGifPos(gif, Activity_Main.gifs);
				int targetPos;
				if (which == SWITCH_NEXT)
					targetPos = pos + 1;
				else
					targetPos = pos - 1;
				
				if (targetPos >= 0 && targetPos < Activity_Main.gifs.size()) {
					gif = Activity_Main.gifs.get(targetPos);
					//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					//	updateSharingIntent();
					finishedDownload = false;
					loaded = false;
					
					if (webView.getVisibility() == View.VISIBLE) {
						AlphaAnimation an = new AlphaAnimation(1.0f, 0.0f);
						an.setDuration(150);
						an.setAnimationListener(new AnimationListener() {
							@Override public void onAnimationStart(Animation animation) { }
							@Override public void onAnimationRepeat(Animation animation) { }
							@Override
							public void onAnimationEnd(Animation animation) {
								webView.setVisibility(View.GONE);
							}
						});
						webView.startAnimation(an);
					}
					
					loadGif();
					
					pos = targetPos;
					
					if (!finishedDownload) {
						if (targetPos == 0)	activity.findViewById(R.id.gif_precedent).setVisibility(View.GONE);
						else			activity.findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
						if (targetPos == Activity_Main.gifs.size()-1)		activity.findViewById(R.id.gif_suivant).setVisibility(View.GONE);
						else			activity.findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
						((TextView) activity.findViewById(R.id.header_nom)).setText(gif.nom);
					}
				}
			}
		}
		else
			toggleTexts();
	}
	
	private void stopThread() {
		if (gifDownloader != null) {
			gifDownloader.cancel(true);
			if (!finishedDownload) {
				File photo = new File(Util.getEntiereFileName(gif, false));
				if (photo.exists())
					photo.delete();
			}
		}
	}
	
	private void toggleTexts() {
		TextView title = (TextView) activity.findViewById(R.id.header_nom);
		RelativeLayout actions = (RelativeLayout) activity.findViewById(R.id.actions_container);
		LinearLayout titleContainer = (LinearLayout) activity.findViewById(R.id.header_nom_container);
		
		AlphaAnimation a;
		if (textsShown)
			a = new AlphaAnimation(1.0f, 0.0f);
		else
			a = new AlphaAnimation(0.0f, 1.0f);
		a.setDuration(250);
		a.setFillEnabled(true);
		a.setFillAfter(true);
		title.startAnimation(a);
		actions.startAnimation(a);
		titleContainer.startAnimation(a);
		
		// Put the gif a little bit higher
		deltaY = findViewById(R.id.actions_container).getHeight()/2;
		if (textsShown)
			deltaY = -deltaY;
		
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, deltaY);
		anim.setDuration(250);
		anim.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.wv_container);
				RelativeLayout act = (RelativeLayout) findViewById(R.id.actions_container);
				if (textsShown)
					ll.layout(ll.getLeft(), ll.getTop()+act.getHeight()/2, ll.getRight(), ll.getBottom());
				else
					ll.layout(ll.getLeft(), ll.getTop()-act.getHeight()/2, ll.getRight(), ll.getBottom());
			}
		});
		anim.setFillEnabled(true);
		anim.setFillAfter(false);
		anim.setFillBefore(false);
		findViewById(R.id.wv_container).startAnimation(anim);
		
		textsShown = !textsShown;
	}
	
	@Override
	public void onBackPressed() {
		stopThread();
		finish();
		Util.setTransition(this, "leftToRight");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		EasyTracker.getInstance(this).activityStart(this);
		
		if (gifDownloader != null) {
			try {
				gifDownloader.cancel(false);
			} catch (Exception ignored) { }
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Util.removeUncompleteGifs(activity, Activity_Main.gifs);
	}
	
	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gifs, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				stopThread();
				startActivity(new Intent(Activity_Gif.this, Activity_Main.class));
				Util.setTransition(this, "leftToRight");
				return true;
			case R.id.menu_refresh:
				stopThread();
				
				gifDownloader = new GifDownloader(this, gif, pos, webView);
				
				AlphaAnimation an = new AlphaAnimation(1.0f, 0.0f);
				an.setDuration(150);
				an.setFillEnabled(true);
				an.setFillAfter(true);
				an.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) { }
					@Override public void onAnimationRepeat(Animation animation) { }

					@Override
					public void onAnimationEnd(Animation animation) {
						activity.findViewById(R.id.pb).setVisibility(View.VISIBLE);
					}
				});
				webView.startAnimation(an);
				
				gifDownloader.execute();
				return true;
			case R.id.menu_openwebsite:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gif.urlArticle));
				startActivity(browserIntent);
				return true;
			case R.id.menu_share:
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Les Joies du Sysadmin");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, gif.nom + " : " + gif.urlArticle);
				startActivity(Intent.createChooser(sharingIntent, "Partager via"));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// Bug fix : when another applications goes foreground : gif becomes null
		if (gif == null && Activity_Main.gifs != null && pos != -1)
			gif = Activity_Main.gifs.get(pos);
		
		EasyTracker.getInstance(this).activityStart(this);
	}
}