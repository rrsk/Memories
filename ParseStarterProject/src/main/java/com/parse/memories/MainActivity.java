/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.memories;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

//import static com.google.android.gms.analytics.internal.zzy.t;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

  Boolean signUpActive;// = true;
  TextView tv;// = (TextView) findViewById(R.id.changeModeText);
  Button sighupBtn;// = (Button) findViewById(R.id.signBtn);
  EditText pass ;//= (EditText) findViewById(R.id.passEdit);

  public void startUserList(){
      Intent i = new Intent(getApplicationContext() , MapsActivity.class);

      startActivity(i);
      finish();

  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {

    if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
       signUp(v);
    }

    return false;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.changeModeText) {
      Log.i("App Info", "Change mode");


      //Button sighupBtn = (Button) findViewById(R.id.signBtn);

      if (signUpActive) {

        signUpActive = false;
        sighupBtn.setText("Login");
        tv.setText("Or, Signup");
      } else {
        signUpActive = true;
        sighupBtn.setText("Signup");
        tv.setText("Or, Login");
      }
    }else if(v.getId() == R.id.background || v.getId() ==R.id.logo){

      InputMethodManager inputmanager =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

      inputmanager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
  }



  public void  signUp(View v){

    EditText useranme = (EditText) findViewById(R.id.userEdit);


    if(useranme.getText().toString().matches("") || pass.getText().toString().matches("")){
      Toast.makeText(this, "A username and password are required", Toast.LENGTH_SHORT).show();
    }
    else{

      if(signUpActive) {
        ParseUser user = new ParseUser();

        user.setUsername(useranme.getText().toString());
        user.setPassword(pass.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
          @Override
          public void done(ParseException e) {
            if (e == null) {
              Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
              Log.i("Signup", "Done");
              startUserList();
            } else {
              Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          }
        });
      }else{

        ParseUser.logInInBackground(useranme.getText().toString(), pass.getText().toString(), new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException e) {
            if(user != null){
              Log.i("Signup", "Login Successful");
              Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
              startUserList();
            }else{

              Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
          }
        });
      }

    }

  }



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setTitle("Memories");

    RelativeLayout background = (RelativeLayout) findViewById(R.id.background);

    ImageView iv = (ImageView)findViewById(R.id.logo);

    background.setOnClickListener(this);
    iv.setOnClickListener(this);


    signUpActive = true;

    tv = (TextView) findViewById(R.id.changeModeText);

    sighupBtn = (Button) findViewById(R.id.signBtn);

    pass = (EditText) findViewById(R.id.passEdit);

    pass.setOnKeyListener(this);

    tv.setOnClickListener(this);

    if(ParseUser.getCurrentUser() != null)
    {
      startUserList();
    }

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }


}