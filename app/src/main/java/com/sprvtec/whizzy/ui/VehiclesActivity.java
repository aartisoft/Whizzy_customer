package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.ApplyCoupnResponse;
import com.sprvtec.whizzy.vo.Business;
import com.sprvtec.whizzy.vo.CustomerRegistrationResponse;
import com.sprvtec.whizzy.vo.DropContact;
import com.sprvtec.whizzy.vo.GetWhizzyVehiclesResponse;
import com.sprvtec.whizzy.vo.LocationInf;
import com.sprvtec.whizzy.vo.PhoneContact;
import com.sprvtec.whizzy.vo.PurchaseProduct;
import com.sprvtec.whizzy.vo.ResgetApplyCoupn;
import com.sprvtec.whizzy.vo.WhizzyVehicle;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentActivity;

/**
 * Created by Sowjanya on 5/25/2017.
 */
public class VehiclesActivity extends FragmentActivity {
    private static final int REQUEST_CODE = 6, PICK_KEY = 11, DROP_KEY = 12;
    private Dialog scheduleDialog;
    private LocationInf pickupInfo, dropOffInfo;
    private int tripType;
    private ImageView info;
    private SharedPreferences sp;
    private TextView dropContactCount;
    private TextView payText;
    private RelativeLayout placeReq;
    private EditText apply_coupn;
    private TextView fare;
    private TextView pickupAddress;
    private TextView dropoffAddress;
    private TextView addDropContact;
    private TextView addPickcontact;
    private List<WhizzyVehicle> whizzyVehicles = new ArrayList<>();
    private WhizzyVehicle whizzyVehicle;
    private String TAG = "Vehicles Activity", coupncode = "", discount_fare = "", discount = "";
    private TextView apply_cpn_btn;
    private TextView dis_code;
    private TextView coupn_code;
    private TextView inv_code;
    private TextView checkNote;
    private int intFare, intFare1;
    private LinearLayout applylayout, deltelayout;
    private List<DropContact> dropcontacts = new ArrayList<>();
    private ListView dropContactsList;
    private String pickupLandmark = "", dropLandmark = "";
    private CheckBox checkMultiple;
    private boolean multiCheck;
    //    PreferenceUtils preferenceUtils;
    private String str_order_type;
    private EditText description;
    private CheckBox termsCheckBox;
    private Utils utils;
    private RelativeLayout pickContactLay;
    private DropContact pickContact;
    private LinearLayout loadingLay;
    private EditText pickName, pickNumber, dropNme, dropNumber;
    private int multiDropFare;
    private int additionalPrice;
    private Business business;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        TextView edit_txt = findViewById(R.id.textViewEdit);
        utils = new Utils();
        loadingLay = findViewById(R.id.loading);
        info = findViewById(R.id.info);

        business = getIntent().getParcelableExtra(Constants.IntentKey.BUSINESS);

