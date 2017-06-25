package com.parse.memories;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class Display extends AppCompatActivity {

    LatLng location;
    Boolean state;
    String title,description,postId;
    TextView titleTV;// = (TextView)findViewById(R.id.showTitle);
    TextView descTV;// = (TextView)findViewById(R.id.showDescription);

    ImageView main,like;// = (ImageView)findViewById(R.id.showMain);

    ParseFile file;

    Boolean heart =false;

    public void likeMe(View v){
        if(!heart) {

            like.setImageResource(R.drawable.shapesfill);
            heart =true;



        }else
        {

            like.setImageResource(R.drawable.shapes);
            heart =false;

        }
    }

    public void LikeDisplay(Boolean bool){

        if(bool) {

            like.setImageResource(R.drawable.shapesfill);


        }else
        {

            like.setImageResource(R.drawable.shapes);

        }


    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        titleTV = (TextView)findViewById(R.id.showTitle);
        descTV = (TextView)findViewById(R.id.showDescription);
        main = (ImageView)findViewById(R.id.showMain);
        like = (ImageView)findViewById(R.id.liked);


        Bundle extras = getIntent().getExtras();

        location = (LatLng) extras.get("location");

        state = extras.getBoolean("state");

        postId = extras.getString("postId");

        ParseQuery<ParseObject> getPosts = new ParseQuery<ParseObject>("Posts");

        getPosts.whereEqualTo("postId",Integer.parseInt(postId));

        Log.i("onCreate: ",postId);

        getPosts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    for(ParseObject object:objects){

                        Toast.makeText(Display.this, "object", Toast.LENGTH_SHORT).show();

                        Log.i("done: ",objects.toString());

                        titleTV.setText(object.get("title").toString());

                        descTV.setText(object.get("description").toString());

                        file = object.getParseFile("image");

                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    // Decode the Byte[] into
                                    // Bitmap
                                    Bitmap bmp = BitmapFactory
                                            .decodeByteArray(
                                                    data, 0,
                                                    data.length);

                                    // Set the Bitmap into the
                                    // ImageView
                                    main.setImageBitmap(bmp);

                                } else {
                                    Log.d("test", "Problem load image the data.");
                                }
                            }

                        });

                    }


                }
            }
        });

        ParseQuery<ParseObject> getLike = new ParseQuery<ParseObject>("Likes");

        getLike.whereEqualTo("postId", postId);
        getLike.whereEqualTo("likedBy", ParseUser.getCurrentUser().getUsername());

        getLike.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    if(objects.size() >0){

                        LikeDisplay(true);

                    }

                }
            }
        });

    }

    public void chat(View view){
        Intent i = new Intent(Display.this,Chat.class);
        startActivity(i);

    }

    @Override
    public void onBackPressed() {

        Intent back = new Intent(Display.this,MapsActivity.class);
        startActivity(back);

        super.onBackPressed();
    }
}
