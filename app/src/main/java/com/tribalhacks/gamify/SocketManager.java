package com.tribalhacks.gamify;


import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tribalhacks.gamify.utils.StringUtils;

import java.net.URISyntaxException;

public class SocketManager {

    public static final String EVENT_ROOM_ID = "roomId";
    public static final String EVENT_USERNAME = "username";
    public static final String EVENT_ROOM_CREATED = "room created";
    public static final String EVENT_USER_JOINED = "user joined";
    public static final String EVENT_BUTTON_CLICKED = "button clicked";
    public static final String EVENT_CREATE_ROOM = "create room";
    public static final String EVENT_CLEAR = "clear";
    public static final String KEY_IS_CORRECT = "isCorrect";
    public static final String KEY_USER = "user";
    public static final String KEY_USERNAME = "username";
    private static final String URL = "http://10.0.1.252:3000";
    private static SocketManager instance;

    private Socket socket;

    private SocketManager() {
        try {
            socket = IO.socket(URL);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }

        return instance;
    }

    public void addListener(String event, Listener listener) {
        if (!StringUtils.isEmptyOrNull(event)) {
            socket.on(event, listener);
        }
    }

    public void removeListener(String event, Listener listener) {
        if (!StringUtils.isEmptyOrNull(event)) {
            socket.off(event, listener);
        }
    }

    public void removeAllListeners() {
        socket.off();
    }

    public void destroy() {
        socket.disconnect();
        removeAllListeners();
    }

    public void emit(String event, Object... args) {
        socket.emit(event, args);
    }
}
