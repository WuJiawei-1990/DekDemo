package com.example.dekdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dekdemo.DateBase.DateBaseHelper;
import com.example.dekdemo.Spectra.BitConverter;
import com.example.dekdemo.Spectra.ModelMLR;
import com.example.dekdemo.bluetoothlib.ACSUtility;
import com.example.dekdemo.ui.history.historyFragment;
import com.example.dekdemo.ui.home.homeFragment;
import com.example.dekdemo.ui.startMeasure.startFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import static com.example.dekdemo.ui.home.homeFragment.REQUEST_ENUM_PORTS;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private TextView mBleStatus;//蓝牙连接状态
    private TextView port_name;
    private TextView privacyAgreement;
    //创建三个fragment实例
    private Fragment mHomeFragment = new homeFragment();
    private Fragment mHistoryFragment = new historyFragment();
    private Fragment mStartMeasureFragment = new startFragment();
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
    private final static String TAG = "MainActivity";
    private Handler resultHandler,historyHandler;
    //数据库
    public  DateBaseHelper dateBaseHelper;
    private SQLiteDatabase db_write,db_read;
    private final static int REQUEST_ENABLE_BT = 11;
    public final static int REQUEST_LOCATION_PERMISSION = 100;
    public DrawerLayout drawerLayout;
    public ImageView mLeftMenu;
    public String isAgreementAccepted = null;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    Log.d("home","click");
                    if (!mHomeFragment.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHistoryFragment);
                        transaction.hide(mStartMeasureFragment);
                        transaction.show(mHomeFragment);
                        transaction.commit();
                    }
                    break;
                case R.id.startMeasure:
                    Log.d("startMeasure","click");
                    if (!mStartMeasureFragment.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHistoryFragment);
                        transaction.hide(mHomeFragment);
                        transaction.show(mStartMeasureFragment);
                        transaction.commit();
                    }
                    break;
                case R.id.history:
                    Log.d("history","click");
                    if (!mHistoryFragment.isVisible()){
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.hide(mHomeFragment);
                        transaction.hide(mStartMeasureFragment);
                        transaction.show(mHistoryFragment);
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
                    Log.d("MainActivity","接收到handler 1");
                    navigationView.setSelectedItemId(R.id.startMeasure);
                    break;

                case 2:
                    Log.d("MainActivity", "接收到handler 2");
                    navigationView.setSelectedItemId(R.id.history);
                    break;
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
            Toast.makeText(MainActivity.this, "设备连接已断开", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> {
                // TODO Auto-generated method stub
                //updateUiObject(); wjw
            });
        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {
//            //后续需添加线程定时
//            if (!StartFlag) {
//                if (packageToSend[0] == '{') {
//                    StartFlag = true;
//                    for (int i = 0; i < packageToSend.length; i++) {
//                        DataReceived[RcvIndex] = packageToSend[i];
//                        RcvIndex++;
//                    }
//                }
//            } else {
//                for (int i = 0; i < packageToSend.length; i++) {
//                    DataReceived[RcvIndex] = packageToSend[i];
//                    RcvIndex++;
//                    if (RcvIndex == DataRcvNum) {
//                        FinishFlag = true;
//                        StartFlag = false;
//                    }
//                }
//            }
//
//            if (FinishFlag) {
//                if (DataReceived[RcvIndex - 1] == '}') {
//                    byte XORCheck = 0;
//                    for (int i = 1; i < RcvIndex - 2; i++) {
//                        XORCheck ^= DataReceived[i];
//                    }
//                    if (XORCheck == DataReceived[RcvIndex - 2]) {
//                        for (int i = 0; i < PixelNum; i++) {
//                            SpectraData[i] = BitConverter.ToInt32(DataReceived, i * 4 + 1);
//
//                            //为了模型稳定性
//                            SpectraData[i] = SpectraData[i]>>7;
//                            SpectraData[i] = SpectraData[i]<<7;
//                        }
//                        //TODO 添加模型
//                        PreTSS = ModelMLR.PredictTSS(SpectraData);
//                        if (PreTSS < 8)
//                            TssToShow = "Low";
//                        else if (PreTSS >= 8 && PreTSS < 10)
//                            TssToShow = (PreTSS + "").substring(0, 3);
//                        else if (PreTSS >= 10 && PreTSS < 20)
//                            TssToShow = (PreTSS + "").substring(0, 4);
//                        else
//                            TssToShow = "High";
//                        //传递糖度值
//                        Message msg = new Message();
//                        msg.what = 2;
//                        msg.obj = TssToShow;
//                        resultHandler.sendMessage(msg);
//                        //添加至数据库
//                        addSQL((String) msg.obj);
//                        //通知历史记录界面更新
//                        Message msgHistory = new Message();
//                        msgHistory.what = 3;
//                        msgHistory.obj = TssToShow;
//                        historyHandler.sendMessage(msgHistory);
//                        //runOnUiThread(() -> updateUiObject());wjw
//                    }
//                }
//                FinishFlag = false;
//                RcvIndex = 0;
//            }
            String s = new String(packageToSend);
            float f = Float.parseFloat(s);
            TssToShow = Float.toString(f);
            //传递糖度值
            Message msg = new Message();
            msg.what = 2;
            msg.obj = TssToShow;
            resultHandler.sendMessage(msg);
            //添加至数据库
            addSQL((String) msg.obj);
            //通知历史记录界面更新
            Message msgHistory = new Message();
            msgHistory.what = 3;
            msgHistory.obj = TssToShow;
            historyHandler.sendMessage(msgHistory);
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
        initView();
        util = new ACSUtility(this, userCallback);//实例化 ACSUtility
        //隐私协议
        if (!getAgreementStatus()){
            showAgreementDialog();
        }
        //权限获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == REQUEST_ENUM_PORTS && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            mSelectedPort = util.new blePort(device);
            if (!isPortOpen && mSelectedPort != null){
                //选中蓝牙设备则在fragment中显示蓝牙名称
                port_name.setText(mSelectedPort._device.getName());
                getProgressDialog().show();
                util.openPort(mSelectedPort);
                isPortOpen = true;
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
        //传递activity实现的callback：wjw
        util.getCallBack(userCallback);
        port_name = findViewById(R.id.port_name);
        //util = new ACSUtility(this, userCallback);//实例化 ACSUtility
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Log.d(TAG, "onResume: ");
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        util.closeACSUtility();
        Log.d(TAG, "onDestroy: ");
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

    public void setResultHandler(Handler handler) {
        resultHandler = handler;
    }
    public void setHistoryHandler(Handler handler) {
        historyHandler = handler;
    }
    public void initDateBase(){
        dateBaseHelper = new DateBaseHelper(MainActivity.this,"result_history",null,1);
        db_write = dateBaseHelper.getWritableDatabase();
        db_read = dateBaseHelper.getReadableDatabase();
    }
    public void initView(){
        navigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.fragmentContainer,mStartMeasureFragment);
        transaction.add(R.id.fragmentContainer,mHistoryFragment);
        transaction.add(R.id.fragmentContainer,mHomeFragment);
        transaction.hide(mHistoryFragment);
        transaction.hide(mStartMeasureFragment);
        transaction.show(mHomeFragment);
        transaction.commit();
        initLeftMenu();
    }
    public void initLeftMenu(){
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        privacyAgreement = (TextView)findViewById(R.id.privacyAgreement);
        privacyAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        privacyAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                showPrivacyAgreementDialog();
            }
        });
        mLeftMenu = (ImageView)findViewById(R.id.leftMenu);
        mLeftMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    private void addSQL(String result) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name","20210416 记录3");
        contentValues.put("value",result);
        db_write.insert("result_history",null , contentValues);
        Log.d(TAG, "add: 成功");
    }
    private void addAgreementStatus(String isAgreementAccepted){
        ContentValues contentValues = new ContentValues();
        contentValues.put("name","AgreementStatus");
        contentValues.put("value",isAgreementAccepted);
        db_write.insert("result_history",null , contentValues);
        Log.d(TAG, "addAgreementStatus: 1");
    }
    public boolean getAgreementStatus(){
        boolean status = false;
        Cursor cursor = db_read.rawQuery("select * from result_history",null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            if (name.equals("AgreementStatus")){
                if (value.equals("1")){
                    status = true;
                }
            }
        }
        cursor.close();
        return status;
    }
    private void showPrivacyAgreementDialog() {
        new MaterialDialog.Builder(this)
                .title("隐私协议")
                .content("本应用尊重并保护所有使用服务用户的个人隐私权。本应用不会将这些信息对外披露或向第三方提供。本应用会不时更新本隐私权政策。 您在同意本应用服务使用协议之时，即视为您已经同意本隐私权政策全部内容。本隐私权政策属于本应用服务使用协议不可分割的一部分" + "\n"
                        + "1. 适用范围" +"\n"
                        + "(a) 在您使用本应用进行蓝牙搜索和连接时，本应用自动接收并记录的您附近的蓝牙设备地址" + "\n"
                        + "(b) 在您使用本设备进行数据测量时，本应用会自动将数据记录在您的当前设备"
                        + "(c) 违反法律规定或违反本应用规则行为及本应用已对您采取的措施" + "\n"
                        + "2. 信息使用" +"\n"
                        + "(a) 本应用不会向任何无关第三方提供、出售、出租、分享或交易您的个人信息" + "\n"
                        + "(b) 本应用亦不允许任何第三方以任何手段收集、编辑、出售或者无偿传播您的个人信息" + "\n"
                        + "3. 信息存储和交换" +"\n"
                        + "本应用收集的有关您的信息和资料将保存在本应用上，这些信息和资料只供您自己存储和展示。" +"\n"
                        + "4. 本隐私政策的更改" +"\n"
                        + "根据应用功能修改保留随时修改本政策的权利，因此请经常查看，如有重大更改，将在更新app之后以通知形式传达" +"\n"
                        + "请您妥善保护自己的个人信息，仅在必要的情形下向他人提供。如您发现自己的个人信息泄密，请您立即联络邮箱jwwu@zd-smart.com，以便本应用采取相应措施。")
                .positiveText("确定")
                .positiveColor(getResources().getColor(R.color.colorAccent))
                .show();
    }
    //隐私协议弹窗
    public void showAgreementDialog(){
        new MaterialDialog.Builder(this)
                .title("隐私政策")
                .content("请你务必审慎阅读、充分理解“隐私政策”各条款，包括但不限于：为了向你提供检测服务，我们需要收集你的设备信息、启用蓝牙服务、保存数据等。你可以在设置中查看、变更并管理你的授权。" +
                        "你可阅读《隐私政策》了解详细信息。如你同意，请选中“同意”开始接受我们的服务。")
                .contentColor(getResources().getColor(R.color.word))
                .positiveText("确认")
                .positiveColor(getResources().getColor(R.color.colorAccent))
                .checkBoxPrompt("同意", false, new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked){
                            Toast.makeText(MainActivity.this,"请先同意协议",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!dialog.isPromptCheckBoxChecked()){
                            Toast.makeText(MainActivity.this,"请先同意协议",Toast.LENGTH_SHORT).show();
                        }else {
                            dialog.dismiss();
                            isAgreementAccepted = "1";
                            addAgreementStatus(isAgreementAccepted);
                        }
                    }
                })
                .show();
    }
    //检测用户返回
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new MaterialDialog.Builder(this)
                    .content("确定要退出吗？")
                    .contentColorRes(R.color.black)
                    .positiveText("确定")
                    .positiveColor(getResources().getColor(R.color.red))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //util.closeACSUtility();
                            finish();//结束当前Activity
                            finishActivity(REQUEST_ENABLE_BT);
                        }
                    })
                    .negativeText("取消")
                    .negativeColor(getResources().getColor(R.color.colorAccent))
                    .canceledOnTouchOutside(false)
                    .show();
        }
        return false;
    }
}