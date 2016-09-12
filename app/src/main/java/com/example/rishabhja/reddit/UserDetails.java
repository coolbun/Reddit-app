package com.example.rishabhja.reddit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rishabhj on 9/13/2016.
 */
public class UserDetails {
    public String getName() {
        return name;
    }

    @SerializedName("name")
    private String name;


}
