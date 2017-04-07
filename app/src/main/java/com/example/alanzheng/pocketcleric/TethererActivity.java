package com.example.alanzheng.pocketcleric;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TethererActivity extends AppCompatActivity {

    private static final String TAG = TethererActivity.class.getSimpleName();
    private Context context;
    private boolean mSystemWritePermission;
    private TextView mStatusTextView;
    private RecyclerView mClientRecyclerView;
    private List<ClientScanResult> mClientScanResultList;
    private ClientDataAdapter mClientDataAdapter;
    private Server server;
    private Client client;
    private Pulse pulse;

    private class ClientTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            client = new Client(context, "alan_client");
            client.connectToTethererDevice();
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (server != null) {
            server.close();
        }
        if (client != null) {
            client.closeConnection();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetherer);

        // Get application context
        this.context = this;

        // Instantiate new pulse object
        pulse = new Pulse(context);

        // Take pulse
        pulse.takePulse(this);

        mStatusTextView = (TextView) findViewById(R.id.textview_status);
        mClientRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_client_devices);
        mClientScanResultList = new ArrayList();
        mClientDataAdapter = new ClientDataAdapter(mClientScanResultList, this);
        mClientRecyclerView.setAdapter(mClientDataAdapter);
        updateStatus();
    }

    private void updateStatus() {
        String statusString = getResources().getString(R.string.status);
        if (ApManager.isApOn(TethererActivity.this)) {
            mStatusTextView.setText(statusString + ": enabled");
        } else {
            mStatusTextView.setText(statusString + ": disabled");
        }
    }

    // Allow user to manually allow manifest to write permissions
    private boolean checkSystemWritePermission() {

        int buildVersionSdk = Build.VERSION.SDK_INT;
        Log.d(TAG, "build version: " + String.valueOf(buildVersionSdk));

        // Proceed if Android build is at least 6.0
        if (buildVersionSdk >= Build.VERSION_CODES.M) {

            // Check if app can write system settings
            boolean canWrite = Settings.System.canWrite(TethererActivity.this);

            // Open permissions menu if "Allow modify system settings" is disabled
            if (canWrite) {
                Log.d(TAG, "Device can already modify system settings.");
                return true;
            } else {
                Log.d(TAG, "Insufficient permissions, launching settings.");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Log.d(TAG, "Launched settings activity.");
                } catch (Exception e) {
                    Log.d(TAG, "Failed to launch Settings activity.");
                }
            }
            return false;
        } else if (buildVersionSdk >= Build.VERSION_CODES.JELLY_BEAN) {
            // Proceed if Android build is at least 4.1
            return true;
        } else {
            Log.d(TAG, "Android version " + String.valueOf(buildVersionSdk) + " is not supported.");
        }
        return false;
    }

    // @+id/button_enable hook
    public void onEnable(View v) {
        // Handle system write permissions
        if (mSystemWritePermission) {
            // Check Ap state
            if (!ApManager.isApOn(TethererActivity.this)) {
                Log.d(TAG, "Wifi AP is not on");
                enableHotspot();
            } else {
                Log.d(TAG, "Could not enable WiFi hotspot.");
            }
        } else {
            Log.d(TAG, "Checking system write permission.");
            mSystemWritePermission = checkSystemWritePermission();
            if (mSystemWritePermission) {
                enableHotspot();
            }
        }
        updateStatus();
    }

    private void enableHotspot() {
        ApManager.configApState(TethererActivity.this);
        Log.d(TAG, "WiFi hotspot enabled.");
    }

    // @+id/button_disable hook
    public void onDisable(View v) {
        // Check Ap state
        if (ApManager.isApOn(TethererActivity.this)) {
            // Change Ap state
            ApManager.configApState(TethererActivity.this);
            Log.d(TAG, "WiFi hotspot disabled.");
        } else {
            Log.d(TAG, "Hotspot already disabled.");
        }
        updateStatus();
    }

    // @+id/button_get_connected_devices
    public void getConnectedDevices(View v) throws IOException {
        List<ClientScanResult> tempClientScanList;
        if (ApManager.isApOn(TethererActivity.this)) {
            // When debugging, set the first argument for getClientList to false if you have hotspot
            // on but don't have data
            tempClientScanList = ApManager.getClientList(false, 1000);
            Toast.makeText(this, tempClientScanList.isEmpty() ?
                    "There are no connected clients" : "Acquired client list", Toast.LENGTH_SHORT).show();
            mClientScanResultList.clear();
            mClientScanResultList.addAll(tempClientScanList);
            mClientDataAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Cannot get connected devices. Hotspot not enabled.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cannot get connected devices. Hotspot not enabled.");
        }
    }

    // Triggers when TRAFFIC Button clicked
    public void goToTrafficMonitorActivity(View arg0) {
        // Check Ap state
        if (ApManager.isApOn(TethererActivity.this)) {
            // Create an intent for login activity
            Intent i = new Intent(this, TrafficMonitorActivity.class);
            // Send user to next activity
            startActivity(i);
        } else {
            // Prevent if Ap is off
            Toast.makeText(this, "Please enable tethering.", Toast.LENGTH_SHORT).show();
        }
    }

    // @+id/button_enable_server
    public void enableServer(View v) {
        if (server == null) {
            server = new Server(this);
        }
        server.start();
    }

    // @+id/button_enable_client
    // This method only works on a client device
    // and not on the tetherer device itself
    public void enableClient(View v) {
        if (client == null || !client.isConnected()) {
            new ClientTask().execute();
        } else {
            Toast.makeText(this, "Device has already been connected.", Toast.LENGTH_SHORT).show();
        }
    }

    // @+id/button_pulse hook
    public void onPulse(View v) {
        // Commit pulse
        Log.d(TAG,"Taking pulse...");
        // Take pulse
        pulse.takePulse(this);
    }

    // @+id/button_logout hook
    public void onLogout(View v) {
        // Commit pulse
        Log.d(TAG,"Logging out...");
        Session session = new Session(this);
        session.logoutUser();
    }

    @Override
    public void onBackPressed()
    {
        // Exit application immediately
        moveTaskToBack(true);
    }
}
