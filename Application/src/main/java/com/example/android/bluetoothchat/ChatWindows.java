package com.example.android.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.common.logger.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nccu_dct on 15/10/13.
 */
public class ChatWindows extends Activity {

    private final String TAG = "UIActivity";

    private Handler mshandler;
    public static List<CheckMessage> items = new ArrayList<>();
    public MessageAdapter itemsAdapter;
    private String Receiver = "broker@140.119.164.18";//"broker@140.119.164.5";
    private String Username;
    // UI set
    ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;


    @Override
    protected  void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_windows);
        Intent intent = this.getIntent();
        Receiver = intent.getStringExtra("account");
        Username = intent.getStringExtra("username");
        mConversationView =(ListView) findViewById(R.id.chat);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);

        mshandler = BluetoothChatFragment.mMessageHandler.getHandler();




    }

    @Override
    protected void onStart(){
        super.onStart();
        setup();
        //BluetoothChatFragment.mXMPPService.setOnline();
        mshandler.obtainMessage(Constants.MESSAGE_ONLINE, "".length(), -1, "")
                .sendToTarget();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected  void onStop(){
        super.onStop();
        //BluetoothChatFragment.mXMPPService.setOffline();
        mshandler.obtainMessage(Constants.MESSAGE_OFFLINE, "".length(), -1, "")
                .sendToTarget();
    }

    private void setup(){
        //items = LoadData();
        mOutEditText.setOnEditorActionListener(mWriteListener);
        itemsAdapter = new MessageAdapter(ChatWindows.this, BluetoothChatFragment.mMessageHandler.getContent(Receiver));
        mConversationView.setAdapter(itemsAdapter);
        //ChatFragment.mXMPP.setCurrentAdapter(itemsAdapter);
        BluetoothChatFragment.mMessageHandler.setHandler(mHandler);

        /*switch (BluetoothChatFragment.getState()){
            case BluetoothChatFragment.

        }*/

        BluetoothChatFragment.mXMPPService.startchat(Receiver);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget

                TextView textView = (TextView) findViewById(R.id.edit_text_out);
                String message = textView.getText().toString();
                sendMessage(message, Username);
            }
        });

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_XMPP_READ:
                    Log.d(TAG, "UIRead");
                    String read = (String) msg.obj;
                    itemsAdapter.Refresh();

                    break;
                case Constants.MESSAGE_XMPP_WRITE:
                    String write = (String) msg.obj;
                    itemsAdapter.Refresh();
                    break;
                case Constants.MESSAGE_XMPP_GROUPWRITE:
                    //String write = (String) msg.obj;
                    itemsAdapter.Refresh();
                    break;
                case Constants.MESSAGE_XMPP_GROUPREAD:
                    //String write = (String) msg.obj;
                    itemsAdapter.Refresh();
                    break;
            }
        }
    };
    private List<CheckMessage> LoadData(){
        return null;
    }


    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message, Username);
            }
            return true;
        }
    };

    private void sendMessage(String message, String name) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            Long tsLong = System.currentTimeMillis();
            String ts = tsLong.toString();
            String namemessage = Receiver+"##"+message+"##"+ts;
            //itemDB.insert(new CheckMessage(0, tsLong, CheckMessage.MessageType_From, name, message));
            mshandler.obtainMessage(Constants.MESSAGE_XMPP_WRITE, -1, -1, namemessage)
                    .sendToTarget();
            //BluetoothChatFragment.mXMPPService.write(message);

            mOutEditText.setText("");
        }
    }
}
