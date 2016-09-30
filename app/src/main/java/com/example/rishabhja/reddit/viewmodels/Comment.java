package com.example.rishabhja.reddit.viewmodels;

import android.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rishabh.ja on 16/09/16.
 */
public class Comment {
    public String htmlText;
    public String author;
    public String points;
    public String postedOn;
    public String body;
    // The 'level' field indicates how deep in the hierarchy
    // this comment is.
    public int level;
    public String name;

    public Comment(String body) {
        this.body = body;
        level = 2;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public String getAuthor() {
        return author;
    }

    public String getPostedOn() {
        return postedOn;
    }

}
