Add library to project:
- Put <b>app-release.aar</b> to module libs folder
- Add ropository inside main gradle.build  <br>
<code>allprojects {<br>
&emsp;&emsp;&emsp;&emsp;repositories {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;...<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;flatDir {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;dirs 'libs'<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;}<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;maven { url "https://jitpack.io" }<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;...<br>
&emsp;&emsp;&emsp;&emsp;}<br>
}</code>
- Add dependency to <b>app-release.aar</b> inside module's gradle.build<br>
<code>dependencies {<br>
&emsp;&emsp;&emsp;&emsp;...<br>
&emsp;&emsp;&emsp;&emsp;implementation fileTree(dir: 'libs', include: ['*.jar'])<br>
&emsp;&emsp;&emsp;&emsp;implementation 'com.github.felHR85:UsbSerial:4.5.2'<br>
&emsp;&emsp;&emsp;&emsp;...<br>
}</code>

How to use:<br>
- Initialize ZCManager in place where you are planing to use it
mACManager = new ACManager(context, boundRate).setDebug(isDebug); <br>
-- boundRate is 38400 in our case<br>
-- isDebug is false by default<br>
- Create Broad cast receiver <br>
<code>private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {<br>
&emsp;&emsp;&emsp;&emsp;@Override<br>
&emsp;&emsp;&emsp;&emsp;public void onReceive(Context context, Intent intent) {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;if(mACManager != null) {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;mACManager.onReceive(context, intent);<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;}<br>
&emsp;&emsp;&emsp;&emsp;}<br>
};</code>
- Create intent filter, register broad cast receiver and connect serial<br>
<code>@Override<br>
protected void onResume() {<br>
&emsp;&emsp;&emsp;&emsp;super.onResume();<br>
&emsp;&emsp;&emsp;&emsp;IntentFilter filter = new IntentFilter();<br>
&emsp;&emsp;&emsp;&emsp;filter.addAction(Constants.ACTION_USB_PERMISSION);<br>
&emsp;&emsp;&emsp;&emsp;filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);<br>
&emsp;&emsp;&emsp;&emsp;filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);<br>
&emsp;&emsp;&emsp;&emsp;registerReceiver(broadcastReceiver, filter);<br>
&emsp;&emsp;&emsp;&emsp;if (mACManager != null) {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;mACManager.connect();<br>
&emsp;&emsp;&emsp;&emsp;}<br>
}</code>
- Don't forget unregister broadcast and do disconnect serial<br>
<code>@Override<br>
protected void onPause() {<br>
&emsp;&emsp;&emsp;&emsp;super.onPause();<br>
&emsp;&emsp;&emsp;&emsp;if (mACManager != null) {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;mACManager.disconnect();<br>
&emsp;&emsp;&emsp;&emsp;}<br>
&emsp;&emsp;&emsp;&emsp;unregisterReceiver(broadcastReceiver);<br>
}</code>
- Add calls to get necessary info from Arduino anywhere you needed <br>
<code>mACManager.getSpeedAndAngle(new ACManager.OnResponseListener() {<br>
&emsp;&emsp;&emsp;&emsp;@Override<br>
&emsp;&emsp;&emsp;&emsp;public void onResponse(final String response) {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;LoggerUtil.i("response: " + response);<br>
&emsp;&emsp;&emsp;&emsp;}<br>
&emsp;&emsp;&emsp;&emsp;@Override<br>
&emsp;&emsp;&emsp;&emsp;public long getTimeout() {<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;return timeout;<br>
&emsp;&emsp;&emsp;&emsp;}<br>
});</code> <br>
-- if response is null then something went wrong. Check Arduino setup.<br>
-- timeout - time that lib will wait for response from Arduino. If timed out then response will be null.<br> The best practice set timeout 10000 ms.<br>
-- available commands: <br>
--- getSpeedAndAngle
--- getPower
--- sendCommand <br>
in case of sendCommand first argument is String. Set
---- "1" to get get Speed And Angle (getSpeedAndAngle is analog)
---- "3" to get get Power (getPower is analog)
- Create <b>device_filter.xml</b> with Arduino Vender ID inside xml source folder <br>
<code>&lt;?xml version="1.0" encoding="utf-8"?&gt;<br>
&lt;resources&gt;<br>
&emsp;&emsp;&emsp;&emsp;&lt;usb-device vendor-id="9025" /&gt;<br>
&emsp;&emsp;&emsp;&emsp;&lt;!-- Vendor ID of Arduino --><br>
&lt;/resources&gt;</code>
- Add intent filter and meta data inside activity block in AndroidManifest.xml<br>
<code>&lt;intent-filter&gt;<br>
&emsp;&emsp;&emsp;&emsp;&lt;action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" /&gt;<br>
/intent-filter&gt;<br>
&lt;meta-data<br>
&emsp;&emsp;&emsp;&emsp;android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"<br>
&emsp;&emsp;&emsp;&emsp;android:resource="@xml/device_filter" <br>/&gt;</code>

