package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.vo.AddAddress;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.PhoneContact;
import com.sprvtec.whizzy.vo.SavedAddress;

import androidx.annotation.Nullable;

import static com.sprvtec.whizzy.ui.SavedAddressesActivity.savedAddresses;

/**
 * Created by Sowjanya on 2/22/2019.
 */
public class EditAddressActivity extends Activity implements TextWatcher {
    private DatabaseReference mFirebaseDatabaseAddress;
    private String userID;
    private TextView addressText;
    private EditText landmarkText, contactNameText, contactNumberText, labelText;
    private Dialog dialog;
    private SavedAddress address;
    private static final int PHONE_BOOK = 22, ADDRESS_EDIT = 11;
    private boolean edited = false;
    private Dialog dialog1;
    private DatabaseReference connectedRef;
    private ValueEventListener networkListner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title);
        addressText = findViewById(R.id.address);
        landmarkText = findViewById(R.id.landmark);
        labelText = findViewById(R.id.label);
        contactNameText = findViewById(R.id.contact_name);
        contactNumberText = findViewById(R.id.contact_number);
        address = getIntent().getParcelableExtra(Constants.IntentKey.KEY_ADDRESS);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabaseAddress = mFirebaseInstance.getReference("My_Saved_Addresses");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        userID = sp.getString(Constants.PreferenceKey.KEY_USER_ID, "");
        addressText.setText(address.Address);
        labelText.setText(address.Label);
        if (address.Label.equals("Home") || address.Label.equals("Work"))
            labelText.setEnabled(false);
        contactNameText.setText(address.ContactName);
        contactNumberText.setText(address.ContactNumber);
        landmarkText.setText(address.Landmark);
        title.setText(address.Label);
        edited = false;

        labelText.addTextChangedListener(this);
        contactNameText.addTextChangedListener(this);
        contactNumberText.addTextChangedListener(this);
        landmarkText.addTextChangedListener(this);

        firebaseConnection();
        addressText.setOnClickListener(v -> {
            edited = true;
            Intent intent = new Intent(EditAddressActivity.this, MapDragAddressActivity.class);
            intent.putExtra(Constants.IntentKey.FROM_EDIT, true);
            LocationInf locationInf = new LocationInf();
            locationInf.locationAddress = address.Address;
            locationInf.latitude = address.Latitude;
            locationInf.longitude = address.Longitude;
            intent.putExtra(Constants.IntentKey.KEY_LOCATION, locationInf);
            intent.putExtra(Constants.IntentKey.KEY_LANDMARK, landmarkText.getText().toString().trim());
            startActivityForResult(intent, ADDRESS_EDIT);

        });
        findViewById(R.id.delete).setOnClickListener(v -> {
            showConfirmationDialog();
        });
        findViewById(R.id.phone_book).setOnClickListener(v -> {
            Intent intent = new Intent(EditAddressActivity.this, PhoneContactsActivity.class);
            startActivityForResult(intent, PHONE_BOOK);
        });
        findViewById(R.id.save).setOnClickListener(v -> {
            if (edited) {
                boolean duplicate = false;
                if (!labelText.getText().toString().trim().equals("")) {
                    for (SavedAddress addr : savedAddresses)
                        if (addr.Label.equalsIgnoreCase(labelText.getText().toString().trim()) && !addr.id.equals(address.id)) {
                            duplicate = true;
                            break;
                        }
                    if (!duplicate) {
                        AddAddress addressNew = new AddAddress();
                        addressNew.Label = labelText.getText().toString().trim();
                        addressNew.Address = addressText.getText().toString().trim();
                        addressNew.Landmark = landmarkText.getText().toString().trim();
                        addressNew.Latitude = address.Latitude;
                        addressNew.Longitude = address.Longitude;
                        addressNew.ContactName = contactNameText.getText().toString().trim();
                        addressNew.ContactNumber = contactNumberText.getText().toString().trim();
                        mFirebaseDatabaseAddress.child(userID).child(address.id).setValue(addressNew, (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                Utils.showDialogFailure(EditAddressActivity.this, "Database error. Please try again");
                            } else {
                                successDialog("Saved Successfully");
                            }
                        });
                    } else
                        Utils.showDialogFailure(EditAddressActivity.this, "An address with Label " + "\"" + labelText.getText().toString().trim() + "\"" + " already exists");
                } else
                    Utils.showDialogFailure(EditAddressActivity.this, "Please add address label");
            } else finish();
        });
    }

    private void firebaseConnection() {
        dialog1 = new Dialog(EditAddressActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.alert_network_spinner);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setCancelable(false);
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
                    if (!EditAddressActivity.this.isFinishing()) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkListner != null)
            connectedRef.removeEventListener(networkListner);
    }

    public void successDialog(String message) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(EditAddressActivity.this);

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
                finish();
            });

            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_EDIT && resultCode == RESULT_OK) {
            if (data != null) {
                LocationInf locationInfo = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
                String addressT = locationInfo.locationName + "\n" + locationInfo.locationAddress;
                addressText.setText(addressT);
                address.Longitude = locationInfo.latitude;
                address.Longitude = locationInfo.longitude;
                address.Address = locationInfo.locationAddress;
                if (!data.getStringExtra(Constants.IntentKey.KEY_LANDMARK).equals(""))
                    landmarkText.setText(data.getStringExtra(Constants.IntentKey.KEY_LANDMARK));
            }
        } else if (requestCode == PHONE_BOOK && resultCode == RESULT_OK) {
            if (data != null) {
                PhoneContact contact = data.getParcelableExtra(Constants.IntentKey.KEY_CONTACT);
                contactNameText.setText(contact.Name);
                contactNumberText.setText(contact.MobileNumber);
            }
        }
    }

    void showConfirmationDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(EditAddressActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_delete_all);
            dialog.setCanceledOnTouchOutside(true);
            TextView title = dialog.findViewById(R.id.title);

            title.setText(getApplicationContext().getResources().getString(R.string.do_you_really_address));
            Button ok = dialog.findViewById(R.id.yes);
            Button no = dialog.findViewById(R.id.no);
            no.setText(getApplicationContext().getResources().getString(R.string.cancel));
            no.setOnClickListener(v -> dialog.dismiss());
//                ok.setText("OK");

            ok.setOnClickListener(v -> {
                dialog.dismiss();
                mFirebaseDatabaseAddress.child(userID).child(address.id).removeValue((databaseError, databaseReference) -> {
                    Toast.makeText(EditAddressActivity.this, "Address Removed", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

            dialog.show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        edited = true;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
