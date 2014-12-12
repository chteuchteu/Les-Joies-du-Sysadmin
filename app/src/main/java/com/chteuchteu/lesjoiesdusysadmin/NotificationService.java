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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

import com.chteuchteu.lesjoiesdusysadmin.hlpr.Util;
import com.chteuchteu.lesjoiesdusysadmin.obj.Gif;
import com.chteuchteu.lesjoiesdusysadmin.ui.Activity_Main;
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
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.chteuchteu.lesjoiesdusysadmin");
		mWakeLock.acquire();
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getBackgroundDataSetting()) {
			stopSelf();
			return;
		}
		// do the actual work, in a separate thread
		new PollTask().execute();
	}
	
	private class PollTask extends AsyncTask<Void, Void, Void> {
		int nbUnseenGifs = 0;
		List<Gif> l;
		
		@Override
		protected Void doInBackground(Void... params) {
			l = new ArrayList<Gif>();
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
			
			String lastUnseenGif = getPref("lastViewed");
			if (l.size() > 0) {
				for (Gif g : l) {
					if (g.urlArticle.equals(lastUnseenGif))
						break;
					else
						nbUnseenGifs++;
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// Check if there are new gifs, and if a notification for them hasn't been dispayed yet
			boolean notif = (nbUnseenGifs > 0 && 
					(getPref("lastNotifiedGif").equals("") 
							|| !getPref("lastNotifiedGif").equals("") && l.size() > 0 && !l.get(0).urlGif.equals(getPref("lastNotifiedGif"))));
			
			if (notif) {
				// Save the last gif
				if (l.size() > 0)
					setPref("lastNotifiedGif", l.get(0).urlGif);
				
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
				.setSmallIcon(R.drawable.ic_notifications)
				.setNumber(nbUnseenGifs)
				.setContentTitle(title)
				.setAutoCancel(true)
				.setContentText(text);
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
	
	public void setPref(String key, String value) {
		SharedPreferences prefs = this.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
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