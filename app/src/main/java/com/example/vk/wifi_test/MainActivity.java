package com.example.vk.wifi_test;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.nio.channels.Channel;

public class MainActivity extends ActionBarActivity {
    private final IntentFilter mIntentFilter  = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    private final String LOGTEXT="logtext";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取了mManager ,mChannel
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        setWifiBroadCastRecvActions();

        mManager.discoverPeers( mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(LOGTEXT,"discoverPeers onSuccess");
            }
            @Override
            public void onFailure(int reasonCode) {
                Log.e(LOGTEXT,"discoverPeers onFailure");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setWifiBroadCastRecvActions() {
        //  指示　Wi-Fi P2P　是否开启
        mIntentFilter .addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // 代表对等节点（peer）列表发生了变化
        mIntentFilter .addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // 表明Wi-Fi P2P的连接状态发生了改变
        mIntentFilter .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // 指示设备的详细配置发生了变化
        mIntentFilter .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
