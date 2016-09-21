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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
    private SQLHelper dbHelper = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean isOffline;
    private PostFetcher postFetcher;
    private RedditApp redditApp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment, container, false);
        defineCallback();
        initRecyclerView(view);

        url = getArguments().getString("URL");
        isOffline = getArguments().getBoolean("isOffline");

        postFetcher = new PostFetcher();
        redditApp = (RedditApp) getActivity().getApplication();

        dbHelper = new SQLHelper(getActivity());

        Log.e("here", String.valueOf(isOffline));
        if (getActivity() instanceof MainActivity) ;
        else
            dbHelper = null;

        if (getActivity() instanceof SearchResultsActivity) {
            showPosts(url);
        } else if (isOffline == true) {
            Log.e("isffline", "true");
            showPostFromDB();
        } else {
            if (redditApp.isLoggedin)
                showPosts(url,
                        redditApp.getToken().getAccessToken());
            else
                showPosts(url);
        }
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

        adapter = new ListAdapter(data, new ListAdapter.NotificationsInterface() {
            @Override
            public void notifyDeletion(RedditCardPost post) {
                if (redditApp.isLoggedin) {
                    Log.e("ID", post.id);
                    ListenableFuture<Boolean> future = postFetcher.deletePost(post.id,
                            redditApp.getToken().getAccessToken());
                }
            }

            @Override
            public boolean notifyVote(RedditCardPost post,int dir) {
                if (redditApp.isLoggedin) {
                    Log.e("ID", post.id);
                    ListenableFuture<Boolean> future = postFetcher.votePost(post.id,
                            redditApp.getToken().getAccessToken(),dir);
                    return true;
                }
                else{
                    InvalidAccessDialog dialog=new InvalidAccessDialog();
                    dialog.show(getFragmentManager(), "invalid access");
                    return false;
                }
            }
        });
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
                if (redditApp.isLoggedin == false)
                    showPosts(url);
                else {
                    showPosts(url,
                            redditApp.getToken().getAccessToken());
                }
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
                Log.e("response", responseData);
                Gson gson = new Gson();
                Model model = gson.fromJson(responseData, Model.class);
                for (Model.Data.Container c : model.getChildrenList()) {
                    String commentURL = c.getCommentsUrl();
                    if (commentURL.endsWith("?ref=search_posts"))
                        commentURL = commentURL.substring(0, commentURL.length() - 17);
                    c.setCommentURL(commentURL);
                    RedditCardPost post = new RedditCardPost(c.getId(), c.getTitle(), c.getUrl(),
                            c.getImgURL(), c.getThumbnail(), c.getCommentsUrl(), c.getNum_comments(),
                            c.getUpvotes());
                    data.add(post);
                    if (dbHelper != null)
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

    public void refreshFragment(String url) {
        swipeRefreshLayout.setRefreshing(true);
        clearAdapter();

        //change the url of the fragment container
        this.url = url;

        if (redditApp.isLoggedin)
            showPosts(url, redditApp.getToken().getAccessToken());
        else
            showPosts(url);
    }

    private void clearAdapter() {
        if (dbHelper != null)
            dbHelper.deleteAll();
        data.clear();
    }

    private void loadMoreData() {
        String url = this.url + "?after=";
        url += data.get(data.size() - 1).id;
        Log.e("URL for load more", url);
        postFetcher.setURL(url);
        postFetcher.setCallback(getPosts);

        if (redditApp.isLoggedin == true) {
            Log.e("TOKEN", redditApp.getToken().getAccessToken());
            postFetcher.execute(redditApp.getToken().getAccessToken());
        } else
            postFetcher.execute();
    }

    private void showPostFromDB() {
        if (dbHelper == null)
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
}
