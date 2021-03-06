Add library to project:
- Put <b>app-release.aar</b> to module libs folder
- Add repositories inside main gradle.build  <br>
``` java
allprojects {
    repositories {
        ...
        flatDir {
            dirs 'libs'
        }
        maven { url "https://jitpack.io" }
        ...
    }
}
```
- Add dependencies to <b>app-release.aar</b> inside module's gradle.build<br>
``` java
dependencies {
    ...
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.github.felHR85:UsbSerial:4.5.2'
    ...
}
````

<b>How to use</b><br>
- Initialize ACManager in place where you are planing to use it
mACManager = new ACManager(context, boundRate).setDebug(isDebug); <br>
-- <b>boundRate</b> is 38400 in our case<br>
-- <b>isDebug</b> is false by default<br>
- Create Broadcast receiver <br>
``` java
private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(mACManager != null) {
            mACManager.onReceive(context, intent);
        }
    }
};
```
- Create intent filter, register Broadcast receiver and connect to serial<br>
``` java
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
```
- Don't forget unregister Broadcast receiever and do disconnect from serial<br>
``` java
@Override
protected void onPause() {
    super.onPause();
    if (mACManager != null) {
        mACManager.disconnect();
    }
    unregisterReceiver(broadcastReceiver);
}
```
- Add calls to get necessary info from Arduino anywhere you needed <br>
``` java
mACManager.getSpeedAndAngle(new ACManager.OnResponseListener() {
    @Override
    public void onResponse(final String response) {
        LoggerUtil.i("response: " + response);
    }
    
    @Override
    public long getTimeout() {
        return timeout;
    }
});
```
-- if response is null then something went wrong. Check Arduino setup.<br>
-- <b>timeout</b> - time that lib will wait for response from Arduino. If timed out then response will be null.<br> The best practice set timeout 10000 ms.<br>
-- available commands: <br>
--- <b>getSpeedAndAngle</b> <br>
example of response: 
``` json 
{"speedSensor":{"speed":"124.53", "time":"63"},"filtered_angle": {"X":"-43.00", "Y":"0.03", "Z":"0.00"}} 
```
--- <b>getPower</b> <br>
example of response: 
``` json 
{"powerSensor":{"V":"0.00", "A":"-1.00", "W":"-1.00", "Wh":"-1.00"}} 
```
--- <b>getFullInfo</b><br>
example of response: 
``` json
{"speed_sensor":{"speed":" 7.57", "time":"1036"}, "power_sensor":{"v":"0.00", "a":"-1.00", "w":"-1.00", "wh":"-1.00"}, "filtered_angle":{"x":"0.59", "y":"-5.83", "z":"-0.15"}}
```
--- <b>sendCommand</b> <br>
in case of sendCommand first argument is String. Set<br>
--- <b>"1"</b> to get get Speed And Angle (getSpeedAndAngle is analog)<br>
--- <b>"3"</b> to get get Power (getPower is analog)<br>
- Create <b>device_filter.xml</b> with Arduino Vender ID inside xml source folder <br>
``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-device vendor-id="9025" />
    <!-- Vendor ID of Arduino -->
</resources>
```
- Add intent filter and meta data inside activity block in AndroidManifest.xml<br>
``` xml
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
    </intent-filter>

    <meta-data
        android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
        android:resource="@xml/device_filter" />
</activity>
```

