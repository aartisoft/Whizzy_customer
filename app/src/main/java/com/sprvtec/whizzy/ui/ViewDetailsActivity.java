package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.BreakFareDownAdapter;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.Farebreakdownresponse;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;


public class ViewDetailsActivity extends AppCompatActivity {
    String orderid;
    ImageView back;


    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_details);


        back = findViewById(R.id.back);
        back.setOnClickListener(view -> finish());

        Bundle b = new Bundle(getIntent().getExtras());
        if (b != null) {
            orderid = b.getString("order_id");
        }

        System.out.println("orderIddddd===" + orderid);
        listView = findViewById(R.id.listView_exp);
//        animalList = new ArrayList<>();

        new GetExpensesTask(ViewDetailsActivity.this, orderid).execute();

    }

    @SuppressLint("StaticFieldLeak")
    private class GetExpensesTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private Farebreakdownresponse getTripSummaryRes;
        private String orderID;
        private ProgressDialog progressDialog;

        GetExpensesTask(Context context, String orderID) {
            this.context = context;
            this.orderID = orderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            if (!ViewDetailsActivity.this.isFinishing())
                progressDialog.show();
//            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_FARE_BREAKFOWN + "?" + Webservice.ORDER_ID + "=" + orderID;
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
                    getTripSummaryRes = new Gson().fromJson(json, Farebreakdownresponse.class);

                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getTripSummaryRes = new Gson().fromJson(json, Farebreakdownresponse.class);

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
            if (progressDialog != null && progressDialog.isShowing() && !ViewDetailsActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (getTripSummaryRes != null) {
                    BreakFareDownAdapter myAdapter = new BreakFareDownAdapter(ViewDetailsActivity.this, R.layout.faredown, getTripSummaryRes.data.FareDetails);
                    listView.setAdapter(myAdapter);
                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                if (getTripSummaryRes != null && !getTripSummaryRes.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!" + "\n" + resp);
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

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            new GetExpensesTask(ViewDetailsActivity.this, orderid).execute();
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


