package com.restaumatic.alllocks;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

  private NotificationManager notificationManager = null;

  public static final int NOTIFICATION_ID = 1232145;
  public static final String NOTIFICATION_TITLE = "Battery";
  public static final String NOTIFICATION_TEXT = "Please disable battery optimizations";

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    wifiManager = (WifiManager) cordova.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    powerManager = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
    notificationManager = (NotificationManager) cordova.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) {

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
        // Create notification to disable battery optimizations if they are enabled
        if (Build.VERSION.SDK_INT >= 23
                && !powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
          String title = args.optString(0, NOTIFICATION_TITLE);
          String text = args.optString(1, NOTIFICATION_TEXT);
          Intent intent = new Intent();
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);

          PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, 0);

          Notification notification = new Notification.Builder(context)
                  .setContentTitle(title)
                  .setContentText(text)
                  .setOngoing(true)
                  .setAutoCancel(true)
                  .setSmallIcon(context.getApplicationInfo().icon)
                  .setContentIntent(pendingIntent)
                  .setPriority(Notification.PRIORITY_MAX)
                  .setVibrate(new long[] {1000, 1000})
                  .build();

          notificationManager.notify(NOTIFICATION_ID, notification);
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
