package com.example.rishabhja.reddit;

import android.app.Application;

import com.example.models.UserDetails;

/**
 * Created by rishabh.ja on 19/09/16.
 */
public class RedditApp extends Application {

    private UserDetails token;

    public UserDetails getToken() {
        return token;
    }

    public void setToken(UserDetails token) {
        this.token = token;
    }
}
