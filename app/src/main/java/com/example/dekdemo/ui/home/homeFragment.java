package com.example.dekdemo.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.dekdemo.EnumPortActivity;
import com.example.dekdemo.MainActivity;
import com.example.dekdemo.R;
import com.example.dekdemo.bluetoothlib.ACSUtility;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {
    //蓝牙Layout
    RelativeLayout mBluetoothLayout;
    //开始测量
    RelativeLayout mStartMeasure;
    //查看光谱
    RelativeLayout mOb;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final int REQUEST_ENABLE_BT = 2;
    public static int REQUEST_ENUM_PORTS =10;
    Handler mHandler;
    public homeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MainActivity activity = (MainActivity)getActivity();
        mHandler = activity.getHandler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //蓝牙
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStartMeasure = (RelativeLayout)getActivity().findViewById(R.id.start_measure);
        mBluetoothLayout = (RelativeLayout) getActivity().findViewById(R.id.bluetoothLayout);
        mOb = (RelativeLayout)getActivity().findViewById(R.id.button_ob);
        mStartMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendData("s");
                //Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = mHandler.obtainMessage(1);
                        mHandler.sendMessage(msg);
                    }
                }).start();
            }
        });
        mBluetoothLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "我是蓝牙", Toast.LENGTH_SHORT).show();
                //跳转activity
                Intent intent = new Intent(getActivity(), EnumPortActivity.class);
                //startActivity(intent);
                getActivity().startActivityForResult(intent,REQUEST_ENUM_PORTS);
            }
        });
        mOb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}