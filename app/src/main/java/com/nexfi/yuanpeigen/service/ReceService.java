package com.nexfi.yuanpeigen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.thoughtworks.xstream.XStream;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ReceService extends Service {
    public String localIP;//本机IP
    BuddyDao dao;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //保存用户的IP
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);
        dao = new BuddyDao(getApplicationContext());
        init();
        initReceUDP();
    }


    //单对单聊天消息的监听
    private void initReceUDP() {
        new Thread() {
            public void run() {

                try {
                    byte[] buf = new byte[1024];
                    DatagramSocket ds = new DatagramSocket(10000);//开始监视12345端口
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.1.255"), 10000);//创建接收数据报的实例
                    while (true) {
//                        Log.e("TAG", "------while (true)---service");
                        ds.receive(dp);//阻塞,直到收到数据报后将数据装入IP中
                        //转化
                        XStream x = new XStream();
                        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                        ChatMessage fromXml= (ChatMessage) x.fromXML(new String(dp.getData()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private void init() {
        new Thread() {
            public void run() {
                try {
                    MulticastSocket ds = new MulticastSocket(8005);
                    InetAddress receiveAddress = InetAddress.getByName("224.1.1.105");
                    ds.joinGroup(receiveAddress);

                    while (true) {

                        //得到数据大小
                        byte[] source = new byte[1024];
                        DatagramPacket dp= new DatagramPacket(source,1024,receiveAddress, 8005);
//                        Log.e("TAG",dp+"--------------------------------receive------------------dp");
                        ds.receive(dp);
                        byte[] raw_data = dp.getData();
                        byte[] size_data = new byte[4];
                        System.arraycopy(raw_data, 0, size_data, 0, 4);
                        int dataLength=byteArrayToInt(size_data);
                        byte[] body_data = new byte[dataLength];
                        System.arraycopy(raw_data, 4, body_data, 0, dataLength);

//                        Log.e("TAG",dataLength+"--------------------------------receive------------------dataLength");//1011050609
                        String xml_content = new String(body_data);
//                        Log.e("TAG", "xml_content:" + xml_content);

                        //解析
                        XStream x = new XStream();
                        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                        ChatMessage fromXml= (ChatMessage) x.fromXML(xml_content);
//                        Log.e("TAG", fromXml.account + "---------------------上线了");


                        if ("online".equals(fromXml.type)) {
//                            Log.e("TAG",  "----------online-----------online"+fromXml.type);
                            if (!localIP.equals(fromXml.account)) {
//                                Log.e("TAG",  dao.find(fromXml.account)+"----------localIP-----------localIP"+fromXml.account);
                                if (!dao.find(fromXml.account)) {
//                                    Log.e("TAG",  "----------find----------find"+fromXml.account);
//                                    dao.add(fromXml);
                                    dao.addP2PMsg(fromXml);
//                                    Log.e("TAG", "----------addP2PMsg----------addP2PMsg================================================"+fromXml.account);
//                                    System.out.println("---tianjia--------------------------------------------------------");
                                }
                            }

                        } else if ("offine".equals(fromXml.type)) {
                            //根据IP删除数据库中的记录
                            dao.delete(fromXml.account);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }



    public static int byteArrayToInt(byte[] bytes) {
        int value= 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

}
