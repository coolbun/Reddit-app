package com.example.rishabhja.reddit;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.rishabhja.reddit.databinding.ActivityCommentsBinding;

public class CommentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCommentsBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_comments);


    }
}
