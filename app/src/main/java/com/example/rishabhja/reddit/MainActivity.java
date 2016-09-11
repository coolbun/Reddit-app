package com.example.rishabhja.reddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.net.*;
import android.widget.ListView;

import com.example.rishabhja.reddit.Model.Data.Container;
import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String URL = "http://www.reddit.com/.json";
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Model.Data.Container> data;
    private Callback getPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView drawerList;
    private Runnable updateAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };
    private final String CLIENT_ID="NtJh8uzYc8BDbQ";
    private final String REDIRECT_URL="http://localhost/";
    private SQLHelper dbHelper;
    private Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();

        dbHelper = new SQLHelper(this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                data.clear();
                dbHelper.deleteAll();
                adapter.notifyDataSetChanged();
                showPosts();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        initRecyclerView();
        defineCallback();
        showPostsOffline();
    }


    private void showPostsOffline() {
        List<RedditPost> postList = dbHelper.getallPosts();
        data.clear();
        for (RedditPost post : postList) {
            data.add(new Container(post));
        }
        adapter.notifyDataSetChanged();
    }

    public void showPosts() {
        PostFetcher postFetcher = new PostFetcher(URL);
        postFetcher.setCallback(getPosts);
        postFetcher.execute();
    }

    private void initRecyclerView() {

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<Model.Data.Container>();
        adapter = new CustomAdapter(data);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMoreData();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMoreData() {
        String url = new String(URL + "?after=");
        url += data.get(data.size() - 1).getId();
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(getPosts);
        postFetcher.execute();
    }

    private void defineCallback() {
        getPosts = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request failed", "Network call exception");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                Gson gson = new Gson();
                Model model = gson.fromJson(responseData, Model.class);
                for (Model.Data.Container c : model.getChildrenList()) {
                    data.add(c);
                    dbHelper.addPost(c);
                }
                runOnUiThread(updateAdapter);
            }
        };
    }

    private void setToolbar() {
        tb = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(tb);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, tb, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_help:
                userLogin();
                return true;
        }
        return true;
    }

    private void userLogin() {
        String URL="https://www.reddit.com/api/v1/authorize?client_id="+CLIENT_ID+"&response_type=code&"+
        "state=RANDOM_STRING&redirect_uri="+REDIRECT_URL+"&duration=permanent&scope=identity";
        Intent intent=new Intent(this, LoginActivity.class);
        intent.putExtra("URL",URL);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1){
            Log.e("Received url",data.getStringExtra("URL"));
            try {
                URL urlparams=new URL(data.getStringExtra("URL"));
                Map<String,String> params=getQueryMap(urlparams.getQuery());
                if(params.containsKey("error")){
                    Log.e("Login error",params.get("error"));
                    //Generate a dialog
                }
                else{
                    getAccessToken(params);
//                    SharedPreferences sharedPreferences=this.getPreferences(Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor=sharedPreferences.edit();
//                    editor.putString("UserCode",params.get("code"));
//                    Log.e("code",params.get("code"));
//                    editor.commit();
                }
            } catch (MalformedURLException e) {
                Log.e("URL response error","Error in parsing URL received");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("URL response error","Error in receiving access token");
                e.printStackTrace();
            }


        }
    }

    private void getAccessToken(Map<String, String> params) throws IOException {
        OkHttpClient client=new OkHttpClient();
        Log.e("code",params.get("code"));
        RequestBody formBody=new FormBody.Builder()
                .add("grant_type","authorization_code")
                .add("code",params.get("code"))
                .add("redirect_uri",REDIRECT_URL)
                .build();
        String base64encodedString = Base64.encode("TutorialsPoint?java8".getBytes("utf-8"),);

        Request request=new Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error","Access token to retrieved");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("Access token",response.body().string());
            }
        });

    }

    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

}
