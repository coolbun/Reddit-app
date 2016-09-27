package com.example.rishabhja.reddit;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.example.PostFetcher;
import com.google.common.util.concurrent.ListenableFuture;

public class DisplaySubRedditActivity extends AppCompatActivity {


    private String url;
    private String query;
    private Toolbar tb;
    private Context context;
    private final String BASE_URL = "http://www.reddit.com";
    private RedditApp redditApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sub_reddit);

        context = this;
        tb = (Toolbar) findViewById(R.id.subreddit_toolbar);
        setSupportActionBar(tb);
        initUI();

        String subreddit = getIntent().getStringExtra("url");
        redditApp = (RedditApp) getApplication();
        url = redditApp.getCurrentUrl() + "/" + subreddit.toLowerCase() + "/.json";
        setupFragment();
    }

    private void setupFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = new com.example.rishabhja.reddit.ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        fragment.setArguments(bundle);
        transaction.add(R.id.search_fragment_container, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_displaysubreddit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            /*case R.id.subscribe_subreddit:
                PostFetcher postFetcher=new PostFetcher();
                if(redditApp.isLoggedin) {
                    postFetcher.setURL("");
                }*/
        }
        return super.onOptionsItemSelected(item);
    }


    private void initUI() {
        setStausBarcolor();
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStausBarcolor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cardview_dark_background));
    }

    private void updateList(String url) {
        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentById(R.id.search_fragment_container);
        fragment.refreshFragment(url);
    }

}