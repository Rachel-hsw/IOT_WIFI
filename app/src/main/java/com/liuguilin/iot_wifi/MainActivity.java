package com.liuguilin.iot_wifi;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liuguilin.iot_wifi.adapter.WifiListAdapter;
import com.liuguilin.iot_wifi.manager.WifiManager;
import com.liuguilin.iot_wifi.model.WifiListModel;
import com.liuguilin.iot_wifi.ui.ChatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


/**
 * IOT系列博客 —— WIFI 通讯
 * 作者：刘桂林
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "IOT_WIFI";

    private static boolean isConn = false;

    private RecyclerView mWifiListRyView;
    private List<WifiListModel> mList = new ArrayList<>();
    private WifiListAdapter mWifiListAdapter;

    private WifiListReceiver mWifiListReceiver;

    private Dialog mPWDialog;
    private EditText et_pw;
    private TextView tv_connet;
    private TextView tv_cancel;

    private WifiListModel model;

    public static String hot_ip ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE}, 1001);
        }

        initView();
    }

    private void initView() {

        initPwDialog();

        WifiManager.getInstance().init(this);

        mWifiListRyView = (RecyclerView) findViewById(R.id.mWifiListRyView);
        mWifiListRyView.setLayoutManager(new LinearLayoutManager(this));
        mWifiListAdapter = new WifiListAdapter(this, mList);
        mWifiListRyView.setAdapter(mWifiListAdapter);

        mWifiListAdapter.setOnItemClickListener(new WifiListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(int i) {
                model = mList.get(i);
                if (model.getState()) {
                    isPwDialog(true);
                }
            }
        });

        mWifiListReceiver = new WifiListReceiver();
        IntentFilter filter = new IntentFilter();
        //搜索结果
        filter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //WIFI改变
        filter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
        //网络改变
        filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //WIFI状态
        filter.addAction(android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mWifiListReceiver, filter);

        if (WifiManager.getInstance().isWifiConnect(this)) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("type","client");
            startActivity(intent);
            finish();
            return;
        }
        if (!WifiManager.getInstance().isWifiEnabled()) {
            WifiManager.getInstance().setWifiEnabled(true);
        }

        WifiManager.getInstance().startScan();
    }

    private void initPwDialog() {
        mPWDialog = new Dialog(this);
        mPWDialog.setContentView(R.layout.layout_dialog_pw_input);
        et_pw = mPWDialog.findViewById(R.id.et_pw);
        tv_connet = mPWDialog.findViewById(R.id.tv_connet);
        tv_cancel = mPWDialog.findViewById(R.id.tv_cancel);

        tv_connet.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
    }

    /**
     * 是否显示dialog
     *
     * @param isShow
     */
    private void isPwDialog(boolean isShow) {
        if (mPWDialog == null) {
            return;
        }
        if (isShow) {
            if (!mPWDialog.isShowing()) {
                mPWDialog.show();
            }
        } else {
            if (mPWDialog.isShowing()) {
                mPWDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiListReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_connet:
                String pw = et_pw.getText().toString().trim();
                if (!TextUtils.isEmpty(pw)) {
                    Toast.makeText(this, "正在连接...", Toast.LENGTH_SHORT).show();
                    isConn = true;
                    isPwDialog(false);
                    WifiConfiguration configuration = WifiManager.getInstance().createWifiInfo(model.getName(), pw, model.getType());
                    WifiManager.getInstance().connetWifi(configuration);
                } else {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_cancel:
                isPwDialog(false);
                break;
        }
    }

    class WifiListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                List<ScanResult> list = WifiManager.getInstance().getScanResult();
                for (int i = 0; i < list.size(); i++) {
                    ScanResult result = list.get(i);
                    showLog("result:" + result.toString());
                    if (result != null) {
                        if(!TextUtils.isEmpty(result.SSID)){
                            Log.e(TAG, result.toString());
                            WifiListModel model = new WifiListModel();
                            model.setName(result.SSID);
                            model.setLevel(result.level);
                            model.setIp(result.BSSID);
                            model.setType(result.capabilities);
                            model.setState(!TextUtils.isEmpty(result.capabilities));
                            mList.add(model);
                        }
                    }
                }
                mWifiListAdapter.notifyDataSetChanged();
            } else if (android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                //获取wifi状态
                int wifiState = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, android.net.wifi.WifiManager.WIFI_STATE_DISABLING);
                switch (wifiState) {
                    case android.net.wifi.WifiManager.WIFI_STATE_DISABLED:
                        showLog("wifi已经关闭");
                        break;
                    case android.net.wifi.WifiManager.WIFI_STATE_DISABLING:
                        showLog("wifi正在关闭");
                        break;
                    case android.net.wifi.WifiManager.WIFI_STATE_ENABLED:
                        showLog("wifi已经开启");
                        break;
                    case android.net.wifi.WifiManager.WIFI_STATE_ENABLING:
                        showLog("wifi正在开启");
                        break;
                }
            } else if (android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                //网络状态改变
                NetworkInfo info = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
                if (NetworkInfo.State.DISCONNECTED.equals(info.getState())) {
                    showLog("wifi网络连接断开");
                } else if (NetworkInfo.State.CONNECTED.equals(info.getState())) {
                    showLog("获取当前网络，wifi名称");
                    hot_ip = WifiManager.getInstance().getHotIp();
                    if(isConn){
                        Intent i = new Intent(MainActivity.this, ChatActivity.class);
                        i.putExtra("type","client");
                        startActivity(i);
                        isConn = false;
                    }
                }
            } else if (android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                //wifi密码错误广播
                SupplicantState netNewState = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NEW_STATE);
                //错误码
                int netConnectErrorCode = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR, android.net.wifi.WifiManager.ERROR_AUTHENTICATING);
                showLog("netConnectErrorCode:" + netConnectErrorCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WifiManager.getInstance().startScan();
    }

    private void showLog(String text) {
        Log.e(TAG, text);
    }

}
