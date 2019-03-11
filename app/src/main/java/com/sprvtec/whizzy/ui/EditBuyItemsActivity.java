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
import com.sprvtec.whizzy.vo.PurchaseProduct;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Sowjanya on 5/3/2018.
 */

public class EditBuyItemsActivity extends Activity {
    private ListView list;
    private EditText product;
    String myvale;
    private SharedPreferences sp;
    TextView title_txt, proc;
    private Dialog dialog;
    private List<PurchaseProduct> products;
    private TextView cnt;
    private String count_value = "", localt_count = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_products);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        myvale = sp.getString(Constants.PreferenceKey.EDITITEM, "");
        list = findViewById(R.id.listview);
        product = findViewById(R.id.item);
        title_txt = findViewById(R.id.title_edit1);
        proc = findViewById(R.id.proceed);


        TextView title_edt = findViewById(R.id.title_edit);
        TextView title_addr = findViewById(R.id.title_addr);
        LinearLayout business_lay = findViewById(R.id.bus_lay);
        cnt = findViewById(R.id.counting);

        System.out.println("palceid==" + sp.getString(Constants.PreferenceKey.PLACEID, "") + "inteid==" + getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID));
        if (sp.getString(Constants.PreferenceKey.PLACEID, "").equals(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID))) {
            loadData();
        }
        if (getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME) != null && !getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME).equals("")) {
            business_lay.setVisibility(View.VISIBLE);
            title_edt.setText(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_NAME));
            title_addr.setText(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ADDR));
        }


        products = new ArrayList<>();
        products = getIntent().getParcelableArrayListExtra(Constants.IntentKey.BUY_ITEMS);
        PurchaseProductsAdapter adapter = new PurchaseProductsAdapter(EditBuyItemsActivity.this);
        list.setAdapter(adapter);
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
                    imm.hideSoftInputFromWindow(product.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });


        if (myvale.equalsIgnoreCase("edit")) {

            proc.setText(getApplicationContext().getResources().getString(R.string.done));
            title_txt.setText(getApplicationContext().getResources().getString(R.string.add_edit));

        }
        thread.start();

        findViewById(R.id.back).setOnClickListener(v -> {

            BuyItemsActivity.buyItems = products;
            Gson gson = new Gson();
            String json = gson.toJson(products);
            sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();
        });
        proc.setOnClickListener(v -> {
            if (products.size() == 0) {
                Utils utils = new Utils();
                utils.showDialog("Please add items to proceed", EditBuyItemsActivity.this);
            } else {
                BuyItemsActivity.buyItems = products;
                Gson gson = new Gson();
                String json = gson.toJson(products);
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        findViewById(R.id.add).setOnClickListener(v -> {
            if (sp.getString(Constants.PreferenceKey.PLACEID, "").equals(getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID))) {
                addItme();
            } else {
                if (localt_count.equalsIgnoreCase("0")) {
                    if (!product.getText().toString().trim().equals("")) {
                        PurchaseProduct item = new PurchaseProduct();
                        item.title = product.getText().toString().trim();
                        item.count = 1;
                        products.add(item);
                        product.setText("");
                        PurchaseProductsAdapter adapter1 = new PurchaseProductsAdapter(EditBuyItemsActivity.this);
                        list.setAdapter(adapter1);


                        list.getAdapter().getCount();
                        count_value = String.valueOf(list.getAdapter().getCount());
                        cnt.setText(count_value);
                        sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();
                        sp.edit().putString(Constants.PreferenceKey.PLACEID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();
                        System.out.println("count" + list.getAdapter().getCount() + "stringcount" + count_value);

                    } else {
                        Utils utils = new Utils();
                        utils.showDialog("Please enter item to add", EditBuyItemsActivity.this);

                    }
                } else {
                    showDialogCount();
                }
            }

        });

    }

    private void addItme() {
        if (!product.getText().toString().trim().equals("")) {
            PurchaseProduct item = new PurchaseProduct();
            item.title = product.getText().toString().trim();
            item.count = 1;
            products.add(item);
            product.setText("");
            PurchaseProductsAdapter adapter = new PurchaseProductsAdapter(EditBuyItemsActivity.this);
            list.setAdapter(adapter);


            list.getAdapter().getCount();
            count_value = String.valueOf(list.getAdapter().getCount());
            cnt.setText(count_value);
            sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();
            sp.edit().putString(Constants.PreferenceKey.PLACEID, getIntent().getStringExtra(Constants.IntentKey.PLACES_ITEM_ID)).apply();
            System.out.println("count" + list.getAdapter().getCount() + "stringcount" + count_value);

        } else {
            Utils utils = new Utils();
            utils.showDialog("Please enter item to add", EditBuyItemsActivity.this);

        }
    }

    private void loadData() {
        Gson gson = new Gson();
        String json = sp.getString(Constants.PreferenceKey.LISTDATA, "");
        if (!json.equals("")) {
            Type type = new TypeToken<ArrayList<PurchaseProduct>>() {
            }.getType();

            products = gson.fromJson(json, type);
        }
        if (products == null) {
            products = new ArrayList<>();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        BuyItemsActivity.buyItems = products;
        Gson gson = new Gson();
        String json = gson.toJson(products);
        sp.edit().putString(Constants.PreferenceKey.LISTDATA, json).apply();
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
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
            return products.size();
        }

        @Override
        public Object getItem(int position) {
            return products.get(position);
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
                        products.remove(product);
                        notifyDataSetChanged();
                    }
                } else showDialog(product);

            });
            return convertView;
        }

        void showDialog(final PurchaseProduct product) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            } else {
                dialog = new Dialog(EditBuyItemsActivity.this);

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

                ok.setOnClickListener(v -> {
                    dialog.dismiss();

                    products.remove(product);
                    list.getAdapter().getCount();
                    System.out.println("count" + list.getAdapter().getCount());
                    count_value = String.valueOf(list.getAdapter().getCount());
                    cnt.setText(count_value);
                    sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, count_value).apply();

                    notifyDataSetChanged();

                });
                if (!EditBuyItemsActivity.this.isFinishing())
                    dialog.show();
            }
        }
    }

    public void showDialogCount() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(EditBuyItemsActivity.this);

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
//                ok.setText("OK");

            ok.setOnClickListener(v -> {
                dialog.dismiss();
                cnt.setText("0");
                localt_count = "0";
                sp.edit().putString(Constants.PreferenceKey.COUNTVALUE, "0").apply();
                sp.edit().putString(Constants.PreferenceKey.LISTDATA, "").apply();
                products.clear();
                thread.start();
                //Add Fucnionality agains

                addItme();
            });
            if (!EditBuyItemsActivity.this.isFinishing())
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
