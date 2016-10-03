package com.example.rishabhja.reddit.posts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.PostFetcher;
import com.example.models.ImgurModel;
import com.example.models.PostSubmitResponseModel;
import com.example.rishabhja.reddit.R;
import com.example.rishabhja.reddit.viewmodels.RedditApp;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddPostActivity extends AppCompatActivity {


    private static final int CAPTURE_IMAGE = 1;
    private static final int IMAGE_GALLERY = 2;
    private static final String TAG = "AddPostActivity";
    private Button submitButton;
    private RedditApp redditApp;
    private EditText title, body, captchaText;
    private ImageView imageView;
    private Context context;
    private ImageView imagePost;
    private TextView loadImage;
    private File imgDir;
    private String iden, imgURL;


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
        loadImage = (TextView) findViewById(R.id.load_image);
        imagePost = (ImageView) findViewById(R.id.image_post);
        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostFetcher postFetcher = new PostFetcher();
                ListenableFuture<PostSubmitResponseModel> future = postFetcher.submitPost(
                        redditApp.getCurrentUrl(),
                        redditApp.getToken().getAccessToken(),
                        title.getText().toString(),
                        body.getText().toString(),
                        "link",
                        captchaText.getText().toString(),
                        iden,
                        "test",
                        imgURL
                );

                Futures.addCallback(future, new FutureCallback<PostSubmitResponseModel>() {
                    @Override
                    public void onSuccess(PostSubmitResponseModel postSubmitResponseModel) {
                        String postUrl = postSubmitResponseModel.json.data.url;
                        Intent intent = new Intent(context, DisplayPostActivity.class);
                        intent.putExtra("URL", postUrl);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, throwable.toString());
                    }
                });
            }
        });

        loadCAPTCHA();

    }

    /**
     * Load captcha url
     */
    private void loadCAPTCHA() {
        PostFetcher postFetcher = new PostFetcher();
        ListenableFuture<String> listenableFuture = postFetcher.getCaptchaURL
                ("https://oauth.reddit.com/api/new_captcha", redditApp.getToken().getAccessToken());

        Futures.addCallback(listenableFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(final String imgUrl) {
                iden = imgUrl;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "https://www.reddit.com/captcha/" + imgUrl;
                        Picasso.with(imageView.getContext())
                                .load(url)
                                .into(imageView);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(AddPostActivity.class.getName(), "Captcha not found");
            }
        });
    }

    /**
     * Opens image chooser on click
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    submitButton.setEnabled(false);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    imgDir = new File(externalDir, "RedditAppImage" + Calendar.getInstance().getTime());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgDir));
                    startActivityForResult(intent, CAPTURE_IMAGE);

                } else if (items[item].equals("Choose from Library")) {
                    submitButton.setEnabled(false);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), IMAGE_GALLERY);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == CAPTURE_IMAGE) {
            PostFetcher postFetcher = new PostFetcher();
            ListenableFuture<String> future = postFetcher.getUrlImgur(imgDir);
            Futures.addCallback(future, new FutureCallback<String>() {
                @Override
                public void onSuccess(final String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgURL = s;
                            submitButton.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            });

            Picasso.with(this).load(imgDir).centerCrop().fit().into(imagePost);
        } else if (requestCode == IMAGE_GALLERY) {
            Uri imgUri = data.getData();
            Picasso.with(this).load(imgUri).centerCrop().fit().into(imagePost);
            File imgdir = new File(imgUri.getPath());
            PostFetcher postFetcher = new PostFetcher();
            ListenableFuture<String> future = postFetcher.getUrlImgur(imgdir);

            Futures.addCallback(future, new FutureCallback<String>() {
                @Override
                public void onSuccess(final String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgURL = s;
                            submitButton.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            });
        }
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
