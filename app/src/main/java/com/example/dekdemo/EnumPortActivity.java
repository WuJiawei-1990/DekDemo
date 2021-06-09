package com.example.dekdemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dekdemo.bluetoothlib.ACSUtility;
import com.example.dekdemo.bluetoothlib.ACSUtility.blePort;
import com.example.dekdemo.bluetoothlib.ACSUtilityService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import android.support.v7.app.AppCompatActivity;

public class EnumPortActivity extends AppCompatActivity {
    private ACSUtility utility;
    private ACSUtility.blePort mNewtPort;
    private boolean utilAvaliable;

    private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";
    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;

    public final static String SELECTED_PORT = "selected port";


    private ACSUtility.IACSUtilityCallback userCallback = new ACSUtility.IACSUtilityCallback() {

        @Override
        public void didFoundPort(blePort newPort) {
            // TODO Auto-generated method stub
            mNewtPort = newPort;
            runOnUiThread(()-> addDevice(mNewtPort._device));
        }

        @Override
        public void didFinishedEnumPorts() {
            // TODO Auto-generated method stub
            if (deviceList.size() == 0) {
                finish();
            }
        }

        @Override
        public void didOpenPort(blePort port, Boolean bSuccess) {
            // TODO Auto-generated method stub

        }

        @Override
        public void didClosePort(blePort port) {
            // TODO Auto-generated method stub

        }

        @Override
        public void didPackageReceived(blePort port, byte[] packageToSend) {
            // TODO Auto-generated method stub

        }

        @Override
        public void heartbeatDebug() {
            // TODO Auto-generated method stub

        }

        @Override
        public void utilReadyForUse() {
            // TODO Auto-generated method stub
            utilAvaliable = true;
            utility.enumAllPorts(10);

        }

        @Override
        public void didPackageSended(boolean succeed) {
            // TODO Auto-generated method stub

        }

    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        this.getSupportActionBar().hide();
        mEmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);

        utility = new ACSUtility(this, userCallback);//实例化ACSUtility

        utilAvaliable = false;

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                utility.closeACSUtility();
                finish();
            }
        });

        populateList();
    }


    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        /*
        Return the set of BluetoothDevice objects that are bonded (paired) to the local adapter.
		If Bluetooth state is not STATE_ON, this API will return an empty set.
		After turning on Bluetooth, wait for ACTION_STATE_CHANGED with STATE_ON to get the updated value.
         */
        /*Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            boolean result = false;

         result = mBtAdapter.isBLEDevice(pairedDevice);
            if (result == true) {
                addDevice(pairedDevice, 0);
            //}
        }*/
    }

    private void addDevice(BluetoothDevice device) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        //devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            mEmptyList.setVisibility(View.GONE);
            //剔除只有地址没有名字的蓝牙空设备
            if (device.getName() != null && !device.getName().equals("")){
                deviceList.add(device);
            }
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            blePort port = utility.new blePort(device);
            /*if (util.isPortOpened(port)){
                Log.i(TAG, "connected devcie");
                showMessage("device already connected");
                return;
            }*/
            //mService.scan(false);
            utility.stopEnum();
            Bundle b = new Bundle();
            b.putParcelable(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position));
            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            utility.closeACSUtility();
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
        if (utilAvaliable) {
            //utility.setUserCallback(null);
            utility.stopEnum();
            //utility.closeACSUtility();//在MainActivity中即可 wjw
        }
    }

	/*private OnItemClickListener mSelectDevice=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Bundle bundle=new Bundle();
			BluetoothDevice device = devices.get(position)._device;
			bundle.putParcelable(SELECTED_PORT, device);
			Intent result=new Intent();
			result.putExtras(bundle);
			setResult(RESULT_OK, result);
			finish();
		}

	};*/

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.VISIBLE);
                //tvrssi.setVisibility(View.GONE);
            } else {
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.GONE);
            }

            if (utility.isPortOpen(utility.new blePort(device))){
                Log.i(TAG, "connected device::"+device.getName());
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText("connected");
                // tvrssi.setVisibility(View.GONE);
            }
            return vg;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
