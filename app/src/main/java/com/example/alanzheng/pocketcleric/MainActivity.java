package com.example.alanzheng.pocketcleric;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean systemWritePermission;
    private boolean hotspotEnabled;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Allow user to manually allow manifest to write permissions
    private boolean checkSystemWritePermission() {

        // Proceed if Android build is at least 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "build version: " + String.valueOf(Build.VERSION.SDK_INT));

            // Check if app can write system settings
            boolean canWrite = Settings.System.canWrite(MainActivity.this);

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
        }
        return true;
    }

    // @+id/button_enable hook
    public void onEnable(View v) {
        if (!hotspotEnabled) {
            // Handle system write permissions
            if (systemWritePermission) {
                // Check Ap state
                if (!ApManager.isApOn(MainActivity.this)) {
                    Log.d(TAG, "Wifi AP is not on");
                    // Change Ap state
                    ApManager.configApState(MainActivity.this);
                    hotspotEnabled = true;
                    Log.d(TAG, "WiFi hotspot enabled.");
                } else {
                    Log.d(TAG, "Could not enable WiFi hotspot.");
                }
            } else {
                Log.d(TAG, "Checking system write permission.");
                systemWritePermission = checkSystemWritePermission();
            }
        } else {
            Log.d(TAG, "Hotspot already enabled.");
        }
    }

    // @+id/button_disable hook
    public void onDisable(View v) {
        if (hotspotEnabled) {
            // Check Ap state
            if (ApManager.isApOn(MainActivity.this)) {
                // Change Ap state
                ApManager.configApState(MainActivity.this);
                hotspotEnabled = false;
                Log.d(TAG, "WiFi hotspot disabled.");
            } else {
                Log.d(TAG, "Could not disable WiFi hotspot.");
            }
        } else {
            Log.d(TAG, "Hotspot already disabled.");
        }
    }

}


