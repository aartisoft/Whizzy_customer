package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.vo.Business;
import com.sprvtec.whizzy.vo.PurchaseProduct;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 5/3/2018.
 */

public class BuyItemsActivity extends Activity {
    private ListView list;
    private EditText product;
    public static List<PurchaseProduct> buyItems = new ArrayList<>();
    Dialog dialog1;
    private Dialog dialog;
    private TextView cnt;
    private String count_value = "", localt_count = "";
    //    private PreferenceUtils preferenceUtils;
    private SharedPreferences sp;
    private Business business;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_products);

//        preferenceUtils = new PreferenceUtils(getApplicationContext());
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        buyItems = new ArrayList<>();
        list = findViewById(R.id.listview);
        product = findViewById(R.id.item);
        TextView title_edt = findViewById(R.id.title_edit);
        TextView title_addr = findViewById(R.id.title_addr);
        LinearLayout business_lay = findViewById(R.id.bus_lay);
        TextView continueShopping = findViewById(R.id.continue_shopping);
        cnt = findViewById(R.id.counting);
        business = getIntent().getParcelableExtra(Constants.IntentKey.BUSINESS);
        thread.start();
//        System.out.println("palceid==" + preferenceUtils.getStringFromPreference(PreferenceUtils.PLACEID, "") + "inteid==" + getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
        System.out.println("palceid==" + sp.getString(Constants.PreferenceKey.PLACEID, "") + "inteid==" + getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
        if (sp.getString(Constants.PreferenceKey.PLACEID, "").equals(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID))) {
            loadData();
        }
