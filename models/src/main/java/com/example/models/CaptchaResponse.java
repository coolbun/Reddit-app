package com.example.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rishabh.ja on 21/09/16.
 */
public class CaptchaResponse {

    @SerializedName("data")
    private Data data;

    public String getIden() {
        return data.iden;
    }


    private class Data {
        @SerializedName("iden")
        public String iden;
    }
}
