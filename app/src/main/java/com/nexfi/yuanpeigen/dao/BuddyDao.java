package com.nexfi.yuanpeigen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.db.BuddyHelper;

import java.util.ArrayList;
import java.util.List;

public class BuddyDao {
    //閹垮秳缍旈弫鐗堝祦鎼?
    private Context context;
    BuddyHelper helper;

    public BuddyDao(Context context) {
        this.context = context;
        helper = new BuddyHelper(context);
    }


    /**
     * 添加用户
     *
     * @param
     */
//    public void add(ChatUser user) {
//        SQLiteDatabase db = helper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("contact_ip", user.account);
//        values.put("nick_name", user.nick);
//        values.put("avatar", user.avatar);
//        values.put("type", user.type);
//        db.insert("long", null, values);
//        db.close();
//        context.getContentResolver().notifyChange(
//                Uri.parse("content://www.nexfi.com"), null);
//    }


    /**
     * 根据IP查找对应的用户
     * @param localIp
     * @return
     */
    public ChatMessage findUserByIp(String localIp){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("messageBase2", null, "contact_ip=?", new String[]{localIp}, null, null, null);
        if (cursor.moveToNext()){
            ChatMessage user = new ChatMessage();
            user.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            user.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            user.type = cursor.getString(cursor.getColumnIndex("type"));
            user.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            Log.e("TAG",user.nick+"-------findUserByIp-------"+user.account);
            if(!("".equals(user.nick))){
                return user;
            }
        }
        return null;
    }

    /**
     * 根据fromIP和toIP查询数据库中的消息记录
     *
     * @param fromIP
     * @param toIP
     * @return
     */
    public boolean findMsgByToIp(String fromIP, String toIP) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("messageBase2", null, "fromIP=? and toIP=?", new String[]{fromIP, toIP}, null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }


    /**
     * 添加单聊消息到数据库
     */
    public void addP2PMsg(ChatMessage msg) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fromIP", msg.fromIP);
        values.put("fromNick", msg.fromNick);
        values.put("fromAvatar", msg.fromAvatar);
        values.put("content", msg.content);
        values.put("toIP", msg.toIP);
        values.put("type", msg.type);//共有的类型
        values.put("msgType", msg.msgType);
        values.put("sendTime", msg.sendTime);
        //TODO
        values.put("fileName", msg.fileName);
        values.put("fileSize", msg.fileSize);
        values.put("fileIcon", msg.fileIcon);
        values.put("isPb", msg.isPb);
        values.put("filePath", msg.filePath);
        values.put("chat_id", msg.chat_id);
        //TODO
        values.put("contact_ip", msg.account);
        values.put("nick_name", msg.nick);
        values.put("avatar", msg.avatar);
        values.put("uuid",msg.uuid);//标志
        db.insert("messageBase2", null, values);
        db.close();
        Log.e("TAG",msg.nick+"----add----"+msg.filePath+"-----add-----"+msg.account);
        if(("".equals(msg.nick))){//如果昵称是空串，说明不是用户登录
            context.getContentResolver().notifyChange(
                    Uri.parse("content://www.file_send"), null);
        }else{
            context.getContentResolver().notifyChange(
                    Uri.parse("content://www.nexfi.com"), null);//content://www.file_send
        }

    }


    /**
     * 添加群聊消息到数据库
     */
    public void addRoomMsg(ChatMessage msg) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fromIP", msg.fromIP);
        values.put("fromNick", msg.fromNick);
        values.put("fromAvatar", msg.fromAvatar);
        values.put("content", msg.content);
        values.put("toIP", msg.toIP);
        values.put("type", msg.type);
        values.put("msgType", msg.msgType);
        values.put("sendTime", msg.sendTime);
        //TODO
        values.put("fileName", msg.fileName);
        values.put("fileSize", msg.fileSize);
        values.put("fileIcon", msg.fileIcon);
        values.put("isPb", msg.isPb);
        values.put("filePath", msg.filePath);
        //TODO
        values.put("contact_ip", msg.account);
        values.put("nick_name", msg.nick);
        values.put("avatar", msg.avatar);
        values.put("uuid",msg.uuid);//标志
        db.insert("chatRoomBaseMsg2", null, values);
        db.close();
    }


    public boolean findRoomMsgByUuid(String uuid){
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("chatRoomBaseMsg2", null, "uuid = ?", new String[]{uuid}, null, null, null);

        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }



    /**
     * 根据IP和类型查找是否有相同数据
     * @param contact_ip
     * @param type
     * @return
     */
    public boolean findSame(String contact_ip,String type){
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("chatRoomBaseMsg2", null, "contact_ip = ? and type=?", new String[]{contact_ip,type}, null, null, null);

        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }


    /**
     * 根据IP删除
     */
    public void delete(String contact_ip) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("messageBase2", "contact_ip = ?",
                new String[]{contact_ip});
        db.close();
    }


    /**
     * 根据IP删除单聊聊天信息
     */
    public void deleteP2PMsg(String fromIP) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("messageBase2", "fromIP = ?",
                new String[]{fromIP});
        db.close();
    }


    /**
     * 根据IP删除群聊聊天信息
     */
    public void deleteRoomMsg(String fromIP) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("chatRoomBaseMsg1", "fromIP = ?",
                new String[]{fromIP});
        db.close();
    }


    //删除所有用户信息
