package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.SavedAddress;

import java.util.List;

/**
 * Created by SPRV on 9/24/2015.
 */
public class SavedAddressesAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<SavedAddress> filteredData;
    private Context context;

    private class MyViewHolder {
        TextView addressTv, label;



        MyViewHolder(View item) {
            addressTv = item.findViewById(R.id.address);
            label = item.findViewById(R.id.label);
        }
    }

    public SavedAddressesAdapter(Context context, List<SavedAddress> filteredData) {
        inflater = LayoutInflater.from(context);
        this.filteredData = filteredData;
        this.context = context;
    }


    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_address, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final SavedAddress address = (SavedAddress) getItem(position);


        mViewHolder.label.setText(address.Label);
        String adressText=address.Address;
        if(!address.Landmark.equals(""))
            adressText=adressText+"\n"+"Landmark: "+address.Landmark;
        if(!address.ContactName.equals(""))
            adressText=adressText+"\n"+address.ContactName;
        if(!address.ContactNumber.equals(""))
            adressText=adressText+", "+address.ContactNumber;
        mViewHolder.addressTv.setText(adressText);

//
//        mViewHolder.lay.setOnClickListener(view -> {
//
//            Intent in = new Intent(context, ScheduleTripDetailsActivity.class);
//            in.putExtra(Constants.IntentKey.KEY_ORDER_ID, order.order_id);
//            context.startActivity(in);
//        });

        return convertView;
    }

}

