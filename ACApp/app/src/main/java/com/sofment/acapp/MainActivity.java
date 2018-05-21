package com.sofment.acapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.sofment.aclibrary.ACManager;
import com.sofment.aclibrary.Constants;
import com.sofment.aclibrary.utils.LoggerUtil;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private ACManager mACManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(this);
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

    private Button mButton = null;

    @Override
    public void onClick(View v) {
        mACManager.getPower(new ACManager.OnResponseListener() {
            @Override
            public void onResponse(final String s) {
                LoggerUtil.i("response: " + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "response: " + s, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public long getTimeout() {
                return 10000;
            }
        });
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mACManager != null) {
                mACManager.onReceive(context, intent);
            }
        }
    };
}
