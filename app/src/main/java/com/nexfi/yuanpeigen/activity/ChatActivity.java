package com.nexfi.yuanpeigen.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.FileTransferUtils;
import com.nexfi.yuanpeigen.util.FileUtils;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.weight.ChatMessageAdapater;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Mark on 2016/2/17.
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    MyApplication app=new MyApplication();//application

    private ListView lv;
    private Button sendMsg;
    private PopupWindow mPopupWindow = null;
    private ImageView iv, iv_add, iv_chatRoom, iv_pic, iv_photo, iv_folder;
    private View View_pop;
    private EditText editText, et_chat;
    private LinearLayout modify_name, release, layout_view;
    private Dialog mDialog_modify, mDialog_remove;
    private RelativeLayout back;
    private ChatMessageAdapater mListViewAdapater;
    private String toIp, username, localIP;
    private int avatar, myAvatar;
    private TextView nick;
    private DatagramSocket mDataSocket;
    private boolean visibility_Flag = false;
    public static final int REQUEST_CODE_SELECT_FILE = 1;//文件
    public static final int REQUEST_CODE_LOCAL=2;//图片
    private String fileName;
    private long fileSize;
    private String select_file_path = "";//发送端选择的文件的路径
    private String rece_file_path = "";//接收端文件的保存路径

    int dynamicClientPort = 0;
    int dynamicServerPort = 0;




    /**
     * 数据
     */
    public List<ChatMessage> mDataArrays = app.mDataArrays;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    receive((ChatMessage) msg.obj);
                }
            }
        }
    };


    private void initmyAvatar() {
        SharedPreferences preferences = getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        myAvatar = preferences.getInt("userhead", R.mipmap.user_head_female_3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        toIp = intent.getStringExtra("8");
        username = intent.getStringExtra("1");
        avatar = intent.getIntExtra("3", R.mipmap.user_head_female_1);
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);

        dynamicClientPort = 10000 + Integer.parseInt(FileUtils.splitIP(localIP));
        dynamicServerPort = 10000 + Integer.parseInt(FileUtils.splitIP(toIp));

        initView();
        SocketUtils.initReUDP(handler, toIp);//初始化UDP接收端

        startServer();
        setAdapter();
        setOnClickListener();
        initmyAvatar();
        //监听数据库的变化
        this.getContentResolver().registerContentObserver(
                Uri.parse("content://www.file_send"), true,
                new Myobserve(new Handler()));

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter();
                        }
                    });
                }
            }.start();
            super.onChange(selfChange);
        }
    }

    //开启接收端
    private void startServer() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                ServerSocket serversock = null;  //监听端口
                try {
                    serversock = new ServerSocket(dynamicServerPort);
                    while (true) {
                        Socket sock = serversock.accept();            //循环等待客户端连接
//                        new Thread(new TcpFenDuanThread(sock,ChatActivity.this,toIp,avatar,lv,mDataArrays)).start(); //当成功连接客户端后开启新线程接收文件
                        new Thread(new TcpFenDuanThread(sock)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //分段接收线程
    class TcpFenDuanThread implements Runnable {
        private Socket s;
        InputStream in = null;

        TcpFenDuanThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            System.out.println(s.getInetAddress().getHostAddress() + "-----ip");
            try {
                in = s.getInputStream();
                byte[] local = new byte[16];
                in.read(local);
                String local_ip = new String(local).trim();//ip
                Log.e("TAG", local_ip + "=========================local_ip--------------========================================");

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
                    //TODO
                    mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);

                    runOnUiThread(new Runnable() {
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
                            fos.flush();
                        } else {
                            break;
                        }
                    }
                    fos.close();
                    in.close();
                    s.close();
                    Log.e("TAG", file_name + "---------------------------文件接收完毕===================");
                    chatMessage.isPb = 0;

                    mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);

                    //发送完毕就隐藏
                    runOnUiThread(new Runnable() {
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
                    BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
                    buddyDao.addP2PMsg(chatMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 分段发送文件
     *
     * @param path
     */
    public void sendFenDuanFile(final String path) {


        new Thread(){
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
                mListViewAdapater = new ChatMessageAdapater(ChatActivity.this, mDataArrays);

                //发送开始就显示
                runOnUiThread(new Runnable() {
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
                    mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);
                    //发送完毕就隐藏
                    runOnUiThread(new Runnable() {
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
                    BuddyDao buddyDao = new BuddyDao(getApplicationContext());
                    buddyDao.addP2PMsg(chatMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FILE) { //选择文件
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    select_file_path = FileUtils.getPath(this, uri);
//                    sendFenDuanFile(select_file_path);
                    FileTransferUtils.sendFenDuanFile(select_file_path,ChatActivity.this,username,myAvatar,toIp,localIP,dynamicClientPort,lv,mDataArrays);
                }
            }
        }else if(requestCode==REQUEST_CODE_LOCAL){
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    final String selectPath = FileUtils.getPath(this, selectedImage);
                    FileTransferUtils.sendFenDuanFile(selectPath,ChatActivity.this,username,myAvatar,toIp,localIP,dynamicClientPort,lv,mDataArrays);
                }
            }
        }
    }

    private void initView() {
        View_pop = LayoutInflater.from(this).inflate(R.layout.pop_menu, null);
        modify_name = (LinearLayout) View_pop.findViewById(R.id.modify_name);
        back = (RelativeLayout) findViewById(R.id.iv_back);
        release = (LinearLayout) View_pop.findViewById(R.id.release_connection);
        iv = (ImageView) findViewById(R.id.iv_man);
        lv = (ListView) findViewById(R.id.lv_chat);
        iv_add = (ImageView) findViewById(R.id.iv_add);
        et_chat = (EditText) findViewById(R.id.et_chat);
        layout_view = (LinearLayout) findViewById(R.id.layout_view);
        nick = (TextView) findViewById(R.id.textView);
        nick.setText(username);
        sendMsg = (Button) findViewById(R.id.btn_sendMsg);
        iv_chatRoom = (ImageView) findViewById(R.id.iv_chatRoom);
        iv_folder = (ImageView) findViewById(R.id.iv_folder);
        iv_photo = (ImageView) findViewById(R.id.iv_photograph);
        iv_pic = (ImageView) findViewById(R.id.iv_pic);
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatActivity.this);

        mDataArrays = buddyDao.findMsgByChatId(toIp);//根据会话id查询数据库中单对单聊天信息

        mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);
        lv.setAdapter(mListViewAdapater);
    }


    private void receive(ChatMessage chatMessage) {
        chatMessage.fromAvatar = avatar;
        chatMessage.msgType = 1;
        //TODO 2016/3/24 22:30
        chatMessage.chat_id = chatMessage.fromIP;//接收的时候把fromIP作为会话ID
        BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
        buddyDao.addP2PMsg(chatMessage);
        mDataArrays.add(chatMessage);
        /**
         * 再次创建Adapater对象
         * */
        mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);
        lv.setAdapter(mListViewAdapater);
        mListViewAdapater.notifyDataSetChanged();
        lv.setSelection(lv.getCount() - 1);
    }

    private void send() {
        final String contString = et_chat.getText().toString();
        if (contString.length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.fromAvatar = myAvatar;
            chatMessage.msgType = 0;
            chatMessage.toIP = toIp;
            chatMessage.fromIP = localIP;
            chatMessage.sendTime = FileUtils.getDateNow();
            chatMessage.content = contString;
            chatMessage.fromNick = username;
            chatMessage.type = "chatP2P";
            //TODO 2016/3/24 22:20
            chatMessage.chat_id = toIp;//发送的时候把toIp作为会话id
            XStream x = new XStream();
            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
            String xml =x.toXML(chatMessage);
            SocketUtils.sendUDP(toIp, xml);//发送单播
            BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
            buddyDao.addP2PMsg(chatMessage);
            mDataArrays.add(chatMessage);
            /**
             * 再次创建Adapater对象
             * */
            mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);
            lv.setAdapter(mListViewAdapater);
            mListViewAdapater.notifyDataSetChanged();
            lv.setSelection(lv.getCount() - 1);
        }
    }


    private void setOnClickListener() {
        iv.setOnClickListener(this);
        modify_name.setOnClickListener(this);
        release.setOnClickListener(this);
        back.setOnClickListener(this);
        sendMsg.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        iv_chatRoom.setOnClickListener(this);
        iv_folder.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        iv_photo.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        intent.putExtra("Dialog", false);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_man:
                initPop();
                break;
            case R.id.iv_add:
                if (visibility_Flag) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                break;
            case R.id.modify_name:
                initDialog_modify();
                break;
            case R.id.release_connection:
                initDialog_remove();
                break;
            case R.id.iv_back:
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.putExtra("Dialog", false);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_chatRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_photograph:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_folder:
                /**
                 * 发送文件
                 * */
                FileTransferUtils.selectFileFromLocal(this);
                break;
            case R.id.iv_pic://选择图片
                FileTransferUtils.selectPicFromLocal(this);
                break;
            case R.id.btn_sendMsg:
                send();
                et_chat.setText(null);
                break;
        }
    }

    private void initDialog_remove() {
        mDialog_remove = new Dialog(ChatActivity.this);
        mDialog_remove.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog_remove.show();
        Window win = mDialog_remove.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        mDialog_remove.getWindow().setContentView(R.layout.dialog_remove);
        mDialog_remove.setCancelable(false);

        /**设置与谁解除连接*/
        TextView textView = (TextView) mDialog_remove.getWindow().findViewById(R.id.tv_remove);
        Button btn_ensure = (Button) mDialog_remove.getWindow().findViewById(R.id.btn_ensure_remove);
        Button btn_cancel = (Button) mDialog_remove.getWindow().findViewById(R.id.btn_cancel_remove);
        btn_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "成功解除连接", Toast.LENGTH_SHORT).show();
                mDialog_remove.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog_remove.dismiss();
            }
        });
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv, 0, 0);
    }

    private void initDialog_modify() {
        mDialog_modify = new Dialog(this);
        mDialog_modify.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog_modify.show();
        Window win = mDialog_modify.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        mDialog_modify.getWindow().setContentView(R.layout.dialog_modifyremarks);
        Button btn_cancel = (Button) mDialog_modify.getWindow().findViewById(R.id.btn_cancel_remarks);
        editText = (EditText) mDialog_modify.getWindow().findViewById(R.id.et_remarks);
        Button btn_ensure = (Button) mDialog_modify.getWindow().findViewById(R.id.btn_ensure_remarks);
        btn_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    saveUserInfo(ChatActivity.this, editText.getText().toString(), null);
                    mDialog_modify.dismiss();
                } else {
                    Toast.makeText(ChatActivity.this, "备注不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog_modify.dismiss();
            }
        });
    }

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
}

