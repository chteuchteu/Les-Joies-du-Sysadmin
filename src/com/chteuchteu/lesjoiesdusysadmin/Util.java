package com.chteuchteu.lesjoiesdusysadmin;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Environment;

public final class Util {
	private Util() { }
	
	@SuppressLint("InlinedApi")
	public static int getActionBarHeight(Context c) {
		final TypedArray styledAttributes = c.getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		int height = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();
		return height;
	}
	
	public static void saveGifs(Activity a, List<Gif> gifs) {
		String str = "";
		int i=0;
		for (Gif g : gifs) {
			if (i != gifs.size()-1)
				str = str + g.nom + "::" + g.urlArticle + "::" + g.urlGif + "::" + g.date + "::" + g.state + ";;";
			else
				str = str + g.nom + "::" + g.urlArticle + "::" + g.urlGif + "::" + g.date + "::" + g.state;
			i++;
		}
		setPref(a, "gifs", str);
	}
	
	public static String getSrcAttribute(String html) {
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		// jsoup : *= : contient la valeur (certains gifs sont sous la forme image.gif?3848483)
		//org.jsoup.nodes.Element img = doc.select("img[src*=.gif]").first();
		org.jsoup.nodes.Element img = doc.select("img[src]").first();
		if (img != null)
			return img.attr("src");
		return "";
	}
	
	public static String GMTDateToFrench3(String gmtDate) {
		try {
			// 2012-06-18 08:47:37 GMT
			//SimpleDateFormat dfGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			SimpleDateFormat dfGMT = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss z", Locale.ENGLISH);
			dfGMT.parse(gmtDate);
			SimpleDateFormat dfFrench = new SimpleDateFormat("d/MM", Locale.FRANCE);
			return dfFrench.format(dfGMT.getCalendar().getTime());
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	public static String getFileName(Gif g) {
		return g.urlArticle.substring(g.urlArticle.lastIndexOf('/'));
	}
	
	public static String getEntiereFileName(Gif g, boolean withFilePrefix) {
		String path = "";
		if (withFilePrefix)
			path += "file://";
		path += Environment.getExternalStorageDirectory().getPath() + "/lesJoiesDuSysadmin/" + Util.getFileName(g) + ".gif";
		return path;
	}
	
	public static boolean removeUncompleteGifs(Activity a, List<Gif> l) {
		boolean needSave = false;
		for (Gif g : l) {
			if (g.state == Gif.ST_DOWNLOADING) {
				File f = new File(Util.getEntiereFileName(g, false));
				if (f.exists())
					f.delete();
				g.state = Gif.ST_EMPTY;
				needSave = true;
			}
		}
		if (needSave)
			saveGifs(a, l);
		return needSave;
	}
	
	public static void removeOldGifs(List<Gif> l) {
		if (l != null && l.size() > 10) {
			String path = Environment.getExternalStorageDirectory().toString() + "/lesJoiesDuSysadmin/";
			File dir = new File(path);
			File files[] = dir.listFiles();
			if (files != null) {
				List<File> toBeDeleted = new ArrayList<File>();
				for (File f : files) {
					boolean shouldBeDeleted = true;
					int max = 15;
					if (l.size() < 15)	max = l.size();
					for (int i=0; i<max; i++) {
						String fileName = getFileName(l.get(i)).replaceAll("/", "");
						if (f.getName().contains(fileName)) {
							shouldBeDeleted = false; break;
						}
					}
					if (shouldBeDeleted)
						toBeDeleted.add(f);
				}
				for (File f : toBeDeleted)
					f.delete();
			}
		}
	}
	
	public static void createLJDSYDirectory() {
		File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/lesJoiesDuSysadmin/");
		if (!dir.exists())
			dir.mkdirs();
	}
	
	public static String getHtml(String gifPath) {
		String css = "html, body, #wrapper {height:100%;width: 100%;margin: 0;padding: 0;border: 0;} #wrapper td {vertical-align: middle;text-align: center;} .container{width:100%;height:100%;background-image:url('" + gifPath +"'); background-size:contain; background-repeat:no-repeat;background-position:center;}";
		//String js = "function resizeToMax(id){myImage = new Image();var img = document.getElementById(id);myImage.src = img.src;if(myImage.width / document.body.clientWidth > myImage.height / document.body.clientHeight){img.style.width = \"100%\"; } else {img.style.height = \"100%\";}}";
		//String html = "<html><head><script>" + js + "</script><style>" + css + "</style></head><body><table id=\"wrapper\"><tr><td><img id=\"gif\" src=\""+ imagePath + "\" onload=\"resizeToMax(this.id)\" /></td></tr></table></body></html>";
		String html = "<html><head><style>" + css + "</style></head><body><div class=\"container\"></div></body></html>";
		return html;
	}
	
	public static String getPref(Activity a, String key) {
		return a.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, "");
	}
	
	public static boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	public static void setPref(Activity a, String key, String value) {
		if (value.equals(""))
			removePref(a, key);
		else {
			SharedPreferences prefs = a.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}
	
	public static void removePref(Activity a, String key) {
		SharedPreferences prefs = a.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Date stringToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String dateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return formatter.format(date);
	}
	
	public static long getSecsDiff(Date first, Date latest) {
		return (latest.getTime() - first.getTime()) / 1000;
	}
	
	public static Gif getGif(List<Gif> l, String nom) {
		for (Gif g : l) {
			if (g.nom.equals(nom))
				return g;
		}
		return null;
	}
	
	public static int getGifPos(Gif gif, List<Gif> l) {
		int i = 0;
		for (Gif g : l) {
			if (g.urlGif.equals(gif.urlGif))
				return i;
			i++;
		}
		return i;
	}
	
	public static Gif getGifFromGifUrl(List<Gif> l, String u) {
		if (l != null) {
			for (Gif g : l) {
				if (g.urlGif.equals(u))
					return g;
			}
		}
		return null;
	}
	
	public static Gif getGifFromPostUrl(List<Gif> l, String u) {
		for (Gif g : l) {
			if (g.urlArticle.equals(u))
				return g;
		}
		return null;
	}
}