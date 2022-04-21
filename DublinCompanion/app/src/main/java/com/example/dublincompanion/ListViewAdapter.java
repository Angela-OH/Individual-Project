package com.example.dublincompanion;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter {
    private ArrayList<String> name = new ArrayList<String>();
    private ArrayList<String> rating = new ArrayList<String>();
    private ArrayList<Bitmap> image = new ArrayList<Bitmap>();
    private Activity context;

    public ListViewAdapter(Activity context, ArrayList<String> name, ArrayList<String> rating, ArrayList<Bitmap> image) {
        super(context, R.layout.row_item, name);
        this.context = context;
        this.name = name;
        this.rating = rating;
        this.image = image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null)
            row = inflater.inflate(R.layout.row_item, null, true);

        TextView near_name = (TextView) row.findViewById(R.id.name);
        TextView near_rating = (TextView) row.findViewById(R.id.rating);
        ImageView near_image = (ImageView) row.findViewById(R.id.image);

        near_name.setText(name.get(position));
        near_rating.setText(rating.get(position));
        near_image.setImageBitmap(image.get(position));

        return row;
    }
}