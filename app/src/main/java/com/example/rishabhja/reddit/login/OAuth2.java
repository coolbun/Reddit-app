package com.example.rishabhja.reddit.login;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.example.PostFetcher;
import com.example.models.AuthorizationToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by rishabhj on 9/12/2016.
 */
public class OAuth2 {

    private final String CLIENT_ID = "NtJh8uzYc8BDbQ";
    private final String REDIRECT_URL = "http://localhost/";


    public String oauthAuthorizationURL() {
        String URL = "https://www.reddit.com/api/v1/authorize?client_id=" + CLIENT_ID + "&response_type=code&" +
                "state=RANDOM_STRING&redirect_uri=" + REDIRECT_URL + "&duration=permanent" +
                "&scope=identity,read,flair,report,submit,mysubreddits,vote";
        return URL;
    }

    public ListenableFuture<AuthorizationToken> onResult(Intent data) {
        Log.e("Received url", data.getStringExtra("URL"));
        try {
            URL urlparams = new URL(data.getStringExtra("URL"));
            Map<String, String> params = getQueryMap(urlparams.getQuery());
            if (params.containsKey("error")) {
                Log.e("Login error", params.get("error"));
            } else {
                PostFetcher postFetcher = new PostFetcher();
                return postFetcher.getAccessToken(params);
            }
        } catch (MalformedURLException e) {
            Log.e("URL response error", "Error in parsing URL received");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("URL response error", "Error in receiving access token");
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
