package com.example.android.bluetoothchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.android.common.logger.Log;

import org.jivesoftware.smack.packet.StreamOpen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by nccu_dct on 15/11/5.
 */
public class MessageHandler extends Service {

    public static final String TAG = "MessageHandler";
    public static String username;

    public static String CurrentMessage;
    public static Map<String, ArrayList<CheckMessage>> StorageMessage = new HashMap<String, ArrayList<CheckMessage>>();
    public static Map<String, ArrayList<CheckMessage>> GroupMessage = new HashMap<String, ArrayList<CheckMessage>>();

    private static MitemDB DBmanager = null;
    static Context C;
    private static Handler UIhandler;


    private static boolean ONLine = false;
    public static NotificationCompat.Builder builder ;
    public static NotificationManager manager; //= (NotificationManager) C.getSystemService(Context.NOTIFICATION_SERVICE);

    //public Sting pn;





    public MessageHandler(Context context, MitemDB db, String n)
    {
        C = context;
        DBmanager = db;
        username = n;


        NotificationCompat.Builder builder = new NotificationCompat.Builder(C);
        manager = (NotificationManager) C.getSystemService(Context.NOTIFICATION_SERVICE);


        builder.setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        long[] vibrate_effect =
                {1000,500};
        builder.setVibrate(vibrate_effect);


    }


    private static final Handler msHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //message format: name@140.xxx.xxx.xxx##message
                case Constants.MESSAGE_XMPP_READ:
                    //Log.d(TAG, "XMPPREAD");
                    String read = (String) msg.obj;
                    String[] mread = read.split("##");
                    Log.d(TAG, "XMPPREAD : "+ read);
                    if(mread.length != 3){
                        break;
                    }

                    //notification
                    String notiname = mread[0].split("@")[0];
                    String noticontent = mread[1];
                    String notitime = mread[2];

                    CheckMessage tmp = new CheckMessage(0, Long.parseLong(notitime), CheckMessage.MessageType_To, notiname, noticontent);
                    getContent(notiname);
                    StorageMessage.get(notiname).add(tmp);
                    DBmanager.insert(tmp);
                    //CurrentMessage.add(tmp);
                    UIhandler.obtainMessage(Constants.MESSAGE_XMPP_READ, "".length(), -1, "")
                            .sendToTarget();


                    if(!ONLine){
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(C);

                        Intent serverIntent = new Intent(C, ChatWindows.class);
                        serverIntent.putExtra("account", notiname+"@140.119.164.18");
                        serverIntent.putExtra("username", username);
                        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                        PendingIntent pendingIntent = PendingIntent.getActivity(C, 1, serverIntent, flags);

                        builder.setSmallIcon(R.drawable.ic_launcher)
                                .setWhen(System.currentTimeMillis())
                                .setContentTitle(notiname)
                                .setContentText(noticontent)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                        long[] vibrate_effect =
                                {1000,500};
                        builder.setVibrate(vibrate_effect);

                        Notification notification = builder.build();

                        manager.notify(1, notification);
                    }

                    break;


                case Constants.MESSAGE_XMPP_WRITE:
                    Log.d(TAG, "XMPPWRITE");
                    String write = (String) msg.obj;
                    String[] mwrite = write.split("##");

                    String name = mwrite[0];
                    String content = mwrite[1];
                    String time = mwrite[2];

                    CheckMessage wtmp = new CheckMessage(0, Long.parseLong(time), CheckMessage.MessageType_From, name.split("@")[0], content);
                    StorageMessage.get(CurrentMessage).add(wtmp);
                    DBmanager.insert(wtmp);
                    //CurrentMessage.add(wtmp);
                    UIhandler.obtainMessage(Constants.MESSAGE_XMPP_WRITE, "".length(), -1, "")
                            .sendToTarget();
                    BluetoothChatFragment.mXMPPService.write(username+"##"+content+"##"+time);
                    break;


                case Constants.MESSAGE_XMPP_GROUPWRITE:
                    Log.d(TAG, "XMPPGroupWRITE");
                    String gwrite = (String) msg.obj;
                    String[] gmwrite = gwrite.split("##");

                    String gname = gmwrite[0];
                    String gcontent = gmwrite[1];
                    String gtime = gmwrite[2];

