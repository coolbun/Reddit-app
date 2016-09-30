package com.example.rishabhja.reddit.search;

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

import com.example.rishabhja.reddit.posts.ListFragment;
import com.example.rishabhja.reddit.R;

public class SearchResultsActivity extends AppCompatActivity {


    private String url;
    private String query;
    private Toolbar tb;
    private Context context;
    private final String BASE_URL = "http://www.reddit.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        context = this;
        tb = (Toolbar) findViewById(R.id.searchactivity_toolbar);
        setSupportActionBar(tb);
        initUI();

        url = getIntent().getStringExtra("url");
        query = getIntent().getStringExtra("query_text");
        url += "?q=" + query;
        setupFragment();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        updateList(BASE_URL + "/search.json?q=" + query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                }
        );
        return true;
    }

    private void setupFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = new ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        fragment.setArguments(bundle);
        transaction.add(R.id.search_fragment_container, fragment).commit();
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
