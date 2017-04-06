package com.example.alanzheng.pocketcleric;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by alanzheng on 2017-04-06.
 */

public class ServerThread {
    private static final String TAG = ServerThread.class.getSimpleName();
    private String name;
    private Socket socket;

    public ServerThread(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
    }

    public void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public String getName() {
        return name;
    }

}