//        if (preferenceUtils.getStringFromPreference(PreferenceUtils.PLACEID, "").equals(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID))) {
//            loadData();
//        }

        if (business != null && !business.name.equals("")) {
            business_lay.setVisibility(View.VISIBLE);
            title_edt.setText(business.name);
            title_addr.setText(business.address);
        }
        if (getIntent().getBooleanExtra(Constants.IntentKey.FROM_CART, false)) {
            continueShopping.setVisibility(View.VISIBLE);
            continueShopping.setOnClickListener(view -> finish());
        }


        product.setHint(getIntent().getStringExtra(Constants.IntentKey.KEY_BUY_HINT));
        product.setOnKeyListener(new View.OnKeyListener() {
            /**
             * This listens for the user to press the enter button on
             * the keyboard and then hides the virtual keyboard
             */
            public boolean onKey(View arg0, int arg1, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (arg1 == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(product.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.proceed).setOnClickListener(v -> {
            if (buyItems.size() == 0) {
                Utils utils = new Utils();
                utils.showDialog("Please add items to proceed", BuyItemsActivity.this);
            } else {
                StringBuilder items = new StringBuilder();
                for (PurchaseProduct item : buyItems) {
                    items.append(item.title).append("-").append(item.count).append("   ");
                }
//                Intent intent1 = new Intent(BuyItemsActivity.this, MapDragActivity.class);
//                intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
//                intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
//                intent1.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
//                intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
//                intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
//                if (business != null)
//                    intent1.putExtra(Constants.IntentKey.BUSINESS, business);
//                startActivity(intent1);
                if (business != null && !business.name.equals("")) {
                    Intent intent1 = new Intent(BuyItemsActivity.this, MapDragDropoffActivity.class);
//                    intent1.putExtra(Constants.IntentKey.KEY_BUY_LOCATION, specific);
                    intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                    intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                    if (business != null)
                        intent1.putExtra(Constants.IntentKey.BUSINESS, business);
                    startActivity(intent1);
                } else {
                    Intent intent1 = new Intent(BuyItemsActivity.this, MapDragPickupActivity.class);
//                    intent1.putExtra(Constants.IntentKey.KEY_BUY_LOCATION, specific);
                    intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, getIntent().getBooleanExtra(Constants.IntentKey.SCHEDULE_ORDER, false));
                    intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, getIntent().getStringExtra(Constants.IntentKey.SCHEDULE_TIME));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_NAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
                    intent1.putExtra(Constants.IntentKey.PLACES_ITEM_ID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
                    if (business != null)
                        intent1.putExtra(Constants.IntentKey.BUSINESS, business);
                    startActivity(intent1);
                }
            }
        });
        localt_count = sp.getString(Constants.PreferenceKey.COUNTVALUE, "");
        findViewById(R.id.add).setOnClickListener(v -> {

            System.out.println("newvlaue" + localt_count);


            if (sp.getString(Constants.PreferenceKey.PLACEID, "").equals(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID))) {
                addItme();
            } else {
                if (localt_count.equalsIgnoreCase("0")) {
                    if (!product.getText().toString().trim().equals("")) {
                        PurchaseProduct item = new PurchaseProduct();
                        item.title = product.getText().toString().trim();
                        item.count = 1;
                        buyItems.add(item);
                        product.setText("");
                        PurchaseProductsAdapter adapter = new PurchaseProductsAdapter(BuyItemsActivity.this);
                        list.setAdapter(adapter);

                        Gson gson = new Gson();
                        String json = gson.toJson(buyItems);
                        sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();

                        list.getAdapter().getCount();
                        count_value = String.valueOf(list.getAdapter().getCount());
                        cnt.setText(count_value);
                        sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();
                        sp.edit().putString(Constants.PreferenceKey.PLACEID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();
                        sp.edit().putString(Constants.PreferenceKey.PLACEID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();
                        sp.edit().putString(Constants.PreferenceKey.PLACENAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME)).apply();
                        sp.edit().putString(Constants.PreferenceKey.PLACEADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR)).apply();
                        if (business != null) {
                            sp.edit().putString(Constants.PreferenceKey.PLACELAT, business.latitude + "").apply();
                            sp.edit().putString(Constants.PreferenceKey.PLACELONG, business.longitude + "").apply();
                        }

                        System.out.println("count" + list.getAdapter().getCount() + "stringcount" + count_value);
                    } else {
                        Utils utils = new Utils();
                        utils.showDialog("Please enter an item to add", BuyItemsActivity.this);

                    }
                } else {
                    if (!product.getText().toString().trim().equals("")) {
                        showDialogCount();
                    } else {
                        Utils utils = new Utils();
                        utils.showDialog("Please enter an item to add", BuyItemsActivity.this);

                    }
                }
            }

        });

        //cnt.setText(preferenceUtils.getStringFromPreference(PreferenceUtils.COUNTVALUE, ""));


        System.out.println("counting value" + localt_count);
        if (cnt.getText().toString().equalsIgnoreCase("0")) {
            cnt.setText(localt_count);
        }
    }

    private void addItme() {
        if (!product.getText().toString().trim().equals("")) {
            PurchaseProduct item = new PurchaseProduct();
            item.title = product.getText().toString().trim();
            item.count = 1;
            buyItems.add(item);
            product.setText("");
            PurchaseProductsAdapter adapter = new PurchaseProductsAdapter(BuyItemsActivity.this);
            list.setAdapter(adapter);

            Gson gson = new Gson();
            String json = gson.toJson(buyItems);
            sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();

            list.getAdapter().getCount();
            count_value = String.valueOf(list.getAdapter().getCount());
            cnt.setText(count_value);
            sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();
            sp.edit().putString(Constants.PreferenceKey.PLACEID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();
            sp.edit().putString(Constants.PreferenceKey.PLACENAME, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME)).apply();
            sp.edit().putString(Constants.PreferenceKey.PLACEADDR, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR)).apply();
            if (business != null) {
                sp.edit().putString(Constants.PreferenceKey.PLACELAT, business.latitude + "").apply();
                sp.edit().putString(Constants.PreferenceKey.PLACELONG, business.longitude + "").apply();
            }
            System.out.println("count" + list.getAdapter().getCount() + "stringcount" + count_value);
        } else {
            Utils utils = new Utils();
            utils.showDialog("Please enter an item to add", BuyItemsActivity.this);

        }
    }

    private void loadData() {
        Gson gson = new Gson();
        String json = sp.getString(Constants.PreferenceKey.LISTDATA, "");
        if (!json.equals("")) {
            Type type = new TypeToken<ArrayList<PurchaseProduct>>() {
            }.getType();

            buyItems = gson.fromJson(json, type);
        }
        if (buyItems == null) {
            buyItems = new ArrayList<>();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (buyItems != null) {
            PurchaseProductsAdapter adapter = new PurchaseProductsAdapter(BuyItemsActivity.this);
            list.setAdapter(adapter);

        }
    }

    public class PurchaseProductsAdapter extends BaseAdapter {
        LayoutInflater inflater;


        private class MyViewHolder {
            TextView title, count;
            ImageView plus, minus;

            MyViewHolder(View item) {
                title = item.findViewById(R.id.title);
                count = item.findViewById(R.id.count);
                plus = item.findViewById(R.id.plus);
                minus = item.findViewById(R.id.minus);


            }
        }

        PurchaseProductsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return buyItems.size();
        }

        @Override
        public Object getItem(int position) {
            return buyItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final MyViewHolder mViewHolder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_row, parent, false);
                mViewHolder = new MyViewHolder(convertView);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            final PurchaseProduct product = (PurchaseProduct) getItem(position);

            mViewHolder.title.setText(product.title);

            mViewHolder.count.setText(String.valueOf(product.count));
            mViewHolder.minus.setVisibility(View.VISIBLE);
            mViewHolder.plus.setVisibility(View.VISIBLE);
            mViewHolder.plus.setOnClickListener(v -> {
                product.count++;
                mViewHolder.count.setText(String.valueOf(product.count));

            });
            mViewHolder.minus.setOnClickListener(v -> {
                if (product.count > 1) {
                    product.count--;
                    mViewHolder.count.setText(String.valueOf(product.count));

                    if (product.count == 0) {
                        buyItems.remove(product);
                        notifyDataSetChanged();
                    }
                } else {
                    showDialog(product);
                }
            });
            return convertView;
        }

        void showDialog(final PurchaseProduct product) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            } else {
                dialog = new Dialog(BuyItemsActivity.this);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_delete_all);
                dialog.setCanceledOnTouchOutside(true);
                TextView title = dialog.findViewById(R.id.title);

                title.setText(getApplicationContext().getResources().getString(R.string.do_you_really));
                Button ok = dialog.findViewById(R.id.yes);
                Button no = dialog.findViewById(R.id.no);
                no.setText(getApplicationContext().getResources().getString(R.string.cancel));
                no.setOnClickListener(v -> dialog.dismiss());
