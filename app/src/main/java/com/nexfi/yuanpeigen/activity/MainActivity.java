package com.nexfi.yuanpeigen.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.weight.Fragment_nearby;
import com.nexfi.yuanpeigen.weight.Fragment_settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener, Runnable {

    private Fragment_nearby fragment_nearby;
    private Fragment_settings fragment_settings;
    private Handler handler, handler_wifiStatus;
    private Handler mHandler;
    private boolean isExit, isWifiStatus, isDialog = true, isUserInfo, isAbout, isDialogRoom, isSettingsAdhoc, isAdhoc, isSettings = true;
    private FragmentManager mFragmentManager;
    private RadioGroup myTabRg;
    private RadioButton rb_nearby, rb_settings;
    private AlertDialog mAlertDialog, alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        isWifiStatus = isWifiConnected(this);
        Intent intent = getIntent();
        isUserInfo = intent.getBooleanExtra("isUserInfo", true);
        isAbout = intent.getBooleanExtra("isAbout", true);
        isAdhoc = intent.getBooleanExtra("isAdhoc", true);
        isSettingsAdhoc = intent.getBooleanExtra("isSettingsAdhoc", true);
        if (isUserInfo && isSettingsAdhoc) {
            initNearByFragment();
        } else {
            initSettingsFragment();
        }
        isDialog = intent.getBooleanExtra("Dialog", true);
        isDialogRoom = intent.getBooleanExtra("DialogRoom", true);
        if (isDialog && isUserInfo && isAbout && isDialogRoom && isWifiStatus && isAdhoc) {
            initDialog();
            handler = new Handler();
            handler.postDelayed(this, 1000);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isExit = false;
            }
        };
        isSettings = intent.getBooleanExtra("isSettings", true);
        if (!isSettings || !isSettingsAdhoc) {
            rb_settings.setChecked(true);
        } else {
            rb_nearby.setChecked(true);
        }
        wifiStatus();
    }


    private void initDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(v);
        mAlertDialog.setCancelable(false);
    }


    @Override
    public void run() {
        mAlertDialog.dismiss();
    }


    private void initNearByFragment() {
        mFragmentManager = getFragmentManager();
        fragment_nearby = new Fragment_nearby();
        fragment_settings = new Fragment_settings();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.container, fragment_settings)
                .add(R.id.container, fragment_nearby)
                .hide(fragment_settings).commit();
    }

    private void initSettingsFragment() {
        mFragmentManager = getFragmentManager();
        fragment_nearby = new Fragment_nearby();
        fragment_settings = new Fragment_settings();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.container, fragment_settings)
                .add(R.id.container, fragment_nearby)
                .hide(fragment_nearby).commit();
    }

    private void initView() {
        myTabRg = (RadioGroup) findViewById(R.id.tab_menu);
        rb_nearby = (RadioButton) findViewById(R.id.rb_nearby);
        rb_settings = (RadioButton) findViewById(R.id.rb_settings);
        myTabRg.setOnCheckedChangeListener(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                mHandler.sendEmptyMessageDelayed(0, 1500);
                Toast.makeText(this, "再按一次退出NexFi", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                finish();
            }
        }
        return true;
    }

    private void wifiStatus() {
        if (!isWifiConnected(MainActivity.this)) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View v = inflater.inflate(R.layout.dialog_wifi, null);
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
            alertDialog.show();
            alertDialog.getWindow().setContentView(v);
            alertDialog.setCancelable(false);
            handler_wifiStatus = new Handler();
            handler_wifiStatus.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            }, 1500);
        }
    }

    /**
     * 判断ＷiFi是否打开
     */
    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static String getTxtFileInfo(Context context) {
        try {
            File file = new File(context.getFilesDir(), "userinfo.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String content = br.readLine();
            Map<String, Object> map = new HashMap<String, Object>();
            String[] contents = content.split("##");
            map.put("username", contents[0]);
//          map.put("password", contents[1]);
            fis.close();
            br.close();
            String username = (String) map.get("username");
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_nearby:
                mFragmentManager.beginTransaction()
                        .show(fragment_nearby).hide(fragment_settings).commit();
                break;
            case R.id.rb_settings:
                mFragmentManager.beginTransaction()
                        .show(fragment_settings).hide(fragment_nearby).commit();
                break;
        }
    }


}