package com.example.android.bluetoothchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rabbit徐 on 2015/7/22.
 */
public class XMPPUser {


    public XMPPUser(int Type, String name, List<CheckMessage> MessageList)
    {
        this.mType = Type;
        this.Name = name;
        this.mList=MessageList;
    }
    public XMPPUser(int Type, String name)
    {
        this.mType = Type;
        this.Name = name;
        this.mList=new ArrayList<CheckMessage>();
    }

    private int mType;
    private String Name;
    private List<CheckMessage> mList;


    /*public long getId(){
        return id;
    }
    public void setId(long n){
        id = n;
    }
    public long getTime(){
        return time;
    }
    public void setTime(long t){
        time = t;
    }*/
    public int getType() {
        return mType;
    }
    public void setType(int mType) {
        this.mType = mType;
    }
    public String getName(){
        return Name;
    }
    public void setName(String n){
        Name = n;
    }
    public List<CheckMessage> getList() {
        return mList;
    }
    public void setList(List<CheckMessage> mContent) {
        this.mList = mContent;
    }
}
