package com.example.dekdemo.bluetoothlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ChenGang on 2018/9/14.
 */

public class ACSUtility extends Object{
    private Context context;

    private static ArrayList<blePort> ports = null;
    private static blePort currentPort = null;
    private int _lengthOfPackage = 10;
    private float _scanTime;
    private static Boolean bScanning;

    private byte[] receivedBuffer;

    private final static String tag = "ACSUtility";
    private final static int ACSUTILITY_SCAN_TIMEOUT_MSG = 0x01;

    private static BluetoothAdapter mBtAdapter;

    private static ACSUtilityService mService;

    private static IACSUtilityCallback userCallback;

    public ACSUtility(){
        Log.d(tag, "ACS Utility Constructor");
    }

    public ACSUtility(Context context, IACSUtilityCallback cb) {
        // TODO Auto-generated constructor stub
        //构造函数，初始化所有变量
        this.context = context;
        userCallback = cb;
        _lengthOfPackage = 10;
        bScanning = false;

        Log.d(tag, "acsUtility 1");
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Log.d(tag, "error,mBtAdapter == null");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(context, ACSUtilityService.class);
        context.startService(intent);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            //mService = ((ACSBinder)service).getServie();
            Log.d(tag, "ACSUtilityService is connected!");
            mService = ((ACSUtilityService.ACSBinder)service).getService();
            mService.initialize();
            mService.addEventHandler(eventHandler);
            //
            //ready to use
            userCallback.utilReadyForUse();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d(tag, "ACSUtilityService is disConnected!");
            mService = null;
        }

    };
    static boolean mIsPortOpen = false;

    private MyHandler eventHandler = new MyHandler(this);
    static class MyHandler extends Handler{
        private final WeakReference<ACSUtility> mACSUtility;
        private MyHandler(ACSUtility acsUtility){
            mACSUtility = new WeakReference<ACSUtility>(acsUtility);
        }
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //super.handleMessage(msg);
            if (mACSUtility.get() != null) {
                Log.e(tag, "EventHandler got a message.flag is " + msg.what);
                if (userCallback == null) {
                    Log.e(tag, "UserCallback is null! All event will not be handled!");
                    return;
                }
                switch (msg.what) {
                    case ACSUtilityService.EVENT_GATT_CONNECTED:

                        break;
                    case ACSUtilityService.EVENT_GATT_DISCONNECTED:
                        userCallback.didClosePort(currentPort);
                        mIsPortOpen = false;
                        break;
                    case ACSUtilityService.EVENT_GATT_SERVICES_DISCOVERED:

                        break;

                    case ACSUtilityService.EVENT_OPEN_PORT_SUCCEED:

                        userCallback.didOpenPort(currentPort, true);
                        mIsPortOpen = true;
                        break;
                    case ACSUtilityService.EVENT_OPEN_PORT_FAILED:

                        userCallback.didOpenPort(currentPort, false);
                        mIsPortOpen = false;
                        break;
                    case ACSUtilityService.EVENT_DATA_AVAILABLE:
                        Bundle data = msg.getData();
                        byte[] receivedData = data.getByteArray(ACSUtilityService.EXTRA_DATA);
                        userCallback.didPackageReceived(currentPort, receivedData);
                        break;

                    case ACSUtilityService.EVENT_HEART_BEAT_DEBUG:

                        userCallback.heartbeatDebug();
                        break;

                    case ACSUtilityService.EVENT_DATA_SEND_SUCEED:
                        // 数据发送成功
                        userCallback.didPackageSended(true);
                        break;
                    case ACSUtilityService.EVENT_DATA_SEND_FAILED:
                        // 数据发送失败
                        userCallback.didPackageSended(false);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // 串口枚举
    public void enumAllPorts(float time){
        //UUID toFoundUUIDs[] = {ACS_SERVICE_UUID};
        ports = null;//清空缓冲区
        _scanTime = time;
        if (bScanning) {
            Log.e(tag, "enum in progress,could not execute again");
            return;
        }
        Log.d(tag, "start scan now");
        mBtAdapter.stopLeScan(mLeScanCallback);
        mBtAdapter.startLeScan(mLeScanCallback);
        //UUID []serviceUuids = {ACSUtilityService.ACS_SERVICE_UUID};
        //mBtAdapter.startLeScan(serviceUuids, mLeScanCallback);
        bScanning = true;
        //定时启动
        Thread timerThread = new Thread(new myThread());
        timerThread.start();
    }

    public boolean isPortOpen(blePort port) {
        return (mIsPortOpen && port._device.equals(currentPort._device));
    }

    private  BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // TODO Auto-generated method stub
            Log.d(tag, "onScanResult() - deviceName = " + device.getName()
                    + ", rssi=" + rssi + ",lengthOfScanRecord is : "
                    + scanRecord.length + ",address : " + device.getAddress());
            if (checkAddressExist(device)) {
                // 同样设备的多个包
                // Log.d(tag, "found same ACS Module");
            } else {
                if (ports == null) {
                    ports = new ArrayList<blePort>();
                }

                // 添加新端口
                Log.d(tag, "==== new Port add here ====");
                blePort newPort = new blePort(device);
                ports.add(newPort);

                if (userCallback != null) {
                    userCallback.didFoundPort(newPort);
                }
            }
        }
    };

    public void stopEnum() {
        bScanning = false;
        mBtAdapter.stopLeScan(mLeScanCallback);
    }

    //串口打开
    public void openPort(blePort port){

        if (mService != null && port != null) {
            currentPort = port;
            mService.connect(port._device.getAddress());
        }
        else {
            Log.e(tag, "ACSUtilityService or port is null!");
        }

    }

    public void closePort(){
        mService.disconnect();
    }

    public boolean writePort(byte[] value){
        if (value != null && mIsPortOpen) {
            return mService.writePort(value);
        }
        Log.e(tag, "Write port failed...value is null...");

        return false;
    }

    public void closeACSUtility() {
        //BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mBtGatt);
        mService.close();
        //closePort();
        mService.removeEventHandler();
        context.unbindService(conn);
        Intent intent = new Intent();
        intent.setClass(context, ACSUtilityService.class);
        context.stopService(intent);
    }

    //utility
    private void openPortFailAction(){
        if (userCallback != null) {
            userCallback.didOpenPort(currentPort, false);
        }
    }
    private void openPortSuccessAction(){
        if (userCallback != null) {
            userCallback.didOpenPort(currentPort, true);
        }
    }

    //回调
    private static Boolean checkAddressExist(BluetoothDevice device){
        if (ports == null) {
            return false;
        }
        for (blePort port : ports) {
            if (port._device.getAddress().equals(device.getAddress())) {
                return true;
            }
        }

        return false;
    }
    private void checkPackageToSend(byte[] newData){
        if (receivedBuffer != null) {
            // 上次接收了的但是没有处理的数据不为null，与这次接收的一块处理
            Log.d(tag, "checkPachageToSend buffer length is " + receivedBuffer.length);
            int newLength = receivedBuffer.length + newData.length;
            byte[] tempBuffer = new byte[newLength];
            byteCopy(receivedBuffer, tempBuffer,0,0, receivedBuffer.length);
            byteCopy(newData, tempBuffer, 0,receivedBuffer.length, newData.length);
            receivedBuffer = null;
            receivedBuffer = tempBuffer;
        }else{
            Log.d(tag, "checkPachageToSend buffer is null !");
            receivedBuffer = new byte[newData.length];
            byteCopy(newData, receivedBuffer, 0,0, newData.length);
        }

        Log.d(tag, "buffer lenght now is " + receivedBuffer.length);
        if (receivedBuffer.length >= _lengthOfPackage) {
            //比设定的一个包的长度要长
            byte[] packageToSend = new byte[_lengthOfPackage];
            // 剩余内容
            byte[] tempBuffer = new byte[receivedBuffer.length - _lengthOfPackage];
            byteCopy(receivedBuffer, packageToSend, 0,0, _lengthOfPackage);
            byteCopy(receivedBuffer, tempBuffer, _lengthOfPackage,0, tempBuffer.length);
            receivedBuffer = null;
            receivedBuffer = tempBuffer;
            userCallback.didPackageReceived(currentPort, packageToSend);
            Log.d(tag, "left length is " + receivedBuffer.length);
        }


    }
    private void byteCopy(byte[] from,byte[] to,int fromIndex,int toIndex,int length){
        int realLength = (from.length<length)?from.length:length;
        for (int i = 0; i < realLength; i++) {
            to[i+toIndex] = from[i+fromIndex];
        }
    }

    //线程定时类
    private TimeOutHandler timeHandler =new TimeOutHandler(this);
    class TimeOutHandler extends Handler{
        private final WeakReference<ACSUtility> mACSUtility;
        private TimeOutHandler(ACSUtility acsUtility){
            mACSUtility = new WeakReference<ACSUtility>(acsUtility);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mACSUtility.get() != null) {
                switch (msg.what) {
                    case ACSUTILITY_SCAN_TIMEOUT_MSG:
                        Log.d(tag, "scan time out");
                        bScanning = false;
                        mBtAdapter.stopLeScan(mLeScanCallback);
                        if (userCallback != null)
                            userCallback.didFinishedEnumPorts();
                        break;

                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        }
    };
    private class myThread implements Runnable{
        @Override
        public void run(){
            Log.d(tag,"runnableID"+Thread.currentThread().getId());
            try {
                Thread.sleep((long)_scanTime*1000);//最长扫描时间
                if (bScanning) {
                    Message msg = new Message();
                    msg.what = ACSUTILITY_SCAN_TIMEOUT_MSG;
                    timeHandler.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    //接口类，用于外部实现与交互
    public interface IACSUtilityCallback{
        public void utilReadyForUse();
        public void didFoundPort(blePort newPort);//一定时间后发现端口
        public void didFinishedEnumPorts();
        public void didOpenPort(blePort port, Boolean bSuccess);
        public void didClosePort(blePort port);
        public void didPackageSended(boolean succeed);
        public void didPackageReceived(blePort port, byte[] packageToSend);
        public void heartbeatDebug();
    }

    //Port类
    public  class blePort implements Serializable {
        public BluetoothDevice _device;

        public blePort(BluetoothDevice device) {
            _device = device;
        }
    }
}