                    CheckMessage gwtmp = new CheckMessage(0, Long.parseLong(gtime), CheckMessage.MessageType_From, username, gcontent, gmwrite[3]);
                    GroupMessage.get(CurrentMessage).add(gwtmp);
                    //CurrentMessage.add(wtmp);
                    UIhandler.obtainMessage(Constants.MESSAGE_XMPP_GROUPWRITE, "".length(), -1, "")
                            .sendToTarget();
                    DBmanager.ginsert(gwtmp);
                    BluetoothChatFragment.mXMPPService.Groupwrite(gwrite);
                    break;

                case Constants.MESSAGE_XMPP_GROUPREAD:
                    Log.d(TAG, "XMPPGroupREAD");
                    String grmwrite = (String) msg.obj;
                    String[] grwrite = grmwrite.split("##");

                    String grname = grwrite[0];
                    String grcontent = grwrite[1];
                    String grtime = grwrite[2];
                    if(grname.equals(username)){
                        break;
                    }

                    CheckMessage grwtmp = new CheckMessage(0, Long.parseLong(grtime), CheckMessage.MessageType_To, grname.split("@")[0], grcontent, grwrite[3]);


                    getGContent(grwrite[3]);
                    GroupMessage.get(grwrite[3]).add(grwtmp);
                    //CurrentMessage.add(wtmp);
                    DBmanager.ginsert(grwtmp);

                    UIhandler.obtainMessage(Constants.MESSAGE_XMPP_GROUPREAD, "".length(), -1, "")
                            .sendToTarget();

                    if(BluetoothChatFragment.getState() == BluetoothChatFragment.dualmode){
                        BluetoothChatFragment.mChatService.relaying(grmwrite);
                    }

                    if(!ONLine){
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(C);

                        Intent serverIntent = new Intent(C, ChatWindows.class);
                        serverIntent.putExtra("account", grname+ "@conferrence.140.119.164.18");
                        serverIntent.putExtra("username", username);
                        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                        PendingIntent pendingIntent = PendingIntent.getActivity(C, 1, serverIntent, flags);

                        builder.setSmallIcon(R.drawable.ic_launcher)
                                .setWhen(System.currentTimeMillis())
                                .setContentTitle(grname)
                                .setContentText(grcontent)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                        long[] vibrate_effect =
                                {1000,500};
                        builder.setVibrate(vibrate_effect);

                        Notification notification = builder.build();

                        manager.notify(1, notification);
                    }

                    break;

                case Constants.MESSAGE_ONLINE:
                    ONLine = true;
                    break;

                case Constants.MESSAGE_OFFLINE:
                    ONLine = false;
                    break;

                case Constants.MESSAGE_READ:
                    //Log.d(TAG, "XMPPREAD");
                    byte[] readBuf = (byte[]) msg.obj;
                    String Bread =  new String(readBuf, 0, msg.arg1);//(String) msg.obj;
                    String[] Bmread = Bread.split("##");
                    Log.d(TAG, "BTREAD : "+ Bread);

                    if(Bmread[0].equals("ChatList") ){
                        Log.d(TAG, "ADD ROom");
                        BluetoothChatFragment.GlistAdapter.clear();
                        for(String n : Bmread){
                            Log.d(TAG, n);
                            BluetoothChatFragment.GlistAdapter.add(n);
                        }
                        BluetoothChatFragment.GlistAdapter.notifyDataSetChanged();
                        break;
                    }

                    if(Bmread.length != 4){
                        break;
                    }

                    //notification
                    String Bnotiname = Bmread[0].split("@")[0];
                    String Bnoticontent = Bmread[1];
                    String Bnotitime = Bmread[2];

                    //wairing for // FIXME: 2016/10/30
                    if(Bnotiname.equals(username)){
                        break;
                    }




                    if(BluetoothChatFragment.getState() == BluetoothChatFragment.dualmode){
                        BluetoothChatFragment.mXMPPService.relaying(Bread);
                        break;
                    }



