package com.example.network.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "网络监听: NetworkReceiver";

    private NetState mLastNetState = NetState.NET_NO;
    private long mLastClearTimestamp;
    private String mNetworkId;


    private OnNetworkConnectedListener mListener;

    //必须添加默认构造方法
    public NetworkReceiver() {}

    public NetworkReceiver(OnNetworkConnectedListener listener) {
        this.mListener = listener;
    }

    public interface OnNetworkConnectedListener {
        void onNetworkConnected(String netName, String netId);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        NetState state = getCurrentNetState(context);
        if (null != state) {
            //打印网络的名称
            String name = getNetworkName(state.ordinal());
            Log.d(TAG, "onReceive: net state : " + name);

            String networkId = null;
            if (state == NetState.NET_2G
                    || state == NetState.NET_3G
                    || state == NetState.NET_4G
                    || state == NetState.NET_4G_plus
                    || state == NetState.NET_WIFI) {
                //可以做一些超时处理
                /*if (mLastNetState != state && System.currentTimeMillis() - mLastClearTimestamp > 5 * 1000) {
                    mLastClearTimestamp = System.currentTimeMillis();
                }*/

                //打印wifi的id
                networkId = state == NetState.NET_WIFI ? getNetworkId(context) : null;
                Log.d(TAG, "onReceive: wifi net work id : " + networkId);
            }

            //处理网络连接
            if (mListener != null && (TextUtils.isEmpty(mNetworkId)
                    || !mNetworkId.equals(networkId)
                    || TextUtils.isEmpty(networkId))) {
                mNetworkId = networkId;
                mListener.onNetworkConnected(name, networkId);
            }
        }
        Log.d(TAG, "onReceive: net state might be no or unknown");
        mLastNetState = state;
    }

    public String getNetworkId(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager == null ? null : wifiManager.getConnectionInfo();
        if (null != wifiInfo) {
            return String.valueOf(wifiInfo.getBSSID());
        }
        return null;
    }

    public String getNetworkName(int type) {
        if (type == NetState.NET_2G.ordinal()) {
            return "2G";
        } else if (type == NetState.NET_3G.ordinal()) {
            return "3G";
        } else if (type == NetState.NET_4G.ordinal()) {
            return "4G";
        } else if (type == NetState.NET_4G_plus.ordinal()) {
            return "4G+";
        } else if (type == NetState.NET_WIFI.ordinal()) {
            return "wifi";
        } else if (type == NetState.NET_NO.ordinal()) {
            return "没有网络连接";
        } else if (type == NetState.NET_UNKNOWN.ordinal()) {
            return "无法识别的网络";
        }
        return null;
    }


    public NetState getCurrentNetState(Context context) {
        NetState state = NetState.NET_NO; // 默认没有网络
        //获取网络监控服务
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        //如果网络已连接或者正在连接
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {
                //wifi
                case ConnectivityManager.TYPE_WIFI:
                    state = NetState.NET_WIFI;
                    break;
                //mobile net
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: //联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: //电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: //移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            state = NetState.NET_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: //电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            state = NetState.NET_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE: //4G
                            state = NetState.NET_4G;
                            break;
                        case 19: //4g+
                            state = NetState.NET_4G_plus;
                            break;
                        default:
                            state = NetState.NET_UNKNOWN;
                    }
                    break;
                default:
                    state = NetState.NET_UNKNOWN;
            }
        }
        return state;
    }
}
