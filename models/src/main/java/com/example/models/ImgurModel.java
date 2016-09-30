package com.example.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rishabh.ja on 30/09/16.
 */
public class ImgurModel {
    @SerializedName("link")
    private String url;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
