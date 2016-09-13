package com.example.rishabhja.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class SubRedditModel {

    @SerializedName("data")
    private Data data;
    private int numberOfPosts;

    public ArrayList<Data.Container> getChildrenList() {
        return data.getChildrenList();
    }

    public static class Data {
        @SerializedName("children")
        private ArrayList<Container> childrenList;

        public ArrayList<Container> getChildrenList() {
            return childrenList;
        }

        public static class Container {

            @SerializedName("data")
            private Content content;

            public String getTitle() {
                return content.getTitle();
            }

            public static class Content {
                @SerializedName("display_name")
                private String title;

                public String getTitle() {
                    return title;
                }
            }

        }
    }
}
