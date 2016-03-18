package com.example.vk.wifi_test;

import android.app.ProgressDialog;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by vk on 2016/3/12.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener ,WifiP2pManager.ChannelListener{
    private WifiP2pDevice device = null;
    private View mContentView = null;
    private ProgressDialog progressDialog = null;
    private WifiP2pInfo info;
    private static final int CHOOSE_FILE_RESULT_CODE = 20;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO: handle Msg
        }
    };
    private TalkManager talkManager=null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);
            }
        });
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceListFragment.DeviceActionListener)getActivity()).disconnect();
            }
        });

        return mContentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: 2016/3/17
        Log.d("stop", "$DeviceDetailFragment @onDestroy}");
        talkManager.stop();
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            // TODO: receive a msg from other
            talkManager = new TalkManager(getActivity(),mHandler);
            talkManager.startReceive(8988);
        } else if (info.groupFormed) {
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources() .getString(R.string.client_text));
            //TODO: Click ,then send a msg to GroupOwner
            Button btn_send = (Button) mContentView.findViewById(R.id.btn_send);
            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (talkManager == null){
                        talkManager = new TalkManager(getActivity(), mHandler);
                        talkManager.startSendThread(DeviceDetailFragment.this.info.groupOwnerAddress.getHostAddress(),8988);
                        Log.d(MainActivity.TAG,"new and startSendThread");
                    }
                    String msg = "hello";
                    talkManager.send(msg);
                }
            });
        }
        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        mContentView.findViewById(R.id.linear_msg).setVisibility(View.VISIBLE);
    }

    @Override
    public void onChannelDisconnected() {

    }

    public void resetView() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
//        TextView tv = (TextView) mContentView.findViewById(R.id.device_address);
//        tv.setText("");
//        tv = (TextView) mContentView.findViewById(R.id.device_info);
//        tv.setText("");
//        tv = (TextView) mContentView.findViewById(R.id.group_owner);
//        tv.setText("");
//        tv = (TextView) mContentView.findViewById(R.id.group_ip);
//        tv.setText("");
        this.getView().setVisibility(View.GONE);
    }


}