        loadingLay.setEnabled(false);
        dropContactCount = findViewById(R.id.count);
        RelativeLayout multichecklay = findViewById(R.id.multicheck_lay);
        checkNote = findViewById(R.id.check_note);
        tripType = getIntent().getIntExtra(Constants.IntentKey.KEY_WAY_TYPE, 0);
        pickupInfo = getIntent().getParcelableExtra(Constants.IntentKey.KEY_PICKUP_LOCATION);
        dropOffInfo = getIntent().getParcelableExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION);
        placeReq = findViewById(R.id.place_req);
        payText = findViewById(R.id.paytext);
        pickContactLay = findViewById(R.id.pic_contact_lay);
        description = findViewById(R.id.description);
        termsCheckBox = findViewById(R.id.terms_check);
        pickupAddress = findViewById(R.id.pic_up_address);
        dropoffAddress = findViewById(R.id.drop_off_address);
        TextView pickupHeading = findViewById(R.id.pickup_heading);

        dropContactsList = findViewById(R.id.drop_contacts_lay);
        pickupLandmark = getIntent().getStringExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK);
        dropLandmark = getIntent().getStringExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK);
        findViewById(R.id.home).setOnClickListener(view -> {
            Intent intentCall = new Intent(VehiclesActivity.this, HomePagerActivity.class);
            intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentCall);
            finish();
        });
        if (business != null && !business.name.equals("")) {
            String addres = pickupInfo.locationName + "\n" + pickupInfo.locationAddress;
            pickupAddress.setText(addres);
            pickupHeading.setText(getApplicationContext().getResources().getString(R.string.buy_items_from));
        } else {
            String addres = pickupInfo.locationName + "\n" + pickupInfo.locationAddress + "\n\n" + "Landmark : " + pickupLandmark;
            pickupAddress.setText(addres);
        }

        String dropAd = dropOffInfo.locationName + "\n" + dropOffInfo.locationAddress + "\n\n" + "Landmark : " + dropLandmark;
        dropoffAddress.setText(dropAd);
        checkMultiple = findViewById(R.id.check_multiple);
        addDropContact = findViewById(R.id.add_dropoff_contact);
        addPickcontact = findViewById(R.id.add_pickup_contact);
        TextView desHeading = findViewById(R.id.textView);
        str_order_type = sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "");
        checkMultiple.setEnabled(false);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'orders' node
        DatabaseReference mFirebaseExtraPersonFareDb = mFirebaseInstance.getReference("price_per_additional_drop_contact");

        mFirebaseExtraPersonFareDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    checkMultiple.setEnabled(true);
                    additionalPrice = ((Number) (dataSnapshot.child("price").getValue())).intValue();
                    String note = "Note: Extra Rs." + additionalPrice + " per each additional drop";
                    checkNote.setText(note);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        info.setOnClickListener(v -> showInfoDialog());
        checkMultiple.setOnCheckedChangeListener((buttonView, isChecked) -> {
            multiCheck = isChecked;
            if (!isChecked) {
                if (dropcontacts.size() == 1)
                    addDropContact.setVisibility(View.GONE);
            } else
                addDropContact.setVisibility(View.VISIBLE);

        });
        findViewById(R.id.terms_text).setOnClickListener(v -> {
            Intent intent = new Intent(VehiclesActivity.this, TCActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.pic_up_address_edit).setOnClickListener(v -> {
            if (business != null && !business.name.equals("")) {
                sp.edit().putString(Constants.PreferenceKey.EDITITEM, "edit").apply();
                Intent intent = new Intent(VehiclesActivity.this, EditBuyItemsActivity.class);
                intent.putParcelableArrayListExtra(Constants.IntentKey.BUY_ITEMS, (ArrayList<? extends Parcelable>) BuyItemsActivity.buyItems);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, business.name);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, business.address);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                startActivityForResult(intent, REQUEST_CODE);


            } else {
                Intent intent = new Intent(VehiclesActivity.this, MapDragEditLocationActivity.class);
                intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickupInfo);
                intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickupLandmark);
                MapDragEditLocationActivity.mMapIsTouched = false;
                startActivityForResult(intent, 0);
            }
        });
        findViewById(R.id.drop_off_address_edit).setOnClickListener(v -> {
            Intent intent = new Intent(VehiclesActivity.this, MapDragEditLocationActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffInfo);
            intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
            MapDragEditLocationActivity.mMapIsTouched = false;
            startActivityForResult(intent, 1);

        });
        findViewById(R.id.add_dropoff_contact).setOnClickListener(v -> showAddContactDialog());
        findViewById(R.id.add_pickup_contact).setOnClickListener(v -> showAddPicContactDialog());
        if (str_order_type.equalsIgnoreCase("Buy")) {
//            addPickcontact.setVisibility(View.GONE);
            pickupHeading.setText(getApplicationContext().getResources().getString(R.string.buy_items_from));
            addPickcontact.setText(getApplicationContext().getResources().getString(R.string.add_contact_op));
            multichecklay.setVisibility(View.GONE);

            description.setFocusable(false);
            desHeading.setText(getApplicationContext().getResources().getString(R.string.items_u_selected));
            description.setOnClickListener(v -> {
                sp.edit().putString(Constants.PreferenceKey.EDITITEM, "edit").apply();
                Intent intent = new Intent(VehiclesActivity.this, EditBuyItemsActivity.class);
                intent.putParcelableArrayListExtra(Constants.IntentKey.BUY_ITEMS, (ArrayList<? extends Parcelable>) BuyItemsActivity.buyItems);
                if (business != null) {
                    intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, business.name);
                    intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, business.address);
                }
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                startActivityForResult(intent, REQUEST_CODE);
            });

            edit_txt.setOnClickListener(view -> {
                sp.edit().putString(Constants.PreferenceKey.EDITITEM, "edit").apply();
                Intent intent = new Intent(VehiclesActivity.this, EditBuyItemsActivity.class);
                intent.putParcelableArrayListExtra(Constants.IntentKey.BUY_ITEMS, (ArrayList<? extends Parcelable>) BuyItemsActivity.buyItems);
                if (business != null) {
                    intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, business.name);
                    intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, business.address);
                }
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                startActivityForResult(intent, REQUEST_CODE);
            });
        } else if (str_order_type.equalsIgnoreCase("Pickup")) {
            desHeading.setText(getApplicationContext().getResources().getString(R.string.tell_us_items));

            edit_txt.setText("");
        }


        fare = findViewById(R.id.fare);
        coupn_code = findViewById(R.id.coupn_code);

        apply_coupn = findViewById(R.id.coup_txt);
        apply_cpn_btn = findViewById(R.id.apply_c);
        dis_code = findViewById(R.id.disccode);
        TextView delt_coupn = findViewById(R.id.delete_c);
        inv_code = findViewById(R.id.invalid_code);

        applylayout = findViewById(R.id.apply_layout);
        deltelayout = findViewById(R.id.delete_layout);


        apply_coupn.setFilters(new InputFilter[]{new InputFilter.AllCaps()});


        apply_cpn_btn.setOnClickListener(v -> {
            coupncode = apply_coupn.getText().toString();
            if (!coupncode.trim().equals("")) {

                Log.e(TAG, "app fare " + intFare);
                String farestr = String.valueOf(intFare);
                Log.e(TAG, "coupn code" + coupncode);
                Log.e(TAG, "user id" + sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));

                new ValidCoupnTask(VehiclesActivity.this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), coupncode, farestr).execute();

            } else {
                utils.showDialog("Please enter Coupon", VehiclesActivity.this);
            }
            //keyboard hide stufff
            hideSoftKeyboard(VehiclesActivity.this);

        });

        delt_coupn.setOnClickListener(v -> removeCoupon());


        coupn_code.setOnClickListener(v -> {
            deltelayout.setVisibility(View.GONE);
            applylayout.setVisibility(View.VISIBLE);
            inv_code.setVisibility(View.GONE);
            showSoftKeyboard(v);


        });


        findViewById(R.id.back).setOnClickListener(v -> {
            Intent intent = getIntent();
            intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickupInfo);
            intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffInfo);
            intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
            intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickupLandmark);
            setResult(RESULT_OK, intent);
            finish();
        });
        payText.setOnClickListener(v -> callCreateOrder());

        if (!pickupInfo.contactName.trim().equals("") && !pickupInfo.contactNumber.trim().equals("")) {
            updatePickContactInfo();
        }

        if (!dropOffInfo.contactName.trim().equals("") && !dropOffInfo.contactNumber.trim().equals("")) {
            updateDropContactInfo();
        }


        new GetWhizzyVehiclesTask(this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), pickupInfo.getLatitude(), pickupInfo.getLongitude(), dropOffInfo.getLatitude(), dropOffInfo.getLongitude()).execute();
        setDescription();

    }

    private void updatePickContactInfo() {
        pickContact = new DropContact();
        pickContact.drop_contact_name = pickupInfo.contactName;
        pickContact.drop_contact_number = pickupInfo.contactNumber;
        pickContact.drop_contact_more_info = "";
        pickContactLay.setVisibility(View.VISIBLE);
        TextView nameT = findViewById(R.id.name);
        ImageView delete = findViewById(R.id.delete);
        delete.setOnClickListener(v1 -> {
            pickContact = null;
            pickContactLay.setVisibility(View.GONE);
            addPickcontact.setVisibility(View.VISIBLE);
        });
        if (pickContact.drop_contact_more_info.trim().equals("")) {
            String info = pickupInfo.contactName + ", " + pickupInfo.contactNumber;
            nameT.setText(info);
        } else {
            String info = pickupInfo.contactName + ", " + pickupInfo.contactNumber + ", " + pickContact.drop_contact_more_info;
            nameT.setText(info);
        }
        addPickcontact.setVisibility(View.GONE);
    }

    private void updateDropContactInfo() {
        DropContact contact = new DropContact();
        contact.drop_contact_name = dropOffInfo.contactName.trim();
        contact.drop_contact_number = dropOffInfo.contactNumber.trim();
        contact.drop_contact_more_info = "";
        dropcontacts.add(contact);
        DropContactsAdapter adapter = new DropContactsAdapter(VehiclesActivity.this, R.layout.lay_contact, dropcontacts);
        dropContactsList.setAdapter(adapter);
//            me.setVisibility(View.GONE);
        if (!multiCheck)
            addDropContact.setVisibility(View.GONE);
        if (dropcontacts.size() == 1) {
            ScrollView sc = findViewById(R.id.scroll);
            sc.fullScroll(View.FOCUS_DOWN);
        }
        if (dropcontacts.size() > 1) {
            checkMultiple.setEnabled(false);
            dropContactCount.setVisibility(View.VISIBLE);
            dropContactCount.setText(String.valueOf(dropcontacts.size()));
            info.setVisibility(View.VISIBLE);
            multiDropFare = (intFare + (dropcontacts.size() - 1) * additionalPrice);
        } else {
            checkMultiple.setEnabled(true);
            info.setVisibility(View.GONE);
            multiDropFare = intFare;
        }
        String fa = "₹ " + multiDropFare;
        fare.setText(fa);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickupInfo);
        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffInfo);
        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
        intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickupLandmark);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void callCreateOrder() {
        if (!(pickupInfo.latitude == dropOffInfo.latitude && pickupInfo.longitude == dropOffInfo.longitude)) {
            if (str_order_type.equalsIgnoreCase("Buy")) {
                if (dropcontacts.size() > 0
                        && !description.getText().toString().trim().equals(""))
                    if (description.getText().toString().length() > 0)
                        if (termsCheckBox.isChecked()) {
                            payText.setEnabled(false);
                            placeReq.setBackgroundResource(R.drawable.button_grayout_sq);
                            CreateOrderTask task = new CreateOrderTask(VehiclesActivity.this,
                                    pickContact, dropcontacts,
                                    description.getText().toString().trim(),
                                    pickupLandmark,
                                    dropLandmark,
                                    discount_fare, discount, coupncode);
                            StartAsyncTaskInParallel1(task);

                        } else
                            utils.showDialog("You need to accept our Terms & Conditions in order to proceed", VehiclesActivity.this);
                    else
                        utils.showDialog("What is being transported text should be more than 10 characters", VehiclesActivity.this);
                else displayMessage1();

            } else if (str_order_type.equalsIgnoreCase("Pickup")) {
                if (pickContact != null
                        && dropcontacts.size() > 0
                        && !description.getText().toString().trim().equals(""))
                    if (description.getText().toString().length() > 0)
                        if (termsCheckBox.isChecked()) {
                            if (!multiCheck || dropcontacts.size() > 1) {
                                payText.setEnabled(false);
                                placeReq.setBackgroundResource(R.drawable.button_grayout_sq);
                                CreateOrderTask task = new CreateOrderTask(VehiclesActivity.this,
                                        pickContact, dropcontacts,
                                        description.getText().toString().trim(),
                                        pickupLandmark,
                                        dropLandmark,
                                        discount_fare, discount, coupncode);
                                StartAsyncTaskInParallel1(task);
                            } else
                                utils.showDialog("Only 1 drop contact given for multiple drops\n" +
                                        "\n" +
                                        "Please add more drop contacts", VehiclesActivity.this);
                        } else
                            utils.showDialog("You need to accept our Terms & Conditions in order to proceed", VehiclesActivity.this);
                    else
                        utils.showDialog("What is being transported text should be more than 10 characters", VehiclesActivity.this);

                else displayMessage();
            }
        } else
            utils.showDialog("Pickup and Drop locations cannot be same", VehiclesActivity.this);

    }

    private void displayMessage() {
        if (pickContact == null)
            utils.showDialog("Please enter Pickup Contact Details", VehiclesActivity.this);
        else if (dropcontacts == null || dropcontacts.size() == 0)
            utils.showDialog("Please enter Dropoff Contact Details", VehiclesActivity.this);
        else if (description.getText().toString().trim().equals(""))
            utils.showDialog("Please tell us what is being transported", VehiclesActivity.this);

    }

    private void displayMessage1() {
        if (dropcontacts == null || dropcontacts.size() == 0)
            utils.showDialog("Please enter Dropoff Contact Details", VehiclesActivity.this);
        else if (description.getText().toString().trim().equals(""))
            utils.showDialog("Please tell us what is being transported", VehiclesActivity.this);
    }

    private void setDescription() {
        if (str_order_type.equalsIgnoreCase("Pickup"))
            description.setText(OptionsActivity.BringItem);
        else {
            StringBuilder items = new StringBuilder();
            if (BuyItemsActivity.buyItems != null)
                for (PurchaseProduct item : BuyItemsActivity.buyItems) {
                    items.append(item.title).append("-").append(item.count).append("   ");
                }
            description.setText(items.toString());
        }
    }

    void showAddContactDialog() {

        final Dialog dialog = new Dialog(VehiclesActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact);
        dropNme = dialog.findViewById(R.id.name);
        dropNumber = dialog.findViewById(R.id.number);
        final EditText moreInfo = dialog.findViewById(R.id.hint);
        final TextView ok = dialog.findViewById(R.id.ok);
        final TextView me = dialog.findViewById(R.id.from_me);
        final ImageView phoneBook = dialog.findViewById(R.id.phone_book);
        phoneBook.setOnClickListener(v -> {
            Intent intent = new Intent(VehiclesActivity.this, PhoneContactsActivity.class);
            startActivityForResult(intent, DROP_KEY);
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        ok.setBackgroundResource(R.drawable.button_grayout);
        ok.setEnabled(false);

        me.setOnClickListener(v -> {
            dropNme.setText(sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, ""));
            dropNumber.setText(sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        });
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check Fields For Empty Values

                if ((dropNme.getText().toString().trim().length() > 0) && dropNumber.getText().toString().trim().length() > 0) {
                    ok.setEnabled(true);
                    ok.setBackgroundResource(R.drawable.button_green);
                } else {
                    ok.setBackgroundResource(R.drawable.button_grayout);
                    ok.setEnabled(false);
                }
            }
        };
        dropNme.addTextChangedListener(mTextWatcher);
        TextWatcher mTextWatcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check Fields For Empty Values

                if ((dropNme.getText().toString().trim().length() > 0) && dropNumber.getText().toString().trim().length() > 0) {
                    ok.setEnabled(true);
                    ok.setBackgroundResource(R.drawable.button_green);
                } else {
                    ok.setBackgroundResource(R.drawable.button_grayout);
                    ok.setEnabled(false);
                }
            }
        };
        dropNumber.addTextChangedListener(mTextWatcher1);

        ok.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(dropNumber.getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN);
            dialog.dismiss();
            DropContact contact = new DropContact();
            contact.drop_contact_name = dropNme.getText().toString();
            contact.drop_contact_number = dropNumber.getText().toString();
            contact.drop_contact_more_info = moreInfo.getText().toString();
            dropcontacts.add(contact);
            DropContactsAdapter adapter = new DropContactsAdapter(VehiclesActivity.this, R.layout.lay_contact, dropcontacts);
            dropContactsList.setAdapter(adapter);
            me.setVisibility(View.GONE);
            if (!multiCheck)
                addDropContact.setVisibility(View.GONE);
            if (dropcontacts.size() == 1) {
                ScrollView sc = findViewById(R.id.scroll);
                sc.fullScroll(View.FOCUS_DOWN);
            }
            if (dropcontacts.size() > 1) {
                checkMultiple.setEnabled(false);
                dropContactCount.setVisibility(View.VISIBLE);
                dropContactCount.setText(String.valueOf(dropcontacts.size()));
                info.setVisibility(View.VISIBLE);
                multiDropFare = (intFare + (dropcontacts.size() - 1) * additionalPrice);
            } else {
                checkMultiple.setEnabled(true);
                info.setVisibility(View.GONE);
                multiDropFare = intFare;
            }
            String fa = "₹ " + multiDropFare;
            fare.setText(fa);

        });


        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    void showAddPicContactDialog() {

        final Dialog dialog = new Dialog(VehiclesActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact_pic);
        pickName = dialog.findViewById(R.id.name);
        pickNumber = dialog.findViewById(R.id.number);
        final TextView ok = dialog.findViewById(R.id.ok);
        final TextView me = dialog.findViewById(R.id.from_me);
        final EditText moreInfo = dialog.findViewById(R.id.hint);
        final ImageView phoneBook = dialog.findViewById(R.id.phone_book);
        phoneBook.setOnClickListener(v -> {
            Intent intent = new Intent(VehiclesActivity.this, PhoneContactsActivity.class);
            startActivityForResult(intent, PICK_KEY);
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        ok.setBackgroundResource(R.drawable.button_grayout);
        ok.setEnabled(false);
        me.setOnClickListener(v -> {
            pickName.setText(sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, ""));
            pickNumber.setText(sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, ""));
        });
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check Fields For Empty Values
                if ((pickName.getText().toString().trim().length() > 0) && pickNumber.getText().toString().trim().length() > 0) {
                    ok.setEnabled(true);
                    ok.setBackgroundResource(R.drawable.button_blue);
                } else {
                    ok.setBackgroundResource(R.drawable.button_grayout);
                    ok.setEnabled(false);
                }
            }
        };
        pickName.addTextChangedListener(mTextWatcher);
        TextWatcher mTextWatcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check Fields For Empty Values
                if (pickNumber.getText().toString().trim().length() > 0 && pickName.getText().toString().trim().length() > 0) {
                    ok.setEnabled(true);
                    ok.setBackgroundResource(R.drawable.button_blue);
                } else {
                    ok.setBackgroundResource(R.drawable.button_grayout);
                    ok.setEnabled(false);
                }
            }
        };
        pickNumber.addTextChangedListener(mTextWatcher1);
        ok.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(pickNumber.getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN);
            dialog.dismiss();
            pickContact = new DropContact();
            pickContact.drop_contact_name = pickName.getText().toString();
            pickContact.drop_contact_number = pickNumber.getText().toString();
            pickContact.drop_contact_more_info = moreInfo.getText().toString();
            pickContactLay.setVisibility(View.VISIBLE);
            TextView nameT = findViewById(R.id.name);
            ImageView delete = findViewById(R.id.delete);
            delete.setOnClickListener(v1 -> {
                pickContact = null;
                pickContactLay.setVisibility(View.GONE);
                addPickcontact.setVisibility(View.VISIBLE);
            });
            if (pickContact.drop_contact_more_info.trim().equals("")) {
                String str = pickName.getText().toString() + ", " + pickNumber.getText().toString();
                nameT.setText(str);
            } else {
                String str = pickName.getText().toString() + ", " + pickNumber.getText().toString() + ", " + pickContact.drop_contact_more_info;
                nameT.setText(str);
            }
            addPickcontact.setVisibility(View.GONE);

        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    void showInfoDialog() {

        final Dialog dialog = new Dialog(VehiclesActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_info);
        TextView textView = dialog.findViewById(R.id.text);
        TextView total = dialog.findViewById(R.id.total);
        String exptext = "Whizzy charges : ₹" + intFare + "\n\n" + "Addl. drop charges : " + (dropcontacts.size() - 1) + " x ₹" + additionalPrice + " = ₹" + ((dropcontacts.size() - 1) * additionalPrice);
        textView.setText(exptext);
        String tot = "Total charges : ₹" + (intFare + (dropcontacts.size() - 1) * additionalPrice);
        total.setText(tot);
        final ImageView close = dialog.findViewById(R.id.close);
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    public void showSoftKeyboard(View view) {
        apply_coupn.requestFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                view.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

    private void removeCoupon() {
        intFare = intFare1;
        if (dropcontacts.size() > 1) {
            multiDropFare = (intFare + (dropcontacts.size() - 1) * additionalPrice);
        } else {
            multiDropFare = intFare;
        }
        String fa = "₹ " + multiDropFare;
        fare.setText(fa);
        deltelayout.setVisibility(View.GONE);
        applylayout.setVisibility(View.GONE);
        dis_code.setVisibility(View.GONE);
        inv_code.setVisibility(View.GONE);
        applylayout.setVisibility(View.VISIBLE);
        apply_coupn.setText("");
        coupncode = "";
        discount_fare = "";
        discount = "";
    }

    //keyboard hide stufff
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        payText.setEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (data != null) {
                LocationInf temp = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
                if (!(temp.latitude == dropOffInfo.latitude && temp.longitude == dropOffInfo.longitude)) {
                    removeCoupon();
                    pickupInfo = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
                    pickupLandmark = data.getStringExtra(Constants.IntentKey.KEY_LANDMARK);
                    String addr = pickupInfo.locationName + "\n" + pickupInfo.locationAddress + "\n\n" + "Landmark : " + pickupLandmark;
                    pickupAddress.setText(addr);
                    if (!pickupInfo.contactName.trim().equals("") && !pickupInfo.contactNumber.trim().equals("")) {
                        updatePickContactInfo();
                    } else if (data.getBooleanExtra(Constants.IntentKey.FROM_SAVED_ADDRESSES, false)) {
                        pickContact = null;
                        pickContactLay.setVisibility(View.GONE);
                        addPickcontact.setVisibility(View.VISIBLE);
                    }


                    new GetWhizzyVehiclesTask(this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), pickupInfo.getLatitude(), pickupInfo.getLongitude(), dropOffInfo.getLatitude(), dropOffInfo.getLongitude()).execute();
                } else
                    utils.showDialog("Pickup and Drop locations cannot be same", VehiclesActivity.this);
            }
        } else if (requestCode == 1) {
            if (data != null) {
                LocationInf temp = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
                if (!(pickupInfo.latitude == temp.latitude && pickupInfo.longitude == temp.longitude)) {
                    removeCoupon();
                    dropOffInfo = data.getParcelableExtra(Constants.IntentKey.KEY_LOCATION);
                    dropLandmark = data.getStringExtra(Constants.IntentKey.KEY_LANDMARK);
                    String addr = dropOffInfo.locationName + "\n" + dropOffInfo.locationAddress + "\n\n" + "Landmark : " + dropLandmark;
                    dropoffAddress.setText(addr);

                    if (!dropOffInfo.contactName.trim().equals("") && !dropOffInfo.contactNumber.trim().equals("")) {

                        dropcontacts = new ArrayList<>();
                        checkMultiple.setEnabled(true);
                        checkMultiple.setChecked(false);
                        updateDropContactInfo();
                    } else if (data.getBooleanExtra(Constants.IntentKey.FROM_SAVED_ADDRESSES, false)) {
                        dropcontacts = new ArrayList<>();
                        DropContactsAdapter adapter = new DropContactsAdapter(VehiclesActivity.this, R.layout.lay_contact, dropcontacts);
                        dropContactsList.setAdapter(adapter);
                        dropContactCount.setVisibility(View.GONE);
                        addDropContact.setVisibility(View.VISIBLE);
                        checkMultiple.setEnabled(true);
                        checkMultiple.setChecked(false);
                        info.setVisibility(View.GONE);
                        multiDropFare = intFare;
                        String fa = "₹ " + multiDropFare;
                        fare.setText(fa);
                    }
                    new GetWhizzyVehiclesTask(this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), pickupInfo.getLatitude(), pickupInfo.getLongitude(), dropOffInfo.getLatitude(), dropOffInfo.getLongitude()).execute();
                } else
                    utils.showDialog("Pickup and Drop locations cannot be same", VehiclesActivity.this);
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setDescription();
            }
        } else if (requestCode == PICK_KEY) {
            if (data != null) {
                PhoneContact contact = data.getParcelableExtra(Constants.IntentKey.KEY_CONTACT);
                pickName.setText(contact.Name);
                pickNumber.setText(contact.MobileNumber);

            }
        } else if (requestCode == DROP_KEY) {
            if (data != null) {
                PhoneContact contact = data.getParcelableExtra(Constants.IntentKey.KEY_CONTACT);
                dropNme.setText(contact.Name);
                dropNumber.setText(contact.MobileNumber);
            }
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */


    @SuppressLint("StaticFieldLeak")
    private class GetWhizzyVehiclesTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetWhizzyVehiclesResponse getWhizzyVehiclesResponse;
        private String consumerID;
        private double fromLatitude, fromLongitude, toLatitude, toLongitude;
        private ProgressDialog progressDialog;

        GetWhizzyVehiclesTask(Context context, String consumerID, double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
            this.context = context;
            this.fromLatitude = fromLatitude;
            this.fromLongitude = fromLongitude;
            this.toLatitude = toLatitude;
            this.toLongitude = toLongitude;
            this.consumerID = consumerID;
//            this.consumerID = "febc2838-8753-4b50-9c55-b097a4029729";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
//            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String tripTypeString = "one_way";
            String url = Webservice.GET_WHIZZY_VEHICLES + "?" + Webservice.USER_ID + "=" + consumerID + "&" + Webservice.FROM_LATITUDE + "=" + fromLatitude + "&" + Webservice.FROM_LONGITUDE + "=" + fromLongitude + "&" + Webservice.TO_LATITUDE + "=" + toLatitude + "&" + Webservice.TO_LONGITUDE + "=" + toLongitude + "&" + Webservice.TRIP_TYPE + "=" + tripTypeString;
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
                    getWhizzyVehiclesResponse = new Gson().fromJson(json, GetWhizzyVehiclesResponse.class);
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
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            progressDialog.dismiss();
            if (resp == null) {
                if (getWhizzyVehiclesResponse != null) {
                    if (getWhizzyVehiclesResponse.data.Vehicles.size() > 0) {
                        whizzyVehicles = getWhizzyVehiclesResponse.data.Vehicles;
                        Log.e(TAG, "from lat-longs " + fromLatitude + fromLongitude);
                        Log.e(TAG, "to lat-longs " + toLatitude + toLongitude);


                        updatePrice();
                        if (getWhizzyVehiclesResponse.data.Coupon != null && getWhizzyVehiclesResponse.data.Coupon.discounted_fare != null) {
                            ResgetApplyCoupn coupon = getWhizzyVehiclesResponse.data.Coupon;
                            if (dropcontacts.size() > 1) {
                                multiDropFare = Integer.parseInt(coupon.discounted_fare) + (dropcontacts.size() - 1) * additionalPrice;
                            } else {
                                multiDropFare = Integer.parseInt(coupon.discounted_fare);
                            }
                            intFare = Integer.parseInt(coupon.discounted_fare);
                            String fa = "₹ " + multiDropFare;
                            fare.setText(fa);
                            dis_code.setVisibility(View.VISIBLE);
                            dis_code.setText(coupon.message);

                            apply_coupn.setEnabled(false);
                            apply_coupn.setHint("Coupon not necessary!");
                            apply_cpn_btn.setTextColor(Color.parseColor("#888888"));
                            apply_cpn_btn.setEnabled(false);


                            coupncode = coupon.coupon_code;
                            discount_fare = coupon.discounted_fare;
                            discount = coupon.discount;
                        }

                    } else {
                        showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
                    }
                }
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {
                showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class ValidCoupnTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private ApplyCoupnResponse getapplycoupnresponse;
        private String userid, coupnCode, farewhizzy;
        private ProgressDialog progressDialog;

        ValidCoupnTask(Context context, String userid, String coupncode, String farewhizzy) {
            this.context = context;
            this.userid = userid;
            this.coupnCode = coupncode;
            this.farewhizzy = farewhizzy;

            Log.e(TAG, "useriddd" + userid);
            Log.e(TAG, "coupncode" + coupnCode);
            Log.e(TAG, "farewhizzy" + farewhizzy);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            if (!VehiclesActivity.this.isFinishing())
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_COUPN_VALID + "?" + Webservice.USER_ID + "=" + userid + "&" + Webservice.COUPNCODE + "=" + coupnCode + "&" + Webservice.WHIZZYFARE + "=" + farewhizzy;
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
                    getapplycoupnresponse = new Gson().fromJson(json, ApplyCoupnResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getapplycoupnresponse = new Gson().fromJson(json, ApplyCoupnResponse.class);
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (progressDialog != null && progressDialog.isShowing() && !VehiclesActivity.this.isFinishing())
                progressDialog.dismiss();
            if (resp == null) {
                if (getapplycoupnresponse != null) {
                    apply_coupn.setText("");
                    if (dropcontacts.size() > 1) {
                        multiDropFare = Integer.parseInt(getapplycoupnresponse.data.discounted_fare) + (dropcontacts.size() - 1) * additionalPrice;
                    } else {
                        multiDropFare = Integer.parseInt(getapplycoupnresponse.data.discounted_fare);
                    }
                    intFare = Integer.parseInt(getapplycoupnresponse.data.discounted_fare);
                    String fa = "₹ " + multiDropFare;
                    fare.setText(fa);
                    applylayout.setVisibility(View.GONE);
                    deltelayout.setVisibility(View.VISIBLE);
                    dis_code.setVisibility(View.VISIBLE);
                    coupn_code.setText(coupnCode);
                    coupn_code.setClickable(false);

                    dis_code.setText(getapplycoupnresponse.data.message);


                    inv_code.setVisibility(View.GONE);

                    discount_fare = getapplycoupnresponse.data.discounted_fare;
                    discount = getapplycoupnresponse.data.discount;
                }
            } else if (!Utils.isNetConnected(context)) {
                showDialog2(context);
                coupncode = "";
                discount_fare = "";
                discount = "";
                coupn_code.setClickable(true);
            } else {
                if (getapplycoupnresponse != null && !getapplycoupnresponse.data.Error.equals("")) {
                    coupncode = "";
                    discount_fare = "";
                    discount = "";
                    //update UI here with Error message

                    inv_code.setText(getapplycoupnresponse.data.Error);
                    inv_code.setVisibility(View.VISIBLE);
                    coupn_code.setText(coupnCode);
                    deltelayout.setVisibility(View.VISIBLE);
                    applylayout.setVisibility(View.GONE);
                    coupn_code.setClickable(true);

                }
            }
        }
    }

    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);

        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);


        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            new GetWhizzyVehiclesTask(VehiclesActivity.this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), pickupInfo.getLatitude(), pickupInfo.getLongitude(), dropOffInfo.getLatitude(), dropOffInfo.getLongitude()).execute();
        });

        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }


    void showDialog2(final Context context) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle("No internet connection");

        alertDialog.setMessage("Please check your wifi/mobile data signal");
        alertDialog.setCancelable(false);


        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            coupncode = apply_coupn.getText().toString();


            Log.e(TAG, "app fare " + intFare);
            String farestr = String.valueOf(intFare);
            Log.e(TAG, "coupn code" + coupncode);
            Log.e(TAG, "user id" + sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"));

            new ValidCoupnTask(VehiclesActivity.this, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0"), coupncode, farestr).execute();

        });

        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePrice() {

        for (WhizzyVehicle vehicle : whizzyVehicles)
            if (vehicle.vehicle_name.equalsIgnoreCase("Two Wheeler")) {
                whizzyVehicle = vehicle;
                try {
                    Log.e(TAG, "app fare " + vehicle.approximate_fare);
                    intFare = (int) vehicle.approximate_fare;
                    intFare1 = (int) vehicle.approximate_fare;
                    if (dropcontacts.size() > 1) {
                        multiDropFare = intFare + (dropcontacts.size() - 1) * additionalPrice;
                    } else {
                        multiDropFare = intFare;
                    }
                    String fa = "₹ " + multiDropFare;
                    fare.setText(fa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }


    class DropContactsAdapter extends ArrayAdapter<DropContact> {

        Context con;

        DropContactsAdapter(Context context, int textViewResourceId, List<DropContact> objects) {
            super(context, textViewResourceId, objects);
            this.con = context;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.lay_contact, null);
            TextView name = v.findViewById(R.id.name);

            ImageView textDelete = v.findViewById(R.id.delete);
            final DropContact contact = dropcontacts.get(dropcontacts.size() - position - 1);
            String totalString = contact.drop_contact_name + ", " + contact.drop_contact_number;
            if (!contact.drop_contact_more_info.equals(""))
                totalString = totalString + ", " + contact.drop_contact_more_info;
            name.setText(totalString);
            textDelete.setOnClickListener(v1 -> {
                dropcontacts.remove(contact);
                notifyDataSetChanged();
                if (dropcontacts.size() <= 1) {
                    checkMultiple.setEnabled(true);

                }

                if (dropcontacts.size() != 0)
                    dropContactCount.setText(String.valueOf(dropcontacts.size()));
                else {
                    dropContactCount.setVisibility(View.GONE);
                    addDropContact.setVisibility(View.VISIBLE);
                }
                if (dropcontacts.size() > 1) {
                    multiDropFare = intFare + (dropcontacts.size() - 1) * additionalPrice;
                    info.setVisibility(View.VISIBLE);
                } else {
                    multiDropFare = intFare;
                    info.setVisibility(View.GONE);
                }
                String fa = "₹ " + multiDropFare;
                fare.setText(fa);
            });

            return v;

        }


    }

    @SuppressLint("StaticFieldLeak")
    class CreateOrderTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private CustomerRegistrationResponse createOrderResponse;
        private SharedPreferences sp;
        private String description, pickLandmark, dropLandmark;
        private String discount_fare, discount, coupn_code;
        private DropContact pickContact;
        private List<DropContact> dropContacts;

        CreateOrderTask(Context context, DropContact pickContact, List<DropContact> dropContacts, String description, String pickLandmark, String dropLandmark,
                        String discount_fare, String discount, String coupn_code) {
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            this.pickContact = pickContact;
            this.dropContacts = dropContacts;
            this.description = description;
            this.pickLandmark = pickLandmark;
            this.dropLandmark = dropLandmark;
            this.discount_fare = discount_fare;
            this.discount = discount;
            this.coupn_code = coupn_code;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_ID, sp.getString(Constants.PreferenceKey.KEY_USER_ID, "0")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.USER_MOBILE, sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_ID, whizzyVehicle.vehicle_id));
            nameValuePairs.add(new BasicNameValuePair(Webservice.VEHICLE_NAME, whizzyVehicle.vehicle_name));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PAYMENT_REQUIRED, "false"));
            if (pickContact != null) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NAME, pickContact.drop_contact_name));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NUMBER, pickContact.drop_contact_number));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_MORE_INFO, pickContact.drop_contact_more_info));
            } else {
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NAME, sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, "")));
                nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_NUMBER, sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "")));
            }
            nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LATITUDE, pickupInfo.getLatitude() + ""));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LONGITUDE, pickupInfo.getLongitude() + ""));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LATITUDE, dropOffInfo.getLatitude() + ""));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_LONGITUDE, dropOffInfo.getLongitude() + ""));
            nameValuePairs.add(new BasicNameValuePair(Webservice.TRANSPORT_DESC, description));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_LANDMARK, pickLandmark));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_LANDMARK, dropLandmark));
            nameValuePairs.add(new BasicNameValuePair(Webservice.TRIP_TYPE, tripType == 0 ? "one_way" : "round_trip"));
            nameValuePairs.add(new BasicNameValuePair(Webservice.PICKUP_FULL_ADDRESS, pickupInfo.getLocationName() + "\n" + pickupInfo.getLocationAddress()));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DROPOFF_FULL_ADDRESS, dropOffInfo.getLocationName() + "\n" + dropOffInfo.getLocationAddress()));
            nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE, sp.getString(Constants.PreferenceKey.BUYANDPICKUP, "")));
            nameValuePairs.add(new BasicNameValuePair(Webservice.APP_VERSION, Constants.APP_VERSION));
            //coupn stuff here
            nameValuePairs.add(new BasicNameValuePair(Webservice.COUPN_CODE, coupn_code));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNTED_FARE, discount_fare));
            nameValuePairs.add(new BasicNameValuePair(Webservice.DISCOUNT, discount));
            sp.edit().putString(Webservice.COUPN_CODE, coupn_code).apply();
            sp.edit().putString(Webservice.DISCOUNTED_FARE, discount_fare).apply();
            sp.edit().putString(Webservice.DISCOUNT, discount).apply();

            if (getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false))
                nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULED_ORDER, "Yes"));
            if (getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME) != null)
                nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_DATE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME)));
            if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME) != null) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME)));
                sp.edit().putString(Webservice.BUSINESS_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME)).apply();
            }
            if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID) != null) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.BUSINESS_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)));
                sp.edit().putString(Webservice.BUSINESS_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();

            }


            JSONArray jArry = new JSONArray();
            for (DropContact contact : dropContacts) {
                JSONObject ob = new JSONObject();
                try {
                    ob.put(Webservice.DROPOFF_NAME, contact.drop_contact_name);
                    ob.put(Webservice.DROPOFF_NUMBER, contact.drop_contact_number);
                    ob.put(Webservice.DROP_MORE_INFO, contact.drop_contact_more_info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jArry.put(ob);
            }
            nameValuePairs.add(new BasicNameValuePair(Webservice.DROP_CONTACT_DETAILS, jArry.toString()));
            sp.edit().putString(Webservice.DROP_CONTACT_DETAILS, jArry.toString()).apply();
            if (dropContacts.size() > 1) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE_FLOW, "single_pickup_multiple_drop_contacts"));
                sp.edit().putString(Webservice.ORDER_TYPE_FLOW, "single_pickup_multiple_drop_contacts").apply();
            } else {
                nameValuePairs.add(new BasicNameValuePair(Webservice.ORDER_TYPE_FLOW, "single_pickup_single_drop_contact"));
                sp.edit().putString(Webservice.ORDER_TYPE_FLOW, "single_pickup_single_drop_contact").apply();
            }


            if (str_order_type.equalsIgnoreCase("Buy")) {
                nameValuePairs.add(new BasicNameValuePair(Webservice.BUY_LOCATION, "Specific"));
                sp.edit().putString(Webservice.BUY_LOCATION, "Specific").apply();
            }

            nameValuePairs.add(new BasicNameValuePair(Webservice.OPERATING_SYSTEM, "Android"));
            nameValuePairs.add(new BasicNameValuePair(Webservice.SCHEDULE_ON_MY_BEHALF, "No"));

            String json = Webservice.callPostService1(Webservice.CREATE_ORDER, nameValuePairs);
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
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    createOrderResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            } else {
                try {
                    // Convert String to json object
                    createOrderResponse = new Gson().fromJson(json, CustomerRegistrationResponse.class);
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                payText.setEnabled(true);
                placeReq.setBackgroundResource(R.drawable.button_blue_sq);
                if (createOrderResponse != null && !createOrderResponse.data.order_id.equals("")) {
                    if (getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false)) {
                        showScheduleDialog(getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));

                    } else {
                        sp.edit().putString(Constants.PreferenceKey.KEY_RECENT_ORDER_ID, createOrderResponse.data.order_id).apply();


                        Intent intent = new Intent(VehiclesActivity.this, LoadingActivity.class);
                        intent.putExtra(Constants.IntentKey.KEY_ORDER_ID, createOrderResponse.data.order_id);
                        intent.putExtra("drivercanc", "two");

                        intent.putExtra(Constants.IntentKey.KEY_PICKUP_LOCATION, pickupInfo);
                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LOCATION, dropOffInfo);
                        intent.putExtra(Constants.IntentKey.KEY_WHIZZY_VEHICLE, whizzyVehicle);
                        intent.putExtra(Constants.IntentKey.KEY_PICKUP_CONTACT, pickContact);
                        intent.putParcelableArrayListExtra(Constants.IntentKey.KEY_DROP_CONTACT, (ArrayList<? extends Parcelable>) dropContacts);
                        intent.putExtra(Constants.IntentKey.KEY_DESC, description);
                        intent.putExtra(Constants.IntentKey.KEY_PICK_UP_LANDMARK, pickLandmark);
                        intent.putExtra(Constants.IntentKey.KEY_DROP_OFF_LANDMARK, dropLandmark);
                        intent.putExtra(Constants.IntentKey.COUPN_CODE, coupn_code);
                        intent.putExtra(Constants.IntentKey.DISCOUNT_FARE, discount_fare);
                        intent.putExtra(Constants.IntentKey.DISCOUNT, discount);

                        if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME) != null)
                            intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
                        if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID) != null)
                            intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));


                        startActivity(intent);
                        termsCheckBox.setChecked(false);
                    }

                }
            } else if (!Utils.isNetConnected(context)) {
                payText.setEnabled(true);
                placeReq.setBackgroundResource(R.drawable.button_blue_sq);
                showDialog3(context, "No internet connection", "Please check your wifi/mobile data signal");
            } else {
                payText.setEnabled(true);
                placeReq.setBackgroundResource(R.drawable.button_blue_sq);
                if (createOrderResponse != null && !createOrderResponse.data.message.equals(""))
                    Utils.showDialogFailure(context, resp);
                else
                    showDialog3(context, "Oops!!!", "Something went wrong Please try again after sometime!");

            }
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel1(
            CreateOrderTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void showDialog3(final Context context, String title, String message) {

//        public void showAlertDialog(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();

            CreateOrderTask task = new CreateOrderTask(VehiclesActivity.this,
                    pickContact,
                    dropcontacts,
                    description.getText().toString().trim(),
                    pickupLandmark.trim(),
                    dropLandmark.trim(),
                    discount_fare, discount, coupncode);
            StartAsyncTaskInParallel1(task);
//                }
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public void showScheduleDialog(String message) {
        if (scheduleDialog != null && scheduleDialog.isShowing()) {
            scheduleDialog.dismiss();
        } else {
            scheduleDialog = new Dialog(VehiclesActivity.this);

            scheduleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            scheduleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            scheduleDialog.setContentView(R.layout.dialog_interest_settings);
            scheduleDialog.setCancelable(false);
            TextView title = scheduleDialog.findViewById(R.id.text);
            TextView heading = scheduleDialog.findViewById(R.id.heading);
            heading.setText(getApplicationContext().getResources().getString(R.string.req_sche_succ));
            heading.setVisibility(View.VISIBLE);
            title.setText(message);
            Button ok = scheduleDialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                Intent intentCall = new Intent(VehiclesActivity.this, HomePagerActivity.class);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);
                scheduleDialog.dismiss();
                finish();
            });

            scheduleDialog.show();
        }
    }

}