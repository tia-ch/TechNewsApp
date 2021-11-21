package com.lau.technewsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Activity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        WebView newsView = findViewById(R.id.webview);

        newsView.getSettings().setJavaScriptEnabled(true);

        newsView.setWebViewClient(new WebViewClient());

        Intent page = getIntent();
        //display news on the webpage
        newsView.loadData(page.getStringExtra("content"), "text/html", "UTF-8");
    }
}