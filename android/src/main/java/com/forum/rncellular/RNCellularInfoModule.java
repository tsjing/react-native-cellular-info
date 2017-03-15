
package com.forum.rncellular;


import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import static com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.net.ConnectivityManagerCompat;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Locale;

public class RNCellularInfoModule extends ReactContextBaseJavaModule  implements LifecycleEventListener {

  public static int NOT_REACHABLE = 0;
  public static int REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
  public static int REACHABLE_VIA_WIFI_NETWORK = 2;

  public static final String WIFI = "wifi";
  public static final String WIMAX = "wimax";
  // mobile
  public static final String MOBILE = "mobile";

  // Android L calls this Cellular, because I have no idea!
  public static final String CELLULAR = "cellular";
  // 2G network types
  public static final String TWO_G = "2g";
  public static final String GSM = "gsm";
  public static final String GPRS = "gprs";
  public static final String EDGE = "edge";
  // 3G network types
  public static final String THREE_G = "3g";
  public static final String CDMA = "cdma";
  public static final String UMTS = "umts";
  public static final String HSPA = "hspa";
  public static final String HSUPA = "hsupa";
  public static final String HSDPA = "hsdpa";
  public static final String ONEXRTT = "1xrtt";
  public static final String EHRPD = "ehrpd";
  // 4G network types
  public static final String FOUR_G = "4g";
  public static final String LTE = "lte";
  public static final String UMB = "umb";
  public static final String HSPA_PLUS = "hspa+";
  // return type
  public static final String TYPE_UNKNOWN = "unknown";
  public static final String TYPE_ETHERNET = "ethernet";
  public static final String TYPE_ETHERNET_SHORT = "eth";
  public static final String TYPE_WIFI = "wifi";
  public static final String TYPE_2G = "2g";
  public static final String TYPE_3G = "3g";
  public static final String TYPE_4G = "4g";
  public static final String TYPE_NONE = "none";

  private final ReactApplicationContext reactContext;

  private static final String LOG_TAG = "RNCellularInfo";

  private static final String CONNECTION_TYPE_NONE = "NONE";
  private static final String CONNECTION_TYPE_UNKNOWN = "UNKNOWN";
  private static final String MISSING_PERMISSION_MESSAGE =
          "To use RNCellularInfo on Android, add the following to your AndroidManifest.xml:\n" +
                  "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />";

  private static final String ERROR_MISSING_PERMISSION = "E_MISSING_PERMISSION";

  private ConnectivityManager connectivityManager;
  private ConnectivityBroadcastReceiver connectivityBroadcastReceiver;
  private String connectivityType;
  private boolean noNetworkPermission = false;
  private boolean ready = false;

  public RNCellularInfoModule(ReactApplicationContext reactContext) {
    super(reactContext);
    connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();
    this.reactContext = reactContext;
    registerReceiver();
  }

  @ReactMethod
  public void getConnectionType(Promise promise) {
    if (noNetworkPermission) {
      promise.reject(ERROR_MISSING_PERMISSION, MISSING_PERMISSION_MESSAGE, null);
      return;
    }
    String currentConnectivity = getCurrentConnectionType();
    promise.resolve(createConnectivityEventMap(currentConnectivity));
  }

