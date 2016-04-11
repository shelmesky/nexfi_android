package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.service.ReceService;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.util.UserInfo;
import com.nexfi.yuanpeigen.weight.MyGridViewAdapter;
import com.thoughtworks.xstream.XStream;


/**
 * Created by Mark on 2016/2/4.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, Runnable {
    private String ip;
    private String nick_name;
    private final String USERSEXMALE = "男";
    private final String USERSEXFEMALE = "女";
    private TextView tv_finish, tv_username;
    private Button btn_finish, btn_cancel;
    private LinearLayout set_userHeadIcon;
    private RelativeLayout layout_username;
    private RadioGroup radioGroup;
    private RadioButton rb_female, rb_male;
    private View View_pop;
    private PopupWindow mPopupWindow = null;
    private MyGridViewAdapter myGridViewAdapter;
    private GridView mGridView;
    private View view;
    private Handler handler;
    private AlertDialog mAlertDialog;
    private int avatar = R.mipmap.user_head_female_3;
    private ImageView iv_userheadIcon;
    private int[] userHeadFlagIcon = {R.mipmap.user_head_female_1, R.mipmap.user_head_female_2, R.mipmap.user_head_female_3, R.mipmap.user_head_female_4, R.mipmap.user_head_male_1, R.mipmap.user_head_male_2, R.mipmap.user_head_male_3, R.mipmap.user_head_male_4};
    private String model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        int ipAddress = getIpAddress();
        ip = intToIp(ipAddress);//本机IP
        UserInfo.saveIP(this, ip);
        initView();
        Intent intent = new Intent(LoginActivity.this, ReceService.class);
        startService(intent);
        wifiState();
        model = android.os.Build.MODEL;
        tv_username.setText(model);
    }

    private int getIpAddress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    private void wifiState() {
        if (!isWifiConnected(LoginActivity.this)) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View v = inflater.inflate(R.layout.dialog_wifi, null);
            mAlertDialog = new AlertDialog.Builder(this).create();
            mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
            mAlertDialog.show();
            mAlertDialog.getWindow().setContentView(v);
            mAlertDialog.setCancelable(false);
            handler = new Handler();
            handler.postDelayed(this, 1500);
        }
    }

    @Override
    public void run() {
        mAlertDialog.dismiss();
    }

    private void initView() {
        View_pop = LayoutInflater.from(this).inflate(R.layout.pop_userhead_select, null);
        mGridView = (GridView) View_pop.findViewById(R.id.gridView);
        btn_cancel = (Button) View_pop.findViewById(R.id.btn_cancel);
        myGridViewAdapter = new MyGridViewAdapter(this);
        mGridView.setAdapter(myGridViewAdapter);
        tv_finish = (TextView) findViewById(R.id.tv_finish);
        tv_username = (TextView) findViewById(R.id.tv_username);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        set_userHeadIcon = (LinearLayout) findViewById(R.id.set_userheadIcon);
        iv_userheadIcon = (ImageView) findViewById(R.id.iv_userhead_icon);
        layout_username = (RelativeLayout) findViewById(R.id.layout_username);
        radioGroup = (RadioGroup) findViewById(R.id.radioGrop);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_female.setChecked(true);
        view = findViewById(R.id.parent);
        rb_male = (RadioButton) findViewById(R.id.rb_male);
        ViewsetOnclickLisener();
        gridViewSetOnclickLisener();
        radioSetOnCheckedListener();
        UserInfo.saveUsersex(this, USERSEXFEMALE);
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPopupWindow.setFocusable(true);
        }
        mPopupWindow.showAsDropDown(view);
    }

    private void ViewsetOnclickLisener() {
        tv_finish.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        set_userHeadIcon.setOnClickListener(this);
        layout_username.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    private void radioSetOnCheckedListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_male:
                        UserInfo.saveUsersex(LoginActivity.this, USERSEXMALE);
                        Toast.makeText(LoginActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_female:
                        UserInfo.saveUsersex(LoginActivity.this, USERSEXFEMALE);
                        Toast.makeText(LoginActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void gridViewSetOnclickLisener() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                iv_userheadIcon.setImageResource(userHeadFlagIcon[position]);
                avatar = userHeadFlagIcon[position];
                UserInfo.saveUserHeadIcon(LoginActivity.this, avatar);
                mPopupWindow.dismiss();
            }
        });
    }


    public String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 判断ＷiFi是否打开
     */
    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finish:
                finishUsername();
                break;
            case R.id.tv_finish:
                finishUsername();
                break;
            case R.id.set_userheadIcon:
                initPop();
                break;
            case R.id.btn_cancel:
                mPopupWindow.dismiss();
                break;
            case R.id.layout_username:
                Intent intent = new Intent(LoginActivity.this, UsernameActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "您还未输入完信息哦", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            nick_name = data.getStringExtra("name");
            tv_username.setText(nick_name);
        }
    }

    public void finishUsername() {
        if ((nick_name != null)) {
            ChatMessage user = new ChatMessage();
            user.nick = nick_name;
            user.account = ip;
            user.avatar = avatar;
            user.type = "online";
            XStream x = new XStream();
            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
            String xml = x.toXML(user);
            SocketUtils.startSendThread(xml);//发送上线通知
            UserInfo.setConfigurationInformation(LoginActivity.this);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "昵称不能为空哦", Toast.LENGTH_SHORT).show();
        }
    }
}

