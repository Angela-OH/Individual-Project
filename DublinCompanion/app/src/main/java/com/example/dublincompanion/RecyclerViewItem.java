package com.example.dublincompanion;

import android.graphics.Bitmap;
import android.net.Uri;

public class RecyclerViewItem {
    private Bitmap image;
    private String name;
    private String id;

    public Bitmap getImage(){
        return image;
    }
    public String getName(){
        return name;
    }
    public String getId() { return id; }
    public void setImage(Bitmap image){
        this.image = image;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setId(String id) { this.id = id; }
}
