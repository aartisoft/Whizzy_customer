package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.Business;
import com.sprvtec.whizzy.vo.BusinessDetails;
import com.sprvtec.whizzy.vo.GetBusinessListResponse;
import com.sprvtec.whizzy.vo.GetHomeImagesResponse;
import com.sprvtec.whizzy.vo.GridCategory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class BusinessListActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    ListView bisness_list;
    BusinessListAdapter adapter;
    String radius = "5000", keys = "AIzaSyCDkfUXckHnm5kfRRcl4FZ5v4L8k_zsNAc";
    LinearLayout loadingLay;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 5;//10
    private static final long FASTEST_INTERVAL = 1000 * 2;//5
    private static final String TAG = "BusinessListActivity";
    private Location mLastLocation;
    private String loclatlong, categoryname = "", subcategory = "", maincategory = "";
    Dialog locationDialog;
    ImageView back;
    private TextView cnt;
    private TextView noDataText;
    private TextView searchGo;
    private boolean initViewShow;
    private int preLast = 0;
    private boolean hasData = true;
    private ArrayList<Business> businessListFireStore;
    private boolean apiData = true, fromSearch;
    private String searchKey = "";
    private EditText searchEdit;
    private Dialog dialog;
    private GetBusinessesTask businessesTask;
    private SharedPreferences sp;


    //new locaion services stuff
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean started;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businesslist);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        locationServiceStuff();
        loadingLay = findViewById(R.id.loading);
        searchGo = findViewById(R.id.go);
        loadingLay.setEnabled(false);
        noDataText = findViewById(R.id.text);
        bisness_list = findViewById(R.id.listView_business);
        bisness_list.setOnScrollListener(this);
        TextView title_edt = findViewById(R.id.title_edit);
        back = findViewById(R.id.back);
        fromSearch = getIntent().getBooleanExtra(Constants.IntentKey.FROM_SEARCH, false);
        loadingLay.setVisibility(View.VISIBLE);
        businessListFireStore = new ArrayList<>();
        searchEdit = findViewById(R.id.search);
        searchGo.setBackgroundResource(R.drawable.button_disable);
        searchGo.setEnabled(false);
        if (fromSearch)
            searchEdit.setHint("Search businesses in all categories");

        findViewById(R.id.canttext).setOnClickListener(v -> {
            Intent intent = new Intent(BusinessListActivity.this, BuyItemsActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_BUY_HINT, "Enter Items");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, "");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, "");
            intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent);
        });
        findViewById(R.id.close).setOnClickListener(view -> {
            searchEdit.setText("");
            clearSearch();
        });
        findViewById(R.id.go).setOnClickListener(view -> {
            searchKey = searchEdit.getText().toString().trim();
            hasData = true;
            preLast = 0;
            bisness_list.setAdapter(null);
            businessListFireStore = new ArrayList<>();
            businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
            StartAsyncTaskInParallel1(businessesTask);
            searchGo.setBackgroundResource(R.drawable.button_disable);
            searchGo.setEnabled(false);
            apiData = true;
            noDataText.setVisibility(View.GONE);
        });


        if (getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM) != null) {
            if (getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM).equals("Food") || getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM).equals("Other"))
                title_edt.setText(getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM));
            else
                title_edt.setText(getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
        }
        cnt = findViewById(R.id.counting);
        findViewById(R.id.top_lay).setOnClickListener(v -> {
            int val = Integer.parseInt(sp.getString(Constants.PreferenceKey.COUNTVALUE, ""));
            String placid = sp.getString(Constants.PreferenceKey.PLACEID, "");
            String address, placelat, placelong, placename;
            address = sp.getString(Constants.PreferenceKey.PLACEADDR, "");
            placelat = sp.getString(Constants.PreferenceKey.PLACELAT, "0");
            placelong = sp.getString(Constants.PreferenceKey.PLACELONG, "0");
            placename = sp.getString(Constants.PreferenceKey.PLACENAME, "");
            if (val > 0) {
                Business b = new Business();
                b.id = placid;
                b.address = address;
                b.latitude = Double.parseDouble(placelat);
                b.longitude = Double.parseDouble(placelong);
                b.name = placename;
                Intent intent = new Intent(BusinessListActivity.this, BuyItemsActivity.class);
                intent.putExtra(Constants.IntentKey.KEY_BUY_HINT, "Enter Items");
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, placid);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, placename);
                intent.putExtra(Constants.IntentKey.FROM_CART, true);
                intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                intent.putExtra(Constants.IntentKey.BUSINESS, b);
                startActivity(intent);
            }

        });
        back.setOnClickListener(v -> finish());

        // Add Text Change Listener to EditText
        searchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
