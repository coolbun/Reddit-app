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

import com.example.PostFetcher;
import com.example.models.Model;
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
    private String url;
    private SQLHelper dbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean isHeader;
    private PostFetcher postFetcher;
    private RedditApp redditApp;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment, container, false);
        defineCallback();
        initRecyclerView(view);

        url = getArguments().getString("URL");
        isHeader = getArguments().getBoolean("isHeader");
        postFetcher = new PostFetcher("");
        dbHelper = null;
        redditApp=(RedditApp) getActivity().getApplication();

        if(getActivity() instanceof MainActivity)
            dbHelper=new SQLHelper(getActivity());

        if (getActivity() instanceof SearchResultsActivity) {
            showPosts(url);
        } else if (!isHeader)
            showOfflinePosts();
        else
            showPosts(url,
                    getArguments().getString("key"),
                    getArguments().getString("value"));
        return view;
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
                    showPosts(url,
                            getArguments().getString("key"),
                            getArguments().getString("value"));
            }
        });
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
                    RedditCardPost post = new RedditCardPost(c.getId(), c.getTitle(), c.getUrl(),
                            c.getImgURL(), c.getThumbnail(), c.getCommentsUrl());
                    data.add(post);
                    if(dbHelper!=null)
                        dbHelper.addPost(post);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
    }

    private void showOfflinePosts() {
        if(dbHelper==null)
            return;
        List<RedditCardPost> postList = dbHelper.getallPosts();
        Log.e("size", String.valueOf(postList.size()));
        for (RedditCardPost post : postList) {
            data.add(post);
        }
        adapter.notifyDataSetChanged();
    }

    public void showPosts(String url, String... headers) {
        postFetcher.setURL(url);
        postFetcher.setCallback(getPosts);
        postFetcher.execute(headers);
    }

    public void refreshFragment(String url) {
        swipeRefreshLayout.setRefreshing(true);
        clearAdapter();
        this.url = url;
        showPosts(url);
    }

    private void clearAdapter() {
        if(dbHelper!=null)
            dbHelper.deleteAll();
        data.clear();
    }

    private void loadMoreData() {
        String url = new String(redditApp.getToken().getUrl() + "?after=");
        Log.e("Load more",url);
        url += data.get(data.size() - 1).id;
        postFetcher.setURL(url);
        postFetcher.setCallback(getPosts);

        if(redditApp.getToken().getHeaderKey()!=null)
            postFetcher.execute(redditApp.getToken().getHeaderKey(),redditApp.getToken().getHeaderValue());
        else
            postFetcher.execute();
    }
}
