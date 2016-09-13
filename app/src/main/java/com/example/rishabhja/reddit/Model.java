package com.example.rishabhja.reddit;

import android.provider.ContactsContract;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class Model {

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
            private Content data;
            private String imgurl;
            boolean present_imgurl = false;

            public Container(RedditPost post) {
                data = new Content();
                setId(post.getId());
                setImgURL(post.getImgUrl());
                setTitle(post.getTitle());
                setUrl(post.getUrl());
            }

            public String getUrl() {
                return data.getUrl();
            }

            public String getTitle() {
                return data.getTitle();
            }

            public String getId() {
                return data.getId();
            }

            public String getImgURL() {
                if (present_imgurl)
                    return imgurl;
                return data.getImgUrl();
            }

            public String getThumbnail() {
                return data.getThumbnail();
            }

            public void setUrl(String url) {
                data.seturl(url);
            }

            public void setTitle(String title) {
                data.settitle(title);
            }

            public void setId(String id) {
                data.setid(id);
            }

            public void setImgURL(String ImgUrl) {
                present_imgurl = true;
                imgurl = ImgUrl;
            }

            public static class Content {
                @SerializedName("title")
                private String title;
                @SerializedName("thumbnail")
                private String thumbnail;
                @SerializedName("name")
                private String id;
                @SerializedName("url")
                private String url;
                @SerializedName("preview")
                private Images preview;

                public String getTitle() {
                    return title;
                }

                public String getId() {
                    return id;
                }

                public String getUrl() {
                    return url;
                }

                public String getImgUrl() {
                    if (preview != null)
                        return preview.getImgURL();
                    return null;
                }

                public void seturl(String url) {
                    this.url = url;
                }

                public void settitle(String title) {
                    this.title = title;
                }

                public void setid(String id) {
                    this.id = id;
                }

                public String getThumbnail() {
                    return thumbnail;
                }

                public static class Images {
                    @SerializedName("images")
                    private ArrayList<ImageContent> images;

                    public String getImgURL() {
                        return images.get(0).getImgURL();
                    }

                    public static class ImageContent {
                        @SerializedName("source")
                        private Source source;

                        public String getImgURL() {
                            if (source != null)
                                return source.getImgURL();
                            return null;
                        }

                        private class Source {
                            @SerializedName("url")
                            private String url;

                            public String getImgURL() {
                                if (url != null)
                                    return url;
                                return null;
                            }
                        }
                    }
                }
            }

        }
    }
}