//                ok.setText("OK");

                ok.setOnClickListener(v -> {
                    dialog.dismiss();
                    buyItems.remove(product);
                    list.getAdapter().getCount();
                    System.out.println("count" + list.getAdapter().getCount());
                    count_value = String.valueOf(list.getAdapter().getCount());
                    cnt.setText(count_value);
                    sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();

                    Gson gson = new Gson();
                    String json = gson.toJson(buyItems);
                    sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();

                    notifyDataSetChanged();
                });

                dialog.show();
            }
        }


    }

    public void showDialogCount() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(BuyItemsActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_delete_all);
            dialog.setCanceledOnTouchOutside(true);
            TextView title = dialog.findViewById(R.id.title);

            title.setText(getApplicationContext().getResources().getString(R.string.you_have_items));
            Button ok = dialog.findViewById(R.id.yes);
            Button no = dialog.findViewById(R.id.no);
            no.setText(getApplicationContext().getResources().getString(R.string.no));
            no.setOnClickListener(v -> dialog.dismiss());

            ok.setOnClickListener(v -> {
                dialog.dismiss();
                cnt.setText("0");
                localt_count = "0";
                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                buyItems.clear();
                try {
                    thread.start();
                } catch (IllegalThreadStateException e) {
                    e.printStackTrace();
                }
                //Add Fucnionality agains

                addItme();
            });

            dialog.show();
        }
    }

    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(() -> cnt.setText(sp.getString(Constants.PreferenceKey.COUNTVALUE, "")));
                }
            } catch (InterruptedException ignored) {
            }
        }
    };

}
