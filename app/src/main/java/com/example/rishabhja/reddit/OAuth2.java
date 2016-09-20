package com.example.rishabhja.reddit;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rishabhj on 9/12/2016.
 */
public class OAuth2 {

    private final String CLIENT_ID = "NtJh8uzYc8BDbQ";
    private final String REDIRECT_URL = "http://localhost/";

    public interface ActivityforResultHandler {
        void updateUserInfo();

        void iStoreToMemory(String key, String value);
    }

    public String oauthAuthorization() {
        String URL = "https://www.reddit.com/api/v1/authorize?client_id=" + CLIENT_ID + "&response_type=code&" +
                "state=RANDOM_STRING&redirect_uri=" + REDIRECT_URL + "&duration=permanent" +
                "&scope=identity,read,flair,report,submit";
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

    private void getAccessToken(Map<String, String> params, Callback callback) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Log.e("code", params.get("code"));
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", params.get("code"))
                .add("redirect_uri", REDIRECT_URL)
                .build();

        String basicOauth = Base64.encodeToString((CLIENT_ID + ":").getBytes(), 0, (CLIENT_ID + ":").length(), Base64.DEFAULT);
        basicOauth = basicOauth.substring(0, basicOauth.length() - 1);
        basicOauth = "Basic " + basicOauth;

        Request request = new Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
                .addHeader("Authorization", basicOauth)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(callback);
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
