package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.PhoneContact;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by SPRV on 9/24/2015.
 */
public class PhoneContactsListAdapter extends BaseAdapter implements Filterable {
    LayoutInflater inflater;
    private List<PhoneContact> originalData;
    private List<PhoneContact> filteredData;
    private ItemFilter mFilter = new ItemFilter();

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class MyViewHolder {
        TextView name, mobileNumber;

        public MyViewHolder(View item) {
            name =  item.findViewById(R.id.name);
            mobileNumber =  item.findViewById(R.id.number);
        }
    }

    public PhoneContactsListAdapter(Context context, List<PhoneContact> users) {
        inflater = LayoutInflater.from(context);

        this.filteredData = users;
        this.originalData = users;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final PhoneContact user = (PhoneContact) getItem(position);
        mViewHolder.name.setText(user.Name);
        mViewHolder.mobileNumber.setText(user.MobileNumber);
        return convertView;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<PhoneContact> list = originalData;

            int count = list.size();
            final ArrayList<PhoneContact> nlist = new ArrayList<>(count);

            PhoneContact filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.Name.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<PhoneContact>) results.values;
            notifyDataSetChanged();
        }

    }
}

