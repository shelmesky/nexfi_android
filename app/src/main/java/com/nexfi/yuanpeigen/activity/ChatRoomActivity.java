package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.FileUtils;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.weight.ChatRoomMessageAdapater;
import com.thoughtworks.xstream.XStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mark on 2016/3/22.
 */
public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {
    static MyApplication app=new MyApplication();

    private TextView textViewRoom;
    private RelativeLayout iv_backRoom;
    private ListView lv_chatRoom;
    private ImageView iv_addRoom, iv_chatRoom_room, iv_picRoom, iv_photographRoom, iv_folderRoom;
    private EditText et_chatRoom;
    private Button btn_sendMsgRoom;
    private ChatRoomMessageAdapater chatRoomMessageAdapater;
    private LinearLayout layout_view;
    private boolean visibility_Flag = false;
    private String username, localIP;
    public static final int REQUEST_CODE_SELECT_FILE = 1;
    private int myAvatar;
    private String select_file_path = "";//发送端选择的文件的路径

    private String rece_file_path = "";//接收端文件的保存路径
    private List<ChatMessage> mDataArrays = new ArrayList<ChatMessage>();

    private int count=0;//在线人数

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    receive((ChatMessage) msg.obj);
                }
            }else if(msg.what==2){
                //上线消息，可以计算在线人数
                ChatMessage chatMessage= (ChatMessage) msg.obj;
                BuddyDao dao=new BuddyDao(getApplicationContext());
                if(!(dao.findSame(chatMessage.account,chatMessage.type))){
                    dao.addRoomMsg(chatMessage);
                    count++;
                    Log.e("TAG",count+"---------------------------------------------------------在线人数");
                }
            }else if(msg.what==3){
                //离线消息
                ChatMessage chatMessage= (ChatMessage) msg.obj;
                BuddyDao dao=new BuddyDao(getApplicationContext());
//                if(!(dao.findSame(chatMessage.account,chatMessage.type))){
//                    dao.addRoomMsg(chatMessage);
//                    count++;
//                    Log.e("TAG",count+"---------------------------------------------------------在线人数");
//                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        SharedPreferences preferences2 = getSharedPreferences("username", Context.MODE_PRIVATE);
        username = preferences2.getString("userName", null);
        localIP=SocketUtils.getLocalIP(getApplicationContext());
        SocketUtils.initReceMul(handler,localIP);
        initView();
        setOnClickListener();
        setAdapter();
        initmyAvatar();
    }


    private void initmyAvatar() {
        SharedPreferences preferences = getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        myAvatar = preferences.getInt("userhead", R.mipmap.user_head_female_3);
    }

    private void receive(ChatMessage chatMessage) {
        //接收到多播后给予反馈
        Log.e("TAG",chatMessage.fromIP+"======================chatMessage.fromIP-------");

        chatMessage.msgType = 1;
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        if(!buddyDao.findRoomMsgByUuid(chatMessage.uuid)){
            buddyDao.addRoomMsg(chatMessage);
            mDataArrays.add(chatMessage);
            chatRoomMessageAdapater.notifyDataSetChanged();
            lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);
        }

        //反馈的信息
        ChatMessage user=new ChatMessage();
        user.content="response";
        user.fromIP=localIP;

        XStream x = new XStream();
        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
        String xml =x.toXML(user);
        sendUDP(chatMessage.fromIP,xml);

    }

    private void send() {

        final String contString = et_chatRoom.getText().toString();
        if (contString.length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.fromAvatar = myAvatar;
            chatMessage.msgType = 0;
            chatMessage.sendTime = FileUtils.getDateNow();
            chatMessage.fromIP = localIP;
            chatMessage.content = contString;
            chatMessage.fromNick = username;
            chatMessage.type = "chatRoom";
            chatMessage.uuid= UUID.randomUUID().toString();
            XStream x = new XStream();
            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
            String xml =x.toXML(chatMessage);
            if(app.DEBUG){
                Log.e("TAG", "-聊天室发送------------------------" + xml);
            }
            SocketUtils.sendBroadcastRoom(xml);//发送群聊消息

            //重复发
//            initReUDP(handler, xml);

            BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
            buddyDao.addRoomMsg(chatMessage);
            mDataArrays.add(chatMessage);
            chatRoomMessageAdapater.notifyDataSetChanged();
            lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);
        }
    }

    //TODO 2016/4/6
    //接收单播
    public void initReUDP(final Handler handler,final String xml) {

        new Thread() {
            public void run() {
                try {
                    DatagramSocket mDataSocket=null;
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    if (mDataSocket == null) {
                        mDataSocket = new DatagramSocket(null);
                        mDataSocket.setReuseAddress(true);
                        mDataSocket.bind(new InetSocketAddress(10001));
                        Log.e("TAG", mDataSocket + "-----------------------------------------mDataSocket------------------------"+dp);
                    }
                    int response_count=0;//反馈次数
                    int sendTimes=0;//重发次数
                    while (true) {
                        mDataSocket.receive(dp);
                        if(null!=dp){
                            Log.e("TAG", dp + "-----------------------------------------XStream------------------------");
                            XStream x = new XStream();
                            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                            ChatMessage fromXml= (ChatMessage)x.fromXML(new String(dp.getData()));
                            Log.e("TAG", fromXml.content + "--------------------------------fromXml.content---------XStream------------------------");
                            if(fromXml.content.equals("response")){
                                response_count++;
                                Log.e("TAG",response_count+"-----------------------------------------response_count------------------------");
                                BuddyDao dao=new BuddyDao(getApplicationContext());
                                List<ChatMessage> mList=dao.findRoomByType("online");//在线人数
                                Log.e("TAG",mList.size()+"=================================-----------mList.size()-------------------------------");
                                if(mList.size()-1==response_count || sendTimes>=3){//在线人数等于反馈个数
                                    break;
                                }else{//如果有人没收到，就重新发一次多播
                                    SocketUtils.sendBroadcastRoom(xml);//发送群聊消息
                                    Log.e("TAG",sendTimes+"-----------------------------------------sendTimes------------------------");
                                    sendTimes++;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //发送UDP单播
    public void sendUDP(final String destIP, final String msg) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    InetAddress target = InetAddress.getByName(destIP);
                    DatagramSocket ds = new DatagramSocket();
                    byte[] buf = msg.getBytes();
                    DatagramPacket op = new DatagramPacket(buf, buf.length, target, 10001);
                    ds.send(op);
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        mDataArrays = buddyDao.findRoomMsgAll();
        chatRoomMessageAdapater = new ChatRoomMessageAdapater(getApplicationContext(), mDataArrays);
        lv_chatRoom.setAdapter(chatRoomMessageAdapater);
    }

    private void initView() {
        textViewRoom = (TextView) findViewById(R.id.textViewRoom);
        iv_backRoom = (RelativeLayout) findViewById(R.id.iv_backRoom);
        iv_chatRoom_room = (ImageView) findViewById(R.id.iv_chatRoom_room);
        lv_chatRoom = (ListView) findViewById(R.id.lv_chatRoom);
        iv_photographRoom = (ImageView) findViewById(R.id.iv_photographRoom);
        iv_picRoom = (ImageView) findViewById(R.id.iv_picRoom);
        iv_folderRoom = (ImageView) findViewById(R.id.iv_folderRoom);
        iv_addRoom = (ImageView) findViewById(R.id.iv_addRoom);
        et_chatRoom = (EditText) findViewById(R.id.et_chatRoom);
        btn_sendMsgRoom = (Button) findViewById(R.id.btn_sendMsgRoom);
        layout_view = (LinearLayout) findViewById(R.id.layout_viewRoom);
        textViewRoom.setText("群聊");
    }

    private void setOnClickListener() {
        btn_sendMsgRoom.setOnClickListener(this);
        iv_addRoom.setOnClickListener(this);
        iv_folderRoom.setOnClickListener(this);
        iv_picRoom.setOnClickListener(this);
        iv_photographRoom.setOnClickListener(this);
        iv_chatRoom_room.setOnClickListener(this);
        iv_backRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_addRoom:
                if (visibility_Flag) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                break;
            case R.id.iv_backRoom:
                Intent intent = new Intent(ChatRoomActivity.this, MainActivity.class);
                intent.putExtra("DialogRoom", false);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_chatRoom_room:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_photographRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_folderRoom:
                /**
                 * 发送文件
                 * */
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_picRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sendMsgRoom:
                /**
                 * 发送消息
                 * */
                send();
                et_chatRoom.setText(null);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatRoomActivity.this, MainActivity.class);
        intent.putExtra("DialogRoom", false);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //用户离开当前界面时发送离线消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.sendTime = FileUtils.getDateNow();
        chatMessage.fromIP = localIP;
        chatMessage.fromNick = username;
        chatMessage.type = "offline";
        chatMessage.uuid= UUID.randomUUID().toString();
        XStream x = new XStream();
        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
        String xml =x.toXML(chatMessage);
        SocketUtils.sendBroadcastRoom(xml);//发送群聊消息
    }
}
