package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.CancelOptionsAdapter;
import com.sprvtec.whizzy.imageloaderstuff.ImageLoader;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.DirectionsJSONParser;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CancelOption;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.GetNearByVehiclesResponse;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.NotificationData;
import com.sprvtec.whizzy.vo.OrderDetails;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;


/**
 * Created by Sowjanya on 5/25/2017.
 */
public class DriverDetailsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, View.OnClickListener {
    private static final int READ_PHONE_STATE_REQUEST = 101;
    private AlertDialog orderCancelDialog;
    private GoogleMap mMap;
    private SharedPreferences sp;
    private View mLayout;
    private TextView vehicleNumber, driverName, viewDrops;


    private String TAG = "DriverDetails Activity";
    private NotificationData notificationData;

    private ValueEventListener listner;

    private static final String GEO_FIRE_DB = "https://whizzydevelopment.firebaseio.com";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/in_trip";

    private GeoFire geoFire;
    private GeoQuery geoQuery;


    private Marker vehicleMarker;

    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerList;
    private boolean started = false;
    private int count = 9;
    private Polyline polyLine;

    private DatabaseReference mFirebaseDatabase, mFirbaseCancelOptions;
    private FirebaseDatabase mFirebaseInstance;
    private TextView nameMenu, viewText;
    private String orderID = "";
    private ImageView profileImage, personImage;
    private Dialog expenceDialog;
    private LinearLayout track_lay;
    private FrameLayout view_lay;
    private ImageView img_accept, img_arr_pickup, img_start_trip, img_arr_dropoff, img_end_trip;
    private TextView txt_acpt, txt_pickup, txt_start, txt_dropoff, txt_endtrip;

