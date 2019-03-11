package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplAuthorizeTransactionListener;
import com.simpl.android.sdk.SimplTransactionAuthorization;
import com.simpl.android.sdk.SimplUser;
import com.simpl.android.sdk.SimplUserApprovalListenerV2;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.FeedbackOptionsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.FeedbackOption;
import com.sprvtec.whizzy.vo.GetTripSummaryRes;
import com.sprvtec.whizzy.vo.OrderDetails;
import com.sprvtec.whizzy.vo.RazorPaymentResponse;
import com.sprvtec.whizzy.vo.TripSummary;
import com.sprvtec.whizzy.vo.UpdateOrderPaymentModeResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sowjanya on 7/28/2017.
 */

public class EndTripSummaryActivity extends Activity implements PaymentResultListener {
    private TextView fare;
    private TextView amountText;
    private TextView payText;
    private TextView payLaterText;
    private TextView payPending;
    private TextView simplNote;
    private TextView razorNote;
    private TextView expens_amt_whizyycharge;
    private LinearLayout ratingLay;
    private RatingBar ratingBar;
    private String orderID;
    private String driverID;
    private SharedPreferences sp;
    private DatabaseReference mFirebaseFeedbackOptions, mFirebaseDatabase, mFirebaseDatabasePay;
    private String TAG = "EndTripSummaryActivity";
    private boolean isSimplEnabled = false;
    private ValueEventListener listner;
    LinearLayout loadingLay;
    Dialog dialog1;
    ImageView down;
    private RelativeLayout payButton, payLaterButton, Cash;
    private float percentageSimpl, percentageRazor, razorpayCharges, simplpayCharges, totalFareRazor, totalFareSimpl;
    private String transportDes = "", pickName = "", picNumber = "", pickMoreInf = "";
    private boolean monthlyBilling;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_trip_summary);
        Checkout.preload(getApplicationContext());


        down = findViewById(R.id.abcs);
        loadingLay = findViewById(R.id.loading);
        loadingLay.setEnabled(false);
        dialog1 = new Dialog(EndTripSummaryActivity.this);
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
                    if (dialog1.isShowing() && !EndTripSummaryActivity.this.isFinishing())
                        dialog1.dismiss();
                } else {
                    if (!EndTripSummaryActivity.this.isFinishing()) {
                        dialog1.show(); //show dialog
                    }
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });


        sp = PreferenceManager.getDefaultSharedPreferences(this);
        fare = findViewById(R.id.fare);
        TextView v_details = findViewById(R.id.view_details);
        expens_amt_whizyycharge = findViewById(R.id.fare_sexp_whizzycharge);
        payButton = findViewById(R.id.pay);
        payText = findViewById(R.id.razor_amount);
        payLaterText = findViewById(R.id.simpl_amount);
        simplNote = findViewById(R.id.simpl_note);
        amountText = findViewById(R.id.amount);
        razorNote = findViewById(R.id.razor_note);
        payLaterButton = findViewById(R.id.pay_later);
        payPending = findViewById(R.id.paym_pen);
        Cash = findViewById(R.id.cash);
        ratingLay = findViewById(R.id.rating_lay);


        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("orders");
        mFirebaseDatabasePay = mFirebaseInstance.getReference("ProcessingFees");
        mFirebaseFeedbackOptions = mFirebaseInstance.getReference("trip_feedback_reasons_customer");
        ratingBar = findViewById(R.id.rating_bar);

        Cash.setOnClickListener(v -> Utils.showDialogFailure(EndTripSummaryActivity.this, "Please give cash to whizzer"));
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            String ratingVal = (int) rating + "_star";
            Log.e("rating", rating + " " + ratingVal);
            if (rating > 0) {
                getFeedbackOptionsFireBase(ratingVal);
            }
        });
        orderID = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        driverID = getIntent().getStringExtra(Constants.IntentKey.KEY_DRIVER_ID);
        String paybutton = getIntent().getStringExtra(Constants.IntentKey.PAYMENT_BUTTON);
        String ratLayout = getIntent().getStringExtra(Constants.IntentKey.RATING_LAYOUT);
        if (!paybutton.equalsIgnoreCase("")) {
            isSimplEnabled = false;
            payButton.setVisibility(View.VISIBLE);
            ratingLay.setVisibility(View.GONE);
        }
        if (!ratLayout.equalsIgnoreCase("")) {
            isSimplEnabled = true;
            payButton.setVisibility(View.GONE);
            payLaterButton.setVisibility(View.GONE);
            Cash.setVisibility(View.GONE);
            ratingLay.setVisibility(View.VISIBLE);
        }

        v_details.setOnClickListener(view -> {
            Intent ine = new Intent(EndTripSummaryActivity.this, ViewDetailsActivity.class);
            ine.putExtra("order_id", orderID);
            startActivity(ine);
        });

        new GetTripSummaryTask(EndTripSummaryActivity.this, orderID).execute();


        Log.d("Simpl", Simpl.getInstance().toString());

        payButton.setOnClickListener(v -> startPaymentViaRazorPay());
        payLaterButton.setOnClickListener(v -> {


            //Simpl payment
            Simpl.getInstance().authorizeTransaction(EndTripSummaryActivity.this, (long) (totalFareSimpl * 100))
                    .execute(new SimplAuthorizeTransactionListener() {
                        @Override
                        public void onSuccess(final SimplTransactionAuthorization transactionAuthorization) {
                            Log.e("TransactionToken", transactionAuthorization.getTransactionToken());
                            payButton.setVisibility(View.GONE);
                            payLaterButton.setVisibility(View.GONE);
                            new PlaceOrderTask().execute(transactionAuthorization.getTransactionToken());
                        }

                        @Override
                        public void onError(final Throwable throwable) {
                            Log.d("generateToken Error", throwable.getMessage());
                        }
                    });
        });

        updateOrderStatus(orderID);

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

                if (order.payment_status.equalsIgnoreCase("Completed")) {
                    ratingLay.setVisibility(View.VISIBLE);
                    payButton.setVisibility(View.GONE);
                    payLaterButton.setVisibility(View.GONE);
                    if (order.payment_mode.equalsIgnoreCase("Cash"))
                        payPending.setText("Payment done through Cash");
                    else if (order.payment_mode.equalsIgnoreCase("N/A"))
                        payPending.setText("Payment Done");
                    else
                        payPending.setText("Payment done through Card");
                    Log.e("updated fare", order.order_fare);
//                    fare.setText("₹ " + order.order_fare);
                    Cash.setVisibility(View.GONE);
                }
                transportDes = order.transport_description;
                pickName = order.pickup_contact_name;
                picNumber = order.pickup_contact_number;
                pickMoreInf = order.pickup_contact_more_info;

            }

            @Override
            public void onCancelled(DatabaseError error) {
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

    private void startPaymentViaRazorPay() {
        Checkout checkout = new Checkout();
        //Checkout.clearUserData(this);
        checkout.setImage(R.drawable.whizzy_app_icon);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Whizzy Logistic Technologies Private Limited");
            options.put("description", "");
            options.put("currency", "INR");
            options.put("amount", (long) (totalFareRazor * 100));

            JSONObject preFill = new JSONObject();
            preFill.put("email", "  support@whizzy.in");
            preFill.put("contact", sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));

            options.put("prefill", preFill);

            JSONObject notes = new JSONObject();
            notes.put("order_id", orderID);

            options.put("notes", notes);

            checkout.open(activity, options);
        } catch (Exception e) {
            Utils.showDialogOpps(activity, "Error in payment: " + e.getMessage());
        }

    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        payButton.setVisibility(View.GONE);
        payLaterButton.setVisibility(View.GONE);
        new GetRazorPayTask(EndTripSummaryActivity.this, (long) (totalFareRazor * 100), razorpayPaymentID).execute();

    }

    @Override
    public void onPaymentError(int i, String s) {
        Utils.showDialogFailure(EndTripSummaryActivity.this, s);
    }

    @SuppressLint("StaticFieldLeak")
    class GetRazorPayTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private RazorPaymentResponse razorPaymentResponse;
        private String paymentID;
        private long amountID;

        GetRazorPayTask(Context context, long amountID, String paymentID) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.paymentID = paymentID;
            this.amountID = amountID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.AMOUNT_ID, String.valueOf(amountID)));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_ID, paymentID));


            String json = Webservice.callPostServiceRazorPay(Webservice.RAZOR_PAYMENT, nameValuePairs);
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

            if (status.equalsIgnoreCase("captured")) {
                try {
                    razorPaymentResponse = new Gson().fromJson(json, RazorPaymentResponse.class);
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
//                return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                new UpdateOrderPaymentStatusTask(EndTripSummaryActivity.this, orderID, paymentID, "Completed", "Payment Successful with RazorPay", "RazorPay", razorpayCharges, percentageRazor, totalFareRazor).execute();
            } else if (!Utils.isNetConnected(context))
                showDialog4(context, paymentID, "No internet connection", "Please check your wifi/mobile data signal");
            else {

                showDialog4(context, paymentID, "Oops!!!", resp);
            }
        }

    }

    void showDialog4(final Context context, final String paymentID, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {


            alertDialog.dismiss();
            new GetRazorPayTask(EndTripSummaryActivity.this, (long) (totalFareRazor * 100), paymentID).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateOrderPaymentStatusTask extends AsyncTask<String, Void, String> {
        private Context context;
        private UpdateOrderPaymentModeResponse updateOrderPaymentModeResponse;
        private String orderID, paymentNumber, paymentStaus, paymentDescription, paymentMode;
        private float paymentCharges, paymentPercentage, totalFare;

        UpdateOrderPaymentStatusTask(Context context, String orderID, String paymentNumber, String paymentStaus, String paymentDescription, String paymentMode, float paymentCharges, float paymentPercentage, float totalFare) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.orderID = orderID;
            this.paymentNumber = paymentNumber;
            this.paymentStaus = paymentStaus;
            this.paymentDescription = paymentDescription;
            this.paymentMode = paymentMode;
            this.paymentCharges = paymentCharges;
            this.paymentPercentage = paymentPercentage;
            this.totalFare = totalFare;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_ID, String.valueOf(orderID)));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_TRANSACTION_NUMBER, paymentNumber));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_STATUS, paymentStaus));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_TRANSACTION_DESCRIPTION, paymentDescription));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_MODE, paymentMode));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_GATEWAY_CHARGES, String.valueOf(paymentCharges)));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_GATEWAY_PERCENTAGE, String.valueOf(paymentPercentage)));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_FARE, String.valueOf(totalFare)));
            if (monthlyBilling) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.MONTHLY_BILLING, "Yes"));
                nameValuePairs.add(new BasicNameValuePair(Webservice.EXPENSE_PAYMENT_MODE, paymentMode));
            }

            String json = Webservice.callPostService12(Webservice.UPDATE_ORDER_PAYMENT_STATUS, nameValuePairs);
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
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    updateOrderPaymentModeResponse = new Gson().fromJson(json, UpdateOrderPaymentModeResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    updateOrderPaymentModeResponse = new Gson().fromJson(json, UpdateOrderPaymentModeResponse.class);

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
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EndTripSummaryActivity.this);
                builder.setMessage("Payment Successful");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    payButton.setVisibility(View.GONE);
                    payLaterButton.setVisibility(View.GONE);
                    Cash.setVisibility(View.GONE);
                    ratingLay.setVisibility(View.VISIBLE);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else if (!Utils.isNetConnected(context))
                showDialog3(context, paymentNumber, "No internet connection", "Please check your wifi/mobile data signal", paymentMode, paymentCharges, paymentPercentage, totalFare, paymentDescription);
            else {
                if (updateOrderPaymentModeResponse != null && !updateOrderPaymentModeResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog3(context, paymentNumber, "Oops!!!", "Something went wrong Please try again after sometime!", paymentMode, paymentCharges, paymentPercentage, totalFare, paymentDescription);
            }
        }

    }

    void showDialog3(final Context context, final String paymentId, String title, String message, final String paymentMode, final float paymentCharges, final float paymentPercentage, final float totalFare, final String desc) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {


            alertDialog.dismiss();
            new UpdateOrderPaymentStatusTask(EndTripSummaryActivity.this, orderID, paymentId, "Completed", desc, paymentMode, paymentCharges, paymentPercentage, totalFare).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void simplCheck() {
        //SimplUser user = new SimplUser("mobeen@sprvtec.com", "7416212576");
        SimplUser user = new SimplUser("mobeen@sprvtec.com", sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        Simpl.getInstance()
                .isUserApproved(user)
                .addParam("transaction_amount_in_paise", (long) (totalFareSimpl * 100) + "") // mandatory
//                .addParam("user_location", "18.9750,72.8258")      // optional
//                .addParam("member_since", "2016-01-08")            // optional
                .execute(new SimplUserApprovalListenerV2() {
                    public void onSuccess(
                            final boolean isApproved,
                            final String buttonText,
                            final boolean showSimplIntroduction
                    ) {
                        runOnUiThread(() -> {
                            if (isApproved) {
                                if (totalFareSimpl != 0 && ratingLay.getVisibility() == View.GONE)
                                    payLaterButton.setVisibility(View.VISIBLE);
                                //payLaterButton.setBackgroundResource(R.drawable.button_blue);
                                payLaterButton.setEnabled(true);
                            } else {
                                payLaterButton.setVisibility(View.GONE);
                                //ratingLay.setVisibility(View.VISIBLE);
                                /*payButton.setBackgroundResource(R.drawable.button_disable);
                                payButton.setEnabled(false);*/
                            }
//                                payButton.setVisibility(isApproved ? View.VISIBLE : View.INVISIBLE);
                        });
                    }

                    public void onError(final Throwable throwable) {
                        runOnUiThread(() -> {
//                                payButton.setVisibility(View.INVISIBLE);
//                                payButton.setBackgroundResource(R.drawable.button_disable);
                            //  payButton.setEnabled(false);
                            Log.e("errorrr_whizzy", throwable.getMessage());
                        });
                    }
                });
    }


    @SuppressLint("StaticFieldLeak")
    class PlaceOrderTask extends AsyncTask<String, Void, JSONObject> {
        String stParams = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(final String... params) {
            stParams = params[0];
            // Replace following HTTP request logic with sophisticated alternatives
            try {
                URL url;
                URLConnection urlConnection;
                url = new URL("http://services.whizzy.in:3001/simpl?order_id=" + orderID);
                urlConnection = url.openConnection();
                urlConnection.setRequestProperty("transaction_token", params[0]);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String inputLine;
                StringBuffer sb = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                br.close();
                // Expecting a JSON response from the remote server
                String responseJson = sb.toString();
                return new JSONObject(responseJson);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            super.onPostExecute(data);
            loadingLay.setVisibility(View.GONE);
            try {
                if (data.getBoolean("success")) {
                    new UpdateOrderPaymentStatusTask(EndTripSummaryActivity.this, orderID, stParams, "Completed", "Payment Successful with Simpl", "Simpl", simplpayCharges, percentageSimpl, totalFareSimpl).execute();


                } else {
                    Utils.showDialogOpps(EndTripSummaryActivity.this, data.getString("error"));

                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showDialogOpps(EndTripSummaryActivity.this, "Unable to process your request. Please try again.");
            }
        }
    }

    private void getFeedbackOptionsFireBase(final String ratingValue) {
        final List<FeedbackOption> options = new ArrayList<>();
        mFirebaseFeedbackOptions.child(ratingValue).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count ", "" + snapshot.getChildrenCount());

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String post = postSnapshot.getValue(String.class);
                    FeedbackOption option = new FeedbackOption();
                    option.customer_feedback_option = post;
                    option.ratings = ratingValue;
                    options.add(option);
                    Log.e("Get Data", post);
                }
                showFeedbackActionSheet(options);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void showFeedbackActionSheet(List<FeedbackOption> options) {

        final Dialog myDialog = new Dialog(EndTripSummaryActivity.this, R.style.CustomTheme);

        myDialog.setContentView(R.layout.actionsheet);
        TextView title = myDialog.findViewById(R.id.title);
        TextView cancel = myDialog.findViewById(R.id.cancel);
        title.setText("Please choose a rating reason");

        cancel.setOnClickListener(v -> {

            myDialog.dismiss();
            ratingBar.setRating(0);

        });


        ListView listView = myDialog.findViewById(R.id.list);
        FeedbackOptionsAdapter adapter = new FeedbackOptionsAdapter(EndTripSummaryActivity.this, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            FeedbackOption option = (FeedbackOption) parent.getItemAtPosition(position);
            new GiveDriverFeedbackTask(EndTripSummaryActivity.this, orderID, driverID, option.customer_feedback_option, option.ratings).execute();
            myDialog.dismiss();
        });

        myDialog.getWindow().getAttributes().windowAnimations = R.anim.slide_up_anim;

        myDialog.show();

        myDialog.getWindow().setGravity(Gravity.BOTTOM);
        myDialog.setCancelable(false);


    }

    public void onStartTransaction(View view) {
        PaytmPGService Service = PaytmPGService.getStagingService();
        Map<String, String> paramMap = new HashMap<String, String>();

        // these are mandatory parameters
//        {"Response":{"MID":"SPRVTe81542040168785","ORDER_ID":"order67890","CUST_ID":"cust123","INDUSTRY_TYPE_ID":"Retail","CHANNEL_ID":"WAP","TXN_AMOUNT":"100","WEBSITE":"APP_STAGING","CALLBACK_URL":"https:\/\/pguat.paytm.com\/paytmchecksum\/paytmCallback.jsp","EMAIL":"abc@gmail.com","MOBILE_NO":"9999999999","CHECKSUMHASH":"MwSAAWJRwrP3vkW\/p+TVWRny1kXoabU6o3zHYrMm7ksz9JQr6nWhiKDBKi4s6WAj1qzvnCLy5b3LF724ehlgTHdtvFhu62VNzfyPjrKmv3Y="}}
//        paramMap.put("ORDER_ID", "order67890");
//        paramMap.put("MID", "SPRVTe81542040168785");
//        paramMap.put("CUST_ID", "cust123");
//        paramMap.put("CHANNEL_ID", "WAP");
//        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
//        paramMap.put("WEBSITE", "APP_STAGING");
//        paramMap.put("TXN_AMOUNT", "100");
//        paramMap.put("THEME", "merchant");
//        paramMap.put("REQUEST_TYPE", "DEFAULT");
//        paramMap.put("EMAIL", "abc@gmail.com");
//        paramMap.put("MOBILE_NO","9999999999");


        paramMap.put("ORDER_ID", orderID);
        paramMap.put("MID", "SPRVTe81542040168785");
        paramMap.put("CUST_ID", sp.getString(Constants.PreferenceKey.KEY_USER_ID, ""));
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("WEBSITE", "APP_STAGING");
        paramMap.put("TXN_AMOUNT", "20");
        paramMap.put("THEME", "merchant");
        paramMap.put("REQUEST_TYPE", "DEFAULT");
        paramMap.put("EMAIL", "sowjanya14.u@gmail.com");
        paramMap.put("CALLBACK_URL", Webservice.VERIFY_CHECKSUM);
//        paramMap.put("CALLBACK_URL", " http://services.whizzy.in:3001/VerifyChecksum");
        paramMap.put("MOBILE_NO", sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        PaytmOrder Order = new PaytmOrder((HashMap<String, String>) paramMap);

        PaytmMerchant Merchant = new PaytmMerchant(
                Webservice.GENERATE_CHECKSUM,
                Webservice.VERIFY_CHECKSUM);
//        PaytmMerchant Merchant = new PaytmMerchant(
//                " http://services.whizzy.in:3001/GenerateChecksum",
//                " http://services.whizzy.in:3001/VerifyChecksum");

        Service.initialize(Order, null);

        Service.startPaymentTransaction(this, true, true,
                new PaytmPaymentTransactionCallback() {
                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {
                        // Some UI Error Occurred in Payment Gateway Activity.
                        // // This may be due to initialization of views in
                        // Payment Gateway Activity or may be due to //
                        // initialization of webview. // Error Message details
                        // the error occurred.
                        Log.e("UI ERROR", inErrorMessage);
                    }


                    @Override
                    public void onTransactionResponse(Bundle inResponse) {

                    }

                    @Override
                    public void networkNotAvailable() { // If network is not
                        // available, then this
                        // method gets called.
                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {
                        // This method gets called if client authentication
                        // failed. // Failure may be due to following reasons //
                        // 1. Server error or downtime. // 2. Server unable to
                        // generate checksum or checksum response is not in
                        // proper format. // 3. Server failed to authenticate
                        // that client. That is value of payt_STATUS is 2. //
                        // Error Message describes the reason for failure.
                        Log.e("ClientAuthFail", inErrorMessage);
                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode,
                                                      String inErrorMessage, String inFailingUrl) {
                        Log.e("Error loading web page", inErrorMessage);

                    }

                    // had to be added: NOTE
                    @Override
                    public void onBackPressedCancelTransaction() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

                    }

                });
    }

    @Override
    public void onBackPressed() {

    }

    @SuppressLint("StaticFieldLeak")
    private class GetTripSummaryTask extends AsyncTask<String, Void, String> {
        private Context context;
        private GetTripSummaryRes getTripSummaryRes;
        private String orderID;

        GetTripSummaryTask(Context context, String orderID) {
            this.context = context;
            this.orderID = orderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_TRIP_SUMMARY + "?" + Webservice.ORDER_ID + "=" + orderID;
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
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getTripSummaryRes = new Gson().fromJson(json, GetTripSummaryRes.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getTripSummaryRes = new Gson().fromJson(json, GetTripSummaryRes.class);
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
            loadingLay.setVisibility(View.GONE);

            if (resp == null) {
                if (getTripSummaryRes != null) {
                    updateUI(getTripSummaryRes.data);
                } else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");

            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (getTripSummaryRes != null && !getTripSummaryRes.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }

    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {


            alertDialog.dismiss();
            new GetTripSummaryTask(EndTripSummaryActivity.this, orderID).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void updateUI(final TripSummary data) {
        float intFare = 0;
        monthlyBilling = false;

        try {

            System.out.println("farecomingornot===" + data.fare);
//            String fr = "";
            if (data.monthly_billing) {
                monthlyBilling = true;
                intFare = (int) data.extra_expenses_amount;
//                fr = String.valueOf(data.extra_expenses_amount);
            } else {
                intFare = (int) data.fare;
//                fr = String.valueOf(data.fare);
            }
//            textViewAmt.setText(String.format("%.2f", value));
            fare.setText("₹ " + String.format("%.2f", intFare));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        fareAmount = (long) data.fare * 100;// convert rupee to pisa

//        distanceText.setText(data.distance);

        final String amount = String.valueOf(data.extra_expenses_amount), amount_wchr = data.delivery_charge;

        double value = 0, value2 = 0;
        if (amount != null && !amount.equals(""))
            value = Double.parseDouble(amount);
        if (amount_wchr != null && !amount_wchr.equals(""))
            value2 = Double.parseDouble(amount_wchr);
        expens_amt_whizyycharge.setText("(" + "Whizzy Charges : " + String.format("%.2f", value2)
                + "," + " Addl Expenses : " + String.format("%.2f", value) + ")");


        final float fare = intFare;
        //getting processing fee
        mFirebaseDatabasePay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("value", dataSnapshot.child("Simpl").getValue() + "");
                    percentageSimpl = ((Number) (dataSnapshot.child("Simpl").getValue())).floatValue();
                    percentageRazor = ((Number) (dataSnapshot.child("Razorpay").getValue())).floatValue();
                    Log.e("ProcessingFee", percentageSimpl + " " + percentageRazor);
                    razorpayCharges = Float.parseFloat(String.format("%.2f", (percentageRazor * fare) / 100));
//                    razorpayCharges = (percentageRazor * fare) / 100;
                    simplpayCharges = Float.parseFloat(String.format("%.2f", (percentageSimpl * fare) / 100));
//                    simplpayCharges = (percentageSimpl * fare) / 100;
                    totalFareRazor = razorpayCharges + fare;
                    payText.setText("₹ " + totalFareRazor);
                    amountText.setText("₹ " + fare);
                    razorNote.setText("Payment gateway charges : ₹ " + razorpayCharges);
                    totalFareSimpl = simplpayCharges + fare;
                    payLaterText.setText("₹ " + totalFareSimpl);
                    simplNote.setText("Payment gateway charges : ₹ " + simplpayCharges);
                    if (fare == 0) {
//                        new UpdateOrderPaymentStatusTask(EndTripSummaryActivity.this, orderID, "N/A", "Completed", "N/A", "N/A", 0, 0, data.fare).execute();
                        ratingLay.setVisibility(View.VISIBLE);
                        payButton.setVisibility(View.GONE);
                        payLaterButton.setVisibility(View.GONE);
//                        if (order.payment_mode.equalsIgnoreCase("Cash"))
//                            payPending.setText("Payment done through Cash");
//                        else
                        payPending.setText("Payment Done");
//                        Log.e("updated fare", order.order_fare);
//                        fare.setText("₹ " + order.order_fare);
                        Cash.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!isSimplEnabled) {
            Simpl.getInstance().runInSandboxMode();
            simplCheck();
        }
        findViewById(R.id.view_trip).setOnClickListener(v -> {
            data.transport_description = transportDes;
            data.pickup_contact_name = pickName;
            data.pickup_contact_number = picNumber;
            data.pickup_contact_more_info = pickMoreInf;
            Intent intent = new Intent(EndTripSummaryActivity.this, SummaryTripDetails.class);
            intent.putExtra(Constants.IntentKey.KEY_TRIP, data);
            startActivity(intent);
        });
    }

    @SuppressLint("StaticFieldLeak")
    class GiveDriverFeedbackTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        //        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String orderID, driverID, feedbackOption, ratings;

        GiveDriverFeedbackTask(Context context, String orderID, String driverID, String feedbackOption, String ratings) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.ratings = ratings;
            this.driverID = driverID;
            this.orderID = orderID;
            this.feedbackOption = feedbackOption;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_ID, orderID));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DRIVER_ID, driverID));
            nameValuePairs.add(new BasicNameValuePair(Webservice.FEEDBACK_OPTION, feedbackOption));
            nameValuePairs.add(new BasicNameValuePair(Webservice.RATINGS, ratings));


            String json = Webservice.callPostService1(Webservice.GIVE_DRIVER_FEEDBACK, nameValuePairs);
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
            loadingLay.setVisibility(View.GONE);
//            progressDialog.dismiss();
            if (resp == null) {
                Intent intent = new Intent(EndTripSummaryActivity.this, HomePagerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (!Utils.isNetConnected(context)) {
                ratingBar.setRating(0);
                Utils.showDialogOpps(context, "No internet connection");
            } else {
                ratingBar.setRating(0);
                Utils.showDialogOpps(context, resp);
            }
        }

    }


}
