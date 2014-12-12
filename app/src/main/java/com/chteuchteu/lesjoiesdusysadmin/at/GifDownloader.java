package com.chteuchteu.lesjoiesdusysadmin.at;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.lesjoiesdusysadmin.R;
import com.chteuchteu.lesjoiesdusysadmin.hlpr.Util;
import com.chteuchteu.lesjoiesdusysadmin.obj.Gif;
import com.chteuchteu.lesjoiesdusysadmin.ui.Activity_Gif;
import com.chteuchteu.lesjoiesdusysadmin.ui.Activity_Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GifDownloader extends AsyncTask<Void, Integer, Void> {
	private Activity_Gif activity;
	private Gif gif;
	private int pos;
	private WebView wv;

	private File photo;
	private ProgressBar pb;

	public GifDownloader(Activity_Gif activity, Gif gif, int pos, WebView webView) {
		super();

		this.activity = activity;
		this.gif = gif;
		this.pos = pos;
		this.wv = webView;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pb = (ProgressBar) activity.findViewById(R.id.pb);
		pb.setVisibility(View.VISIBLE);
		pb.setIndeterminate(true);
		pb.setProgress(0);
		pb.setMax(100);
		activity.finishedDownload = false;
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
		int fileLength;
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

		activity.finishedDownload = true;

		if (gif == null && Activity_Main.gifs != null && pos != -1)
			gif = Activity_Main.gifs.get(pos);

		int pos = Util.getGifPos(gif, Activity_Main.gifs);
		if (pos == 0)	activity.findViewById(R.id.gif_precedent).setVisibility(View.GONE);
		else			activity.findViewById(R.id.gif_precedent).setVisibility(View.VISIBLE);
		if (pos == Activity_Main.gifs.size()-1)		activity.findViewById(R.id.gif_suivant).setVisibility(View.GONE);
		else			activity.findViewById(R.id.gif_suivant).setVisibility(View.VISIBLE);
		((TextView) activity.findViewById(R.id.header_nom)).setText(gif.nom);

		if (photo != null && photo.exists()) {
			activity.loaded = true;
			try {
				Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_COMPLETE;
				Util.saveGifs(activity, Activity_Main.gifs);

				wv.setVisibility(View.GONE);
				String imagePath = Util.getEntiereFileName(gif, true);
				wv.loadDataWithBaseURL("", Util.getHtml(imagePath), "text/html","utf-8", "");

				wv.setWebViewClient(new WebViewClient() {
					public void onPageFinished(WebView v, String u) {
						wv.setVisibility(View.VISIBLE);
						AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
						a.setStartOffset(250);
						a.setDuration(350);
						a.setFillEnabled(true);
						a.setFillAfter(true);
						wv.startAnimation(a);
					}
				});
			} catch (Exception ex) {
				Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_DOWNLOADING;

				Util.removeUncompleteGifs(activity, Activity_Main.gifs);
				ex.printStackTrace();
				Toast.makeText(activity, "Erreur lors du téléchargement de ce gif...", Toast.LENGTH_SHORT).show();
				pb.setVisibility(View.GONE);
			}
		} else {
			Toast.makeText(activity, "Erreur lors du téléchargement de ce gif...", Toast.LENGTH_SHORT).show();
			Util.getGif(Activity_Main.gifs, gif.nom).state = Gif.ST_DOWNLOADING;
			Util.removeUncompleteGifs(activity, Activity_Main.gifs);
			pb.setVisibility(View.GONE);
		}
	}
}
