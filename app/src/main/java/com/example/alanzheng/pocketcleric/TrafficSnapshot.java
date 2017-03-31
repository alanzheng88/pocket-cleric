package com.example.alanzheng.pocketcleric;

/**
 * Created by Max on 2017-03-31.
 */

import java.util.HashMap;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class TrafficSnapshot {

    TrafficRecord device=null;
    HashMap<Integer, TrafficRecord> apps=
            new HashMap<Integer, TrafficRecord>();

    TrafficSnapshot(Context ctxt) {
        device=new TrafficRecord();

        HashMap<Integer, String> appNames=new HashMap<Integer, String>();

        for (ApplicationInfo app :
                ctxt.getPackageManager().getInstalledApplications(0)) {
            appNames.put(app.uid, app.packageName);
        }

        for (Integer uid : appNames.keySet()) {
            apps.put(uid, new TrafficRecord(uid, appNames.get(uid)));
        }
    }
}
