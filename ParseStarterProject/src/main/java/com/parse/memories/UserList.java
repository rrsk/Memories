package com.parse.memories;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import static com.google.android.gms.analytics.internal.zzy.f;
//import static com.google.android.gms.analytics.internal.zzy.l;
//import static com.google.android.gms.analytics.internal.zzy.t;
import static com.parse.ParseUser.logOutInBackground;

public class UserList extends AppCompatActivity {

    public void getPhoto() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode ==1){
            if(grantResults.length >0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){

                getPhoto();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.share_menu,menu);

        return super.onCreateOptionsMenu(menu);

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()== R.id.share){

            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},1);

            }else{
                getPhoto();
            }

        }

        if(item.getItemId() == R.id.logOut){

            if(ParseUser.getCurrentUser() != null)

                ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {

                    if(e == null){
                        Toast.makeText(UserList.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    }



                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode== RESULT_OK && data !=null){

            Uri image = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);

                Log.i("Image", "Recieved");
                Toast.makeText(this, "Recieved", Toast.LENGTH_SHORT).show();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);

                byte[] byteArray = stream.toByteArray();

                ParseFile file = new ParseFile("image.png", byteArray);

                ParseObject object = new ParseObject("Image");

                object.put("image",file);

                object.put("username",ParseUser.getCurrentUser().getUsername());

                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if( e == null){

                            Toast.makeText(UserList.this, "Image shared", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(UserList.this, "Not shared- try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        final ListView userList = (ListView) findViewById(R.id.users);

        final ArrayList<String> usernames = new ArrayList<String>();

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);

        ParseQuery<ParseUser> q = ParseUser.getQuery();

        q.whereNotEqualTo("username",ParseUser.getCurrentUser().getUsername());

        q.addAscendingOrder("username");

        q.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {

                if(e ==null) {


                    if (objects.size() > 0) {
                        for (ParseUser user : objects) {
                            usernames.add(user.getUsername());
                        }
                    }
                }
                else{
                    e.printStackTrace();
                }

                userList.setAdapter(arrayAdapter);

            }
        });




    }
}
