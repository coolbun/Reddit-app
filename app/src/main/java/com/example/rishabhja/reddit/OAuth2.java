package com.example.rishabhja.reddit;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

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
    private final int LOGIN_REQUEST_CODE = 1;
    private final String REDIRECT_URL = "http://localhost/";
    private final Intent callingintent;

    public interface ActivityforResultHandler {
        void istartActivityForResult(Intent intent, int requestCode);

        void updateUserInfo();

        void iStoreToMemory(String key, String value);
    }

    ActivityforResultHandler activityHandler;

    OAuth2(Activity activity) {
        activityHandler = (ActivityforResultHandler) activity;
        callingintent = new Intent(activity, LoginActivity.class);
    }

    public void oauthAuthorization() {
        String URL = "https://www.reddit.com/api/v1/authorize?client_id=" + CLIENT_ID + "&response_type=code&" +
                "state=RANDOM_STRING&redirect_uri=" + REDIRECT_URL + "&duration=permanent&scope=identity";
        callingintent.putExtra("URL", URL);
        activityHandler.istartActivityForResult(callingintent, LOGIN_REQUEST_CODE);
    }

    public void onResult(Intent data) {
        Log.e("Received url", data.getStringExtra("URL"));
        try {
            URL urlparams = new URL(data.getStringExtra("URL"));
            Map<String, String> params = getQueryMap(urlparams.getQuery());
            if (params.containsKey("error")) {
                Log.e("Login error", params.get("error"));
            } else {
                getAccessToken(params);
            }
        } catch (MalformedURLException e) {
            Log.e("URL response error", "Error in parsing URL received");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("URL response error", "Error in receiving access token");
            e.printStackTrace();
        }
    }

    private void getAccessToken(Map<String, String> params) throws IOException {
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Access token to retrieved");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.e("Access token", response.body().string());
                final String responseData=response.body().string();
                Gson gson = new Gson();
                AuthorizationToken authorizationToken = gson.fromJson(responseData,
                        AuthorizationToken.class);
                activityHandler.iStoreToMemory("access_token", authorizationToken.getAccess_token());
                activityHandler.iStoreToMemory("refresh_token", authorizationToken.getRefresh_token());
                activityHandler.iStoreToMemory("expires_in", authorizationToken.getExpires());
                activityHandler.updateUserInfo();
            }
        });
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
