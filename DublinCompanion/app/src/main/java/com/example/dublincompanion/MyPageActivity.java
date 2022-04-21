package com.example.dublincompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import java.sql.SQLException;
import java.util.ArrayList;

public class MyPageActivity extends AppCompatActivity {

    ArrayList<RecyclerViewItem> list = new ArrayList<RecyclerViewItem>();
    ArrayList<String> idList = new ArrayList<String>();
    ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();
    ArrayList<String> nameList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        DatabaseManager dbManager = new DatabaseManager(MyPageActivity.this);
        try {

            dbManager.open();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Cursor cursor = dbManager.getAllLike();

        while(cursor.moveToNext()) {
            RecyclerViewItem item = new RecyclerViewItem();
            item.setId(cursor.getString(0));
            byte[] bytes = cursor.getBlob(1);
            item.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            item.setName(cursor.getString(2));
            list.add(item);
        }

        RecyclerView recyclerView = findViewById(R.id.LikerecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyPageActivity.this, LinearLayoutManager.HORIZONTAL, false));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);
        recyclerView.setAdapter(adapter);

        View button = (View) findViewById(R.id.chatbot);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, Chatbot.class);

                startActivity(intent);

            }
        });
    }

}