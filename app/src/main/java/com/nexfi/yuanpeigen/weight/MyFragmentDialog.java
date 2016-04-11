package com.nexfi.yuanpeigen.weight;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.activity.OpenAdhocActivity;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.UserInfo;

import java.io.DataOutputStream;
import java.io.File;

/**
 * Created by Mark on 2016/3/14.
 */
public class MyFragmentDialog extends DialogFragment {
    private TextView tv_adhoc, tv_wifi, tv_cancel;
    private WifiManager wifiManager;
    private AlertDialog alertDialog;
    private boolean isRoot;
    private boolean enabled_Flag, isNexFi = false;
    private LinearLayout layout_wifi, layout_nexfi;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.net_dialog, null);
        layout_nexfi = (LinearLayout) view.findViewById(R.id.layout_nexfi);
        layout_wifi = (LinearLayout) view.findViewById(R.id.layout_wifi);
        tv_adhoc = (TextView) view.findViewById(R.id.tv_Adhoc);
        tv_wifi = (TextView) view.findViewById(R.id.tv_WiFi);
        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        initIsFirstInformation();
        initEnabledInformation();
        if (isNexFi) {
            if (enabled_Flag) {
                layout_wifi.setBackgroundColor(Color.rgb(173, 173, 173));
                tv_wifi.setEnabled(false);
                layout_nexfi.setBackgroundColor(Color.rgb(255, 255, 255));
                tv_adhoc.setEnabled(true);
                enabled_Flag = false;
            } else {
                layout_nexfi.setBackgroundColor(Color.rgb(173, 173, 173));
                tv_adhoc.setEnabled(false);
                layout_wifi.setBackgroundColor(Color.rgb(255, 255, 255));
                tv_wifi.setEnabled(true);
                enabled_Flag = true;
            }
        }
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        tv_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo.setNexFiInformation(getActivity());
                if (enabled_Flag) {
                    if (wifiManager.isWifiEnabled()) {
                        Toast.makeText(getActivity(), "WiFi已开启", Toast.LENGTH_SHORT).show();
                    } else {
                        wifiManager.setWifiEnabled(true);
                    }
                    layout_wifi.setBackgroundColor(Color.rgb(173, 173, 173));
                    tv_wifi.setEnabled(false);
                    layout_nexfi.setBackgroundColor(Color.rgb(255, 255, 255));
                    tv_adhoc.setEnabled(true);
                    enabled_Flag = false;

                    UserInfo.saveEnabledInformation(getActivity(), true);
                }

            }
        });


        tv_adhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo.setNexFiInformation(getActivity());
                if (!enabled_Flag) {
                    layout_nexfi.setBackgroundColor(Color.rgb(173, 173, 173));
                    tv_adhoc.setEnabled(false);
                    layout_wifi.setBackgroundColor(Color.rgb(255, 255, 255));
                    tv_wifi.setEnabled(true);
                    enabled_Flag = true;
                    UserInfo.saveEnabledInformation(getActivity(), false);
                }
                if (isRoot()) {
                    isRoot = upgradeRootPermission(getActivity().getPackageCodePath());
                    if (isRoot) {
                        wifiManager.setWifiEnabled(false);
                        startActivity(new Intent(getActivity(), OpenAdhocActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "请重新授权Root权限", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "抱歉，您手机尚未Root", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setView(view);
        alertDialog = builder.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        return alertDialog;
    }

    private void initEnabledInformation() {
        SharedPreferences preferences = getActivity().getSharedPreferences("EnabledInformation", Context.MODE_PRIVATE);
        enabled_Flag = preferences.getBoolean("Enabled", true);
    }

    private void initIsFirstInformation() {
        SharedPreferences preferences = getActivity().getSharedPreferences("first_nexfi", Context.MODE_PRIVATE);
        isNexFi = preferences.getBoolean("isNexFi", false);
    }

    /**
     * 应用程序运行命令获取 Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public boolean isRoot() {
        boolean bool = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }
}