    private Dialog dialog1, cont_dailog;
    private TextView pay;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mLayout = findViewById(R.id.main);
        viewDrops = findViewById(R.id.view_drops);
        notificationData = getIntent().getParcelableExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA);
        track_lay = findViewById(R.id.view_layout);
        view_lay = findViewById(R.id.track_layout);
        viewText = findViewById(R.id.view_txt);

        cont_dailog = new Dialog(DriverDetailsActivity.this);
        cont_dailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cont_dailog.setContentView(R.layout.calllayout);
        cont_dailog.setCanceledOnTouchOutside(true);
        pay = findViewById(R.id.pay);
        pay.setOnClickListener(v -> {
            Intent intent = new Intent(DriverDetailsActivity.this, PaymentOptionsActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, orderID);
            startActivityForResult(intent, 111);
        });

        Button call = cont_dailog.findViewById(R.id.call);
        Button dimsiss = cont_dailog.findViewById(R.id.dismiss_call);


        call.setOnClickListener(v -> {
            Intent callWhizzy = new Intent(Intent.ACTION_DIAL);
            callWhizzy.setData(Uri.parse("tel:" + Constants.KEY_SUPPORT_NUMBER));

//            if (ContextCompat.checkSelfPermission(DriverDetailsActivity.this,
//                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                Log.e("No permission", "call permission");
//                cont_dailog.dismiss();
//                requestReadPhoneStatePermission();
//                // TODO: Consider calling
//                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
            cont_dailog.dismiss();
            startActivity(callWhizzy);
        });

        dimsiss.setOnClickListener(view -> cont_dailog.dismiss());


        img_accept = findViewById(R.id.accept_img);
        img_arr_pickup = findViewById(R.id.arr_pickup_img);
        img_start_trip = findViewById(R.id.start_trip_img);
        img_arr_dropoff = findViewById(R.id.arr_dropoff_img);
        img_end_trip = findViewById(R.id.end_trip_img);

        txt_acpt = findViewById(R.id.accept_txt);
        txt_pickup = findViewById(R.id.pickup_txt);
        txt_start = findViewById(R.id.start_txt);
        txt_dropoff = findViewById(R.id.dropoff_txt);
        txt_endtrip = findViewById(R.id.end_trip_txt);


        viewText.setOnClickListener(view -> {

            if (viewText.getText().toString().equalsIgnoreCase("View")) {
                track_lay.setVisibility(View.GONE);
                view_lay.setVisibility(View.VISIBLE);
                viewText.setText(getApplicationContext().getResources().getString(R.string.track));
            } else if (viewText.getText().toString().equalsIgnoreCase("Track")) {
                view_lay.setVisibility(View.GONE);
                track_lay.setVisibility(View.VISIBLE);
                viewText.setText(getApplicationContext().getResources().getString(R.string.view));
            }
        });


        Log.e("NotificationData 1", notificationData + " data");

        Log.e("Driver_vehicle_id", notificationData.driver_vehicle_id + "ID");


        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'orders' node
        mFirebaseDatabase = mFirebaseInstance.getReference("orders");
        mFirbaseCancelOptions = mFirebaseInstance.getReference("trip_cancellation_reasons_customer");
        updateOrderStatus(notificationData.order_id);
        orderID = notificationData.order_id;


        vehicleNumber = findViewById(R.id.vehicle_number);
        driverName = findViewById(R.id.driver_name);
        LinearLayout contactDriver = findViewById(R.id.contact);
        LinearLayout share = findViewById(R.id.share);
        LinearLayout cancelTrip = findViewById(R.id.cancel_trip);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
        ImageView menu = findViewById(R.id.menu);
        profileImage = findViewById(R.id.image);
        personImage = findViewById(R.id.person_image);


        //Menu items
        LinearLayout yourTripMenu, legalMenu, contactUsMenu, helpMenu, aboutMenu, referFriendMenu;

        RelativeLayout editProfile;


        yourTripMenu = findViewById(R.id.your_trips);
        yourTripMenu.setOnClickListener(this);
        LinearLayout mySavedAddresses = findViewById(R.id.my_saved_addresses);
        mySavedAddresses.setOnClickListener(this);
        referFriendMenu = findViewById(R.id.refer_friend);
        referFriendMenu.setOnClickListener(this);
        legalMenu = findViewById(R.id.legal);
        legalMenu.setOnClickListener(this);
        contactUsMenu = findViewById(R.id.contact_us);
        contactUsMenu.setOnClickListener(this);
        helpMenu = findViewById(R.id.help);
        helpMenu.setOnClickListener(this);
        aboutMenu = findViewById(R.id.about);
        aboutMenu.setOnClickListener(this);
        editProfile = findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(this);
        nameMenu = findViewById(R.id.name);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerLayout.closeDrawers();
        mDrawerLayout.setFocusableInTouchMode(false);
        menu.setOnClickListener(v -> {

            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawers();
            } else {
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("")) {
                    GetProfileDetailsTask task = new GetProfileDetailsTask(DriverDetailsActivity.this);
                    StartAsyncTaskInParallel2(task);
                }
                nameMenu.setText(sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, "Name"));
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        mDrawerList.setOnClickListener(v -> {

        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        contactDriver.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + notificationData.driver_mobile));

