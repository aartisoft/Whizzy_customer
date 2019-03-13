package com.sprvtec.whizzy.ui;

import android.app.Activity;
import android.os.Bundle;

import com.sprvtec.whizzy.R;

/**
 * Created by Sowjanya on 5/22/2017.
 */

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        //gfdgdfg
    }


}
