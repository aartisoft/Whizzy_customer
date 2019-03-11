package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.DropContactdetailsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.DropContact;
import com.sprvtec.whizzy.vo.TripSummary;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 11/2/2018.
 */
public class SummaryTripDetails extends Activity {
    private TextView timeText, pickupAddress, dropAddress, distanceText, item_list, pickupDetails;
    private ListView droplist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_block);
        TripSummary tripDetails = getIntent().getParcelableExtra(Constants.IntentKey.KEY_TRIP);
        timeText = findViewById(R.id.time);
        pickupAddress = findViewById(R.id.pic_up_address);
        dropAddress = findViewById(R.id.drop_off_address);
        distanceText = findViewById(R.id.distance);
        item_list = findViewById(R.id.itemlsit);
        droplist = findViewById(R.id.drop_contacts_list);
        pickupDetails = findViewById(R.id.pick_up_details);

        findViewById(R.id.back).setOnClickListener(v -> finish());
        updateUI(tripDetails);
    }

    private void updateUI(TripSummary data) {

        timeText.setText(data.created_time);
        pickupAddress.setText(data.pickup_full_address);
        dropAddress.setText(data.drop_full_address);
        distanceText.setText(data.distance);
        item_list.setText(data.transport_description);
        if (data.pickup_contact_more_info.trim().equals("")) {
            String pickString = data.pickup_contact_name + ", " + data.pickup_contact_number;
            pickupDetails.setText(pickString);
        } else {
            String pickString = data.pickup_contact_name + ", " + data.pickup_contact_number + ", " + data.pickup_contact_more_info;
            pickupDetails.setText(pickString);
        }
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDatabase1 = mFirebaseInstance.getReference("order_drop_contacts").child(data.order_id);

        ValueEventListener listener = new ValueEventListener() {
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
                DropContactdetailsAdapter adapter = new DropContactdetailsAdapter(SummaryTripDetails.this, dropcontacts);
                droplist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mFirebaseDatabase1.addValueEventListener(listener);

    }
}
