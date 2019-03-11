package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.ScheduledTripsAdapter;
import com.sprvtec.whizzy.adapter.TripsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.GetNearByVehiclesResponse;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by Sowjanya on 8/9/2017.
 */

public class AllTripsActivity extends Activity {
    private SharedPreferences sp;
    private TextView text, myRequests, scheduledRequests;
    private ListView list;
    private GetAllTripsTask asyncTask;
    private GetScheduledTripsTask asyncTaskSchedule;
    Dialog dialog1;
    private LinearLayout loadingLay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_trips);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        list = findViewById(R.id.list);
        text = findViewById(R.id.text);
        loadingLay = findViewById(R.id.loading);
        myRequests = findViewById(R.id.my_requests);
        scheduledRequests = findViewById(R.id.scheduled_requests);
        myRequests.setOnClickListener(view -> callMyRequests());
        scheduledRequests.setOnClickListener(view -> callScheduledRequests());
        ImageView back = findViewById(R.id.back);

        back.setOnClickListener(v -> finish());
        callMyRequests();
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshPage, new IntentFilter(Constants.BROADCAST_REFRESH));

    }

    private BroadcastReceiver refreshPage = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            callScheduledRequests();
        }
    };

    private void callMyRequests() {
        myRequests.setBackgroundResource(R.drawable.button_blue_sq);
        scheduledRequests.setBackgroundResource(R.drawable.button_grayout_sq);
        asyncTask = new GetAllTripsTask(this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));
        asyncTask.execute();
    }

    private void callScheduledRequests() {
        scheduledRequests.setBackgroundResource(R.drawable.button_blue_sq);
        myRequests.setBackgroundResource(R.drawable.button_grayout_sq);
        asyncTaskSchedule = new GetScheduledTripsTask(this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));
        asyncTaskSchedule.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAllTripsTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetNearByVehiclesResponse getOrdersResponse;
        private String consumerID;
//        private ProgressDialog progressDialog;

        GetAllTripsTask(Context context, String consumerID) {
            this.context = context;
            this.consumerID = consumerID;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String json = Webservice.callGetService(Webservice.GET_MY_ORDERS + "?" + Webservice.USER_ID + "=" + consumerID);
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

            if (status.equalsIgnoreCase("Success")) {
                try {
                    getOrdersResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
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
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                if (getOrdersResponse != null) {
                    text.setVisibility(View.GONE);
                    int currentPosition = list.getFirstVisiblePosition();
                    TripsAdapter tripsAdapter = new TripsAdapter(context, getOrdersResponse.data.Orders);
                    list.setAdapter(tripsAdapter);
                    list.setSelectionFromTop(currentPosition, 0);

                }

            } else if (!Utils.isNetConnected(context))
                showDialog1(context);
            else {
                list.setAdapter(null);
                text.setVisibility(View.VISIBLE);
                text.setText("Request History Not Found");
            }
        }
    }

    void showDialog1(final Context context) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle("No internet connection");
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage("Please check your wifi/mobile data signal");
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            asyncTask = new GetAllTripsTask(AllTripsActivity.this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));
            asyncTask.execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private class GetScheduledTripsTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetNearByVehiclesResponse getOrdersResponse;
        private String consumerID;
//        private ProgressDialog progressDialog;

        GetScheduledTripsTask(Context context, String consumerID) {
            this.context = context;
            this.consumerID = consumerID;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String json = Webservice.callGetService(Webservice.GET_MY_SCHEDULED_ORDERS + "?" + Webservice.USER_ID + "=" + consumerID);
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

            if (status.equalsIgnoreCase("Success")) {
                try {
                    getOrdersResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
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
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                if (getOrdersResponse != null) {
                    text.setVisibility(View.GONE);
                    int currentPosition = list.getFirstVisiblePosition();
                    ScheduledTripsAdapter tripsAdapter = new ScheduledTripsAdapter(context, getOrdersResponse.data.Orders);
                    list.setAdapter(tripsAdapter);
                    list.setSelectionFromTop(currentPosition, 0);

                }

            } else if (!Utils.isNetConnected(context))
                showDialog2(context);
            else {
                list.setAdapter(null);
                text.setVisibility(View.VISIBLE);
                text.setText("Request History Not Found");
            }
        }
    }

    void showDialog2(final Context context) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle("No internet connection");
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage("Please check your wifi/mobile data signal");
//        alertDialog.setMessage("Please check your wifi/mobile data to access the application");

        // Setting alert dialog icon
//            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

//                System.exit(0);
//                ((Activity) context).finish();
            alertDialog.dismiss();
            asyncTaskSchedule = new GetScheduledTripsTask(AllTripsActivity.this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));
            asyncTaskSchedule.execute();
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
