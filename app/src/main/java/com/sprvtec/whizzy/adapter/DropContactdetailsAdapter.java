package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.DropContact;

import java.util.List;

/**
 * Created by SPRV on 9/24/2015.
 */
public class DropContactdetailsAdapter extends BaseAdapter {
    LayoutInflater inflater;
    private List<DropContact> filteredData;

    private class MyViewHolder {
        TextView text;

        public MyViewHolder(View item) {
            text = item.findViewById(R.id.name);

        }
    }

    public DropContactdetailsAdapter(Context context, List<DropContact> filteredData) {
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
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final DropContact contact = (DropContact) getItem(position);
        if (contact.drop_contact_more_info.equals("")) {
            String nameNumber = contact.drop_contact_name + ", " + contact.drop_contact_number;
            mViewHolder.text.setText(nameNumber);
        } else {
            String nameNumber = contact.drop_contact_name + ", " + contact.drop_contact_number + ", " + contact.drop_contact_more_info;
            mViewHolder.text.setText(nameNumber);
        }

        return convertView;
    }

}

