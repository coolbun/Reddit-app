package com.example.rishabhja.reddit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rishabhja.reddit.Model.Data.Container;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements OAuth2.ActivityforResultHandler, NavigationView.OnNavigationItemSelectedListener {

    private static final String OATH_URL = "https://oauth.reddit.com";
    private OAuth2 getAccessToken;
    private final String URL = "http://www.reddit.com/.json";
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Model.Data.Container> data;
    private Callback getPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView mDrawerList;
    private Context context;
    private ProgressBar progressBar;
    private Runnable updateAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };
    private final int LOGIN_REQUEST_CODE = 1;
    private SQLHelper dbHelper;
    private Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new SQLHelper(this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        tb = (Toolbar) findViewById(R.id.main_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        context = this;

        setStausBarcolor();
        setSupportActionBar(tb);
        initRecyclerView();
        defineCallback();
        showPostsOffline();
        setDrawer();
        setOnLoginClickListener();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearAdapter();
                showPosts();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void updateUserInfo() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_navigation);
        final TextView loginButton = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loginButton);

        final PostFetcher getUsername = new PostFetcher(OATH_URL+"/api/v1/me/.json");
        getUsername.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Gson gson=new Gson();
                final UserDetails userDetails=gson.fromJson(response.body().string(),UserDetails.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setText(userDetails.getName());
                    }
                });
            }
        });
        Log.e("Access_token",getFromMemory("access_token"));
        getUsername.executeWithHeader("Authorization","bearer "+getFromMemory("access_token"));
    }


    private void clearAdapter() {
        data.clear();
        dbHelper.deleteAll();
        adapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStausBarcolor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cardview_dark_background));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setOnLoginClickListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_navigation);
        TextView loginButton = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            DrawerLayout mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            @Override
            public void onClick(View v) {
                getAccessToken = new OAuth2((Activity) context);
                if (mdrawer.isDrawerOpen(GravityCompat.START))
                    mdrawer.closeDrawer(GravityCompat.START);
                getAccessToken.oauthAuthorization();
            }
        });
    }

    private void setDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.main_navigation);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            //clear adapter and start progress bar
            clearAdapter();
            progressBar.setVisibility(View.VISIBLE);
            getAccessToken.onResult(data);
        }
    }

    @Override
    public void istartActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }


    @Override
    public void iStoreToMemory(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getFromMemory(String key) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
