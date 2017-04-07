package com.example.alanzheng.pocketcleric;

import android.util.Log;

import java.io.DataOutputStream;
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
                socket = null;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void sendData(String data) {
        if (socket == null) {
            return;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeBytes(data);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

}
