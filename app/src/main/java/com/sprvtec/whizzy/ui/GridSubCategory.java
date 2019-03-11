package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.Business;
import com.sprvtec.whizzy.vo.GridCategory;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class GridSubCategory extends Activity {
    private GridView categoriesGrid;
    private ArrayList<GridCategory> categories = new ArrayList<>();
    ImageView back;
    Button goBuyScreen;
    TextView cnt, title;
    //    private PreferenceUtils preferenceUtils;
    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridsubcate);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
//        preferenceUtils = new PreferenceUtils(getApplicationContext());
        categoriesGrid = findViewById(R.id.category_grid);
        title = findViewById(R.id.title_edit);
        goBuyScreen = findViewById(R.id.canttext);
        cnt = findViewById(R.id.counting);
        findViewById(R.id.search).setOnClickListener(view -> {
            Intent intent1 = new Intent(GridSubCategory.this, BusinessListActivity.class);
            intent1.putExtra(Constants.IntentKey.FROM_SEARCH, true);
            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent1);
        });
        findViewById(R.id.top_lay).setOnClickListener(v -> {
//            int val = Integer.parseInt(preferenceUtils.getStringFromPreference(PreferenceUtils.COUNTVALUE, ""));
//            String placid = preferenceUtils.getStringFromPreference(PreferenceUtils.PLACEID, "");
//            String address, placelat, placelong, placename;
//            address = preferenceUtils.getStringFromPreference(PreferenceUtils.PLACEADDR, "");
//            placelat = preferenceUtils.getStringFromPreference(PreferenceUtils.PLACELAT, "0");
//            placelong = preferenceUtils.getStringFromPreference(PreferenceUtils.PLACELONG, "0");
//            placename = preferenceUtils.getStringFromPreference(PreferenceUtils.PLACENAME, "");
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
                Intent intent = new Intent(GridSubCategory.this, BuyItemsActivity.class);
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


        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        if (getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM).equalsIgnoreCase("Food")) {
            accessCate();
            title.setText("Food");
        } else if (getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM).equalsIgnoreCase("Other")) {
            accessCateOthers();
            title.setText("Other");
        }
        goBuyScreen.setOnClickListener(v -> {
//                if (!preferenceUtils.getStringFromPreference(PreferenceUtils.COUNTVALUE, "0").equals("0"))
//                    showDialogCount();
//                else {
//                    preferenceUtils.saveString(PreferenceUtils.COUNTVALUE, "0");
            Intent intent = new Intent(GridSubCategory.this, BuyItemsActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_BUY_HINT, "Enter Items");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, "");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, "");
            intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
//                    preferenceUtils.saveString(PreferenceUtils.LISTDATA, "");
            startActivity(intent);
//                }
        });
        thread.start();
    }


//    private void accessCate1() {
//
//        categories.add(new GridCategory("Restaurants", R.drawable.reastarant));
//        categories.add(new GridCategory("Rolls/Sandwiches", R.drawable.sandwich));
//        categories.add(new GridCategory("Snacks/Street Food", R.drawable.snacks));
//
//        categories.add(new GridCategory("Bakery", R.drawable.bakery));
//        categories.add(new GridCategory("Sweets", R.drawable.sweets));
//        categories.add(new GridCategory("Ice Creams", R.drawable.ice_cream));
//
//        categories.add(new GridCategory("Juices", R.drawable.juices));
//        categories.add(new GridCategory("Coffee/Tea", R.drawable.coffee_tea));
//        categories.add(new GridCategory("Paan Shops", R.drawable.pannshop));
//
//        categories.add(new GridCategory("Fruits/Vegetables", R.drawable.fruits));
//        categories.add(new GridCategory("Dairy", R.drawable.dairy));
//        categories.add(new GridCategory("Take Away", R.drawable.takeaway));
//
//        CatGridAdapter adapter1 = new CatGridAdapter(GridSubCategory.this, categories);
//        categoriesGrid.setAdapter(adapter1);
//        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
//            Log.e("value", parent.getItemAtPosition(position) + "");
//            GridCategory category = (GridCategory) (parent.getItemAtPosition(position));
//
//            Intent intent1 = new Intent(GridSubCategory.this, BusinessListActivity.class);
//            intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
//            intent1.putExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM, category.name);
//            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
//            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
//            startActivity(intent1);
//
//
//        });
//
//    }
    private void accessCate() {

        categories.add(new GridCategory("Restaurants", R.drawable.reastarant,"Food", "Restaurant", "restaurant"));
        categories.add(new GridCategory("Rolls/Sandwiches", R.drawable.sandwich,"Food","Franky/Sandwiches",""));
        categories.add(new GridCategory("Snacks/Street Food", R.drawable.snacks,"Food","Snacks/Streetfood",""));

        categories.add(new GridCategory("Bakery", R.drawable.bakery,"Food","Bakery","bakery"));
        categories.add(new GridCategory("Sweets", R.drawable.sweets,"Food","Sweets",""));
        categories.add(new GridCategory("Ice Creams", R.drawable.ice_cream,"Food","Ice Creams",""));

        categories.add(new GridCategory("Juices", R.drawable.juices,"Food","Juices",""));
        categories.add(new GridCategory("Coffee/Tea", R.drawable.coffee_tea,"Food","Coffee/Tea","cafe"));
        categories.add(new GridCategory("Paan Shops", R.drawable.pannshop,"Food","Paan Shop",""));

        categories.add(new GridCategory("Fruits/Vegetables", R.drawable.fruits,"Food","Fruits/Vegetables",""));
        categories.add(new GridCategory("Dairy", R.drawable.dairy,"Food","Dairy",""));
        categories.add(new GridCategory("Take Away", R.drawable.takeaway,"Food","Curry Point",""));

        CatGridAdapter adapter1 = new CatGridAdapter(GridSubCategory.this, categories);
        categoriesGrid.setAdapter(adapter1);
        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
            Log.e("value", parent.getItemAtPosition(position) + "");
            GridCategory category = (GridCategory) (parent.getItemAtPosition(position));

            Intent intent1 = new Intent(GridSubCategory.this, BusinessListActivity.class);
            intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
            intent1.putExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM, category.name);
            intent1.putExtra(Constants.IntentKey.KEY_BUSINESS_CATEGORY, category);
            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent1);


        });

    }

