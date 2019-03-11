package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.DropContact;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.NotificationData;
import com.sprvtec.whizzy.vo.OrderDetails;
import com.sprvtec.whizzy.vo.WhizzyVehicle;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Sowjanya on 6/21/2017.
 */

public class LoadingActivity extends Activity {
    private static final String TAG = "Loading Activity";
    private static AlertDialog internetAlertDialog;
    private boolean called = false;
    private String orderID;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private SharedPreferences sp;
    private Dialog dialog;
    private ValueEventListener listner;
    private boolean yes, active, yesState;

    Dialog dialog1;
    ImageView load;
    RequestQueue requestQueue;
    StringRequest stringRequest;
    private LocationInf pickupInfo, dropOffInfo;
    private WhizzyVehicle whizzyVehicle;
    private DropContact pickContact;
    private List<DropContact> dropContacts;
    private String description, pickLandmark, dropLandmark, coupn_code, discount_fare, discount, str_order_type;
    private Dialog scheduleDialog;
    private TextView behalf, noButton, yesButton;
    private OrderDetails order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_loading);
        active = true;
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        TextView title = findViewById(R.id.order_id_text);
        if (getIntent().getStringExtra(Constants.IntentKey.KEY_PAGE_TITLE) != null && !getIntent().getStringExtra(Constants.IntentKey.KEY_PAGE_TITLE).equals(""))
            title.setText(getIntent().getStringExtra(Constants.IntentKey.KEY_PAGE_TITLE));
        pickupInfo = getIntent().getParcelableExtra(Constants.IntentKey.KEY_PICKUP_LOCATION);
        dropOffInfo = getIntent().getParcelableExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION);
        whizzyVehicle = getIntent().getParcelableExtra(Constants.IntentKey.KEY_WHIZZY_VEHICLE);
        pickContact = getIntent().getParcelableExtra(Constants.IntentKey.KEY_PICKUP_CONTACT);
        dropContacts = getIntent().getParcelableArrayListExtra(Constants.IntentKey.KEY_DROP_CONTACT);
        description = getIntent().getStringExtra(Constants.IntentKey.KEY_DESC);
        pickLandmark = getIntent().getStringExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK);
        dropLandmark = getIntent().getStringExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK);
        coupn_code = getIntent().getStringExtra(Constants.IntentKey.COUPN_CODE);
        discount_fare = getIntent().getStringExtra(Constants.IntentKey.DISCOUNT_FARE);
        discount = getIntent().getStringExtra(Constants.IntentKey.DISCOUNT);

        orderID = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        load = findViewById(R.id.loading_img);
        str_order_type = sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "");

        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'orders' node
        mFirebaseDatabase = mFirebaseInstance.getReference("orders");

        requestQueue = Volley.newRequestQueue(this);

        String JSON_URL = "http://services.whizzy.in:8080/GetDriverDetails?order_id=" + orderID;

        stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                response -> {
                    //hiding the progressbar after completion
                    try {
                        //getting the whole json object from the response
                        JSONObject obj = new JSONObject(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    //displaying the error in toast if occurrs
                    if (!Utils.isNetConnected(LoadingActivity.this))
                        showDialog1(LoadingActivity.this, "No internet connection", "Please check your wifi/mobile data signal");
                    // no else becoz for failure response we are showing retry dialog based on firebase order_status update
                });


        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); // this to avoid multicalling


        if (getIntent().getIntExtra(Constants.IntentKey.KEY_PROCESSING, -1) == 0) {
            getOrder(orderID);
        } else if (getIntent().getIntExtra(Constants.IntentKey.KEY_PROCESSING, -1) == 1) {
            updateOrderStatus(orderID);
        } else if (getIntent().getIntExtra(Constants.IntentKey.KEY_PROCESSING, -1) == 2) {
            showDialogOpps(LoadingActivity.this);
        } else {
            Date currentTime = Calendar.getInstance().getTime();
            Log.e("time", currentTime.toString());
            mFirebaseDatabase.child(orderID).child("Android_api_call").child(currentTime.toString()).setValue("called");
            requestQueue.add(stringRequest);
            Log.e("called", "called");
            updateOrderStatus(orderID);
        }

        try {
            Glide.with(LoadingActivity.this)
                    .load(R.drawable.loadingscreen_full)
                    .into(load);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        if (yes)
            yesState = true;
    }


    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> alertDialog.dismiss());

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void callRetry() {
        mFirebaseInstance.getReference("orders").child(orderID).child("order_status").setValue("Looking for Drivers", (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e("database error", databaseError.getMessage());
                showRetryDialog1("Something went wrong..");
            } else {
                Date currentTime = Calendar.getInstance().getTime();
                Log.e("time", currentTime.toString());
                mFirebaseDatabase.child(orderID).child("Android_api_call").child(currentTime.toString()).setValue("called");
                requestQueue.add(stringRequest);
                Log.e("called", "called");
                updateOrderStatus(orderID);
            }
        });

    }


    private void showRetryDialog1(String message) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_retry);
        dialog.setCancelable(false);
        TextView title = dialog.findViewById(R.id.title);
        title.setText(message);
        yesButton = dialog.findViewById(R.id.yes);
        noButton = dialog.findViewById(R.id.no);
        behalf = dialog.findViewById(R.id.on_behalf);
        behalf.setVisibility(View.VISIBLE);
        behalf.setOnClickListener(view -> {
            if (order != null) {
                behalf.setEnabled(false);
                yesButton.setEnabled(false);
                noButton.setEnabled(false);
                behalf.setBackgroundResource(R.drawable.button_grayout);
                yesButton.setBackgroundResource(R.drawable.button_grayout);
//                noButton.setBackgroundResource(R.drawable.button_grayout);
                CreateOrderTask task = new CreateOrderTask(LoadingActivity.this);
                StartAsyncTaskInParallel1(task);
            } else {
                Log.e("order", "null");
            }
        });

        yesButton.setOnClickListener(v -> {
            dialog.dismiss();
            callRetry();
        });
        noButton.setOnClickListener(v -> {
            dialog.dismiss();
            sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "").apply();
            if (getIntent().getBooleanExtra(Constants.IntentKey.KEY_LAUNCH_HOME, false)) {
                Intent intentCall = new Intent(LoadingActivity.this, HomePagerActivity.class);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);
                if (listner != null)
                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                finish();
            } else {
                if (listner != null)
                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                finish();
            }
        });
        if (active) {
            dialog.show();

            Window window = dialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void updateOrderStatus(final String orderID) {

        if (listner != null)
            mFirebaseDatabase.child(orderID).removeEventListener(listner);
        listner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order = dataSnapshot.getValue(OrderDetails.class);

                // Check for null
                if (order == null) {
                    Log.e(TAG, "Order data is null!");
                    return;
                }
                Log.e(TAG, "Order data is changed!" + order.order_status + ", " + order.order_id + ", " + order.processing);
                if (order.order_status.equals("Accepted by Driver") || order.order_status.equals("start_trip") || order.order_status.equals("arrived_at_pickup") || order.order_status.equals("arrived_at_drop") || order.order_status.equals("trip_ended") || order.order_status.equals("Completed") || order.order_status.equals("Driver Canceled") || order.order_status.equals("Order Canceled")) {
                    if (!called) {
                        called = true;
                        NotificationData notificationData = new NotificationData();
                        notificationData.crn_number = order.crn_number;
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

                        Log.e("Called", "calleddddd");
                        Intent intentCall = new Intent(LoadingActivity.this, DriverDetailsActivity.class);
                        intentCall.putExtra(Constants.IntentKey.KEY_NOTIFICATION_DATA, notificationData);
//                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentCall);
                        if (listner != null)
                            mFirebaseDatabase.child(orderID).removeEventListener(listner);
                        finish();
                    }
                } else if (order.order_status.equals("No Vehicles Available")) {
//                    handler.removeCallbacks(runnable);
                    showRetryDialog1("No Whizzers available at the moment");
                    if (listner != null)
                        mFirebaseDatabase.child(orderID).removeEventListener(listner);
                } else if (order.order_status.equals("Exception")) {
                    showDialogOpps(LoadingActivity.this);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Utils.showDialogOpps(LoadingActivity.this, error.toException().toString());
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        };
        // User data change listener
        mFirebaseDatabase.child(orderID).addValueEventListener(listner);
    }


    public void showDialogOpps(final Context context) {
        if (internetAlertDialog != null)
            if (internetAlertDialog.isShowing())
                internetAlertDialog.dismiss();
        internetAlertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        internetAlertDialog.setTitle("Oops!!! Something went wrong Please try again after sometime!");
        internetAlertDialog.setCancelable(false);
        // Setting Dialog Message
        internetAlertDialog.setMessage("Please try again after sometime!");

        // Setting OK Button
        internetAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", (dialog, which) -> {
            internetAlertDialog.dismiss();
            sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "").apply();
            if (getIntent().getBooleanExtra(Constants.IntentKey.KEY_LAUNCH_HOME, false)) {
                Intent intentCall = new Intent(LoadingActivity.this, HomePagerActivity.class);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);
                if (listner != null)
                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                finish();
            } else {
                if (listner != null)
                    mFirebaseDatabase.child(orderID).removeEventListener(listner);
                finish();
            }


        });

        // Showing Alert Message
        try {
            internetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listner != null)
            mFirebaseDatabase.child(orderID).removeEventListener(listner);
