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

public class CategoryGridActivity extends Activity {
    private GridView categoriesGrid;
    private ArrayList<GridCategory> categories = new ArrayList<>();
    private TextView cnt;
    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_grid);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        cnt = findViewById(R.id.counting);
        findViewById(R.id.search).setOnClickListener(view -> {
            Intent intent1 = new Intent(CategoryGridActivity.this, BusinessListActivity.class);
            intent1.putExtra(Constants.IntentKey.FROM_SEARCH, true);
            intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent1);
        });
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
                Intent intent = new Intent(CategoryGridActivity.this, BuyItemsActivity.class);
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
        categoriesGrid = findViewById(R.id.category_grid);
        accessCate();
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.canttext).setOnClickListener(v -> {
            Intent intent = new Intent(CategoryGridActivity.this, BuyItemsActivity.class);
            intent.putExtra(Constants.IntentKey.KEY_BUY_HINT, "Enter Items");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_ID, "");
            intent.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, "");

            intent.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
            intent.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
            startActivity(intent);

        });
        thread.start();
    }



    private void accessCate() {
        categories.add(new GridCategory("Food", R.drawable.food, "Food", "", ""));
        categories.add(new GridCategory("Restaurants", R.drawable.reastarant, "Food", "Restaurant", "restaurant"));
        categories.add(new GridCategory("Grocery", R.drawable.grocery, "Grocery", "", ""));

        categories.add(new GridCategory("Bakery", R.drawable.bakery, "Food", "Bakery", "bakery"));
        categories.add(new GridCategory("Sweets", R.drawable.sweets, "Food", "Sweets", ""));
        categories.add(new GridCategory("Meat", R.drawable.meat, "Food", "Meat Shop", ""));

        categories.add(new GridCategory("Medicines", R.drawable.medicine, "Medical Shop", "", "pharmacy"));
        categories.add(new GridCategory("Flowers", R.drawable.flowers1, "Flower Shop", "", "florist"));
        categories.add(new GridCategory("Other", R.drawable.others, "Other", "", ""));

        CatGridAdapter adapter1 = new CatGridAdapter(CategoryGridActivity.this, categories);
        categoriesGrid.setAdapter(adapter1);
        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
            Log.e("value", parent.getItemAtPosition(position) + "");
            GridCategory category = (GridCategory) (parent.getItemAtPosition(position));
            if (category.name.equalsIgnoreCase("Food") || category.name.equalsIgnoreCase("Other")) {
                Intent intent1 = new Intent(CategoryGridActivity.this, GridSubCategory.class);
                intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, category.name);
                intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent1);
            } else {
                Intent intent1 = new Intent(CategoryGridActivity.this, BusinessListActivity.class);
                intent1.putExtra(Constants.IntentKey.KEY_CATEGORY_ITEM, category.name);
                intent1.putExtra(Constants.IntentKey.KEY_BUSINESS_CATEGORY, category);
                intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                startActivity(intent1);
            }


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
            TextView catName;
            ImageView image;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_grid, null);
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
