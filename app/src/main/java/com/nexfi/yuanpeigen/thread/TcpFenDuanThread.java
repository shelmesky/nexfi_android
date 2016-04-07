package com.nexfi.yuanpeigen.thread;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.activity.MainActivity;
import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.util.FileUtils;
import com.nexfi.yuanpeigen.weight.ChatMessageAdapater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengbaolong on 2016/3/31.
 */
//分段接收线程
public class TcpFenDuanThread implements Runnable {
    static MyApplication app=new MyApplication();
    private Socket s;
    InputStream in = null;
    ChatActivity mContext;
    String toIp;//发送到哪一个IP
    int avatar;
    ListView lv;
    private String rece_file_path = "";//接收端文件的保存路径
    private ChatMessageAdapater mListViewAdapater;
    List<ChatMessage> mDataArrays;

    public TcpFenDuanThread(Socket s,ChatActivity context,String toIp,int avatar,ListView lv,List<ChatMessage> mDataArrays,ChatMessageAdapater mListViewAdapater) {
        this.s = s;
        this.mContext=context;
        this.toIp=toIp;
        this.avatar=avatar;
        this.lv=lv;
        this.mDataArrays=mDataArrays;
        this.mListViewAdapater=mListViewAdapater;
        if(app.DEBUG){
            Log.e("TAG", mDataArrays.size() + "---------------------------集合长度===================");
            for (ChatMessage msg:mDataArrays) {
                Log.e("TAG", msg.toString() + "---------------------------集合长度===================");
            }
        }
    }

    @Override
    public void run() {
        try {
            in = s.getInputStream();
            byte[] local = new byte[16];
            in.read(local);
            String local_ip = new String(local).trim();//ip

            if (toIp.equals(local_ip)) {
                byte[] filename2 = new byte[256];//
                int leng = 0;
                while (leng < filename2.length) {
                    leng += in.read(filename2, leng, filename2.length - leng);//
                }
                String filename = new String(filename2, 0, leng);
                Log.e("TAG", filename + "=========================filename2--------------========================================");

                String file_name = new String(filename).trim();//文件名
                File fileDir = new File(Environment.getExternalStorageDirectory().getPath() + "/NexFi");
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                rece_file_path = fileDir + "/" + file_name;
                File fileout = new File(rece_file_path);
                FileOutputStream fos = new FileOutputStream(fileout);

                byte[] filesize = new byte[64];
                int b = 0;
                while (b < filesize.length) {
                    b += in.read(filesize, b, filesize.length - b);
                }
                int ends = 0;
                for (int i = 0; i < filesize.length; i++) {
                    if (filesize[i] == 0) {
                        ends = i;
                        break;
                    }
                }
                String filesizes = new String(filesize, 0, ends);
                int ta = Integer.parseInt(filesizes);//
                final long fileSize = filesize.length;
                //文件扩展名
                final String extensionName = FileUtils.getExtensionName(file_name);
                ChatMessage chatMessage = new ChatMessage();
                //设置文件接收路径
                chatMessage.filePath = rece_file_path;
                //设置文件图标
                FileUtils.setFileIcon(chatMessage, extensionName);
                //文件大小
                final int finalTa = ta;
                chatMessage.isPb = 1;


                //TODO
                /**
                 * 文件接收
                 * */
                chatMessage.fromAvatar = avatar;
                chatMessage.msgType = 3;
                if (file_name.length() > 23) {
                    file_name = file_name.substring(0, 23) + "\n" + file_name.substring(23);
                }
                chatMessage.fileName = file_name;
                chatMessage.fileSize = finalTa;
                chatMessage.sendTime = FileUtils.getDateNow();
                //TODO 2016/3/25 10:00
                chatMessage.chat_id = local_ip;
                mDataArrays.add(chatMessage);
                if(app.DEBUG){
                    Log.e("TAG", mDataArrays.size() + "---------------------------集合长度1===================");
                    for (ChatMessage msg:mDataArrays) {
                        Log.e("TAG", msg.toString() + "---------------------------集合长度1===================");
                    }
                }
                //TODO
                mListViewAdapater = new ChatMessageAdapater(mContext, mDataArrays);


                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv.setAdapter(mListViewAdapater);
                        if (mListViewAdapater != null) {
                            mListViewAdapater.notifyDataSetChanged();
                        }
                        if (mDataArrays.size() > 0) {
                            lv.setSelection(lv.getCount() - 1);
                        }
                    }
                });
                byte[] buf = new byte[1024 * 1024];
                //循环接收
                while (true) {
                    if (ta == 0) {
                        break;
                    }
                    int len = ta;
                    if (len > buf.length) {
                        len = buf.length;
                    }
                    int rlen = in.read(buf, 0, len);
                    ta -= rlen;
                    if (rlen > 0) {
                        fos.write(buf, 0, rlen);
//                        if(app.DEBUG){
//                            Log.e("TAG", rlen + "---------------------------receive===================");
//                        }
                        fos.flush();
                    } else {
                        break;
                    }
                }
                fos.close();
                in.close();
                s.close();
                if(app.DEBUG){
                    Log.e("TAG", file_name + "---------------------------文件接收完毕===================");
                }
                chatMessage.isPb = 0;
                mListViewAdapater = new ChatMessageAdapater(mContext, mDataArrays);
                //发送完毕就隐藏
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv.setAdapter(mListViewAdapater);
                        if (mListViewAdapater != null) {
                            mListViewAdapater.notifyDataSetChanged();
                        }
                        if (mDataArrays.size() > 0) {
                            lv.setSelection(lv.getCount() - 1);
                        }
                    }
                });
                if(app.DEBUG){
                    Log.e("TAG", mDataArrays.size() + "---------------------------集合长度2===================");
                    for (ChatMessage msg:mDataArrays) {
                        Log.e("TAG", msg.toString() + "---------------------------集合长度2===================");
                    }
                }
                BuddyDao buddyDao = new BuddyDao(mContext);
                buddyDao.addP2PMsg(chatMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

