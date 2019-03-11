package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.db.DBHandler;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.LocationInf;

import java.util.List;

/**
 * Created by Sowjanya on 5/8/2017.
 */

public class LocationSearchActivity extends Activity {
    private DBHandler db;
    private TextView   recentText;
//    private RelativeLayout homeLay;
//    private LocationInf homeLocation;
    private LinearLayout locationsLay, recentLay;
    private int type;
    private String locationNameString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_location_search);
        db = new DBHandler(this);

        //new code
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Location search", "Place: " + place.getName()+place.getLatLng());
                Location targetLocation = new Location("");//provider name is unecessary
                targetLocation.setLatitude(place.getLatLng().latitude);//your coords of course
                targetLocation.setLongitude(place.getLatLng().longitude);
                LocationInf loca = new LocationInf();
                try {
                    loca.setLocationName(place.getName().toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                loca.setLatitude(place.getLatLng().latitude);
                loca.setLongitude(place.getLatLng().longitude);
                loca.setLocationAddress(place.getAddress().toString());
                //adding recent locations to db
                loca.setType(2);
                if (db.getlocation(place.getAddress().toString()) == null)
                    db.addLocation(loca);

                Intent intent = getIntent();
                intent.putExtra(Constants.IntentKey.KEY_LOCATION, loca);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Location search", "An error occurred: " + status);
            }
        });
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .build();

        autocompleteFragment.setFilter(typeFilter);

//        homeLocation = db.getHomeLocation();
//        homeText = findViewById(R.id.home_text);
        recentLay = findViewById(R.id.recent_location_lay);
        locationsLay = findViewById(R.id.locations_lay);
        recentText = findViewById(R.id.recent_text);
//        homeaddress = findViewById(R.id.home_address_text);
//        homeLay = findViewById(R.id.home_lay);
        findViewById(R.id.back).setOnClickListener(v -> finish());

