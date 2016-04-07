package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.FileUtils;

import java.util.List;

/**
 * Created by Mark on 2016/2/17.
 */
public class ChatMessageAdapater extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ChatMessage> coll;
    private Context mContext;
    private static final int MESSAGE_TYPE_SEND_CHAT_CONTEXT = 0;
    private static final int MESSAGE_TYPE_RECV_CHAT_CONTEXT = 1;
    private static final int MESSAGE_TYPE_SEND_FOLDER = 2;
    private static final int MESSAGE_TYPE_RECV_FOLDER = 3;

    public ChatMessageAdapater(Context context, List<ChatMessage> coll) {
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }


    private static final String TAG = ChatMessageAdapater.class.getSimpleName();

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage entity = coll.get(position);
        switch (entity.msgType) {
            case MESSAGE_TYPE_SEND_CHAT_CONTEXT:
                return MESSAGE_TYPE_SEND_CHAT_CONTEXT;
            case MESSAGE_TYPE_RECV_CHAT_CONTEXT:
                return MESSAGE_TYPE_RECV_CHAT_CONTEXT;
            case MESSAGE_TYPE_SEND_FOLDER:
                return MESSAGE_TYPE_SEND_FOLDER;
            case MESSAGE_TYPE_RECV_FOLDER:
                return MESSAGE_TYPE_RECV_FOLDER;
        }
        return -1;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatMessage entity = coll.get(position);
        int msgType = entity.msgType;
        ViewHolder_chatSend viewHolder_chatSend = null;
        ViewHolder_chatReceive viewHolder_chatReceive = null;
        ViewHolder_sendFile viewHolder_sendFile = null;
        ViewHolder_ReceiveFile viewHolder_receiveFile = null;
        if (convertView == null) {
            switch (msgType) {
                case MESSAGE_TYPE_SEND_CHAT_CONTEXT:
                    viewHolder_chatSend = new ViewHolder_chatSend();
                    convertView = mInflater.inflate(R.layout.item_chatting_msg_send, null);
                    viewHolder_chatSend.tv_chatText_send = (TextView) convertView.findViewById(R.id.tv_chatText_send);
                    viewHolder_chatSend.tv_sendTime_send = (TextView) convertView.findViewById(R.id.tv_sendtime_send);
                    viewHolder_chatSend.iv_userhead_send_chat = (ImageView) convertView.findViewById(R.id.iv_userhead_send);
                    convertView.setTag(viewHolder_chatSend);
                    break;
                case MESSAGE_TYPE_RECV_CHAT_CONTEXT:
                    viewHolder_chatReceive = new ViewHolder_chatReceive();
                    convertView = mInflater.inflate(R.layout.item_chatting_msg_receive, null);
                    viewHolder_chatReceive.tv_chatText_receive = (TextView) convertView.findViewById(R.id.tv_chatText_receive);
                    viewHolder_chatReceive.tv_sendTime_receive = (TextView) convertView.findViewById(R.id.tv_sendtime_receive);
                    viewHolder_chatReceive.iv_userhead_receive_chat = (ImageView) convertView.findViewById(R.id.iv_userhead_receive);
                    convertView.setTag(viewHolder_chatReceive);
                    break;
                case MESSAGE_TYPE_SEND_FOLDER:
                    viewHolder_sendFile = new ViewHolder_sendFile();
                    convertView = mInflater.inflate(R.layout.item_send_file, null);
                    viewHolder_sendFile.tv_sendTime_send_folder = (TextView) convertView.findViewById(R.id.tv_sendTime_send_folder);
                    viewHolder_sendFile.iv_userhead_send_folder = (ImageView) convertView.findViewById(R.id.iv_userhead_send_folder);
                    viewHolder_sendFile.tv_file_name_send = (TextView) convertView.findViewById(R.id.tv_file_name_send);
                    viewHolder_sendFile.tv_size_send = (TextView) convertView.findViewById(R.id.tv_size_send);
                    viewHolder_sendFile.iv_icon_send = (ImageView) convertView.findViewById(R.id.iv_icon_send);
                    viewHolder_sendFile.pb_send = (ProgressBar) convertView.findViewById(R.id.pb_send);
                    viewHolder_sendFile.chatcontent_send = (RelativeLayout) convertView.findViewById(R.id.chatcontent_send);
                    convertView.setTag(viewHolder_sendFile);
                    break;
                case MESSAGE_TYPE_RECV_FOLDER:
                    viewHolder_receiveFile = new ViewHolder_ReceiveFile();
                    convertView = mInflater.inflate(R.layout.item_recevied_file, null);
                    viewHolder_receiveFile.tv_sendTime_receive_folder = (TextView) convertView.findViewById(R.id.tv_sendTime_receive_folder);
                    viewHolder_receiveFile.iv_userhead_receive_folder = (ImageView) convertView.findViewById(R.id.iv_userhead_receive_folder);
                    viewHolder_receiveFile.tv_file_name_receive = (TextView) convertView.findViewById(R.id.tv_file_name_receive);
                    viewHolder_receiveFile.tv_size_receive = (TextView) convertView.findViewById(R.id.tv_size_receive);
                    viewHolder_receiveFile.iv_icon_receive = (ImageView) convertView.findViewById(R.id.iv_icon_receive);
                    viewHolder_receiveFile.pb_receive = (ProgressBar) convertView.findViewById(R.id.pb_receive);
                    viewHolder_receiveFile.chatcontent_receive = (RelativeLayout) convertView.findViewById(R.id.chatcontent_receive);
                    convertView.setTag(viewHolder_receiveFile);
                    break;
            }
        } else {
            switch (msgType) {
                case MESSAGE_TYPE_SEND_CHAT_CONTEXT:
                    viewHolder_chatSend = (ViewHolder_chatSend) convertView.getTag();
                    break;
                case MESSAGE_TYPE_RECV_CHAT_CONTEXT:
                    viewHolder_chatReceive = (ViewHolder_chatReceive) convertView.getTag();
                    break;
                case MESSAGE_TYPE_SEND_FOLDER:
                    viewHolder_sendFile = (ViewHolder_sendFile) convertView.getTag();
                    break;
                case MESSAGE_TYPE_RECV_FOLDER:
                    viewHolder_receiveFile = (ViewHolder_ReceiveFile) convertView.getTag();
                    break;
            }

        }


        switch (msgType) {
            case MESSAGE_TYPE_SEND_CHAT_CONTEXT:
                viewHolder_chatSend.iv_userhead_send_chat.setImageResource(entity.fromAvatar);
                viewHolder_chatSend.tv_sendTime_send.setText(entity.sendTime);
                viewHolder_chatSend.tv_chatText_send.setText(entity.content);
                break;
            case MESSAGE_TYPE_RECV_CHAT_CONTEXT:
                viewHolder_chatReceive.iv_userhead_receive_chat.setImageResource(entity.fromAvatar);
                viewHolder_chatReceive.tv_sendTime_receive.setText(entity.sendTime);
                viewHolder_chatReceive.tv_chatText_receive.setText(entity.content);
                break;
            case MESSAGE_TYPE_SEND_FOLDER:
                viewHolder_sendFile.iv_userhead_send_folder.setImageResource(entity.fromAvatar);
                viewHolder_sendFile.tv_sendTime_send_folder.setText(entity.sendTime);
                viewHolder_sendFile.tv_file_name_send.setText(entity.fileName);
                String formatSize = android.text.format.Formatter.formatFileSize(mContext, entity.fileSize);
                viewHolder_sendFile.tv_size_send.setText(formatSize);
                viewHolder_sendFile.iv_icon_send.setImageResource(entity.fileIcon);
                viewHolder_sendFile.chatcontent_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = FileUtils.openFile(entity.filePath);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
                if (entity.isPb == 0) {
                    viewHolder_sendFile.pb_send.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder_sendFile.pb_send.setVisibility(View.VISIBLE);
                }
                break;
            case MESSAGE_TYPE_RECV_FOLDER:
                viewHolder_receiveFile.iv_userhead_receive_folder.setImageResource(entity.fromAvatar);
                viewHolder_receiveFile.tv_sendTime_receive_folder.setText(entity.sendTime);
                viewHolder_receiveFile.tv_file_name_receive.setText(entity.fileName);
                String formatSize2 = android.text.format.Formatter.formatFileSize(mContext, entity.fileSize);
                viewHolder_receiveFile.tv_size_receive.setText(formatSize2);
                viewHolder_receiveFile.iv_icon_receive.setImageResource(entity.fileIcon);
                //选择文件的打开方式
                viewHolder_receiveFile.chatcontent_receive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = FileUtils.openFile(entity.filePath);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
                if (entity.isPb == 0) {
                    viewHolder_receiveFile.pb_receive.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder_receiveFile.pb_receive.setVisibility(View.VISIBLE);
                }
                break;
        }
        return convertView;
    }


    static class ViewHolder_chatSend {
        public TextView tv_chatText_send, tv_sendTime_send;
        public ImageView iv_userhead_send_chat;
    }

    static class ViewHolder_chatReceive {
        public TextView tv_chatText_receive, tv_sendTime_receive;
        public ImageView iv_userhead_receive_chat;
    }

    static class ViewHolder_sendFile {
        public TextView tv_sendTime_send_folder, tv_size_send, tv_file_name_send;
        public ImageView iv_userhead_send_folder, iv_icon_send;
        public RelativeLayout chatcontent_send;
        public ProgressBar pb_send;
    }

    static class ViewHolder_ReceiveFile {
        public TextView tv_sendTime_receive_folder, tv_size_receive, tv_file_name_receive;
        public ImageView iv_userhead_receive_folder, iv_icon_receive;
        public ProgressBar pb_receive;
        public RelativeLayout chatcontent_receive;
    }

}