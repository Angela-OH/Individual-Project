package com.example.dublincompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    int heart_check;
    int arrow_check = 0;
    DatabaseManager dbManager;
    String place_id;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle b = getIntent().getExtras();
        place_id = b.getString("place_id");
        image = b.getParcelable("image");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
        String key = getApplicationContext().getResources().getString(R.string.MAPS_API_KEY);
        placeDetail handler = new placeDetail();
        handler.execute(place_id, key);

        ImageView heart = (ImageView) findViewById(R.id.heart);
        dbManager = new DatabaseManager(DetailActivity.this);
        try {

            dbManager.open();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Cursor cursor = dbManager.getLike(place_id);
        heart_check = cursor.getCount();
        if (heart_check == 0){
            heart.setImageResource(R.drawable.heart_empty);
        }
        else {
            heart.setImageResource(R.drawable.heart);
        }
    }

    private class placeDetail extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/details/json?place_id=" + params[0]
                        + "&fields=name,formatted_phone_number,formatted_address,rating,website,opening_hours&key="
                        + params[1])
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("response", response.body().toString());
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Log.d("object", jsonObject.toString());
                JSONObject result = jsonObject.getJSONObject("result");
                Log.d("result", result.toString());

                TextView detail_name = (TextView) findViewById(R.id.detail_name);
                TextView detail_rating = (TextView) findViewById(R.id.detail_rating);
                TextView detail_address = (TextView) findViewById(R.id.detail_address);
                TextView detail_hours = (TextView) findViewById(R.id.detail_hours);
                TextView detail_website = (TextView) findViewById(R.id.detail_website);
                TextView detail_phone = (TextView) findViewById(R.id.detail_phone);
                ImageView arrow = (ImageView) findViewById(R.id.arrow);
                ImageView next = (ImageView) findViewById(R.id.next);
                ImageView next2 = (ImageView) findViewById(R.id.next2);

                String name = result.getString("name");
                detail_name.setText(name);

                String phone = result.getString("formatted_phone_number");
                detail_phone.setText(phone);
                next2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + (String) detail_phone.getText()));
                        startActivity(intent);
                    }
                });

                String address = result.getString("formatted_address");
                detail_address.setText(address);

                arrow.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (arrow_check == 0) {
                            JSONArray hours = null;
                            try {
                                hours = result.getJSONObject("opening_hours").getJSONArray("weekday_text");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String opening_hours = "";
                            for (int i = 0; i < hours.length(); i++) {
                                try {
                                    opening_hours += hours.get(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                opening_hours += "\n";
                            }
                            detail_hours.setText(opening_hours);
                            arrow.setImageResource(R.drawable.up);
                            arrow_check = 1;
                        }
                        else {
                            detail_hours.setText("");
                            arrow.setImageResource(R.drawable.down);
                            arrow_check = 0;
                        }
                    }
                });

                String website = result.getString("website");
                if (website == null)
                    website = "";
                detail_website.setText(website);
                next.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) detail_website.getText()));
                        startActivity(intent);
                    }
                });

                String rating = result.getString("rating");
                detail_rating.setText(rating);

                ImageView heart = (ImageView) findViewById(R.id.heart);
                heart.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d("heart", String.valueOf(heart_check));
                        if (heart_check == 0) {
                            dbManager.InsertLike(place_id, image, name);
                            heart.setImageResource(R.drawable.heart);
                        }
                        else{
                            dbManager.DeleteLike(place_id);
                            heart.setImageResource(R.drawable.heart_empty);
                        }
                    }
                });


            } catch (Exception e) {

            }
        }
    }
}