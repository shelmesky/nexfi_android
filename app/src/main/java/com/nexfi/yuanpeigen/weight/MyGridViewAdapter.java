package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/3/3.
 */
public class MyGridViewAdapter extends BaseAdapter {
    private Context context;
    private int[] userHeadIcon = {R.drawable.selector_female_1, R.drawable.selector_female_2, R.drawable.selector_female_3, R.drawable.selector_female_4, R.drawable.selector_male_1, R.drawable.selector_male_2, R.drawable.selector_male_3, R.drawable.selector_male_4};

    public MyGridViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return userHeadIcon.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        ImageView iv = new ImageView(context);
        iv.setImageResource(userHeadIcon[position]);
        linearLayout.addView(iv);
        return linearLayout;
    }
}
