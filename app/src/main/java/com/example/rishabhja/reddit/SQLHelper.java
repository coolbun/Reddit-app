package com.example.rishabhja.reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by rishabh.ja on 07/09/16.
 */
public class SQLHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PostsDatabase.db";
    public static final String TABLE_NAME = "finalRedditPosts";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String ImgURL = "imgURL";
    public static final String THUMBANILS = "thumbanil";
    public static final String COMMENTSURL = "commentsUrl";
    public static final String NUMBEROFCOMMENTS = "num_comments";
    public static final String UPVOTES = "upvotes_count";
    public static final String SUBREDDIT = "subreddit";



    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " TEXT PRIMARY KEY," +
                TITLE + " TEXT," +
                URL + " TEXT," +
                ImgURL + " TEXT," +
                THUMBANILS + " TEXT," +
                COMMENTSURL + " TEXT," +
                NUMBEROFCOMMENTS + " INTEGER," +
                UPVOTES + " INTEGER," +
                SUBREDDIT + " TEXT" +
                ");";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addPost(RedditCardPost post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, post.id);
        values.put(ImgURL, post.imgURL);
        values.put(URL, post.url);
        values.put(TITLE, post.title);
        values.put(THUMBANILS, post.thumbanil);
        values.put(COMMENTSURL,post.commentsUrl);
        values.put(NUMBEROFCOMMENTS,post.num_comments);
        values.put(UPVOTES,post.upvotes);
        values.put(SUBREDDIT,post.subreddit);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public RedditCardPost getPost(String id) {

        SQLiteDatabase db = this.getReadableDatabase();
        RedditCardPost post = null;
        @Closeable Cursor cursor = db.query(TABLE_NAME, new String[]{ID, TITLE, URL, ImgURL,
                THUMBANILS,COMMENTSURL}, ID + "=?",
                new String[]{id}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            post = new RedditCardPost(cursor.getString(0),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4),cursor.getString(5),cursor.getInt(6),cursor.getInt(7),
                    cursor.getString(8));
        }
        return post;
    }

    public List<RedditCardPost> getallPosts() {
        List<RedditCardPost> postList = new ArrayList<RedditCardPost>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        RedditCardPost post = new RedditCardPost(cursor.getString(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                cursor.getString(5),
                                cursor.getInt(6),
                                cursor.getInt(7),
                                cursor.getString(8));
                        postList.add(post);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return postList;
    }

    public void deletePost(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID + "=?", new String[]{id});
        db.close();
    }

    public void deleteAll() {
        List<RedditCardPost> postlist = getallPosts();
        for (RedditCardPost post : postlist) {
            deletePost(post.id);
        }
    }
}
