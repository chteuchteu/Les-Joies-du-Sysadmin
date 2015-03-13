package com.chteuchteu.lesjoiesdusysadmin;

import android.content.Context;

import com.chteuchteu.gifapplicationlibrary.i.IDataSourceParser;
import com.chteuchteu.gifapplicationlibrary.obj.Gif;
import com.chteuchteu.gifapplicationlibrary.obj.GifApplicationBundle;
import com.chteuchteu.lesjoiesdusysadmin.hlpr.FeedParser;
import com.chteuchteu.lesjoiesdusysadmin.serv.NotificationService;
import com.chteuchteu.lesjoiesdusysadmin.ui.Activity_Main;

import java.util.List;

public class GifFoo {
    public static GifApplicationBundle getApplicationBundle(Context context) {
        return new GifApplicationBundle(
                context.getString(R.string.app_name),
                "lesjoiesdusysadmin.tumblr.com",
                new IDataSourceParser() {
                    @Override
                    public List<Gif> parseDataSource(String dataSourceUrl) {
                        return FeedParser.parseFeed(dataSourceUrl, null);
                    }
                },
                "lesJoiesDuSysadmin",
                context.getString(R.string.about),
                Activity_Main.class,
                NotificationService.class
        );
    }
}