//    public void deleteAll() {
//        SQLiteDatabase db = helper.getWritableDatabase();
//        int row = db.delete("long", null, null);
//        db.close();
//    }


    //删除所有单聊聊天信息
    public void deleteP2PMsgAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("messageBase2", null, null);
        db.close();
    }


    //删除所有群聊聊天信息
    public void deleteRoomMsgAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("chatRoomBaseMsg2", null, null);
        db.close();
    }


    /**
     * 查找所有用户
     *
     * @return
     */
    public List<ChatMessage> findAll(String localIp) {
//        Log.e("TAG",localIp+"==============localIp-----------------------------------------------dao");
        Log.e("TAG",helper.toString()+"--------helper----------------------------------------------------");
        SQLiteDatabase db = helper.getWritableDatabase();//报空指针
        Log.e("TAG",db+"--------db--------------------------------------------------------------------------db-----");
        Cursor cursor = db.query("messageBase2", null, null, null, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        List<ChatMessage> mList = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            Log.e("TAG",cursor+"--------cursor--------------------------------------------------------------------------cursor-----");
            ChatMessage user = new ChatMessage();
            user.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            user.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            user.type = cursor.getString(cursor.getColumnIndex("type"));
            user.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            if(!(localIp.equals(user.account))){
                if(!("".equals(user.nick))){
                    Log.e("TAG","--------findAll--------------------------------------------------------------------------add-----");
                    mList.add(user);
                }
            }
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        Log.e("TAG", mDatas.size() + "--------mDatas.size() --------------------------------------------------------------------------mDatas.size() -----");
        return mDatas;
    }


    /**
     * 查找所有单聊聊天信息
     *
     * @return
     */
    public List<ChatMessage> findP2PMsgAll() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("messageBase2", null, null, null, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        List<ChatMessage> mList = new ArrayList<ChatMessage>();
        List<ChatMessage> mUserDatas = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            ChatMessage msg = new ChatMessage();
            msg.fromIP = cursor.getString(cursor.getColumnIndex("fromIP"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            msg.fromAvatar = cursor.getInt(cursor.getColumnIndex("fromAvatar"));
            msg.toIP = cursor.getString(cursor.getColumnIndex("toIP"));
            msg.msgType = cursor.getInt(cursor.getColumnIndex("msgType"));
            msg.sendTime = cursor.getString(cursor.getColumnIndex("sendTime"));
            //TODO
            msg.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            msg.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
            msg.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            msg.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            msg.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            //TODO 2016/4/5
            msg.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            msg.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            msg.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            msg.uuid=cursor.getString(cursor.getColumnIndex("uuid"));
            mList.add(msg);
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    /**
     * 根据会话id查找对应的单对单聊天记录
     *
     * @param chat_id
     * @return
     */
    public List<ChatMessage> findMsgByChatId(String chat_id) {
        SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query("messageBase2", null, "chat_id=?", new String[]{chat_id}, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            ChatMessage msg = new ChatMessage();
            msg.fromIP = cursor.getString(cursor.getColumnIndex("fromIP"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            msg.fromAvatar = cursor.getInt(cursor.getColumnIndex("fromAvatar"));
            msg.toIP = cursor.getString(cursor.getColumnIndex("toIP"));
            msg.msgType = cursor.getInt(cursor.getColumnIndex("msgType"));
            msg.sendTime = cursor.getString(cursor.getColumnIndex("sendTime"));
            //TODO
            msg.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            msg.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
            msg.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            msg.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            msg.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            //TODO
            msg.chat_id = cursor.getString(cursor.getColumnIndex("chat_id"));
            //TODO 2016/4/5
            msg.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            msg.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            msg.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            msg.uuid=cursor.getString(cursor.getColumnIndex("uuid"));
            mDatas.add(msg);
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    /**
     * 查找所有群聊聊天信息
     *
     * @return
     */
    public List<ChatMessage> findRoomMsgAll() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("chatRoomBaseMsg2", null, null, null, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        List<ChatMessage> mList = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            ChatMessage msg = new ChatMessage();
            msg.fromIP = cursor.getString(cursor.getColumnIndex("fromIP"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            msg.fromAvatar = cursor.getInt(cursor.getColumnIndex("fromAvatar"));
            msg.toIP = cursor.getString(cursor.getColumnIndex("toIP"));
            msg.msgType = cursor.getInt(cursor.getColumnIndex("msgType"));
            msg.sendTime = cursor.getString(cursor.getColumnIndex("sendTime"));
            //TODO
            msg.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            msg.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
            msg.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            msg.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            msg.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            //TODO 2016/4/5
            msg.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            msg.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            msg.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            msg.uuid=cursor.getString(cursor.getColumnIndex("uuid"));
            mList.add(msg);
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }



    //根据online查找群聊用户
    public List<ChatMessage> findRoomByType(String type) {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("chatRoomBaseMsg2", null, "type=?", new String[]{type}, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        List<ChatMessage> mList = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            ChatMessage msg = new ChatMessage();
            msg.fromIP = cursor.getString(cursor.getColumnIndex("fromIP"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            msg.fromAvatar = cursor.getInt(cursor.getColumnIndex("fromAvatar"));
            msg.toIP = cursor.getString(cursor.getColumnIndex("toIP"));
            msg.msgType = cursor.getInt(cursor.getColumnIndex("msgType"));
            msg.sendTime = cursor.getString(cursor.getColumnIndex("sendTime"));
            //TODO
            msg.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            msg.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
            msg.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            msg.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            msg.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            //TODO 2016/4/5
            msg.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            msg.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            msg.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            msg.uuid=cursor.getString(cursor.getColumnIndex("uuid"));
            mList.add(msg);
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }






    /**
     * 根据IP查找是否有同样的用户数据
     *
     * @param contact_ip
     */
    public boolean find(String contact_ip) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("messageBase2", null, "contact_ip = ?", new String[]{contact_ip}, null, null, null);

        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }


    /**
     * 閺嶈宓両P閺屻儴顕楅懕濠傘亯閺佺増宓侀弰顖氭儊閸︺劍鏆熼幑顔肩氨闁插矂娼?
     *
     * @param contact_ip
     */
//	public boolean find(String fromIP) {
//		boolean result = false;
//		SQLiteDatabase db = helper.getReadableDatabase();
////		Cursor cursor = db.query("bao", null, "contact_ip = ?",
////				new String[] { contact_ip }, null, null, null);
//		Cursor cursor = db.query("chatMessa", null,"fromIP = ?", new String[] { fromIP }, null, null, null);
//
//		if (cursor.moveToNext()) {
//			result = true;
//		}
//		cursor.close();
//		db.close();
//		return result;
//	}
}

	
	
