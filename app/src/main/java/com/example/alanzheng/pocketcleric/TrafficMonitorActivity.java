package com.example.alanzheng.pocketcleric;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TrafficMonitorActivity extends Activity {

    private static final String TAG = TrafficMonitorActivity.class.getSimpleName();

    Pulse pulse = null;
    Context context = null;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitor);

        // Get application context
        context = this.getApplicationContext();

        // Instantiate new pulse object
        pulse = new Pulse(context);

        // Take pulse
        pulse.takePulse(this);

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
    }

    public void buttonPress(View v) {

        // Take pulse
        pulse.takePulse(this);

        // Text views for TOTAL
        latest_rx.setText(Utility.humanReadableByteCount(pulse.latest.device.rx));
        latest_tx.setText(Utility.humanReadableByteCount(pulse.latest.device.tx));

        // Only capture the rest after a button press
        if (pulse.previous != null) {

            previous_rx.setText(Utility.humanReadableByteCount(pulse.previous.device.rx));
            previous_tx.setText(Utility.humanReadableByteCount(pulse.previous.device.tx));

            // Text views for DELTA
            delta_rx.setText(Utility.humanReadableByteCount(pulse.delta_rx_value));
            delta_tx.setText(Utility.humanReadableByteCount(pulse.delta_tx_value));

            tether_latest_rx.setText(Utility.humanReadableByteCount(pulse.tether_latest_rx_value));
            tether_latest_tx.setText(Utility.humanReadableByteCount(pulse.tether_latest_tx_value));

            tether_previous_rx.setText(Utility.humanReadableByteCount(pulse.tether_previous_rx_value));
            tether_previous_tx.setText(Utility.humanReadableByteCount(pulse.tether_previous_tx_value));

            tether_delta_rx.setText(Utility.humanReadableByteCount(pulse.tether_delta_rx_value));
            tether_delta_tx.setText(Utility.humanReadableByteCount(pulse.tether_delta_tx_value));
        }
    }
}