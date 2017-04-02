package com.example.alanzheng.pocketcleric;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class TrafficMonitorActivity extends Activity {

    private static final String TAG = TrafficMonitorActivity.class.getSimpleName();
    private static final int READ_PHONE_STATE_REQUEST_TAG = 1000;

    TextView latest_rx = null;
    TextView latest_tx = null;
    TextView previous_rx = null;
    TextView previous_tx = null;
    TextView tether_latest_rx = null;
    TextView tether_latest_tx = null;
    TextView tether_previous_rx = null;
    TextView tether_previous_tx = null;
    TextView delta_rx = null;
    TextView delta_tx = null;
    TextView tether_delta_rx = null;
    TextView tether_delta_tx = null;

    Context context = null;
    TrafficSnapshot latest = null;
    TrafficSnapshot previous = null;
    NetworkStatsManager networkStatsManager = null;
    NetworkStats networkStatsByApp;
    NetworkStats.Bucket bucket;
    TelephonyManager tm = null;
    String subscriberId = null;
    int uid = 0;

    long tether_latest_rx_value = 0;
    long tether_latest_tx_value = 0;
    long tether_previous_rx_value = 0;
    long tether_previous_tx_value = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitor);

        // Get application context
        context = this.getApplicationContext();

        // Get references to text views
        latest_rx = (TextView) findViewById(R.id.latest_rx);
        latest_tx = (TextView) findViewById(R.id.latest_tx);
        previous_rx = (TextView) findViewById(R.id.previous_rx);
        previous_tx = (TextView) findViewById(R.id.previous_tx);
        delta_rx = (TextView) findViewById(R.id.delta_rx);
        delta_tx = (TextView) findViewById(R.id.delta_tx);
        tether_delta_rx = (TextView) findViewById(R.id.tether_delta_rx);
        tether_delta_tx = (TextView) findViewById(R.id.tether_delta_tx);
        tether_latest_rx = (TextView) findViewById(R.id.tether_latest_rx);
        tether_latest_tx = (TextView) findViewById(R.id.tether_latest_tx);
        tether_previous_rx = (TextView) findViewById(R.id.tether_previous_rx);
        tether_previous_tx = (TextView) findViewById(R.id.tether_previous_tx);

        // Compute data usage of tethering by Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Collect tether usage accurately using NetworkStatsManager (SDK level 23+)
            Toast.makeText(this, "Device uses at least Android 6.0. Tracking accurately!", Toast.LENGTH_SHORT).show();
            computeDataUsageOfTethering();
        } else {
            // Collect tether usage inaccurately using TrafficStats
            Toast.makeText(this, "App suggests using Android 6.0. Tracking inaccurately.", Toast.LENGTH_SHORT).show();
        }

        // Initialize monitor table
        takeSnapshot(null);
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
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // Check if device should show an explanation
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                        // TODO: (Optional) Show an explanation to the user asynchronously.
                    } else {
                        // No explanation needed, request permission
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_TAG);
                    }
                }
            }
        } else {
            // Launch settings activity to request action usage access
            Log.d(TAG, "Does not have usage access. Launching settings");
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    // Return a byte count in human readable format
    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf("kMGTPE".charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    // Initialize values to perform telephony task
    public void setupTelephonyTask() {
        // Get tethering system service UID
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
            networkStatsByApp = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, subscriberId, 0, end, uid);
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
    @Override
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

    public void takeSnapshot(View v) {
        // Swap latest and previous
        previous = latest;
        latest = new TrafficSnapshot(this);

        latest_rx.setText(humanReadableByteCount(latest.device.rx));
        latest_tx.setText(humanReadableByteCount(latest.device.tx));

        // If a snapshot ahs been takes before, collect next stats
        if (previous != null) {
            previous_rx.setText(humanReadableByteCount(previous.device.rx));
            previous_tx.setText(humanReadableByteCount(previous.device.tx));

            // Calculate delta values
            long delta_rx_value = latest.device.rx - previous.device.rx;
            long delta_tx_value = latest.device.tx - previous.device.tx;

            delta_rx.setText(humanReadableByteCount(delta_rx_value));
            delta_tx.setText(humanReadableByteCount(delta_tx_value));
        }

        // Perform accurate tethering tracking for SDK 23+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Swap latest and previous
            tether_previous_rx_value = tether_latest_rx_value;
            tether_previous_tx_value = tether_latest_tx_value;

            // Calculate new values
            tether_latest_rx_value = getAllRxBytesMobile(context);
            tether_latest_tx_value = getAllTxBytesMobile(context);

            // If a snapshot ahs been takes before, collect next stats for SDK 23+
            if (previous != null) {
                tether_previous_rx.setText(humanReadableByteCount(tether_previous_rx_value));
                tether_previous_tx.setText(humanReadableByteCount(tether_previous_tx_value));

                tether_latest_rx.setText(humanReadableByteCount(tether_latest_rx_value));
                tether_latest_tx.setText(humanReadableByteCount(tether_latest_tx_value));

                // Calculate delta values
                long tether_delta_rx_value = tether_latest_rx_value - tether_previous_rx_value;
                long tether_delta_tx_value = tether_latest_tx_value - tether_previous_tx_value;

                tether_delta_rx.setText(humanReadableByteCount(tether_delta_rx_value));
                tether_delta_tx.setText(humanReadableByteCount(tether_delta_tx_value));
            }
        } else {
            // SDK < 23, fall back on inaccurate implementation
            if (previous != null) {
                tether_previous_rx.setText("N/A");
                tether_previous_tx.setText("N/A");

                tether_latest_rx.setText("N/A");
                tether_latest_tx.setText("N/A");

                tether_delta_rx.setText("N/A");
                tether_delta_tx.setText("N/A");
            }

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
                Log.d("TrafficMonitor", row);
            }
        }
    }

    // Build a debug log for logcat of data usage by application
    private void emitLog(CharSequence name, TrafficRecord latest_rec, TrafficRecord previous_rec, ArrayList<String> rows) {
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
}