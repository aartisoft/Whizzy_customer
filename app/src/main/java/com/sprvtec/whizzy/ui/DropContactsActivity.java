package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.DropContactsAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.DropContact;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 11/27/2018.
 */
public class DropContactsActivity extends Activity {
    private ListView list;
    private DatabaseReference mFirebaseDatabase1;
    private ValueEventListener listener;
    private LinearLayout loadingLay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_contacts);
        list = findViewById(R.id.listview);
        loadingLay = findViewById(R.id.loading);
        loadingLay.setVisibility(View.VISIBLE);
        String orderID = getIntent().getStringExtra(Constants.IntentKey.KEY_ORDER_ID);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase1 = mFirebaseInstance.getReference("order_drop_contacts").child(orderID);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.back).setOnClickListener(v -> finish());
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
                loadingLay.setVisibility(View.GONE);
                DropContactsAdapter adapter = new DropContactsAdapter(DropContactsActivity.this, dropcontacts);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mFirebaseDatabase1.addValueEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null)
            mFirebaseDatabase1.removeEventListener(listener);
    }
}
