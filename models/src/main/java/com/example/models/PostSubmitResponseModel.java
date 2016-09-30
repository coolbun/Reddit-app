package com.example.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rishabh.ja on 30/09/16.
 */
public class PostSubmitResponseModel {

    @SerializedName("json")
    public Json json;

    public static class Errors {
    }

    public static class Data {
        @SerializedName("url")
        public String url;
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
    }

    public static class Json {
        @SerializedName("errors")
        public List<Errors> errors;
        @SerializedName("data")
        public Data data;
    }
}