//    private void accessCateOthers1() {
//
//
//        categories.add(new GridCategory("Gift Shops", R.drawable.gift));
//        categories.add(new GridCategory("Stationery Stores", R.drawable.stationery));
//        categories.add(new GridCategory("Toy Stores", R.drawable.toy_store));
//        categories.add(new GridCategory("Xerox Shops", R.drawable.xerox));
//        categories.add(new GridCategory("Electrical Stores", R.drawable.electrical));
//        categories.add(new GridCategory("Nursery/Plants", R.drawable.nursery));
//        categories.add(new GridCategory("Dry Cleaning", R.drawable.iron));
//        categories.add(new GridCategory("Petrol Pumps", R.drawable.gas));
//        categories.add(new GridCategory("Pet Supplies", R.drawable.pet_supplies));
//
//        CatGridAdapter adapter1 = new CatGridAdapter(GridSubCategory.this, categories);
//        categoriesGrid.setAdapter(adapter1);
//        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
//            Log.e("value", parent.getItemAtPosition(position) + "");
//            GridCategory category = (GridCategory) (parent.getItemAtPosition(position));
//
//            Intent intent1 = new Intent(GridSubCategory.this, BusinessListActivity.class);
//            intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
//            intent1.putExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM, category.name);
//            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
//            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
//            startActivity(intent1);
//        });
//
//    }

    private void accessCateOthers() {


        categories.add(new GridCategory("Gift Shops", R.drawable.gift,"Other","Gift Shop",""));
        categories.add(new GridCategory("Stationery Stores", R.drawable.stationery,"Other","Stationery Store",""));
        categories.add(new GridCategory("Toy Stores", R.drawable.toy_store,"Other","Toy Store",""));
        categories.add(new GridCategory("Xerox Shops", R.drawable.xerox,"Other","Xeroz Shop",""));
        categories.add(new GridCategory("Electrical Stores", R.drawable.electrical,"Other","Electrical Store","electronics_store"));
        categories.add(new GridCategory("Nursery/Plants", R.drawable.nursery,"Other","Nursery/Plants",""));
        categories.add(new GridCategory("Dry Cleaning", R.drawable.iron,"Other","Laundry/Ironing","laundry"));
        categories.add(new GridCategory("Petrol Pumps", R.drawable.gas,"Other","petrol pumps","gas_station"));
        categories.add(new GridCategory("Pet Supplies", R.drawable.pet_supplies,"Other","Pet Supplies","pet_store"));

        CatGridAdapter adapter1 = new CatGridAdapter(GridSubCategory.this, categories);
        categoriesGrid.setAdapter(adapter1);
        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
            Log.e("value", parent.getItemAtPosition(position) + "");
            GridCategory category = (GridCategory) (parent.getItemAtPosition(position));

            Intent intent1 = new Intent(GridSubCategory.this, BusinessListActivity.class);
            intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, getIntent().getStringExtra(Constants.IntentKey.KEY_CATEGORY_ITEM));
            intent1.putExtra(Constants.IntentKey.KEY_SUB_CATEGORY_ITEM, category.name);
            intent1.putExtra(Constants.IntentKey.KEY_BUSINESS_CATEGORY, category);
            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent1);
        });

    }
    public class CatGridAdapter extends BaseAdapter {

        private ArrayList<GridCategory> mDisplayedValues;    // Values to be displayed
        LayoutInflater inflater;

        CatGridAdapter(Context context, ArrayList<GridCategory> mProductArrayList) {
            this.mDisplayedValues = mProductArrayList;
            this.mDisplayedValues = mProductArrayList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mDisplayedValues.size();
        }

        @Override
        public Object getItem(int position) {
            return mDisplayedValues.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            //            LinearLayout llContainer;
            TextView catName;
            ImageView image;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_grid, null);
//                holder.llContainer = (LinearLayout) convertView.findViewById(R.id.Container);
                holder.catName = convertView.findViewById(R.id.name);
                holder.image = convertView.findViewById(R.id.imae);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.catName.setText(mDisplayedValues.get(position).name);
            holder.image.setImageResource(mDisplayedValues.get(position).image);


            return convertView;
        }


    }

    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(() -> {

//                        if (!preferenceUtils.getStringFromPreference(PreferenceUtils.COUNTVALUE, "").equals("")) {
//                            cnt.setText(preferenceUtils.getStringFromPreference(PreferenceUtils.COUNTVALUE, ""));
//
//                        }
                        if (!sp.getString(Constants.PreferenceKey.COUNTVALUE, "").equals("")) {
                            cnt.setText(sp.getString(Constants.PreferenceKey.COUNTVALUE, ""));

                        }

                    });
                }
            } catch (InterruptedException ignored) {
            }
        }
    };
}
