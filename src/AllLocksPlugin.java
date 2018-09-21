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
import android.util.Log;

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

    wifiManager = (WifiManager) cordova.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) throws JSONException {

    Log.d("AllLocks", "Executing "+action);
    Context context = cordova.getContext();
    switch (action) {
      case "acquire":
        acquireWifiLock();
        acquireWakeLock();
        break;
      case "release":
        releaseWifiLock();
        releaseWakeLock();
        break;
      case "battery-optimization":
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String packageName = context.getPackageName();
        if (Build.VERSION.SDK_INT >= 23 && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);
        }
        break;
    }

    callbackContext.success();
    return true;
  }


  private void acquireWakeLock() {
    if (wakeLock == null) {
      Log.d("WakeLock", "Acquring..");
      wakeLock = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AllLocksPluginWakeLock");
      wakeLock.acquire();
    }

  }

  private void acquireWifiLock() {
    if (wifiLock == null) {
      Log.d("WifiLock", "Acquring..");
      wifiLock = this.wifiManager.createWifiLock("AllLocksPluginWifiLock");
      wifiLock.acquire();
    }
  }


  private void releaseWakeLock() {
    if (wakeLock != null) {
      Log.d("WakeLock", "Releasing..");
      wakeLock.release();
      wakeLock = null;
    }

  }

  private void releaseWifiLock() {
    if (wifiLock != null) {
      Log.d("WifiLock", "Releasing..");
      wifiLock.release();
      wifiLock = null;
    }
  }


  @Override
  public void onPause(boolean multitasking) {
    if (releaseOnPause && wakeLock != null) {
      Log.d("WifiLock", "Releasing on pause..");
      wakeLock.release();
    }

    super.onPause(multitasking);
  }

  @Override
  public void onResume(boolean multitasking) {
    if (releaseOnPause && wakeLock != null) {
      Log.d("WifiLock", "Acquiring on resume..");
      this.wakeLock.acquire();
    }

    super.onResume(multitasking);
  }
}
