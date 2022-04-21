package com.example.dublincompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoreResult extends AppCompatActivity {

    ArrayList<RecyclerViewItem> list = new ArrayList<RecyclerViewItem>();
    ArrayList<String> idList = new ArrayList<String>();
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> ratingList = new ArrayList<String>();
    ArrayList<String> photoIdList = new ArrayList<String>();
    ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Bundle b = getIntent().getExtras();
        String latitude = b.getString("latitude");
        String longitude = b.getString("longitude");

        String key = getApplicationContext().getResources().getString(R.string.MAPS_API_KEY);
        nearPlace handler = new nearPlace();
        handler.execute(latitude, longitude, key);

        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // submit search button
            @Override
            public boolean onQueryTextSubmit(String query) {
                // send data to SearchResult activity
                Intent intent = new Intent(MoreResult.this, SearchResult.class);

                Bundle b = new Bundle();
                b.putString("keyword", query); // searched keyword
                intent.putExtras(b);

                startActivity(intent);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(MoreResult.this, DetailActivity.class);

                Bundle b = new Bundle();
                b.putString("place_id", idList.get(position));
                b.putParcelable("image", imageList.get(position));
                intent.putExtras(b);
                startActivity(intent);
            }
        });

    }

    private class nearPlace extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + params[0] + "," + params[1] + "&type=restaurant&rankby=distance&key=" + params[2])
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("results");
                int length = result.length();
                for (int i = 0; i < length; i++) {
                    JSONObject resultObject = result.getJSONObject(i);
                    String id = resultObject.getString("place_id");
                    String name = resultObject.getString("name");
                    String rating = resultObject.getString("rating");
                    String photoId = "none";
                    if (!resultObject.isNull("photos"))
                        photoId = resultObject.optJSONArray("photos").getJSONObject(0).getString("photo_reference");

                    idList.add(id);
                    nameList.add(name);
                    ratingList.add(rating);
                    photoIdList.add(photoId);
                }

                String key = getApplicationContext().getResources().getString(R.string.MAPS_API_KEY);

                for (int i = 0; i < length; i++) {
                    Bitmap image;
                    if (photoIdList.get(i) == "none"){
                        image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.restaurant);
                    }
                    else {
                        FirstActivity.placePhoto photoHandler = new FirstActivity.placePhoto();
                        image = photoHandler.execute(photoIdList.get(i), key).get();
                    }
                    imageList.add(image);
                }

                ListViewAdapter simpleCursorAdapter = new ListViewAdapter(MoreResult.this, nameList, ratingList, imageList);
                ListView listView = (ListView) findViewById(R.id.list);
                listView.setAdapter(simpleCursorAdapter);

            } catch (Exception e) {

            }
        }
    }
}