//            if (ContextCompat.checkSelfPermission(DriverDetailsActivity.this,
//                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                requestReadPhoneStatePermission();
//                // TODO: Consider calling
//                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
            startActivity(callIntent);
        });
        share.setOnClickListener(v -> {
            String shareBody = notificationData.driver_name + ", " + notificationData.driver_mobile + " with vehicle number " + notificationData.vehicle_number + " will arrive shortly to fulfill your request " + ". - WHIZZY";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

        });
        findViewById(R.id.view_drops).setOnClickListener(v -> {
            Intent intent = new Intent(DriverDetailsActivity.this, DropContactsActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, orderID);
            startActivity(intent);
        });
        cancelTrip.setOnClickListener(v -> {
            if (started) {
                youCannotCancelTripPopup();
            } else {
                getCancelOptionsFireBase();
            }
        });
        updateDriverDetails();
        dialog1 = new Dialog(DriverDetailsActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.alert_network);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);


        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    if (dialog1.isShowing())
                        dialog1.dismiss();
                } else {
                    if (!DriverDetailsActivity.this.isFinishing()) {
                        dialog1.show(); //show dialog
                    }//nothing to show in else
//                    dialog1.show();
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });


    }

    private void requestReadPhoneStatePermission() {
        Log.i("Splash", "read phone state permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(phone_state_permission)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("Splash",
                    "Displaying phone state permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_phone_state_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> ActivityCompat.requestPermissions(DriverDetailsActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            READ_PHONE_STATE_REQUEST))
                    .show();
        } else {

            // phone state permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    READ_PHONE_STATE_REQUEST);
        }
        // END_INCLUDE(phone_state_permission)
    }


    private void youCannotCancelTripPopup() {
        if (expenceDialog == null) {
            expenceDialog = new Dialog(DriverDetailsActivity.this);
            expenceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            expenceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }//nothing to add in else
        expenceDialog.setContentView(R.layout.you_cannot_cancel);
        expenceDialog.setCanceledOnTouchOutside(false);

        final TextView dismissText = expenceDialog.findViewById(R.id.dismiss);
        final TextView callDriverText = expenceDialog.findViewById(R.id.call_driver);


        callDriverText.setOnClickListener(v -> {
            Intent callWhizzy = new Intent(Intent.ACTION_DIAL);
            callWhizzy.setData(Uri.parse("tel:" + notificationData.driver_mobile));

//            if (ContextCompat.checkSelfPermission(DriverDetailsActivity.this,
//                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                expenceDialog.dismiss();
//                requestReadPhoneStatePermission();
//                // TODO: Consider calling
//                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
            startActivity(callWhizzy);
        });
        dismissText.setOnClickListener(v -> expenceDialog.dismiss());
        if (!(this).isFinishing()) {
            expenceDialog.show();
        }//nothing to add in else
    }

    private void getCancelOptionsFireBase() {
        final List<CancelOption> options = new ArrayList<>();
        mFirbaseCancelOptions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count ", "" + snapshot.getChildrenCount());

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String post = postSnapshot.getValue(String.class);
                    CancelOption option = new CancelOption();
                    option.option = post;
                    options.add(option);
                    Log.e("Get Data", post);
                }
                showActionSheet(options);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void updateOrderStatus(final String orderID) {
        listner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetails order = dataSnapshot.getValue(OrderDetails.class);

                // Check for null
                if (order == null) {
                    Log.e(TAG, "Order data is null!");
                    return;
                }
                if (order.order_type_flow.equalsIgnoreCase("single_pickup_multiple_drop_contacts")) {
                    viewDrops.setVisibility(View.VISIBLE);
                    pay.setVisibility(View.VISIBLE);
                }

                if (order.payment_status.equalsIgnoreCase("Completed")) {
                    pay.setOnClickListener(null);
                    if (order.payment_mode.equalsIgnoreCase("Cash"))
                        pay.setText("Payment done through Cash");
                    else
                        pay.setText("Payment done through Card");
                    if (PaymentOptionsActivity.fa != null)
                        PaymentOptionsActivity.fa.finish();
                }
                if (order.driver_details != null) {
                    notificationData.driver_name = order.driver_details.driver_name;
                    notificationData.driver_mobile = order.driver_details.driver_mobile;
                    notificationData.vehicle_number = order.driver_details.vehicle_number;
                    notificationData.vehicle_type = order.driver_details.vehicle_type;
                    notificationData.driver_image = order.driver_details.driver_image;
                    String iamgepath = Webservice.DRIVER_IMAGE_PATH_NEW;
                    String iamge_set = iamgepath + order.driver_details.driver_image;


                    if (!order.driver_details.driver_image.equalsIgnoreCase("")) {
                        Picasso.with(getApplicationContext()).load(iamge_set).into(personImage);
                    }


                    updateDriverDetails();
                }
                Log.e(TAG, "Order data is changed!" + order.order_status + ", " + order.order_id);

                if (order.order_status.equals("Request Accepted")) {
//already in driverdetails screen
                } else if (order.order_status.equals("start_trip")) {
                    started = true;
                } else if (order.order_status.equalsIgnoreCase("arrived_at_drop")) {
                    if (!order.order_type_flow.equalsIgnoreCase("single_pickup_multiple_drop_contacts")) {
                        Intent endTripIntent = new Intent(DriverDetailsActivity.this, EndTripSummaryActivity.class);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                        endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
                        endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
                        startActivity(endTripIntent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    }
                } else if (order.order_status.equals("trip_ended")) {
                    Intent endTripIntent = new Intent(DriverDetailsActivity.this, EndTripSummaryActivity.class);
                    endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                    endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                    endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
                    endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
                    startActivity(endTripIntent);
                    if (listner != null)
                        mFirebaseDatabase.child(orderID).removeEventListener(listner);
                    finish();
                } else if (order.order_status.equals("Driver Canceled")) {
                    mFirebaseInstance.getReference("orders").child(orderID).child("order_status").setValue("Looking for Drivers", (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            Log.e("database error", databaseError.getMessage());
                        } else {
                            Intent intent = new Intent(DriverDetailsActivity.this, LoadingActivity.class);
                            intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                            intent.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                            intent.putExtra(Constants.IntentKey.KEY_PAGE_TITLE, "Whizzer cancelled..Looking for another");
//                    intent.putExtra(Constants.IntentKey.KEY_PROCESSING, 0);
                            intent.putExtra("drivercanc", "one");
                            startActivity(intent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                        }
                    });
//                    mFirebaseInstance.getReference("orders").child(orderID).child("order_status").setValue("Looking for Drivers");

                } else if (order.order_status.equals("Order Canceled")) {
                    if (listner != null)
                        mFirebaseDatabase.child(orderID).removeEventListener(listner);
                    showOrderCancelDialog(order.cancel_option.reason);
                } else if (order.order_status.equals("Completed")) {
                    if (order.payment_status.equalsIgnoreCase("Not Availabe") || order.payment_status.equalsIgnoreCase("Pending") || order.payment_status.equalsIgnoreCase("Not Available")) {
                        String paybutton = "payButton";
                        Intent endTripIntent = new Intent(DriverDetailsActivity.this, EndTripSummaryActivity.class);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                        endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, paybutton);
                        endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
//                        endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(endTripIntent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                        //show trip summary screen with payment button
                    } else if (order.ratings_status.equals("Not Availabe") || order.ratings_status.equals("Pending") || order.ratings_status.equals("Not Available")) {
                        String rayLayout = "ratingLayout";
                        Intent endTripIntent = new Intent(DriverDetailsActivity.this, EndTripSummaryActivity.class);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                        endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, rayLayout);
                        endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
//                        endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(endTripIntent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                        //show trip summary screen with rating layout
                    } else {
                        Intent intent = new Intent(DriverDetailsActivity.this, HomePagerActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    }
                }


                if (order.order_status.equalsIgnoreCase("Accepted by Driver")) {
                    img_accept.setBackgroundResource(R.drawable.checked);
                    txt_acpt.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                } else if (order.order_status.equalsIgnoreCase("arrived_at_pickup")) {
                    img_accept.setBackgroundResource(R.drawable.checked);
                    img_arr_pickup.setBackgroundResource(R.drawable.checked);
                    txt_acpt.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_pickup.setTypeface(viewText.getTypeface(), Typeface.BOLD);

                } else if (order.order_status.equalsIgnoreCase("start_trip")) {
                    img_accept.setBackgroundResource(R.drawable.checked);
                    img_arr_pickup.setBackgroundResource(R.drawable.checked);
                    img_start_trip.setBackgroundResource(R.drawable.checked);
                    txt_acpt.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_pickup.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_start.setTypeface(viewText.getTypeface(), Typeface.BOLD);

                } else if (order.order_status.equalsIgnoreCase("arrived_at_drop")) {
                    img_accept.setBackgroundResource(R.drawable.checked);
                    img_arr_pickup.setBackgroundResource(R.drawable.checked);
                    img_start_trip.setBackgroundResource(R.drawable.checked);
                    img_arr_dropoff.setBackgroundResource(R.drawable.checked);
                    txt_acpt.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_pickup.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_start.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_dropoff.setTypeface(viewText.getTypeface(), Typeface.BOLD);

                } else if (order.order_status.equalsIgnoreCase("trip_ended")) {
                    img_accept.setBackgroundResource(R.drawable.checked);
                    img_arr_pickup.setBackgroundResource(R.drawable.checked);
                    img_start_trip.setBackgroundResource(R.drawable.checked);
                    img_arr_dropoff.setBackgroundResource(R.drawable.checked);
                    txt_acpt.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_pickup.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_start.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    txt_dropoff.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                    img_end_trip.setBackgroundResource(R.drawable.checked);
                    txt_endtrip.setTypeface(viewText.getTypeface(), Typeface.BOLD);
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                Utils.showDialogOpps(DriverDetailsActivity.this, error.toException().toString());
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        };
        // User data change listener
        mFirebaseDatabase.child(orderID).addValueEventListener(listner);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listner != null)
            mFirebaseDatabase.child(orderID).removeEventListener(listner);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void geoFireStuff() {
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
        this.geoFire = new GeoFire(FirebaseDatabase.getInstance(app).getReferenceFromUrl(GEO_FIRE_REF));
        this.geoFire.getLocation(notificationData.driver_vehicle_id, new LocationCallback() {
//        this.geoFire.getLocation("f8aaca10-4459-11e7-b3d3-f5576c0d3e6b", new LocationCallback() {

            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (vehicleMarker == null && location != null) {
                    Log.e("Location", "not null");
                    Bitmap b;
                    Bitmap bhalfsize;
                    switch (notificationData.vehicle_name) {
                        case "Four Wheeler":
                            b = BitmapFactory.decodeResource(getResources(), R.drawable.car);
                            bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 3, b.getHeight() / 3, false);
                            break;
                        case "Three Wheeler":
                            b = BitmapFactory.decodeResource(getResources(), R.drawable.three_wheeler);
                            bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);
                            break;
                        default:
                            b = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_two);
                            bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), false);
                            break;
                    }


                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(location.latitude, location.longitude))
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));

                    vehicleMarker = mMap.addMarker(markerOptions);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    if (geoQuery == null) {
                        geoQuery = geoFire.queryAtLocation(new GeoLocation(vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude), 50);
                        geoQuery.addGeoQueryEventListener(DriverDetailsActivity.this);
                    }
                }
                if (vehicleMarker != null && location != null)
                    animateMarkerTo(vehicleMarker, location.latitude, location.longitude);
                if (location == null)
                    Log.e("Location", "Nullo null");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError + "");
            }
        });
    }


    private void updateDriverDetails() {
        driverName.setText(notificationData.driver_name);
        vehicleNumber.setText(notificationData.vehicle_number);
        if (!notificationData.driver_image.equals(""))
            Picasso.with(getApplicationContext()).load(Webservice.DRIVER_IMAGE_PATH_NEW + notificationData.driver_image).into(personImage);

    }

    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
