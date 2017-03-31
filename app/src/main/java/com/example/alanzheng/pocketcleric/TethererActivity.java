package com.example.alanzheng.pocketcleric;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class TethererActivity extends AppCompatActivity {

    private static final String TAG = TethererActivity.class.getSimpleName();
    private boolean mSystemWritePermission;
    private TextView mConnectedDevicesTextView;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetherer);

        mConnectedDevicesTextView = (TextView) findViewById(R.id.textview_connected_devices);
        mStatusTextView = (TextView) findViewById(R.id.textview_status);
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

        if (buildVersionSdk >= Build.VERSION_CODES.M) {
            // Proceed if Android build is at least 6.0
            Log.d(TAG, "build version: " + String.valueOf(buildVersionSdk));

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
        } else if (buildVersionSdk >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Proceed if Android build is at least 4.2.2
            return true;
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
    public void getConnectedDevices(View v) {
        if (ApManager.isApOn(TethererActivity.this)) {
            // Storing all the info temporarily in a string for now
            // later we will move it to a RecyclerView
            String info = "";
            // When debugging, set the first argument for getClientList to false if you have hotspot
            // on but don't have data
            List<ClientScanResult> clientScanResultList = ApManager.getClientList(false, 1000);
            for (ClientScanResult clientResult : clientScanResultList) {
                info += clientResult.getHwAddress()j + "\n\n";
            }
            Log.d(TAG, "Displaying info: " + info);
            mConnectedDevicesTextView.setText(info);

        } else {
            Toast.makeText(this, "Cannot get connected devices. Hotspot not enabled.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cannot get connected devices. Hotspot not enabled.");
        }
    }

}
