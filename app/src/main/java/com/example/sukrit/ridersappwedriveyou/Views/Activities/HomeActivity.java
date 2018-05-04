package com.example.sukrit.ridersappwedriveyou.Views.Activities;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sukrit.ridersappwedriveyou.Models.Notification;
import com.example.sukrit.ridersappwedriveyou.Models.Rider;
import com.example.sukrit.ridersappwedriveyou.Models.Sender;
import com.example.sukrit.ridersappwedriveyou.Models.Token;
import com.example.sukrit.ridersappwedriveyou.R;
import com.example.sukrit.ridersappwedriveyou.Remote.IFCMService;
import com.example.sukrit.ridersappwedriveyou.Utils.Common;
import com.example.sukrit.ridersappwedriveyou.Utils.CustomInfoWindow;
import com.example.sukrit.ridersappwedriveyou.Views.Fragments.BottomSheetRiderFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    SupportMapFragment mapFragment;

    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference mRidersDb,mOngoingRidesDb;
    GeoFire geoFire;

    Marker mUserMarker;
    ImageView imgExpandable;
    Button btnRequestPickup;
    BottomSheetRiderFragment mBottomSheetFragment;
    public static final String TAG = "DHON";

    boolean isDriverFound = false;
    String driverId = "";
    int radius = 1;
    int distance = 1;
    private static final int LIMIT = 3;

    IFCMService ifcmService;
    String driverToken="";
    String time,address,dist,duration,phoneDriver;

    Double driverLat = 0.0,driverLng = 0.0;
    Double distUserDriver = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ifcmService = Common.getFCMService();

        mOngoingRidesDb = FirebaseDatabase.getInstance().getReference().child("OnGoingRides");

        imgExpandable = findViewById(R.id.expandableBtn);
        btnRequestPickup = findViewById(R.id.btnRequestPickup);
        mBottomSheetFragment = BottomSheetRiderFragment.newInstance("Rider Bottom Sheet");

        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetFragment.show(getSupportFragmentManager(), mBottomSheetFragment.getTag());
            }
        });

        Log.d(TAG, "onCreate: drId"+driverId);
        btnRequestPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+driverId);
                if(!isDriverFound)
                {
                    Log.d(TAG, "reqPic: ");
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Log.d(TAG, "onClick: "+driverId);

                }
                if(isDriverFound) {
                    Log.d(TAG, "sendReq: ");
                    sendRequestToDriver(driverId);
                }

            }
        });


        setUpLocation();

        updateTokenToServer(FirebaseInstanceId.getInstance().getToken());

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

