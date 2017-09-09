/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.android.common.logger.Log;


import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {
    //2 steps
    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    //XMPP codes
    private static final int REQUEST_XMPP_CONNECT = 4;
    private static final int REQUEST_XMPP_LOGIN = 5;

    // Layout Views
    ListView mConversationView;
    public static ArrayAdapter<String> FriendlistAdapter;

    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private MessageAdapter mConversationArrayAdapter;
    private List<CheckMessage> mdata;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    public static BluetoothChatService mChatService = null;

    //auto
    ArrayList<BluetoothDevice> device = new ArrayList<BluetoothDevice>();
    //XMPP
    public static XMPPChatService mXMPPService = null;
    public static boolean XMPPing = false;
    private  String mXMPPname = "ABCC";
    public static   String username = "user2";
    private  String password = "123456";
    private boolean trylogin = false;

    //DB
    public static MitemDB itemDB;
    private List<CheckMessage> items= new ArrayList<>();

    //three
    public static String MyName = "USERv";//Guest";

    //gchat
    public static ArrayList<String> RoomList = new ArrayList<String>();
    public static ListView GlistView;
    public static List<String> Grouplist;
    public static ArrayAdapter<String> GlistAdapter;
    public static boolean BTmode = false;

    //json
    private String JSONString;

    //Message handle
    public static MessageHandler mMessageHandler;

    private static ArrayList<String> BTfriend = new ArrayList<>();

    //state parameter
    public static int appState = 0;
    public static final int idle = 0;
    public static final int internet = 1;
    public static final int bt = 2;
    public static final int dualmode = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate.");



        // 建立資料庫物件
        itemDB = new MitemDB(getActivity().getApplicationContext());



        // 取得所有記事資料
        items = itemDB.getAll();

        /*for(CheckMessage i : items){
            itemDB.delete(i.getId());
        }
        items.clear();*/

        //itemDB.sample();
        // Get local Bluetooth adapter

        //Toast.makeText(getActivity(), "onCreat.", Toast.LENGTH_LONG).show();
        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        MyName = mBluetoothAdapter.getName();
        if(mMessageHandler == null)
            mMessageHandler = new  MessageHandler(this.getActivity(), itemDB, username);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }



    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart.");
        //Toast.makeText(getActivity(), "onStart.", Toast.LENGTH_LONG).show();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            while (!mBluetoothAdapter.isEnabled()) {

            }
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getActivity(), "onDestroy.", Toast.LENGTH_LONG).show();
        if (mChatService != null) {
            mChatService.stop();
            mChatService = null;
        }
        stopAdvertising();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume.");

        //Toast.makeText(getActivity(), "onResume.", Toast.LENGTH_LONG).show();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        //getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        if(mXMPPService != null){
            RoomList = mXMPPService.getConferenceRoom();
            GlistAdapter.clear();
            //mChatService.sendRoomList(RoomList);
            Log.d(TAG, "Room List:\n\n");
            for(String l : RoomList){
                Log.d(TAG, l);
                GlistAdapter.add(l);
                //list.setText(list.getText() + "\n" + l);
            }
            Log.d(TAG, "Friend List:\n\n");
            ArrayList<String> tmpList = mXMPPService.getRoster();
            FriendlistAdapter.clear();
            for(String tmp : tmpList){
                Log.d(TAG, tmp);
                FriendlistAdapter.add(tmp);
            }
            for(String tmp : BTfriend){
                Log.d(TAG, tmp);
                FriendlistAdapter.add(tmp);
            }
        }else if(!trylogin){
            setState(idle);
            Intent serverIntent = new Intent(getActivity(), LoginXMPPActivity.class);
            startActivityForResult(serverIntent, REQUEST_XMPP_LOGIN);
        }

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            //Toast.makeText(getActivity(), mChatService.getState()+"", Toast.LENGTH_LONG).show();
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
                //mChatService = null;
                //setupChat();
            }
            //mChatService.start();// = null;
            //setupChat();

        }
        else {
        //    setupChat();
        }


    }
    @Override
    public void onStop() {
        super.onStop();
        //Toast.makeText(getActivity(), "onStop.", Toast.LENGTH_LONG).show();
        device.clear();
        /*if(mChatService != null) {
            mChatService.stop();
        }*/
    }
    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(getActivity(), "onPause.", Toast.LENGTH_LONG).show();
        try {
            getActivity().unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e){

        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_select, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //mConversationView = (ListView) view.findViewById(R.id.chat);

        //freind list
        mConversationView =(ListView) view.findViewById(R.id.friend_list);
        FriendlistAdapter = new ArrayAdapter<String>(getActivity(), R.layout.chat_name);
        mConversationView.setAdapter(FriendlistAdapter);
        mConversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent serverIntent = new Intent(getActivity(), ChatWindows.class);
                String info = ((TextView) view).getText().toString();
                String account = info.split("\n")[0];

                Toast.makeText(getActivity(), account, Toast.LENGTH_SHORT).show();

                serverIntent.putExtra("account", account);
                serverIntent.putExtra("username", username);
                startActivity(serverIntent);

            }
        });


        //Chatroom list
        GlistView = (ListView) getActivity().findViewById(R.id.sample_output).findViewById(R.id.sample);
        GlistAdapter = new ArrayAdapter<String>(getActivity(), R.layout.chat_name);
        GlistView.setAdapter(GlistAdapter);
        GlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent serverIntent = new Intent(getActivity(), GroupChatWindows.class);
                String info = ((TextView) view).getText().toString().split("\n")[0];
                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                //mXMPPService.joinRoom(MyName, "", info);
                mConnectedDeviceName = "Room: " + info;


                serverIntent.putExtra("account", info);
                serverIntent.putExtra("username", username);
                startActivity(serverIntent);
            }
        });
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");



        //測試先不用
        startAdvertising();


        if(mXMPPService== null)
            XMPPconnect();


        // Initialize the array adapter for the conversation thread
        /*mdata = LoadData();

        mConversationArrayAdapter = new MessageAdapter(getActivity(), mdata);

        mConversationView.setAdapter(mConversationArrayAdapter);
        mConversationArrayAdapter.Refresh();

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message, MyName);
                }
            }
        });*/

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mMessageHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //Intent serverIntent = new Intent(getActivity(), LoginXMPPActivity.class);
        //startActivityForResult(serverIntent, REQUEST_XMPP_LOGIN);
        //XMPPconnect();
        mChatService.start();


        //try auto
        //Toast.makeText(getActivity(), "Start try.", Toast.LENGTH_SHORT).show();
        //doDiscovery();


    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
            //startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message, String name) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED&&!XMPPing) {


            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;

        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            Long tsLong = System.currentTimeMillis();
            String ts = tsLong.toString();
            String namemessage = name+"##"+message+"##"+ts;
            itemDB.insert(new CheckMessage(0, tsLong, CheckMessage.MessageType_From, name, message));
            if(XMPPing){
                // Get the message bytes and tell the BluetoothChatService to write
                //byte[] send = message.getBytes();
                mXMPPService.write(namemessage);

            }
            if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = namemessage.getBytes();
                mChatService.write(namemessage);

                // Reset out string buffer to zero and clear the edit text field

            }
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message, MyName);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //ArrayAdapter.clear();
                            //mdata.clear();
                            //mConversationArrayAdapter.Refresh();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    String wMessage = writeMessage.split("##")[1];
                    if(!writeMessage.split("##")[0].equals(MyName))
                        break;
                    mdata.add(new CheckMessage(0, Long.parseLong(writeMessage.split("##")[2]), CheckMessage.MessageType_From, MyName, wMessage));
                    mConversationArrayAdapter.Refresh();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String rMessage = readMessage.split("##")[1];
                    CheckMessage tmp;

                    try {
                        if ((filter(Long.parseLong(readMessage.split("##")[2]), readMessage.split("##")[0], readMessage.split("##")[1])))
                            break;
                    }catch (Exception ex){

                        Log.e(TAG, "Error: " + readMessage);
                        break;
                    }
                    if(readMessage.split("##")[0].equals(MyName)&&(!MyName.equals("Guest"))){
                        tmp = new CheckMessage(0, Long.parseLong(readMessage.split("##")[2]), CheckMessage.MessageType_From,
                                readMessage.split("##")[0], rMessage);
                    }
                    else{
                        tmp = new CheckMessage(0, Long.parseLong(readMessage.split("##")[2]), CheckMessage.MessageType_To,
                                readMessage.split("##")[0], rMessage);
                    }
                    itemDB.insert(tmp);
                    mdata.add(tmp);

                    //relay!?
                    if(XMPPing){
                        mXMPPService.relaying(readMessage);
                    }
                    mChatService.relaying(readMessage);

                    mConversationArrayAdapter.Refresh();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_XMPP_READ:
                    String read = (String) msg.obj;
                    if(read.split("##").length < 3){
                        read = "unknownXMPP##"+read+"##0";
                    }
                    String rread = read.split("##")[1];
                    CheckMessage ttmp;

                    if ((filter(Long.parseLong(read.split("##")[2]),read.split("##")[0], read.split("##")[1])) )
                        break;

                    if(read.split("##")[0].equals(MyName)&&(!MyName.equals("Guest"))) {
                        ttmp = new CheckMessage(0, Long.parseLong(read.split("##")[2]), CheckMessage.MessageType_From,
                                read.split("##")[0], rread);
                    }
                    else{
                        ttmp = new CheckMessage(0, Long.parseLong(read.split("##")[2]), CheckMessage.MessageType_To,
                                read.split("##")[0], rread);
                    }
                    itemDB.insert(ttmp);
                    mdata.add(ttmp);

                    //relay!?
                    if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                        // Get the message bytes and tell the BluetoothChatService to write
                        //byte[] Xsend = read.getBytes();
                        mChatService.relaying(read);
                    }

                    mConversationArrayAdapter.Refresh();
                    break;
                case Constants.MESSAGE_XMPP_WRITE:

                    //avoid UI dup
                    if(mChatService.getState()==BluetoothChatService.STATE_CONNECTED){
                        break;
                    }

                    String write = (String) msg.obj;
                    String wwrite = write.split("##")[1];
                    mdata.add(new CheckMessage(0, Long.parseLong(write.split("##")[2]), CheckMessage.MessageType_From, MyName, wwrite));
                    mConversationArrayAdapter.Refresh();
                    break;
            }
        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //XMPPing = false;
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //XMPPing = false;
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case REQUEST_XMPP_CONNECT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    /*if(mChatService != null) {
                        mChatService.stop();
                    }*/
                    connectXMPPUser(data);

                }
                break;
            case REQUEST_XMPP_LOGIN:
                //LOGIN INFORMATION
                trylogin = true;
                if (resultCode == Activity.RESULT_OK){
                    username = data.getExtras().getString("USER");
                    password = data.getExtras().getString("PW");
                    MyName = data.getExtras().getString("NAME");
                    XMPPconnect();
                    setupChat();
                }
                else{
                    Log.d(TAG, "No XmppLogIn");
                    BTmode = true;
                    setupChat();
                }
                break;
            default:
                Log.d(TAG, "onActivity fail.");
                break;
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.xmpp_connect: {
                XMPPconnect();

                //Intent serverIntent = new Intent(getActivity(), XMPPListActivity.class);
                //startActivityForResult(serverIntent, REQUEST_XMPP_CONNECT);
                return true;
            }
            case R.id.DB_Clear: {
                for(CheckMessage i : items){
                    itemDB.delete(i.getId());
                }
                items.clear();
                mdata = LoadData();
                mConversationArrayAdapter.Refresh();

                return true;
            }
        }
        return false;
    }
    private List<CheckMessage> LoadData(){
        //List<CheckMessage> Messages=new ArrayList<CheckMessage>();
        //Messages = ;
        //Messages.add(new CheckMessage(CheckMessage.MessageType_To, ""));
        return items;
    }

    //auto receiver
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(getActivity(), "Receive.", Toast.LENGTH_SHORT).show();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(getActivity(), "Add.", Toast.LENGTH_SHORT).show();
                BluetoothDevice aaa = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.add(aaa);
                Toast.makeText(getActivity(), "Added.", Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getActivity(), "Finish.", Toast.LENGTH_SHORT).show();
                getActivity().unregisterReceiver(receiver);
                if(!device.isEmpty()){
                    //Autochat();
                }

            }

        }
    };


    private void doDiscovery(){
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        mBluetoothAdapter.startDiscovery();
    }

    public String getName(){
        return username;
    }

    //XMPP
    public void XMPPconnect(){
        //MainActivity.check = true;
        //mXMPPService = new XMPPChatService(getActivity(), mMessageHandler.getHandler(), username, password);
        if (mXMPPService == null){
            GlistAdapter.clear();
            GlistAdapter.add("No connection");
            FriendlistAdapter.clear();
            FriendlistAdapter.add("No connection");
            return;
        }
        while(!XMPPing){

        }
        RoomList = mXMPPService.getConferenceRoom();
        GlistAdapter.clear();
        Log.d(TAG, "Room List:\n\n");
        for(String l : RoomList){
            Log.d(TAG, l);
            GlistAdapter.add(l);
            //list.setText(list.getText() + "\n" + l);
        }
        Log.d(TAG, "Friend List:\n\n");
        FriendlistAdapter.clear();
        ArrayList<String> tmpList = mXMPPService.getRoster();
        for(String tmp : tmpList){
            Log.d(TAG, tmp);
            FriendlistAdapter.add(tmp);
        }

        //chatroomtest
        //XMPPing = true;
    }

    private void connectXMPPUser(Intent data){
        // Get user account
        XMPPing = true;
        Log.d(TAG, "XMPPing True.");
        //Toast.makeText(getActivity(), "XMPPing TRUE.", Toast.LENGTH_SHORT).show();
        String account = data.getExtras()
                .getString(XMPPListActivity.EXTRA_ACCOUNT);

        //mdata.clear();
        //mConversationArrayAdapter.Refresh();

        //mXMPPService.startchat(account);
        //mXMPPname = account.split("@")[0];

    }
    public boolean filter(Long time, String name, String content){

        return itemDB.Check(time, name, content);

    }

    public void relay(){

    }


    private void startAdvertising() {
        Log.d(TAG, "Start fragment.");
        Context c = getActivity();
        c.startService(getServiceIntent(c));
    }


    private void stopAdvertising() {
        Log.d(TAG, "Stop fragment.");
        Context c = getActivity();
        c.stopService(getServiceIntent(c));

    }

    private static Intent getServiceIntent(Context c) {
        return new Intent(c, AdvertiserService.class);
    }

    public static void addFriend(String n){
        FriendlistAdapter.add(n);
        BTfriend.add(n);
        FriendlistAdapter.notifyDataSetChanged();
    }

    public static void delFriend(){
        BTfriend.clear();
    }

    public static void setUsername(String n){
        mMessageHandler.setName(n);
    }

    public static void setState(int i){
        appState = i;
    }

    public static int getState(){
        return appState;
    }



}
