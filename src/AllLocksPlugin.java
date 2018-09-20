package com.restaumatic.alllocks;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;


public class AllLocksPlugin extends CordovaPlugin {
  private PowerManager.WakeLock wakeLock = null;
  private PowerManager powerManager = null;
  private boolean releaseOnPause = false;

  private WifiManager wifiManager = null;
  private WifiManager.WifiLock wifiLock = null;


  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    this.wifiManager = (WifiManager) cordova.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    this.powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) throws JSONException {

    Context context = cordova.getContext();
    if (action.equals("acquire") ) {
      this.acquireWakeLock(PowerManager.PARTIAL_WAKE_LOCK);
      this.acquireWifiLock();
    } else if (action.equals("release")) {
      this.releaseWakeLock();
      this.releaseWifiLock();
    } else if (action.equals("battery-optimization")) {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      String packageName = context.getPackageName();
      if (Build.VERSION.SDK_INT >= 23 && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);
      }
    }

    callbackContext.success();
    return true;
  }


  private void acquireWakeLock(int flags) {
    if (this.wakeLock == null) {
      this.wakeLock = this.powerManager.newWakeLock(flags, "AllLocksPlugin");
      this.wakeLock.acquire();
    }

  }

  private void acquireWifiLock() {
    if (this.wifiLock == null) {
      this.wifiLock = this.wifiManager.createWifiLock("wifiLock");
      this.wifiLock.acquire();
    }
  }


  private void releaseWakeLock() {
    if (this.wakeLock != null) {
      this.wakeLock.release();
      this.wakeLock = null;
    }

  }

  private void releaseWifiLock() {
    if (this.wifiLock != null) {
      this.wifiLock.release();
      this.wifiLock = null;
    }
  }


  @Override
  public void onPause(boolean multitasking) {
    if(this.releaseOnPause && this.wakeLock != null) {
      this.wakeLock.release();
    }

    super.onPause(multitasking);
  }

  @Override
  public void onResume(boolean multitasking) {
    if(this.releaseOnPause && this.wakeLock != null) {
      this.wakeLock.acquire();
    }

    super.onResume(multitasking);
  }
}
