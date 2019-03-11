package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.DropContactdetailsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.DropContact;
import com.sprvtec.whizzy.vo.TripDetailsResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by Sowjanya on 9/26/2017.
 */

public class TripDetailsActivity extends Activity {

    private TextView fare;
    private TextView pickupAddress;
    private TextView dropAddress;
    private TextView timeText;
    private TextView bike_n;
    private TextView pay_mode;
    private TextView distanceText;
    private TextView expens_amt_whizyycharge;
    private TextView item_list;
    private TextView request_fulfil;
    private TextView pickupDetails;


    LinearLayout item_delive;
    String orderid;
    ImageView down;
    private DatabaseReference mFirebaseDatabase1;
    private ValueEventListener listener;


    ImageView emp_one, emp_two, emp_three, emp_four, emp_five;
    private ListView droplist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        down = findViewById(R.id.abcs);


        emp_one = findViewById(R.id.empty_one);
        emp_two = findViewById(R.id.empty_two);
        emp_three = findViewById(R.id.empty_three);
        emp_four = findViewById(R.id.empty_four);
        emp_five = findViewById(R.id.empty_five);
        pickupDetails = findViewById(R.id.pick_up_details);
        droplist = findViewById(R.id.drop_contacts_list);


        fare = findViewById(R.id.price);
        TextView v_details = findViewById(R.id.view_details);
        item_list = findViewById(R.id.itemlsit);
        expens_amt_whizyycharge = findViewById(R.id.fare_sexp_whizzycharge);
        request_fulfil = findViewById(R.id.fulifiled_cut_name);
        pickupAddress = findViewById(R.id.pic_up_address);
        dropAddress = findViewById(R.id.drop_off_address);
        timeText = findViewById(R.id.time);
        distanceText = findViewById(R.id.distance);
        pay_mode = findViewById(R.id.paymode);
        bike_n = findViewById(R.id.bike_num);
        request_fulfil = findViewById(R.id.fulifiled_cut_name);


        item_delive = findViewById(R.id.item_del);

        item_delive.setOnClickListener(view -> {
            if (item_list.getVisibility() == View.VISIBLE) {
                item_list.setVisibility(View.GONE);
                down.setImageResource(R.drawable.down_icon);
            } else {
                item_list.setVisibility(View.VISIBLE);
                down.setImageResource(R.drawable.up_icon);


            }
        });


        orderid = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        System.out.println("orderidddd" + orderid);
        v_details.setOnClickListener(view -> {
            Intent ine = new Intent(TripDetailsActivity.this, ViewDetailsActivity.class);
            ine.putExtra("order_id", orderid);
            startActivity(ine);
        });

        (findViewById(R.id.back)).setOnClickListener(v -> finish());
        (findViewById(R.id.help)).setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "support@whizzy.in", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });

        new GetTripSummaryTask(TripDetailsActivity.this).execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class GetTripSummaryTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private TripDetailsResponse getTripSummaryRes;
        private ProgressDialog progressDialog;


        GetTripSummaryTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
