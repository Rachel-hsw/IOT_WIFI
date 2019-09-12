package com.liuguilin.iot_wifi.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import java.util.List;

/**
 * FileName: WifiManager
 * Founder: LiuGuiLin
 * Create Date: 2019/2/12 17:53
 * Email: lgl@szokl.com.cn
 * Profile:WIFI管理类
 */
public class WifiManager {

    private static WifiManager mInstance = null;

    private android.net.wifi.WifiManager mWifiManager;

    private WifiManager() {

    }

    public static WifiManager getInstance() {
        if (mInstance == null) {
            synchronized (WifiManager.class) {
                if (mInstance == null) {
                    mInstance = new WifiManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param mContext
     */
    public void init(Context mContext) {
        mWifiManager = (android.net.wifi.WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * WIFI是否打开
     *
     * @return
     */
    public boolean isWifiEnabled() {
        if (mWifiManager != null) {
            return mWifiManager.isWifiEnabled();
        }
        return false;
    }

    /**
     * WIFI 开关
     *
     * @param enabled
     */
    public void setWifiEnabled(boolean enabled) {
        if (mWifiManager != null) {
            mWifiManager.setWifiEnabled(enabled);
        }
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        if (mWifiManager != null) {
            mWifiManager.startScan();
        }
    }

    /**
     * 获取扫描结果
     *
     * @return
     */
    public List<ScanResult> getScanResult() {
        if (mWifiManager != null) {
            return mWifiManager.getScanResults();
        }
        return null;
    }

    /**
     * 获取热点IP
     *
     * @return
     */
    public String getHotIp() {
        DhcpInfo ipinfo = mWifiManager.getDhcpInfo();
        String ip = fixIp(ipinfo.serverAddress);
        return ip;
    }

    /**
     * int形式的ip转为字符串形式的ip
     *
     * @param i
     * @return
     */
    private String fixIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    /**
     * WIFI是否连接
     *
     * @param mContext
     * @return
     */
    public boolean isWifiConnect(Context mContext) {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * 创建WIFI信息
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password, String Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type.contains("WEP") || Type.contains("wep")) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (Type.contains("WPA") || Type.contains("wpa")) {
            config.preSharedKey = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    /**
     * 是否十六进制的密码
     *
     * @param wepKey
     * @return
     */
    private boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();
        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }
        return isHex(wepKey);
    }

    /**
     * 是否十六进制
     *
     * @param key
     * @return
     */
    private boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 连接WIFI
     *
     * @param mWifiConfiguration
     * @return
     */
    public boolean connetWifi(WifiConfiguration mWifiConfiguration) {
        int netID = mWifiManager.addNetwork(mWifiConfiguration);
        boolean enabled = mWifiManager.enableNetwork(netID, true);
        return enabled;
    }
}
