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
    private AsyncTask serverAsyncTask;

    private class ServerThreadTask extends AsyncTask<Void, ServerThread, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                try {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        serverSocket = new ServerSocket(SERVER_PORT);
                        serverSocket.setReuseAddress(true);
                    }
                    Log.i(TAG, "Waiting for client to connect");
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "Connection accepted");
                    String name = socket.getRemoteSocketAddress().toString();
                    ServerThread serverThread = new ServerThread(name, socket);
                    serverThreads.add(serverThread);
                    publishProgress(serverThread);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return null;
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

    public List<ServerThread> getServerThreads() {
        return serverThreads;
    }

    public void start() {
        boolean hasServerThreadTask = serverAsyncTask != null &&
                !serverAsyncTask.isCancelled();
        if (hasServerThreadTask) {
            return;
        }
        serverAsyncTask = new ServerThreadTask().execute();
    }

    public void close() {
        for (ServerThread serverThread : serverThreads) {
            serverThread.closeSocket();
        }
        serverThreads.clear();
        try {
            if (serverSocket != null) {
                serverAsyncTask.cancel(true);
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
