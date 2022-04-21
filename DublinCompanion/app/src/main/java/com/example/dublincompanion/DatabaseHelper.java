package com.example.dublincompanion;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "";
    private static final String DB_NAME = "likes.db";
    private static final String TABLE_NAME = "Likes";
    public static final int DATABASE_VERSION = 1;
    private Context mContext;
    private SQLiteDatabase myDatabase;

    private static final String DATABASE_CREATE =
            "create table " + TABLE_NAME  +
                    " (Place_id text not null primary key, " +
                    "Image BLOB, " +
                    "Name text not null);";

    public DatabaseHelper(Context context)
    {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;
        databaseCheck();
    }

    // copy db file from assets folder
    // Reference: https://andro-jinu.tistory.com/entry/todaysaying5
    private void databaseCheck()
    {
        File dbFile = new File(DB_PATH + TABLE_NAME);
        if (!dbFile.exists())
        {
            dbCopy();
        }
        else
        {
            openDatabase();
        }
    }
    private void dbCopy()
    {
        try
        {
            File folder = new File(DB_PATH);
            if (!folder.exists())
            {
                folder.mkdir();
            }
            InputStream inputStream = mContext.getAssets().open(DB_NAME);
            String out_filename = DB_PATH + TABLE_NAME;
            OutputStream outputStream = new FileOutputStream(out_filename);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = inputStream.read(mBuffer)) > 0)
            {
                outputStream.write(mBuffer, 0, mLength);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void openDatabase() throws SQLException {
        String path = DB_PATH + TABLE_NAME;
        myDatabase = SQLiteDatabase.openDatabase(path,null, SQLiteDatabase.OPEN_READWRITE);
    }
    // Reference Complete

    @Override
    public synchronized void close() {
        if (myDatabase!=null)
            myDatabase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
