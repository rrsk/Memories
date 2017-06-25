package com.parse.memories;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PostIt extends AppCompatActivity {

    EditText title;

    EditText description;

    ImageView main;

    ImageView like;

    Boolean state;

    Boolean heart = false;

    LatLng location;

    int postID;

    ParseFile file;

    ParseQuery<ParseObject> query = new ParseQuery<>("Posts");




    public void getphoto(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, 1);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode== RESULT_OK && data !=null){

            Uri image = data.getData();

            try {


                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);

                Picasso.with(this)
                        .load(image)
                        .resize(200, 200)
                        .centerCrop()
                        .into(main);

                ByteArrayOutputStream stream =new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 5, stream);

                byte[] byteArray = stream.toByteArray();

                file = new ParseFile(Integer.toString(query.count()+1)+".png",byteArray);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void Like(View view){

        heart = !heart;

        Liked(heart);



    }

    public void Liked(Boolean heart){

        if(heart) {

            like.setImageResource(R.drawable.shapesfill);
        }else
        {

            like.setImageResource(R.drawable.shapes);

        }

    }

    public void shareMemory(View view){
        if(postID > 0){
            Intent map = new Intent(this, MapsActivity.class);
            startActivity(map);
            finish();
            Toast.makeText(this, "Edited", Toast.LENGTH_SHORT).show();
        }
        ViewMemory();
    }

    public void ViewMemory() {

        Log.i("Info", "Memory");



        if (postID == 0) {

            Log.i("ViewMemory: ",Integer.toString(postID));

            try {
                postID = query.count();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            postID += 1;

            LatLng lastKnownLocation = location;

            Log.i("shareMemory: ", lastKnownLocation.toString());

            if (title.getText().toString().equals("") || description.getText().toString().equals("")) {

                Toast.makeText(PostIt.this, "Title or Description are required", Toast.LENGTH_SHORT).show();

            } else {

                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude);

                if (lastKnownLocation != null) {


                    ParseObject post = new ParseObject("Posts");

                    post.put("postId", postID);
                    post.put("username", ParseUser.getCurrentUser().getUsername());
                    post.put("location", parseGeoPoint);
                    post.put("title", title.getText().toString());
                    post.put("description", description.getText().toString());
                    post.put("shareType", state);
                    if (file != null) {
                        post.put("image", file);
                    }

                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    if (heart) {
                        ParseObject likes = new ParseObject("Likes");

                        likes.put("postOf", ParseUser.getCurrentUser().getUsername());
                        likes.put("shareType", state);
                        likes.put("likedBy", ParseUser.getCurrentUser().getUsername());
                        likes.put("postId", postID);

                        likes.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.i("Likes: ", "Posted");
                                }
                            }
                        });
                    }

                    Intent map = new Intent(this, MapsActivity.class);
                    startActivity(map);
                    finish();


                } else {

                    Toast.makeText(this, "Could not find location. Check GPS Signal.", Toast.LENGTH_SHORT).show();

                }
            }
        }
        else{

        ParseQuery<ParseObject> getPosts = new ParseQuery<>("Posts");

        getPosts.whereEqualTo("postId", postID);

        Log.i("onCreate: ", Integer.toString(postID));

        getPosts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    for (ParseObject object : objects) {

                        title.setText(object.get("title").toString());

                        description.setText(object.get("description").toString());

                        file = object.getParseFile("image");

                        if(file != null){
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

                        });}

                    }
                }
            }

        });

            ParseQuery<ParseObject> getLike = new ParseQuery<ParseObject>("Likes");

            getLike.whereEqualTo("postId", postID);
            getLike.whereEqualTo("likedBy", ParseUser.getCurrentUser().getUsername());

            getLike.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){

                        if(objects.size() >0){

                            Liked(true);

                        }

                    }
                }
            });



        }}




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_it);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

            }
        }

        Bundle extras = getIntent().getExtras();

       location =(LatLng)extras.get("location");

       state = extras.getBoolean("state");

        postID = extras.getInt("postId");

        Log.i("onCreate: ",location.toString()+" State: "+state.toString()+ "PostID: "+postID);


       title = (EditText)findViewById(R.id.Title);

       description = (EditText)findViewById(R.id.content);

       main = (ImageView)findViewById(R.id.mainImage);

       like = (ImageView)findViewById(R.id.like);


        if(postID>0){
            ViewMemory();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode ==1){
            if(grantResults.length >0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){

                Toast.makeText(PostIt.this, "You can add image", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onBackPressed() {

        Intent back = new Intent(PostIt.this,MapsActivity.class);
        startActivity(back);

        super.onBackPressed();
    }
}
