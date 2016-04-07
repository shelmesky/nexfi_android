
package com.nexfi.yuanpeigen.bean;


public class Userinfo {
    private static final String TAG = Userinfo.class.getSimpleName();

    private String username;

    private String ip;

    private int userhead;

    private String type;


    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }


    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }


    public int getUserHead() {
        return userhead;
    }

    public void setUserHead() {
        this.userhead = userhead;
    }

    public String getType() {
        return type;
    }

    public void setType(String text) {
        this.type = type;
    }


    public Userinfo() {
    }

    public Userinfo(String username, String ip, int userhead, String type) {
        super();
        this.username = username;
        this.ip = ip;
        this.userhead = userhead;
        this.type = type;
    }

}