                    CheckMessage Btmp = new CheckMessage(0, Long.parseLong(Bnotitime), CheckMessage.MessageType_To, Bnotiname, Bnoticontent, Bmread[3]);
                    getGContent(Bmread[3]);
                    GroupMessage.get(Bmread[3]).add(Btmp);
                    //CurrentMessage.add(wtmp);
                    DBmanager.ginsert(Btmp);

                    UIhandler.obtainMessage(Constants.MESSAGE_XMPP_GROUPREAD, "".length(), -1, "")
                            .sendToTarget();



                    /*if(StorageMessage != null){
                        StorageMessage.get(Bnotiname).add(Btmp);
                        DBmanager.insert(Btmp);
                    }*/

                    //CurrentMessage.add(tmp);
                    /*if(UIhandler != null) {
                        UIhandler.obtainMessage(Constants.MESSAGE_READ, "".length(), -1, "")
                                .sendToTarget();
                    }*/


                    if(!ONLine){
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(C);

                        Intent serverIntent = new Intent(C, ChatWindows.class);
                        serverIntent.putExtra("account", Bnotiname+"@140.119.164.18");
                        serverIntent.putExtra("username", username);
                        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                        PendingIntent pendingIntent = PendingIntent.getActivity(C, 1, serverIntent, flags);

                        builder.setSmallIcon(R.drawable.ic_launcher)
                                .setWhen(System.currentTimeMillis())
                                .setContentTitle(Bnotiname)
                                .setContentText(Bnoticontent)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                        long[] vibrate_effect =
                                {1000,500};
                        builder.setVibrate(vibrate_effect);

                        Notification notification = builder.build();

                        manager.notify(1, notification);
                    }

                    break;


                case Constants.MESSAGE_WRITE:
                    Log.d(TAG, "BTWRITE");

                    //byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //String readMessage =
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = ;

                    String Bwrite = (String) msg.obj ;
                    Log.d(TAG, "In: "+Bwrite);
                    String[] Bmwrite = Bwrite.split("##");

                    if(Bmwrite[0].equals("ChatList") ){
                        //BluetoothChatFragment.mChatService.write(Bwrite);
                        break;
                    }

                    String Bname = Bmwrite[0];
                    String Bcontent = Bmwrite[1];
                    String Btime = Bmwrite[2];

                    CheckMessage Bwtmp = new CheckMessage(0, Long.parseLong(Btime), CheckMessage.MessageType_From, Bname.split("@")[0], Bcontent, CurrentMessage);

                    if(BluetoothChatFragment.getState() == BluetoothChatFragment.bt){
                        GroupMessage.get(CurrentMessage).add(Bwtmp);
                    //CurrentMessage.add(wtmp);

                        DBmanager.ginsert(Bwtmp);
                    }

                    /*if(CurrentMessage != null && Bwtmp != null){
                        StorageMessage.get(CurrentMessage).add(Bwtmp);
                        DBmanager.insert(Bwtmp);
                    }*/


                    //CurrentMessage.add(wtmp);
                    if(UIhandler != null){
                        UIhandler.obtainMessage(Constants.MESSAGE_XMPP_GROUPWRITE, "".length(), -1, "")
                                .sendToTarget();
                        BluetoothChatFragment.mChatService.write(username + "##" + Bcontent + "##" + Btime + "##" + CurrentMessage);
                    }

                    break;




            }
        }
    };

    public Handler getHandler(){
        return msHandler;
    }

    public void setHandler(Handler mh){
        UIhandler = mh;
    }

    public static List<CheckMessage> getContent(String n){
        Log.d(TAG, "Get " + n + "'s messages.");
        CurrentMessage = (n.split("@")[0]);
        if(StorageMessage.get(CurrentMessage) == null) {
            StorageMessage.put(CurrentMessage, DBmanager.getUser(CurrentMessage));
            //StorageMessage.put(CurrentMessage, new ArrayList<CheckMessage>());
        }
        return StorageMessage.get(CurrentMessage);

    }

    public static List<CheckMessage> getGContent(String n){
        Log.d(TAG, "Get room " + n + "'s messages.");
        CurrentMessage = n;
        if(GroupMessage.get(CurrentMessage) == null) {
            GroupMessage.put(CurrentMessage, new ArrayList<CheckMessage>());
        }
        return GroupMessage.get(CurrentMessage);
    }

    public void setName(String n){
        username = n;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
