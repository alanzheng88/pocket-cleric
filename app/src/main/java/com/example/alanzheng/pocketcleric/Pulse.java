package com.example.alanzheng.pocketcleric;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

class Pulse implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = Pulse.class.getSimpleName();
    private static final String PREFS_TAG = "LoginData";
    private static final int READ_PHONE_STATE_REQUEST_TAG = 1000;
    private static final int CONNECTION_TIMEOUT = 20000; // in milliseconds
    private static final int READ_TIMEOUT = 30000; // in milliseconds

    Context context = null;
    TrafficSnapshot latest = null;
    TrafficSnapshot previous = null;
    private NetworkStatsManager networkStatsManager = null;
    private NetworkStats.Bucket bucket;
    private String subscriberId = null;
    private int uid = 0;

    long tether_latest_rx_value = 0;
    long tether_latest_tx_value = 0;
    long tether_previous_rx_value = 0;
    long tether_previous_tx_value = 0;
    long delta_rx_value = 0;
    long delta_tx_value = 0;
    long tether_delta_rx_value = 0;
    long tether_delta_tx_value = 0;

    // Constructor
    Pulse(Context context) {
        // Get activity context
        this.context = context;

        // Compute data usage of tethering by Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Collect tether usage accurately using NetworkStatsManager (SDK level 23+)
            Toast.makeText(context, "Device uses at least Android 6.0. Tracking accurately!", Toast.LENGTH_SHORT).show();
            computeDataUsageOfTethering();
        } else {
            // Collect tether usage inaccurately using TrafficStats
            Toast.makeText(context, "App suggests using Android 6.0. Tracking inaccurately.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void computeDataUsageOfTethering() {

        // Check for action usage access
        boolean hasUsageAccess = checkForUsageAccess(context);
        if (hasUsageAccess) {
            // Device has usage access already
            Log.d(TAG, "Has usage access, proceeding...");
            networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);

            // Check for telephony access
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // User has telephony access already
                Log.d(TAG, "Has telephony access, proceeding...");
                // Setup telephony task
                setupTelephonyTask();
                // Perform telephony task
                performTelephonyTask();
            } else {
                // Device does not have telephony access yet
                Log.d(TAG, "Does not telephony access, requesting permission.");
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // Check if device should show an explanation
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_PHONE_STATE)) {
                        // TODO: (Optional) Show an explanation to the user asynchronously.
                    } else {
                        // No explanation needed, request permission
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_TAG);
                    }
                }
            }
        } else {
            // Launch settings activity to request action usage access
            Log.d(TAG, "Does not have usage access. Launching settings");
            context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    // Initialize values to perform telephony task
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setupTelephonyTask() {
        // Get tethering system service UID
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        subscriberId = tm.getSubscriberId();
        uid = NetworkStats.Bucket.UID_TETHERING; // -5
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void performTelephonyTask() {
        // Get network usage caused by tethering
        try {
            // Get current time in milliseconds
            long end = System.currentTimeMillis();
            // Query network manager for tether data usage
            NetworkStats networkStatsByApp = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, subscriberId, 0, end, uid);
            // Instantiate a new bucket
            bucket = new NetworkStats.Bucket();
            // Retrieve bucket contents
            networkStatsByApp.getNextBucket(bucket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getAllRxBytesMobile(Context context) {
        try {
            // Fill bucket
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, getSubscriberId(context, ConnectivityManager.TYPE_MOBILE), 0, System.currentTimeMillis());
            if (bucket != null) {
                // Return all time tethering usage in rx
                return bucket.getRxBytes();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getAllTxBytesMobile(Context context) {
        try {
            // Fill bucket
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, getSubscriberId(context, ConnectivityManager.TYPE_MOBILE), 0, System.currentTimeMillis());
            if (bucket != null) {
                // Return all time tethering usage in tx
                return bucket.getTxBytes();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Return subscriber id when getting tx/rx bytes mobile
    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Handle request codes
        switch (requestCode) {
            case READ_PHONE_STATE_REQUEST_TAG: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, perform telephony task for the first time
                    setupTelephonyTask();
                    performTelephonyTask();
                } else {
                    // permission denied, no functionality to disable however
                }
                return;
            }
            // Other 'case' lines to check for other permissions this app might request
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean checkForUsageAccess(Context context) {
        // Check if the apps ops manager allows the collection of usage stats
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // Build a debug log for logcat of data usage by application
    protected void emitLog(CharSequence name, TrafficRecord latest_rec, TrafficRecord previous_rec, ArrayList<String> rows) {
        if (previous_rec != null && latest_rec.rx - previous_rec.rx > 0) {
            StringBuilder buf = new StringBuilder(String.valueOf(latest_rec.rx));
            buf.append("\t");

            if (previous_rec != null) {
                buf.append(String.valueOf(latest_rec.rx - previous_rec.rx));
                buf.append("\t");
            }
            buf.append(String.valueOf(latest_rec.tx));
            buf.append("\t");

            if (previous_rec != null) {
                buf.append(String.valueOf(latest_rec.tx - previous_rec.tx));
                buf.append("\t");
            }
            buf.append(name);
            rows.add(buf.toString());
        }
    }

    public void commitPulse(Context cxt, long rx, long tx) {

        // Check Ap state
        if (ApManager.isApOn(context)) {

            // Send pulse containing upload/downloaded amounts to server
            SharedPreferences sp;
            sp = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);

            String username = sp.getString("username", null);

            // Do not pulse if both values are zero
            if (rx > 0 || tx > 0) {
                if (username != null) {
                    Log.d(TAG, "Pulsing for user " + username);

                    // Process on a background thread
                    new AsyncPulse(cxt).execute(username, String.valueOf(rx), String.valueOf(tx));
                } else {
                    Log.d(TAG, "Could not pulse. Username was null.");
                }
            } else {
                Log.d(TAG, "No tethering network traffic detected.");
                Toast.makeText(context, "Nothing to pulse yet.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Prevent if Ap is off
            Toast.makeText(context, "Please enable tethering.", Toast.LENGTH_SHORT).show();
        }
    }

    class AsyncPulse extends AsyncTask<String, String, String>
    {
        Context context;
        ProgressDialog pdLoading;
        HttpURLConnection conn;
        URL url = null;

        public AsyncPulse(Context cxt) {
            context = cxt;
            pdLoading = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // This method will be running on UI thread
            pdLoading.setMessage("\tSending Pulse...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your php file resides

                // Uncomment if developing on local machine (XAMPP)
                //url = new URL("http://10.0.2.2/pocketcleric/login.inc.php");

                // Uncomment if testing on live server
                url = new URL("http://www.prawnskunk.com/pocketcleric/pulse.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("rx", params[1])
                        .appendQueryParameter("tx", params[2]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();
                Log.d(TAG,"HTTP " +response_code);
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                } else {

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true")) {
                // Pulse was successful
                Session session = new Session(context);
                HashMap<String, String> user = session.getUserDetails();
                String username = user.get(user.keySet().toArray()[0]);
                Toast.makeText(context, "Successfully pulsed, " + username + "!\n\nDownload: " + Utility.humanReadableByteCount(tether_delta_tx_value) + "\nUpload: " + Utility.humanReadableByteCount(tether_delta_rx_value), Toast.LENGTH_LONG).show();
            }
            else if (result.equalsIgnoreCase("false")) {
                // Pulse failed
                Toast.makeText(context, "Pulse failed. The server may be down.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void takePulse(Context cxt)
    {
        // TOTAL swap latest and previous
        previous = latest;
        latest = new TrafficSnapshot(context);

        // TOTAL previous
        if (previous != null) {
            // TOTAL delta
            delta_rx_value = ApManager.isApOn(context) ? (latest.device.rx - previous.device.rx) : 0;
            delta_tx_value = ApManager.isApOn(context) ? (latest.device.tx - previous.device.tx) : 0;
        }

        // Perform accurate tethering tracking for SDK 23+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // TETHERING swap latest and previous
            tether_previous_rx_value = tether_latest_rx_value;
            tether_previous_tx_value = tether_latest_tx_value;
            // TETHERING Calculate latest
            tether_latest_rx_value = getAllRxBytesMobile(context); // rx value
            tether_latest_tx_value = getAllTxBytesMobile(context); // tx value
            // TETHERING previous
            if (previous != null) {
                // TETHERING delta
                tether_delta_rx_value = ApManager.isApOn(context) ? (tether_latest_rx_value - tether_previous_rx_value) : 0;
                tether_delta_tx_value = ApManager.isApOn(context) ? (tether_latest_tx_value - tether_previous_tx_value) : 0;
                // Send pulse containing upload/downloaded amounts to server
                commitPulse(cxt, tether_delta_rx_value, tether_delta_tx_value);
            }
        }

        // Perform inaccurate tethering tracking for SDK < 23
        else {
            // TETHERING swap latest and previous
            tether_previous_rx_value = tether_latest_rx_value;
            tether_previous_tx_value = tether_latest_tx_value;
            // TETHERING Calculate latest
            tether_latest_rx_value = latest.tether.rx;  // rx value
            tether_latest_tx_value = latest.tether.tx;  // tx value
            // TETHERING previous
            if (previous != null) {
                // TETHERING delta
                tether_delta_rx_value = tether_latest_rx_value - tether_previous_rx_value;
                tether_delta_tx_value = tether_latest_tx_value - tether_previous_tx_value;
                // Send pulse containing upload/downloaded amounts to server
                commitPulse(cxt, tether_delta_rx_value, tether_delta_tx_value);
            }

            // Perform debugging
            ArrayList<String> log = new ArrayList<String>();
            HashSet<Integer> intersection = new HashSet<Integer>(latest.apps.keySet());
            if (previous != null) {
                intersection.retainAll(previous.apps.keySet());
            }
            for (Integer uid : intersection) {
                TrafficRecord latest_rec = latest.apps.get(uid);
                TrafficRecord previous_rec =
                        (previous == null ? null : previous.apps.get(uid));

                emitLog(latest_rec.tag, latest_rec, previous_rec, log);
            }
            // Sort and print debug statements
            Collections.sort(log);
            for (String row : log) {
                Log.d("Traffic: ", row);
            }
        }
    }
}