//        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final Interpolator interpolator = new LinearInterpolator();
        final LatLng startPosition = marker.getPosition();
        Location prevLoc = new Location("");
        prevLoc.setLatitude(startPosition.latitude);
        prevLoc.setLongitude(startPosition.longitude);
        Location newLoc = new Location("");
        newLoc.setLatitude(lat);
        newLoc.setLongitude(lng);
        final float bearing = prevLoc.bearingTo(newLoc);
        final float startRotation = marker.getRotation();
//        marker.setRotation(bearing);
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
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude))      // Sets the center of the map to location user
                        .zoom(15)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this::updateMap);
        Log.e("Map ready", "map map");

    }

    private void updateMap() {
        mMap.clear();
        LatLng pickup = new LatLng(notificationData.pickup_latitude, notificationData.pickup_longitude);
        LatLng dropOff = new LatLng(notificationData.drop_latitude, notificationData.drop_longitude);
//        Map<Marker, Integer> allMarkersMap = new HashMap<>();
        LocationInf pickupInfo = new LocationInf();
        pickupInfo.setLocationName(notificationData.pickup_full_address.split(",")[0]);
        pickupInfo.setLocationAddress(notificationData.pickup_full_address);
        MarkerOptions pickMarker = new MarkerOptions()
                .position(pickup)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(DriverDetailsActivity.this, pickupInfo))).anchor(0.5f, 1.8f);
        mMap.addMarker(pickMarker);
