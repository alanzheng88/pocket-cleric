package com.example.alanzheng.pocketcleric;

/**
 * Created by Max on 2017-04-06.
 */

public class Utility {

    // Return a byte count in human readable format
    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf("kMGTPE".charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
