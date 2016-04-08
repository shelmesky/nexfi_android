package com.nexfi.yuanpeigen.weight;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;

import java.io.DataOutputStream;
import java.io.File;

/**
 * Created by Mark on 2016/3/14.
 */
public class MyFragmentDialog extends DialogFragment {
    private TextView tv_adhoc, tv_wifi;
    private WifiManager wifiManager;
    private AlertDialog alertDialog;
    private boolean isRoot;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.net_dialog, null);
        tv_adhoc = (TextView) view.findViewById(R.id.tv_Adhoc);
        tv_wifi = (TextView) view.findViewById(R.id.tv_WiFi);
        tv_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    Toast.makeText(getActivity(), "WiFi已开启", Toast.LENGTH_SHORT).show();
                } else {
                    wifiManager.setWifiEnabled(true);
                }
                alertDialog.dismiss();
            }
        });

        tv_adhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isRoot()) {
//                    isRoot = upgradeRootPermission(getActivity().getPackageCodePath());
//                    if (isRoot) {
//                        Toast.makeText(getActivity(), "NexFi开启成功", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getActivity(), "请重新授权", Toast.LENGTH_SHORT).show();
//                    }
//                    alertDialog.dismiss();
//                } else {
//                    Toast.makeText(getActivity(), "抱歉，您手机尚未Root", Toast.LENGTH_SHORT).show();
//                }
                Toast.makeText(getActivity(), "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(view);
        alertDialog = builder.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        return alertDialog;
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
