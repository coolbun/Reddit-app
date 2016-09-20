package com.example.rishabhja.reddit;

import android.app.Application;

import com.example.models.UserDetails;

/**
 * Created by rishabh.ja on 19/09/16.
 */
public class RedditApp extends Application {

    private UserDetails token;
    public boolean isLoggedin;
    private String currentUrl;


    public UserDetails getToken() {
        return token;
    }

    public void setToken(UserDetails token) {
        this.token = token;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }
}
