package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nexfi.yuanpeigen.activity.AboutActivity;
import com.nexfi.yuanpeigen.activity.UserinfoActivity;
import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/4.
 */
public class Fragment_settings extends Fragment implements View.OnClickListener {

    private RelativeLayout modify_userinfo, about, net_settings;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        modify_userinfo = (RelativeLayout) view.findViewById(R.id.modify_userInfo);
        about = (RelativeLayout) view.findViewById(R.id.about);
        net_settings = (RelativeLayout) view.findViewById(R.id.net_settings);
        net_settings.setOnClickListener(this);
        modify_userinfo.setOnClickListener(this);
        about.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                getActivity().finish();
                break;
            case R.id.modify_userInfo:
                Intent intent = new Intent(getActivity(), UserinfoActivity.class);
                intent.putExtra("userSex", true);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.net_settings:
                MyFragmentDialog myFragmentDialog = new MyFragmentDialog();
                myFragmentDialog.show(getFragmentManager(), "netDialog");
                break;
        }
    }

}
