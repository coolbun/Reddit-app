package com.example.rishabhja.reddit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rishabhja.reddit.databinding.NavHeaderMainBinding;
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
    private static final String BASE_URL = "https://www.reddit.com";
    private OAuth2 getAccessToken;
    private Context context;
    private ProgressBar progressBar;
    private final int LOGIN_REQUEST_CODE = 1;
    private Toolbar tb;
    private NavigationView navigationView;
    private PostFetcher fetchFromURL;
    public final ObservableField<UserDetails> userDetails = new ObservableField<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = (Toolbar) findViewById(R.id.main_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        context = this;
        fetchFromURL = new PostFetcher("");
        navigationView = (NavigationView) findViewById(R.id.main_navigation);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = new com.example.rishabhja.reddit.ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", BASE_URL + "/.json");
        fragment.setArguments(bundle);
        transaction.add(R.id.fragment_container, fragment).commit();

        setStausBarcolor();
        setSupportActionBar(tb);
        setDrawer();
        setNavDrawer(BASE_URL + "/subreddits/.json");
        setOnLoginClickListener();
    }

    /*
        Updates main activity after
        user has logged in
     */
    @Override
    public void updateUserInfo() {
        PostFetcher getUsername = new PostFetcher(OATH_URL + "/api/v1/me/.json");
        getUsername.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Gson gson = new Gson();
                String repsonseString = response.body().string();
                final UserDetails user = gson.fromJson(repsonseString, UserDetails.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userDetails.set(user);
                        refreshAdapter(OATH_URL + "/.json");
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
        Log.e("Access_token", getFromMemory("access_token"));
        getUsername.executeWithHeader("Authorization", "bearer " + getFromMemory("access_token"));
    }

    private void refreshAdapter(String url) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = new com.example.rishabhja.reddit.ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        bundle.putBoolean("isHeader", true);
        bundle.putString("key", "Authorization");
        bundle.putString("value", "bearer " + getFromMemory("access_token"));
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment).commit();
    }

    private void updateList(String url) {
        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.refreshFragment(url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStausBarcolor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cardview_dark_background));
    }

    /*
        Retrieves the Oauth access
        token from the server
     */
    private void setOnLoginClickListener() {
        TextView loginButton = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            DrawerLayout mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

            @Override
            public void onClick(View v) {
                getAccessToken = new OAuth2((Activity) context);
                if (mdrawer.isDrawerOpen(GravityCompat.START))
                    mdrawer.closeDrawer(GravityCompat.START);

                String url = getAccessToken.oauthAuthorization();
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("URL", url);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            }
        });
    }

    private void setDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        NavHeaderMainBinding bind = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.nav_header_main, null, false);
        userDetails.set(new UserDetails("Name"));
        bind.setUser(userDetails.get());
        navigationView.addHeaderView(bind.getRoot());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            progressBar.setVisibility(View.VISIBLE);

            getAccessToken.onResult(data, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Error", "Access token to retrieved");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseData = response.body().string();
                    Gson gson = new Gson();
                    AuthorizationToken authorizationToken = gson.fromJson(responseData,
                            AuthorizationToken.class);
                    iStoreToMemory("access_token", authorizationToken.getAccess_token());
                    iStoreToMemory("refresh_token", authorizationToken.getRefresh_token());
                    iStoreToMemory("expires_in", authorizationToken.getExpires());
                    updateUserInfo();
                }
            });
        }
    }


    /*
        Add subreddits to the navigation
        drawer
     */
    public void setNavDrawer(String url) {
        final Menu menu = navigationView.getMenu();
        fetchFromURL.setURL(url);
        fetchFromURL.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Naviagtion Drawer Error", "Subreddits fetch failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    String responseData = response.body().string();
                    @Override
                    public void run() {
                        final SubMenu submenu = menu.addSubMenu("Reddit Picks");
                        Gson gson = new Gson();
                        SubRedditModel model = gson.fromJson(responseData, SubRedditModel.class);
                        ArrayList<SubRedditModel.Data.Container> subreddits = model.getChildrenList();
                        for (final SubRedditModel.Data.Container subreddit : subreddits) {
                            MenuItem item = submenu.add("r/" + subreddit.getTitle());
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    updateList(BASE_URL + "/r/" + subreddit.getTitle() + "/.json");
                                    return true;
                                }
                            });
                        }
                    }
                });
            }
        });
        fetchFromURL.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
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
        return sharedPreferences.getString(key, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
