package com.example.dublincompanion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

public class DatabaseManager {
    Context context;
    private com.example.dublincompanion.DatabaseHelper myDatabaseHelper;
    private SQLiteDatabase myDatabase;
    public static final String DATABASE_TABLE = "Likes";

    public DatabaseManager(Context context)
    {
        this.context = context;

    }

    public DatabaseManager open() throws SQLException {
        myDatabaseHelper = new DatabaseHelper(context);
        myDatabase = myDatabaseHelper.getReadableDatabase();
        return this;
    }

    public void close()
    {
        myDatabaseHelper.close();
    }

    public byte[] getImage(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
    }
    // about Restaurant
    public long InsertLike(String place_id, Bitmap image, String name)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("Place_id", place_id);
        initialValues.put("Image", getImage(image));
        initialValues.put("Name", name);
        Log.d("place_id", place_id);
        return myDatabase.insert(DATABASE_TABLE, null, initialValues);
    }

    public Cursor getLike(String place_id)
    {
        return myDatabase.query(DATABASE_TABLE, new String[]{"Place_id"},
                "Place_id"+"=?",
                new String[] {place_id},
                null,
                null,
                null
        );
    }

    public boolean DeleteLike(String place_id)
    {
        return myDatabase.delete(DATABASE_TABLE, "Place_id='" + place_id +"'", null) > 0;
    }

   public Cursor getAllLike()
    {
        return myDatabase.query(DATABASE_TABLE, new String[]{"Place_id", "Image", "Name"},
                null,
                null,
                null,
                null,
                null);
    }
}
