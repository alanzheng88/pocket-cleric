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
    final String UID_STAT_DIR = "/proc/uid_stat/";
    final String TCP_RVC = "tcp_rcv";
    final String TCP_SND = "tcp_snd";
    String tag = null;

    TrafficRecord() {
        tx = TrafficStats.getTotalTxBytes();
        rx = TrafficStats.getTotalRxBytes();
    }

    TrafficRecord(int uid) {
        tx = TrafficStats.getUidTxBytes(uid);
        rx = TrafficStats.getUidRxBytes(uid);
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
        File dir = new File(UID_STAT_DIR);
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File(UID_STAT_DIR + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, TCP_RVC);
        File uidActualFileSent = new File(uidFileDir, TCP_SND);

        String textSent = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.valueOf(textSent).longValue();
    }

    private Long getReceivedBytes(int localUid) {

        File dir = new File(UID_STAT_DIR);
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File(UID_STAT_DIR + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, TCP_RVC);
        File uidActualFileSent = new File(uidFileDir, TCP_SND);

        String textReceived = "0";

        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.valueOf(textReceived).longValue();

    }

}
