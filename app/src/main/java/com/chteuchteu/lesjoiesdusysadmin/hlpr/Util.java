package com.chteuchteu.lesjoiesdusysadmin.hlpr;

import android.annotation.SuppressLint;
import android.util.Log;

import com.chteuchteu.gifapplicationlibrary.obj.Gif;

import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class Util {
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
			// 2014-01-09 16:57:58 GMT
			//SimpleDateFormat dfGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			SimpleDateFormat dfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH);
			Log.v("gmtDate", gmtDate);
			dfGMT.parse(gmtDate);
			SimpleDateFormat dfFrench = new SimpleDateFormat("d/MM", Locale.FRANCE);
			return dfFrench.format(dfGMT.getCalendar().getTime());
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	public static Gif getGifFromGifUrl(List<Gif> l, String u) {
		if (l != null) {
			for (Gif g : l) {
				if (g.getGifUrl().equals(u))
					return g;
			}
		}
		return null;
	}
}
