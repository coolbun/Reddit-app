package com.example.rishabhja.reddit;

import android.util.Log;

import com.example.PostFetcher;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by rishabh.ja on 27/09/16.
 */
public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.e("reg TOKEN",refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //Implement this method if you want to store the token on your server
        PostFetcher postFetcher=new PostFetcher();
        postFetcher.setCallback(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"Unable to send token to app server "+ e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG,response.body().toString());
            }
        });
        postFetcher.setURL("http://172.16.44.27:5000/add?title=rishabh&regid="+token);
        Log.e(TAG,"Token "+token);
        postFetcher.execute();
    }
}