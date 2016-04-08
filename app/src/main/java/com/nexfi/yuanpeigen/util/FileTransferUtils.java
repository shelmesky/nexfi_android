package com.nexfi.yuanpeigen.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.ListView;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.thread.TcpFenDuanThread;
import com.nexfi.yuanpeigen.weight.ChatMessageAdapater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by gengbaolong on 2016/3/31.
 */
public class FileTransferUtils {
    public static final int REQUEST_CODE_SELECT_FILE = 1;
    public static final int REQUEST_CODE_LOCAL = 2;
    static ChatMessageAdapater mListViewAdapater;

    /**
     * 分段发送文件
     *
     * @param path
     */
    public static void sendFenDuanFile(final String path, final ChatActivity mContext, final String username, final int myAvatar, final String toIp, final String localIP, final int dynamicClientPort, final ListView lv, final List<ChatMessage> mDataArrays) {


        new Thread() {
            @Override
            public void run() {
                super.run();

                String fileName;
                long fileSize;
                Socket s = null;
                OutputStream out = null;

                String select_file_path = "";//发送端选择的文件的路径


                try {
                    s = new Socket(toIp, dynamicClientPort);
                    s.setSoTimeout(5000);
                    out = s.getOutputStream();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final File fileToSend = new File(path);

                fileSize = fileToSend.length();
                fileName = fileToSend.getName();
                String extensionName = FileUtils.getExtensionName(fileName);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.isPb = 1;//让进度条显示
                if (fileName.length() > 23) {
                    fileName = fileName.substring(0, 23) + "\n" + fileName.substring(23);
                }
                chatMessage.fileName = fileName;
                chatMessage.fileSize = fileSize;
                chatMessage.filePath = path;

                //设置文件图标
                FileUtils.setFileIcon(chatMessage, extensionName);

                //TODO 2016.3.24  14:40
                //发送文件时携带本机IP
                byte[] ipByte = new byte[16];
                byte[] localIpByte = localIP.getBytes();
                for (int i = 0; i < localIpByte.length; i++) {
                    ipByte[i] = localIpByte[i];
                }
                ipByte[localIpByte.length] = 0;
                try {
                    out.write(ipByte, 0, ipByte.length);//
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //文件名
                byte[] file = new byte[256];//定义字节数组用于存储文件名字大小
                byte[] tfile = fileToSend.getName().getBytes();
                for (int i = 0; i < tfile.length; i++) {
                    file[i] = tfile[i];
                }
                file[tfile.length] = 0;
                try {
                    out.write(file, 0, file.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //文件本身大小
                byte[] size = new byte[64];
                byte[] tsize = ("" + fileToSend.length()).getBytes();

                for (int i = 0; i < tsize.length; i++) {
                    size[i] = tsize[i];
                }

                size[tsize.length] = 0;
                try {
                    out.write(size, 0, size.length);//灏嗘枃浠跺ぇ灏忎紶鍒版帴鏀剁
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //读取文件的输入流
                FileInputStream fis = null;
                byte[] buf = new byte[1024 * 1024];
                try {
                    fis = new FileInputStream(fileToSend);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    //如果出现异常

                }
                int readsize = 0;
                //TODO
                chatMessage.fromAvatar = myAvatar;
                chatMessage.msgType = 2;
                chatMessage.toIP = toIp;
                chatMessage.fromIP = localIP;
                chatMessage.sendTime = FileUtils.getDateNow();
                chatMessage.fromNick = username;
                chatMessage.type = "chatP2P";
                //TODO 2016/3/25 9:50
                chatMessage.chat_id = toIp;
                mDataArrays.add(chatMessage);
                //TODO
                mListViewAdapater = new ChatMessageAdapater(mContext, mDataArrays);

                //发送开始就显示
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
                try {
                    while ((readsize = fis.read(buf, 0, buf.length)) > 0) {
                        out.write(buf, 0, readsize);
                        //等待一会
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        out.flush();
                    }
                    out.close();
                    s.close();
                    //TODO
                    //隐藏进度条
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
                    BuddyDao buddyDao = new BuddyDao(mContext);
                    buddyDao.addP2PMsg(chatMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //开启文件接收端
    public static void startServer(final int dynamicServerPort, final ChatActivity context, final String toIp, final int avatar, final ListView lv, final List<ChatMessage> mDataArrays, final ChatMessageAdapater mListViewAdapater) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ServerSocket serversock = null;  //监听端口
                try {
                    serversock = new ServerSocket(dynamicServerPort);
                    while (true) {
                        Socket sock = serversock.accept();            //循环等待客户端连接
                        new Thread(new TcpFenDuanThread(sock, context, toIp, avatar, lv, mDataArrays, mListViewAdapater)).start(); //当成功连接客户端后开启新线程接收文件
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 选择本地文件
     */
    public static void selectFileFromLocal(Activity context) {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        context.startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 从图库获取图片
     */
    public static void selectPicFromLocal(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        context.startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

}
