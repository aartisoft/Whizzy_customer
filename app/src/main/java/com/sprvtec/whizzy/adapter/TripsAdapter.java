package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.ui.TripDetailsActivity;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.Order;

import java.util.List;

/**
 * Created by SPRV on 9/24/2015.
 */
public class TripsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Order> filteredData;
    private Context context;

    private class MyViewHolder {
        TextView date, toLocation, fromLocation, fare;

        RelativeLayout lay;


        MyViewHolder(View item) {
            date = item.findViewById(R.id.time);
            toLocation = item.findViewById(R.id.to_location);
            fromLocation = item.findViewById(R.id.from_location);
            fare = item.findViewById(R.id.fare);
            lay = item.findViewById(R.id.lay);
        }
    }

    public TripsAdapter(Context context, List<Order> filteredData) {
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
            convertView = inflater.inflate(R.layout.list_item_orders, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final Order order = (Order) getItem(position);


        mViewHolder.date.setText(order.created_time_ist);
        String pickString = order.pickup_full_address.trim();
        mViewHolder.fromLocation.setText(pickString);
        String dropString = order.drop_full_address.trim();
        mViewHolder.toLocation.setText(dropString);
        if (!order.order_fare.equals("N / A")) {
            String fareString = "₹" + order.order_fare;
            mViewHolder.fare.setText(fareString);
        } else
            mViewHolder.fare.setText("");

        mViewHolder.lay.setOnClickListener(view -> {

            Intent in = new Intent(context, TripDetailsActivity.class);
            in.putExtra(Constants.IntentKey.KEY_ORDER_ID, order.order_id);
            context.startActivity(in);
        });

        return convertView;
    }

}

