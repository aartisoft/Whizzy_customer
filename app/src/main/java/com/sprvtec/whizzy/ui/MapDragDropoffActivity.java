package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.CustomMapFragment;
import com.sprvtec.whizzy.util.MapWrapperLayout;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.vo.AddAddress;
import com.sprvtec.whizzy.vo.Business;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.SavedAddress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


public class MapDragDropoffActivity extends FragmentActivity implements MapWrapperLayout.OnDragListener, OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final int LOCATION_REQUEST = 100;
    private AlertDialog internetAlertDialog;
    // Google Map
    private GoogleMap googleMap;

    private View mMarkerParentView;
    private ImageView mMarkerImageView, searchIcon;

    private int centerX = -1;
    private int centerY = -1;

    private TextView address, locationSearch, locationText;
    private String completeAddress;
    private Button submit;
    private double latitude = 0, longitude = 0;


    private static final String TAG = "MapDragAct";
    private Point p1;
    private LocationInf pickUpLocationInfo, dropOffLocationInfo, tempLocationInfo;
    private int wayType = 0;

    //geofire stuff
    private static final String GEO_FIRE_DB = "https://whizzydevelopment.firebaseio.com";
    private static final String GEO_FIRE_REF_TWO_WHEELER = GEO_FIRE_DB + "/two_wheelers";
    private GeoFire geoFireTwo;
    private GeoQuery geoQueryTwo;
    private Map<String, Marker> markersTwo;
    private Utils utils;
    private DatabaseReference mFirebaseLatLongDb;
    private long servingDistance;
    private double servingLatitude, servingLongitude;
    String str_buy;
    Dialog dialog1;

    private Location currentLocation;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    private boolean singleTime = true;
    Dialog locationDialog;
    private View mLayout;
    private boolean firstTime = true, searchFirstTime;
    private LinearLayout loadingLay, layPickDrop;
    private EditText landmark;
    private String pickLandmark = "", dropLandmark = "";
    Business business;
    private int FROM_BACK = 111;

    //new locaion services stuff
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean started;

    private RadioGroup radioGroup;
    private String label = "";
    private RelativeLayout othersLay;
    private EditText labelEdit;
    private LinearLayout addressLay;

    private DatabaseReference connectedRef;
    private ValueEventListener networkListner;

    //    private List<SavedAddress> savedAddresses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map_drag_drop);
        utils = new Utils();
        locationServiceStuff();
        loadingLay = findViewById(R.id.loading);
        addressLay = findViewById(R.id.address_lay);
        loadingLay.setEnabled(false);
        loadingLay.setVisibility(View.VISIBLE);
        layPickDrop = findViewById(R.id.lay_pic_drop);
        landmark = findViewById(R.id.landmark);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        locationDialog = new Dialog(this);
        mLayout = findViewById(R.id.main);
        othersLay = findViewById(R.id.others_lay);
        labelEdit = findViewById(R.id.label);
        radioGroup = findViewById(R.id.radio_group);
        business = getIntent().getParcelableExtra(Constants.IntentKey.BUSINESS);


        findViewById(R.id.cancel).setOnClickListener(v -> {
            Log.e("onclick", "onclick");
            othersLay.setVisibility(View.GONE);
            radioGroup.setVisibility(View.VISIBLE);
            radioGroup.clearCheck();
            label = "";
            labelEdit.setText("");
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.home:
                    label = "Home";
//                    if (submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.continue1)) || submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.save_continue))) {
//                        submit.setText(getApplicationContext().getResources().getString(R.string.save_continue));
//                    }
                    break;
                case R.id.work:
                    label = "Work";
//                    if (submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.continue1)) || submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.save_continue))) {
//                        submit.setText(getApplicationContext().getResources().getString(R.string.save_continue));
//                    }
                    break;
                case R.id.others:
                    label = "";
                    radioGroup.setVisibility(View.GONE);
                    othersLay.setVisibility(View.VISIBLE);
//                    if (submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.continue1)) || submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.save_continue))) {
//                        submit.setText(getApplicationContext().getResources().getString(R.string.save_continue));
//                    }
                    break;
                case -1:
                    othersLay.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.VISIBLE);
                    label = "";
                    labelEdit.setText("");
//                    if (submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.continue1)) || submit.getText().toString().equals(getApplicationContext().getResources().getString(R.string.save_continue))) {
//                        submit.setText(getApplicationContext().getResources().getString(R.string.continue1));
//                    }
                    break;
            }
        });
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDatabaseAddress = mFirebaseInstance.getReference("My_Saved_Addresses");
        String userID = sp.getString(Constants.PreferenceKey.KEY_USER_ID, "");