//        homeLay.setOnClickListener(v -> {
//            if (homeLocation == null) {
//                Intent intent =
//                        null;
//                try {
//                    type = 0;
//                    AutocompleteFilter typeFilter1 = new AutocompleteFilter.Builder()
//                            .setCountry("IN")
//                            .build();
//                    intent = new PlaceAutocomplete
//                            .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter1)
//                            .build(LocationSearchActivity.this);
//                } catch (GooglePlayServicesRepairableException e) {
//                    e.printStackTrace();
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    e.printStackTrace();
//                }
//                startActivityForResult(intent, 1);
//            } else {
//                Intent intent = getIntent();
//                intent.putExtra(Constants.IntentKey.KEY_LOCATION, (LocationInf) homeLay.getTag(R.string.tag_location));
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        });
//        if (homeLocation != null) {
//            RelativeLayout.LayoutParams layoutParams =
//                    (RelativeLayout.LayoutParams) homeText.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
//            homeText.setLayoutParams(layoutParams);
//            homeText.setText(homeLocation.getLocationName());
//            homeaddress.setVisibility(View.VISIBLE);
//            homeaddress.setText(homeLocation.getLocationAddress());
//            homeLay.setTag(R.string.tag_location, homeLocation);
//
//        } else {
//            RelativeLayout.LayoutParams layoutParams =
//                    (RelativeLayout.LayoutParams) homeText.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
//            homeText.setLayoutParams(layoutParams);
//
//        }
//        updateCustomeLocations();
        updateRecentLocations();


    }

    private void updateCustomeLocations() {
        locationsLay.removeAllViews();
        List<LocationInf> addedLocations = db.getCustomLocations();
        for (LocationInf loc : addedLocations) {
            View customLay = getLayoutInflater().inflate(R.layout.add_location_lay, null);
            final RelativeLayout lay = customLay.findViewById(R.id.lay);
            TextView title = customLay.findViewById(R.id.text);
            ImageView image = customLay.findViewById(R.id.location_image);
            image.setImageResource(R.drawable.ic_saved);
            TextView address = customLay.findViewById(R.id.address_text);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) title.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            title.setLayoutParams(layoutParams);
            title.setText(loc.getLocationName());
            address.setText(loc.getLocationAddress());
            address.setVisibility(View.VISIBLE);
            lay.setTag(R.string.tag_location, loc);
            lay.setOnClickListener(v -> {
                Intent intent = getIntent();
                intent.putExtra(Constants.IntentKey.KEY_LOCATION, (LocationInf) lay.getTag(R.string.tag_location));
                setResult(RESULT_OK, intent);
                finish();
            });
            locationsLay.addView(customLay);
        }
        if (addedLocations.size() < 5) {
            View customLay = getLayoutInflater().inflate(R.layout.add_location_lay, null);
            RelativeLayout lay = customLay.findViewById(R.id.lay);
            lay.setOnClickListener(v -> showLocationDialog());
            locationsLay.addView(customLay);
        }
    }

    void showLocationDialog() {

        final Dialog dialog = new Dialog(LocationSearchActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_location_name);
        final EditText locationName = dialog.findViewById(R.id.location_name);
        final Button ok = dialog.findViewById(R.id.continuee);
        ok.setBackgroundResource(R.drawable.button_disable);
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
                if (locationName.getText().toString().trim().length() > 0) {
                    ok.setEnabled(true);
                    ok.setBackgroundResource(R.drawable.button_blue);
                } else {
                    ok.setBackgroundResource(R.drawable.button_disable);
                    ok.setEnabled(false);
                }
            }
        };
        locationName.addTextChangedListener(mTextWatcher);
        ok.setOnClickListener(v -> {
            if (locationName.getText().toString().trim().length() > 0) {

                dialog.dismiss();
                Intent intent =
                        null;
                try {
                    type = 1;
                    locationNameString = locationName.getText().toString().trim();
//                        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
//                                .setTypeFilter(Place.TYPE_COUNTRY)
//                                .setCountry("INDIA")
//                                .build();
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .build();
                    intent = new PlaceAutocomplete
                            .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter)
                            .build(LocationSearchActivity.this);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 1);

            }

        });

        dialog.show();
    }

    private void updateRecentLocations() {
        recentLay.removeAllViews();
        List<LocationInf> recentLocations = db.getRecentLocations();
        if (recentLocations.size() > 0)
            recentText.setVisibility(View.VISIBLE);
        else
            recentText.setVisibility(View.GONE);
        for (LocationInf loc : recentLocations) {
            View customLay = getLayoutInflater().inflate(R.layout.add_location_lay, null);
            final RelativeLayout lay = customLay.findViewById(R.id.lay);
            ImageView image = customLay.findViewById(R.id.location_image);
            image.setImageResource(R.drawable.ic_recent_search);
            TextView title = customLay.findViewById(R.id.text);
            TextView address = customLay.findViewById(R.id.address_text);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) title.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            title.setLayoutParams(layoutParams);
            title.setText(loc.getLocationName());
            address.setText(loc.getLocationAddress());
            address.setVisibility(View.VISIBLE);
            lay.setTag(R.string.tag_location, loc);
            lay.setOnClickListener(v -> {
                Intent intent = getIntent();
                intent.putExtra(Constants.IntentKey.KEY_LOCATION, (LocationInf) lay.getTag(R.string.tag_location));
                setResult(RESULT_OK, intent);
                finish();
            });
            recentLay.addView(customLay);
        }

    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber() + "Lat lang" + place.getLatLng().latitude + " " + place.getLatLng().longitude);

//                ((TextView) findViewById(R.id.pick_up_location))
//                        .setText(place.getName() + ",\n" +
//                                place.getAddress() + "\n" + place.getPhoneNumber());
                LocationInf loc = new LocationInf();
                loc.setType(type);
                if (type == 0)
                    loc.setLocationName("Home");
                else
                    loc.setLocationName(locationNameString);
                loc.setLocationAddress(place.getAddress() + "");
                loc.setLatitude(place.getLatLng().latitude);
                loc.setLongitude(place.getLatLng().longitude);
                db.addLocation(loc);
//                if (type == 0) {
//                    RelativeLayout.LayoutParams layoutParams =
//                            (RelativeLayout.LayoutParams) homeText.getLayoutParams();
//                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
//                    homeText.setLayoutParams(layoutParams);
//                    homeText.setText(loc.getLocationName());
//                    homeaddress.setText(loc.getLocationAddress());
//                    homeaddress.setVisibility(View.VISIBLE);
//                    homeLay.setTag(R.string.tag_location, loc);
//                    homeLocation = loc;
//                } else {
//                    updateCustomeLocations();
//                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            }
        }
    }
}
