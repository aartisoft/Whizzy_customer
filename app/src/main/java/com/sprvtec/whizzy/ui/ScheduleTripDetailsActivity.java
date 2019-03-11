package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.CancelOptionsAdapter;
import com.sprvtec.whizzy.adapter.DropContactdetailsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CancelOption;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.DropContact;
import com.sprvtec.whizzy.vo.TripDetailsResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by Sowjanya on 9/26/2017.
 */

public class ScheduleTripDetailsActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {

    private TextView pickupAddress, dropAddress, status,
            item_list, pickupDetails, dateTime;


    LinearLayout item_delive;
    String orderid;
    ImageView down;
    private DatabaseReference mFirebaseDatabase1;
    private ValueEventListener listener;
    private TripDetailsResponse getTripSummaryRes;
    private DatabaseReference mFirbaseCancelOptions;
    private FirebaseDatabase mFirebaseInstance;

    private ListView droplist;

    private int startTime, endTime, currentHourIn24Format, currentMin;
    private int hour, minute;
    private Calendar myCalendar;
    private boolean nextDay;
    private Dialog dialog;
//    private static final int READ_PHONE_STATE_REQUEST = 101;
//    private String TAG = "scheduletrip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_trip_details);

        down = findViewById(R.id.abcs);

        status = findViewById(R.id.status);
        pickupDetails = findViewById(R.id.pick_up_details);
        droplist = findViewById(R.id.drop_contacts_list);
        dateTime = findViewById(R.id.date_time);

//        fare = findViewById(R.id.price);
//        v_details = findViewById(R.id.view_details);
        item_list = findViewById(R.id.itemlsit);
//        expens_amt_whizyycharge = findViewById(R.id.fare_sexp_whizzycharge);
        pickupAddress = findViewById(R.id.pic_up_address);
        dropAddress = findViewById(R.id.drop_off_address);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirbaseCancelOptions = mFirebaseInstance.getReference("schedule_trip_cancellation_reasons_customer");
        item_delive = findViewById(R.id.item_del);
        if (!Utils.isNetConnected(ScheduleTripDetailsActivity.this))
            Utils.showDialog(ScheduleTripDetailsActivity.this);
        findViewById(R.id.cancel_req).setOnClickListener(view -> {
//                if (started) {
//                    youCannotCancelTripPopup();
//                } else {
            if (!Utils.isNetConnected(ScheduleTripDetailsActivity.this))
                Utils.showDialog(ScheduleTripDetailsActivity.this);
            else
                getCancelOptionsFireBase();
//                }
        });

        item_delive.setOnClickListener(view -> {
            if (item_list.getVisibility() == View.VISIBLE) {
                item_list.setVisibility(View.GONE);
                down.setImageResource(R.drawable.down_icon);
            } else {
                item_list.setVisibility(View.VISIBLE);
                down.setImageResource(R.drawable.up_icon);


            }
        });
        findViewById(R.id.edit).setOnClickListener(view -> {
            if (!Utils.isNetConnected(ScheduleTripDetailsActivity.this))
                Utils.showDialog(ScheduleTripDetailsActivity.this);
            else
                callScheduleFun();
        });


        orderid = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        System.out.println("orderidddd" + orderid);


        (findViewById(R.id.back)).setOnClickListener(v -> finish());


