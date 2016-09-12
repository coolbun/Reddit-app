package com.example.rishabhja.reddit;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostFetcher {

    private String url;
    private Callback callback;

    PostFetcher(String url) {
        this.url = url;
    }

    public void setCallback(Callback c){
        callback=c;
    }

    public void execute() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void executeWithHeader(String key,String value) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader(key,value)
                .build();
        client.newCall(request).enqueue(callback);
    }
}