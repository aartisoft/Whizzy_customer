package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.vo.FareDetails;

public class FareDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_details);
        FareDetails details = getIntent().getParcelableExtra(Constants.IntentKey.KEY_FARE_DETAILS);
        TextView cgst = findViewById(R.id.cgst);
        cgst.setText(details.CGST);
        TextView sgst = findViewById(R.id.sgst);
        sgst.setText(details.SGST);
        TextView baseFare = findViewById(R.id.base_fare);
        baseFare.setText(details.Base_Fare);
        findViewById(R.id.close).setOnClickListener(v -> finish());

    }

}