//                adapterPlaces.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((searchEdit.getText().toString()).trim().length() > 0) {
                    searchGo.setEnabled(true);
                    searchGo.setBackgroundResource(R.drawable.button_green);
                } else {
                    searchGo.setBackgroundResource(R.drawable.button_disable);
                    searchGo.setEnabled(false);
                    clearSearch();
                }
            }
        });


        thread.start();


        System.out.println("counting value" + sp.getString(Constants.PreferenceKey.COUNTVALUE, ""));
    }

    private void clearSearch() {
        searchKey = "";
        hasData = true;
        preLast = 0;
        bisness_list.setAdapter(null);
        businessListFireStore = new ArrayList<>();
        if (!fromSearch) {
            businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
            StartAsyncTaskInParallel1(businessesTask);
        }
        apiData = true;
        noDataText.setVisibility(View.GONE);
    }

    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        if (!sp.getString(Constants.PreferenceKey.COUNTVALUE, "").equals("")) {
                            cnt.setText(sp.getString(Constants.PreferenceKey.COUNTVALUE, ""));

                        }
                    });
                }
            } catch (InterruptedException ignored) {
            }
        }
    };

    private void initView() {
        loadingLay.setVisibility(View.GONE);
        initViewShow = true;

        if (!fromSearch) {
            loclatlong = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            System.out.println("locationlatlong" + loclatlong + getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
            GridCategory businessCategory = getIntent().getParcelableExtra(Constants.IntentKey.KEY_BUSINESS_CATEGORY);
            if (businessCategory != null) {
                maincategory = businessCategory.mainCategory;
                subcategory = businessCategory.subCategory;
                categoryname = businessCategory.googleCategory;
                businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
                StartAsyncTaskInParallel1(businessesTask);
                Log.e("data", "maincategory: " + maincategory + "\n" + "subcategory: " + subcategory + "\n" + "categoryname: " + categoryname);
            }
//            switch (getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM)) {
//                case "Food":
//
//                    if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Restaurants")) {
//                        subcategory = "Restaurant";
//                        categoryname = "restaurant";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Coffee/Tea")) {
//                        subcategory = "Coffee/Tea";
//                        categoryname = "cafe";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Dairy")) {
//                        subcategory = "Dairy";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Take Away")) {
//                        subcategory = "Curry Point";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Juices")) {
//                        subcategory = "Juices";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Ice Creams")) {
//                        subcategory = "Ice Creams";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Bakery")) {
//                        subcategory = "Bakery";
//                        categoryname = "bakery";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Sweets")) {
//                        subcategory = "Sweets";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Fruits/Vegetables")) {
//                        subcategory = "Fruits/Vegetables";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Snacks/Street Food")) {
//                        subcategory = "Snacks/Streetfood";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Rolls/Sandwiches")) {
//                        subcategory = "Franky/Sandwiches";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Paan Shops")) {
//                        subcategory = "Paan Shop";
//                        maincategory = "Food";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//
//                    }
//
//                    break;
//                case "Restaurants":
//                    categoryname = "restaurant";
//                    maincategory = "Food";
//                    subcategory = "Restaurant";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Bakery":
//                    categoryname = "bakery";
//                    maincategory = "Food";
//                    subcategory = "Bakery";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Sweets":
//                    maincategory = "Food";
//                    subcategory = "Sweets";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Grocery":
//                    maincategory = "Grocery";
//                    subcategory = "";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//                    break;
//                case "Medicines":
//                    categoryname = "pharmacy";
//                    maincategory = "Medical Shop";
//                    subcategory = "";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Flowers":
//                    categoryname = "florist";
//                    maincategory = "Flower Shop";
//                    subcategory = "";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Meat":
//                    maincategory = "Food";
//                    subcategory = "Meat Shop";
//                    businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                    StartAsyncTaskInParallel1(businessesTask);
//
//                    break;
//                case "Other":
//
//                    if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Stationery Stores")) {
//                        maincategory = "Other";
//                        subcategory = "Stationery Store";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Gift Shops")) {
//                        subcategory = "Gift Shop";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Toy Stores")) {
//                        subcategory = "Toy Store";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Xerox Shops")) {
//                        subcategory = "Xeroz Shop";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Electrical Stores")) {
//                        subcategory = "Electrical Store";
//                        categoryname = "electronics_store";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Petrol Pumps")) {
//                        categoryname = "gas_station";
//                        subcategory = "petrol pumps";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Pet Supplies")) {
//                        subcategory = "Pet Supplies";
//                        categoryname = "pet_store";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Dry Cleaning")) {
//                        subcategory = "Laundry/Ironing";
//                        categoryname = "laundry";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM).equalsIgnoreCase("Nursery/Plants")) {
//                        subcategory = "Nursery/Plants";
//                        maincategory = "Other";
//                        businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
//                        StartAsyncTaskInParallel1(businessesTask);
//                    }
//
//                    break;
//            }

            System.out.println("loc===" + mLastLocation.getLatitude() + "longialso==" + mLastLocation.getLatitude());

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        checkLocationEnable();
        startNewLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNewLocationUpdates();
    }


    @Override
    public void onResume() {
        super.onResume();
        startNewLocationUpdates();
    }


    private void checkLocationEnable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        if (!gps_enabled && !network_enabled) {
            locationSettingsDialog();
        }
    }

    private void locationSettingsDialog() {
        locationDialog = new Dialog(this);

        locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        locationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        locationDialog.setContentView(R.layout.dialog_delete_all);
        locationDialog.setCanceledOnTouchOutside(false);
        TextView title = locationDialog.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.gps_network_not_enabled));
        Button yes = locationDialog.findViewById(R.id.yes);
        yes.setText(getResources().getString(R.string.open_location_settings));

        Button no = locationDialog.findViewById(R.id.no);
        no.setText(getResources().getString(R.string.cancel));
        yes.setOnClickListener(v -> {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
            locationDialog.dismiss();
        });
        no.setOnClickListener(v -> {
            finish();
            locationDialog.dismiss();
        });
        locationDialog.show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        switch (view.getId()) {
            case R.id.listView_business:

                // Make your calculation stuff here. You have all your
                // needed info from the parameters of this function.

                // Sample calculation to determine if the last
                // item is fully visible.

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        Log.d("Last", "Last");
                        if (hasData && businessListFireStore.size() != 0) {
                            businessesTask = new GetBusinessesTask(BusinessListActivity.this, businessListFireStore.size(), maincategory, subcategory, searchKey);
                            StartAsyncTaskInParallel1(businessesTask);
                            preLast = lastItem;
                        } else if (apiData && searchKey.equals("")) {
                            if (!categoryname.equals("") && (maincategory.equalsIgnoreCase("Food") ||
                                    maincategory.equalsIgnoreCase("Other") ||
                                    maincategory.equalsIgnoreCase("Grocery") ||
                                    maincategory.equalsIgnoreCase("Medical Shop") ||
                                    maincategory.equalsIgnoreCase("Flower Shop") ||
                                    maincategory.equalsIgnoreCase("Meat"))
                                    ) {
                                apiData = false;
                                if (businessListFireStore.size() == 0)
                                    new GetGoogleBusinessListTask(BusinessListActivity.this, radius).execute();

                            }
                        }
                    }
                }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetGoogleBusinessListTask extends AsyncTask<String, Void, String> {
        private Context context;
        private GetBusinessListResponse getBusinessListResponse;
        private String radius;

        GetGoogleBusinessListTask(Context context, String radius) {
            this.context = context;
            this.radius = radius;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLay.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

//            String url = Webservice.LOCATION + "=" + loclatlong + "&" + Webservice.RADUIS + "=" +
//                    radius + "&" + Webservice.TYPES + "=" + categoryname + "&" + Webservice.KEY + "=" + keys;
            String url = Webservice.LOCATION + "=" + loclatlong + "&" + Webservice.RADUIS + "=" +
                    radius + "&" + Webservice.TYPES + "=" + categoryname + "&" + Webservice.KEY + "=" + Constants.GOOGLE_API_KEY;
            String json = Webservice.callGetServicePlaces(url);
            String status = "";
            Log.e("Response send request", json);
            Log.e("Response send request", url);
            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("OK")) {
                try {
                    getBusinessListResponse = new Gson().fromJson(json, GetBusinessListResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    getBusinessListResponse = new Gson().fromJson(json, GetBusinessListResponse.class);
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
            loadingLay.setVisibility(View.GONE);
            if (resp == null) {
                if (getBusinessListResponse != null) {

                    List<BusinessDetails> business = getBusinessListResponse.results;
                    for (BusinessDetails b : business) {
                        Business buss = new Business();
                        buss.name = b.name;
                        buss.id = b.id;
                        buss.BusinessID = b.id;
                        buss.address = b.vicinity;
                        buss.latitude = b.geometry.location.lat;
                        buss.longitude = b.geometry.location.lng;
                        businessListFireStore.add(buss);
                    }
                    int currentPosition = bisness_list.getFirstVisiblePosition();
                    adapter = new BusinessListAdapter(BusinessListActivity.this, R.layout.businesslist_layout, businessListFireStore);
                    bisness_list.setAdapter(adapter);
                    bisness_list.setSelectionFromTop(currentPosition, 0);
                    apiData = false;
                    System.out.println("businessdata==" + business);


                } else {
                    apiData = false;
                }

            } else if (!Utils.isNetConnected(context)) {
//                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            } else {
                if (getBusinessListResponse != null)
                    Utils.showDialogFailure(context, resp);
                else {
//                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
                }
            }
        }
    }


    class BusinessListAdapter extends ArrayAdapter<Business> {

        List<Business> animalList;
        Context con;


        BusinessListAdapter(Context context, int textViewResourceId, List<Business> objects) {
            super(context, textViewResourceId, objects);
            animalList = objects;
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
            v = inflater.inflate(R.layout.businesslist_layout, null);
            TextView textViewBus = v.findViewById(R.id.bus_list);
            TextView textViewBus_addr = v.findViewById(R.id.bus_adres_list);
//            TextView distance = v.findViewById(R.id.distance);


            textViewBus.setText(animalList.get(position).name);
            textViewBus_addr.setText(animalList.get(position).address);
//            if (animalList.get(position).distance != 0)
//                distance.setText(String.format("%.2f", animalList.get(position).distance) + " Km");
            RelativeLayout nextScreen = v.findViewById(R.id.businessln);
            System.out.println("datacoming==" + animalList.get(position).name);
            nextScreen.setOnClickListener(v1 -> {
                Intent intent = new Intent(BusinessListActivity.this, BuyItemsActivity.class);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, animalList.get(position).name);
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, animalList.get(position).BusinessID);
                intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, animalList.get(position).address);
                intent.putExtra(Constants.IntentKey.BUSINESS, animalList.get(position));
                startActivity(intent);

            });
            return v;

        }


    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel1(
            GetBusinessesTask task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetBusinessesTask extends AsyncTask<String, Void, String> {
        private Context context;

        String status = "";
        private GetHomeImagesResponse getHomeImagesResponse;
        String json;
        String category, subCategory, searchKey;
        int offset;


        GetBusinessesTask(Context context, int offset, String category, String subCategory, String searchKey) {
            this.context = context;
            this.category = category;
            this.subCategory = subCategory;
            this.searchKey = searchKey;
            this.offset = offset;

        }


        @Override

        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... params) {
            String url = Webservice.GET_BUSINESSES + "?" + Webservice.CATEGORY + "=" + category + "&" + Webservice.SUB_CATEGORY + "=" + subCategory + "&" + Webservice.LATITUDE + "=" + mLastLocation.getLatitude() + "&" + Webservice.LONGITUDE + "=" + mLastLocation.getLongitude() + "&" + Webservice.RADIUS + "=" + 10 + "&" + Webservice.OFFSET + "=" + offset + "&" + Webservice.PAGE_COUNT + "=" + 10 + "&" + Webservice.KEYWORD + "=" + searchKey;

            json = Webservice.callGetService(url);

            Log.e(TAG, json);
            try {
                // Convert String to json object
                JSONObject object1 = new JSONObject(json);
                status = object1.getString("status");


            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getHomeImagesResponse = new Gson().fromJson(json, GetHomeImagesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {

                try {
                    getHomeImagesResponse = new Gson().fromJson(json, GetHomeImagesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
//                    JSONObject jsonObject = new JSONObject(json);
//
//                    return jsonObject.getJSONObject("data").getString("message");
                return null;


            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
//            progressDialog.dismiss();
            searchGo.setBackgroundResource(R.drawable.button_green);
            searchGo.setEnabled(true);
            if (resp == null) {
                if (getHomeImagesResponse != null) {
                    if (getHomeImagesResponse.status.equals("success")) {
                        if (getHomeImagesResponse.data.results.size() != 10) {
                            hasData = false;

                        }
                        businessListFireStore.addAll(getHomeImagesResponse.data.results);
                        int currentPosition = bisness_list.getFirstVisiblePosition();
                        adapter = new BusinessListAdapter(BusinessListActivity.this, R.layout.businesslist_layout, businessListFireStore);
                        bisness_list.setAdapter(adapter);
                        bisness_list.setSelectionFromTop(currentPosition, 0);

                        //if no data from server display api data


                    } else {
                        if (apiData && searchKey.equals("")) {
                            Log.e("api", "api data show");
                            if (!categoryname.equals("") && (maincategory.equalsIgnoreCase("Food") ||
                                    maincategory.equalsIgnoreCase("Other") ||
                                    maincategory.equalsIgnoreCase("Grocery") ||
                                    maincategory.equalsIgnoreCase("Medical Shop") ||
                                    maincategory.equalsIgnoreCase("Flower Shop") ||
                                    maincategory.equalsIgnoreCase("Meat"))) {
                                if (businessListFireStore.size() == 0)//show places data only when no data from our db
                                    new GetGoogleBusinessListTask(BusinessListActivity.this, radius).execute();
                                else apiData = false;
                            } else if (businessListFireStore.size() == 0) {
//                                noDataText.setVisibility(View.VISIBLE);
                                if (searchKey.equals(""))
                                    noDataDialog();
                                else
                                    searchNoDataDialog();
                            }
                        } else if (businessListFireStore.size() == 0) {
//                            noDataText.setVisibility(View.VISIBLE);
                            if (searchKey.equals(""))
                                noDataDialog();
                            else
                                searchNoDataDialog();
                        }
                    }
                } else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!", offset, category, subCategory, searchKey);
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal", offset, category, subCategory, searchKey);
            else {

                showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!", offset, category, subCategory, searchKey);
            }
        }

    }

    void showDialog1(final Context context, String title, String message, int offset, String category, String subCategory, String searchKey) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            businessesTask = new GetBusinessesTask(BusinessListActivity.this, offset, category, subCategory, searchKey);
            StartAsyncTaskInParallel1(businessesTask);
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public void noDataDialog() {
        if (dialog != null && dialog.isShowing() && !BusinessListActivity.this.isFinishing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(BusinessListActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCanceledOnTouchOutside(false);
            TextView title = dialog.findViewById(R.id.text);
            TextView heading = dialog.findViewById(R.id.heading);
            heading.setText(getApplicationContext().getResources().getString(R.string.list_of_businesses));
            heading.setVisibility(View.VISIBLE);
            title.setText(getApplicationContext().getResources().getString(R.string.in_mean_time));
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(BusinessListActivity.this, BuyItemsActivity.class);
                intent.putExtra(Constants.IntentKey.KEY_BUY_HINT, "Enter Items");
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, "");
                intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, "");
                intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent);
                finish();
            });
            if (dialog != null && !BusinessListActivity.this.isFinishing())
                dialog.show();
        }
    }

    public void searchNoDataDialog() {
        if (dialog != null && dialog.isShowing() && !BusinessListActivity.this.isFinishing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(BusinessListActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(getApplicationContext().getResources().getString(R.string.sorry_no_businesses));
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText(getApplicationContext().getResources().getString(R.string.ok));
            ok.setOnClickListener(v -> dialog.dismiss());
            if (!BusinessListActivity.this.isFinishing())
                dialog.show();
        }
    }

    // new location services stuff
    private void locationServiceStuff() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (mLastLocation == null)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        else
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("BusinessListActivity", location.getLatitude() + " " + location.getLongitude());

                    mLastLocation = location;
//                    mLastLocation.setLatitude(17.4483);
//                    mLastLocation.setLongitude(78.3915);
                    if (!initViewShow)
                        initView();
                }
            }


        };

    }

    private void startNewLocationUpdates() {
        if (started)
            return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.e("started", "start loc");
        started = true;
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopNewLocationUpdates() {
        started = false;
        Log.e("stopped", "stop loc");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
