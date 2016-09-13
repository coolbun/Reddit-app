package com.example.rishabhja.reddit;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Model.Data.Container> dataSet;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        ImageView imageViewIcon;
        ImageButton imageButtonIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
            this.imageButtonIcon = (ImageButton) itemView.findViewById(R.id.upvote);
        }
    }


    public CustomAdapter(ArrayList<Model.Data.Container> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    public void setEmpty() {
        dataSet.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textViewName;
        ImageView imageView = holder.imageViewIcon;
        ImageButton imageButton = holder.imageButtonIcon;

        textViewName.setText(dataSet.get(listPosition).getTitle());
        textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayPost.class);
                intent.putExtra("URL", dataSet.get(listPosition).getUrl());
                context.startActivity(intent);
            }
        });

        String imgURL = dataSet.get(listPosition).getImgURL();

        if (imgURL != null && imgURL.startsWith("http")) {
            imageView.setVisibility(View.VISIBLE);

            if (imgURL.contains("gif")) {
                imgURL = dataSet.get(listPosition).getThumbnail();
                Log.e("imgURL",imgURL);
            }
            Picasso.with(context)
                    .load(imgURL)
                    .fit().centerCrop()
                    .into(imageView);
            final String finalImgURL = imgURL;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.setContentView(R.layout.image_view);
                    ImageView image = (ImageView) dialog.findViewById(R.id.zoomImage);
                    Picasso.with(context)
                            .load(finalImgURL)
                            .into(image);
                    dialog.show();
                }
            });
        } else {
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}