//        Marker pickupMarker = mMap.addMarker(pickMarker);
//        allMarkersMap.put(pickupMarker, 0);

        LocationInf dropOffInfo = new LocationInf();
        dropOffInfo.setLocationName(notificationData.drop_full_address.split(",")[0]);
        dropOffInfo.setLocationAddress(notificationData.drop_full_address);
        MarkerOptions dropMarker = new MarkerOptions()
                .position(dropOff)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(DriverDetailsActivity.this, dropOffInfo))).anchor(0.5f, 1.8f);
        mMap.addMarker(dropMarker);
//        Marker dropOffMarker = mMap.addMarker(dropMarker);
//        allMarkersMap.put(dropOffMarker, 1);

        ///////////////////////////////////////
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluepin);
        Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), false);
        MarkerOptions marker = new MarkerOptions()
                .position(pickup)
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
        mMap.addMarker(marker);
//        Marker m1 = mMap.addMarker(marker);
//        allMarkersMap.put(m1, 0);

        Bitmap b1 = BitmapFactory.decodeResource(getResources(), R.drawable.greenpin);
        Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);
        MarkerOptions marker1 = new MarkerOptions()
                .position(dropOff)
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1));
        mMap.addMarker(marker1);
//        Marker m2 = mMap.addMarker(marker1);
//        allMarkersMap.put(m2, 1);


        ///////////////////////////////////////////////

        //animate camera
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickup);
        builder.include(dropOff);