//        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (listner != null)
            mFirebaseDatabase.child(orderID).removeEventListener(listner);
        if (yesState)
            yes = true;
        if (mFirebaseDatabase != null && listner != null)
            mFirebaseDatabase.child(orderID).addValueEventListener(listner);
    }


    @SuppressLint("StaticFieldLeak")
    class CreateOrderTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        private SharedPreferences sp;
        private String scheduleDateTime;


        CreateOrderTask(Context context) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar myCalendar = Calendar.getInstance();
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            java.text.DecimalFormat nft = new
                    java.text.DecimalFormat("#00.###");
            nft.setDecimalSeparatorAlwaysShown(false);
//            String month = (myCalendar.get(Calendar.MONTH) + 1) > 9 ? (myCalendar.get(Calendar.MONTH) + 1) + "" : "0" + (myCalendar.get(Calendar.MONTH) + 1);
//            String date = myCalendar.get(Calendar.DAY_OF_MONTH) > 9 ? myCalendar.get(Calendar.DAY_OF_MONTH) + "" : "0" + myCalendar.get(Calendar.DAY_OF_MONTH);
//            scheduleDateTime = date + "/" + month + "/" + myCalendar.get(Calendar.YEAR) + " " + (hour >= 13 ? (hour - 12) : hour) + ":" + minute + (hour >= 12 ? " PM" : " AM");
            scheduleDateTime = nft.format(myCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + nft.format((myCalendar.get(Calendar.MONTH) + 1)) + "/" + myCalendar.get(Calendar.YEAR) + " " + (hour >= 13 ? nft.format((hour - 12)) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " PM" : " AM");


        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            // behalf
            int tripType = 0;
            if (whizzyVehicle != null) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_ID, whizzyVehicle.vehicle_id));
                nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_NAME, whizzyVehicle.vehicle_name));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_REQUIRED, "false"));
                if (pickContact != null) {
                    nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NAME, pickContact.drop_contact_name));
                    nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NUMBER, pickContact.drop_contact_number));
                    nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_MORE_INFO, pickContact.drop_contact_more_info));
                } else {
                    nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NAME, sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, "")));
                    nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NUMBER, sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "")));
                }
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LATITUDE, pickupInfo.getLatitude() + ""));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LONGITUDE, pickupInfo.getLongitude() + ""));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LATITUDE, dropOffInfo.getLatitude() + ""));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LONGITUDE, dropOffInfo.getLongitude() + ""));
                nameValuePairs.add(new BasicNameValuePair(Webservice.TRANSPORT_DESC, description));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LANDMARK, pickLandmark));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_LANDMARK, dropLandmark));
                nameValuePairs.add(new BasicNameValuePair(Webservice.TRIP_TYPE, tripType == 0 ? "one_way" : "round_trip"));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_FULL_ADDRESS, pickupInfo.getLocationName() + "\n" + pickupInfo.getLocationAddress()));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_FULL_ADDRESS, dropOffInfo.getLocationName() + "\n" + dropOffInfo.getLocationAddress()));
                nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE, sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.APP_VERSION, Constants.APP_VERSION));
                //coupn stuff here
                nameValuePairs.add(new BasicNameValuePair(Webservice.COUPN_CODE, coupn_code));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNTED_FARE, discount_fare));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNT, discount));
                nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULED_ORDER, "Yes"));
                if (scheduleDateTime != null)
                    nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_DATE_TIME, scheduleDateTime));
                if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME) != null)
                    nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME)));
                if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID) != null)
                    nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)));

                JSONArray jArry = new JSONArray();
                for (DropContact contact : dropContacts) {
                    JSONObject ob = new JSONObject();
                    try {
                        ob.put(Webservice.DROPOFF_NAME, contact.drop_contact_name);
                        ob.put(Webservice.DROPOFF_NUMBER, contact.drop_contact_number);
                        ob.put(Webservice.DROP_MORE_INFO, contact.drop_contact_more_info);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    jArry.put(ob);
                }
//                nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_CONTACT_DETAILS, jArry.toString()));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_CONTACT_DETAILS, sp.getString(Webservice.DROP_CONTACT_DETAILS, "")));

                if (dropContacts.size() > 1)
                    nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE_FLOW, "single_pickup_multiple_drop_contacts"));
                else
                    nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE_FLOW, "single_pickup_single_drop_contact"));


                if (str_order_type.equalsIgnoreCase("Buy")) {
                    nameValuePairs.add(new BasicNameValuePair(Webservice.BUY_LOCATION, "Specific"));
                }

            } else {
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_ID, order.vehicle_id));
                nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_NAME, order.vehicle_name));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_REQUIRED, "false"));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NAME, order.pickup_contact_name));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NUMBER, order.pickup_contact_number));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_MORE_INFO, order.pickup_contact_more_info));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LATITUDE, order.pickup_latitude));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LONGITUDE, order.pickup_longitude));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LATITUDE, order.drop_latitude));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LONGITUDE, order.drop_longitude));
                nameValuePairs.add(new BasicNameValuePair(Webservice.TRANSPORT_DESC, order.transport_description));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LANDMARK, order.pickup_landmark));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_LANDMARK, order.drop_landmark));
                nameValuePairs.add(new BasicNameValuePair(Webservice.TRIP_TYPE, tripType == 0 ? "one_way" : "round_trip"));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_FULL_ADDRESS, order.pickup_full_address));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_FULL_ADDRESS, order.drop_full_address));
                nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE, sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.APP_VERSION, Constants.APP_VERSION));

                nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULED_ORDER, "Yes"));
                if (scheduleDateTime != null)
                    nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_DATE_TIME, scheduleDateTime));

                //coupn stuff here
                nameValuePairs.add(new BasicNameValuePair(Webservice.COUPN_CODE, sp.getString(Webservice.COUPN_CODE, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNTED_FARE, sp.getString(Webservice.DISCOUNTED_FARE, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNT, sp.getString(Webservice.DISCOUNT, "")));

                nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_NAME, sp.getString(Webservice.BUSINESS_NAME, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_ID, sp.getString(Webservice.BUSINESS_ID, "")));


                nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_CONTACT_DETAILS, sp.getString(Webservice.DROP_CONTACT_DETAILS, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE_FLOW, sp.getString(Webservice.ORDER_TYPE_FLOW, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.BUY_LOCATION, sp.getString(Webservice.BUY_LOCATION, "")));
//


            }

            nameValuePairs.add(new BasicNameValuePair(Webservice.OPERATING_SYSTEM, "Android"));
            nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_ON_MY_BEHALF, "Yes"));

            String json = Webservice.callPostService1(Webservice.CREATE_ORDER, nameValuePairs);
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
                    return e.getMessage();
                }
            } else {
                try {
                    // Convert String to json object
                    createOrderResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
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
            if (resp == null) {

                if (createOrderResponse != null && !createOrderResponse.data.order_id.equals("")) {
                    sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, "").apply();
                    showScheduleDialog("Our agents will allocate a Whizzer shortly!");

                }
            } else if (!Utils.isNetConnected(context)) {
                behalf.setEnabled(true);
                yesButton.setEnabled(true);
                noButton.setEnabled(true);
                behalf.setBackgroundResource(R.drawable.button_blue);
                yesButton.setBackgroundResource(R.drawable.button_blue);
//                noButton.setBackgroundResource(R.drawable.button_red);
                showDialog3(context, "No internet connection", "Please check your wifi/mobile data signal");
            } else {
                behalf.setEnabled(true);
                yesButton.setEnabled(true);
                noButton.setEnabled(true);
                behalf.setBackgroundResource(R.drawable.button_blue);
                yesButton.setBackgroundResource(R.drawable.button_blue);
//                noButton.setBackgroundResource(R.drawable.button_red);
                if (createOrderResponse != null && !createOrderResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog3(context, "Oops!!!", "Something went wrong Please try again after sometime!");

            }
        }
    }

    void showDialog3(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();

            behalf.setEnabled(false);
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            behalf.setBackgroundResource(R.drawable.button_grayout);
            yesButton.setBackgroundResource(R.drawable.button_grayout);
//            noButton.setBackgroundResource(R.drawable.button_grayout);
            CreateOrderTask task = new CreateOrderTask(LoadingActivity.this);
            StartAsyncTaskInParallel1(task);
//                }
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel1(
            CreateOrderTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showScheduleDialog(String message) {
        if (scheduleDialog != null && scheduleDialog.isShowing()) {
            scheduleDialog.dismiss();
        } else {
            scheduleDialog = new Dialog(LoadingActivity.this);

            scheduleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            scheduleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            scheduleDialog.setContentView(R.layout.dialog_interest_settings);
            scheduleDialog.setCancelable(false);
            TextView title = scheduleDialog.findViewById(R.id.text);
            TextView heading = scheduleDialog.findViewById(R.id.heading);
            heading.setText(getApplicationContext().getResources().getString(R.string.request_noted));
            heading.setVisibility(View.VISIBLE);
            title.setText(message);
            Button ok = scheduleDialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                Intent intentCall = new Intent(LoadingActivity.this, HomePagerActivity.class);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);
                scheduleDialog.dismiss();
                finish();
            });

            try {
                scheduleDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void getOrder(final String orderID) {

        DatabaseReference mFirebaseDatabase1 = mFirebaseInstance.getReference("orders");
        mFirebaseDatabase1.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order = dataSnapshot.getValue(OrderDetails.class);
                showRetryDialog1("No Whizzers available at the moment");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