//        if (!userID.equals(""))
//            mFirebaseDatabaseAddress.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    savedAddresses = new ArrayList<>();
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        SavedAddress address = postSnapshot.getValue(SavedAddress.class);
//                        address.id = postSnapshot.getKey();
//                        savedAddresses.add(address);
//                        Log.e("Get Data", address.Label);
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        if (getIntent().getStringExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK) != null)
            pickLandmark = getIntent().getStringExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();

        }
        findViewById(R.id.my_saved_addresses).setOnClickListener(v -> {
            Intent intent = new Intent(MapDragDropoffActivity.this, SavedAddressesActivity.class);
            intent.putExtra(Constants.IntentKey.FROM_SEARCH, true);
            startActivityForResult(intent, 3);
        });

        str_buy = sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "");
        pickUpLocationInfo = getIntent().getParcelableExtra(Constants.IntentKey.KEY_PICKUP_LOCATION);
        if (pickUpLocationInfo == null && business != null && !business.name.equals("")) {
            pickUpLocationInfo = new LocationInf();
            pickUpLocationInfo.locationName = business.name;
            pickUpLocationInfo.locationAddress = business.address;
            pickUpLocationInfo.latitude = business.latitude;
            pickUpLocationInfo.longitude = business.longitude;
        }

        // get reference to 'orders' node
        mFirebaseLatLongDb = mFirebaseInstance.getReference("Reference_Location");

        mFirebaseLatLongDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                servingDistance = ((Number) (dataSnapshot.child("distance").getValue())).longValue();
                servingLatitude = ((Number) (dataSnapshot.child("latitude").getValue())).doubleValue();
                servingLongitude = ((Number) (dataSnapshot.child("longitude").getValue())).doubleValue();
                Log.e("LatLong serving", servingLatitude + " " + servingLongitude + " dis " + servingDistance);
                if (googleMap != null)
                    zoomMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // InitializeUI
        address = findViewById(R.id.address);

        searchIcon = findViewById(R.id.icon1);
        submit = findViewById(R.id.submit);
        submit.setEnabled(false);
        submit.setBackgroundResource(R.drawable.button_grayout);
        locationSearch = findViewById(R.id.location_search);
        locationText = findViewById(R.id.location_text);

        findViewById(R.id.back).setOnClickListener(v -> {
            Intent intent = getIntent();
            intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickUpLocationInfo);
            intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickLandmark);
            setResult(RESULT_OK, intent);
            finish();
        });
        submit.setOnClickListener(v -> {
            submit.setEnabled(false);
            if (!address.getText().toString().equalsIgnoreCase("")) {


                dropOffLocationInfo = tempLocationInfo;
                dropLandmark = landmark.getText().toString();
                Log.e("LATLONGPCDROP", pickUpLocationInfo.latitude + " " + dropOffLocationInfo.latitude + " " + pickUpLocationInfo.longitude + " " + dropOffLocationInfo.longitude);
                if (!(pickUpLocationInfo.latitude == dropOffLocationInfo.latitude && pickUpLocationInfo.longitude == dropOffLocationInfo.longitude)) {


                    if (radioGroup.getCheckedRadioButtonId() != -1) {
                        if (radioGroup.getCheckedRadioButtonId() == R.id.others)
                            label = labelEdit.getText().toString().trim();
                        if (!label.equals("")) {
                            boolean duplicate = false;
                            for (SavedAddress addr : OptionsActivity.savedAddresses)
                                if (addr.Label.equalsIgnoreCase(label)) {
                                    duplicate = true;
                                    break;
                                }
                            if (!duplicate) {
                                AddAddress address = new AddAddress();
                                address.Label = label;
                                address.Address = dropOffLocationInfo.locationName + "\n" + dropOffLocationInfo.locationAddress;
                                address.Landmark = dropLandmark;
                                address.Latitude = dropOffLocationInfo.latitude;
                                address.Longitude = dropOffLocationInfo.longitude;
                                address.ContactName = "";
                                address.ContactNumber = "";
                                mFirebaseDatabaseAddress.child(userID).push().setValue(address, (databaseError, databaseReference) -> {
                                    if (databaseError != null) {
                                        submit.setEnabled(true);
                                        Utils.showDialogFailure(MapDragDropoffActivity.this, "Database error. Please try again");
                                    } else {
                                        SavedAddress addressSaved = new SavedAddress();
                                        addressSaved.Label = label;
                                        addressSaved.Address = address.Address;
                                        addressSaved.Landmark = address.Landmark;
                                        addressSaved.Latitude = address.Latitude;
                                        addressSaved.Longitude = address.Longitude;
                                        addressSaved.ContactName = address.ContactName;
                                        addressSaved.ContactNumber = address.ContactNumber;
                                        OptionsActivity.savedAddresses.add(addressSaved);
                                        Intent intent = new Intent(MapDragDropoffActivity.this, VehiclesActivity.class);
                                        intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickUpLocationInfo);
                                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffLocationInfo);
                                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
                                        intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickLandmark);
                                        intent.putExtra(Constants.IntentKey.KEY_WAY_TYPE, wayType);
                                        intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                                        intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
                                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
                                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                                        intent.putExtra(Constants.IntentKey.BUSINESS, business);
                                        startActivityForResult(intent, FROM_BACK);
                                        Toast.makeText(MapDragDropoffActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                        othersLay.setVisibility(View.GONE);
                                        radioGroup.setVisibility(View.VISIBLE);
                                        radioGroup.clearCheck();
                                        label = "";
                                        labelEdit.setText("");
                                        addressLay.setVisibility(View.GONE);
                                        submit.setEnabled(true);
                                    }
                                });
                            } else {
                                submit.setEnabled(true);
                                Utils.showDialogFailure(MapDragDropoffActivity.this, "An address with Label " + "\"" + label + "\"" + " already exists");
                            }
                        } else {
                            submit.setEnabled(true);
                            Utils.showDialogFailure(MapDragDropoffActivity.this, "Please select address label");
                        }


                    } else {
                        Intent intent = new Intent(MapDragDropoffActivity.this, VehiclesActivity.class);
                        intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickUpLocationInfo);
                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffLocationInfo);
                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
                        intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickLandmark);
                        intent.putExtra(Constants.IntentKey.KEY_WAY_TYPE, wayType);
                        intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                        intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
                        intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                        intent.putExtra(Constants.IntentKey.BUSINESS, business);
                        startActivityForResult(intent, FROM_BACK);
                        submit.setEnabled(true);
                    }

                } else {
                    submit.setEnabled(true);
                    Utils.showDialogFailure(MapDragDropoffActivity.this, "Pickup and drop locations cannot be same");
                }
            } else {
                submit.setEnabled(true);
                utils.showDialog("Please select proper location", MapDragDropoffActivity.this);
            }
        });
        locationSearch.setOnClickListener(v -> {

            Intent intent = new Intent(MapDragDropoffActivity.this, LocationSearchActivity.class);
            startActivityForResult(intent, 3);

        });
        initializeUI();

        geofireStuff();
        firebaseConnection();
    }

    private void firebaseConnection() {
        dialog1 = new Dialog(MapDragDropoffActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.alert_network_spinner);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);
