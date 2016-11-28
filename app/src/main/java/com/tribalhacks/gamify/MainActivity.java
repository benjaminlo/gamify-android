package com.tribalhacks.gamify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GamifyMain";

    private Button buttonSendMessage;
    private Button buttonAddUser;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://10.0.1.229:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        if (buttonSendMessage != null) {
            buttonSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick send message!");
                    mSocket.emit("new message", "hello world");
                }
            });
        }


        buttonAddUser = (Button) findViewById(R.id.button_add_user);
        if (buttonAddUser != null) {
            buttonAddUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick add user!");
                    mSocket.emit("add user", "test user");
                }
            });
        }

        mSocket.on("new message", onNewMessage);
        mSocket.on("login", onUserJoined);
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
        mSocket.off("login", onUserJoined);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "new message received");
            final JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String username = data.getString("username");
                        String message = data.getString("message");
                        Toast.makeText(MainActivity.this, username + " " + message, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "JSON Error", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "login");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (buttonSendMessage != null) {
                        buttonSendMessage.setVisibility(View.VISIBLE);
                    }

                    if (buttonAddUser != null) {
                        buttonAddUser.setVisibility(View.GONE);
                    }
                }
            });
        }
    };
}
