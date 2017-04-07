package com.example.alanzheng.pocketcleric;

import android.content.*;
import android.net.wifi.*;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ApManager {

    public static final String PROC_NET_ARP = "/proc/net/arp";
    private static WifiManager mWifiManager;
    private static final String TAG = ApManager.class.getSimpleName();


    /**
     * Get list of clients connected to hotspot
     * @param onlyReachables {@code false} if the list should contain disconnected clients, {@code true} otherwise
     * @param reachableTimeout reachable timeout in milliseconds
     * @return ArrayList of {@link ClientScanResult}
     */
    public static List<ClientScanResult> getClientList(boolean onlyReachables, int reachableTimeout) {
        BufferedReader br = null;
        List<ClientScanResult> result = null;
        Log.d(TAG, "Getting client list");
        try {
            result = new ArrayList<>();
            // read file which shows list of connected devices from android device
            br = new BufferedReader(new FileReader(PROC_NET_ARP));
            String line;

            while ((line = br.readLine()) != null) {
                Log.d(TAG, "arp - " + line);
                String[] lineSplitted = line.split(" +");
                boolean hasMacAddressColumn = lineSplitted.length >= 4;
                if (hasMacAddressColumn) {
                    String macAddress = lineSplitted[3];
                    boolean isValidMacAddress = macAddress.matches("..:..:..:..:..:..");
                    if  (isValidMacAddress) {
                        String ipAddress = lineSplitted[0];
                        boolean isReachable = getIsReachable(reachableTimeout, ipAddress);
                        if (!onlyReachables || isReachable) {
                            String device = lineSplitted[5];
                            result.add(new ClientScanResult(ipAddress, macAddress, device, isReachable));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return result;
    }

    private static boolean getIsReachable(int reachableTimeout, String ipAddress) throws IOException {
        boolean isReachable;
        try {
            isReachable = InetAddress.getByName(ipAddress).isReachable(reachableTimeout);
        } catch (Exception e) {
            Log.i(TAG, "The IP address " + ipAddress + " is not reachable. Ensure you can access the internet for the hotspot.");
            isReachable = false;
        }
        return isReachable;
    }

    // Check if WiFi hotspot is enabled
    public static boolean isApOn(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            //Log.d(TAG, "Checking if Wifi AP is on");
            return (Boolean) method.invoke(mWifiManager);
        } catch (Throwable ignored) {
            Log.d(TAG, "Could not invoke WiFiManager");
        }
        Log.d(TAG, "isWifiApEnabled check was not performed. This line should not be reached.");
        return false;
    }

    // Toggle WiFi hotspot
    public static boolean configApState(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wifiConfiguration = null;
        try {
            // If WiFi is on, turn it off
            if(mWifiManager.isWifiEnabled()) {
                Log.d(TAG, "Wifi is on. Turning Wifi Off");
                mWifiManager.setWifiEnabled(false);
            }
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            Log.d(TAG, "Ap is on before: " + isApOn(context));
            method.invoke(mWifiManager, wifiConfiguration, !isApOn(context));
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