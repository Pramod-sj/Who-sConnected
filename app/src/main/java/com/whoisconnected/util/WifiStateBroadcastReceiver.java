package com.whoisconnected.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiStateBroadcastReceiver extends BroadcastReceiver {

    OnWifiStateChangeListener onWifiStateChangeListener;

    public WifiStateBroadcastReceiver(OnWifiStateChangeListener onWifiStateChangeListener) {
        this.onWifiStateChangeListener = onWifiStateChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean connected=networkInfo.isConnected();
            onWifiStateChangeListener.onChanged(connected);
        }
    }

    public interface OnWifiStateChangeListener{
        void onChanged(boolean isConnected);
    }
}