        new GetTripSummaryTask(ScheduleTripDetailsActivity.this).execute();
    }

    private void callScheduleFun() {
        if (startTime == 0) {
            DatabaseReference mFirebaseScheduleTimingsDb = mFirebaseInstance.getReference("schedule_timings");

            mFirebaseScheduleTimingsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        startTime = ((Number) (dataSnapshot.child("start_time").getValue())).intValue();
                        endTime = ((Number) (dataSnapshot.child("end_time").getValue())).intValue();


                        showDatepicker();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else
            showDatepicker();
    }

    private void showDatepicker() {
        Calendar rightNow = Calendar.getInstance();
        currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        currentMin = rightNow.get(Calendar.MINUTE);
        Log.e("Times", startTime + " " + endTime + " " + currentHourIn24Format + " " + currentMin);
//        EditText hours = findViewById(R.id.hours);
//        EditText min = findViewById(R.id.min);
//        currentHourIn24Format = Integer.parseInt(hours.getText().toString());
//        currentMin = Integer.parseInt(min.getText().toString());
        nextDay = false;


        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            Log.e("date", monthOfYear + " " + dayOfMonth + " " + year);
            if (!(rightNow.get(Calendar.MONTH) == monthOfYear && rightNow.get(Calendar.DAY_OF_MONTH) == dayOfMonth && rightNow.get(Calendar.YEAR) == year))
                nextDay = true;
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                updateLabel();
            callTimePicker();
        };
        if (getTripSummaryRes != null && !getTripSummaryRes.data.order.scheduled_date_time.equals("")) {

            try {
//                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//                Date date1 = df.parse(getTripSummaryRes.data.order.scheduled_date_time);
//                Date c = Calendar.getInstance().getTime();
//                String formattedDate = df.format(c);
//                Date date2 = df.parse(formattedDate);
//                Log.e("compare", compareTwoDates(date1, date2) + "");
//                if (compareTwoDates(date1, date2) > 0) {
                    String dateString = getTripSummaryRes.data.order.scheduled_date_time.split(" ")[0];
                    String year = dateString.split("/")[2];
                    String month = dateString.split("/")[1];
                    String dateDay = dateString.split("/")[0];
                    Log.e("dates", year + "    " + month + "   " + dateDay);
                    myCalendar.set(Calendar.YEAR, Integer.parseInt(year));
                    myCalendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
                    myCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateDay));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatePickerDialog dateDalog = new DatePickerDialog(ScheduleTripDetailsActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        if (currentHourIn24Format < (endTime - 1)) {
            dateDalog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else if (currentHourIn24Format == (endTime - 1) && currentMin <= 30) {
            dateDalog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            nextDay = true;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            dateDalog.getDatePicker().setMinDate(cal.getTimeInMillis());
        }
        dateDalog.show();
    }

//    public static int compareTwoDates(Date date1, Date date2) {
//        if (date1 != null && date2 != null) {
//            int retVal = date1.compareTo(date2);
//
//            if (retVal > 0)
//                return 1; // date1 is greatet than date2
//            else if (retVal == 0) // both dates r equal
//                return 0;
//
//        }
//        return -1; // date1 is less than date2
//    }

    private void callTimePicker() {
        if (nextDay) {

            hour = startTime;
            minute = 0;
        } else {
            if (currentHourIn24Format < startTime) {
                if (currentHourIn24Format == (startTime - 1) && currentMin >= 30) {
                    hour = startTime;
                    minute = currentMin + 30 - 60;
                } else {
                    hour = startTime;
                    minute = 0;
                }
            } else {
                if (currentMin >= 30) {
                    hour = currentHourIn24Format + 1;
                    minute = currentMin + 30 - 60;
                } else {
                    hour = currentHourIn24Format;
                    minute = currentMin + 30;
                }
            }
//            timePickerDialog.setMin(currentHourIn24Format, currentMin);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(ScheduleTripDetailsActivity.this, this, hour, minute, false);
        timePickerDialog.show();

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

    public void showActionSheet(List<CancelOption> options) {

        final Dialog myDialog = new Dialog(ScheduleTripDetailsActivity.this, R.style.CustomTheme);

        myDialog.setContentView(R.layout.actionsheet);

        TextView cancel = myDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(v -> myDialog.dismiss());

        ListView listView = myDialog.findViewById(R.id.list);
        CancelOptionsAdapter adapter = new CancelOptionsAdapter(ScheduleTripDetailsActivity.this, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CancelOption option = (CancelOption) parent.getItemAtPosition(position);
            CancelTripTask task = new CancelTripTask(ScheduleTripDetailsActivity.this, orderid, option.option);
            StartAsyncTaskInParallel(task);
        });

        myDialog.getWindow().getAttributes().windowAnimations = R.anim.slide_up_anim;

        myDialog.show();

        myDialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    public void showDialogConfirm(final String dateTimeText, boolean fromCancel, CancelOption option) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(ScheduleTripDetailsActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_delete_all);
            dialog.setCanceledOnTouchOutside(true);
            TextView title = dialog.findViewById(R.id.title);
            if (fromCancel)
                title.setText(getApplicationContext().getResources().getString(R.string.do_you_really_want_cancel));
            else
                title.setText(getApplicationContext().getResources().getString(R.string.do_u_make_changes));
            Button ok = dialog.findViewById(R.id.yes);
            Button no = dialog.findViewById(R.id.no);
            no.setText(getApplicationContext().getResources().getString(R.string.no));
            no.setOnClickListener(v -> dialog.dismiss());
//                ok.setText("OK");

            ok.setOnClickListener(v -> {
                dialog.dismiss();
                if (fromCancel) {
                    CancelTripTask task = new CancelTripTask(ScheduleTripDetailsActivity.this, orderid, option.option);
                    StartAsyncTaskInParallel(task);
                } else

                    new ReScheduleTripTask(ScheduleTripDetailsActivity.this, orderid, dateTimeText).execute();
                //Add Fucnionality agains


            });

            dialog.show();
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
        java.text.DecimalFormat nft = new
                java.text.DecimalFormat("#00.###");
        nft.setDecimalSeparatorAlwaysShown(false);
        if (hourOfDay >= hour && hourOfDay <= endTime) {
            if (hourOfDay == hour) {
                if (minutes >= minute) {
                    callTimeProceed(hourOfDay, minutes);
                    //call
                } else {
//                    String h=hour>9?hour+"":"0"+hour
                    String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + nft.format((endTime - 12)) + ":00 pm" + "</b>";
                    successDialog(str, false);
                }
            } else if (hourOfDay == endTime && minutes > 0) {
                String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + nft.format((endTime - 12)) + ":00 pm" + "</b>";
                successDialog(str, false);
            } else callTimeProceed(hourOfDay, minutes);
        } else {
            String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + nft.format((endTime - 12)) + ":00 pm" + "</b>";
            successDialog(str, false);

        }
    }

    private void callTimeProceed(int hourOfDay, int minutes) {
        java.text.DecimalFormat nft = new
                java.text.DecimalFormat("#00.###");
        nft.setDecimalSeparatorAlwaysShown(false);
        hour = hourOfDay;
        minute = minutes;
        Log.e("time", hourOfDay + " hrs, minutes " + minute);
//        String month = (myCalendar.get(Calendar.MONTH) + 1) > 9 ? (myCalendar.get(Calendar.MONTH) + 1) + "" : "0" + (myCalendar.get(Calendar.MONTH) + 1);
//        String date = myCalendar.get(Calendar.DAY_OF_MONTH) > 9 ? myCalendar.get(Calendar.DAY_OF_MONTH) + "" : "0" + myCalendar.get(Calendar.DAY_OF_MONTH);
//        String dateTime = nft.format((myCalendar.get(Calendar.MONTH) + 1)) + "/" + nft.format(myCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + myCalendar.get(Calendar.YEAR) + " " + (hour >= 13 ? nft.format((hour - 12)) : nft.format(hour)) + ":" + nft.format(minute) + ":00" + (hour >= 12 ? " pm" : " am");
        String dateTime = nft.format(myCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + nft.format((myCalendar.get(Calendar.MONTH) + 1)) + "/" + myCalendar.get(Calendar.YEAR) + " " + (hour >= 13 ? nft.format((hour - 12)) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " PM" : " AM");
        showDialogConfirm(dateTime, false, null);
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
            if (!ScheduleTripDetailsActivity.this.isFinishing())
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
            if (progressDialog != null && progressDialog.isShowing() && !ScheduleTripDetailsActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null && createOrderResponse != null) {
                successDialog("Order cancelled", true);
            } else if (!Utils.isNetConnected(context))
                Utils.showDialogOpps(context, "No internet connection");
            else {

                Utils.showDialogOpps(context, resp);
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class GetTripSummaryTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
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
            if (!ScheduleTripDetailsActivity.this.isFinishing())
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
            if (progressDialog != null && progressDialog.isShowing() && !ScheduleTripDetailsActivity.this.isFinishing())
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
            if (getTripSummaryRes.data.order.pickup_contact_more_info == null || getTripSummaryRes.data.order.pickup_contact_more_info.trim().equals("")) {
                String pickString = getTripSummaryRes.data.order.pickup_contact_name + ", " + getTripSummaryRes.data.order.pickup_contact_number;
                pickupDetails.setText(pickString);
            } else {
                String pickString = getTripSummaryRes.data.order.pickup_contact_name + ", " + getTripSummaryRes.data.order.pickup_contact_number + ", " + getTripSummaryRes.data.order.pickup_contact_more_info;
                pickupDetails.setText(pickString);
            }

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
                    DropContactdetailsAdapter adapter = new DropContactdetailsAdapter(ScheduleTripDetailsActivity.this, dropcontacts);
                    droplist.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            dateTime.setText(getTripSummaryRes.data.order.scheduled_date_time);
            mFirebaseDatabase1.addValueEventListener(listener);
//            fare.setText("₹ " + getTripSummaryRes.data.order.order_fare);
//            expens_amt_whizyycharge.setText("(" + "Total Expenses :" + getTripSummaryRes.data.order.extra_expenses_amount
//                    + "," + "Whizyy Charges :" + getTripSummaryRes.data.order.delivery_charge + ")");
//            expens_amt_whizyycharge.setVisibility(View.GONE);

            pickupAddress.setText(getTripSummaryRes.data.order.pickup_full_address);


            //status.setText(data.order_status);
            status.setText(getTripSummaryRes.data.order.order_status);
            dropAddress.setText(getTripSummaryRes.data.order.drop_full_address);
            item_list.setText(getTripSummaryRes.data.order.transport_description);


        }
    }

    @SuppressLint("StaticFieldLeak")
    class ReScheduleTripTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
        private String orderID, dateTimeText;

        ReScheduleTripTask(Context context, String orderID, String dateTimeText) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.dateTimeText = dateTimeText;
            this.orderID = orderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
//            progressDialog.setCanceledOnTouchOutside(false);
            if (!ScheduleTripDetailsActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_ID, orderID));
            nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_DATE_TIME, dateTimeText));


            String json = Webservice.callPostService1(Webservice.EDIT_SCHEDULED_ORDER, nameValuePairs);
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
            if (progressDialog != null && progressDialog.isShowing() && !ScheduleTripDetailsActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
//                Utils.showDialogFailure(ScheduleTripDetailsActivity.this, createOrderResponse.data.message);
                successDialog(createOrderResponse.data.message, true);
                dateTime.setText(dateTimeText);
//                Intent msgrcv = new Intent(Constants.BROADCAST_REFRESH);
//                LocalBroadcastManager.getInstance(ScheduleTripDetailsActivity.this).sendBroadcast(msgrcv);
            } else if (!Utils.isNetConnected(context))
                Utils.showDialogOpps(context, "No internet connection");
            else {

                Utils.showDialogOpps(context, resp);
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
            new GetTripSummaryTask(ScheduleTripDetailsActivity.this).execute();
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

    private void StartAsyncTaskInParallel(
            CancelTripTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public void successDialog(String message, boolean val) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(ScheduleTripDetailsActivity.this);

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
                if (val) {
                    Intent msgrcv = new Intent(Constants.BROADCAST_REFRESH);
                    LocalBroadcastManager.getInstance(ScheduleTripDetailsActivity.this).sendBroadcast(msgrcv);
                    finish();
                }

            });

            dialog.show();
        }
    }
}
