package com.liuguilin.iot_wifi.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liuguilin.iot_wifi.MainActivity;
import com.liuguilin.iot_wifi.R;

/**
 * FileName: SplashActivity
 * Founder: LiuGuiLin
 * Create Date: 2019/2/18 16:14
 * Email: lgl@szokl.com.cn
 * Profile:
 */
public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_client;
    private Button btn_service;
    private static String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private int REQUEST_PERMISSION=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
////                ToastUtils.get().showText("获取权限失败,部分功能可能无法正常使用！");
//            } else {
////                gotNecessaryPermission = true;
//            }
//        }else {

        }
    }
    private void initView() {
        btn_client = (Button) findViewById(R.id.btn_client);
        btn_service = (Button) findViewById(R.id.btn_service);

        btn_client.setOnClickListener(this);
        btn_service.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_client:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.btn_service:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("type","service");
                startActivity(intent);
                break;
        }
    }
}
