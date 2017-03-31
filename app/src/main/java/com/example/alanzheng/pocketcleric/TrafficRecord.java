package com.example.alanzheng.pocketcleric;

/**
 * Created by Max on 2017-03-31.
 */

import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class TrafficRecord {

    long tx = 0;
    long rx = 0;
    String tag = null;

    TrafficRecord() {
        tx = TrafficStats.getTotalTxBytes();
        rx = TrafficStats.getTotalRxBytes();
    }

    TrafficRecord(int uid) {
        tx=TrafficStats.getUidTxBytes(uid);
        rx=TrafficStats.getUidRxBytes(uid);

        //Log.d("DEBUG","uid: "+uid+", tx: "+tx+", rx: "+rx);

        this.tag = "Tether";
    }

    TrafficRecord(int uid, String tag) {
        //tx=TrafficStats.getUidTxBytes(uid);
        //rx=TrafficStats.getUidRxBytes(uid);
        tx = getSentBytes(uid);
        rx = getReceivedBytes(uid);

        this.tag = tag;
    }

    private Long getSentBytes(int localUid) {
        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
        File uidActualFileSent = new File(uidFileDir, "tcp_snd");

        String textReceived = "0";
        String textSent = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }

        } catch (IOException e) {

        }
        return Long.valueOf(textSent).longValue();
    }

    private Long getReceivedBytes(int localUid) {

        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
        File uidActualFileSent = new File(uidFileDir, "tcp_snd");

        String textReceived = "0";
        String textSent = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }

        } catch (IOException e) {

        }
        return Long.valueOf(textReceived).longValue();

    }

}
