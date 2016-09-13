package com.example.rishabhja.reddit;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by rishabh.ja on 13/09/16.
 */
public class ListFragment extends Fragment{

    private RecyclerView recyclerView;
    private ArrayList<Model.Data.Container> data;
    private CustomAdapter adapter;
    private Callback getPosts;
    private final String URL = "http://www.reddit.com/.json";
    private String url;
    private Runnable updateAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };
    private SQLHelper dbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean isHeader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.list_fragment,container,false);
        defineCallback();
        initRecyclerView(view);
        url=getArguments().getString("URL");
        isHeader=getArguments().getBoolean("isHeader");
        dbHelper = new SQLHelper(getActivity());

        if(isHeader==false)
            showOfflinePosts();
        else
            showPostsWithHeader(getArguments().getString("key"),getArguments().getString("value"));
        return view;
    }

    private void showPostsWithHeader(String key,String value) {
        Log.e("URL with HEAder",url);
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(getPosts);
        postFetcher.executeWithHeader(key,value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    private void initRecyclerView(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);


        data = new ArrayList<Model.Data.Container>();
        adapter = new CustomAdapter(data);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMoreData();
            }
        });
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearAdapter();
                if(isHeader==false)
                    showPosts(url);
                else
                    showPostsWithHeader(getArguments().getString("key"),getArguments().getString("value"));
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void clearAdapter() {
        data.clear();
        dbHelper.deleteAll();
        adapter.notifyDataSetChanged();
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
                getActivity().runOnUiThread(updateAdapter);
            }
        };
    }


    private void showOfflinePosts() {
        List<RedditPost> postList = dbHelper.getallPosts();
        data.clear();
        for (RedditPost post : postList) {
            data.add(new Model.Data.Container(post));
        }
        adapter.notifyDataSetChanged();
    }

    public void showPosts(String url) {
        clearAdapter();
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(getPosts);
        postFetcher.execute();
    }
}
