package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplAuthorizeTransactionListener;
import com.simpl.android.sdk.SimplTransactionAuthorization;
import com.simpl.android.sdk.SimplUser;
import com.simpl.android.sdk.SimplUserApprovalListenerV2;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.OrderDetails;
import com.sprvtec.whizzy.vo.RazorPaymentResponse;
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
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 11/27/2018.
 */
public class PaymentOptionsActivity extends Activity implements PaymentResultListener {
    private RelativeLayout pay, payLater;
    private TextView payText, payLaterText, simplNote, amountText, razorNote;
    private SharedPreferences sp;
    private ValueEventListener listner;
    private static String TAG = "PaymentOptionsActivity";
    private DatabaseReference mFirebaseDatabase, mFirebaseDatabasePay;
    private String orderID;
    private float percentageSimpl, percentageRazor, razorpayCharges, simplpayCharges, totalFareRazor, totalFareSimpl;
    LinearLayout loadingLay;
    public static Activity fa;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        pay = findViewById(R.id.pay);
        payLater = findViewById(R.id.pay_later);
        payText = findViewById(R.id.razor_amount);
        payLaterText = findViewById(R.id.simpl_amount);
        simplNote = findViewById(R.id.simpl_note);
        amountText = findViewById(R.id.amount);
        razorNote = findViewById(R.id.razor_note);
        loadingLay = findViewById(R.id.loading);
        orderID = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        fa = this;

        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("orders");
        mFirebaseDatabasePay = mFirebaseInstance.getReference("ProcessingFees");

        updateOrderStatus();


        Simpl.getInstance().runInSandboxMode();
        simplCheck();
        findViewById(R.id.cash).setOnClickListener(v -> Utils.showDialogFailure(PaymentOptionsActivity.this, "Please give cash to whizzer"));

        findViewById(R.id.back).setOnClickListener(v -> finish());
        pay.setOnClickListener(v -> startPaymentViaRazorPay());
        payLater.setOnClickListener(v -> {


            //Simpl payment
            Simpl.getInstance().authorizeTransaction(PaymentOptionsActivity.this, (long) (totalFareSimpl * 100))
                    .execute(new SimplAuthorizeTransactionListener() {
                        @Override
                        public void onSuccess(final SimplTransactionAuthorization transactionAuthorization) {
                            Log.e("TransactionToken", transactionAuthorization.getTransactionToken());
                            pay.setVisibility(View.GONE);
                            payLater.setVisibility(View.GONE);
                            new PlaceOrderTask().execute(transactionAuthorization.getTransactionToken());
                        }

                        @Override
                        public void onError(final Throwable throwable) {
                            Log.d("generateToken Error", throwable.getMessage());
                        }
                    });
        });
    }

    private void simplCheck() {
//        SimplUser user = new SimplUser("mobeen@sprvtec.com", "7416212576");
        SimplUser user = new SimplUser("mobeen@sprvtec.com", sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        Simpl.getInstance()
                .isUserApproved(user)
                .addParam("transaction_amount_in_paise", (long) (100) + "") // mandatory 100 paisa = 1 rupee
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
                                payLater.setVisibility(View.VISIBLE);

                            } else {
                                payLater.setVisibility(View.GONE);

                            }
                        });
                    }

                    public void onError(final Throwable throwable) {
                        runOnUiThread(() -> {
                            Log.e("errorrr_whizzy", throwable.getMessage());
                            payLater.setVisibility(View.GONE);
                        });
                    }
                });
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

    private void updateOrderStatus() {
        listner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetails order = dataSnapshot.getValue(OrderDetails.class);

                // Check for null
                if (order == null) {
                    Log.e(TAG, "Order data is null!");

                    return;
                }
                if (!order.order_fare.equals(""))
                    updateUI(order);


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        };
        // User data change listener
        mFirebaseDatabase.child(orderID).addValueEventListener(listner);
////        mFirebaseDatabase.child(orderID).addListenerForSingleValueEvent(listner);
    }

    private void updateUI(OrderDetails order) {

        float intFare = 0;

        try {

            intFare = Integer.parseInt(order.order_fare);

        } catch (Exception e) {
            e.printStackTrace();
        }


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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listner != null)
            mFirebaseDatabase.child(orderID).removeEventListener(listner);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        pay.setVisibility(View.GONE);
        payLater.setVisibility(View.GONE);
        new GetRazorPayTask(PaymentOptionsActivity.this, (long) (totalFareRazor * 100), razorpayPaymentID).execute();

    }

    @Override
    public void onPaymentError(int i, String s) {
        Utils.showDialogFailure(PaymentOptionsActivity.this, s);
    }

    @SuppressLint("StaticFieldLeak")
    class GetRazorPayTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private RazorPaymentResponse razorPaymentResponse;
        //        private ProgressDialog progressDialog;
        private String paymentID;
        private long amountID;

        GetRazorPayTask(Context context, long amountID, String paymentID) {
            this.context = context;
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
                new UpdateOrderPaymentStatusTask(PaymentOptionsActivity.this, orderID, paymentID, "Completed", "Payment Successful with RazorPay", "RazorPay", razorpayCharges, percentageRazor, totalFareRazor).execute();
            } else if (!Utils.isNetConnected(context))
                showDialog4(context, paymentID, "No internet connection", "Please check your wifi/mobile data signal");
            else {

                showDialog4(context, paymentID, "Oops!!!", resp);
            }
        }

    }

    void showDialog4(final Context context, final String paymentID, String title, String message) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);

        // Setting Dialog Message
        alertDialog.setMessage(message);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            new GetRazorPayTask(PaymentOptionsActivity.this, (long) (totalFareRazor * 100), paymentID).execute();
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
                    new UpdateOrderPaymentStatusTask(PaymentOptionsActivity.this, orderID, stParams, "Completed", "Payment Successful with Simpl", "Simpl", simplpayCharges, percentageSimpl, totalFareSimpl).execute();

                } else {
                    Utils.showDialogOpps(PaymentOptionsActivity.this, data.getString("error"));

                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showDialogOpps(PaymentOptionsActivity.this, "Unable to process your request. Please try again.");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateOrderPaymentStatusTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private UpdateOrderPaymentModeResponse updateOrderPaymentModeResponse;
        //        private ProgressDialog progressDialog;
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


            String json = Webservice.callPostService12(Webservice.UPDATE_ORDER_PAYMENT_STATUS, nameValuePairs);
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
//                return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
//            progressDialog.dismiss();
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentOptionsActivity.this);
                builder.setMessage("Payment Successful");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
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

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            new UpdateOrderPaymentStatusTask(PaymentOptionsActivity.this, orderID, paymentId, "Completed", desc, paymentMode, paymentCharges, paymentPercentage, totalFare).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

}
