package com.yl.dtd;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DtDService extends Service {
    private static final String TAG = "DtDService";

    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");
    private BluetoothAdapter mBluetoothAdapter;
    private List<String> bluetoothDevices = new ArrayList<String>();
    private InputStream is;//输出流
    private AcceptThread acceptThread;
    private final String NAME = "XT1097";

    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothDevices.add(device.getName() + ":" + device.getAddress());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
            }
        }
    };
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream os;//输出流

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //每搜索到一个设备就会发送一个该广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        //当全部搜索完后发送该广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        this.unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        //如果当前在搜索，就先取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //开启搜索
        mBluetoothAdapter.startDiscovery();

        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device.getName() + ":" + device.getAddress());
            }
        }

        return ret;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        //如果当前在搜索，就先取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //开启搜索
        mBluetoothAdapter.startDiscovery();

        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device.getName() + ":" + device.getAddress());
            }
        }
        return new DtDBinder();
    }

    public class DtDBinder extends Binder
    {
        public DtDService getServiceInstance()
        {
            return DtDService.this;
        }
    }

    //public void sendByBT(byte[] content) {
    public void sendByBT() {
        Log.d(TAG, "sendByBT ++");
        for (String btlist : bluetoothDevices) {
            String address = btlist.substring(btlist.indexOf(":") + 1).trim();//把地址解析出来
            //主动连接蓝牙服务端
            try {
                //判断当前是否正在搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                try {
                    if (device == null) {
                        //获得远程设备
                        device = mBluetoothAdapter.getRemoteDevice(address);
                    }
                    if (clientSocket == null) {
                        //创建客户端蓝牙Socket
                        clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                        clientSocket.connect();
                        //获得输出流（客户端指向服务端输出文本）
                        os = clientSocket.getOutputStream();
                    }
                } catch (Exception e) {
                }
                if (os != null) {
                    //往服务端写信息
//                    os.write(content);
                    os.write("蓝牙信息来了".getBytes("utf-8"));
                }

            } catch (Exception e) {
            }
        }
        Log.d(TAG, "sendByBT --");
    }

    //服务端监听客户端的线程类
    private class AcceptThread extends Thread {
        public AcceptThread() {
            try {

                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
            }
        }

        public void run() {
            Log.d(TAG, "AcceptThread start");
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();
                while (true) {
                    byte[] buffer = new byte[1024];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), String.valueOf(msg.obj),
                    Toast.LENGTH_LONG).show();
            super.handleMessage(msg);
        }
    };
}