//        int routePadding = 10;
        LatLngBounds latLngBounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.25); // offset from edges of the map 10% of screen

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(pickup, dropOff);

        DownloadTask downloadTask = new DownloadTask(url);
        StartAsyncTaskInParallel(downloadTask);
        // Start downloading json data from Google Directions API
//        downloadTask.execute(url);
        geoFireStuff();
    }

    private void updateRoute(LatLng pickup, LatLng dropOff) {
        String url = getDirectionsUrl(pickup, dropOff);

        DownloadTask downloadTask = new DownloadTask(url);
        StartAsyncTaskInParallel(downloadTask);

    }

    public Bitmap createDrawableFromView(Context context, LocationInf locationInfo) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.custom_marker, null);
        TextView tv_location_name = marker.findViewById(R.id.title);
//        TextView address = (TextView) marker.findViewById(R.id.address_text);

        tv_location_name.setText(locationInfo.getLocationName());
//        address.setText(locationInfo.getLocationAddress());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);
        return bitmap;
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Log.e(TAG, "onKeyEntered");

    }

    @Override
    public void onKeyExited(String key) {

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Log.e(TAG, "onKeyMoved" + location);
        if (key.equals(notificationData.driver_vehicle_id)) {
//            Marker marker = this.markers.get(key);
            Location oldLocation = new Location("");
            oldLocation.setLatitude(vehicleMarker.getPosition().latitude);
            oldLocation.setLongitude(vehicleMarker.getPosition().longitude);
            Location newLocation = new Location("");
            newLocation.setLatitude(location.latitude);
            newLocation.setLongitude(location.longitude);
            float distanceInMetersOne = oldLocation.distanceTo(newLocation);
            Log.e("Distance is: ", distanceInMetersOne + " distance" + key);
            if (vehicleMarker != null && distanceInMetersOne > 5) {
//            if (vehicleMarker != null ) {
                this.animateMarkerTo(vehicleMarker, location.latitude, location.longitude);
                count++;
            }
            if (count == 10) { //check for every 30sec 3sec x 10 = 30sec
                LatLng pickup = new LatLng(location.latitude, location.longitude);
                LatLng dropOff = new LatLng(notificationData.drop_latitude, notificationData.drop_longitude);
                count = 0;
                updateRoute(pickup, dropOff);
            }
        }
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_profile:
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                    Intent editIntent = new Intent(DriverDetailsActivity.this, ProfileActivity.class);
                    startActivity(editIntent);
                } else {
                    Intent editIntent = new Intent(DriverDetailsActivity.this, SignUpActivity.class);
                    startActivity(editIntent);
                }
                break;
            case R.id.your_trips:
                Intent tripsIntent = new Intent(DriverDetailsActivity.this, AllTripsActivity.class);
                startActivity(tripsIntent);
                break;
            case R.id.my_saved_addresses:
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                    Intent editIntent = new Intent(DriverDetailsActivity.this, SavedAddressesActivity.class);
                    startActivity(editIntent);
                } else {
                    Intent editIntent = new Intent(DriverDetailsActivity.this, SignUpActivity.class);
                    startActivity(editIntent);
                }
                break;
            case R.id.legal:
                Intent tcIntent = new Intent(DriverDetailsActivity.this, TCActivity.class);
                startActivity(tcIntent);
                break;
            case R.id.contact_us:
                cont_dailog.show();
                break;
            case R.id.help:
                Intent helpIntent = new Intent(DriverDetailsActivity.this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.refer_friend:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi, Click on the link to download the Whizzy app to get anything done. https://play.google.com/store/apps/details?id=com.sprvtec.whizzy \n -WHIZZY");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(DriverDetailsActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

        }
        mDrawerLayout.closeDrawers();

    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        private String urlSt;

        DownloadTask(String urlSt) {
            this.urlSt = urlSt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(urlSt);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(result);
            StartAsyncTaskInParallel(parserTask);

//            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private String resultSt;

        ParserTask(String resultSt) {
            this.resultSt = resultSt;
        }

        // Parsing the data in non-ui thread

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(resultSt);
                Log.e("Routes", jObject + "");
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList points;
            PolylineOptions lineOptions = null;
            if (result != null)
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(12);
                    lineOptions.color(Color.BLACK);
                    lineOptions.geodesic(true);

                }

