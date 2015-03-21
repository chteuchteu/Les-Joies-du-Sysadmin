package com.chteuchteu.lesjoiesdusysadmin.hlpr;

import com.chteuchteu.gifapplicationlibrary.async.DataSourceParser;
import com.chteuchteu.gifapplicationlibrary.obj.Gif;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeedParser {
    public static List<Gif> parseFeed(String tumblrUrl, DataSourceParser thread) {
		List<Gif> l = new ArrayList<>();
		try {
			JumblrClient client = new JumblrClient("3TRQZe87tlv3jXHuF9AHtDydThIn1hDijFNLLhGEULVRRHpM3q", "4BpchUIeOkEFMAkNGiIKjpgG8sLVliKA8cgIFSa3JuQ6Ta0qNd");
			Blog blog = client.blogInfo(tumblrUrl);
			int nbPosts = blog.getPostCount();
			boolean getPosts = true;
			int offset = 0;
			int progress = 0;
			while (getPosts) {
				Map<String, Object> params = new HashMap<>();
				params.put("limit", 20);
				params.put("offset", offset);
				List<Post> posts = client.blogPosts(tumblrUrl, params);

				for (Post p : posts) {
					TextPost tp = (TextPost) p;
					Gif gif = new Gif();
                    gif.setDate(parseDate(tp.getDateGMT()));
					gif.setName(tp.getTitle());
					gif.setArticleUrl(tp.getPostUrl());
					// <p><p class="c1"><img alt="image" src="http://i.imgur.com/49DLfGd.gif"/></p>
					gif.setGifUrl(getSrcAttribute(tp.getBody()));
					if (gif.isValid() && getGifFromGifUrl(l, gif.getGifUrl()) == null)
						l.add(gif);
					progress++;
					int percentage = progress*100/nbPosts;
                    if (thread != null)
					    thread.manualPublishProgress(percentage > 100 ? 100 : percentage);
				}
				if (posts.size() > 0)
					offset += 20;
				else
					getPosts = false;
			}
		} catch (Exception ex) { ex.printStackTrace(); }

        return l;
	}

    public static String getSrcAttribute(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        org.jsoup.nodes.Element img = doc.select("img[src]").first();
        if (img != null)
            return img.attr("src");
        return "";
    }

    public static Calendar parseDate(String gmtDate) {
        try {
            // 2012-06-18 08:47:37 GMT
            // 2014-01-09 16:57:58 GMT
            SimpleDateFormat dfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH);
            dfGMT.parse(gmtDate);
	        return dfGMT.getCalendar();
        } catch (ParseException ex) {
            ex.printStackTrace();
	        return null;
        }
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
