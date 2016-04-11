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
 * Created by Mark on 2016/3/2.
 */
public class UsernameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView finish;
    private EditText et_username;
    private String username;
    private String model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        finish = (TextView) findViewById(R.id.tv_finish);
        et_username = (EditText) findViewById(R.id.et_username);
        initUsername();
        model = android.os.Build.MODEL;
        if (username != null) {
            et_username.setText(username);
        } else {
            et_username.setText(model);
        }
        finish.setOnClickListener(this);
    }

    private void initUsername() {
        SharedPreferences preferences = getSharedPreferences("username", Context.MODE_PRIVATE);
        username = preferences.getString("userName", null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_finish:
                if (!TextUtils.isEmpty(et_username.getText())) {
                    UserInfo.saveUsername(UsernameActivity.this, et_username.getText().toString());
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UsernameActivity.this, LoginActivity.class);
                    intent.putExtra("name", et_username.getText().toString());
                    setResult(2, intent);
                    UsernameActivity.this.finish();
                } else {
                    Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
