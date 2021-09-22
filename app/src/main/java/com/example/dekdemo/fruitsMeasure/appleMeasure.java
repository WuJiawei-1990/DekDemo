package com.example.dekdemo.fruitsMeasure;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dekdemo.DateBase.DateBaseHelper;
import com.example.dekdemo.MainActivity;
import com.example.dekdemo.R;
import com.example.dekdemo.bluetoothlib.ACSUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link appleMeasure#newInstance} factory method to
 * create an instance of this fragment.
 */
public class appleMeasure extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "appleMeasure";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout backToStartFragment;
    private ListView resultListView;
    //item
    private TextView measure_result;
    //糖度值
    private TextView brix_result;
    //指针
    private ImageView point;
    private RelativeLayout send_date;
    private ImageView delete_date;
    private MainActivity mainActivity;
    private ResultListViewAdapter resultListViewAdapter;
    private DateBaseHelper dateBaseHelper;
    private SQLiteDatabase db_write,db_read;
    public IAppleMeasure iAppleMeasure;
    public appleMeasure() {
        // Required empty public constructor
    }
    public appleMeasure(IAppleMeasure iAppleMeasure){
        this.iAppleMeasure = iAppleMeasure;
    }
    private ArrayList arrayList = getDate();
    private ArrayList getDate(){
        ArrayList arrayList = new ArrayList();
        arrayList.add("12");
        arrayList.add("12.1");
        arrayList.add("12.2");
        arrayList.add("12.3");
        arrayList.add("12.4");
        return arrayList;
    }
    private MaterialDialog materialDialog;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment appleMeasure.
     */
    // TODO: Rename and change types and number of parameters
    public static appleMeasure newInstance(String param1, String param2) {
        appleMeasure fragment = new appleMeasure();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //初始化数据库
        initDateBase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_apple_measure, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resultListView = (ListView)getActivity().findViewById(R.id.result_list_view);
        brix_result = (TextView)getActivity().findViewById(R.id.brix_result);
        //绑定BaseAdapter
        resultListViewAdapter = new ResultListViewAdapter(this.getActivity(),arrayList);
        resultListView.setAdapter(resultListViewAdapter);
        point = (ImageView)getActivity().findViewById(R.id.point);
//        //给text添加下划线
//        back_text = (TextView) getActivity().findViewById(R.id.back_to_measure_text);
//        back_text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
//        back_text.getPaint().setAntiAlias(true);//抗锯齿
//        back_text.setTextColor(this.getResources().getColor(R.color.blue2));

        send_date = (RelativeLayout)getActivity().findViewById(R.id.send_date);
        delete_date = (ImageView)getActivity().findViewById(R.id.delete_group);

        send_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("appleMeasure","开启光谱计");
                //发送光谱计开启信号
                sendData("s");
                progressDialog().show();
//                TimerTask timerTask = new TimerTask() {
//                    @Override
//                    public void run() {
//                        if (materialDialog.isShowing()) {
//                            materialDialog.dismiss();
//                        }
//                    }
//                };
//                timer.schedule(timerTask,5000);
            }
        });
        delete_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("appleMeasure", "删除数据");
                arrayList.removeAll(arrayList);
                resultListView.setAdapter(resultListViewAdapter);
                resultListViewAdapter.notifyDataSetChanged();
            }
        });
        //返回
        backToStartFragment =(LinearLayout) getActivity().findViewById(R.id.back_to_measure);
        backToStartFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("appleMeasure","返回触发");
                iAppleMeasure.removeFragment();
            }
        });
    }
    //接收到糖度值显示
    public Handler resultHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 2 :
                    progressDialog().cancel();
                    if (msg.obj != null){
                        brix_result.setText((String) msg.obj);
                        arrayList.add(0,(String)msg.obj);
                        resultListView.setAdapter(resultListViewAdapter);
                        resultListViewAdapter.notifyDataSetChanged();
                    }
                    if (msg.obj != null && msg.obj.toString() != "High" && msg.obj.toString() != "Low"){
                        rotateAnimation(point,Float.valueOf(msg.obj.toString()));
                    }else if (msg.obj.toString() == "Low"){
                        rotateAnimation(point,0f);
                    }else if (msg.obj.toString() == "High"){
                        rotateAnimation(point,20f);
                    }
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setResultHandler(resultHandler);
    }

    public class ResultListViewAdapter extends BaseAdapter{
        private List date;
        private LayoutInflater layoutInflater;
        private Context context;
        public ResultListViewAdapter(Context context,List date){
            this.context = context;
            this.date = date;
            this.layoutInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return date.size();
        }

        @Override
        public Object getItem(int position) {
            return date.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null){
                convertView = layoutInflater.inflate(R.layout.fragment_apple_measure_list_item,null);
                //用convertView绑定组件，findViewByID无法完成绑定
                measure_result = (TextView)convertView.findViewById(R.id.measure_result);
                //设置标签防止错乱
                convertView.setTag(measure_result);
            }else {
                measure_result = (TextView)convertView.getTag();
            }
            measure_result.setText(arrayList.get(position).toString());
            return convertView;
        }
    }
    //开启光谱计
    private void sendData(String data) {
        byte[] dataBytes = data.getBytes();
        new ACSUtility().writePort(dataBytes);
    }
    //旋转
    /**
     （1）LinearInterpolator：动画从开始到结束，变化率是线性变化。
     （2）AccelerateInterpolator：动画从开始到结束，变化率是一个加速的过程。
     （3）DecelerateInterpolator：动画从开始到结束，变化率是一个减速的过程。
     （4）CycleInterpolator：动画从开始到结束，变化率是循环给定次数的正弦曲线。
     （5）AccelerateDecelerateInterpolator：动画从开始到结束，变化率是先加速后减速的过程
     **/
    public void rotateAnimation(ImageView imageView,float brixResult){
        float f = brixResult/20*180;
        float toDegrees = (float)(Math.round(f*10)/10);
        Animation anim =new RotateAnimation(0f, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true); // 设置保持动画最后的状态
        anim.setDuration(1000); // 设置动画时间
        anim.setInterpolator(new AccelerateDecelerateInterpolator()); // 设置插入器
        imageView.startAnimation(anim);
    }
    //等待dialog
    public MaterialDialog progressDialog(){
        if (materialDialog != null){
            return materialDialog ;
        }
        materialDialog = new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .content("测量中...")
                .contentColor(getResources().getColor(R.color.white))
                .backgroundColorRes(R.color.alpha_black_70)
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();
        return materialDialog;
    }
    //数据库
    public void initDateBase(){
        dateBaseHelper = new DateBaseHelper(getActivity(), "result_history", null, 1);
        db_write = dateBaseHelper.getWritableDatabase();
        db_read = dateBaseHelper.getReadableDatabase();
    }
    public void sendModelDate(int date){
        byte[] temp = new byte[4];
        //temp[0] = '{';
        temp[0] = (byte) (date>>24);
        temp[1] = (byte) (date>>16<<8);
        temp[2] = (byte) (date>>8<<16);
        temp[3] = (byte) (date<<24);
       // temp[5] = '}';
    }

    public interface IAppleMeasure{
        void removeFragment();
    }
}
