package com.example;

import android.util.Base64;
import android.util.Log;

import com.example.models.AuthorizationToken;
import com.example.models.CaptchaResponse;
import com.example.models.Model;
import com.example.models.SubRedditModel;
import com.example.models.UserDetails;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PostFetcher {

    public static final String TAG = PostFetcher.class.getName();
    private String url;
    private Callback callback;
    private Request.Builder builder;
    private OkHttpClient client;
    private RequestBody params;
    private final String CLIENT_ID = "NtJh8uzYc8BDbQ";
    private final String REDIRECT_URL = "http://localhost/";


    public PostFetcher(String url) {

        this.url = url;
        client = new OkHttpClient();
    }

    public PostFetcher() {
        client = new OkHttpClient();
    }

    public void setCallback(Callback c) {
        callback = c;
    }

    public void setURL(String url) {
        this.url = url;
    }

    //Recieves key,value pairs and adds the header to the request
    public void execute(String... args) {
        builder = new Request.Builder()
                .url(url);
        Request request;
        int len = args.length;
        if (len > 0 && args[0] != null) {
            for (int i = 0; i < len; i++) {
                builder.addHeader("Authorization", args[i]);
            }
        }
        if (params != null)
            request = builder.post(params).build();
        else
            request = builder.build();
        client.newCall(request).enqueue(callback);
    }

    public void setFormValues(String... args) {
        params = null;
        int len = args.length;
        if (len > 0) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (int i = 0; i < len; i += 2) {
                formBuilder.add(args[i], args[i + 1]);
            }
            params = formBuilder.build();
        }
    }

    public ListenableFuture<SubRedditModel> getSubRedditModel(String subRedditsurl, String... headers) {

        final SettableFuture<SubRedditModel> future = SettableFuture.create();
        PostFetcher postFetcher = new PostFetcher();
        postFetcher.setURL(subRedditsurl);
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Naviagtion Drawer Error", "Subreddits fetch failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                Gson gson = new Gson();
                SubRedditModel model = gson.fromJson(responseData, SubRedditModel.class);
                future.set(model);
            }
        });
        if (headers.length > 0 && headers[0] != null)
            postFetcher.execute(headers);
        else
            postFetcher.execute();
        return future;
    }

    public ListenableFuture<AuthorizationToken> getAccessToken(Map<String, String> params) throws IOException {

        final SettableFuture<AuthorizationToken> future = SettableFuture.create();
        OkHttpClient client = new OkHttpClient();
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
                Log.e(PostFetcher.class.getName(), e.toString());
                future.setException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                Gson gson = new Gson();
                AuthorizationToken authorizationToken = gson.fromJson(responseData,
                        AuthorizationToken.class);
                future.set(authorizationToken);
            }
        });
        return future;
    }

    public ListenableFuture<UserDetails> getUser(String url, String... args) {
        final SettableFuture<UserDetails> future = SettableFuture.create();

        PostFetcher networkCaller = new PostFetcher();
        networkCaller.setURL(url);
        networkCaller.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                String repsonseString = response.body().string();
                final UserDetails user = gson.fromJson(repsonseString, UserDetails.class);
                future.set(user);
            }
        });
        networkCaller.execute(args);
        return future;
    }

    public ListenableFuture<String> fetchComments() {
        final SettableFuture future = SettableFuture.create();
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String comments = response.body().string();
                    future.set(comments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        postFetcher.execute();
        return future;
    }

    public ListenableFuture<String> getCaptchaURL(String url, String... headers) {
        final SettableFuture<String> settableFuture = SettableFuture.create();

        PostFetcher fetchCaptchaIden = new PostFetcher();
        fetchCaptchaIden.setURL(url);
        fetchCaptchaIden.setFormValues("api_type", "json");
        fetchCaptchaIden.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(PostFetcher.class.getName(), "Captcha fetch failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                String responseData = response.body().string();

                try {
                    JSONObject data = new JSONObject(responseData);
                    String idenJson = data.getJSONObject("json").toString();
                    Log.e("IDENJSON", idenJson);
                    CaptchaResponse captcha = gson.fromJson(idenJson, CaptchaResponse.class);
                    Log.e("Captcha iden resp", responseData);
                    settableFuture.set("https://www.reddit.com/captcha/iden?iden=" + captcha.getIden());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        fetchCaptchaIden.execute(headers);
        return settableFuture;
    }

    public ListenableFuture<Boolean> deletePost(String id, String... headers) {
        final SettableFuture<Boolean> future = SettableFuture.create();
        PostFetcher postFetcher = new PostFetcher();
        postFetcher.setURL("https://oauth.reddit.com/api/hide");
        postFetcher.setFormValues("id", id);
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(PostFetcher.class.getName(), "Hide post failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("Response on hide", response.body().string());
                future.set(new Boolean(true));
            }
        });
        postFetcher.execute(headers);
        return future;
    }

    public ListenableFuture<Boolean> votePost(String id, String accessToken, int dir) {
        final SettableFuture<Boolean> future = SettableFuture.create();
        PostFetcher postFetcher = new PostFetcher();
        postFetcher.setURL("https://oauth.reddit.com/api/vote");
        String dirString = String.valueOf(dir);
        postFetcher.setFormValues("dir", dirString,
                "id", id,
                "rank", "2");
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.setException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "response code: " + response.code());
                Log.e("Response on vote", response.body().string());
                future.set(Boolean.TRUE);
            }
        });
        postFetcher.execute(accessToken);
        return future;
    }

    public ListenableFuture<Model> getPosts(String url, String... headers) {
        PostFetcher postFetch = new PostFetcher();
        postFetch.setURL(url);
        final SettableFuture future = SettableFuture.create();

        postFetch.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.setException(e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                Gson gson = new Gson();
                Model model = gson.fromJson(responseData, Model.class);
                future.set(model);
            }
        });
        postFetch.execute(headers);
        return future;
    }

    public ListenableFuture<Model> fetchUserSubreddits(String url, String ...headers) {
        PostFetcher postFetch = new PostFetcher();
        postFetch.setURL(url);
        final SettableFuture future = SettableFuture.create();

        postFetch.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.setException(e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                Log.e("my subreddit",responseData);
                Gson gson = new Gson();
                Model model = gson.fromJson(responseData, Model.class);
                future.set(model);
            }
        });
        postFetch.execute(headers);
        return future;
    }
}