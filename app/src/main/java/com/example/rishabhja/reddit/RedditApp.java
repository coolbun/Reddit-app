package com.example.rishabhja.reddit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.models.UserDetails;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by rishabh.ja on 19/09/16.
 */
public class RedditApp extends Application {

    private static final String OATH_URL = "https://oauth.reddit.com";
    private static final String BASE_URL = "https://www.reddit.com";
    private static final String TAG = Application.class.getName();
    private UserDetails token;
    public boolean isLoggedin;
    private String currentUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        String registerationToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token " + registerationToken);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void saveToApplication() {
        if (getFromMemory("access_token") != null) {
            token = new UserDetails();
            token.setName(getFromMemory("userName"));
            token.setAccessToken(getFromMemory("access_token"));
            Log.e("name of user", token.getName());

            isLoggedin = true;
            currentUrl = OATH_URL;

        } else {
            token = null;
            isLoggedin = false;
            currentUrl = BASE_URL;
        }
    }

    public void storeToMemory(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getFromMemory(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString(key, null);
    }

    public UserDetails getToken() {
        return token;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

}
