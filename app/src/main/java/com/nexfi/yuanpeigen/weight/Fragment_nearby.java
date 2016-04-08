package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.activity.ChatRoomActivity;
import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/2/4.
 */
public class Fragment_nearby extends Fragment {
    private List<ChatMessage> mDataArraysNew = new ArrayList<ChatMessage>();
    private List<ChatMessage> mDataArraysOnline = new ArrayList<ChatMessage>();
    private List<ChatMessage> mDataArraysOffline = new ArrayList<ChatMessage>();
    private ImageView iv_add;
    private LinearLayout addChatRoom, share;
    private View View_pop, view_share, v_parent;
    private PopupWindow mPopupWindow = null, mPopupWindow_share = null;
    private String localIp;
    private BaseExpandableListAdapter usrListAdapter;
    private ExpandableListView userList;
    private List<String> groupList = new ArrayList<String>();
    private List<List<ChatMessage>> childListNew = new ArrayList<List<ChatMessage>>();
    private List<List<ChatMessage>> childListOnline = new ArrayList<List<ChatMessage>>();
    private List<List<ChatMessage>> childListOffline = new ArrayList<List<ChatMessage>>();


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    userList.setAdapter(usrListAdapter);
                }
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v_parent = inflater.inflate(R.layout.fragment_nearby, container, false);
        userList = (ExpandableListView) v_parent.findViewById(R.id.ex_userlist);
        View_pop = inflater.inflate(R.layout.pop_menu_add, null);
        view_share = inflater.inflate(R.layout.layout_share, null);
        int ipAddress = getIpAddress();
        localIp = intToIp(ipAddress);
        addChatRoom = (LinearLayout) View_pop.findViewById(R.id.add_chatRoom);
        share = (LinearLayout) View_pop.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                initPopShare();
            }
        });
        addChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage msg = new ChatMessage();
                SharedPreferences preferences = getActivity().getSharedPreferences("username", getActivity().MODE_PRIVATE);
                String username = preferences.getString("userName", "宝宝");
                msg.account = localIp;
                msg.nick = username;
                msg.type = "online";
                XStream x = new XStream();
                x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                String xml = x.toXML(msg);
                SocketUtils.startSendRoomThread(xml);//发送进入聊天室通知
                //同时进入聊天室
                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        iv_add = (ImageView) v_parent.findViewById(R.id.iv_add);
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPop();
            }
        });


        setAdapter();//给附近的好友设置适配器

        userList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 2) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("8", mDataArraysNew.get(childPosition).account);
                    intent.putExtra("1", mDataArraysNew.get(childPosition).nick);
                    intent.putExtra("3", mDataArraysNew.get(childPosition).avatar);
                    startActivity(intent);
                    getActivity().finish();
                } else {
//                    startActivity(new Intent(getActivity(), ChatActivity.class));
                    Toast.makeText(Fragment_nearby.this.getActivity(), "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        Log.e("TAG", Fragment_nearby.this.getActivity() + "-------------onCreateView-----------------Fragment_nearby.this.getActivity()");
        getActivity().getContentResolver().registerContentObserver(
                Uri.parse("content://www.nexfi.com"), true,
                new Myobserve(new Handler()));
        return v_parent;
    }


    private class Myobserve extends ContentObserver {

        public Myobserve(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    groupList.clear();
                    childListNew.clear();
                    childListOnline.clear();
                    childListOffline.clear();
                    /**
                     *  Group数据
                     * */
                    groupList.add("在线好友");
                    groupList.add("离线好友");
                    groupList.add("附近的人");
                    Log.e("TAG", Fragment_nearby.this.getActivity()+ "----------------------findAll--------Fragment_nearby.this.getActivity()");
                    BuddyDao buddyDao = new BuddyDao(MyApplication.getContext());
                    mDataArraysNew = buddyDao.findAll(localIp);//查找所有用户

                    for (int index = 0; index < groupList.size(); ++index) {
                        childListNew.add(mDataArraysNew);
                        childListOnline.add(mDataArraysOnline);
                        childListOffline.add(mDataArraysOffline);
                    }
                    usrListAdapter = new UserList(MyApplication.getContext(), mDataArraysNew, mDataArraysOnline, mDataArraysOffline, groupList, childListNew, childListOnline, childListOffline);
                    Message msg=handler.obtainMessage();
                    msg.what=1;
                    msg.obj=usrListAdapter;
                    handler.sendMessage(msg);
                }
            }.start();
            super.onChange(selfChange);
        }
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(this.getActivity());
        mDataArraysNew = buddyDao.findAll(localIp);
        Log.e("TAG", "setAdapter----------------------------------------------------------" + mDataArraysNew.size());

        /**
         * Online 虚拟数据
         * */
        ChatMessage chatUserOnline1 = new ChatMessage();
        chatUserOnline1.nick = "Mark";
        chatUserOnline1.avatar = R.mipmap.user_head_male_1;
        mDataArraysOnline.add(chatUserOnline1);
        ChatMessage chatUserOnline2 = new ChatMessage();
        chatUserOnline2.nick = "AngelBaby";
        chatUserOnline2.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline2);
        ChatMessage chatUserOnline3 = new ChatMessage();
        chatUserOnline3.nick = "李晨";
        chatUserOnline3.avatar = R.mipmap.user_head_male_2;
        mDataArraysOnline.add(chatUserOnline3);
        ChatMessage chatUserOnline4 = new ChatMessage();
        chatUserOnline4.nick = "赵薇";
        chatUserOnline4.avatar = R.mipmap.user_head_female_4;
        mDataArraysOnline.add(chatUserOnline4);
        ChatMessage chatUserOnline5 = new ChatMessage();
        chatUserOnline5.nick = "冯小刚";
        chatUserOnline5.avatar = R.mipmap.user_head_male_3;
        mDataArraysOnline.add(chatUserOnline5);
        ChatMessage chatUserOnline6 = new ChatMessage();
        chatUserOnline6.nick = "佟丽娅";
        chatUserOnline6.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline6);
        ChatMessage chatUserOnline7 = new ChatMessage();
        chatUserOnline7.nick = "沈腾";
        chatUserOnline7.avatar = R.mipmap.user_head_male_4;
        mDataArraysOnline.add(chatUserOnline7);
        ChatMessage chatUserOnline8 = new ChatMessage();
        chatUserOnline8.nick = "马云";
        chatUserOnline8.avatar = R.mipmap.user_head_male_3;
        mDataArraysOnline.add(chatUserOnline8);
        ChatMessage chatUserOnline9 = new ChatMessage();
        chatUserOnline9.nick = "马化腾";
        chatUserOnline9.avatar = R.mipmap.user_head_male_2;
        mDataArraysOnline.add(chatUserOnline9);
        ChatMessage chatUserOnline10 = new ChatMessage();
        chatUserOnline10.nick = "高圆圆";
        chatUserOnline10.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline10);

        /**
         * Offline 虚拟数据
         * */
        ChatMessage chatUserOffline1 = new ChatMessage();
        chatUserOffline1.nick = "Lights";
        chatUserOffline1.avatar = R.mipmap.user_head_male_1;
        mDataArraysOffline.add(chatUserOffline1);
        ChatMessage chatUserOffline2 = new ChatMessage();
        chatUserOffline2.nick = "李晨";
        chatUserOffline2.avatar = R.mipmap.user_head_male_2;
        mDataArraysOffline.add(chatUserOffline2);
        ChatMessage chatUserOffline3 = new ChatMessage();
        chatUserOffline3.nick = "高圆圆";
        chatUserOffline3.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline3);
        ChatMessage chatUserOffline4 = new ChatMessage();
        chatUserOffline4.nick = "马化腾";
        chatUserOffline4.avatar = R.mipmap.user_head_male_2;
        mDataArraysOffline.add(chatUserOffline4);
        ChatMessage chatUserOffline5 = new ChatMessage();
        chatUserOffline5.nick = "马云";
        chatUserOffline5.avatar = R.mipmap.user_head_male_3;
        mDataArraysOffline.add(chatUserOffline5);
        ChatMessage chatUserOffline6 = new ChatMessage();
        chatUserOffline6.nick = "赵薇";
        chatUserOffline6.avatar = R.mipmap.user_head_female_4;
        mDataArraysOffline.add(chatUserOffline6);
        ChatMessage chatUserOffline7 = new ChatMessage();
        chatUserOffline7.nick = "冯小刚";
        chatUserOffline7.avatar = R.mipmap.user_head_male_3;
        mDataArraysOffline.add(chatUserOffline7);
        ChatMessage chatUserOffline8 = new ChatMessage();
        chatUserOffline8.nick = "AngelBaby";
        chatUserOffline8.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline8);
        ChatMessage chatUserOffline9 = new ChatMessage();
        chatUserOffline9.nick = "沈腾";
        chatUserOffline9.avatar = R.mipmap.user_head_male_4;
        mDataArraysOffline.add(chatUserOffline9);
        ChatMessage chatUserOffline10 = new ChatMessage();
        chatUserOffline10.nick = "佟丽娅";
        chatUserOffline10.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline10);

        /**
         *  Group数据
         * */
        groupList.add("在线好友");
        groupList.add("离线好友");
        groupList.add("附近的人");


        for (int index = 0; index < groupList.size(); ++index) {
            childListNew.add(mDataArraysNew);
            childListOnline.add(mDataArraysOnline);
            childListOffline.add(mDataArraysOffline);
        }

        usrListAdapter = new UserList(this.getActivity(), mDataArraysNew, mDataArraysOnline, mDataArraysOffline, groupList, childListNew, childListOnline, childListOffline);
        userList.setAdapter(usrListAdapter);
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv_add, 0, 0);
    }

    private void initPopShare() {
        if (mPopupWindow_share == null) {
            mPopupWindow_share = new PopupWindow(view_share, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow_share.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow_share.showAtLocation(v_parent, Gravity.CENTER, 0, 0);
    }

    public String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private int getIpAddress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

}

