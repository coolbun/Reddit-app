package com.example.rishabhja.reddit;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class RedditCardPost {

    public final String title;
    public final String imgURL;
    public final String url;
    public final String thumbanil;
    public final String id;
    public final String commentsUrl;
    private final String BASE_URL="http://www.reddit.com";

    public RedditCardPost(String id, String title, String url, String imgurl,
                          String thumbanil, String commentsUrl) {
        this.id = id;
        this.title = title;
        this.url = url;
        Log.e("insert url",url);
        this.imgURL = imgurl;
        this.thumbanil = thumbanil;
        this.commentsUrl=commentsUrl;
    }


    @BindingAdapter({"bind:imageUrl", "bind:thumbnail"})
    public static void loadImage(ImageView imageView, String url, String thumbanil) {
        if (url != null && url.startsWith("http")) {
            imageView.setVisibility(View.VISIBLE);
            if (url.contains("gif") || url.contains("mp4") || url.contains("avi"))
                url = thumbanil;
            Picasso.with(imageView.getContext())
                    .load(url)
                    .fit().centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
        }
    }

    public void onImageClick(View view) {
        ImageView imageView = (ImageView) view;
        Dialog dialog = new Dialog(imageView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.image_view);
        ImageView image = (ImageView) dialog.findViewById(R.id.zoomImage);
        Picasso.with(image.getContext())
                .load(imgURL)
                .into(image);
        dialog.show();
    }

    public void onCommentClick(View view) {
        Intent intent = new Intent(view.getContext(), CommentsActivity.class);
        intent.putExtra("URL", BASE_URL+commentsUrl);
        intent.putExtra("Title",title);
        view.getContext().startActivity(intent);
    }

    public void onTitleClick(View view){
        Intent intent = new Intent(view.getContext(), DisplayPostActivity.class);
        Log.e("url",url);
        intent.putExtra("URL", url);
        view.getContext().startActivity(intent);
    }
}
