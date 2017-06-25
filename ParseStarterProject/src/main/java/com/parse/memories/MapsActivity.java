package com.parse.memories;

import android.content.Intent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import static com.parse.memories.R.id.seekBar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {




    private GoogleMap mMap;
    Marker setup;
    LatLng p;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker, pointing;
    LocationRequest mLocationRequest;
    String currentUser;
    SeekBar simpleSeekBar;//=(SeekBar) findViewById(R.id.simpleSeekBar);



    Button shareButton;

    //Boolean share = false;

    Switch s ;

    long min = 1493894132;

    Boolean state = false;

    LatLng userLocation;



    public void openShare(View view) {

        Log.i("Info", "Memory");

        state = s.isChecked();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LatLng lastKnownLocation = setup.getPosition();
                    String[] s = pointing.getTag().toString().split("_");



                    if(pointing.getTag().equals("new")){

                    Intent i = new Intent(this.getApplicationContext(),PostIt.class);

                    i.putExtra("location",lastKnownLocation);
                    i.putExtra("state",state);
                        i.putExtra("postId",0);
                    startActivity(i);
                        finish();
                    }

                    else if(s[0].equals("people")){

                        Intent i = new Intent(this.getApplicationContext(),Display.class);

                        i.putExtra("location",lastKnownLocation);
                        i.putExtra("state",state);
                        i.putExtra("postId",s[1]);
                        startActivity(i);
                        finish();

                    }else{

                        Intent i = new Intent(this.getApplicationContext(),PostIt.class);

                        i.putExtra("location",lastKnownLocation);
                        i.putExtra("state",state);
                        i.putExtra("postId",Integer.parseInt(s[1]));
                        startActivity(i);
                        finish();

                    }


                } else {

                    Toast.makeText(this, "Could not find location. Please try again later.", Toast.LENGTH_SHORT).show();

                }
            }




    public void markLocations(final boolean state){



        mMap.clear();

        setup = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are Here!").draggable(true));
        setup.setTag("new");

        Location location = mLastLocation;

        if( location != null){

            ParseQuery<ParseObject> q = new ParseQuery<>("Posts");

            //Location lastKnownLocation = mLastLocation;

            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            Log.i("markLocations : " , parseGeoPoint.toString());

            q.whereNear("location",parseGeoPoint);

            q.whereEqualTo("shareType",state);

            q.setLimit(15);




            q.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {



                    if(e == null){

                        if(objects.size() > 0){

                            //Log.i("markLocations : " ,Integer.toString(objects.size()));

                            currentUser = ParseUser.getCurrentUser().toString();

                            for ( ParseObject object : objects){

                                Log.i("markLocations: ",Long.toString(System.currentTimeMillis()/1000l -simpleSeekBar.getProgress()));
                                Log.i("markLocations OBJECT: ",Long.toString(object.getCreatedAt().getTime()/1000l));

                                long time = object.getCreatedAt().getTime()/1000l;

                                if(time < (System.currentTimeMillis()/1000l -simpleSeekBar.getProgress())) {

                                    if (!object.get("username").toString().equals(ParseUser.getCurrentUser().get("username").toString()) && !state) {

                                        LatLng l = new LatLng(object.getParseGeoPoint("location").getLatitude(), object.getParseGeoPoint("location").getLongitude());
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(l);
                                        markerOptions.title(object.get("username").toString() + " " + object.getCreatedAt().toString());
                                        Log.i("done:", object.get("username").toString() + object.getCreatedAt().toString());
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                        mCurrLocationMarker = mMap.addMarker(markerOptions);
                                        mCurrLocationMarker.setTag("people_" + object.get("postId").toString());

                                    } else if (object.get("username").toString().equals(ParseUser.getCurrentUser().get("username").toString())) {
                                        Log.i("markLocations : ", "In People");
                                        LatLng l = new LatLng(object.getParseGeoPoint("location").getLatitude(), object.getParseGeoPoint("location").getLongitude());
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(l);
                                        markerOptions.title(object.get("username").toString() + " " + object.getCreatedAt().toString());
                                        Log.i("done:", object.get("username").toString() + object.getCreatedAt().toString());
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                        mCurrLocationMarker = mMap.addMarker(markerOptions);
                                        mCurrLocationMarker.setTag("edit_" + object.get("postId").toString());
                                    }

                                }


                            }

                        }


                    }

                }
            });

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        shareButton = (Button) findViewById(R.id.shareMem);
        simpleSeekBar = (SeekBar) findViewById(seekBar);

        s = (Switch) findViewById(R.id.switch1);

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    state = true;
                    markLocations(state);
                } else {
                    state = false;
                    markLocations(state);
                }
            }
        });

        min = 1493914132;



        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                markLocations(state);

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }



    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        Log.i("onLocationChanged: ",location.toString());

        //Current marker
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());


        markLocations(s.isChecked());

        //Place current location marker
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(userLocation);
        markerOptions.title("Current Position");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);*/

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }




    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inf = getMenuInflater();

        inf.inflate(R.menu.share_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.help:
                Log.i("onOptionsItemSelected: ","share");
                startActivity(new Intent(this, Home.class));
                return true;
            case R.id.logOut:
                ParseUser.logOut();
                //shareActive = false;
                //shareButton.setText("Share Memory");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            case R.id.chatting:
                startActivity(new Intent(this, Chat.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        p = setup.getPosition();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.showInfoWindow();

        Log.i("onMarkerClick: ", marker.getTitle() + marker.getPosition());

        String[] s = marker.getTag().toString().split("_");

        if(marker.isInfoWindowShown() && (s[0] == null||s[0].equals("new"))){

            pointing =marker;
            shareButton.setText("New Memory");

            shareButton.setVisibility(View.VISIBLE);

        }else if(marker.isInfoWindowShown() && s[0].equals("edit")){

            pointing =marker;

            shareButton.setText("Edit Memory");

            shareButton.setVisibility(View.VISIBLE);

        }else{

            pointing =marker;

            shareButton.setText("Read Memory");

            shareButton.setVisibility(View.VISIBLE);

        }

        return false;
    }



}