// Drawing polyline in the Google Map for the i-th route
            if (polyLine != null)
                polyLine.remove();
            if (lineOptions != null)
                polyLine = mMap.addPolyline(lineOptions);
//            else
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
//        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + "AIzaSyCeVDzYNYJe7V_vEeiZQu2Sngs-UBQgr7g";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + Constants.GOOGLE_API_KEY;

        // Output format
        String output = "json";

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_PHONE_STATE_REQUEST) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                Snackbar.make(mLayout, R.string.permission_available_phone_state,
                        Snackbar.LENGTH_SHORT).show();
//                init();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
//                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void showActionSheet(List<CancelOption> options) {

        final Dialog myDialog = new Dialog(DriverDetailsActivity.this, R.style.CustomTheme);

        myDialog.setContentView(R.layout.actionsheet);

        TextView cancel = myDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(v -> myDialog.dismiss());

        ListView listView = myDialog.findViewById(R.id.list);
        CancelOptionsAdapter adapter = new CancelOptionsAdapter(DriverDetailsActivity.this, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (started) {
                myDialog.dismiss();
                youCannotCancelTripPopup();

            } else {
                CancelOption option = (CancelOption) parent.getItemAtPosition(position);
                CancelTripTask task = new CancelTripTask(DriverDetailsActivity.this, notificationData.order_id, option.option);
                StartAsyncTaskInParallel(task);
            }
//                cancelTripDialog(option);
        });

        myDialog.getWindow().getAttributes().windowAnimations = R.anim.slide_up_anim;

        myDialog.show();

        myDialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    @SuppressLint("StaticFieldLeak")
    class CancelTripTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String orderID, cancelOption;

        CancelTripTask(Context context, String orderID, String cancelOption) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.cancelOption = cancelOption;
            this.orderID = orderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
