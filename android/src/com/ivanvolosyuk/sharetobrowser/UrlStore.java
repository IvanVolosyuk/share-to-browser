package com.ivanvolosyuk.sharetobrowser;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.util.Log;

public class UrlStore extends ContentProvider {
  public static final String URL = "url";
  public static final String BROWSER_ID = "browser_id";
  public static final Uri CONTENT_URI = Uri.parse("content://com.ivanvolosyuk.sharetobrowser.urls");

  private class DB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "urls";
    private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                URL + " TEXT, " +
                BROWSER_ID + " TEXT);";
    
    public DB(Context context) {
      super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // nothing for version 1
    }
  };
  
  DB db;
  
  @Override
  public boolean onCreate() {
    db = new DB(getContext());
    return true;
  }
  
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return db.getWritableDatabase().delete(DB.TABLE_NAME, selection, selectionArgs);
  }
  
  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Uri insert(Uri uri, ContentValues values) {
    Log.d("sendtocomputer", "Got url: " + values.getAsString(URL));
    db.getWritableDatabase().insert(DB.TABLE_NAME, null, values);
    return null;
  }
  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    return db.getReadableDatabase().query(
        DB.TABLE_NAME, projection, selection, selectionArgs,
        null, null, sortOrder);
  }
  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    // TODO Auto-generated method stub
    return 0;
  }
}
