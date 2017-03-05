package com.example.alanzheng.pocketcleric;

import android.content.*;
import android.net.wifi.*;
import android.util.Log;

import java.lang.reflect.*;

public class ApManager {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Check if WiFi hotspot is enabled
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        Method method = null;
        try {
            method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            Log.d(TAG, "Checking if Wifi AP is on");
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
            Log.d(TAG, "Could not invoke WiFiManager");
        }
        Log.d(TAG, "isWifiApEnabled check was not performed. This line should not be reached.");
        return false;
    }

    // Toggle WiFi hotspot
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // If WiFi is on, turn it off
            if(wifimanager.isWifiEnabled()) {
                Log.d(TAG, "Wifi is on. Turning Wifi Off");
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            Log.d(TAG, "Ap is on before: " + isApOn(context));
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            Log.d(TAG, "sleeping for 5 seconds...");
            Thread.sleep(5000);
            Log.d(TAG, "finished sleeping");
            Log.d(TAG, "Ap is on after: " + isApOn(context));
            Log.d(TAG, "Configuring AP state succeeded. Wifi Ap is now " + (isApOn(context) ? "on" : "off") );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Configuring AP state failed");
        return false;
    }
}