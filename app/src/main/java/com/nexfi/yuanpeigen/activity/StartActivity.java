package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/3/3.
 */
public class StartActivity extends AppCompatActivity implements Runnable {
    private boolean isFirstIn = false;
    private Handler handler;
    private static final String SHAREDPREFERENCES_NAME = "first_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.icon_loading);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setContentView(imageView);
        initConfigurationInformation();
        handler = new Handler();
        handler.postDelayed(this, 1500);
    }

    private void initConfigurationInformation() {
        SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        isFirstIn = preferences.getBoolean("isFirstIn", true);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void run() {
        if (!isFirstIn) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        }
    }
}
