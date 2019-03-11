package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.SavedAddress;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 5/14/2018.
 */

public class OptionsActivity extends Activity implements View.OnClickListener {

    private RelativeLayout buyForMe, pickupDrop, otherServices;
    private LinearLayout medicine, streetFood, flowers, lunchBox, laptop, dress, depositCheque, payBill, getMeCash;
    private TextView manyMore1, manyMore2, manyMore3;
    //    private PreferenceUtils preferenceUtils;
    public static String BringItem = "";
    Dialog dialog1;
    private SharedPreferences sp;
    public static ArrayList<SavedAddress> savedAddresses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        buyForMe = findViewById(R.id.buy_for_me);
        buyForMe.setOnClickListener(this);
        pickupDrop = findViewById(R.id.pickup_drop);
        pickupDrop.setOnClickListener(this);
        otherServices = findViewById(R.id.other_services);
        otherServices.setOnClickListener(this);
        manyMore1 = findViewById(R.id.many_more1);
        manyMore1.setOnClickListener(this);
        manyMore2 = findViewById(R.id.many_more2);
        manyMore2.setOnClickListener(this);
        manyMore3 = findViewById(R.id.many_more3);
        manyMore3.setOnClickListener(this);
        medicine = findViewById(R.id.medicine);
        medicine.setOnClickListener(this);
        streetFood = findViewById(R.id.street_food);
        streetFood.setOnClickListener(this);
        flowers = findViewById(R.id.flowers);
        flowers.setOnClickListener(this);
        lunchBox = findViewById(R.id.lunch_box);
        lunchBox.setOnClickListener(this);
        laptop = findViewById(R.id.laptop);
        laptop.setOnClickListener(this);
        dress = findViewById(R.id.dress);
        dress.setOnClickListener(this);
        depositCheque = findViewById(R.id.bank_cheque);
        depositCheque.setOnClickListener(this);
        payBill = findViewById(R.id.pay_bill);
        payBill.setOnClickListener(this);
        getMeCash = findViewById(R.id.get_me_cash);
        getMeCash.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        DatabaseReference mFirebaseDatabaseAddress = mFirebaseInstance.getReference("My_Saved_Addresses");
        String userID = sp.getString(Constants.PreferenceKey.KEY_USER_ID, "");
        if (!userID.equals(""))
            mFirebaseDatabaseAddress.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    savedAddresses = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SavedAddress address = postSnapshot.getValue(SavedAddress.class);
                        address.id = postSnapshot.getKey();
                        savedAddresses.add(address);
                        Log.e("Get Data", address.Label);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //avoid multiple tapping
        buyForMe.setEnabled(true);
        medicine.setEnabled(true);
        streetFood.setEnabled(true);
        flowers.setEnabled(true);
        lunchBox.setEnabled(true);
        laptop.setEnabled(true);
        dress.setEnabled(true);
        pickupDrop.setEnabled(true);
        depositCheque.setEnabled(true);
        payBill.setEnabled(true);
        getMeCash.setEnabled(true);
        manyMore1.setEnabled(true);
        manyMore2.setEnabled(true);
        manyMore3.setEnabled(true);
        otherServices.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy_for_me:

                buyForMe.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Buy").apply();
                BringItem = "";
                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACEID, "").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                Intent intent = new Intent(OptionsActivity.this, CategoryGridActivity.class);
                intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent);
                break;
            case R.id.medicine:
                medicine.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Buy").apply();
                BringItem = "";

                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACEID, "").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                Intent intent1 = new Intent(OptionsActivity.this, CategoryGridActivity.class);
                intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent1);
                break;
            case R.id.street_food:
                streetFood.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Buy").apply();
                BringItem = "";

                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACEID, "").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                Intent intent2 = new Intent(OptionsActivity.this, CategoryGridActivity.class);
                intent2.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent2.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent2);
                break;
            case R.id.flowers:
                flowers.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Buy").apply();
                BringItem = "";

                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACEID, "").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                Intent intent3 = new Intent(OptionsActivity.this, CategoryGridActivity.class);
                intent3.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent3.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent3);
                break;
            case R.id.lunch_box:
                lunchBox.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Bring my Lunch box";
                Intent intent4 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent4.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent4.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent4);
                break;
            case R.id.laptop:
                laptop.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Bring my Laptop";
                Intent intent5 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent5.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent5.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent5);
                break;
            case R.id.dress:
                dress.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Bring my Dress";
                Intent intent6 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent6.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent6.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent6);
                break;
            case R.id.pickup_drop:
                pickupDrop.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "";
                Intent intent7 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent7.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent7.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent7);
                break;
            case R.id.bank_cheque:
                depositCheque.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Deposit Cheque";
                Intent intent11 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent11.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent11.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent11);
                break;
            case R.id.pay_bill:
                payBill.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Pay bill";
                Intent intent12 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent12.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent12.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent12);
                break;
            case R.id.get_me_cash:
                getMeCash.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "Get me cash";
                Intent intent13 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent13.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent13.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent13);
                break;
            case R.id.many_more1:
                manyMore1.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Buy").apply();
                BringItem = "";

                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACEID, "").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                Intent intent9 = new Intent(OptionsActivity.this, CategoryGridActivity.class);
                intent9.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent9.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent9);
                break;
            case R.id.many_more2:
                manyMore2.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "";
                Intent intent8 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent8.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent8.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent8);
                break;
            case R.id.many_more3:
                manyMore3.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "";
                Intent intent10 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent10.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent10.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent10);
                break;
            case R.id.other_services:
                otherServices.setEnabled(false);
                sp.edit().putString(Constants.PreferenceKey.BUYANDPICKUP, "Pickup").apply();
                BringItem = "";
                Intent intent14 = new Intent(OptionsActivity.this, MapDragPickupActivity.class);
                intent14.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent14.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent14);
                break;

        }
    }
}
