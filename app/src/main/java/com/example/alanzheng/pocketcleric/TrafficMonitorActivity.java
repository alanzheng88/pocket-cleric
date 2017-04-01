package com.example.alanzheng.pocketcleric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import android.support.annotation.NonNull;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class TrafficMonitorActivity extends Activity {
    Context context = null;
    TextView latest_rx=null;
    TextView latest_tx=null;
    TextView previous_rx=null;
    TextView previous_tx=null;
    TextView tether_latest_rx=null;
    TextView tether_latest_tx=null;
    TextView tether_previous_rx=null;
    TextView tether_previous_tx=null;
    TextView delta_rx=null;
    TextView delta_tx=null;
    TextView tether_delta_rx=null;
    TextView tether_delta_tx=null;
    TrafficSnapshot latest=null;
    TrafficSnapshot previous=null;
    NetworkStatsManager networkStatsManager = null;
    private static final String TAG = TrafficMonitorActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1000;
    NetworkStats networkStatsByApp;
    NetworkStats.Bucket bucket;
    long tether_latest_rx_value=0;
    long tether_latest_tx_value=0;
    long tether_previous_rx_value=0;
    long tether_previous_tx_value=0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitor);

        context = this.getApplicationContext();

        latest_rx=(TextView)findViewById(R.id.latest_rx);
        latest_tx=(TextView)findViewById(R.id.latest_tx);
        previous_rx=(TextView)findViewById(R.id.previous_rx);
        previous_tx=(TextView)findViewById(R.id.previous_tx);
        delta_rx=(TextView)findViewById(R.id.delta_rx);
        delta_tx=(TextView)findViewById(R.id.delta_tx);
        tether_delta_rx=(TextView)findViewById(R.id.tether_delta_rx);
        tether_delta_tx=(TextView)findViewById(R.id.tether_delta_tx);
        tether_latest_rx=(TextView)findViewById(R.id.tether_latest_rx);
        tether_latest_tx=(TextView)findViewById(R.id.tether_latest_tx);
        tether_previous_rx=(TextView)findViewById(R.id.tether_previous_rx);
        tether_previous_tx=(TextView)findViewById(R.id.tether_previous_tx);

        // Print build version
        Log.d(TAG, "build version: " + String.valueOf(Build.VERSION.SDK_INT));

        // Proceed if Android build is at least 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasUsageAccess = checkForPermission(context);

            if (hasUsageAccess)
            {
                // User has action usage access
                Log.d(TAG, "Has usage access, proceeding...");
                networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);

                // Check for telephony service access
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    // User has telephony access
                    Log.d(TAG, "Has telephony access, proceeding...");
                    try {
                        performTelephonyTask();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    // User does not have telephony access
                    Log.d(TAG, "Does not telephony access, requesting permission.");
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_PHONE_STATE)) {

                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                }
            } else {
                // Request action usage access
                Log.d(TAG, "Does not have usage access. Launching settings");
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        } else {
            // TODO: Collect tether data some other way
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void performTelephonyTask() throws RemoteException
    {
        Context context = this.getApplicationContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String subscriberId = tm.getSubscriberId();

        // Get tethering system service UID
        int uid = NetworkStats.Bucket.UID_TETHERING;
        //Log.d("TETHER", uid + "");

        // Get network usage caused by tethering
        try {
            long start = 0;
            long end = System.currentTimeMillis();
            networkStatsByApp = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, subscriberId, start, end, uid);
            //networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, subscriberId, 0, System.currentTimeMillis(), uid);
            bucket = new NetworkStats.Bucket();

            // Get bucket
            networkStatsByApp.getNextBucket(bucket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //takeSnapshot(null);
    }

    public long getAllRxBytesMobile(Context context) {
        NetworkStats.Bucket bucket = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                        getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                        0,
                        System.currentTimeMillis());
            }
        } catch (RemoteException e) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return bucket.getRxBytes();
        }
        return 0;
    }

    public long getAllTxBytesMobile(Context context) {
        NetworkStats.Bucket bucket = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                        getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                        0,
                        System.currentTimeMillis());
            }
        } catch (RemoteException e) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return bucket.getTxBytes();
        }
        return 0;
    }

    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    try {
                        performTelephonyTask();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void takeSnapshot(View v) {
        previous=latest;
        latest=new TrafficSnapshot(this);

        latest_rx.setText(String.valueOf(latest.device.rx));
        latest_tx.setText(String.valueOf(latest.device.tx));

        if (previous != null) {
            previous_rx.setText(String.valueOf(previous.device.rx));
            previous_tx.setText(String.valueOf(previous.device.tx));

            delta_rx.setText(String.valueOf(latest.device.rx - previous.device.rx));
            delta_tx.setText(String.valueOf(latest.device.tx - previous.device.tx));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            // Swap latest and previous
            tether_previous_rx_value=tether_latest_rx_value;
            tether_previous_tx_value=tether_latest_tx_value;

            // Calculate new values
            long rx = getAllRxBytesMobile(context);
            long tx = getAllTxBytesMobile(context);
            // Log.d(TAG,rx+", "+tx);

            tether_latest_rx_value = rx;
            tether_latest_tx_value = tx;

            long tether_delta_rx_value = tether_latest_rx_value - tether_previous_rx_value;
            long tether_delta_tx_value = tether_latest_tx_value - tether_previous_tx_value;

            // Target accurate information for SDK 23+
            if (previous != null) {
                tether_previous_rx.setText(String.valueOf(tether_previous_rx_value));
                tether_previous_tx.setText(String.valueOf(tether_previous_tx_value));

                tether_latest_rx.setText(String.valueOf(tether_latest_rx_value));
                tether_latest_tx.setText(String.valueOf(tether_latest_tx_value));

                tether_delta_rx.setText(String.valueOf(tether_delta_rx_value));
                tether_delta_tx.setText(String.valueOf(tether_delta_tx_value));
            }

        } else {
            // Fallback on crappy implementation
            if (previous != null) {
                tether_previous_rx.setText("N/A");
                tether_previous_tx.setText("N/A");

                tether_previous_rx.setText("N/A");
                tether_previous_tx.setText("N/A");

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

            Collections.sort(log);
            for (String row : log) {
                Log.d("TrafficMonitor", row);
            }
        }

        // queryDetailsForUid() UID for tethering is (UID_TETHERING) -5
        // https://developer.android.com/reference/android/app/usage/NetworkStatsManager.html
    }

    private void emitLog(CharSequence name, TrafficRecord latest_rec,
                         TrafficRecord previous_rec,
                         ArrayList<String> rows) {
        if (previous_rec!=null && latest_rec.rx-previous_rec.rx>0) {
            StringBuilder buf=new StringBuilder(String.valueOf(latest_rec.rx));
            buf.append("\t");

            if (previous_rec!=null) {
                buf.append(String.valueOf(latest_rec.rx-previous_rec.rx));
                buf.append("\t");
            }

            buf.append(String.valueOf(latest_rec.tx));
            buf.append("\t");

            if (previous_rec!=null) {
                buf.append(String.valueOf(latest_rec.tx-previous_rec.tx));
                buf.append("\t");
            }

            buf.append(name);

            rows.add(buf.toString());
        }
    }
}