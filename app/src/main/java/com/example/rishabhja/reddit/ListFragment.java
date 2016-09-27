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
import android.widget.Toast;

import com.example.PostFetcher;
import com.example.models.Model;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;


public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<RedditCardPost> data;
    private ListAdapter adapter;
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
        initRecyclerView(view);

        url = getArguments().getString("URL");
        isOffline = getArguments().getBoolean("isOffline");
        postFetcher = new PostFetcher();
        redditApp = (RedditApp) getActivity().getApplication();

        if (getActivity() instanceof MainActivity)
            dbHelper = new SQLHelper(getActivity());
        else
            dbHelper = null;

        if (getActivity() instanceof SearchResultsActivity)
            showPosts(url);
        else if (isOffline == true)
            showPostFromDB();
        else {
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
                    ListenableFuture<Boolean> future = postFetcher.deletePost(post.id,
                            redditApp.getToken().getAccessToken());
                }
            }

            @Override
            public boolean notifyVote(RedditCardPost post, int dir) {
                if (redditApp.isLoggedin) {
                    ListenableFuture<Boolean> future = postFetcher.votePost(post.id,
                            redditApp.getToken().getAccessToken(), dir);
                    return true;
                } else {
                    InvalidAccessDialog dialog = new InvalidAccessDialog();
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
                    Log.e("access_token", redditApp.getToken().getAccessToken());
                }
                Log.e("URL in fragments", url);
            }
        });
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
    }

    private void loadMoreData() {

        String url = this.url + "?after=";
        if (getActivity() instanceof SearchResultsActivity)
            url = this.url + "&after=";
        url += data.get(data.size() - 1).id;

        swipeRefreshLayout.setRefreshing(true);
        String accessToken;

        Log.e("Load more URL", url);
        Toast.makeText(getActivity(),"Loading more posts",100).show();
        if (redditApp.isLoggedin == true && url.contains("?q=") == false)
            accessToken = redditApp.getToken().getAccessToken();
        else
            accessToken = null;

        ListenableFuture<Model> future = postFetcher.getPosts(url, accessToken);
        Futures.addCallback(future, new FutureCallback<Model>() {
            @Override
            public void onSuccess(Model model) {
                for (Model.Data.Container c : model.getChildrenList()) {
                    String commentURL = c.getCommentsUrl();
                    if (commentURL.endsWith("?ref=search_posts"))
                        commentURL = commentURL.substring(0, commentURL.length() - 17);
                    c.setCommentURL(commentURL);
                    RedditCardPost post = new RedditCardPost(c.getId(), c.getTitle(), c.getUrl(),
                            c.getImgURL(), c.getThumbnail(), c.getCommentsUrl(), c.getNum_comments(),
                            c.getUpvotes(), "r/" + c.getSubreddit());
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

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(ListFragment.class.getName(), "Network request failed");
            }
        });
    }

    private void showPostFromDB() {
        if (dbHelper == null)
            return;
        List<RedditCardPost> postList = dbHelper.getallPosts();
        for (RedditCardPost post : postList) {
            data.add(post);
        }
        adapter.notifyDataSetChanged();
    }

    public void showPosts(String url, String... headers) {
        ListenableFuture<Model> future = postFetcher.getPosts(url, headers);
        if (getActivity() instanceof SearchResultsActivity) {
            Log.e(ListFragment.class.getName(), "Set swipe to true");
            swipeRefreshLayout.setRefreshing(true);
        }
        Futures.addCallback(future, new FutureCallback<Model>() {
            @Override
            public void onSuccess(Model model) {
                ArrayList<RedditCardPost> newPosts = new ArrayList<RedditCardPost>();
                for (Model.Data.Container c : model.getChildrenList()) {
                    String commentURL = c.getCommentsUrl();
                    if (commentURL.endsWith("?ref=search_posts"))
                        commentURL = commentURL.substring(0, commentURL.length() - 17);
                    c.setCommentURL(commentURL);
                    RedditCardPost post = new RedditCardPost(c.getId(), c.getTitle(), c.getUrl(),
                            c.getImgURL(), c.getThumbnail(), c.getCommentsUrl(), c.getNum_comments(),
                            c.getUpvotes(), "r/" + c.getSubreddit());
                    newPosts.add(post);
                    Log.e("Posts", post.id);
                    if (dbHelper != null)
                        dbHelper.addPost(post);
                }
                data.clear();
                for (RedditCardPost post : newPosts)
                    data.add(post);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(ListFragment.class.getName(), "Network request failed");
            }
        });
    }
}
