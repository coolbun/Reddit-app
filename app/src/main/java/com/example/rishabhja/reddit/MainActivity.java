package com.example.rishabhja.reddit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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

import com.example.PostFetcher;
import com.example.models.AuthorizationToken;
import com.example.models.SubRedditModel;
import com.example.models.UserDetails;
import com.example.rishabhja.reddit.databinding.NavHeaderMainBinding;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String OATH_URL = "https://oauth.reddit.com";
    private static final String BASE_URL = "https://www.reddit.com";
    private OAuth2 getAccessToken;
    private ProgressBar progressBar;
    private final int LOGIN_REQUEST_CODE = 1;
    private Toolbar tb;
    private NavigationView navigationView;
    private PostFetcher fetchFromURL;
    private UserDetails userDetails;
    private UserViewModel userViewModel;
    private RedditApp applicationContent;
    private NavHeaderMainBinding bind;
    private Context context;
    private DrawerLayout mdrawer;
    private ArrayList<String> sortTypes;
    private final int FRONT_INDEX = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fetchFromURL = new PostFetcher();
        navigationView = (NavigationView) findViewById(R.id.main_navigation);
        userViewModel = new UserViewModel();
        context = this;
        mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        tb = (Toolbar) findViewById(R.id.main_toolbar);
        applicationContent = (RedditApp) getApplication();

        initUI();
        saveToApplication();
        setSortTypes();
        setNavDrawer();
        setClickListeners();
    }

    private void setSortTypes() {
        sortTypes = new ArrayList<>();
        sortTypes.add("Front");
        sortTypes.add("Hot");
        sortTypes.add("New");
        sortTypes.add("Rising");
        sortTypes.add("Controversial");
        sortTypes.add("Top");
    }

    private void saveToApplication() {
        if (getFromMemory("access_token") != null) {
            userDetails = new UserDetails();
            userDetails.setName(getFromMemory("userName"));
            userDetails.setAccessToken(getFromMemory("access_token"));

            applicationContent.isLoggedin = true;
            applicationContent.setToken(userDetails);
            applicationContent.setCurrentUrl(OATH_URL);

            //Binding updates
            userViewModel.name.set(userDetails.getName());
            userViewModel.logout.set("Logout");
        } else {
            userDetails = null;

            applicationContent.isLoggedin = false;
            applicationContent.setToken(null);
            applicationContent.setCurrentUrl(BASE_URL);

            //Binding updates
            userViewModel.name.set("Login");
            userViewModel.logout.set("");
        }
    }

    private void initUI() {
        setStausBarcolor();
        setSupportActionBar(tb);
        initDrawer();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = new com.example.rishabhja.reddit.ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", BASE_URL + "/.json");
        fragment.setArguments(bundle);
        transaction.add(R.id.fragment_container, fragment).commit();
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

    private void setClickListeners() {

        //on Login Click
        TextView loginButton = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdrawer.closeDrawers();
                getAccessToken = new OAuth2();
                String url = getAccessToken.oauthAuthorizationURL();
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                intent.putExtra("URL", url);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            }
        });

        //on Logout Click
        TextView logoutButton = (TextView) navigationView.getHeaderView(0).findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdrawer.closeDrawers();
                storeToMemory("access_token", null);
                saveToApplication();
                updateList(applicationContent.getCurrentUrl() + "/.json");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == 1) {
            progressBar.setVisibility(View.VISIBLE);
            ListenableFuture<AuthorizationToken> listenableFuture = getAccessToken.onResult(data);

            Futures.addCallback(listenableFuture, new FutureCallback<AuthorizationToken>() {
                @Override
                public void onSuccess(AuthorizationToken authorizationToken) {
                    storeToMemory("access_token", "bearer " + authorizationToken.getAccess_token());
                    PostFetcher networkCaller = new PostFetcher();
                    Log.e("Access_token", getFromMemory("access_token"));
                    ListenableFuture<UserDetails> future = networkCaller.getUser(OATH_URL + "/api/v1/me/.json",
                            getFromMemory("access_token"));

                    Futures.addCallback(future, new FutureCallback<UserDetails>() {
                        @Override
                        public void onSuccess(final UserDetails user) {
                            storeToMemory("userName", user.getName());
                            updateOnLogin(user);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                        }
                    });
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e("Failed in Login token", "");
                }
            });
        }
    }

    private void initDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        bind = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.nav_header_main, null, false);

        userViewModel.name.set("Login");
        userViewModel.logout.set("");
        bind.setUser(userViewModel);
        navigationView.addHeaderView(bind.getRoot());
    }

    /*
        Add subreddits to the navigation
        drawer
     */
    public void setNavDrawer() {
        String subRedditsurl = applicationContent.getCurrentUrl() + "/subreddits/.json";
        final Menu menu = navigationView.getMenu();

        //Clear the current menu
        menu.clear();

        SubMenu subMenu = menu.addSubMenu("Sort by : ");
        for (String sortTitle : sortTypes)
            subMenu.add(sortTitle);

        final SubMenu submenu = menu.addSubMenu("Reddit Picks");
        String accessToken = null;
        if (userDetails != null)
            accessToken = userDetails.getAccessToken();

        ListenableFuture<SubRedditModel> modelFuture = fetchFromURL.getSubRedditModel(
                subRedditsurl,
                accessToken);
        Futures.addCallback(modelFuture, new FutureCallback<SubRedditModel>() {
            @Override
            public void onSuccess(final SubRedditModel model) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<SubRedditModel.Data.Container> subreddits = model.getChildrenList();
                        for (final SubRedditModel.Data.Container subreddit : subreddits) {
                            submenu.add("r/" + subreddit.getTitle());
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    private void updateOnLogin(final UserDetails user) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //refreshAdapter(OATH_URL + "/.json");
                progressBar.setVisibility(View.GONE);
                saveToApplication();
                setNavDrawer();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mdrawer.isDrawerOpen(GravityCompat.START)) {
            mdrawer.closeDrawers();
        } else
            super.onBackPressed();
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
                        Intent intent = new Intent(context, SearchResultsActivity.class);
                        intent.putExtra("url", BASE_URL + "/search.json");
                        intent.putExtra("query_text", query);
                        startActivity(intent);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mdrawer.isDrawerOpen(GravityCompat.START))
            mdrawer.closeDrawer(GravityCompat.START);

        if (item.getTitle().equals(sortTypes.get(FRONT_INDEX)))
            updateList(BASE_URL + "/.json");
        else if (sortTypes.contains(item.getTitle()))
            updateList(BASE_URL + "/" + new String((String) item.getTitle()).toLowerCase() + "/.json");
        else
            updateList(BASE_URL + "/" + item.getTitle() + "/.json");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_post:
                if (applicationContent.isLoggedin == false) {
                    InvalidAccessDialog dialog = new InvalidAccessDialog();
                    dialog.show(getFragmentManager(), "invalid access");
                } else {
                    //Intent intent = new Intent(context, );
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void storeToMemory(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getFromMemory(String key) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}
