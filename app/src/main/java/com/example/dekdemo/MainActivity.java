package com.example.dekdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dekdemo.DateBase.DateBaseHelper;
import com.example.dekdemo.Spectra.BitConverter;
import com.example.dekdemo.Spectra.ModelMLR;
import com.example.dekdemo.bluetoothlib.ACSUtility;
import com.example.dekdemo.ui.history.historyFragment;
import com.example.dekdemo.ui.home.homeFragment;
import com.example.dekdemo.ui.startMeasure.startFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.dekdemo.ui.home.homeFragment.REQUEST_ENUM_PORTS;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private TextView mBleStatus;//蓝牙连接状态
    //创建三个fragment实例
    private Fragment mHomeFragment = new homeFragment();
    private Fragment mHistoryFrament = new historyFragment();
    private Fragment mStartMeasureFrament = new startFragment();
    //定义FragmentManager
    private FragmentManager fm = getSupportFragmentManager();
    private ACSUtility util;
    private boolean isPortOpen = false;
    private ACSUtility.blePort mSelectedPort;
    private ProgressDialog mProgressDialog;
    //public final static int REQUEST_ENUM_PORTS = 10;
    private BluetoothAdapter mBluetoothAdapter;
    private final int PixelNum = 256;
    private final int DataRcvNum = PixelNum * 4 + 3;
    private boolean StartFlag = false;
    private boolean FinishFlag = false;
    private byte[] DataReceived = new byte[DataRcvNum];
    private int[] SpectraData = new int[PixelNum];
    private int RcvIndex = 0;
    private double PreTSS = 0;
    private String TssToShow = null;
    private final static String TAG = "ACSMainActivity";
    private TextView port_name;
    private Handler resultHandler,resultRecordHandler;
    //数据库
    private DateBaseHelper dateBaseHelper;
    private SQLiteDatabase db_write,db_read;
    private final static int REQUEST_ENABLE_BT = 11;
    public final static int REQUEST_LOCATION_PERMISSION = 100;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    Log.d("home","click");
                    if (!mHomeFragment.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHistoryFrament);
                        transaction.hide(mStartMeasureFrament);
                        transaction.show(mHomeFragment);
                        transaction.commit();
                    }
                    break;
                case R.id.startMeasure:
                    Log.d("startMeasure","click");
                    if (!mStartMeasureFrament.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHistoryFrament);
                        transaction.hide(mHomeFragment);
                        transaction.show(mStartMeasureFrament);
                        transaction.commit();
                    }
                    break;
                case R.id.history:
                    Log.d("history","click");
                    if (!mHistoryFrament.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHomeFragment);
                        transaction.hide(mStartMeasureFrament);
                        transaction.show(mHistoryFrament);
                        transaction.commit();
                    }
                    break;
                default:
            }
            return true;
        }
    };

    //handler处理homeFragment的跳转消息
    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Log.d("MainActivity","接收到handler");
                    navigationView.setSelectedItemId(R.id.startMeasure);
            }
        }
    };

    private ACSUtility.IACSUtilityCallback userCallback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void didFoundPort(ACSUtility.blePort newPort) {
            // TODO Auto-generated method stub

        }

        @Override
        public void didFinishedEnumPorts() {
            // TODO Auto-generated method stub
        }

        @Override
        public void didOpenPort(ACSUtility.blePort port, Boolean bSuccess) {
            // TODO Auto-generated method stub
            Log.d(TAG, "The port is open ? " + bSuccess);
            if (bSuccess) {
                isPortOpen = true;
                runOnUiThread(() -> {
                    // TODO Auto-generated method stub
                    //updateUiObject(); wjw
                    showSuccessDialog();
                    mBleStatus = findViewById(R.id.ble_status);
                    mBleStatus.setText("已连接");
                    mBleStatus.setTextColor(getResources().getColor(R.color.colorAccent));
                    getProgressDialog().cancel();
                });
            } else {
                getProgressDialog().cancel();
                showFailDialog();
            }
        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {
            // TODO Auto-generated method stub
            isPortOpen = false;
            if (getProgressDialog().isShowing()) {
                getProgressDialog().dismiss();
            }
            Toast.makeText(MainActivity.this, "Disconnected from Peripheral", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> {
                // TODO Auto-generated method stub
                //updateUiObject(); wjw
            });
        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {
            //后续需添加线程定时
            if (!StartFlag) {
                if (packageToSend[0] == '{') {
                    StartFlag = true;
                    for (int i = 0; i < packageToSend.length; i++) {
                        DataReceived[RcvIndex] = packageToSend[i];
                        RcvIndex++;
                    }
                }
            } else {
                for (int i = 0; i < packageToSend.length; i++) {
                    DataReceived[RcvIndex] = packageToSend[i];
                    RcvIndex++;
                    if (RcvIndex == DataRcvNum) {
                        FinishFlag = true;
                        StartFlag = false;
                    }
                }
            }

            if (FinishFlag) {
                if (DataReceived[RcvIndex - 1] == '}') {
                    byte XORCheck = 0;
                    for (int i = 1; i < RcvIndex - 2; i++) {
                        XORCheck ^= DataReceived[i];
                    }
                    if (XORCheck == DataReceived[RcvIndex - 2]) {
                        for (int i = 0; i < PixelNum; i++) {
                            SpectraData[i] = BitConverter.ToInt32(DataReceived, i * 4 + 1);

                            //为了模型稳定性
                            SpectraData[i] = SpectraData[i]>>7;
                            SpectraData[i] = SpectraData[i]<<7;
                        }
                        //TODO 添加模型
                        PreTSS = ModelMLR.PredictTSS(SpectraData);
                        if (PreTSS < 8)
                            TssToShow = "Low";
                        else if (PreTSS >= 8 && PreTSS < 10)
                            TssToShow = (PreTSS + "").substring(0, 3);
                        else if (PreTSS >= 10 && PreTSS < 20)
                            TssToShow = (PreTSS + "").substring(0, 4);
                        else
                            TssToShow = "High";
                        //传递糖度值
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = TssToShow;
                        resultHandler.sendMessage(msg);
                        //runOnUiThread(() -> updateUiObject());wjw
                    }
                }
                FinishFlag = false;
                RcvIndex = 0;
            }
        }

        @Override
        public void heartbeatDebug() {
            // TODO Auto-generated method stub

        }

        @Override
        public void utilReadyForUse() {
            // TODO Auto-generated method stub

        }

        @Override
        public void didPackageSended(boolean succeed) {
            // TODO Auto-generated method stub
            if (succeed) {
                Toast.makeText(MainActivity.this, "数据发送成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "数据发送失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getSupportActionBar().hide();
        //初始化数据库
        initDateBase();
        navigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.fragmentContainer,mStartMeasureFrament);
        transaction.add(R.id.fragmentContainer,mHistoryFrament);
        transaction.add(R.id.fragmentContainer,mHomeFragment);
        transaction.hide(mHistoryFrament);
        transaction.hide(mStartMeasureFrament);
        transaction.show(mHomeFragment);
        transaction.commit();

        //权限获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENUM_PORTS
                && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            mSelectedPort = util.new blePort(device);
            //util = new ACSUtility(this, userCallback);
            //util.setUserCallback(userCallback);
            //updateUiObject(); wjw
            //port_name.setText(mSelectedPort._device.getName());
            if (!isPortOpen){
                //选中蓝牙设备则在fragment中显示蓝牙名称
                if (mSelectedPort != null) {
                    port_name.setText(mSelectedPort._device.getName());
                    util.openPort(mSelectedPort);
                }
            }
            //返回进行连接蓝牙
            if (isPortOpen) {
                util.closePort(); //点击同样的蓝牙item则会关闭
                isPortOpen = false;
            } else if (isPortOpen == false && mSelectedPort != null) {
                getProgressDialog().show();
                util.openPort(mSelectedPort);
                isPortOpen = true;

//                connect = findViewById(R.id.connect);
//                connect.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getProgressDialog().show();
//                        util.openPort(mSelectedPort);
//                        isPortOpen = true;
//                    }
//                });
            } else {
                Log.i(TAG, "User didn't select a port...So the port won't be opened...");
                Toast.makeText(this, "Please select a port first!", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == REQUEST_ENABLE_BT) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(MainActivity.this, "Bluetooth Disable...Quit...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        port_name = findViewById(R.id.port_name);
        util = new ACSUtility(this, userCallback);//实例化 ACSUtility
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        util.closeACSUtility();
    }

    private void showSuccessDialog() {
//        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
//                .setTitle("蓝牙连接")
//                .setMessage("蓝牙已连接！")
//                .setPositiveButton("确定", (dialog, which) -> {
//                    // TODO Auto-generated method stub
//                    dialog.dismiss();
//                }).show();
//        //修改按钮颜色
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.white));

        MaterialDialog materialDialog = new MaterialDialog.Builder(MainActivity.this)
                .title("蓝牙")
                .titleColor(getResources().getColor(R.color.black))
                .content("蓝牙已连接！")
                .contentColorRes(R.color.word)
                .icon(getResources().getDrawable(R.drawable.bluetooth))
                .maxIconSize(60)
                .positiveText("确定")
                .positiveColor(getResources().getColor(R.color.colorAccent))
                .show();
    }
    private void showFailDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Open Port")
                .setMessage("open port failed")
                .setPositiveButton("comfirm", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }
    private ProgressDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("连接中...");
        return mProgressDialog;
    }

    public Handler getHandler(){
        return mHandler;
    }

    public void setHandler(Handler handler) {
        resultHandler = handler;
    }
    public void initDateBase(){
        dateBaseHelper = new DateBaseHelper(MainActivity.this,"result_history",null,1);
        db_write = dateBaseHelper.getWritableDatabase();
        db_read = dateBaseHelper.getReadableDatabase();
    }
}