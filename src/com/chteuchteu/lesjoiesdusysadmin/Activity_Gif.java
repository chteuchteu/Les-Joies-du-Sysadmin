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
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Activity_Gif extends Activity {
	private static Activity 	a;
	private static Gif 			gif;
	private static AsyncTask<Void, Integer, Void> downloadGifTh;
	private static int 		oldImageCount;
	private static WebView		wv;
	private static boolean		textsShown = true;
	
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_gif);
		TextView header_nom = (TextView) findViewById(R.id.header_nom);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(" les_joies_du_code();");
			//int c = Color.argb(140, 0, 0, 0);
			int c = Color.argb(200, 12, 106, 179);
			actionBar.setBackgroundDrawable(new ColorDrawable(c));
			final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
					new int[] { android.R.attr.actionBarSize });
			int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
			header_nom.setPadding(0, actionBarHeight, 0, 0);
		}
		
		a = this;
		Intent thisIntent = getIntent();
		String url = null;
		if (thisIntent != null && thisIntent.getExtras() != null
				&& thisIntent.getExtras().containsKey("url"))
			url = thisIntent.getExtras().getString("url");
		gif = Util.getGifFromGifUrl(Activity_Main.gifs, url);
		
		header_nom.setText(gif.nom);
		
		TextView gif_precedent = (TextView) findViewById(R.id.gif_precedent);
		TextView gif_suivant = (TextView) findViewById(R.id.gif_suivant);
		int pos = Util.getGifPos(gif, Activity_Main.gifs);
		if (pos == 0)
			gif_precedent.setVisibility(View.GONE);
		if (pos == Activity_Main.gifs.size()-1)
			gif_suivant.setVisibility(View.GONE);
		gif_precedent.setText(Html.fromHtml("<font color='#222222'>gifPrecedent(</font><font color='#8441FF'>CURRENT_GIF--</font><font color='#222222'>);</font><font color='#00b800'> // GIFs plus recents</font>"), TextView.BufferType.SPANNABLE);
		gif_suivant.setText(Html.fromHtml("gifSuivant(</font><font color='#8441FF'>CURRENT_GIF++</font><font color='#222222'>);</font><font color='#00b800'> // GIFs plus anciens</font>"), TextView.BufferType.SPANNABLE);
		gif_precedent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchGif("previous");
			}
		});
		gif_suivant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchGif("next");
			}
		});
		
		header_nom.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { toggleTexts(); } });
		
		setFont((ViewGroup) findViewById(R.id.actions_container), "SourceCodePro-Regular.ttf");
		setFont((ViewGroup) findViewById(R.id.gif_container), "SourceCodePro-Regular.ttf");
		
		
		File photo = new File(Util.getEntiereFileName(gif, false));
		if (!photo.exists()) {
			downloadGifTh = new downloadGif();
			downloadGifTh.execute();
		} else {
			wv = new WebView(a);
			wv.getSettings().setAllowFileAccess(true);
			wv.getSettings().setJavaScriptEnabled(true);
			wv.getSettings().setBuiltInZoomControls(false);
			wv.setHorizontalScrollBarEnabled(false);
			wv.setVerticalScrollBarEnabled(false);
			wv.setVerticalFadingEdgeEnabled(false);
			wv.setHorizontalFadingEdgeEnabled(false);
			wv.setBackgroundColor(0x00000000);
			String imagePath = Util.getEntiereFileName(gif, true);
			wv.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");
			wv.setVisibility(View.GONE);
			
			LinearLayout ll = (LinearLayout) a.findViewById(R.id.gifContainer);
			for (int i=0; i<ll.getChildCount(); i++) {
				View v = ll.getChildAt(i);
				if (v instanceof WebView)
					ll.removeViewAt(i);
			}
			((ViewGroup) ll).addView(wv, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
			} else
				wv.setVisibility(View.VISIBLE);
		}
	}
	
	private void switchGif(String which) {
		if (textsShown) {
			int pos = Util.getGifPos(gif, Activity_Main.gifs);
			int targetPos = 0;
			if (which.equals("next"))
				targetPos = pos + 1;
			else
				targetPos = pos - 1;
			
			if (targetPos >= 0 && targetPos < Activity_Main.gifs.size()) {
				Intent intent = new Intent(Activity_Gif.this, Activity_Gif.class);
				intent.putExtra("name", Activity_Main.gifs.get(targetPos).nom);
				intent.putExtra("fromName", gif.nom);
				Gif g = Util.getGif(Activity_Main.gifs, Activity_Main.gifs.get(targetPos).nom);
				if (g != null) {
					if (downloadGifTh != null) {
						downloadGifTh.cancel(true);
						File photo = new File(Util.getEntiereFileName(gif, false));
						if (photo.exists())
							photo.delete();
					}
					intent.putExtra("url", g.urlGif);
					intent.putExtra("fromUrl", gif.urlGif);
					startActivity(intent);
					setTransition("rightToLeft");
					finish();
				}
			}
		}
		else
			toggleTexts();
	}
	
	@SuppressLint("NewApi")
	private static void toggleTexts() {
		TextView title = (TextView) a.findViewById(R.id.header_nom);
		LinearLayout actions = (LinearLayout) a.findViewById(R.id.actions_container);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			AlphaAnimation a;
			if (textsShown)
				a = new AlphaAnimation(1.0f, 0.0f);
			else
				a = new AlphaAnimation(0.0f, 1.0f);
			a.setDuration(250);
			a.setFillAfter(true);
			title.startAnimation(a);
			actions.startAnimation(a);
		} else {
			int toBeShown = View.VISIBLE;
			if (textsShown)	toBeShown = View.GONE;
			title.setVisibility(toBeShown);
		}
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
		
		@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
		@Override
		protected void onPostExecute(Void result) {
			pb.setVisibility(View.GONE);
			
			if (photo != null && photo.exists()) {
				try {
					Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_COMPLETE;
					Util.saveGifs(a, Activity_Main.gifs);
					////GifMovieView view = new GifMovieView(a, stream);
					//GifDecoderView view = new GifDecoderView(a, stream);
					////GifWebView view = new GifWebView(a, "file:///android_asset/piggy.gif");
					
					wv = new WebView(a);
					wv.getSettings().setAllowFileAccess(true);
					wv.getSettings().setJavaScriptEnabled(true);
					wv.getSettings().setBuiltInZoomControls(false);
					wv.setHorizontalScrollBarEnabled(false);
					wv.setVerticalScrollBarEnabled(false);
					wv.setVerticalFadingEdgeEnabled(false);
					wv.setHorizontalFadingEdgeEnabled(false);
					wv.setBackgroundColor(0x00000000);
					wv.setVisibility(View.GONE);
					
					String imagePath = Util.getEntiereFileName(gif, true);
					wv.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");
					
					LinearLayout ll = (LinearLayout) a.findViewById(R.id.gifContainer);
					for (int i=0; i<ll.getChildCount(); i++) {
						View v = ll.getChildAt(i);
						if (v instanceof WebView)
							ll.removeViewAt(i);
					}
					((ViewGroup) ll).addView(wv, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
					} else
						wv.setVisibility(View.VISIBLE);
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
		startActivity(new Intent(Activity_Gif.this, Activity_Main.class));
		setTransition("leftToRight");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
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
				startActivity(new Intent(Activity_Gif.this, Activity_Main.class));
				return true;
			case R.id.menu_refresh:
				if (downloadGifTh != null)
					downloadGifTh.cancel(true);
				File photo = new File(Util.getEntiereFileName(gif, false));
				if (photo.exists())
					photo.delete();
				downloadGifTh = new downloadGif();
				final LinearLayout rl = (LinearLayout) a.findViewById(R.id.gifContainer);
				
				oldImageCount = 0;
				for (int i=0; i<rl.getChildCount(); i++) {
					/*if (rl.getChildAt(i) instanceof GifDecoderView) {
						oldImageCount = i; break;
					}*/ // TODO 
				}
				if (rl.getChildCount() > 1) {
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
							rl.removeViewAt(oldImageCount);
							((ProgressBar) a.findViewById(R.id.pb)).setVisibility(View.VISIBLE);
						}
					});
					rl.getChildAt(oldImageCount).startAnimation(an);
				}
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
}