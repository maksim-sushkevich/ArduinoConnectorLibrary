package com.nvpsoftware.aclibrary;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.nvpsoftware.aclibrary.utils.LoggerUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nvpsoftware.aclibrary.Constants.ACTION_USB_PERMISSION;

/**
 * project ACLibrary
 * package com.nvpsoftware.aclibrary
 *
 * Created by maxim on 5/20/18.
 * Copyright © 2018 NVP Software. All rights reserved.
 */

public class ACManager implements UsbSerialInterface.UsbReadCallback {
    private Context mContext;
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private UsbSerialDevice mUsbSerialDevice;
    private UsbDeviceConnection mUsbDeviceConnection;
    private int mBoundRate;
    private int mVendorId;
    private StringBuilder mStringBuilder;
    private boolean mIsResponseCompleted;

    private List<RequestItem> mQueue = new ArrayList<>();

    public ACManager(Activity context, int boundRate) {
        this(context, 0x2341, boundRate);
    }

    public ACManager setDebug(boolean isDebug) {
        LoggerUtil.isDebug = isDebug;
        return this;
    }

    public ACManager(Activity context, int vendorId, int boundRate) {
        mVendorId = vendorId;
        this.mBoundRate = boundRate;
        this.mContext = context;
        if (this.mContext == null) {
            throw new IllegalStateException("Activity can not be null");
        }
        this.mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mIsResponseCompleted = false;
    }

    public void connect() {
        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        if (mUsbDevice != null) {
            return;
        }

        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                mUsbDevice = entry.getValue();
                int deviceVID = mUsbDevice.getVendorId();
                if (deviceVID == mVendorId) {//Arduino Vendor ID
                    PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
                    mUsbManager.requestPermission(mUsbDevice, pi);
                    keep = false;
                } else {
                    mUsbDeviceConnection = null;
                    mUsbDevice = null;
                }

                if (!keep)
                    break;
            }
        }

        LoggerUtil.i("connect: " + mUsbDevice);
    }

    public void sendCommand(String command, OnResponseListener onResponseListener) {
        mQueue.add(new RequestItem(command, onResponseListener));
        if (mQueue.size() == 1) {
            doRequest(mQueue.get(0));
        }
    }

    public void getSpeedAndAngle(OnResponseListener onResponseListener) {
        if(onResponseListener == null)
            throw new IllegalStateException("onResponseListener can not be null");
        sendCommand("1", onResponseListener);
    }

    public void getPower(OnResponseListener onResponseListener) {
        if(onResponseListener == null)
            throw new IllegalStateException("onResponseListener can not be null");
        sendCommand("3", onResponseListener);
    }

    private void doRequest(final RequestItem requestItem) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mIsResponseCompleted = false;
                LoggerUtil.i("mUsbSerialDevice: " + mUsbSerialDevice);
                if (mUsbSerialDevice == null) return;
                mStringBuilder = new StringBuilder();
                mUsbSerialDevice.write(requestItem.getCommand().getBytes());
                long time = System.currentTimeMillis();
                while (true) {
                    if (mIsResponseCompleted) {
                        break;
                    }
                    if (System.currentTimeMillis() - time > requestItem.getOnResponseListener().getTimeout()) {
                        mStringBuilder = null;
                        break;
                    }
                }
                LoggerUtil.i("mStringBuilder: " + mStringBuilder);
                requestItem.getOnResponseListener().onResponse(mStringBuilder != null ? mStringBuilder.toString() : null);
                if (mQueue.size() > 0) {
                    mQueue.remove(mQueue.size() - 1);
                }
                if (mQueue.size() > 0) {
                    doRequest(mQueue.get(mQueue.size() - 1));
                }
            }
        }).start();
    }

    public void disconnect() {
        if (mUsbSerialDevice != null) {
            mUsbSerialDevice.close();
        }

        mUsbDevice = null;
        mUsbDeviceConnection = null;
        mUsbSerialDevice = null;
    }

    @Override
    public void onReceivedData(byte[] bytes) {
        String data = null;
        try {
            data = new String(bytes, "UTF-8");
            LoggerUtil.i("onReceivedData: " + data);
            data.concat("/n");
            if (mStringBuilder == null) {
                mStringBuilder = new StringBuilder();
            }
            mStringBuilder.append(data);
            if (data.trim().endsWith(";")) {
                mIsResponseCompleted = true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onReceive(Context context, Intent intent) {
        LoggerUtil.i("onReceive: " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_USB_PERMISSION:
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (mUsbDevice == null) {
                    LoggerUtil.i("USB DEVICE IS STILL NULL");
                    return;
                }
                if (granted) {
                    mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
                    mUsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, mUsbDeviceConnection);
                    if (mUsbSerialDevice != null) {
                        if (mUsbSerialDevice.open()) { //Set Serial Connection Parameters.
                            mUsbSerialDevice.setBaudRate(this.mBoundRate);
                            mUsbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            mUsbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            mUsbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                            mUsbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            mUsbSerialDevice.read(this);
                        } else {
                            LoggerUtil.i("PORT NOT OPEN");
                        }
                    } else {
                        LoggerUtil.i("PORT IS NULL");
                    }
                } else {
                    LoggerUtil.i("PERM NOT GRANTED");
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                break;
        }
    }

    public interface OnResponseListener {
        void onResponse(String response);

        long getTimeout();
    }
}
