package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.SavedAddressesAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.SavedAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 2/20/2019.
 */
public class SavedAddressesActivity extends Activity {
    private ValueEventListener listner;
    private DatabaseReference mFirebaseDatabase;
    private String userID;
    public static List<SavedAddress> savedAddresses;
    private Dialog dialog;
    private LinearLayout loadingLay;
    private DatabaseReference connectedRef;
    private ValueEventListener networkListner;
    private Dialog dialog1;
    private boolean screenLaunch = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("My_Saved_Addresses");
        loadingLay = findViewById(R.id.loading);

        ListView listView = findViewById(R.id.list);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.add).setOnClickListener(v -> {
            Intent intent = new Intent(SavedAddressesActivity.this, MapDragAddressActivity.class);
            startActivity(intent);
        });
        if (getIntent().getBooleanExtra(Constants.IntentKey.FROM_SEARCH, false)) {
            ImageView add = findViewById(R.id.add);
            add.setVisibility(View.GONE);
        }

        firebaseConnection();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SavedAddress address = (SavedAddress) parent.getItemAtPosition(position);
            if (getIntent().getBooleanExtra(Constants.IntentKey.FROM_SEARCH, false)) {
                LocationInf loca = new LocationInf();

                loca.setLatitude(address.Latitude);
                loca.setLongitude(address.Longitude);
                loca.setLocationAddress(address.Address);
                loca.contactName = address.ContactName;
                loca.contactNumber = address.ContactNumber;
                loca.landmark = address.Landmark;

                Intent intent = getIntent();
                intent.putExtra(Constants.IntentKey.KEY_LOCATION, loca);
                intent.putExtra(Constants.IntentKey.FROM_SAVED_ADDRESSES, true);
                setResult(RESULT_OK, intent);
                finish();
            } else {

                Intent intent = new Intent(SavedAddressesActivity.this, EditAddressActivity.class);
                intent.putExtra(Constants.IntentKey.KEY_ADDRESS, address);
                startActivity(intent);
            }
        });
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        userID = sp.getString(Constants.PreferenceKey.KEY_USER_ID, "");

        listner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadingLay.setVisibility(View.GONE);
                if (dataSnapshot.hasChildren()) {
                    savedAddresses = new ArrayList<>();
                    int priority = 2;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SavedAddress address = postSnapshot.getValue(SavedAddress.class);
                        address.id = postSnapshot.getKey();
                        switch (address.Label) {
                            case "Home":
                                address.priority = 0;
                                break;
                            case "Work":
                                address.priority = 1;
                                break;
                            default:
                                address.priority = priority++;
                                break;
                        }
                        savedAddresses.add(address);
                        Log.e("Get Data", address.Label);
                    }
                    Collections.sort(savedAddresses);
                    listView.setAdapter(null);
                    SavedAddressesAdapter adapter = new SavedAddressesAdapter(SavedAddressesActivity.this, savedAddresses);
                    listView.setAdapter(adapter);
                    screenLaunch = false;
                } else {
                    listView.setAdapter(null);
                    if (screenLaunch) {
                        screenLaunch = false;
                        searchNoDataDialog();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (!userID.equals("")) {
            loadingLay.setVisibility(View.VISIBLE);
            mFirebaseDatabase.child(userID).addValueEventListener(listner);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listner != null)
            mFirebaseDatabase.child(userID).removeEventListener(listner);
        if (networkListner != null)
            connectedRef.removeEventListener(networkListner);
    }

    private void firebaseConnection() {
        dialog1 = new Dialog(SavedAddressesActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.alert_network_spinner);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);
//        dialog1.setCancelable(false);
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
                    if (!SavedAddressesActivity.this.isFinishing()) {
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
        connectedRef.addValueEventListener(networkListner);
    }

    public void searchNoDataDialog() {
        if (dialog != null && dialog.isShowing() && !SavedAddressesActivity.this.isFinishing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(SavedAddressesActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(getApplicationContext().getResources().getString(R.string.no_addresses));
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                if (getIntent().getBooleanExtra(Constants.IntentKey.FROM_SEARCH, false)) {
                    dialog.dismiss();
                    finish();
                } else dialog.dismiss();
            });
            if (!SavedAddressesActivity.this.isFinishing())
                dialog.show();
        }
    }
}
