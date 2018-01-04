package com.wlu.android.khan_fark_project;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static java.lang.StrictMath.abs;

public class WelcomeScreenMap extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private static double TEN_KILOMETERS = 0.09009009009;
    private GoogleMap mMap;
    ArrayList<University> mUniversityValues = new ArrayList<University>();
    public static double mLongitude;
    public static double mLatitude;
    ArrayList<Card> mSpots = new ArrayList<>();
    ArrayList<Card> mNearbySpots = new ArrayList<>();
    FirebaseDatabase mFirebaseDatabase;
    ArrayList<Marker> mMarkers = new ArrayList<>();
    private LocationRequest mLocationRequest;
    public static boolean centerer=false;
    public static Deck deck;
    public static boolean ready=false;
    public static boolean permReady=false;
    public static CameraUpdate cu;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    public static SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ready=false;
        Log.d("test","creating again");

        super.onCreate(savedInstanceState);
        if (!checkPermissions()) {
            startLocationPermissionRequest();
            //getLastLocation();
        } else {
            getLastLocation();
        }
        setContentView(R.layout.activity_welcome_screen_map);
        createDeck();
        startLocationUpdates();




        //setActivityBackgroundColor(Color.WHITE);
        //setTextColor(Color.parseColor("#a8e907"));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

       //

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference myPosts = mFirebaseDatabase.getReference("POSTS");
        myPosts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Card carz = postSnapshot.getValue(Card.class);
                    Log.d("main","Card title is: "+carz.getTitle());
                    mSpots.add(carz);
                    // here you can access to name property like university.name

                }
                if(!mSpots.equals(null)) {
                    Log.d("WSM", "Inside" + Integer.toString(mSpots.size()));
                }
                Object value = dataSnapshot.getValue();
                Log.d("main","datasnapshot is:"+value.toString());
                if(value.toString().equals("NONE")){
                    return;
                }

                Log.d(TAG, "Value is: " + value);
                if(mMap!=null) {
                    getNearbySpots();
                    placeMarkers();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myUniversities = mFirebaseDatabase.getReference("UNIVERSITIES");

        myUniversities.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();
                String innerValue = value.toString();
                int location = innerValue.lastIndexOf("}");
                String[] vals = value.toString().substring(innerValue.indexOf("uni-"), location).split("uni-");
                //Hearst={latitude=49.7075, longitude=-83.66544},
                for (String values : vals) {
                    if(!values.equals("")) {
                        String name = values.substring(0, values.indexOf("="));
                        String latitude = values.substring(values.indexOf("latitude=")+9, values.indexOf(","));
                        String longitude = values.substring(values.indexOf("longitude=")+10, values.indexOf("}"));
                        University uni = new University(name, Double.parseDouble(latitude), Double.parseDouble(longitude));
                        mUniversityValues.add(uni);
                    }
                }
                Log.d(TAG, "Value is: " + value);
                TextView tvUni = (TextView)findViewById(R.id.tvUniversity);
                tvUni.setText(tvUni.getText() + getNearestUniversity());
                //tvUni.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
                tvUni.setGravity(Gravity.CENTER_HORIZONTAL);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });






        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);





    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     *      *   private static final LatLng MELBOURNE = new LatLng(-37.813, 144.962);
     *private Marker melbourne = mMap.addMarker(new MarkerOptions()
     *.position(MELBOURNE)
     *.title("Melbourne")
     *.snippet("Population: 4,137,400")
     *.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
     *--- example of changing the icon for each marker.
     *
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

//        mMap.setOnMarkerClickListener(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mMap = googleMap;
                mMap.setOnMarkerClickListener(this);
                mMap.setMyLocationEnabled(true);
                getNearbySpots();
                placeMarkers();
            } else {
                //Request Location Permission
                //checkLocationPermission();
            }
        }
        else {
            //buildGoogleApiClient();
            mMap = googleMap;
            mMap.setOnMarkerClickListener(this);
            mMap.setMyLocationEnabled(true);
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        if (checkPermissions()) {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

        } else {
            return;
        }

 //       getNearbySpots();
//        placeMarkers();
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        for(Marker curMarker : mMarkers){
            if(curMarker.getPosition().equals(marker.getPosition())){
                for(Card card : mSpots){
                    LatLng curCard = new LatLng(card.getLatitude(),card.getLongitude());

                    if(card.getTitle().equals(marker.getTitle())){
                        FragmentManager fragmentManager = getSupportFragmentManager();

                        Intent intent = CardPagerActivity.newIntent(this, card.getId(),true, card.getImage());
                        //CardFragment.newInstance(card.getId());
                        startActivity(intent);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public String getNearestUniversity(){
        // Get the location manager
        double lat,lon;
        double min = 0;
        University selected = new University("Laurier",43.4724,-80.5263);
        try {
            for(University uni : mUniversityValues){
                Log.d("main","Latuide is"+mLatitude+" "+mLongitude);
                double distance = calculateDistance(mLatitude,mLongitude,uni.getLatitude(),uni.getLongitude());
                if(min > distance){
                    min = distance;
                    selected = uni;
                }
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
        return selected.getName();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_PERMISSIONS_REQUEST_CODE);


    }

    public void placeMarkers(){
        Log.d("WSM","placeMarkers"+Integer.toString(mSpots.size()));
        //Set Camera to current location
        Log.d("main","OK SO THIS IS THE LOCATION:"+mLongitude+" "+mLatitude);
        LatLng curLocation = new LatLng(mLatitude, mLongitude);
        if(permReady==false){
            Log.d("test","Not this time");
            permReady=true;
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 12.0f));

        }
        Log.d("WSM","Got here" + Integer.toString(mNearbySpots.size()));



        for(Card curCard : mNearbySpots){
            Log.d("WSM","Placing markers");
            LatLng curSpot = new LatLng(curCard.getLatitude(), curCard.getLongitude());
            mMarkers.add(mMap.addMarker(new MarkerOptions().position(curSpot).title(curCard.getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.spottedmarker))));
        }

        if(mNearbySpots.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLng curPoint = new LatLng(mLatitude,mLongitude);
            builder.include(curPoint);
            for (Marker marker : mMarkers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if(mMap!=null) {
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.animateCamera(cu);                    }
                });


            }
        }
    }

    public void getNearbySpots(){
        int min = 10;
        Log.d("WSM","GetNearbySpots"+Integer.toString(mSpots.size()));

        try {
            for(Card curCard : mSpots){
                if((abs(mLatitude-curCard.getLatitude()))<TEN_KILOMETERS || (abs(mLongitude-curCard.getLongitude()))<TEN_KILOMETERS) {
                    double distance = calculateDistance(mLatitude, mLongitude, curCard.getLatitude(), curCard.getLongitude());
                    if (min > distance) {
                        Log.d("WSM","Got nearby spots");
                        mNearbySpots.add(curCard);
                    }
                }
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        if(mMap!=null) {
            placeMarkers();
        }
    }


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   \public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        Location loc= locationResult.getLastLocation();

                        mLatitude=loc.getLatitude();
                        mLongitude=loc.getLongitude();
                        onLocationChanged(locationResult.getLastLocation());
                        if(centerer==false) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 12.0f));
                            centerer=true;
                        }
                    }
                },
                Looper.myLooper());
    }


    public void getLastLocation() {
        Log.d("test","We are here");
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        mLatitude=location.getLatitude();
        mLongitude=location.getLongitude();

        Log.d("main","location change is "+msg);
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }


    public Double calculateDistance(double lat1, double lon1, double lat2, double lon2){
        double earthRadius = 6371;
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double latDiff = Math.toRadians((lat2 - lat1));
        double longDiff =  Math.toRadians((lon2 - lon1));

        double a = Math.sin(latDiff/2)*Math.sin(latDiff/2) + Math.cos(rLat1)*Math.cos(rLat2)*Math.sin(longDiff/2)*Math.sin(longDiff/2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double distance = earthRadius*c;
        return distance;
    }

    public void showRecycleView(View view){
        Intent intent = new Intent(this,CardListActivity.class);
        //intent.putExtra();
        startActivity(intent);
    }

    public void centerCamera(View view){
        if(mNearbySpots.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : mMarkers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if(mMap!=null) {
                mMap.animateCamera(cu);
            }
        }
    }

    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    public void setTextColor(int color){
        TextView tv = (TextView) findViewById(R.id.tvUniversity);
        tv.setTextColor(color);
    }


    public void createDeck(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("POSTS");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Card carz = postSnapshot.getValue(Card.class);
                    Log.d("main","Card title is: "+carz.getTitle());
                    deck.get(getApplicationContext()).addCard(carz);
                    // here you can access to name property like university.name
                }
                Object value = dataSnapshot.getValue();
                Log.d("main","datasnapshot is:"+value.toString());
                if(value.toString().equals("NONE")){
                    return;
                }

                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}
