package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by alanzheng on 2017-04-06.
 */

public class Client extends ClientServerHelper {

    private static final String TAG = Client.class.getSimpleName();
    public static final String TETHERING_IP = "192.168.43.1";
    public static final int CONNECTION_MAX = 50;
    private String name;
    private Socket socket;
    private BufferedReader reader;
    private AsyncTask clientThreadTask;
    private ReceptorInterface receptorInterface;

    private class ClientThreadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            connectToTethererDevice();
            fetchDataFromTethererDevice();
            return null;
        }
    }

    public static Client create(Context context, String name) {
        Client client = new Client(context, name);
        return client;
    }

    private Client(Context context, String name) {
        super(context);
        this.name = name;
        socket = null;
        reader = null;
        clientThreadTask = null;
        receptorInterface = null;
    }

    public void register(ReceptorInterface receptorInterface) {
        this.receptorInterface = receptorInterface;
    }

    public void start() {
        if (clientThreadTask == null || !isConnected()) {
            clientThreadTask = new ClientThreadTask().execute();
        } else {
            displayToastMessage("Device has already been connected.");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void connectToTethererDevice() {
        while (!isConnected()) {
            Log.d(TAG, "Connecting");
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(TETHERING_IP, Server.SERVER_PORT), CONNECTION_MAX);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                displayToastMessage("Connected");
                Log.d(TAG, "Connected");

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                displayToastMessage("Connection failed");
                try {
                    if (socket != null) {
                        socket.close();
                        Log.d(TAG, "Sleeping for a few seconds before retrying...");
                        Thread.sleep(5000);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void fetchDataFromTethererDevice() {
        try {
            while (reader != null) {
                String message = reader.readLine();
                if (message != null) {
                    Log.d(TAG, "message received: " + message);
                    displayToastMessage("message received: " + message);
                    receptorInterface.processData(message);
                }

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
