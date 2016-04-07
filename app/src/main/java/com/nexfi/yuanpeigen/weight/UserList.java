package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.nexfi.R;

import java.util.List;

/**
 * Created by Mark on 2016/4/2.
 */
public class UserList extends BaseExpandableListAdapter {
    private LayoutInflater inflater;
    private List<ChatMessage> userinfosNew;
    private List<String> groupList;
    private List<ChatMessage> userinfosOnline;
    private List<ChatMessage> userinfosOffilne;
    private List<List<ChatMessage>> childListNew;
    private List<List<ChatMessage>> childListOnline;
    private List<List<ChatMessage>> childListOffline;

    public UserList(Context context, List<ChatMessage> userinfosNew, List<ChatMessage> userinfosOnline, List<ChatMessage> userinfosOffilne, List<String> groupList, List<List<ChatMessage>> childListNew, List<List<ChatMessage>> childListOnline, List<List<ChatMessage>> childListOffline) {
        this.userinfosNew = userinfosNew;
        this.userinfosOffilne = userinfosOffilne;
        this.userinfosOnline = userinfosOnline;
        inflater = LayoutInflater.from(context);
        this.groupList = groupList;
        this.childListNew = childListNew;
        this.childListOnline = childListOnline;
        this.childListOffline = childListOffline;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == 0) {
            return childListOnline.get(groupPosition).size();
        } else if (groupPosition == 1) {
            return childListOffline.get(groupPosition).size();
        }
        return childListNew.get(groupPosition).size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == 0) {
            return childListOnline.get(groupPosition).get(childPosition);
        } else if (groupPosition == 1) {
            return childListOffline.get(groupPosition).get(childPosition);
        }
        return childListNew.get(groupPosition).get(childPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.new_group, null);
        TextView tv_parent = (TextView) view.findViewById(R.id.tv_parent);
        tv_parent.setText(groupList.get(groupPosition));
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition == 2) {
            final ChatMessage entity = userinfosNew.get(childPosition);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.new_child, null);
                holder = new ViewHolder();
                holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username_new);
                holder.iv_user = (ImageView) convertView.findViewById(R.id.iv_user);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_username.setText(entity.nick);
            holder.iv_user.setImageResource(entity.avatar);
        } else if (groupPosition == 0) {
            final ChatMessage entity = userinfosOnline.get(childPosition);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.new_child, null);
                holder = new ViewHolder();
                holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username_new);
                holder.iv_user = (ImageView) convertView.findViewById(R.id.iv_user);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_username.setText(entity.nick);
            holder.iv_user.setImageResource(entity.avatar);
        } else if (groupPosition == 1) {
            final ChatMessage entity = userinfosOffilne.get(childPosition);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.new_child, null);
                holder = new ViewHolder();
                holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username_new);
                holder.iv_user = (ImageView) convertView.findViewById(R.id.iv_user);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_username.setText(entity.nick);
            holder.iv_user.setImageResource(entity.avatar);
        }
        return convertView;
    }

    class ViewHolder {
        public TextView tv_username;
        public ImageView iv_user;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
