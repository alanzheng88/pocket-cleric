package com.example.alanzheng.pocketcleric;

/**
 * Created by alanzheng on 2017-03-08.
 */

public class ClientScanResult {

    private String mIpAddress;
    private String mHwAddress;
    private String mDevice;
    private boolean mIsReachable;

    public ClientScanResult(String ipAddress, String hardwareAddress,
                            String device, boolean isReachable) {
        mIpAddress = ipAddress;
        mHwAddress = hardwareAddress;
        mDevice = device;
        mIsReachable = isReachable;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public String getHwAddress() {
        return mHwAddress;
    }

    public String getDevice() {
        return mDevice;
    }

    public boolean isReachable() {
        return mIsReachable;
    }

    @Override
    public String toString() {
        return "IP address: " + mIpAddress + ", MAC Address: " + mHwAddress + ", Device: " + mDevice;
    }

}
