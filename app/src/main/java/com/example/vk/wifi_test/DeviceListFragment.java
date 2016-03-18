package com.example.vk.wifi_test;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vk on 2016/3/10.
 */
public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device;
    private View mContentView = null;
    private ProgressDialog mProgressDialog = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list,null);
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        // If an AdapterView is backed by this data, notify it
        // of the change.  For instance, if you have a ListView of available
        // peers, trigger an update.
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(MainActivity.TAG, "No devices found");
            return;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener)getActivity()).showDetails(device);//这个方法在外面谁implement谁实现
    }

    /**
     * Update UI for this device.更新本机设备的ui
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        //TextView tv = (TextView) getActivity().findViewById(R.id.my_name);
        TextView tv = (TextView) mContentView.findViewById(R.id.my_name);
        tv.setText(device.deviceName);
        tv = (TextView) mContentView.findViewById(R.id.my_status);
        tv.setText(getDeviceStatus(device.status));
    }

    public void onInitiateDiscovery() {
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }

        mProgressDialog= ProgressDialog.show(getActivity(), "按返回键取消", "正在寻找配对设备..", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
    }

    public void cleerPeers() {
        peers.clear();
        ((WiFiPeerListAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private List<WifiP2pDevice>  items;
        /**
         * @param context
         * @param viewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int viewResourceId, List<WifiP2pDevice> objects) {
            super(context, viewResourceId, objects);
            items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v= convertView;
            if (v==null){
                v = getActivity().getLayoutInflater().inflate(R.layout.row_devices, null);
            }

            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;
        }
    }

    private static  String getDeviceStatus(int status) {
        Log.d(MainActivity.TAG, "Peer status :" + status);
        switch (status) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    /**
     * 为Activity准备的接口回调函数，为了监听fragment的互动事件
     */
    public interface DeviceActionListener{
        void connect(WifiP2pConfig config);
        void disconnect();
        void showDetails(WifiP2pDevice device);
    }
}
