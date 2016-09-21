package com.example.rishabhja.reddit;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.PostFetcher;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddPostActivity extends AppCompatActivity {


    private Button submitButton;
    private RedditApp redditApp;
    private EditText title, body, captchaText;
    private ImageView imageView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        initUI();
        setStausBarcolor();

        context = this;
        captchaText = (EditText) findViewById(R.id.captcha_text);
        title = (EditText) findViewById(R.id.postTitle);
        body = (EditText) findViewById(R.id.postBody);
        imageView = (ImageView) findViewById(R.id.captcha);
        redditApp = (RedditApp) getApplication();
        submitButton = (Button) findViewById(R.id.submitPost);

        PostFetcher postFetcher = new PostFetcher();
        ListenableFuture<String> listenableFuture = postFetcher.getCaptchaURL
                ("https://oauth.reddit.com/api/new_captcha", redditApp.getToken().getAccessToken());

        Futures.addCallback(listenableFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(final String imgUrl) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("IMAGE URL", imgUrl);
                        Picasso.with(imageView.getContext())
                                .load(imgUrl)
                                .into(imageView);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(AddPostActivity.class.getName(), "Captcha not found");
            }
        });


        Log.e("authorization tokeb",redditApp.getToken().getAccessToken());
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostFetcher postFetcher = new PostFetcher();
                postFetcher.setURL(redditApp.getCurrentUrl() + "/api/submit");

                Log.e("captcha",captchaText.getText().toString());
                postFetcher.setFormValues("api_type", "json",
                        "title", title.getText().toString(),
                        "text", body.getText().toString(),
                        "kind", "self",
                        "captcha", captchaText.getText().toString()
                );

                postFetcher.setCallback(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e("Post submit", response.body().string());
                    }
                });
                postFetcher.execute(redditApp.getToken().getAccessToken());
                //finish();
            }
        });


    }


    private void initUI() {
        Toolbar tb = (Toolbar) findViewById(R.id.add_post_toolbar);
        tb.setTitle("Create new post");
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