//            progressDialog.setCanceledOnTouchOutside(false);
            if (!DriverDetailsActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_ID, orderID));
            nameValuePairs.add(new BasicNameValuePair(Webservice.CANCEL_OPTION, cancelOption));


            String json = Webservice.callPostService1(Webservice.CUSTOMER_CANCEL_TRIP, nameValuePairs);
//            String json = Webservice.postData(Webservice.REGISTER_USER, nameValuePairs);
            String status = "";
            Log.e("INTEREST", json + "responseeeeee");
            Log.i("INTEREST", json + "responseeeeee");


            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
//            return context.getResources().getString(R.string.json_exception);
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    createOrderResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    createOrderResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);

                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
//                return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !DriverDetailsActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (createOrderResponse != null)
                    successDialog("Trip cancelled");
            } else if (!Utils.isNetConnected(context))
                Utils.showDialogOpps(context, "No internet connection");
            else {

                Utils.showDialogOpps(context, resp);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel(
            DownloadTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel(
            ParserTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    @SuppressLint("StaticFieldLeak")
    private class GetProfileDetailsTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetNearByVehiclesResponse getProfileDetailsResponse;
//        private ProgressDialog progressDialog;

        GetProfileDetailsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_PROFILE_DETAILS + "?" + Webservice.USER_ID + "=" + sp.getString(Constants.PreferenceKey.KEY_USER_ID, "") + "&" + Webservice.USER_MOBILE + "=" + sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "");
            String json = Webservice.callGetService(url);
            String status = "";
            Log.e("Response send request", json);
            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
//                return context.getResources().getString(R.string.json_exception);
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getProfileDetailsResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
//                    return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
//            progressDialog.dismiss();
//            loading.setVisibility(View.GONE);
            if (resp == null) {
                if (getProfileDetailsResponse != null && getProfileDetailsResponse.data.user_details.user_image_url != null) {
                    ImageLoader imageLoader = new ImageLoader(context);
                    imageLoader.DisplayImage(Webservice.PROFILE_IMAGE_PATH + getProfileDetailsResponse.data.user_details.user_image_url, profileImage, R.drawable.male);

                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }

    void showDialog1(final Context context, String title, String message) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            new GetProfileDetailsTask(DriverDetailsActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void StartAsyncTaskInParallel(
            CancelTripTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void StartAsyncTaskInParallel2(
            GetProfileDetailsTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void showOrderCancelDialog(String message) {
        if (orderCancelDialog != null)
            if (orderCancelDialog.isShowing())
                orderCancelDialog.dismiss();
        orderCancelDialog = new AlertDialog.Builder(DriverDetailsActivity.this).create();

        // Setting Dialog Title
        orderCancelDialog.setTitle("Sorry, your request could not be fulfilled");
        orderCancelDialog.setCancelable(false);
        // Setting Dialog Message
        orderCancelDialog.setMessage("Whizzer says " + message);
        orderCancelDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {
            sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "").apply();
            Intent intent = new Intent(DriverDetailsActivity.this, HomePagerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            orderCancelDialog.dismiss();
            finish();
        });

        orderCancelDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK) {
            pay.setOnClickListener(null);
        }
    }

    public void successDialog(String message) {
        if (dialog != null && dialog.isShowing() && !DriverDetailsActivity.this.isFinishing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(DriverDetailsActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(Html.fromHtml(message));
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                dialog.dismiss();
//                if (getIntent().getBooleanExtra(Constants.IntentKey.KEY_LAUNCH_HOME, false)) {
                Intent intent = new Intent(DriverDetailsActivity.this, HomePagerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (listner != null)
                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                finish();
//                } else {
//                    if (listner != null)
//                        mFirebaseDatabase.child(orderID).removeEventListener(listner);
//                    finish();
//                }

            });
            if (!DriverDetailsActivity.this.isFinishing())
                dialog.show();
        }
    }


}
