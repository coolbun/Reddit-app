package com.example.rishabhja.reddit;

import android.provider.BaseColumns;


public class RedditPost implements BaseColumns {

    String id, title, url, imgurl;
    //int PID;

    public RedditPost(String id, String title, String url, String imgurl) {
        this.id=id;
        this.title=title;
        this.url=url;
        this.imgurl=imgurl;
    }

    public RedditPost(Model.Data.Container content) {
        this.id=content.getId();
        this.title=content.getTitle();
        this.url=content.getUrl();
        this.imgurl=content.getImgURL();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getImgUrl() {
        return imgurl;
    }

    public String getId() {
        return id;
    }

}