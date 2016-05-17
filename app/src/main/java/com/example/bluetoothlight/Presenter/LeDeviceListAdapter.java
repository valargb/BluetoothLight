package com.example.bluetoothlight.Presenter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bluetoothlight.Model.iBeaconClass.iBeacon;
import com.example.bluetoothlight.R;

import java.util.ArrayList;

/**
 * Created by 肥肥 on 2016/4/13.
 */
public class LeDeviceListAdapter extends BaseAdapter{

    // Adapter for holding devices found through scanning.
    private ArrayList<iBeacon> mLeDevices;
    private LayoutInflater mInflator;
    private Activity mContext;

    public LeDeviceListAdapter(Activity c){
        super();
        mContext = c;
        mLeDevices = new ArrayList<iBeacon>();
        mInflator = mContext.getLayoutInflater();
    }

    public void addDevice(iBeacon device){
        if (device == null){
            return;
        }

        for (int i = 0;i < mLeDevices.size();i++){
            String btAddress = mLeDevices.get(i).bluetoothAddress;
            //判断若设备已经在列表中则不添加
            if (btAddress.equals(device.bluetoothAddress)){
                mLeDevices.add(i + 1, device);
                mLeDevices.remove(i);
                return;
            }
        }
        mLeDevices.add(device);
    }

    public iBeacon getDevice(int position){
        return mLeDevices.get(position);
    }

    public void clear(){
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i){
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //General ListView optimization code.
        if (convertView == null){
            convertView = mInflator.inflate(R.layout.listitem_device,null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView)convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView)convertView.findViewById(R.id.device_name);
            viewHolder.deviceUUID = (TextView)convertView.findViewById(R.id.device_beacon_uuid);
            viewHolder.devicetxPower_RSSI = (TextView)convertView.findViewById(R.id.device_txPower_rssi);
            viewHolder.deviceMajor_Minor = (TextView)convertView.findViewById(R.id.device_major_minor);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        iBeacon device = mLeDevices.get(position);
        final String devicename = device.name;
        if (devicename != null && devicename.length() >= 0){
            viewHolder.deviceName.setText(devicename);
        }else {
            viewHolder.deviceName.setText(R.string.unknown_device);
        }
        if (device.isIbeacon){
            viewHolder.deviceName.append("[iBeacon]");
        }

        viewHolder.deviceAddress.setText(device.bluetoothAddress);
        viewHolder.deviceUUID.setText(device.proximityUuid);
        if (device.isIbeacon){
            viewHolder.deviceMajor_Minor.setText("major:"+device.major+" ,minor:"+device.minor);
            viewHolder.devicetxPower_RSSI.setText("txPower:" + device.txPower + ", rssi:" + device.rssi);
        }else{
            viewHolder.devicetxPower_RSSI.setText("rssi:" + device.rssi);
        }

// view.setBackgroundColor(Color.argb(255-iAlpha,255-iRed, 0xFF-iGreen,
        // 0xFF-iBlue));
        if (position % 2 == 0)// set color
        {
            convertView.setBackgroundColor(Color.argb(25, 255, 0, 0));
        } else {
            convertView.setBackgroundColor(Color.argb(25, 0, 255, 0));
        }
        return convertView;

    }


    class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceUUID;
        TextView deviceMajor_Minor;
        TextView devicetxPower_RSSI;
    }
}
