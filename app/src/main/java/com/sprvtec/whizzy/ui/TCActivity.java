package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.sprvtec.whizzy.R;

/**
 * Created by Sowjanya on 5/22/2017.
 */

public class TCActivity extends Activity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
//        String url = getIntent().getStringExtra(Constants.IntentKey.KEY_URL);
//        webView.loadUrl(url);
        webView.loadUrl("file:///android_asset/tc.html");
        findViewById(R.id.back).setOnClickListener(v -> finish());

    }


}
