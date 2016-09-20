package com.example.models;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserDetails {

    @SerializedName("name")
    private String name;
    private String headerKey;
    private String url;

    public String getName() {
        return name;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    private String headerValue;

    public String getHeaderKey() {
        return headerKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }
}
