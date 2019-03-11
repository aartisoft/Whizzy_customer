package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.DropContact;

import java.util.List;

/**
 * Created by SPRV on 9/24/2015.
 */
public class DropContactsAdapter extends BaseAdapter {
    LayoutInflater inflater;
    private List<DropContact> filteredData;

    private class MyViewHolder {
        TextView text, landmark;
        ImageView image;

        public MyViewHolder(View item) {
            text = item.findViewById(R.id.text);
            landmark = item.findViewById(R.id.landmark);
            image = item.findViewById(R.id.check);
        }
    }

    public DropContactsAdapter(Context context, List<DropContact> filteredData) {
        inflater = LayoutInflater.from(context);
        this.filteredData = filteredData;
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
            convertView = inflater.inflate(R.layout.lay_drop_contact, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final DropContact contact = (DropContact) getItem(position);
        String nameNumber=contact.drop_contact_name + ", " + contact.drop_contact_number;
        mViewHolder.text.setText(nameNumber);
        mViewHolder.landmark.setText(contact.drop_contact_more_info);
        Log.e("signature", contact.receiver_signature + "");
        if (!contact.receiver_name.equals(""))
            mViewHolder.image.setImageResource(R.drawable.ic_check_green);
        else mViewHolder.image.setImageResource(R.drawable.ic_check_gray);
        return convertView;
    }

}

