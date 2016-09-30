package com.example.rishabhja.reddit.comments;

import android.annotation.TargetApi;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.models.UserDetails;
import com.example.rishabhja.reddit.utils.DividerItemDecoration;
import com.example.rishabhja.reddit.utils.InvalidAccessDialog;
import com.example.rishabhja.reddit.R;
import com.example.rishabhja.reddit.viewmodels.Comment;
import com.example.rishabhja.reddit.viewmodels.RedditApp;

import java.util.ArrayList;
import java.util.List;


public class CommentsActivity extends AppCompatActivity {


    private ArrayList<Comment> data;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RedditApp redditApp;
    private UserDetails userDetails;
    private ProgressBar progressBar;
    private CommentProcessor commentProcessor;
    private FloatingActionButton addCommentButton;
    private String namePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        setStausBarcolor();
        String url = getIntent().getStringExtra("URL") + ".json";
        Log.e("Comments activity url",url);
        namePost=getIntent().getStringExtra("NAME");
        redditApp = (RedditApp) getApplication();
        userDetails = redditApp.getToken();
        progressBar = (ProgressBar) findViewById(R.id.commentprogressbar);
        addCommentButton = (FloatingActionButton) findViewById(R.id.addCommentToMainThread);

        String title = getIntent().getStringExtra("Title");
        TextView textView = (TextView) findViewById(R.id.commentTitle);
        textView.setText(title);

        initUI();
        commentProcessor = new CommentProcessor(url, new
                CommentProcessor.SendResult() {
                    @Override
                    public void sendComments(ArrayList<Comment> comments) {
                        Log.e("Call sendComments", "here main");
                        for (Comment comment : comments) {
                            data.add(comment);
                        }
                        Log.d(CommentsActivity.class.getName(), "notifyDataSetChanged");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
        commentProcessor.fetchComments();
    }


    private void initUI() {
        initListView();
        Toolbar tb = (Toolbar) findViewById(R.id.commentsToolbar);
        setSupportActionBar(tb);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("name of Post", namePost);
                if (redditApp.isLoggedin == false) {
                    InvalidAccessDialog dialog = new InvalidAccessDialog();
                    dialog.show(getFragmentManager(), "invalid_access");
                } else {
                    CommentInputFragment dialog = new CommentInputFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("id", namePost);
                    dialog.setArguments(bundle);

                    dialog.setInteface(new CommentInputFragment.DialogToActivity() {
                        @Override
                        public void updateUI() {
                            data.clear();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            commentProcessor.fetchComments();
                        }
                    });
                    dialog.show(getFragmentManager(), "input");
                }
            }
        });
    }

    private void initListView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyler_comments);
        data = new ArrayList<>();
        adapter = new CustomAdapter(data);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CommentBindingHolder> {

    private List<Comment> mComments;

    @Override
    public CommentBindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_layout, parent, false);
        CommentBindingHolder holder = new CommentBindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(CommentBindingHolder holder, final int position) {
        final ViewDataBinding binding = holder.getBinding();
        TextView textView = (TextView) binding.getRoot().findViewById(R.id.addComment);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("name of comment clicked", mComments.get(position).name);
                if (redditApp.isLoggedin == false) {
                    InvalidAccessDialog dialog = new InvalidAccessDialog();
                    dialog.show(getFragmentManager(), "invalid_access");
                } else {
                    CommentInputFragment dialog = new CommentInputFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("id", mComments.get(position).name);
                    dialog.setArguments(bundle);

                    dialog.setInteface(new CommentInputFragment.DialogToActivity() {
                        @Override
                        public void updateUI() {
                            data.clear();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            commentProcessor.fetchComments();
                        }
                    });
                    dialog.show(getFragmentManager(), "input");
                }
            }
        });

        Comment post = mComments.get(position);
        holder.getBinding().getRoot().setPadding(mComments.get(position).level * 20, 0, 0, 0);
        holder.getBinding().setVariable(com.example.rishabhja.reddit.BR.comment, post);
        holder.getBinding().executePendingBindings();
    }


    public CustomAdapter(ArrayList<Comment> comments) {
        mComments = comments;
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    protected class CommentBindingHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public CommentBindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStausBarcolor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cardview_dark_background));
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

}
