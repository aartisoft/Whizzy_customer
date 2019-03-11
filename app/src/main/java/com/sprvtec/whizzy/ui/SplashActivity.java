package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.GlobalApplication;
import com.sprvtec.whizzy.util.PermissionUtil;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.NotificationData;
import com.sprvtec.whizzy.vo.OrderDetails;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 * Created by SPRV on 9/22/2015.
 */
public class SplashActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private Handler handler;
    private final int LOCATION_REQUEST = 100;
    private final String TAG = "SplashActivity";
    private View mLayout;
    private boolean check = false, regTaskExe = false;
    private String android_id;
    private String orderID;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ValueEventListener listner;
    private Dialog dialog1;
    private DatabaseReference connectedRef;
    private ValueEventListener networkListner;
    private AlertDialog orderCancelDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mLayout = findViewById(R.id.main);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SplashActivity.this, instanceIdResult -> {
            String newToken = instanceIdResult.getToken();
            Log.e("newToken", newToken);
            GlobalApplication.fcmToken = newToken;

        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();

        } else init();

//        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
//                .setLink(Uri.parse("http://whizzy.in/"))
//                .setDomainUriPrefix("https://whizzypro.page.link")
//                // Open links with this app on Android
//                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
//                // Open links with com.example.ios on iOS
//                .setIosParameters(new DynamicLink.IosParameters.Builder("com.sprvtec.Whizzy").build())
//                .buildDynamicLink();
//
//        Uri dynamicLinkUri = dynamicLink.getUri();
//        Log.e("dynamicLinkUri", dynamicLinkUri + "  ");

        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.e("Android id", android_id);
        orderID = sp.getString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "");
        Log.e("OrderID id", orderID + "Order ID");
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'orders' node
        mFirebaseDatabase = mFirebaseInstance.getReference("orders");

        dialog1 = new Dialog(SplashActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.alert_network_spinner);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setCancelable(false);
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
                    if (!SplashActivity.this.isFinishing()) {
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


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (networkListner != null) {
            if (dialog1.isShowing())
                dialog1.dismiss();
            connectedRef.removeEventListener(networkListner);
        }
        connectedRef.addValueEventListener(networkListner);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacksAndMessages(null);
    }


    private void init() {
        handler = new Handler();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        // Splash screen timer
        int SPLASH_TIME_OUT = 3000;
        /*
         * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company
         */
        //                checkLocationEnable();
        handler.postDelayed(this::screenNavigation, SPLASH_TIME_OUT);


    }

    private void checkLocationEnable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            locationSettingsDialog();
        } else {
            screenNavigation();
        }
    }

    private void screenNavigation() {
        //// TODO:  un comment code 5/23/2017
        if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("")) {
            if (!sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "").equals("")) {
//                    new GetCustomerSettingsTask(SplashActivity.this).execute();
                //7fe0d088-506d-46a8-b2f6-1e226e1fca71
                if (!orderID.equals(""))
                    updateOrderStatus(orderID);
                else {
                    Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            } else {
                Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        } else {
            String fcmToken = GlobalApplication.fcmToken;

            LocalBroadcastManager.getInstance(this).registerReceiver(fcmTokenReceiver, new IntentFilter(Constants.BROADCAST_FCM_TOKEN));

            if (fcmToken != null) {
                Log.e("Normal", "FCM Token");
                new RegisterTask(SplashActivity.this).execute();
            }
        }

    }

    private BroadcastReceiver fcmTokenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!regTaskExe) {
                Log.e("Broadcast", "FCM Token");
                new RegisterTask(SplashActivity.this).execute();
            }
        }
    };

    private void locationSettingsDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_delete_all);
        dialog.setCanceledOnTouchOutside(false);
        TextView title = dialog.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.gps_network_not_enabled));
        Button yes = dialog.findViewById(R.id.yes);
        yes.setText(getResources().getString(R.string.open_location_settings));

        Button no = dialog.findViewById(R.id.no);
        no.setText(getResources().getString(R.string.cancel));
        yes.setOnClickListener(v -> {
            check = true;
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
            dialog.dismiss();
        });
        no.setOnClickListener(v -> {
            finish();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (check) {
            Log.e("check", " check true");
            checkLocationEnable();
        }
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

            init();
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

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(mLayout, R.string.permission_available_location,
                        Snackbar.LENGTH_SHORT)
                        .show();
                init();
            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                init();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class RegisterTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse customerRegistrationResponse;
        private ProgressDialog progressDialog;

        RegisterTask(Context context) {
            this.context = context;
            Log.e("FCM TOKEN", GlobalApplication.fcmToken + " fcm token");
            regTaskExe = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            if (!SplashActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair(Webservice.DEVICE_ID, android_id));
            nameValuePairs.add(new BasicNameValuePair(Webservice.OPERATING_SYSTEM, "Android"));
            if (GlobalApplication.fcmToken != null)
                nameValuePairs.add(new BasicNameValuePair(Webservice.FCM_TOKEN, GlobalApplication.fcmToken));
            else
                nameValuePairs.add(new BasicNameValuePair(Webservice.FCM_TOKEN, ""));
            String json = Webservice.callPostService1(Webservice.CREATE_USER, nameValuePairs);
            String status = "";
            Log.e("INTEREST", json);
            Log.i("INTEREST", json);


            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    customerRegistrationResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
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
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !SplashActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (customerRegistrationResponse != null) {
                    sp.edit().putString(Constants.PreferenceKey.KEY_USER_ID, customerRegistrationResponse.data.user_id).apply();
                    Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }

    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {
            alertDialog.dismiss();
            new RegisterTask(SplashActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void updateOrderStatus(final String orderID) {
        listner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetails order = dataSnapshot.getValue(OrderDetails.class);
                Log.e("Order details", order + "");
                if (order != null) {
                    final NotificationData notificationData = new NotificationData();
                    notificationData.crn_number = order.crn_number;
                    notificationData.vehicle_id = order.vehicle_id;
                    notificationData.driver_id = order.driver_id;
                    if (order.driver_details != null) {
                        notificationData.driver_name = order.driver_details.driver_name;
                        notificationData.driver_mobile = order.driver_details.driver_mobile;
                        notificationData.vehicle_number = order.driver_details.vehicle_number;
                        notificationData.vehicle_type = order.driver_details.vehicle_type;
                        notificationData.driver_vehicle_id = order.driver_details.driver_vehicle_id;
                        notificationData.driver_image = order.driver_details.driver_image;
                    }
                    notificationData.transport_description = order.transport_description;
                    notificationData.trip_type = order.trip_type;
                    notificationData.pickup_contact_name = order.pickup_contact_name;
                    notificationData.pickup_contact_number = order.pickup_contact_number;
                    notificationData.drop_contact_name = order.drop_contact_name;
                    notificationData.drop_contact_number = order.drop_contact_number;
                    notificationData.pickup_latitude = Double.parseDouble(order.pickup_latitude);
                    notificationData.pickup_longitude = Double.parseDouble(order.pickup_longitude);
                    notificationData.pickup_full_address = order.pickup_full_address;
                    notificationData.drop_latitude = Double.parseDouble(order.drop_latitude);
                    notificationData.drop_longitude = Double.parseDouble(order.drop_longitude);
                    notificationData.drop_full_address = order.drop_full_address;
                    notificationData.payment_required = Boolean.parseBoolean(order.payment_required);
                    notificationData.vehicle_name = order.vehicle_name;
                    notificationData.order_id = order.order_id;


                    Log.e(TAG, "Order data is changed!" + order.order_status + ", " + order.order_id);
                    if (order.order_status.equals("Looking for Drivers")) {
                        Intent intent = new Intent(SplashActivity.this, LoadingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        if (order.processing.equalsIgnoreCase("No"))
                            intent.putExtra(Constants.IntentKey.KEY_PROCESSING, 0);
                        else if (order.processing.equalsIgnoreCase("Yes"))
                            intent.putExtra(Constants.IntentKey.KEY_PROCESSING, 1);
                        intent.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        if (order.cancel_option != null && order.cancel_option.flag == 1)
                            intent.putExtra(Constants.IntentKey.KEY_PAGE_TITLE, "Whizzer cancelled..Looking for another");
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("No Vehicles Available")) {
                        Intent intent = new Intent(SplashActivity.this, LoadingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        intent.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        intent.putExtra(Constants.IntentKey.KEY_PROCESSING, 0);
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("Exception")) {
                        Intent intent = new Intent(SplashActivity.this, LoadingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                        intent.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        intent.putExtra(Constants.IntentKey.KEY_PROCESSING, 2);
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("Accepted by Driver")) {
                        Intent intentCall = new Intent(SplashActivity.this, DriverDetailsActivity.class);
                        intentCall.putExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA, notificationData);
                        intentCall.putExtra(Constants.IntentKey.KEY_DRIVER_STATUS, 0);
                        intentCall.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentCall);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("arrived_at_pickup")) {
                        Intent intentCall = new Intent(SplashActivity.this, DriverDetailsActivity.class);
                        intentCall.putExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA, notificationData);
                        intentCall.putExtra(Constants.IntentKey.KEY_DRIVER_STATUS, 0);
                        intentCall.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentCall);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("start_trip")) {
                        Intent intentCall = new Intent(SplashActivity.this, DriverDetailsActivity.class);
                        intentCall.putExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA, notificationData);
                        intentCall.putExtra(Constants.IntentKey.KEY_DRIVER_STATUS, 1);
                        intentCall.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentCall);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("arrived_at_drop")) {
                        if (order.order_type_flow.equalsIgnoreCase("single_pickup_multiple_drop_contacts")) {
                            Intent intentCall = new Intent(SplashActivity.this, DriverDetailsActivity.class);
                            intentCall.putExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA, notificationData);
                            intentCall.putExtra(Constants.IntentKey.KEY_DRIVER_STATUS, 1);
                            intentCall.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                            intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentCall);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                        } else {
                            if (order.payment_status.equalsIgnoreCase("Not Availabe") || order.payment_status.equalsIgnoreCase("Pending") || order.payment_status.equalsIgnoreCase("Not Available")) {
                                String paybutton = "payButton";
                                Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                                endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                                endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                                endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, paybutton);
                                endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
                                endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(endTripIntent);
                                if (listner != null)
                                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                                finish();
                                //show trip summary screen with payment button
                            } else if (order.ratings_status.equals("Not Availabe") || order.ratings_status.equals("Pending") || order.ratings_status.equals("Not Available")) {
                                String rayLayout = "ratingLayout";
                                Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                                endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                                endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                                endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, rayLayout);
                                endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
                                endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(endTripIntent);
                                if (listner != null)
                                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                                finish();
                                //show trip summary screen with rating layout
                            } else {
                                Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                if (listner != null)
                                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                                finish();
                            }
                        }
                    } else if (order.order_status.equalsIgnoreCase("trip_ended")) {
                        if (order.payment_status.equalsIgnoreCase("Not Availabe") || order.payment_status.equalsIgnoreCase("Pending") || order.payment_status.equalsIgnoreCase("Not Available")) {
                            String paybutton = "payButton";
                            Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                            endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, paybutton);
                            endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(endTripIntent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                            //show trip summary screen with payment button
                        } else if (order.ratings_status.equals("Not Availabe") || order.ratings_status.equals("Pending") || order.ratings_status.equals("Not Available")) {
                            String rayLayout = "ratingLayout";
                            Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                            endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, rayLayout);
                            endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(endTripIntent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                            //show trip summary screen with rating layout
                        } else {
                            Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                        }
                    } else if (order.order_status.equals("Driver Canceled")) {

                        mFirebaseInstance.getReference("orders").child(orderID).child("order_status").setValue("Looking for Drivers", (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                Log.e("database error", databaseError.getMessage());
                            } else {
                                if (listner != null)
                                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                                Intent intent = new Intent(SplashActivity.this, LoadingActivity.class);
                                intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                                intent.putExtra(Constants.IntentKey.KEY_LAUNCH_HOME, true);
                                intent.putExtra(Constants.IntentKey.KEY_PAGE_TITLE, "Whizzer cancelled..Looking for another");
                                intent.putExtra("drivercanc", "one");
                                startActivity(intent);

                                finish();
                            }
                        });


                    } else if (order.order_status.equals("Order Canceled")) {
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        showOrderCancelDialog(order.cancel_option.reason);
                    } else if (order.order_status.equals("Canceled") || order.order_status.equals("Customer_Canceled")) {
                        Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    } else if (order.order_status.equals("Completed")) {
                        if (order.payment_status.equalsIgnoreCase("Not Availabe") || order.payment_status.equalsIgnoreCase("Pending") || order.payment_status.equalsIgnoreCase("Not Available")) {
                            String paybutton = "payButton";
                            Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                            endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, paybutton);
                            endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, "");
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(endTripIntent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                            //show trip summary screen with payment button
                        } else if (order.ratings_status.equals("Not Available") || order.ratings_status.equals("Pending") || order.ratings_status.equals("Not Availabe")) {
                            String rayLayout = "ratingLayout";
                            Intent endTripIntent = new Intent(SplashActivity.this, EndTripSummaryActivity.class);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_ORDER_ID, notificationData.order_id);
                            endTripIntent.putExtra(Constants.IntentKey.KEY_DRIVER_ID, notificationData.driver_id);
                            endTripIntent.putExtra(Constants.IntentKey.RATING_LAYOUT, rayLayout);
                            endTripIntent.putExtra(Constants.IntentKey.PAYMENT_BUTTON, "");
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            endTripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(endTripIntent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                            //show trip summary screen with rating layout
                        } else {
                            Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            if (listner != null)
                                mFirebaseDatabase.child(orderID).removeEventListener(listner);
                            finish();
                        }
                    } else {
                        Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();

                    }
                } else {
                    Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    if (listner != null)
                        mFirebaseDatabase.child(orderID).removeEventListener(listner);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("oncancel", "firebaasecanccel");
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
        if (dialog1.isShowing())
            dialog1.dismiss();
    }

    public void showOrderCancelDialog(String message) {
        if (orderCancelDialog != null)
            if (orderCancelDialog.isShowing())
                orderCancelDialog.dismiss();
        orderCancelDialog = new AlertDialog.Builder(SplashActivity.this).create();

        // Setting Dialog Title
        orderCancelDialog.setTitle("Sorry, your request could not be fulfilled");
        orderCancelDialog.setCancelable(false);
        // Setting Dialog Message
        orderCancelDialog.setMessage("Whizzer says " + message);
        // Setting OK Button
        orderCancelDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {
            sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "").apply();
            Intent intent = new Intent(SplashActivity.this, HomePagerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            orderCancelDialog.dismiss();
            finish();
        });

        orderCancelDialog.show();

    }
}

