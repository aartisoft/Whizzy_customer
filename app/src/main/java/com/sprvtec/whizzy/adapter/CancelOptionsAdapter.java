package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.CancelOption;

import java.util.List;

/**
 * Created by SPRV on 9/24/2015.
 */
public class CancelOptionsAdapter extends BaseAdapter {
    LayoutInflater inflater;
    private List<CancelOption> filteredData;

    private class MyViewHolder {
        TextView optionText;

        public MyViewHolder(View item) {
            optionText = item.findViewById(R.id.text);
        }
    }

    public CancelOptionsAdapter(Context context, List<CancelOption> filteredData) {
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
            convertView = inflater.inflate(R.layout.list_item_cancel, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final CancelOption option = (CancelOption) getItem(position);
        mViewHolder.optionText.setText(option.option);


        return convertView;
    }

}