//        dialog1.setCancelable(false);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        networkListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    Log.e("Connected", "internet");
                    if (dialog1.isShowing())
                        dialog1.dismiss();
                } else {
                    if (!MapDragDropoffActivity.this.isFinishing()) {
                        dialog1.show(); //show dialog
                    }
                    Log.e("DisConnected", "internet");
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Listener was cancelled");
            }
        };
        connectedRef.addValueEventListener(networkListner);
    }

    private void geofireStuff() {
        //geofire stuff
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://whizzydevelopment.firebaseio.com")
                .setApiKey("AIzaSyD1gphuuMVqUwFJmEn1znQ_s74S-xpAyic")
                .setApplicationId("whizzydevelopment").build();
        FirebaseApp app;
        try {
            app = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions,
                    "whizzydevelopment");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            app = FirebaseApp.initializeApp(this);
        }
        // setup GeoFire
        this.geoFireTwo = new GeoFire(FirebaseDatabase.getInstance(app).getReferenceFromUrl(GEO_FIRE_REF_TWO_WHEELER));

        // setup markers
        this.markersTwo = new HashMap<>();
    }

    private void updateGeofireQueries(Location location) {
        if (this.geoQueryTwo != null) {
            this.geoQueryTwo.removeAllListeners();
            for (Marker marker : this.markersTwo.values()) {
                marker.remove();
            }
            this.markersTwo.clear();
        }
        this.geoQueryTwo = this.geoFireTwo.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 50);
        this.geoQueryTwo.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.e(TAG, "onKeyEntered");
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_two);
//                Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(location.latitude, location.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(b));

                Marker marker = googleMap.addMarker(markerOptions);
