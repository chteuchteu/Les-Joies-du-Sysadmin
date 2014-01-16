package com.chteuchteu.lesjoiesdusysadmin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class Activity_Gif extends Activity {
	private static Activity 	a;
	private static Gif 			gif;
	private static Gif			old_gif;
	private AsyncTask<Void, Integer, Void> downloadGifTh;
	private static WebView		wv;
	private boolean			textsShown = true;
	private float				deltaY;
	
	private static boolean		finishedDownload = true;
	private static boolean		loaded = false;
	private int					actionBarColor = Color.argb(210, 0, 82, 156); // (210, 44, 62, 80);
	
	private static int			SWITCH_UNKNOWN = -1;
	private static int			SWITCH_NEXT = 1;
	private static int			SWITCH_PREVIOUS = 0;
	
	private static int			currentlySwitching = SWITCH_UNKNOWN;
	private static boolean		fromWeb;
	
	@SuppressLint({ "SetJavaScriptEnabled", "InlinedApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_gif);
		
		fromWeb = false;
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (action != null && action.equals(Intent.ACTION_VIEW)) {
			fromWeb = true;
			String uri = intent.getDataString();
			if (Activity_Main.gifs == null || Activity_Main.gifs.size() == 0)
				Activity_Main.gifs = Util.getGifs(this);
			gif = Util.getGifFromWebUrl(Activity_Main.gifs, uri);
			if (gif == null) {
				fromWeb = false;
				Toast.makeText(this, "Impossible d'afficher ce gif depuis le web...", Toast.LENGTH_LONG).show();
			}
		}
		
		int contentPaddingTop = 0;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(" Les Joies du Sysadmin");
		int c = actionBarColor;
		actionBar.setBackgroundDrawable(new ColorDrawable(c));
		final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		contentPaddingTop += (int) styledAttributes.getDimension(0, 0);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				LinearLayout notifBarBG = (LinearLayout) findViewById(R.id.kitkat_actionbar_notifs);
				notifBarBG.setBackgroundColor(actionBarColor);
				notifBarBG.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight()));
				notifBarBG.setVisibility(View.VISIBLE);
				contentPaddingTop += getStatusBarHeight();
			}
		}
		if (contentPaddingTop != 0) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, contentPaddingTop, 0, 0);
			findViewById(R.id.actions_container).setLayoutParams(lp);
		}
		
		a = this;
		Intent thisIntent = getIntent();
		String url = "";
		if (Activity_Main.gifs != null && Activity_Main.gifs.size() > 0)
			url = Activity_Main.gifs.get(0).urlGif;
		if (thisIntent != null && thisIntent.getExtras() != null
				&& thisIntent.getExtras().containsKey("url"))
			url = thisIntent.getExtras().getString("url");
		
		if (!fromWeb || gif == null)
			gif = Util.getGifFromGifUrl(Activity_Main.gifs, url);
		
		old_gif = gif;
		
		if (url != null)
			restoreActivity();
		
		TextView header_nom = (TextView) findViewById(R.id.header_nom);
		header_nom.setText(gif.nom);
		if (header_nom.getText().toString().length() / 32 > 4) // nb lines
			header_nom.setLineSpacing(-10, 1);
		else if (header_nom.getText().toString().length() / 32 > 6)
			header_nom.setLineSpacing(-25, 1);
		
		wv = (WebView) findViewById(R.id.wv);
		wv.getSettings().setAllowFileAccess(true);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(false);
		wv.setHorizontalScrollBarEnabled(false);
		wv.setVerticalScrollBarEnabled(false);
		wv.setVerticalFadingEdgeEnabled(false);
		wv.setHorizontalFadingEdgeEnabled(false);
		wv.setBackgroundColor(0x00000000);
		int marginTop = 0;
		marginTop += Util.getActionBarHeight(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			marginTop += Util.getActionBarHeight(this) / 2;
		marginTop += 35; // Actions
		findViewById(R.id.wv_container).setPadding(0, marginTop, 0, 0);
		
		TextView gif_precedent = (TextView) findViewById(R.id.gif_precedent);
		TextView gif_suivant = (TextView) findViewById(R.id.gif_suivant);
		int pos = Util.getGifPos(gif, Activity_Main.gifs);
		if (pos == 0)
			gif_precedent.setVisibility(View.GONE);
		if (pos == Activity_Main.gifs.size()-1)
			gif_suivant.setVisibility(View.GONE);
		gif_precedent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchGif(SWITCH_PREVIOUS);
			}
		});
		gif_suivant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchGif(SWITCH_NEXT);
			}
		});
		
		findViewById(R.id.actions_container).post(new Runnable(){
			public void run() {
				deltaY = findViewById(R.id.actions_container).getHeight()/2;
			}
		});
		findViewById(R.id.onclick_catcher).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { toggleTexts(); } });
		
		setFont(findViewById(R.id.header_nom), "RobotoCondensed-Light.ttf");
		setFont(findViewById(R.id.gif_precedent), "RobotoCondensed-Regular.ttf");
		setFont(findViewById(R.id.gif_suivant), "RobotoCondensed-Regular.ttf");
		
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
			wv.setVisibility(View.GONE);
			if (!photo.exists()) {
				downloadGifTh = new downloadGif();
				downloadGifTh.execute();
			} else {
				String imagePath = Util.getEntiereFileName(gif, true);
				wv.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");
				
				int pos = Util.getGifPos(gif, Activity_Main.gifs);
				if (pos == 0)	findViewById(R.id.gif_precedent).setVisibility(View.GONE);
				else			findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
				if (pos == Activity_Main.gifs.size()-1)		findViewById(R.id.gif_suivant).setVisibility(View.GONE);
				else			findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
				
				((TextView) findViewById(R.id.header_nom)).setText(gif.nom);
				
				currentlySwitching = SWITCH_UNKNOWN;
				
				wv.setWebViewClient(new WebViewClient() {
					public void onPageFinished(WebView v, String u) {
						wv.setVisibility(View.VISIBLE);
						AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
						a.setStartOffset(250);
						a.setDuration(350);
						a.setFillAfter(true);
						wv.startAnimation(a);
					}
				});
			}
		}
	}
	
	private void switchGif(int which) {
		if (textsShown) {
			stopThread();
			int pos = Util.getGifPos(gif, Activity_Main.gifs);
			int targetPos = 0;
			if (currentlySwitching == SWITCH_NEXT && which == SWITCH_PREVIOUS)
				targetPos = pos - 1;
			else if (currentlySwitching == SWITCH_PREVIOUS && which == SWITCH_NEXT)
				targetPos = pos + 1;
			else if (currentlySwitching == SWITCH_UNKNOWN) {
				if (which == SWITCH_NEXT)
					targetPos = pos + 1;
				else
					targetPos = pos - 1;
			}
			currentlySwitching = which;
			
			if (targetPos >= 0 && targetPos < Activity_Main.gifs.size()) {
				gif = Activity_Main.gifs.get(targetPos);
				finishedDownload = false;
				loaded = false;
				loadGif();
				
				if (!finishedDownload) {
					if (targetPos == 0)	a.findViewById(R.id.gif_precedent).setVisibility(View.GONE);
					else			a.findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
					if (targetPos == Activity_Main.gifs.size()-1)		a.findViewById(R.id.gif_suivant).setVisibility(View.GONE);
					else			a.findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
					((TextView) a.findViewById(R.id.header_nom)).setText(gif.nom);
				}
			}
		}
		else
			toggleTexts();
	}
	
	private void stopThread() {
		if (downloadGifTh != null) {
			downloadGifTh.cancel(true);
			if (!finishedDownload) {
				File photo = new File(Util.getEntiereFileName(gif, false));
				if (photo.exists())
					photo.delete();
			}
		}
	}
	
	private void toggleTexts() {
		TextView title = (TextView) a.findViewById(R.id.header_nom);
		RelativeLayout actions = (RelativeLayout) a.findViewById(R.id.actions_container);
		LinearLayout titleContainer = (LinearLayout) a.findViewById(R.id.header_nom_container);
		
		AlphaAnimation a;
		if (textsShown)
			a = new AlphaAnimation(1.0f, 0.0f);
		else
			a = new AlphaAnimation(0.0f, 1.0f);
		a.setDuration(250);
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
	
	static class downloadGif extends AsyncTask<Void, Integer, Void> {
		File photo;
		ProgressBar pb;
		
		public downloadGif() {
			super();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb = (ProgressBar) a.findViewById(R.id.pb);
			pb.setVisibility(View.VISIBLE);
			pb.setIndeterminate(true);
			pb.setProgress(0);
			pb.setMax(100);
			finishedDownload = false;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			pb.setProgress(progress[0]);
			pb.setIndeterminate(false);
		}
		
		@SuppressWarnings("resource")
		@Override
		protected Void doInBackground(Void... arg0) {
			int fileLength = 0;
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				photo = new File(Util.getEntiereFileName(gif, false));
				URL url = new URL(gif.urlGif);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return null;
				
				fileLength = connection.getContentLength();
				
				input = connection.getInputStream();
				output = new FileOutputStream(Util.getEntiereFileName(gif, false));
				
				byte data[] = new byte[4096];
				long total = 0;
				int count;
				
				Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_DOWNLOADING;
				Util.saveGifs(a, Activity_Main.gifs);
				
				while ((count = input.read(data)) != -1) {
					if (isCancelled()) {
						input.close();
						if (photo.exists())
							photo.delete();
						return null;
					}
					total += count;
					publishProgress((int)((total*100)/fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) { }
				
				if (connection != null)
					connection.disconnect();
			}
			
			return null;
		}
		
		@SuppressLint("SetJavaScriptEnabled")
		@Override
		protected void onPostExecute(Void result) {
			pb.setVisibility(View.GONE);
			
			finishedDownload = true;
			
			int pos = Util.getGifPos(gif, Activity_Main.gifs);
			if (pos == 0)	a.findViewById(R.id.gif_precedent).setVisibility(View.GONE);
			else			a.findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
			if (pos == Activity_Main.gifs.size()-1)		a.findViewById(R.id.gif_suivant).setVisibility(View.GONE);
			else			a.findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
			((TextView) a.findViewById(R.id.header_nom)).setText(gif.nom);
			
			if (photo != null && photo.exists()) {
				loaded = true;
				currentlySwitching = SWITCH_UNKNOWN;
				try {
					Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_COMPLETE;
					Util.saveGifs(a, Activity_Main.gifs);
					
					
					wv.setVisibility(View.GONE);
					String imagePath = Util.getEntiereFileName(gif, true);
					wv.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");
					
					wv.setWebViewClient(new WebViewClient() {
						public void onPageFinished(WebView v, String u) {
							wv.setVisibility(View.VISIBLE);
							AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
							a.setStartOffset(250);
							a.setDuration(350);
							a.setFillAfter(true);
							wv.startAnimation(a);
						}
					});
				}
				catch (Exception ex) {
					ex.printStackTrace();
					Toast.makeText(a, "Erreur lors du téléchargement de ce gif...", Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(a, "Erreur lors du téléchargement de ce gif...", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		stopThread();
		finish();
		setTransition("leftToRight");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		EasyTracker.getInstance(this).activityStart(this);
		
		if (downloadGifTh != null) {
			try {
				downloadGifTh.cancel(false);
			} catch (Exception ignored) { }
		}
	}
	
	
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
				finish();
				setTransition("leftToRight");
				return true;
			case R.id.menu_refresh:
				stopThread();
				
				downloadGifTh = new downloadGif();
				
				AlphaAnimation an = new AlphaAnimation(1.0f, 0.0f);
				an.setDuration(150);
				an.setFillAfter(true);
				an.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationRepeat(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						((ProgressBar) a.findViewById(R.id.pb)).setVisibility(View.VISIBLE);
					}
				});
				wv.startAnimation(an);
				
				downloadGifTh.execute();
				return true;
			case R.id.menu_openwebsite:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gif.urlArticle));
				startActivity(browserIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void setFont(ViewGroup g, String font) {
		Typeface mFont = Typeface.createFromAsset(getAssets(), font);
		setFont(g, mFont);
	}
	public void setFont(View v, String font) {
		Typeface mFont = Typeface.createFromAsset(getAssets(), font);
		((TextView) v).setTypeface(mFont);
	}
	public void setFont(ViewGroup group, Typeface font) {
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
				((TextView) v).setTypeface(font);
			} else if (v instanceof ViewGroup)
				setFont((ViewGroup) v, font);
		}
	}
	
	public void setTransition(String level) {
		if (level.equals("rightToLeft"))
			overridePendingTransition(R.anim.deeper_in, R.anim.deeper_out);
		else if (level.equals("leftToRight"))
			overridePendingTransition(R.anim.shallower_in, R.anim.shallower_out);
	}
	
	public int getStatusBarHeight() {
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			return getResources().getDimensionPixelSize(resourceId);
		return 0;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
}