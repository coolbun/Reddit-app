package com.example.models;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserDetails {

    @SerializedName("name")
    private String name;
    private String accessToken;

    public String getName() {
        return name;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setName(String name) {
        this.name = name;
    }


}
