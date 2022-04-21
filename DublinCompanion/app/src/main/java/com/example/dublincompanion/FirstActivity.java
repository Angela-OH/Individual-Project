// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.dublincompanion;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class FirstActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = FirstActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    ArrayList<RecyclerViewItem> list = new ArrayList<RecyclerViewItem>();
    ArrayList<String> idList = new ArrayList<String>();
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> photoIdList = new ArrayList<String>();
    ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();
    ArrayList<String> latList = new ArrayList<String>();
    ArrayList<String> lngList = new ArrayList<String>();
    ArrayList<String> typeList = new ArrayList<String>();
    MarkerOptions markerOptions = new MarkerOptions();
    Map<String, String> id_map = new HashMap<>();
    Map<String, Bitmap> image_map = new HashMap<>();

    // [START maps_current_place_on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_first);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]

        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // submit search button
            @Override
            public boolean onQueryTextSubmit(String query) {
                // send data to SearchResult activity
                Intent intent = new Intent(FirstActivity.this, SearchResult.class);

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

        TextView more = (TextView) findViewById(R.id.textView3);
        more.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstActivity.this, MoreResult.class);
                Bundle b = new Bundle();
                b.putString("latitude", String.valueOf(lastKnownLocation.getLatitude()));
                b.putString("longitude", String.valueOf(lastKnownLocation.getLongitude()));
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        View button = (View) findViewById(R.id.chatbot);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstActivity.this, Chatbot.class);

                startActivity(intent);

            }
        });
    }
    // [END maps_current_place_on_create]


    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    // [END maps_current_place_on_save_instance_state]


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        //updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }
    // [END maps_current_place_on_map_ready]

    private void getNearBy(Location location) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String key = getApplicationContext().getResources().getString(R.string.MAPS_API_KEY);

        // transportation
        nearPlace bus_handler = new nearPlace();
        bus_handler.execute(latitude, longitude, key, "bus_station");

        nearPlace train_handler = new nearPlace();
        train_handler.execute(latitude, longitude, key, "train_station");

        // attraction
        nearPlace tourist_handler = new nearPlace();
        tourist_handler.execute(latitude, longitude, key, "tourist_attraction");

        // food
        nearPlace bar_handler = new nearPlace();
        bar_handler.execute(latitude, longitude, key, "bar");

        nearPlace handler = new nearPlace();
        handler.execute(latitude, longitude, key, "restaurant");
    }

    private void marker() {
        float color = 0;
        for (int i = 0; i < nameList.size(); i++) {
            Double lat = Double.parseDouble(latList.get(i));
            Double lng = Double.parseDouble(lngList.get(i));
            //id_map.put(nameList.get(i), idList.get(i));
            //image_map.put(nameList.get(i), imageList.get(i));
            switch (typeList.get(i)) {
                case "restaurant":
                    color = BitmapDescriptorFactory.HUE_RED;
                    break;
                case "bar":
                    color = BitmapDescriptorFactory.HUE_ROSE;
                    break;
                case "bus_station":
                    color = BitmapDescriptorFactory.HUE_BLUE;
                    break;
                case "train_station":
                    color = BitmapDescriptorFactory.HUE_AZURE;
                    break;
                case "tourist_attraction":
                    color = BitmapDescriptorFactory.HUE_GREEN;
                    break;
            }
            markerOptions
                    .position(new LatLng(lat, lng))
                    .title(nameList.get(i))
                    .icon(BitmapDescriptorFactory
                    .defaultMarker(color));
            Marker mkr = this.map.addMarker(markerOptions);
            id_map.put(mkr.getId(), idList.get(i));
            image_map.put(mkr.getId(), imageList.get(i));
        }
        this.map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(FirstActivity.this, DetailActivity.class);

                Bundle b = new Bundle();
                b.putString("place_id", id_map.get(marker.getId()));
                b.putParcelable("image", image_map.get(marker.getId()));
                intent.putExtras(b);
                startActivity(intent);
            }});
    }
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                getNearBy(lastKnownLocation);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    // [START maps_current_place_show_current_place]
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .position(defaultLocation));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
    // [END maps_current_place_show_current_place]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]

    private class nearPlace extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                            + params[0] + "," + params[1] + "&type=" + params[3] +
                            "&rankby=distance&key=" + params[2])
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return new String[] {response.body().string(), params[3]};
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] params) {
            super.onPostExecute(params);
            String response = params[0];
            String type = params[1];
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("results");
                int length = result.length();
                idList.clear();
                nameList.clear();
                photoIdList.clear();
                imageList.clear();
                latList.clear();
                lngList.clear();
                list.clear();
                typeList.clear();

                for (int i = 0; i < length; i++) {
                    JSONObject resultObject = result.getJSONObject(i);
                    String id = resultObject.getString("place_id");
                    String name = resultObject.getString("name");
                    String photoId = "none";
                    if (!resultObject.isNull("photos"))
                        photoId = resultObject.optJSONArray("photos")
                                .getJSONObject(0).getString("photo_reference");
                    JSONObject location = resultObject.getJSONObject("geometry").getJSONObject("location");
                    String latitude = location.getString("lat");
                    String longitude = location.getString("lng");

                    idList.add(id);
                    nameList.add(name);
                    photoIdList.add(photoId);
                    latList.add(latitude);
                    lngList.add(longitude);
                    typeList.add(type);
                }

                String key = getApplicationContext().getResources().getString(R.string.MAPS_API_KEY);

                for (int i = 0; i < length; i++) {
                    RecyclerViewItem item = new RecyclerViewItem();
                    item.setId(idList.get(i));
                    item.setName(nameList.get(i));
                    Bitmap image;
                    if (photoIdList.get(i) == "none") {
                        image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.restaurant);
                    } else {
                        placePhoto photoHandler = new placePhoto();
                        image = photoHandler.execute(photoIdList.get(i), key).get();
                    }
                    item.setImage(image);
                    imageList.add(image);
                    list.add(item);
                }
                Log.d("type", type);
                if (type == "restaurant") {
                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                    RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);
                    recyclerView.setAdapter(adapter);
                }
                marker();

            } catch (Exception e) {

            }
        }
    }

    public static class placePhoto extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference="
                            + params[0] + "&key=" + params[1])
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return BitmapFactory.decodeStream(response.body().byteStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap response) {
            super.onPostExecute(response);
            try {
            } catch (Exception e) {
            }
        }
    }

    // action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.my_page:
                // send data to SearchResult activity
                Intent intent = new Intent(FirstActivity.this, MyPageActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}