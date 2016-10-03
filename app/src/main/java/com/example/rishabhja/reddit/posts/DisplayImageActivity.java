package com.example.rishabhja.reddit.posts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rishabhja.reddit.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class DisplayImageActivity extends AppCompatActivity {

    private static final String TAG = "DisplayImageActivity";
    private Context context;
    private Toolbar tb;
    private Uri imgUri;
    private ImageView imageView;
    private File imgfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        context = this;
        tb = (Toolbar) findViewById(R.id.displayimageactivity_toolbar);
        setSupportActionBar(tb);
        setStausBarcolor();
        initUI();

        String imgURL = getIntent().getStringExtra("URL");

        imageView = (ImageView) findViewById(R.id.postImageZoom);
        Picasso.with(this)
                .load(imgURL)
                .into(new MyTarget(imageView, imgURL));
    }


    class MyTarget implements Target {
        private ImageView imageView;
        private String imgURL;

        public MyTarget(ImageView imageView, String url) {
            this.imageView = imageView;
            this.imgURL = url;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            this.imageView.setImageBitmap(bitmap);
            try {
                File dir = Environment.getExternalStorageDirectory();
                File path = new File(dir.getAbsolutePath(), "SavesIReddit");
                if (!path.exists())
                    path.mkdirs();
                imgfile = new File(path, "myimage.png");
                OutputStream fos = new FileOutputStream(imgfile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }


    private void initUI() {
        setStausBarcolor();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_displayimage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.share_image:

                //Testing
                Picasso.with(this)
                        .load(Uri.fromFile(imgfile))
                        .into(imageView);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgfile));
                Log.d(TAG, "File path " + imgfile.getAbsolutePath());
                intent.setType("image/*");
                startActivity(Intent.createChooser(intent, "Share image via..."));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
