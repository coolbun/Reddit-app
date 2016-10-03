package com.example.rishabhja.reddit.comments;

/**
 * Created by rishabh.ja on 18/09/16.
 */


import android.util.Log;

import com.example.PostFetcher;
import com.example.rishabhja.reddit.viewmodels.Comment;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;

public class CommentProcessor {

    private final SendResult sendResult;
    private final String url;

    CommentProcessor(String u, SendResult sendResult) {
        url = u;
        this.sendResult = sendResult;
    }

    /**
     * Load various details about the comment
     */

    private Comment loadComment(JSONObject data, int level) {
        Comment comment = new Comment("ge");
        try {
            comment.htmlText = data.getString("body_html");
            comment.author = data.getString("author");
            comment.points = (data.getInt("ups")
                    - data.getInt("downs"))
                    + "";
            comment.body = data.getString("body");
            comment.postedOn = new Date((long) data
                    .getDouble("created_utc"))
                    .toString();
            comment.level = level;
            comment.name = data.getString("name");
        } catch (Exception e) {
            Log.d("ERROR", "Unable to parse comment : " + e);
        }
        return comment;
    }

    // This is where the comment is actually loaded
    // For each comment, its replies are recursively loaded
    private void process(ArrayList<Comment> comments
            , JSONArray c, int level)
            throws Exception {
        for (int i = 0; i < c.length(); i++) {
            if (c.getJSONObject(i).optString("kind") == null)
                continue;
            if (!c.getJSONObject(i).optString("kind").equals("t1"))
                continue;
            JSONObject data = c.getJSONObject(i).getJSONObject("data");
            Comment comment = loadComment(data, level);
            if (comment.author != null) {
                comments.add(comment);
                addReplies(comments, data, level + 1);
            }
        }
    }

    // Add replies to the comments
    private void addReplies(ArrayList<Comment> comments,
                            JSONObject parent, int level) {
        try {
            if (parent.get("replies").equals("")) {
                // This means the comment has no replies
                return;
            }
            JSONArray r = parent.getJSONObject("replies")
                    .getJSONObject("data")
                    .getJSONArray("children");
            process(comments, r, level);
        } catch (Exception e) {
            Log.d("ERROR", "addReplies : " + e);
        }
    }

    /**
     * Load the comments as an ArrayList, so that it can be
     * easily passed to the ArrayAdapter
     */

    void fetchComments() {
        PostFetcher getComments = new PostFetcher();
        getComments.setURL(url);
        ListenableFuture<String> listenableFuture = getComments.fetchComments();
        Futures.addCallback(listenableFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(String comments) {
                try {
                    onCommentsResult(comments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(CommentProcessor.class.getName(), "Error connecting");
            }
        });
    }

    private void onCommentsResult(String raw) throws Exception {
        ArrayList<Comment> comments = new ArrayList<>();
        JSONArray r = new JSONArray(raw)
                .getJSONObject(1)
                .getJSONObject("data")
                .getJSONArray("children");

        // All comments at this point are at level 0
        // (i.e., they are not replies)
        process(comments, r, 0);
        sendResult.sendComments(comments);
    }

    public interface SendResult {
        public void sendComments(ArrayList<Comment> comments);
    }

}
