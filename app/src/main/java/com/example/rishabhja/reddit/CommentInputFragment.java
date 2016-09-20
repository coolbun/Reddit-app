package com.example.rishabhja.reddit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.PostFetcher;
import com.example.models.UserDetails;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by rishabh.ja on 19/09/16.
 */
public class CommentInputFragment extends DialogFragment {

    private DialogToActivity dialogToActivity;
    private String commentId;
    private String comment;

    public void setInteface(DialogToActivity dialogToActivity) {
        this.dialogToActivity = dialogToActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        commentId=getArguments().getString("id");

        final View view=inflater.inflate(R.layout.comment_input_fragment, null);
        builder.setView(view)
                .setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText= (EditText) view.findViewById(R.id.comment_text);
                        Log.e("comment by user",editText.getText().toString());
                        comment=editText.getText().toString();
                        postComment();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    private void postComment() {
        RedditApp redditApp = (RedditApp) getActivity().getApplication();
        UserDetails userDetails = redditApp.getToken();
        PostFetcher postFetcher = new PostFetcher(redditApp.getCurrentUrl() + "/api/comment");
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Comment Post Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("response",response.toString());
                dialogToActivity.updateUI();
            }
        });
        postFetcher.setFormValues("api_type","json","thing_id",commentId,"text",
                comment);
        postFetcher.execute("Authorization",userDetails.getAccessToken());
    }

    public interface DialogToActivity {
        public void updateUI();
    }
}