package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.paytm.pgsdk.Log;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplUser;
import com.simpl.android.sdk.SimplUserApprovalListenerV2;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.PaytmRespone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Sowjanya on 10/30/2018.
 */
public class PaymentModeActivity extends Activity {

    private RelativeLayout simplLay;
    private SharedPreferences sp;
    private ImageView check_cash, check_card, check_simpl, check_paytm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_mode);
        simplLay = (findViewById(R.id.simpl));
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        check_card = (findViewById(R.id.check_credit_debit));
        check_simpl = (findViewById(R.id.check_simpl));
        check_paytm = (findViewById(R.id.check_paytm));
        check_cash = (findViewById(R.id.check_cash));

        Simpl.getInstance().runInSandboxMode();
        simplCheck();
//        list = findViewById(R.id.payment_options);
        if (ContextCompat.checkSelfPermission(PaymentModeActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PaymentModeActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
//        mFirebaseInstance = FirebaseDatabase.getInstance();
//        mFirebaseFeedbackOptions = mFirebaseInstance.getReference("PaymentOptions");
//        final List<String> options = new ArrayList<>();
//        mFirebaseFeedbackOptions.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                android.util.Log.e("Count ", "" + snapshot.getChildrenCount());
//
//                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    String post = postSnapshot.getValue(String.class);
//                    options.add(post);
//                    android.util.Log.e("Get Data", post);
//                }
//                PaymentOptionsAdapter adapter = new PaymentOptionsAdapter(PaymentModeActivity.this, options);
//                list.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String item = (String) parent.getItemAtPosition(position);
//                if (item.equalsIgnoreCase("paytm")) {
//                    new GenerateChecksum(PaymentModeActivity.this).execute();
//                }
//            }
//        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_cash.setVisibility(View.GONE);
                check_card.setVisibility(View.VISIBLE);
                check_simpl.setVisibility(View.GONE);
                check_paytm.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.simpl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new GenerateChecksum(PaymentModeActivity.this).execute();
                check_cash.setVisibility(View.GONE);
                check_card.setVisibility(View.GONE);
                check_simpl.setVisibility(View.VISIBLE);
                check_paytm.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.paytm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_cash.setVisibility(View.GONE);
                check_card.setVisibility(View.GONE);
                check_simpl.setVisibility(View.GONE);
                check_paytm.setVisibility(View.VISIBLE);
                new GenerateChecksum(PaymentModeActivity.this).execute();
            }
        });
        findViewById(R.id.cash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new GenerateChecksum(PaymentModeActivity.this).execute();
                check_cash.setVisibility(View.VISIBLE);
                check_card.setVisibility(View.GONE);
                check_simpl.setVisibility(View.GONE);
                check_paytm.setVisibility(View.GONE);
            }
        });
    }

    private void simplCheck() {
//        SimplUser user = new SimplUser("mobeen@sprvtec.com", "7416212576");
        SimplUser user = new SimplUser("mobeen@sprvtec.com", sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        Simpl.getInstance()
                .isUserApproved(user)
                .addParam("transaction_amount_in_paise", (long) (1 * 100) + "") // mandatory
//                .addParam("user_location", "18.9750,72.8258")      // optional
//                .addParam("member_since", "2016-01-08")            // optional
                .execute(new SimplUserApprovalListenerV2() {
                    public void onSuccess(
                            final boolean isApproved,
                            final String buttonText,
                            final boolean showSimplIntroduction
                    ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isApproved) {
                                    simplLay.setVisibility(View.VISIBLE);

                                } else {
                                    simplLay.setVisibility(View.GONE);

                                }
                            }
                        });
                    }

                    public void onError(final Throwable throwable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                android.util.Log.e("errorrr_whizzy", throwable.getMessage());
                                simplLay.setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

    class GenerateChecksum extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        private ProgressDialog progressDialog;
        private SharedPreferences sp;
//        private String orderID, cancelOption;

        GenerateChecksum(Context context) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
//            this.cancelOption = cancelOption;
//            this.orderID = orderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
//            progressDialog.setCanceledOnTouchOutside(false);
            if (!PaymentModeActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_ID, "ABCDEFGHIJKLM1116"));
//            nameValuePairs.add(new BasicNameValuePair(Webservice.CANCEL_OPTION, cancelOption));


            String json = Webservice.callPostService1("GenerateChecksum", nameValuePairs);
//            String json = Webservice.postData(Webservice.REGISTER_USER, nameValuePairs);
            String status = "";
            android.util.Log.e("INTEREST", json + "responseeeeee");
            android.util.Log.i("INTEREST", json + "responseeeeee");


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
            if (progressDialog != null && progressDialog.isShowing() && !PaymentModeActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                callPayment(createOrderResponse.data.Response);
            } else if (!Utils.isNetConnected(context))
                Utils.showDialogOpps(context, "No internet connection");
            else {

                Utils.showDialogOpps(context, resp);
            }
        }

    }

    private void callPayment(PaytmRespone response) {

        PaytmPGService Service = PaytmPGService.getStagingService();
//                paramMap.put( "MID" , "WHIZZY15364213674102");
        Map<String, String> paramMap = new HashMap<String, String>();

        paramMap.put("MID", response.MID);

//                paramMap.put("MID", "rxazcv89315285244163");
// Key in your staging and production MID available in your dashboard
        paramMap.put("ORDER_ID", response.ORDER_ID);
        paramMap.put("CUST_ID", response.CUST_ID);
        paramMap.put("INDUSTRY_TYPE_ID", response.INDUSTRY_TYPE_ID);
        paramMap.put("CHANNEL_ID", response.CHANNEL_ID);
        paramMap.put("TXN_AMOUNT", response.TXN_AMOUNT);
        paramMap.put("WEBSITE", response.WEBSITE);
        paramMap.put("CALLBACK_URL", response.CALLBACK_URL);
        paramMap.put("CHECKSUMHASH", response.CHECKSUMHASH);
        paramMap.put("MOBILE_NO", response.MOBILE_NO);
        paramMap.put("EMAIL", response.EMAIL);


// This is the staging value. Production value is available in your dashboard

// This is the staging value. Production value is available in your dashboard
//        paramMap.put("CALLBACK_URL", "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + response.ORDER_ID);

        PaytmOrder Order = new PaytmOrder((HashMap<String, String>) paramMap);

        Service.initialize(Order, null);
        Service.startPaymentTransaction(PaymentModeActivity.this, true, true, new PaytmPaymentTransactionCallback() {
            /*Call Backs*/
            public void someUIErrorOccurred(String inErrorMessage) {
                Log.e("error", inErrorMessage);
                /*Display the error message as below */
                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            public void onTransactionResponse(Bundle inResponse) {/*Display the message as below */
                Log.e("response", inResponse.toString());
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
            }

            public void networkNotAvailable() {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
            }

            public void clientAuthenticationFailed(String inErrorMessage) {
                Log.e("client error", inErrorMessage);
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onBackPressedCancelTransaction() {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();
            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                Log.e("cancelerror", inErrorMessage);
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Transaction Cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
