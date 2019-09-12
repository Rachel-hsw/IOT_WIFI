package com.liuguilin.iot_wifi.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.liuguilin.iot_wifi.MainActivity;
import com.liuguilin.iot_wifi.R;
import com.liuguilin.iot_wifi.adapter.ChatListAdapter;
import com.liuguilin.iot_wifi.manager.WifiManager;
import com.liuguilin.iot_wifi.model.ChatListModel;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * FileName: ChatActivity
 * Founder: LiuGuiLin
 * Create Date: 2019/2/18 16:48
 * Email: lgl@szokl.com.cn
 * Profile:
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int H_DATA = 1001;

    private static final int PORT = 5000;

    private Socket socket;
    private PrintStream output;

    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintStream out;

    private BufferedInputStream bufferedInputStream;
    private ReadThread readThread;

    private RecyclerView mChatRyView;
    private EditText et_text;
    private Button btn_send;

    private ChatListAdapter mChatListAdapter;
    private List<ChatListModel> mList = new ArrayList<>();

    private String type;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case H_DATA:
                    String text = (String) msg.obj;
                    addRight(text);
                    Toast.makeText(ChatActivity.this, "text：" + text, Toast.LENGTH_SHORT).show();
                    Log.e(MainActivity.TAG, "-3-------");
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {
        mChatRyView = (RecyclerView) findViewById(R.id.mChatRyView);
        et_text = (EditText) findViewById(R.id.et_text);
        btn_send = (Button) findViewById(R.id.btn_send);

        btn_send.setOnClickListener(this);

        mChatRyView.setLayoutManager(new LinearLayoutManager(this));
        mChatListAdapter = new ChatListAdapter(this, mList);
        mChatRyView.setAdapter(mChatListAdapter);

        type = getIntent().getStringExtra("type");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (type.equals("client")) {
                    if (!TextUtils.isEmpty(MainActivity.hot_ip)) {
                        initClient();
                    } else {
                        if (!TextUtils.isEmpty(WifiManager.getInstance().getHotIp())) {
                            MainActivity.hot_ip = WifiManager.getInstance().getHotIp();
                            initClient();
                        } else {
                            Toast.makeText(ChatActivity.this, "启动重连机制", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (type.equals("service")) {
                    initTopService();
                }
            }
        }).start();
    }

    private void initTopService() {
        try {
            serverSocket = new ServerSocket(PORT);
            Socket clientSocket = serverSocket.accept();
            String remoteIP = clientSocket.getInetAddress().getHostAddress();
            int remotePort = clientSocket.getLocalPort();
            Log.e(MainActivity.TAG, "A client connected. IP:" + remoteIP + ", Port: " + remotePort);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintStream(clientSocket.getOutputStream(), true, "utf-8");

            while (true) {
                String tmp = in.readLine();
                if (!TextUtils.isEmpty(tmp)) {
                    Message message = mHandler.obtainMessage();
                    message.obj = tmp;
                    message.what = H_DATA;
                    mHandler.sendMessage(message);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "serverSocket" + e.toString());
        }
    }

    private void initClient() {
        Log.e(MainActivity.TAG, "initClient" + MainActivity.hot_ip);
        try {
            socket = new Socket(MainActivity.hot_ip, PORT);
            output = new PrintStream(socket.getOutputStream(), true, "utf-8");
            initService();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "initClient" + e.toString());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //无限重连
                            initClient();
                        }
                    }).start();
                }
            },1000);
        }
    }

    private void initService() {
        Log.e(MainActivity.TAG, "initService");
        readThread = new ReadThread();
        readThread.start();
    }

    /**
     * 发送消息
     *
     * @param text
     */
    private void sendMsg(final String text) {

        if (type.equals("client")) {
            if (output == null) {runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatActivity.this, "未连接", Toast.LENGTH_SHORT).show();
                }
            });
                return;
            }
            output.println(text);
            addLeft(text);
        } else if (type.equals("service")) {
            Log.e(MainActivity.TAG, "service");
            if (out == null) {
                Toast.makeText(this, "未连接", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    out.println(text);
                    Log.e(MainActivity.TAG, "server send:" + text);
                }
            }).start();
            addLeft(text);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                final String text = et_text.getText().toString().trim();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg(text);
                    }
                }).start();
                et_text.setText("");
                break;
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            //不断读取
            while (true) {
                byte[] data = receiveData();
                if (data != null) {
                    if (data.length > 1) {
                        String text = new String(data);
                        Log.e(MainActivity.TAG, text);
                        Message message = new Message();
                        message.what = H_DATA;
                        message.obj = text;
                        mHandler.sendMessage(message);
                    }
                }
            }
        }
    }

    /**
     * 接收数据
     *
     * @return
     */
    public byte[] receiveData() {
        byte[] data = null;
        if (socket == null || socket.isClosed()) {
            try {
                Log.e(MainActivity.TAG,"receiveData:" + MainActivity.hot_ip);
                socket = new Socket(MainActivity.hot_ip, PORT);
            } catch (Exception e) {
                Log.e(MainActivity.TAG, "receiveData 1 " + e.toString());
            }
        }
        if (socket.isConnected()) {
            try {
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                data = new byte[bufferedInputStream.available()];
                bufferedInputStream.read(data);
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "receiveData 2" + e.toString());
            }
        } else {
            data = new byte[1];
        }
        return data;
    }

    private void addLeft(String text) {
        ChatListModel model = new ChatListModel();
        model.setType(ChatListAdapter.LEFT_TEXT);
        model.setLeftText(text);
        mList.add(model);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatListAdapter.notifyDataSetChanged();
            }
        });

    }

    private void addRight(String text) {
        ChatListModel model = new ChatListModel();
        model.setType(ChatListAdapter.RIGHT_TEXT);
        model.setRightText(text);
        mList.add(model);
        mChatListAdapter.notifyDataSetChanged();
    }
}
