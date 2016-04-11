package com.nexfi.yuanpeigen.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/15.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back_about;
    private LinearLayout update;
    private Dialog mDialog;
    private TextView mButton_ensure, mButton_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        back_about = (RelativeLayout) findViewById(R.id.back_about);
        update = (LinearLayout) findViewById(R.id.update);
        update.setOnClickListener(this);
        back_about.setOnClickListener(this);
    }


    private void initDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_update, null);
        mDialog = new AlertDialog.Builder(AboutActivity.this).create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        mDialog.show();
        mDialog.getWindow().setContentView(v);
        mButton_ensure = (TextView) v.findViewById(R.id.btn_ensure2);
        mButton_cancel = (TextView) v.findViewById(R.id.btn_cancel2);
        mButton_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mButton_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_about:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isAbout", false);
                startActivity(intent);
                finish();
                break;
            case R.id.update:
                initDialog();
                break;
        }
    }
}
