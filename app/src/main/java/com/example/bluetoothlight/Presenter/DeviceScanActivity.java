package com.example.bluetoothlight.Presenter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetoothlight.Model.BluetoothLeClass;
import com.example.bluetoothlight.Model.LogUtil;
import com.example.bluetoothlight.Model.ToastUtils;
import com.example.bluetoothlight.Model.Utils;
import com.example.bluetoothlight.Model.iBeaconClass;
import com.example.bluetoothlight.Model.iBeaconClass.iBeacon;
import com.example.bluetoothlight.R;

import java.util.List;

/**
 * Created by 肥肥 on 2016/4/14.
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private final static String TAG = "DeviceScanActivity";// DeviceScanActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR1 = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR2 = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR3 = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR4 = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR5 = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR7 = "0000fff7-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR8 = "0000fff8-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR9 = "0000fff9-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHARA = "0000fffa-0000-1000-8000-00805f9b34fb";

    public static BluetoothGattCharacteristic gattCharacteristic_keydata = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char1 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char2 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char3 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char4 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char5 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char6 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char7 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char8 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char9 = null;
    public static BluetoothGattCharacteristic gattCharacteristic_charA = null;

    private LeDeviceListAdapter mLeDeviceListAdapter = null;
    //搜素BLE终端
    private BluetoothAdapter mBluetoothAdapter;
    //读写BLE终端
    static private BluetoothLeClass mBLE;
    public String bluetoothAddress;
    private boolean mScanning;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mLeDeviceListAdapter = new LeDeviceListAdapter(DeviceScanActivity.this);
            setListAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
            LogUtil.e(TAG,"线程中更新UI");
        }
    };

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle("正在扫描设备。。。");

        // Use this check to determine whether BLE is supported on the device.Then you can selectively disable BLE-related features.
        //查看设备是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.ble_not_supported,Toast.LENGTH_SHORT).show();
            finish();
        }else{
            LogUtil.i(TAG,"initialize Bluetooth,has BLE system");
        }

        //android6.0使用beacon位置权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Android M Permission check
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(R.string.ALLOW, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }


        // Initializes a Bluetooth adapter. For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null){
            Toast.makeText(this,R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else{
            LogUtil.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        }

        //打开蓝牙
        mBluetoothAdapter.enable();
        LogUtil.i(TAG, "mBluetoothAdapter.enable");

        mBLE = new BluetoothLeClass(this);
        if (! mBLE.initialize()){
            LogUtil.e(TAG,"Unable to initialize Bluetooth");
            finish();
        }
        LogUtil.i(TAG, "mBLE = e" + mBLE);

        //发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);

        //收到BLE终端数据交互的事件回调
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
    }

    //反对Android M 所申请允许的权限
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults){
        switch (requestCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    LogUtil.e(TAG,"coarse location permission granted");
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not ben granted ,this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(R.string.ALLOW, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                        @Override
                        public void onDismiss(DialogInterface dialog) {}
                    });
                    builder.show();
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e(TAG, "onStart----------------------------------onStart");
    }

    @Override
    protected void onResume() {
        LogUtil.e(TAG, "onResume----------------------------------onResume");
        super.onResume();
        //服务关闭(为什么ondestroy那里关闭一次这里还要关闭？)
        mBLE.close();
        //Initializes list view adapter
        mHandler.postDelayed(mRunnable,5000);
    }

    @Override
    protected void onPause() {
        LogUtil.e(TAG, "------------------------------->onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.e(TAG, "onStop-------------------------------->onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, " onDestroy~~~");
        scanLeDevice(false);
        mBLE.disconnect();
        mBLE.close();
        mBluetoothAdapter.disable();

    }

    static public void WriteCharX(BluetoothGattCharacteristic GattCharacteristic , byte[] writeValue){
        LogUtil.i(TAG, "writeCharX = " + GattCharacteristic);
        if (GattCharacteristic != null){
            GattCharacteristic.setValue(writeValue);
            mBLE.writeCharacteristic(GattCharacteristic);
        }
    }

    static public void ReadCharX(BluetoothGattCharacteristic GattCharacteristic){
        LogUtil.i(TAG, "GattCharacteristic = " + GattCharacteristic);
        if(GattCharacteristic != null){
            mBLE.readCharacteristic(GattCharacteristic);
        }
    }

    static public void setCharacteristicNotification(BluetoothGattCharacteristic gattCharacteristic,boolean enabled){
        LogUtil.i(TAG, "gattCharacteristic = " + gattCharacteristic);
        if (gattCharacteristic != null) {
            mBLE.setCharacteristicNotification(gattCharacteristic, enabled);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final iBeacon device = mLeDeviceListAdapter.getDevice(position);
        //设备为null
        if (device == null) return;
        //若在搜索中!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
        if(mScanning){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }

        LogUtil.i(TAG, "mBluetoothAdapter.enable");
        bluetoothAddress = device.bluetoothAddress;
        boolean bRet = mBLE.connect(device.bluetoothAddress);

        LogUtil.i(TAG, "connect bRet = " + bRet);


        /*final Toast toast = Toast.makeText(getApplicationContext(),"正在连接设备并获取服务。。。",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        //设置显示时间为500ms
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        },500);*/
        ToastUtils.makeText(getApplicationContext(),"正在连接设备并获取服务。。。",500);

    }

    private void scanLeDevice(final boolean enable){
        if (enable){
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }else{
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }



    //搜索BLE终端服务事件
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {
        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }
    };

    //搜索到BLE终端数据交互的事件
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener(){

        //BLE终端数据被读事件
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // 执行 mBLE.readCharacteristic(gattCharacteristic); 后就会收到数据 if(status == BluetoothGatt.GATT_SUCCESS)
                LogUtil.e(TAG, "onCharRead " + gatt.getDevice().getName() + " read "
                        + characteristic.getUuid().toString() + " -> "
                                + Utils.bytesToHexString(characteristic.getValue()));

            RgbActivity.onCharacteristicRead(gatt, characteristic);

        }

        //收到BLE终端写入的数据回调

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            LogUtil.e(TAG, "onCharWrite " + gatt.getDevice().getName() + " write "
                    + characteristic.getUuid().toString() + " -> "
                    + new String(characteristic.getValue()));

            RgbActivity.onCharacteristicRead(gatt, characteristic);
        }
    };

    //Device scan callback
        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {


        final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mLeDeviceListAdapter.addDevice(ibeacon);
                mLeDeviceListAdapter.notifyDataSetChanged();

                if (mScanning == true) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }
        });

        // rssi
        LogUtil.i(TAG,"rssi = "+rssi);
        LogUtil.i(TAG,"mac = "+device.getAddress());
        LogUtil.i(TAG,"scanRecord.length = "+scanRecord.length);
    }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices){

        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices){
            //-----Service的字段信息----
            int type = gattService.getType();
            LogUtil.e(TAG, "-->service type:" + Utils.getServiceType(type));
            LogUtil.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            LogUtil.e(TAG, "-->service uuid:" + gattService.getUuid());

            //-----Characteristics的字段信息----
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){

                LogUtil.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                LogUtil.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0){
                    LogUtil.e(TAG, "---->char value:" + new String(data));
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)){
                    gattCharacteristic_keydata = gattCharacteristic;
                    //接受Characteristic被写的消息 收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic,true);
                    LogUtil.e(TAG, "+++++++++UUID_KEY_DATA");
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR1)) {
                    gattCharacteristic_char1 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR2)) {
                    gattCharacteristic_char2 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR3)) {
                    gattCharacteristic_char3 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR4)) {
                    gattCharacteristic_char4 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR5)) {
                    gattCharacteristic_char5 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)) {
                    gattCharacteristic_char6 = gattCharacteristic;
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);

                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR7)) {
                    gattCharacteristic_char7 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR8)) {
                    gattCharacteristic_char8 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR9)) {
                    gattCharacteristic_char9 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHARA)) {
                    gattCharacteristic_charA = gattCharacteristic;
                }

                //---Descriptors的字段信息
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors){
                    LogUtil.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0){
                        LogUtil.e(TAG, "-------->desc value:" + new String(desData));
                    }

                }
            }
        }

            //打开新窗体
        Intent intent = new Intent();
        intent.setClass(DeviceScanActivity.this, RgbActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }


}