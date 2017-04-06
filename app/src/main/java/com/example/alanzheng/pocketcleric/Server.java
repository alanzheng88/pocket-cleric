package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alanzheng on 2017-04-02.
 */

public class Server extends ClientServerHelper {

    private static final String TAG = Server.class.getSimpleName();
    public static final int SERVER_PORT = 9000;
    private final List<ServerThread> serverThreads;
    private ServerSocket serverSocket;
    private Socket socket;
    private AsyncTask serverAsyncTask;

    private class ServerThreadTask extends AsyncTask<Void, ServerThread, ServerThread> {

        @Override
        protected ServerThread doInBackground(Void... params) {
            while (true) {
                try {
                    Log.i(TAG, "Waiting for client to connected");
                    socket = serverSocket.accept();
                    String name = socket.getRemoteSocketAddress().toString();
                    ServerThread serverThread = new ServerThread(name, socket);
                    serverThreads.add(serverThread);
                    publishProgress(serverThread);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        @Override
        protected void onProgressUpdate(ServerThread... serverThreads) {
            super.onProgressUpdate(serverThreads);
            ServerThread serverThread = serverThreads[0];
            displayToastMessage("New client connected: " + serverThread.getName());
            Log.i(TAG, "A new client connected");
        }
    }

    public Server(Context context) {
        super(context);
        serverThreads = new ArrayList<>();
        serverAsyncTask = null;
    }

    public void start() {
        try {
            if (serverAsyncTask != null &&
                    !serverAsyncTask.isCancelled()) {
                return;
            }
            if (serverSocket == null) {
                serverSocket = new ServerSocket(SERVER_PORT);
            }
            serverAsyncTask = new ServerThreadTask().execute();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }


    }

    public void close() {
        for (ServerThread serverThread : serverThreads) {
            serverThread.closeSocket();
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