  @ReactMethod
  public void isConnectionMetered(Promise promise) {
    if (noNetworkPermission) {
      promise.reject(ERROR_MISSING_PERMISSION, MISSING_PERMISSION_MESSAGE, null);
      return;
    }
    promise.resolve(ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager));
  }

  @Override
  public String getName() {
    return "RNCellularInfo";
  }

  @Override
  public void onHostResume() {
    registerReceiver();
  }

  @Override
  public void onHostPause() {
    unregisterReceiver();
  }

  @Override
  public void onHostDestroy() {

  }

  @Override
  public void initialize() {
    getReactApplicationContext().addLifecycleEventListener(this);
    ready = true;
  }

  private void registerReceiver() {
    //Log.d("RNCellularInfo", "register");
    IntentFilter filter = new IntentFilter();
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    getReactApplicationContext().registerReceiver(connectivityBroadcastReceiver, filter);
    connectivityBroadcastReceiver.setRegistered(true);
  }

  private void unregisterReceiver() {
    //Log.d("RNCellularInfo", "unregister");

    if (connectivityBroadcastReceiver.isRegistered()) {
      getReactApplicationContext().unregisterReceiver(connectivityBroadcastReceiver);
      connectivityBroadcastReceiver.setRegistered(false);
    }
  }

  private void updateAndSendConnectionType() {
    String currentConnectivity = getCurrentConnectionType();
    //Log.d("RNCellularInfo", currentConnectivity);

    // It is possible to get multiple broadcasts for the same connectivity change, so we only
    // update and send an event when the connectivity has indeed changed.
    if (!currentConnectivity.equalsIgnoreCase(connectivityType)) {
      connectivityType = currentConnectivity;
      sendConnectivityChangedEvent(connectivityType);
    }
  }

  private void sendConnectivityChangedEvent(String connectivityType) {
    //Log.d("RNCellularInfo", "Changed to:" + connectivityType);
    if (ready) {
      getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
              .emit("connectionTypeUpdated", createConnectivityEventMap(connectivityType));
    } else {
      Log.d("RNCellularInfo", "Tried to emit connectionTypeUpdated event, but not yet ready.");
    }
  }

  private WritableMap createConnectivityEventMap(String type) {
    WritableMap event = new WritableNativeMap();
    event.putString("type", type);
    return event;
  }

  private String getCurrentConnectionType() {
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    return mapType(networkInfo);
  }

  private String mapType(NetworkInfo networkInfo) {
    try {
      if (networkInfo == null || !networkInfo.isConnected()) {
        return TYPE_NONE;
      } else if (ConnectivityManager.isNetworkTypeValid(networkInfo.getType())) {
        String type = networkInfo.getTypeName().toLowerCase(Locale.US);
        if (type.equals(WIFI)) {
          return TYPE_WIFI;
        }
        else if (type.toLowerCase().equals(TYPE_ETHERNET) || type.toLowerCase().startsWith(TYPE_ETHERNET_SHORT)) {
          return TYPE_ETHERNET;
        }
        else if (type.equals(MOBILE) || type.equals(CELLULAR)) {
          type = networkInfo.getSubtypeName().toLowerCase(Locale.US);
          if (type.equals(GSM) ||
                  type.equals(GPRS) ||
                  type.equals(EDGE) ||
                  type.equals(TWO_G)) {
            return TYPE_2G;
          }
          else if (type.startsWith(CDMA) ||
                  type.equals(UMTS) ||
                  type.equals(ONEXRTT) ||
                  type.equals(EHRPD) ||
                  type.equals(HSUPA) ||
                  type.equals(HSDPA) ||
                  type.equals(HSPA) ||
                  type.equals(THREE_G)) {
            return TYPE_3G;
          }
          else if (type.equals(LTE) ||
                  type.equals(UMB) ||
                  type.equals(HSPA_PLUS) ||
                  type.equals(FOUR_G)) {
            return TYPE_4G;
          }
        }
      } else {
        return TYPE_UNKNOWN;
      }
    } catch (SecurityException e) {
      noNetworkPermission = true;
      return TYPE_UNKNOWN;
    }
    return TYPE_UNKNOWN;
  }

  /**
   * Class that receives intents whenever the connection type changes.
   * NB: It is possible on some devices to receive certain connection type changes multiple times.
   */
  public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    //TODO: Remove registered check when source of crash is found. t9846865
    private boolean isRegistered = false;

    public void setRegistered(boolean registered) {
      isRegistered = registered;
    }

    public boolean isRegistered() {
      return isRegistered;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      //Log.d("RNCellularInfo", "onReceive");
      //Log.d("RNCellularInfo", intent.getAction().toString());

      if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        updateAndSendConnectionType();
      }
    }
  }

}