package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.UserInfo;

/**
 * Created by Mark on 2016/3/3.
 */
public class ModifyUsernameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView cancel, finish;
    private EditText modifyUsername;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_username);
        initUsername();
        initView();
        modifyUsername.setText(username);
        setOnclicklistener();
    }

    private void initUsername() {
        SharedPreferences preferences = getSharedPreferences("username", Context.MODE_PRIVATE);
        username = preferences.getString("userName", "宝宝");
    }

    private void setOnclicklistener() {
        cancel.setOnClickListener(this);
        finish.setOnClickListener(this);
    }

    private void initView() {
        cancel = (TextView) findViewById(R.id.tv_cancel);
        finish = (TextView) findViewById(R.id.tv_finish);
        modifyUsername = (EditText) findViewById(R.id.et_modify_username);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                Intent intent2 = new Intent(ModifyUsernameActivity.this, UserinfoActivity.class);
                startActivity(intent2);
                ModifyUsernameActivity.this.finish();
                break;
            case R.id.tv_finish:
                if (!TextUtils.isEmpty(modifyUsername.getText())) {
                    UserInfo.saveUsername(ModifyUsernameActivity.this, modifyUsername.getText().toString());
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ModifyUsernameActivity.this, UserinfoActivity.class);
                    intent.putExtra("name", modifyUsername.getText().toString());
                    startActivityForResult(intent, 1);
                    ModifyUsernameActivity.this.finish();
                } else {
                    Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

