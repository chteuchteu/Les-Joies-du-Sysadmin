package com.chteuchteu.lesjoiesdusysadmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

public class NotificationService extends Service {
	private WakeLock mWakeLock;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void handleIntent(Intent intent) {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.chteuchteu.lesjoiesducode");
		mWakeLock.acquire();
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getBackgroundDataSetting()) {
			stopSelf();
			return;
		}
		Log.v("LesJoisDuSysadmin", "Launching thread");
		// do the actual work, in a separate thread
		new PollTask().execute();
	}
	
	/*private boolean downloadGif(String urlGif, String urlArticle) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlGif);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				return false;
			
			input = connection.getInputStream();
			output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/lesJoiesDuCode/" + urlArticle.substring(urlArticle.lastIndexOf('/')) + ".gif");
			
			byte data[] = new byte[4096];
			int count;
			
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
		return true;
	}*/
	
	private class PollTask extends AsyncTask<Void, Void, Void> {
		int nbUnseenGifs = 0;
		
		@Override
		protected Void doInBackground(Void... params) {
			List<Gif> l = new ArrayList<Gif>();
			try {
				JumblrClient client = new JumblrClient("3TRQZe87tlv3jXHuF9AHtDydThIn1hDijFNLLhGEULVRRHpM3q", "4BpchUIeOkEFMAkNGiIKjpgG8sLVliKA8cgIFSa3JuQ6Ta0qNd");
				boolean getPosts = true;
				int offset = 0;
				while (getPosts) {
					Map<String, Object> feedparams = new HashMap<String, Object>();
					feedparams.put("limit", 20);
					feedparams.put("offset", offset);
					List<Post> posts = client.blogPosts("lesjoiesdusysadmin.tumblr.com", feedparams);
					
					for (Post p : posts) {
						TextPost tp = (TextPost) p;
						Log.v("", "Fetched post " + tp.getTitle());
						Gif g = new Gif();
						//g.date = Util.GMTDateToFrench3(tp.getDateGMT());
						g.nom = tp.getTitle();
						g.state = Gif.ST_EMPTY;
						g.urlArticle = tp.getPostUrl();
						// <p><p class="c1"><img alt="image" src="http://i.imgur.com/49DLfGd.gif"/></p>
						g.urlGif = Util.getSrcAttribute(tp.getBody());
						l.add(g);
					}
					if (posts.size() > 0)
						offset += 20;
					else
						getPosts = false;
				}
			} catch (Exception ex) { ex.printStackTrace(); }
			
			if (l.size() == 0)
				return null;
			
			Log.v("", "Checking if there are new ones");
			String lastUnseenGif = getPref("lastViewed");
			if (l.size() > 0) {
				for (Gif g : l) {
					Log.v("", "Looking at gif " + g.nom);
					if (g.urlArticle.equals(lastUnseenGif))
						break;
					else {
						nbUnseenGifs++;
					}
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Log.v("", "onPostExecute");
			if (nbUnseenGifs > 0) {
				String title;
				String text;
				if (nbUnseenGifs > 1) {
					title = "Les Joies du Sysadmin";
					text = nbUnseenGifs + " nouveaux gifs !";
				} else {
					title = "Les Joies du Sysadmin";
					text = "1 nouveau gif !";
				}
				NotificationCompat.Builder builder =
						new NotificationCompat.Builder(NotificationService.this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(text);
				int NOTIFICATION_ID = 1664;
				
				Intent targetIntent = new Intent(NotificationService.this, Activity_Main.class);
				PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				builder.setContentIntent(contentIntent);
				NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nManager.notify(NOTIFICATION_ID, builder.build());
			} else {
				NotificationCompat.Builder builder =
						new NotificationCompat.Builder(NotificationService.this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Les Joies du Sysadmin")
				.setContentText("Pas de nouveau gif");
				int NOTIFICATION_ID = 1664;
				Intent targetIntent = new Intent(NotificationService.this, Activity_Main.class);
				PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				builder.setContentIntent(contentIntent);
				NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nManager.notify(NOTIFICATION_ID, builder.build());
			}
			stopSelf();
		}
	}
	
	private String getPref(String key) {
		return this.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, "");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		handleIntent(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_NOT_STICKY;
	}
	
	public void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
	}
}