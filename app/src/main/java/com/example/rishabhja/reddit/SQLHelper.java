package com.example.rishabhja.reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.rishabhja.reddit.Model.Data.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishabh.ja on 07/09/16.
 */
public class SQLHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Posts.db";
    public static final String TABLE_NAME = "redditPost";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String ImgURL = "imgURL";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES="CREATE TABLE "+ TABLE_NAME+" ("+
                ID+" TEXT PRIMARY KEY,"+
                TITLE+" TEXT,"+
                URL+" TEXT,"+
                ImgURL+" TEXT"+
                ");";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        final String SQL_DELETE_ENTRIES="DROP TABLE IF EXISTS "+ TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addPost(Container content){
        SQLiteDatabase db=this.getWritableDatabase();

        RedditPost post=new RedditPost(content);

        ContentValues values=new ContentValues();
        values.put(ID,post.getId());
        values.put(ImgURL,post.getImgUrl());
        values.put(URL,post.getUrl());
        values.put(TITLE,post.getTitle());
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public RedditPost getPost(String id){

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_NAME, new String[]{ID,TITLE,URL,ImgURL},ID+"=?",new String[]{id},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        RedditPost post=new RedditPost(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
        return post;
    }

    public List<RedditPost> getallPosts(){
        List<RedditPost> postList=new ArrayList<RedditPost>();

        String selectQuery="SELECT * FROM "+TABLE_NAME;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                RedditPost post=new RedditPost(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3));
                postList.add(post);
            }while(cursor.moveToNext());
        }
        return postList;
    }

    public void deletePost(String id){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME,ID+"=?",new String[]{id});
        db.close();
    }

    public void deleteAll(){
        List<RedditPost> postlist=getallPosts();
        for(RedditPost post: postlist){
            deletePost(post.getId());
        }
    }
}
