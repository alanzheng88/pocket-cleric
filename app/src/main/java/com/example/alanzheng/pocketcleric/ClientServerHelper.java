package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by alanzheng on 2017-04-06.
 */

public abstract class ClientServerHelper {

    protected final Handler handler;
    protected Context context;

    public ClientServerHelper(Context context) {
        this.context = context;
        handler = new Handler(context.getMainLooper());
    }

    protected void displayToastMessage(final String message) {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    protected void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
