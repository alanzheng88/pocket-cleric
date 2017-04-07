package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ReceptorActivity extends AppCompatActivity {

    private static final String TAG = ReceptorActivity.class.getSimpleName();
    private WebView receptorWebView;
    private Client client;
    private Context context;

    public class ReceptorCallback implements ReceptorInterface {
        @Override
        public void processData(final String data) {
            Log.d(TAG, "callback with data: " + data);
            receptorWebView.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Loading url " + data);
                    receptorWebView.loadUrl(data);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptor);
        context = getApplicationContext();
        receptorWebView = (WebView) findViewById(R.id.receptorWebView);
        receptorWebView.setWebViewClient(new WebViewClient());
        startClient();
    }

    private void startClient() {
        if (client == null) {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String macAddress = info.getMacAddress();
            client = Client.create(this, macAddress);
            client.register(new ReceptorCallback());
        }
        client.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            client.closeConnection();
            client = null;
        }
    }

}