/*
    private void volleyFunction() {
        Log.d(TAG, "volleyFunction: drToken "+driverToken);
       StringRequest stringRequest = new StringRequest(Request.Method.POST,Common.FCM_Url,new Response.Listener<String>(){
           @Override
           public void onResponse(String response) {
               Log.d(TAG, "onResponse: "+response);

           }
       }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
               Log.d(TAG, "onErrorResponse: "+error);
           }
       }){

           @Override
           public String getBodyContentType() {
               return "application/json; charset=utf-8";
           }

           @Override
           public Map<String, String> getHeaders() throws AuthFailureError {
               Map<String, String> params = new HashMap<String, String>();
//               params.put("Content-Type", "application/json; charset=UTF-8");
               params.put("Authorization", "KEY=AAAAv8t6tlE:APA91bHZ9IIEvWHgxDUT0O5bt4vXUjbUnOO_LWfSjnVLrH2kTDElmIjwpFM5zUfxixlpXSG9Kq1QbWLvo7QgbCS2mBOawPUGACzhLPukqqnJXxmnQ24D2edquGFXc8GIsESiPqDEsr97");
               return params;
           }
           @Override
           protected Map<String, String> getParams() {
               Map<String, String> params = new HashMap<>();
               JSONObject jsonObject = new JSONObject();

               try {
                   Log.d(TAG, "getParams: ");
                   jsonObject.put("title","hello");
                   jsonObject.put("text","hi");
                   params.put("to", driverToken);
                   params.put("notification", content.toString()); //the object
                   Log.d(TAG, "getParams: "+params);
               } catch (JSONException e) {
                   e.printStackTrace();
               }
               return params;
           }
       };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
*/

    private void volleyFunction()
    {
        Log.d(TAG, "volleyFunction: "+address);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Common.FCM_Url, getUpdateJsonObject(driverToken), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error);
            }
        }){
            @Override
            public String getPostBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "KEY=AAAAv8t6tlE:APA91bHZ9IIEvWHgxDUT0O5bt4vXUjbUnOO_LWfSjnVLrH2kTDElmIjwpFM5zUfxixlpXSG9Kq1QbWLvo7QgbCS2mBOawPUGACzhLPukqqnJXxmnQ24D2edquGFXc8GIsESiPqDEsr97");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public JSONObject getUpdateJsonObject(String driverToken)
    {
        JSONObject notif = new JSONObject();
        JSONObject obj = new JSONObject();
        try {
            notif.put("title","Pickup point at :"+address);
            notif.put("text","Distance:"+distUserDriver+" , Time:"+duration+" , Address:"+address);
            obj.put("to",driverToken);
            obj.put("notification",notif);
            obj.put("from",FirebaseInstanceId.getInstance().getToken());
            Log.e(TAG,obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getUpdateJsonObject: "+obj);
        return obj;
    }

    private void updateTokenToServer(String refreshedToken) {
        DatabaseReference dbToken = FirebaseDatabase.getInstance().getReference().child(Common.token_tb1);

        Token token = new Token(refreshedToken);
        dbToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }


    private void sendRequestToDriver(final String driverId) {
        Log.d(TAG, "sendRequestToDriver: driverId: "+driverId);

        DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference(Common.token_tb1);
        tokenDB.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.toString());
              //  Token token = dataSnapshot.getValue(Token.class); // Get token object from database with key
                //Log.d(TAG,"TOKEN VALUE IS:"+token.getToken());
                Log.d(TAG, "onDataChange22: "+dataSnapshot.child(driverId).child("token").getValue());
                // make raw payload
                Token token = new Token(dataSnapshot.child(driverId).child("token").getValue().toString());
                String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                Log.d(TAG, "onDataChange: "+json_lat_lng);
                Notification notification = new Notification("Title",json_lat_lng);
                Log.d(TAG, "onDataChange: notif:  "+notification.toString());
                driverToken = dataSnapshot.child(driverId).child("token").getValue().toString();
                Log.d(TAG, "onDataChange: loc: "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                getDirection(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                mOngoingRidesDb.child(driverId)
                               .child("riderId").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mOngoingRidesDb.child(driverId).child("rideStarted").setValue(false);
                mOngoingRidesDb.child(driverId).child("rideConfirmed").setValue(false);
//                volleyFunction();

                Sender senderContent = new Sender(dataSnapshot.child(driverId).child("token").getValue().toString(),notification);
                Log.d(TAG, "onDataChange: send: "+senderContent.getTo()+",\n"+senderContent.getNotification().getTitle()+",\n"+senderContent.getNotification().getBody());
                /*ifcmService.sendMessage(senderContent)
                        .enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                Log.d(TAG, "onResponse: "+response);
                               *//* if(response.body().success==1)
                                {
                                    Toast.makeText(HomeActivity.this, "Request Sent.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(HomeActivity.this, "Failed to sent.", Toast.LENGTH_SHORT).show();
                                }*//*
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                Toast.makeText(HomeActivity.this, "", Toast.LENGTH_SHORT).show();
                            }
                        });*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "driver::: "+driverId);
        DatabaseReference mDriverLocationDetails = FirebaseDatabase.getInstance().getReference().child("Drivers")
                                            .child(driverId);
        mDriverLocationDetails.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChangeDriver: "+dataSnapshot.toString());
                        Log.d(TAG, "onDataChangeDriverLat: "+dataSnapshot.child("l").getValue());
                        String ar = dataSnapshot.child("l").getValue().toString();
                        driverLat = Double.valueOf(ar.substring(ar.indexOf('[')+1,ar.indexOf(',')));
                        driverLng = Double.valueOf(ar.trim().substring(ar.indexOf(',')+1,ar.indexOf(']')));
                        Log.d(TAG, "driverLat: "+driverLat);
                        Log.d(TAG, "driverLng: "+driverLng);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        DatabaseReference mDriverDetails = FirebaseDatabase.getInstance().getReference().child("DriversInformation").child(driverId);
        mDriverDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phoneDriver = dataSnapshot.child("phone").getValue().toString();
                Toast.makeText(HomeActivity.this, phoneDriver, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //DataSnapshot
        // { key = AU3s2RWulFZHaY4el5J6EiCzelc2,
        // value = {.priority=ttp4beutst, g=ttp4beutst, l={1=77.3720996, 0=28.6299693}} }
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequestPickup = FirebaseDatabase.getInstance().getReference()
                .child("Pickup_Requests");
        GeoFire geoFire = new GeoFire(dbRequestPickup);
        Log.d(TAG, "requestPickupHere: " + mLastLocation);

        geoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new
                GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Sent pickup request", Toast.LENGTH_SHORT).show();
                    }
                });

        if (mUserMarker.isVisible()) {
            mUserMarker.remove();
        }
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();
        btnRequestPickup.setText("Getting your driver..");
        findDrivers();
    }

    private void getDirection(final Double lat, final Double lng) {
        Log.d(TAG, "getDirection: ");
        String requestApi = null;
        try {
            requestApi = "https://maps.google.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preferences=less_driving&" +
                    "origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + "AIzaSyDnMWoL7DaisMdDDs2e8h1_gu8cCG-_CQw";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    requestApi, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(TAG, "onResponse: "+response);
                        JSONArray routes = response.getJSONArray("routes");
                        Log.d(TAG, "onResponse: rr: "+routes);
                        JSONObject routesObject = routes.getJSONObject(0);
                        JSONArray legs = routesObject.getJSONArray("legs");
                        Log.d(TAG, "legs: "+legs);
                        JSONObject legsObject = legs.getJSONObject(0);
                        Log.d(TAG, "legsObject: "+legsObject);
                        address = legsObject.getString("start_address");
                        Log.d(TAG, "address: "+address);
                        //dist = legsObject.getJSONObject("distance").getString("text");
                        Log.d(TAG, "onResponse: "+lat+","+lng+","+driverLat+","+driverLng);
                        distUserDriver = Common.getDistance(lat,lng,driverLat,driverLng,"K");
                        Log.d(TAG, "onResponse: "+distUserDriver);
                        duration = legsObject.getJSONObject("duration").getString("text");
                        volleyFunction();
                    }catch (Exception e){}
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: getDir");
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonObjectRequest);

                        /*        Log.d("TAG", "onResponse: "+response.body().toString());
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject routesObject = routes.getJSONObject(0);
                                JSONArray legs = routesObject.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);
                                txtTime.setText(legsObject.getString("time"));
                                txtAddress.setText(legsObject.getString("end_address"));
                                txtDistance.setText(legsObject.getString("distance"));
*/
        } catch (Exception e) {
        }
    }


    private void findDrivers() {
        final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference().child(Common.driver_tb1);
        GeoFire gfDrivers = new GeoFire(drivers);
        GeoQuery gfQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);

        gfQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if Driver found..
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverId = key;
                    sendRequestToDriver(driverId);
                    Log.d(TAG, "onKeyEntered: driverId =  "+driverId);
                    btnRequestPickup.setText("CALL DRIVER");
                    Toast.makeText(HomeActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if Driver not found, increase the radius..
                radius++;
                if (!isDriverFound) {
                    findDrivers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request runtime permission.
            ActivityCompat.requestPermissions(HomeActivity.this
                    , new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST)
                        .show();
            else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("TAG", "displayLocation: " + mLastLocation);
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();
         /*   geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    new GeoLocation(latitude, longitude)
                    , new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            //Add marker
                            if (mUserMarker != null)
                                mUserMarker.remove();
                            mUserMarker = mMap.addMarker(new MarkerOptions()
                                    // .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                    .position(new LatLng(latitude, longitude))
                                    .title("You"));

                            //Move camera to this position.
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

                            // rotateMarker(mCurrent,-360,mMap);
                        }
                    });*/
         
            if (mUserMarker != null)
                mUserMarker.remove();
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    // .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    .position(new LatLng(latitude, longitude))
                    .title("Pickup Here..!"));
            //Move camera to this position.
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                       
            loadAllAvailableDrivers();
        } else {
            //Toast.makeText(this, "ERROR: Cannot give location", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadAllAvailableDrivers() {
        DatabaseReference driverLocations = FirebaseDatabase.getInstance().getReference().child(Common.driver_tb1);
        GeoFire gfDriverLoc = new GeoFire(driverLocations);
        GeoQuery geoQuery = gfDriverLoc.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference().child(Common.user_driver_tb1)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Rider user = dataSnapshot.getValue(Rider.class);
                                mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude,location.longitude))
                                .flat(true)
                                .title(user.getPhone())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance<LIMIT)
                {
                    distance++;
                    loadAllAvailableDrivers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, HomeActivity.this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMap.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
}