//            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.DRIVER_GET_ORDER_DETAIL + "?" + Webservice.ORDER_ID + "=" + orderid;
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
                    getTripSummaryRes = new Gson().fromJson(json, TripDetailsResponse.class);


                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getTripSummaryRes = new Gson().fromJson(json, TripDetailsResponse.class);

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
            progressDialog.dismiss();
            if (resp == null) {
                if (getTripSummaryRes != null) {
                    updateUI();

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

        private void updateUI() {
            if (getTripSummaryRes.data.order.pickup_contact_more_info == null || getTripSummaryRes.data.order.pickup_contact_more_info.trim().equals("")){
                String pickString = getTripSummaryRes.data.order.pickup_contact_name + ", " + getTripSummaryRes.data.order.pickup_contact_number;
                pickupDetails.setText(pickString);}
            else{
                String pickString =getTripSummaryRes.data.order.pickup_contact_name + ", " + getTripSummaryRes.data.order.pickup_contact_number + ", " + getTripSummaryRes.data.order.pickup_contact_more_info;
                        pickupDetails.setText(pickString);}

            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            mFirebaseDatabase1 = mFirebaseInstance.getReference("order_drop_contacts").child(getTripSummaryRes.data.order.order_id);

            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<DropContact> dropcontacts = new ArrayList<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        Log.e("tag is", item.getKey());
                        DropContact user = item.getValue(DropContact.class);
                        assert user != null;
                        user.id = item.getKey();
                        dropcontacts.add(user);
                        Log.e("ID is", user.id + " " + user.drop_contact_name);
                    }
                    if (dropcontacts.size() == 0) {
                        DropContact contact = new DropContact();
                        contact.drop_contact_name = getTripSummaryRes.data.order.drop_contact_name;
                        contact.drop_contact_number = getTripSummaryRes.data.order.drop_contact_number;
                        dropcontacts.add(contact);
                    }
                    DropContactdetailsAdapter adapter = new DropContactdetailsAdapter(TripDetailsActivity.this, dropcontacts);
                    droplist.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mFirebaseDatabase1.addValueEventListener(listener);
            String fareText="₹ " + getTripSummaryRes.data.order.order_fare;
            fare.setText(fareText);
            String expString="(" + "Total Expenses :" + getTripSummaryRes.data.order.extra_expenses_amount
                    + "," + "Whizyy Charges :" + getTripSummaryRes.data.order.delivery_charge + ")";
            expens_amt_whizyycharge.setText(expString);
            expens_amt_whizyycharge.setVisibility(View.GONE);
            bike_n.setText(getTripSummaryRes.data.order.vehicle_number);
            pay_mode.setText(getTripSummaryRes.data.order.payment_mode);

            pickupAddress.setText(getTripSummaryRes.data.order.pickup_full_address);


            //status.setText(data.order_status);
            dropAddress.setText(getTripSummaryRes.data.order.drop_full_address);
            distanceText.setText(getTripSummaryRes.data.order.approximate_distance);
            timeText.setText(getTripSummaryRes.data.order.created_time);
            String requestString=" -- REQUEST FULFILLED BY " + getTripSummaryRes.data.order.driver_name + " --";
            request_fulfil.setText(requestString);
            item_list.setText(getTripSummaryRes.data.order.transport_description);
            if (getTripSummaryRes.data.order.ratings_by_customer != null)
                if (getTripSummaryRes.data.order.ratings_by_customer.equalsIgnoreCase("1_star")) {
                    emp_one.setImageResource(R.drawable.fill_stars);
                } else if (getTripSummaryRes.data.order.ratings_by_customer.equalsIgnoreCase("2_star")) {
                    emp_one.setImageResource(R.drawable.fill_stars);
                    emp_two.setImageResource(R.drawable.fill_stars);

                } else if (getTripSummaryRes.data.order.ratings_by_customer.equalsIgnoreCase("3_star")) {
                    emp_one.setImageResource(R.drawable.fill_stars);
                    emp_two.setImageResource(R.drawable.fill_stars);
                    emp_three.setImageResource(R.drawable.fill_stars);

                } else if (getTripSummaryRes.data.order.ratings_by_customer.equalsIgnoreCase("4_star")) {
                    emp_one.setImageResource(R.drawable.fill_stars);
                    emp_two.setImageResource(R.drawable.fill_stars);
                    emp_three.setImageResource(R.drawable.fill_stars);
                    emp_four.setImageResource(R.drawable.fill_stars);

                } else if (getTripSummaryRes.data.order.ratings_by_customer.equalsIgnoreCase("5_star")) {
                    emp_one.setImageResource(R.drawable.fill_stars);
                    emp_two.setImageResource(R.drawable.fill_stars);
                    emp_three.setImageResource(R.drawable.fill_stars);
                    emp_four.setImageResource(R.drawable.fill_stars);
                    emp_five.setImageResource(R.drawable.fill_stars);

                }

        }
    }


    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);


        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            new GetTripSummaryTask(TripDetailsActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null)
            mFirebaseDatabase1.removeEventListener(listener);
    }
}
