package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.sprvtec.whizzy.R;

/**
 * Created by Sowjanya on 5/22/2017.
 */

public class HelpActivity extends Activity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/faq.html");
        findViewById(R.id.back).setOnClickListener(v -> finish());

    }


}
