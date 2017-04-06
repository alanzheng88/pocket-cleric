package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by alanzheng on 2017-04-06.
 */

public class Client extends ClientServerHelper{

    private static final String TAG = Client.class.getSimpleName();
    public static final String TETHERING_IP = "192.168.43.1";
    public static final int CONNECTION_MAX = 50;
    private String name;
    private Socket socket;

    public Client(Context context, String name) {
        super(context);
        this.name = name;
        socket = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public boolean connectToTethererDevice() {
        Log.d(TAG, "Connecting");
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(TETHERING_IP, Server.SERVER_PORT), CONNECTION_MAX);
            displayToastMessage("Connected");
            Log.d(TAG, "Connected");
            return true;
        } catch (Exception e) {
            displayToastMessage("Connection failed");
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
