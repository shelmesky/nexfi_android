package com.nexfi.yuanpeigen.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Mark on 2016/3/3.
 */
public class UserInfo {

    public static boolean saveUserInfo(Context context, String username, String password) {
        try {
            File file = new File(context.getFilesDir(), "userinfo.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((username + "##" + password).getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static void saveIP(Context context, String userip) {
        SharedPreferences sp = context.getSharedPreferences("IP", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("useIP", userip);
        editor.commit();
    }

    public static void saveUsername(Context context, String username) {
        SharedPreferences sp = context.getSharedPreferences("username", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", username);
        editor.commit();
    }

    public static void saveUsersex(Context context, String usersex) {
        SharedPreferences sp = context.getSharedPreferences("usersex", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userSex", usersex);
        editor.commit();
    }

    public static void setConfigurationInformation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("first_pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstIn", false);
        editor.commit();
    }

    public static void setNexFiInformation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("first_nexfi", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isNexFi", true);
        editor.commit();
    }

    public static void saveEnabledInformation(Context context, boolean flag) {
        SharedPreferences preferences = context.getSharedPreferences("EnabledInformation", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("Enabled", flag);
        editor.commit();
    }



    public static void saveUserHeadIcon(Context context, int id) {
        SharedPreferences preferences = context.getSharedPreferences("UserHeadIcon", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("userhead", id);
        editor.commit();
    }

}
