package com.nexfi.yuanpeigen.util;

/**
 * Created by gengbaolong on 2016/2/25.
 */

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.BaseChatMsg;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.thoughtworks.xstream.XStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

public class SocketUtils {

    static MyApplication app = new MyApplication();
    private static Timer m_PlayTimer;
    private static TimerTask m_PlayTimerTask;
    private static MulticastSocket ms;

    /* 定时发送多播*/
    public static void startSendThread(final String msg) {

        m_PlayTimer = new Timer();

        m_PlayTimerTask = new TimerTask() {
            public void run() {
                sendBroadcast(msg);
            }

        };
        m_PlayTimer.schedule(m_PlayTimerTask, 0, 1000);//每隔1s发送一次
    }

    public static MulticastSocket GetMultiCastSock() {
         /*创建多播Socket对象*/
        try {
            if (ms == null) {
                ms = new MulticastSocket();
                ms.setBroadcast(true);
                InetAddress address = InetAddress.getByName("224.1.1.105");
                //加入组播
                ms.joinGroup(address);
            }
            return ms;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendBroadcast(String msg) {
        try {
            MulticastSocket ms = GetMultiCastSock();
            InetAddress address = InetAddress.getByName("224.1.1.105");
            byte[] source = new byte[1024];
            byte[] data = msg.getBytes();
            byte[] size_data = intToByteArray(data.length);
//            Log.e("TAG", size_data.length + "----------send-----------size_data_length");
//            Log.e("TAG", data.length + "----------send-----------data_length");
            System.arraycopy(size_data, 0, source, 0, 4);
            System.arraycopy(data, 0, source, 4, data.length);
            DatagramPacket dataPacket = new DatagramPacket(source, 1024, address, 8005);
            //发送
            ms.send(dataPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }


    public static void startSendRoomThread(final String msg) {

        m_PlayTimer = new Timer();

        m_PlayTimerTask = new TimerTask() {
            public void run() {
                sendBroadcastRoom(msg);
            }

        };
        m_PlayTimer.schedule(m_PlayTimerTask, 0, 1000);//每隔1s发送一次
    }


    public static MulticastSocket getMuiSocket() {
         /*创建多播Socket对象*/
        try {
            if (ms == null) {
                ms = new MulticastSocket();
                ms.setBroadcast(true);
                InetAddress address = InetAddress.getByName("224.0.0.110");
                //加入组播
                ms.joinGroup(address);
            }
            return ms;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 聊天室发送多播
     *
     * @param msg
     */
    public static void sendBroadcastRoom(final String msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
            /*创建组播对象*/
                    MulticastSocket send_mul_socket = getMuiSocket();
                    InetAddress address = InetAddress.getByName("224.0.0.110");

//                    Log.e("TAG:", "Send MSG:#################" + msg);

                    byte[] send_room_source = new byte[1024];
                    byte[] send_room_data = msg.getBytes();
                    byte[] send_room_size_data = intToByteArray(send_room_data.length);
                    System.arraycopy(send_room_size_data, 0, send_room_source, 0, send_room_size_data.length);
                    System.arraycopy(send_room_data, 0, send_room_source, send_room_size_data.length, send_room_data.length);
                    DatagramPacket dataPacket = new DatagramPacket(send_room_source, 1024, address, 8007);
//                    Log.e("TAG:", "Ready to send:#################" + send_room_size_data.length + "##" + send_room_data.length);
                    //发送
                    send_mul_socket.send(dataPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 接收UDP多播
     */
    public static void initReceMul(final Handler handler, final String localIP) {
        new Thread() {
            public void run() {
                try {
                    MulticastSocket ds = new MulticastSocket(8007);
                    InetAddress receiveAddress = InetAddress.getByName("224.0.0.110");
                    ds.joinGroup(receiveAddress);
//                    byte[] buff = new byte[1024];
//                    DatagramPacket dp = new DatagramPacket(buff, buff.length, receiveAddress, 8007);
                    while (true) {
                        //通过二进制和数组复制来接收数据：
                        //得到数据大小
                        byte[] source = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(source, 1024, receiveAddress, 8007);
                        ds.receive(dp);
                        byte[] raw_data = dp.getData();
                        byte[] size_data = new byte[4];
                        System.arraycopy(raw_data, 0, size_data, 0, 4);
                        int dataLength = byteArrayToInt(size_data);
//                        Log.e("TAG:", "Receive data length:#################" + dataLength);
                        byte[] body_data = new byte[dataLength];
                        System.arraycopy(raw_data, 4, body_data, 0, dataLength);
//                        Log.e("TAG:", "Receive body length:#################" + body_data.length);

                        String xml_content = new String(body_data);
//                        Log.e("TAG:", "Receive data content:#################" + xml_content);

                        XStream x = new XStream();
                        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                        ChatMessage fromXml = (ChatMessage) x.fromXML(xml_content);
                        if ("online".equals(fromXml.type)) {
                            //上线消息,可以计算出在线人数
                            Message msg = handler.obtainMessage();
                            msg.obj = fromXml;
                            msg.what = 2;
                            handler.sendMessage(msg);

                        } else if ("chatRoom".equals(fromXml.type)) {
                            //群聊消息
                            if (!localIP.equals(fromXml.fromIP)) {
                                Message msg = handler.obtainMessage();
                                msg.obj = fromXml;
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        } else if ("offline".equals(fromXml.type)) {
                            //离线消息
                            Message msg = handler.obtainMessage();
                            msg.obj = fromXml;
                            msg.what = 3;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }


    //发送UDP单播
    public static void sendUDP(final String destIP, final String msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InetAddress target = InetAddress.getByName(destIP);
                    DatagramSocket ds = new DatagramSocket();
                    byte[] buf = msg.getBytes();
                    DatagramPacket op = new DatagramPacket(buf, buf.length, target, 10005);
                    ds.send(op);
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 接收UDP单播
     */
    public static void initReUDP(final Handler handler, final String toIp) {

        new Thread() {
            public void run() {
                try {
                    DatagramSocket mDataSocket = null;
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    if (mDataSocket == null) {
                        mDataSocket = new DatagramSocket(null);
                        mDataSocket.setReuseAddress(true);
                        mDataSocket.bind(new InetSocketAddress(10005));
                    }
                    while (true) {
                        mDataSocket.receive(dp);
                        XStream x = new XStream();
                        x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
                        ChatMessage fromXml = (ChatMessage) x.fromXML(new String(dp.getData()));
                        //TODO
                        if (toIp.equals(fromXml.fromIP)) {
                            Message msg = handler.obtainMessage();
                            msg.obj = fromXml;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static int getIpAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    public static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取本机IP
     *
     * @param context
     * @return
     */
    public static String getLocalIP(Context context) {
        int ipAddress = getIpAddress(context);
        String localIP = intToIp(ipAddress);
        return localIP;
    }
}