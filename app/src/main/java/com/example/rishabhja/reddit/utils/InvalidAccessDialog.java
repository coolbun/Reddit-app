package com.example.rishabhja.reddit.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.rishabhja.reddit.R;

/**
 * Created by rishabh.ja on 19/09/16.
 */
public class InvalidAccessDialog extends DialogFragment {

    public interface LoginClickInterface {
        public void onClick();
    }
    LoginClickInterface loginClick;

    public void setClickListener(LoginClickInterface loginClick){
        this.loginClick=loginClick;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.invalid_access,null))
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }


}