//        Marker marker = this.mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))).setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
                markersTwo.put(key, marker);
            }

            @Override
            public void onKeyExited(String key) {
                // Remove any old marker
                Marker marker = markersTwo.get(key);
                Log.e(TAG, "onKeyExited");
                if (marker != null) {
                    marker.remove();
                    markersTwo.remove(key);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.e(TAG, "onKeyMoved");

                Marker marker = markersTwo.get(key);
                if (marker != null) {
                    Location oldLocation = new Location("");
                    oldLocation.setLatitude(marker.getPosition().latitude);
                    oldLocation.setLongitude(marker.getPosition().longitude);
                    Location newLocation = new Location("");
                    newLocation.setLatitude(location.latitude);
                    newLocation.setLongitude(location.longitude);
                    float distanceInMetersOne = oldLocation.distanceTo(newLocation);
                    Log.e("Distance is: ", distanceInMetersOne + " distance");
                    if (marker != null && distanceInMetersOne > 10) {
                        animateMarkerTo(marker, location.latitude, location.longitude);
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                new AlertDialog.Builder(MapDragDropoffActivity.this)
                        .setTitle("Error")
                        .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }

    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        Location prevLoc = new Location("");
        prevLoc.setLatitude(startPosition.latitude);
        prevLoc.setLongitude(startPosition.longitude);
        Location newLoc = new Location("");
        newLoc.setLatitude(lat);
        newLoc.setLongitude(lng);
        final float bearing = prevLoc.bearingTo(newLoc);
        final float startRotation = marker.getRotation();
        marker.setFlat(true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));
                //smooth car rotation
                float rot = t * bearing + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void initializeUI() {

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mMarkerParentView = findViewById(R.id.marker_view_incl);
        mMarkerImageView = findViewById(R.id.marker_icon_view);
        mMarkerImageView.setImageResource(R.drawable.green_marker);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int imageParentWidth = mMarkerParentView.getWidth();
        int imageParentHeight = mMarkerParentView.getHeight();
        int imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }

    private void initilizeMap() {
        if (googleMap == null) {
            CustomMapFragment mCustomMapFragment = ((CustomMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            mCustomMapFragment.setOnDragListener(MapDragDropoffActivity.this);
//            googleMap = mCustomMapFragment.getMap();
            mCustomMapFragment.getMapAsync(this);
            // check if map is created successfully or not

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void zoomMap() {
//        loadingLay.setVisibility(View.GONE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        if (currentLocation != null) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            Log.e(TAG, "latlang: " + latLng);
//            updateLocation(latLng);
        }
    }


    @Override
    public void onDrag(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            p1 = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Point p2 = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
            double dist = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
            Log.e("Distance", dist + " distance");
        }
    }

    private void getMapCenterPoint() {
        Projection projection = (googleMap != null && googleMap
                .getProjection() != null) ? googleMap.getProjection()
                : null;
        //
        if (projection != null) {
            LatLng centerLatLng = projection.fromScreenLocation(new Point(
                    centerX, centerY));
            latitude = centerLatLng.latitude;
            longitude = centerLatLng.longitude;
            Log.e("CenterLatLong", centerLatLng.latitude + " " + centerLatLng.longitude);
            if (!searchFirstTime) {
                addressLay.setVisibility(View.VISIBLE);
                updateLocation(centerLatLng);

                othersLay.setVisibility(View.GONE);
                radioGroup.setVisibility(View.VISIBLE);
                radioGroup.clearCheck();
                label = "";
                labelEdit.setText("");
            }
            searchFirstTime = false;


        }
    }

    @SuppressLint("StaticFieldLeak")
    class getLocationFromAddressTask extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        LatLng latLng;

        getLocationFromAddressTask(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(String... params) {

            try {
                jsonObject = getLocationFormGoogle(latLng);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadingLay.setVisibility(View.GONE);
            getLatLng(jsonObject);
        }
    }

    private void updateLocation(LatLng centerLatLng) {
        latitude = centerLatLng.latitude;
        longitude = centerLatLng.longitude;

        if (centerLatLng != null) {
            Geocoder geocoder = new Geocoder(MapDragDropoffActivity.this,
                    Locale.getDefault());

            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(centerLatLng.latitude,
                        centerLatLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
                if (!Utils.isNetConnected(MapDragDropoffActivity.this) && !locationDialog.isShowing())
                    Utils.showDialog(MapDragDropoffActivity.this);
                else {
                    new getLocationFromAddressTask(centerLatLng).execute();
                    return;
                }
            }

            if (addresses != null && addresses.size() > 0) {
                String k = addresses.get(0).getSubLocality();
                Log.e(TAG, "latitude and longiude: " + k);


                String addressIndex0 = addresses
                        .get(0).getAddressLine(0);
                Log.e(TAG, "addressIndex0: " + addressIndex0);

                String addressIndex1 = addresses
                        .get(0).getAddressLine(1);
                Log.e(TAG, "addressIndex1: " + addressIndex1);

                completeAddress = "";
                if (addressIndex1 != null)
                    completeAddress = addressIndex0 + ",\n" + addressIndex1;
                else
                    completeAddress = addressIndex0;

                if (completeAddress != null) {
                    String finaladresss = completeAddress;
                    address.setText(finaladresss);
                    tempLocationInfo = new LocationInf();
                    tempLocationInfo.setLatitude(latitude);
                    tempLocationInfo.setLongitude(longitude);
                    tempLocationInfo.setLocationAddress(finaladresss);
                    try {
                        tempLocationInfo.setLocationName(finaladresss.split(",")[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateButton(latitude, longitude);
            } else
                showDialogOpps(MapDragDropoffActivity.this);

        } else
            showDialogOpps(MapDragDropoffActivity.this);

    }

    public JSONObject getLocationFormGoogle(LatLng latlong) {
//        String url = "https://maps.google.com/maps/api/geocode/json?latlng=" + latlong.latitude + "," + latlong.longitude + "&sensor=true%22&key=AIzaSyCeVDzYNYJe7V_vEeiZQu2Sngs-UBQgr7g";
        String url = "https://maps.google.com/maps/api/geocode/json?latlng=" + latlong.latitude + "," + latlong.longitude + "&sensor=true%22&key=" + Constants.GOOGLE_API_KEY;
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();
        Log.e("url", url);
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException ignored) {
        } catch (IOException ignored) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }
        Log.e("json", jsonObject + "");
        return jsonObject;
    }

    public void getLatLng(JSONObject jsonObject) {
        if (jsonObject != null) {

            String fullAddress = null;

            try {
                fullAddress = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getString("formatted_address");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            completeAddress = fullAddress;
            if (completeAddress != null) {
                String finaladresss = completeAddress;
                address.setText(finaladresss);
                tempLocationInfo = new LocationInf();
                tempLocationInfo.setLatitude(latitude);
                tempLocationInfo.setLongitude(longitude);
                tempLocationInfo.setLocationAddress(finaladresss);
                try {
                    tempLocationInfo.setLocationName(finaladresss.split(",")[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateButton(latitude, longitude);
            } else {
                showDialogOpps(MapDragDropoffActivity.this);
            }
        } else {
            showDialogOpps(MapDragDropoffActivity.this);
        }

    }


    private void updateButton(double latitude, double longitude) {
        loadingLay.setVisibility(View.GONE);
        final Location loc1 = new Location("");
        loc1.setLatitude(latitude);
        loc1.setLongitude(longitude);


        firstTime = false;

        if (servingDistance != 0) {
            Location loc2 = new Location("");
            loc2.setLatitude(servingLatitude);
            loc2.setLongitude(servingLongitude);
            final float distanceInMeters = loc1.distanceTo(loc2);

            Log.e("Distance is:", distanceInMeters + "");
            Log.e("latlong", loc1.getLatitude() + "   " + loc1.getLongitude() + "   " + loc2.getLatitude() + " " + loc2.getLongitude() + "    " + servingDistance + "  " + loc1.distanceTo(loc2));
            if (distanceInMeters > servingDistance * 1000) { //100km
                submit.setEnabled(false);
                submit.setBackgroundResource(R.drawable.button_grayout);
                submit.setText(getApplicationContext().getResources().getString(R.string.currently_we_donot));
            } else {
                submit.setEnabled(true);
//                if (radioGroup.getCheckedRadioButtonId() != -1)
//                    submit.setText(getApplicationContext().getResources().getString(R.string.save_continue));
//                else
                submit.setText(getApplicationContext().getResources().getString(R.string.continue1));
                submit.setBackgroundResource(R.drawable.button_green);
            }
        } else {
            mFirebaseLatLongDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    servingDistance = ((Number) (dataSnapshot.child("distance").getValue())).longValue();
                    servingLatitude = ((Number) (dataSnapshot.child("latitude").getValue())).doubleValue();
                    servingLongitude = ((Number) (dataSnapshot.child("longitude").getValue())).doubleValue();
                    Location loc2 = new Location("");
                    loc2.setLatitude(servingLatitude);
                    loc2.setLongitude(servingLongitude);
                    final float distanceInMeters = loc1.distanceTo(loc2);
                    Log.e("Distance is:", distanceInMeters + "");
                    if (distanceInMeters > servingDistance * 1000) { //100km
                        submit.setEnabled(false);
                        submit.setBackgroundResource(R.drawable.button_grayout);
                        submit.setText(getApplicationContext().getResources().getString(R.string.currently_we_donot));
                    } else {
                        submit.setEnabled(true);
//                        if (radioGroup.getCheckedRadioButtonId() != -1)
//                            submit.setText(getApplicationContext().getResources().getString(R.string.save_continue));
//                        else
                        submit.setText(getApplicationContext().getResources().getString(R.string.continue1));
                        submit.setBackgroundResource(R.drawable.button_green);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void updateMapToSearchLocation(LocationInf locationInfo) {
        tempLocationInfo = locationInfo;
        String addressString = locationInfo.getLocationName() + "\n" + locationInfo.getLocationAddress();
        address.setText(addressString);
        if (googleMap != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude()), 17));
        updateButton(locationInfo.getLatitude(), locationInfo.getLongitude());
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickUpLocationInfo);
        intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickLandmark);
        setResult(RESULT_OK, intent);
        finish();
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        this.googleMap.setPadding(0, 300, 0, 0);
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
        this.googleMap.setOnMyLocationButtonClickListener(() -> {

            return false;
        });
        googleMap.setOnCameraIdleListener(this);

        zoomMap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 3) {
            if (data != null) {
                searchFirstTime = true;
                dropOffLocationInfo = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
//                if (!dropOffLocationInfo.landmark.equals(""))
                landmark.setText(dropOffLocationInfo.landmark);
                if (data.getBooleanExtra(Constants.IntentKey.FROM_SAVED_ADDRESSES, false)) {
                    addressLay.setVisibility(View.GONE);
                    radioGroup.clearCheck();
                    label = "";
                } else addressLay.setVisibility(View.VISIBLE);
                updateMapToSearchLocation(dropOffLocationInfo);

            }
        } else if (requestCode == FROM_BACK) {
            if (data != null) {
                pickUpLocationInfo = data.getParcelableExtra(Constants.IntentKey.KEY_PICKUP_LOCATION);
                dropOffLocationInfo = data.getParcelableExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION);
                pickLandmark = data.getStringExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK);
                dropLandmark = data.getStringExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK);
                searchFirstTime = true;
                updateMapToSearchLocation(dropOffLocationInfo);
                landmark.setText(dropLandmark);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkListner != null)
            connectedRef.removeEventListener(networkListner);
    }

    private void checkLocationEnable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        if (!gps_enabled && !network_enabled) {
            locationSettingsDialog();
        }
    }

    private void locationSettingsDialog() {
        locationDialog = new Dialog(this);

        locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        locationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        locationDialog.setContentView(R.layout.dialog_delete_all);
        locationDialog.setCanceledOnTouchOutside(false);
        TextView title = locationDialog.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.gps_network_not_enabled));
        Button yes = locationDialog.findViewById(R.id.yes);
        yes.setText(getResources().getString(R.string.open_location_settings));

        Button no = locationDialog.findViewById(R.id.no);
        no.setText(getResources().getString(R.string.cancel));
        yes.setOnClickListener(v -> {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
            locationDialog.dismiss();
        });
        no.setOnClickListener(v -> {
            finish();
            locationDialog.dismiss();
        });
        locationDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationEnable();
        startNewLocationUpdates();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNewLocationUpdates();
    }


    @Override
    public void onResume() {
        super.onResume();
        startNewLocationUpdates();
    }


    @Override
    public void onCameraIdle() {

        Log.e("Camera state", "The camera has stopped moving.");
        getMapCenterPoint();
    }


    private void requestLocationPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(location_permission)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> ActivityCompat.requestPermissions(MapDragDropoffActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_REQUEST))
                    .show();
        } else {

            // phone state permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);
        }
        // END_INCLUDE(location_permission)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == LOCATION_REQUEST) {
            Log.i(TAG, "Received response for location permissions request.");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your work here
                finish();
                startActivity(getIntent());
            } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                if ((locationDialog != null && locationDialog.isShowing()))
                    locationDialog.dismiss();
                Utils.showDialogPermission(MapDragDropoffActivity.this, "Go to App Settings and Grant the Location  permission to use this feature.");
                // User selected the Never Ask Again Option
            } else {
                finish();
            }


        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showDialogOpps(final Context context) {
        if (internetAlertDialog != null)
            if (internetAlertDialog.isShowing())
                internetAlertDialog.dismiss();
//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        internetAlertDialog.setTitle("Oops!!!");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
//        internetAlertDialog.setMessage("Something went wrong Please try again after sometime!" + "\n" + text);
        internetAlertDialog.setMessage("Something went wrong Please try again after sometime!");
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {

//                System.exit(0);
            internetAlertDialog.dismiss();
        });

        // Showing Alert Message
        try {
            if (!firstTime)
                internetAlertDialog.show();
//            firstTime = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }


    // new location services stuff
    private void locationServiceStuff() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (currentLocation == null)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        else
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("CurrentLocation", location.getLatitude() + " " + location.getLongitude());
                    currentLocation = location;
                    if (googleMap != null && singleTime) {
                        singleTime = false;
                        zoomMap();
                    }
                }
            }


        };

    }

    private void startNewLocationUpdates() {
        if (started)
            return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.e("started", "start loc");
        started = true;
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopNewLocationUpdates() {
        started = false;
        Log.e("stopped", "stop loc");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}