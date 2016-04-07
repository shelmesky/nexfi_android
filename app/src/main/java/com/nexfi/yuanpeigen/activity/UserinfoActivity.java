package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.util.UserInfo;
import com.nexfi.yuanpeigen.weight.MyGridViewAdapter;
import com.thoughtworks.xstream.XStream;

/**
 * Created by Mark on 2016/3/3.
 */
public class UserinfoActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_userheadIcon;
    private final String USERSEXMALE = "男";
    private final String USERSEXFEMALE = "女";
    private LinearLayout modify_userheadIcon;
    private RelativeLayout layout_username, iv_back;
    private RadioGroup radioGroup;
    private RadioButton rb_female, rb_male;
    private View View_pop;
    private PopupWindow mPopupWindow = null;
    private MyGridViewAdapter myGridViewAdapter;
    private GridView mGridView;
    private View view;
    private Button btn_cancel;
    private String userName, userSex, nick_name;
    private int userHeadIcon;
    private int avatar;
    private boolean isUserSex = false;
    private TextView tv_modifyUsername;
    private int[] userHeadFlagIcon = {R.mipmap.user_head_female_1, R.mipmap.user_head_female_2, R.mipmap.user_head_female_3, R.mipmap.user_head_female_4, R.mipmap.user_head_male_1, R.mipmap.user_head_male_2, R.mipmap.user_head_male_3, R.mipmap.user_head_male_4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        initView();
        initUsername();
        initUserSex();
        initUserHeadIcon();
        Intent intent = getIntent();
        isUserSex = intent.getBooleanExtra("userSex", true);
        nick_name = intent.getStringExtra("name");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            nick_name = data.getStringExtra("name");
            tv_modifyUsername.setText(nick_name);
        }
    }

    private void initUsername() {
        SharedPreferences preferences = getSharedPreferences("username", Context.MODE_PRIVATE);
        userName = preferences.getString("userName", "宝宝");
        tv_modifyUsername.setText(userName);
    }

    private void initUserHeadIcon() {
        SharedPreferences preferences = getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        userHeadIcon = preferences.getInt("userhead", R.mipmap.user_head_female_3);
        iv_userheadIcon.setImageResource(userHeadIcon);
    }

    private void initUserSex() {
        SharedPreferences preferences = getSharedPreferences("usersex", Context.MODE_PRIVATE);
        userSex = preferences.getString("userSex", "男");
        if (userSex.equals("女")) {
            rb_female.setChecked(true);
        } else {
            rb_male.setChecked(true);
        }
    }

    private void initView() {
        iv_back = (RelativeLayout) findViewById(R.id.iv_back);
        tv_modifyUsername = (TextView) findViewById(R.id.tv_modify_username);
        modify_userheadIcon = (LinearLayout) findViewById(R.id.modify_userheadIcon);
        layout_username = (RelativeLayout) findViewById(R.id.layout_username);
        radioGroup = (RadioGroup) findViewById(R.id.radioGrop);
        iv_userheadIcon = (ImageView) findViewById(R.id.iv_userhead_icon);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);
        View_pop = LayoutInflater.from(this).inflate(R.layout.pop_userhead_select, null);
        mGridView = (GridView) View_pop.findViewById(R.id.gridView);
        myGridViewAdapter = new MyGridViewAdapter(this);
        mGridView.setAdapter(myGridViewAdapter);
        btn_cancel = (Button) View_pop.findViewById(R.id.btn_cancel);
        radioGroup = (RadioGroup) findViewById(R.id.radioGrop);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_female.setChecked(true);
        view = findViewById(R.id.parent);
        rb_male = (RadioButton) findViewById(R.id.rb_male);
        gridViewSetOnclickLisener();
        radioSetOnCheckedListener();
        viewSetOnclickListener();
    }

    private void viewSetOnclickListener() {
        iv_back.setOnClickListener(this);
        modify_userheadIcon.setOnClickListener(this);
        layout_username.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    private void radioSetOnCheckedListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_male:
                        UserInfo.saveUsersex(UserinfoActivity.this, USERSEXMALE);
                        if (isUserSex) {
                            Toast.makeText(UserinfoActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.rb_female:
                        UserInfo.saveUsersex(UserinfoActivity.this, USERSEXFEMALE);
                        if (isUserSex) {
                            Toast.makeText(UserinfoActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        }
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
                UserInfo.saveUserHeadIcon(UserinfoActivity.this, userHeadFlagIcon[position]);
                avatar = userHeadFlagIcon[position];
                mPopupWindow.dismiss();
            }
        });
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPopupWindow.setFocusable(true);
        }
        mPopupWindow.showAsDropDown(view);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isUserInfo", false);
                intent.putExtra("isSettings", false);
                startActivity(intent);
                finish();
                sendUDP();
                break;
            case R.id.modify_userheadIcon:
                initPop();
                break;
            case R.id.layout_username:
                Intent intent1 = new Intent(UserinfoActivity.this, ModifyUsernameActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.btn_cancel:
                mPopupWindow.dismiss();
                break;
        }
    }

    public void sendUDP() {
        if (nick_name != null) {
            ChatMessage user = new ChatMessage();
            user.nick = nick_name;
            user.avatar = avatar;
//            String xml = user.toXml();
            XStream x = new XStream();
            x.alias(ChatMessage.class.getSimpleName(), ChatMessage.class);
            String xml =x.toXML(user);
            SocketUtils.startSendThread(xml);
            UserInfo.setConfigurationInformation(UserinfoActivity.this);
        }
    }
}
