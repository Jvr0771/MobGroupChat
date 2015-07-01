package io.yxy.mobgroupchat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

import java.net.URI;


public class MainActivity extends ActionBarActivity {


    private final String TAG = MainActivity.class.getSimpleName();

    private WebSocketClient client;
    private TextView main_txt;

    private void init() {
        main_txt = (TextView) findViewById(R.id.main_text);
        connectGroupChat();
        client.connect();
        Log.i(TAG, "initialized");
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer("hello");
            }
        });

    }

    private void connectGroupChat() {
        client = new WebSocketClient(URI.create("ws://192.168.1.104:8881/chat?name=hello"), new WebSocketClient.Listener() {

            @Override
            public void onConnect() {
                Log.i(TAG, "socket connected");
            }

            @Override
            public void onMessage(String message) {
                Log.i(TAG, "onMessage: " + message);

            }

            @Override
            public void onMessage(byte[] data) {

            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.i(TAG, "websocket disconnect for reason " + reason);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, error.getMessage());

            }
        }, null);
    }

    public void sendMessageToServer(String message) {
        if (client.isConnected()) {
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
