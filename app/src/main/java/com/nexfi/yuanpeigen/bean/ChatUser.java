package com.nexfi.yuanpeigen.bean;

/**
 * Created by Mark on 2016/2/25.
 */

import java.io.Serializable;

public class ChatUser extends BaseChatMsg implements Serializable {
    private static final long serialVersionUID = 40L;
    public String account;//用户IP
    public String nick = "";//用户昵称
    public int avatar;//用户头像
//    public String type = "";//消息类型

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return account + "==" + nick + "==" + type;
    }
}
