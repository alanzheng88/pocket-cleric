package com.example.alanzheng.pocketcleric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TrafficMonitorActivity extends Activity {
    TextView latest_rx=null;
    TextView latest_tx=null;
    TextView previous_rx=null;
    TextView previous_tx=null;
    TextView delta_rx=null;
    TextView delta_tx=null;
    TextView tether_delta_rx=null;
    TextView tether_delta_tx=null;
    TrafficSnapshot latest=null;
    TrafficSnapshot previous=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitor);

        latest_rx=(TextView)findViewById(R.id.latest_rx);
        latest_tx=(TextView)findViewById(R.id.latest_tx);
        previous_rx=(TextView)findViewById(R.id.previous_rx);
        previous_tx=(TextView)findViewById(R.id.previous_tx);
        delta_rx=(TextView)findViewById(R.id.delta_rx);
        delta_tx=(TextView)findViewById(R.id.delta_tx);
        tether_delta_rx=(TextView)findViewById(R.id.tether_delta_rx);
        tether_delta_tx=(TextView)findViewById(R.id.tether_delta_tx);

        takeSnapshot(null);
    }

    public void takeSnapshot(View v) {
        previous=latest;
        latest=new TrafficSnapshot(this);

        latest_rx.setText(String.valueOf(latest.device.rx));
        latest_tx.setText(String.valueOf(latest.device.tx));

        if (previous!=null) {
            previous_rx.setText(String.valueOf(previous.device.rx));
            previous_tx.setText(String.valueOf(previous.device.tx));

            delta_rx.setText(String.valueOf(latest.device.rx-previous.device.rx));
            delta_tx.setText(String.valueOf(latest.device.tx-previous.device.tx));

            tether_delta_rx.setText(String.valueOf(latest.tether.rx-previous.tether.rx));
            tether_delta_tx.setText(String.valueOf(latest.tether.tx-previous.tether.tx));
        }

        ArrayList<String> log=new ArrayList<String>();
        HashSet<Integer> intersection=new HashSet<Integer>(latest.apps.keySet());

        if (previous!=null) {
            intersection.retainAll(previous.apps.keySet());
        }

        for (Integer uid : intersection) {
            TrafficRecord latest_rec=latest.apps.get(uid);
            TrafficRecord previous_rec=
                    (previous==null ? null : previous.apps.get(uid));

            emitLog(latest_rec.tag, latest_rec, previous_rec, log);
        }

        Collections.sort(log);
        for (String row : log) {
            Log.d("TrafficMonitor", row);
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