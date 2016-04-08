package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mark on 2016/3/22.
 */
public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {
    static MyApplication app = new MyApplication();

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
    private List<ChatMessage> mTempDataArrays = new ArrayList<ChatMessage>();

    private int count = 0;//在线人数

    // 多播群聊Socket
    private DatagramSocket chatroom_response_socket = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    receive((ChatMessage) msg.obj);
                }
            } else if (msg.what == 2) {
                //上线消息，可以计算在线人数
                ChatMessage chatMessage = (ChatMessage) msg.obj;
                BuddyDao dao = new BuddyDao(getApplicationContext());
                if (!(dao.findSame(chatMessage.account, chatMessage.type))) {
                    dao.addRoomMsg(chatMessage);
                    count++;
//                    Log.e("TAG", count + "---------------------------------------------------------在线人数");
                }
            } else if (msg.what == 3) {
                //离线消息
                ChatMessage chatMessage = (ChatMessage) msg.obj;
                BuddyDao dao = new BuddyDao(getApplicationContext());
                //此时chatMessage.type是offline
//                if(!(dao.findSame(chatMessage.account,chatMessage.type))){
//                    dao.addRoomMsg(chatMessage);
//                    count--;
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
        localIP = SocketUtils.getLocalIP(getApplicationContext());
        SocketUtils.initReceMul(handler, localIP);
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
//        Log.e("TAG", chatMessage.content + "=============群聊接收=========chatMessage.content -------");

        chatMessage.msgType = 1;
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        if (!buddyDao.findRoomMsgByUuid(chatMessage.uuid)) {
            buddyDao.addRoomMsg(chatMessage);
            mDataArrays.add(chatMessage);
            chatRoomMessageAdapater.notifyDataSetChanged();
            lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);
        }

        //反馈的信息
        ChatMessage user = new ChatMessage();
        user.content = "response";
        user.fromIP = localIP;
        user.uuid = chatMessage.uuid;//添加消息ID

        XStream x = new XStream();
        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
        String xml = x.toXML(user);
        sendResponseUDP(chatMessage.fromIP, xml);

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
            chatMessage.uuid = UUID.randomUUID().toString();
            XStream x = new XStream();
            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
            String message = x.toXML(chatMessage);
            if (app.DEBUG) {
                Log.e("TAG", "-聊天室发送------------------------" + chatMessage.uuid + "===========message======" + message);
            }
            SocketUtils.sendBroadcastRoom(message);//发送群聊消息

            // 判断响应数量并重发
            reSendUDPMultiBroadcast(handler, message, chatMessage.uuid);

            BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
            buddyDao.addRoomMsg(chatMessage);
            mDataArrays.add(chatMessage);
            chatRoomMessageAdapater.notifyDataSetChanged();
            lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);
        }
    }

    public DatagramSocket getChatroomResopnseSocket() {
         /*创建多播Socket对象*/
        try {
            if (chatroom_response_socket == null) {
                chatroom_response_socket = new DatagramSocket(null);

                chatroom_response_socket.setReuseAddress(true);
                chatroom_response_socket.bind(new InetSocketAddress(10001));
            }
            return chatroom_response_socket;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public int byteArrayToInt(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    //发送完成后，等待消息回应或重发
    public void reSendUDPMultiBroadcast(final Handler handler, final String message, final String uuid) {

        new Thread() {
            public void run() {
                Log.e("00000000000000", "has entry resend");
                try {
                    // 等待当前所有群聊用户的消息确认
                    DatagramSocket responseDataSocket = getChatroomResopnseSocket();

                    int response_count = 0;// 当前反馈次数
                    int sendTimes = 0;// 当前重发次数
                    int needSendTimes = 2; // 需要重发的次数

                    while (true) {
                        BuddyDao dao = new BuddyDao(getApplicationContext());
                        List<ChatMessage> mUserList = dao.findRoomByType("online");
                        int user_list_size = mUserList.size() - 1; //获取当前在线人数，没有包括自己
                        Log.e("TAG", "got --------------------------------------在线人数===" + user_list_size);
                        for (ChatMessage mssg:mUserList) {
                            Log.e("TAG","#mssg.account#===="+mssg.account+"#mssg.nick#===="+mssg.nick);
                        }
                        int need_send_times = user_list_size; // 重发给N个人

                        // 如重复的次数超过指定数量，或者已经接收到足够数量的响应，则退出循环
                        if ((sendTimes >= needSendTimes) || response_count == user_list_size) {
                            break;
                        }

                        while (true) {
//                            Log.e("111111111111111111", "start to got response");


                            //二进制接收
                            byte[] response_source = new byte[1024];
                            DatagramPacket response_db = new DatagramPacket(response_source, 1024);
                            try{
                                chatroom_response_socket.setSoTimeout(2000);
                                responseDataSocket.receive(response_db);
                                chatroom_response_socket.setSoTimeout(0);
                            }catch(Exception e){
                                Log.e("TAG",e.toString());
                            }

                            byte[] response_raw_data = response_db.getData();
                            byte[] response_size_data = new byte[4];
                            System.arraycopy(response_raw_data, 0, response_size_data, 0, 4);
                            int response_dataLength = byteArrayToInt(response_size_data);
                            byte[] response_body_data = new byte[response_dataLength];
                            System.arraycopy(response_raw_data, 4, response_body_data, 0, response_dataLength);

                            String response_xml_content = new String(response_body_data);

                            //解析XML
                            XStream x = new XStream();
                            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                            ChatMessage fromXml=null;
                            try {
                                fromXml = (ChatMessage) x.fromXML(response_xml_content);
                            }catch (Exception e){
//                                Log.e("TAG","---fromXml--time_out================---"+e.toString());
                                // 如果接收到的消息响应数量小于在线人数，则重发消息
                                if (response_count < user_list_size) {
//                                    Log.e("RESEND:", "444444444444444444444444444response_count：" + response_count + "######user_list_size" + user_list_size);
                                    SocketUtils.sendBroadcastRoom(message);
                                    sendTimes += 1;
                                    response_count=0;
                                }
                                break;
                            }
//                            Log.e("22222222222222222222222", "-----------------content" + fromXml.content + "----from_uuid=====" + fromXml.uuid + "-----origin_uudi" + uuid);
                            //确定响应内容的正确性和唯一
                            if ((uuid.equals(fromXml.uuid)) && fromXml.content.equals("response")) {
                                response_count += 1;
//                                Log.e("3333333333333333333", "got response");
                            }
                            need_send_times -= 1;
                            // 如已经重发给N个人，则退出本次重发的动作
                            if (need_send_times == 0) {
                                break;
                            }

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //发送响应数据: UDP
    public void sendResponseUDP(final String destIP, final String msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InetAddress target = InetAddress.getByName(destIP);
                    DatagramSocket send_data_socket = new DatagramSocket();
                    byte[] send_responseResource = new byte[1024];
                    byte[] send_responseBuf = msg.getBytes();
                    byte[] send_response_size_data = intToByteArray(send_responseBuf.length);
                    System.arraycopy(send_response_size_data, 0, send_responseResource, 0, 4);
                    System.arraycopy(send_responseBuf, 0, send_responseResource, 4, send_responseBuf.length);
                    DatagramPacket send_data_packet = new DatagramPacket(send_responseResource, send_responseResource.length, target, 10001);
                    Log.e("TAG", "------sendResponseUDP-------------------" + send_responseBuf.length);
                    send_data_socket.send(send_data_packet);
                    send_data_socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        mTempDataArrays = buddyDao.findRoomMsgAll();
        for (int i = 0; i <mTempDataArrays.size() ; i++) {
            if(null!=mTempDataArrays.get(i).content){
                mDataArrays.add(mTempDataArrays.get(i));
            }
        }
        for (ChatMessage mssg:mDataArrays) {
            Log.e("TAG","#mssg.nick#"+mssg.nick+"#mssg.fromIP#"+mssg.fromIP+"----#mssg.account#-----"+mssg.account+"---#mssg.toIP#---"+mssg.toIP+"----#mssg.content#----"+mssg.content);
        }
        Log.e("TAG",mDataArrays.size()+"------------------------------------所有群聊消息的数量---------------");
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
        chatMessage.uuid = UUID.randomUUID().toString();
        XStream x = new XStream();
        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
        String xml = x.toXML(chatMessage);
        SocketUtils.sendBroadcastRoom(xml);//发送群聊消息
    }
}
