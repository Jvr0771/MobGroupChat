package io.yxy.mobgroupchat;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends BaseActivity {

    // Logcat tag
    private final String TAG = MainActivity.class.getSimpleName();
    private Button btnSend;
    private EditText inputMsg;
    private WebSocketClient client;

    // Chat messages list apapter
    private MessageListAdapter messageListAdapter;
    private List<Message> messageList;
    private ListView messageListView;

    private Utils utils;

    // Client name
    private String name = null;

    // JSON flags to identify the kind of JSON response
    private static final String TAG_SELF = "self", TAG_NEW = "new", TAG_MESSAGE = "message", TAG_EXIT = "exit";

    private void init() {
        btnSend = (Button) findViewById(R.id.btn_send);
        inputMsg = (EditText) findViewById(R.id.input_msg);
        messageListView = (ListView) findViewById(R.id.list_view_messages);

        utils = new Utils(getApplicationContext());

        // Getting the person name from previous screen
        Intent i = getIntent();
        name = i.getStringExtra("name");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer(utils.getSendMessageJSON(inputMsg.getText().toString()));

                // Clearing the input filed once message was sent
                inputMsg.setText("");
            }
        });

        messageList = new ArrayList<Message>();

        messageListAdapter = new MessageListAdapter(this, messageList);
        messageListView.setAdapter(messageListAdapter);

        initConnection();
        client.connect();

        Log.i(TAG, "initialized");

    }

    private void initConnection() {
        client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET + URLEncoder.encode(name)), new WebSocketClient.Listener() {

            @Override
            public void onConnect() {
                Log.i(TAG, "socket connected");
            }

            @Override
            public void onMessage(String message) {
                Log.i(TAG, "Received: " + message);
                parseMessage(message);
            }

            @Override
            public void onMessage(byte[] data) {
                String message = bytesToHex(data);
                Log.i(TAG, String.format("Received binary message: %s", message));
                parseMessage(message);
            }

            @Override
            public void onDisconnect(int code, String reason) {
                String message = String.format(Locale.US, "Disconnected! Code: %d Reason: %s", code, reason);
                showToast(message);
                utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, error.getMessage());
                showToast("Error: " + error);
            }
        }, null);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void parseMessage(final String message) {
        try {
            JSONObject jObj = new JSONObject(message);
            String flag = jObj.getString("flag");



            // if flag is 'self', this is JSON contains session id
            if (flag.equalsIgnoreCase(TAG_SELF)) {
                String sessionId = jObj.getString("sessionId");
                utils.storeSessionId(sessionId);
            } else if (flag.equalsIgnoreCase(TAG_NEW)) {
                // if the flag is 'new', new person joined the room
                String name = jObj.getString("name");
                String msg = jObj.getString("message");

                // number of people online
                String onlineCount = jObj.getString("onlineCount");
                showToast(name + msg + ". Currently " + onlineCount + " people online!");

            } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
                // if the flag is 'message', new message received
                String fromName = name;
                String msg = jObj.getString("message");
                String sessionId = jObj.getString("sessionId");
                boolean isSelf = true;

                // Checking if the message was sent by you
                if (!sessionId.equals(utils.getSessionId())) {
                    fromName = jObj.getString("name");
                    isSelf = false;
                }
                Message m = new Message(fromName, msg, isSelf);
                
                // Appending the message to chat list
                appendMessage(m);
            } else if (flag.equalsIgnoreCase(TAG_EXIT)) {
                String name = jObj.getString("name");
                String msg = jObj.getString("message");
                showToast(name + msg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void appendMessage(final Message m) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(m);
                messageListAdapter.notifyDataSetChanged();

                playBeep();
            }
        });
    }

    private void playBeep() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    public void sendMessageToServer(String message) {
        if (client != null && client.isConnected()) {
            Log.d(TAG, "sent message: " + message);
            client.send(message);
        } else {
            Log.e(TAG, client.toString() + " client lost connection");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null && client.isConnected())
            client.disconnect();
    }
}
