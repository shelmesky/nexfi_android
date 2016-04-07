package com.nexfi.yuanpeigen.bean;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 35L;

    public String account;//用户IP
    public String nick = "";//用户昵称
    public int avatar;//用户头像

    public String fromIP;
    public String fromNick;
    public int fromAvatar;
    public String content;
    public String toIP;
    public String type;//聊天消息类型：单聊，群聊
    public int msgType;
    public String sendTime;
    //TODO
    public String fileName;//文件名
    public long fileSize;//文件大小
    public int fileIcon;//文件图标
    public int isPb;//进度条显示隐藏标记
    public String filePath;//文件路径
    //
    public String chat_id;

    //标记
    public String uuid;

    @Override
    public String toString() {
        return nick+"----------------"+account+"------------"+toIP+"--------------"+fileName;
    }
}
