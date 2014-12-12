package com.chteuchteu.lesjoiesdusysadmin.at;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.chteuchteu.lesjoiesdusysadmin.R;
import com.chteuchteu.lesjoiesdusysadmin.hlpr.Util;
import com.chteuchteu.lesjoiesdusysadmin.obj.Gif;
import com.chteuchteu.lesjoiesdusysadmin.ui.Activity_Main;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedParser extends AsyncTask<String, Integer, Void> {
	boolean needsUpdate = false;
	private ProgressBar pb;
	private Activity_Main activity;
	private List<Gif> gifs;
	private ArrayList<HashMap<String, String>> list;

	public FeedParser(Activity_Main activity, List<Gif> gifs, ArrayList<HashMap<String, String>> list) {
		this.activity = activity;
		this.gifs = gifs;
		this.list = list;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pb = (ProgressBar) activity.findViewById(R.id.pb);
		pb.setIndeterminate(true);
		pb.setProgress(0);
		pb.setMax(100);
		pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);

		if (activity.findViewById(R.id.first_disclaimer).getVisibility() == View.VISIBLE)
			((TextView) activity.findViewById(R.id.ascii_loading)).setText(progress[0] + "%");
		else {
			if (pb.getVisibility() == View.GONE)
				pb.setVisibility(View.VISIBLE);
			pb.setProgress(progress[0]);
			pb.setIndeterminate(false);
		}
	}

	@Override
	protected Void doInBackground(String... url) {
		List<Gif> l = new ArrayList<>(); // new gifs
		try {
			JumblrClient client = new JumblrClient("3TRQZe87tlv3jXHuF9AHtDydThIn1hDijFNLLhGEULVRRHpM3q", "4BpchUIeOkEFMAkNGiIKjpgG8sLVliKA8cgIFSa3JuQ6Ta0qNd");
			Blog blog = client.blogInfo(Activity_Main.tumblrUrl);
			int nbPosts = blog.getPostCount();
			boolean getPosts = true;
			int offset = 0;
			int progress = 0;
			while (getPosts) {
				Map<String, Object> params = new HashMap<>();
				params.put("limit", 20);
				params.put("offset", offset);
				List<Post> posts = client.blogPosts(Activity_Main.tumblrUrl, params);

				for (Post p : posts) {
					TextPost tp = (TextPost) p;
					Gif g = new Gif();
					g.date = Util.GMTDateToFrench3(tp.getDateGMT());
					g.nom = tp.getTitle();
					g.state = Gif.ST_EMPTY;
					g.urlArticle = tp.getPostUrl();
					// <p><p class="c1"><img alt="image" src="http://i.imgur.com/49DLfGd.gif"/></p>
					g.urlGif = Util.getSrcAttribute(tp.getBody());
					if (g.isValide() && Util.getGifFromGifUrl(gifs, g.urlGif) == null)
						l.add(g);
					progress++;
					int percentage = progress*100/nbPosts;
					if (percentage > 100)
						percentage = 100;
					publishProgress(percentage);
				}
				if (posts.size() > 0)
					offset += 20;
				else
					getPosts = false;
			}
		} catch (Exception ex) { ex.printStackTrace(); }

		if (l.size() == 0)
			return null;
		if (gifs == null || gifs.size() == 0 || l.size() != gifs.size() || (gifs.size() > 0 && !l.get(0).equals(gifs.get(0)))) {
			needsUpdate = true;
			for (int i=l.size()-1; i>=0; i--) {
				gifs.add(0, l.get(i));
			}
			Util.saveGifs(activity, gifs);
		}

		return null;
	}
	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(Void result) {
		pb.setVisibility(View.GONE);
		if (needsUpdate) {
			// Populate listview
			ListView l = (ListView) activity.findViewById(R.id.gifs_list);
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

			int scrollY = l.getFirstVisiblePosition();
			View v = l.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();

			SimpleAdapter sa = new SimpleAdapter(activity, list, R.layout.gifs_list, new String[] { "line1","line2" }, new int[] {R.id.line_a, R.id.line_b});
			l.setAdapter(sa);

			l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
					activity.itemClick(view);
				}
			});

			l.setSelectionFromTop(scrollY, top);

			activity.saveLastViewed();
		}
		Util.setPref(activity, "lastGifsListUpdate", Util.dateToString(new Date()));
		activity.loaded = true;
	}
}