package com.nexfi.yuanpeigen.application;

import android.app.Application;
import android.content.Context;

import com.nexfi.yuanpeigen.bean.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/2/29.
 */
public class MyApplication extends Application {

    public boolean DEBUG=true;
    public List<ChatMessage> mDataArrays = new ArrayList<ChatMessage>();
    public static Context mContext;



    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
