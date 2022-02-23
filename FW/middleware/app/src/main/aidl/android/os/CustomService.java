package android.os;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yl.middleware.RemoteIntent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CustomService extends ICustomService.Stub {
    private static final String TAG = "DtDService";
    private static final boolean DEBUG = false;
    private static final boolean MEASURE = true;
    private static final int ACTIVITY = 0;
    private static final int BROADCAST = 1;

    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");

    private final UUID MY_UUID_BROADCAST = UUID
            .fromString("abcd0000-ab00-ab00-ab00-abcdef000000");
    private final String NAME = "XT1097";
    private BluetoothAdapter mBluetoothAdapter;
    //    private List<String> bluetoothDevices = new ArrayList<String>();
    private Set<String> bluetoothDevices = new HashSet<>();
    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive");
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
    private InputStream is;//输出流
    private AcceptThread acceptThread;
    private AcceptBroadcastThread acceptTBroadcastThread;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream os;//输出流
    private Context mContext;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
//            Log.d(TAG, "reseive data" + msg.obj);
//            Toast.makeText(mContext, String.valueOf(msg.obj),
//                    Toast.LENGTH_SHORT).show();
            int what = msg.what;
            switch (what) {
                case ACTIVITY:
                    try {
                        Bundle b = msg.getData();
                        byte[] hostIntent = b.getByteArray("intent");

                        Parcel p = Parcel.obtain();
                        p.unmarshall(hostIntent, 0, hostIntent.length);
                        p.setDataPosition(0);

                        RemoteIntent originalhostIntent = RemoteIntent.CREATOR.createFromParcel(p);
                        //originalhostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (DEBUG) {
                            Log.d(TAG, "originalhostIntent.mAction" + originalhostIntent.mAction);
                            Log.d(TAG, "originalhostIntent.mCategories" + originalhostIntent.mCategories);
                        }
                        Intent test_intent = originalhostIntent.backtoIntent();
                        test_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // need to check whether it is for start activity or send broadcast
                        if (MEASURE) Log.d(TAG, "BT receiving Activity data " + System.currentTimeMillis());
                        mContext.startActivity(test_intent);

                    } catch (Exception e) {
                        if (DEBUG) Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case BROADCAST:
                    try {
                        Bundle b = msg.getData();
                        byte[] hostIntent = b.getByteArray("intent");

                        Parcel p = Parcel.obtain();
                        p.unmarshall(hostIntent, 0, hostIntent.length);
                        p.setDataPosition(0);

                        RemoteIntent originalhostIntent = RemoteIntent.CREATOR.createFromParcel(p);
                        //originalhostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (DEBUG) {
                            Log.d(TAG, "originalhostIntent.mAction" + originalhostIntent.mAction);
                            Log.d(TAG, "originalhostIntent.mCategories" + originalhostIntent.mCategories);
                        }
                        Intent test_intent = originalhostIntent.backtoIntent();
                        test_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // need to check whether it is for start activity or send broadcast
                        test_intent.putExtra("IM", true);
                        if (MEASURE) Log.d(TAG, "BT receiving Broadcast data " + System.currentTimeMillis());
                        mContext.sendBroadcast(test_intent);

                    } catch (Exception e) {
                        if (DEBUG) Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                    break;
            }

//            try {
//                Bundle b = msg.getData();
//                byte[] hostIntent = b.getByteArray("intent");
//
//                /*
//                Parcel p = Parcel.obtain();
//                p.unmarshall(hostIntent, 0, hostIntent.length);
//                p.setDataPosition(0);
//                Log.d(TAG, "TAG PolyIntent.CREATOR.createFromParcel(p)");
//                Intent originalhostIntent = Intent.CREATOR.createFromParcel(p);
//
//                mContext.startActivity(originalhostIntent);
//                */
//
//                Parcel p = Parcel.obtain();
//                p.unmarshall(hostIntent, 0, hostIntent.length);
//                p.setDataPosition(0);
//                Log.d(TAG, "TAG PolyIntent.CREATOR.createFromParcel(p)");
//                RemoteIntent originalhostIntent = RemoteIntent.CREATOR.createFromParcel(p);
//                //originalhostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Log.d(TAG, "originalhostIntent.mAction" + originalhostIntent.mAction);
//                Log.d(TAG, "originalhostIntent.mCategories" + originalhostIntent.mCategories);
//
//                Intent test_intent = originalhostIntent.backtoIntent();
//                test_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Log.d(TAG, "mContext.startActivity(test_intent)");
//                // need to check whether it is for start activity or send broadcast
//                mContext.startActivity(test_intent);
//
//            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
//                e.printStackTrace();
//            }


//            super.handleMessage(msg);


//            Log.d(TAG, "reseive data");
//            Bundle b = msg.getData();
//            byte[] hostIntent = b.getByteArray("intent");
//
////            byte[] hostIntent = (byte[]) msg.obj;
//            Parcel p = Parcel.obtain();
//            p.unmarshall(hostIntent, 0, hostIntent.length);
//            p.setDataPosition(0);
//            Log.d("Xposed", "PolyIntent.CREATOR.createFromParcel(p)");
//            Intent originalhostIntent = Intent.CREATOR.createFromParcel(p);
//
//            mContext.startActivity(originalhostIntent);
//
//            super.handleMessage(msg);
        }
    };


    public CustomService(Context context) {
        mContext = context;
    }

    @Override
    public String sayHello() throws RemoteException {
        return "Just Hello World";
    }

    //ActivityManagerService的systemReady在所有服务初始化完成后触发，这定义这个是为了实现自定义服务的初始化代码实现
    public void systemReady() {

    }

    @Override
    public void startBTConnection() {
        if (DEBUG) Log.d(TAG, "startBTConnection");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//       TODO connection 的时候可以实现diff-hallman protocol来synchorize keys
//        //每搜索到一个设备就会发送一个该广播
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        this.registerReceiver(receiver, filter);
//        //当全部搜索完后发送该广播
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        this.registerReceiver(receiver, filter);

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
        if (acceptThread == null || !acceptThread.isAlive()) {
            if (DEBUG) Log.d(TAG, "acceptThread.isAlive()");
            //        acceptThread.stop();
        }

        acceptThread = new AcceptThread();
        acceptThread.start();

        acceptTBroadcastThread = new AcceptBroadcastThread();
        acceptTBroadcastThread.start();
    }

    @Override
    public void stopBTConnection() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        if (acceptThread == null || !acceptThread.isAlive()) {
            if (DEBUG) Log.d(TAG, "acceptThread.isAlive()");
            acceptThread.stop();
        }

        if (acceptTBroadcastThread == null || !acceptTBroadcastThread.isAlive()) {
            if (DEBUG) Log.d(TAG, "acceptTBroadcastThread.isAlive()");
            acceptTBroadcastThread.stop();
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    @Override
    public void sendBroadcastByBT(byte[] content) {
        if (DEBUG) Log.d(TAG, "sendBroadcastByBT ++");
        for (String btlist : bluetoothDevices) {
            String address = btlist.substring(btlist.indexOf(":") + 1).trim();//把地址解析出来
            if (DEBUG) Log.d(TAG, "address = " + address);
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
                        clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID_BROADCAST);
                        //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                        clientSocket.connect();
                        //获得输出流（客户端指向服务端输出文本）
                        os = clientSocket.getOutputStream();
                    }
                } catch (Exception e) {
                    if (DEBUG) Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
                if (os != null) {

                    if (MEASURE) Log.d(TAG, "BT sending Broadcast data " + System.currentTimeMillis());
                    //往服务端写信息
                    os.write(content);
                    //os.write("蓝牙信息来了".getBytes("utf-8"));
                    //FIXME below lines shout NOT be added otherwise it generates errors:
                    // D/DtDService(  928): socket closed
                    // W/System.err(  928): java.io.IOException: socket closed
                    //os.flush();
                    //os.close();
                }

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        if (DEBUG) Log.d(TAG, "sendBroadcastByBT --");
    }

    //public void sendByBT(byte[] content) {
    // public void sendByBT() {
    @Override
    public void sendByBT(byte[] content) {
        if (DEBUG) Log.d(TAG, "sendByBT ++");
        for (String btlist : bluetoothDevices) {
            String address = btlist.substring(btlist.indexOf(":") + 1).trim();//把地址解析出来
            if (DEBUG) Log.d(TAG, "address = " + address);
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
                    if (DEBUG) Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
                if (os != null) {
                    if (MEASURE) Log.d(TAG, "BT sending Activity data " + System.currentTimeMillis());
                    //往服务端写信息
                    os.write(content);
                    //os.write("蓝牙信息来了".getBytes("utf-8"));
                    //FIXME below lines shout NOT be added otherwise it generates errors:
                    // D/DtDService(  928): socket closed
                    // W/System.err(  928): java.io.IOException: socket closed
                    //os.flush();
                    //os.close();
                }

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        if (DEBUG) Log.d(TAG, "sendByBT --");
    }

    //服务端监听客户端的线程类
    private class AcceptBroadcastThread extends Thread {
        public AcceptBroadcastThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID_BROADCAST);
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        public void run() {
            if (DEBUG) Log.d(TAG, "AcceptBroadcastThread start");
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();

//                if (MEASURE) Log.d(TAG, "BT receiving Broadcast data " + System.currentTimeMillis());

                while (true) {
                    byte[] buffer = new byte[1024];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.what = BROADCAST;
                    Bundle bundle = new Bundle();
                    byte[] dest = new byte[count];
                    System.arraycopy(buffer, 0, dest, 0, count);
                    if (DEBUG) Log.d(TAG, "dest lengh = " + dest.length + "count = " + count);
                    bundle.putByteArray("intent", dest);
                    msg.setData(bundle);
//                    if (MEASURE) Log.d(TAG, "BT receiving Broadcast data " + System.currentTimeMillis());
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //服务端监听客户端的线程类
    private class AcceptThread extends Thread {
        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        public void run() {
            if (DEBUG) Log.d(TAG, "AcceptThread start");
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();

//                if (MEASURE) Log.d(TAG, "BT receiving Activity data " + System.currentTimeMillis());

//                while (true) {
//                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//                    byte[] data = new byte[1024];
//                    int len = 0;
//
//                    while ((len = is.read(data)) != -1) {
//
//                        outStream.write(data, 0, len);
//
//                    }
//                    Message msg = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putByteArray("intent", outStream.toByteArray());
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                    outStream.close();
//                }

//                is.close();


                while (true) {
                    byte[] buffer = new byte[1024];

                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.what = ACTIVITY;
                    ////////////////////////////////////////
                    Bundle bundle = new Bundle();
                    byte[] dest = new byte[count];
                    System.arraycopy(buffer, 0, dest, 0, count);
                    if (DEBUG) Log.d(TAG, "dest lengh = " + dest.length + "count = " + count);

                    bundle.putByteArray("intent", dest);
                    msg.setData(bundle);
                    //////////////////////////////////////////
//                    msg.obj = buffer;
//                    msg.obj = new String(buffer, 0, count, "utf-8");
//                    Log.d(TAG, "send message" + msg.obj);
//                    if (MEASURE) Log.d(TAG, "BT receiving Activity data " + System.currentTimeMillis());
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }


}
