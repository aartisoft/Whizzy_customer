package com.sprvtec.whizzy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.vo.Item;

import java.util.List;
import java.util.Locale;

public class BreakFareDownAdapter extends ArrayAdapter<Item> {

    private List<Item> animalList;

    public BreakFareDownAdapter(Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        animalList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.faredown, null);

        String amount = animalList.get(position).amount;
        double value = Double.parseDouble(amount);

        TextView textViewAmt =  v.findViewById(R.id.amnt);
        TextView textViewDesc =  v.findViewById(R.id.desc);


        System.out.println("data"+animalList.get(position).amount);

        if (animalList.get(position).amount.equalsIgnoreCase("0")){
            textViewAmt.setText("");
            textViewDesc.setText("");
        }else {
            textViewAmt.setText(String.format(Locale.getDefault(),"%.2f", value));
            textViewDesc.setText(animalList.get(position).expense_description);
        }




        return v;

    }


}
