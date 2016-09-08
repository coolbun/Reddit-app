package com.example.rishabhja.reddit;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListView;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.example.rishabhja.reddit.Model.Data.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static RecyclerView.Adapter adapter;
    private static RecyclerView recyclerView;
    private static ArrayList<Model.Data.Container> data;
    private final String URL = "http://www.reddit.com/.json";
    private Callback getPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView drawerList;
    private Runnable updateAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };
    private SQLHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper=new SQLHelper(this);

        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swiperefresh);
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
        List<RedditPost> postList= dbHelper.getallPosts();
        data.clear();
        for(RedditPost post:postList){
            data.add(new Container(post));
        }
        adapter.notifyDataSetChanged();
    }


    public void clearDatabase(){
        dbHelper.deleteAll();
    }

    public void showPosts(){
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
                Log.e("Request failed","Network call exception");
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
