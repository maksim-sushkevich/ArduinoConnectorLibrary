package com.nvpsoftware.acapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.nvpsoftware.aclibrary.ACManager;
import com.nvpsoftware.aclibrary.Constants;
import com.nvpsoftware.aclibrary.utils.LoggerUtil;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    /**
     * project ACApp
     * package com.nvpsoftware.acapp
     *
     * Created by maxim on 5/20/18.
     * Copyright © 2018 NVP Software. All rights reserved.
     */

    private ACManager mACManager;
    private TextView mTextView;

    private ACManager.OnResponseListener mResponseListener = new ACManager.OnResponseListener() {
        @Override
        public void onResponse(final String response) {
            LoggerUtil.i("response: " + response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(response);
                }
            });
        }

        @Override
        public long getTimeout() {
            return 10000;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nvpsoftware.acapp.R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        mTextView = findViewById(R.id.textView);
        mACManager = new ACManager(this, 38400).setDebug(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        if (mACManager != null) {
            mACManager.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mACManager != null) {
            mACManager.disconnect();
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mTextView.setText("Request of speed and angle");
                mACManager.getSpeedAndAngle(mResponseListener);
                break;
            case R.id.button1:
                mTextView.setText("Request of power info");
                mACManager.getPower(mResponseListener);
                break;
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mACManager != null) {
                mACManager.onReceive(context, intent);
            }
        }
    };
}
