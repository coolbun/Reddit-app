package com.example.rishabhja.reddit;

import android.app.Fragment;
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


public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<RedditCardPost> data;
    private ListAdapter adapter;
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
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment, container, false);
        defineCallback();
        initRecyclerView(view);

        url = getArguments().getString("URL");
        isHeader = getArguments().getBoolean("isHeader");
        dbHelper = new SQLHelper(getActivity());

        if (!isHeader)
            showOfflinePosts();
        else
            showPostsWithHeader(getArguments().getString("key"), getArguments().getString("value"));
        return view;
    }

    private void showPostsWithHeader(String key, String value) {
        Log.e("URL with HEAder", url);
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(getPosts);
        postFetcher.executeWithHeader(key, value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        data = new ArrayList<RedditCardPost>();
        adapter = new ListAdapter(data);

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
                if (isHeader == false)
                    showPosts(url);
                else
                    showPostsWithHeader(getArguments().getString("key"), getArguments().getString("value"));
            }
        });
    }

    public void refreshFragment(String url){
        swipeRefreshLayout.setRefreshing(true);
        clearAdapter();
        this.url=url;
        showPosts(url);
    }

    private void clearAdapter() {
        dbHelper.deleteAll();
        data.clear();
    }

    private void loadMoreData() {
        String url = new String(URL + "?after=");
        url += data.get(data.size() - 1).id;
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
                    final RedditCardPost post = new RedditCardPost(c.getId(), c.getTitle(), c.getUrl(), c.getImgURL(), c.getThumbnail());
                    data.add(post);
                    dbHelper.addPost(post);
                }
                getActivity().runOnUiThread(updateAdapter);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
    }

    private void showOfflinePosts() {
        List<RedditCardPost> postList = dbHelper.getallPosts();
        for (RedditCardPost post : postList) {
            data.add(post);
        }
        adapter.notifyDataSetChanged();
    }

    public void showPosts(String url) {
        PostFetcher postFetcher = new PostFetcher(url);
        postFetcher.setCallback(getPosts);
        postFetcher.execute();
    }
}
