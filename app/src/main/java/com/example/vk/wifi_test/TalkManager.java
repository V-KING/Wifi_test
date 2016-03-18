package com.example.vk.wifi_test;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by vk on 2016/3/17.
 */
public class TalkManager {
    public static final int TYPE_MSG = 0x11;
    private final Context mContext;
    private Handler mHandler;
    private RecvThread mRecvThread;
    private SendThread mSendThread;
    static String mMsg = null;

    public TalkManager(Context context, Handler handler) {
        super();
        mHandler = handler;
        mContext = context;
    }

    public void startReceive(int port) {
        mRecvThread = new RecvThread(port);
        mRecvThread.start();
    }

    public void send(String msg) {
        mMsg = msg;
    }

    public void startSendThread(String addr, int port) {
        mSendThread = new SendThread(addr, port);
        mSendThread.start();
    }

    public void stop() {
        Log.d(MainActivity.TAG, "TalkManager stop");
        if (mRecvThread != null) {
            mRecvThread.cancel();
            mRecvThread.interrupt();
            mRecvThread = null;
        }
        if (mSendThread != null) {
            mSendThread.cancel();
            mSendThread.interrupt();
            mSendThread = null;
        }
    }

    private class SendThread extends Thread {
        Socket mmSocket = new Socket();
        String mmAddr = null;
        int mmPort;
        OutputStream mmOutputStream = null;

        public SendThread(String addr, int port) {
            super();
            mmAddr = addr;
            mmPort = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                mmSocket.bind(null);
                mmSocket.connect(new InetSocketAddress(mmAddr, mmPort), 5000);
                Log.d(MainActivity.TAG, "client: connected");
                mmOutputStream = mmSocket.getOutputStream();
                Log.d(MainActivity.TAG, "@SendThread :get output Stream");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    if (mMsg != null) {
                        Log.d(MainActivity.TAG, "@SendThread :" + mMsg);
                        mmOutputStream.write(mMsg.getBytes());
                        Log.d(MainActivity.TAG, "@SendThread :write");
                        mMsg = null;
                    }
                } catch (IOException e) {
                    try {
                        mmOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                } finally {
                    mMsg = null;
                }
            }
        }

        public void cancel() {
            try {
                mmOutputStream.close();
                if (mmSocket != null) {
                    if (mmSocket.isConnected()) {
                        mmSocket.close();
                    }
                }
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "@SendThread close() socket failed:", e);
            }
        }
    }

    private class RecvThread extends Thread {
        private InputStream mmInput;
        byte[] buffer = new byte[1024];
        ServerSocket mmServerSocket;
        Socket mmClient;

        public RecvThread(int port) {
            super();
            try {
                mmServerSocket = new ServerSocket(port);
                Log.d(MainActivity.TAG, "Server: Socket opened");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                mmClient = mmServerSocket.accept();
                Log.d(MainActivity.TAG, "Server: Socket get client");
                mmInput = mmClient.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    if (mmInput.available()>0) {
                        int bytes = mmInput.read(buffer);
                        String msg = new String(buffer, 0, bytes);
                        Log.d(MainActivity.TAG, "Server Recv: " + msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                mmInput.close();
                if (mmServerSocket != null) {
                    if (!mmServerSocket.isClosed()) {
                        mmServerSocket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
