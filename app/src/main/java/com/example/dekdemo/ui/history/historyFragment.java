package com.example.dekdemo.ui.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dekdemo.DateBase.DateBaseHelper;
import com.example.dekdemo.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link historyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class historyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "historyFragment";
    ArrayList<String> group_list = new ArrayList<>();
    ArrayList<String> child_item_list = new ArrayList<>();
    ArrayList<ArrayList<String>> child_list = new ArrayList<>();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button test;
    private DateBaseHelper dateBaseHelper;
    private SQLiteDatabase db_write,db_read;
    private TextView history_title,history_content;
    private ExpandableListView expandableListView;
    private MyExpendableListViewAdapter myExpendableListViewAdapter= new MyExpendableListViewAdapter();
    public historyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment historyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static historyFragment newInstance(String param1, String param2) {
        historyFragment fragment = new historyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expandableListView = (ExpandableListView)getActivity().findViewById(R.id.history_ListView);
        group_list.add("20210416 记录3");
        group_list.add("20210415 记录2");
        group_list.add("20210414 记录1");
        child_item_list.add("12.1");
        child_item_list.add("13.5");
        child_list.add(child_item_list);
        child_list.add(child_item_list);
        child_list.add(new ArrayList<String>());
        expandableListView.setAdapter(myExpendableListViewAdapter);
        myExpendableListViewAdapter.notifyDataSetChanged();
        test = (Button)getActivity().findViewById(R.id.add_test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"add",Toast.LENGTH_SHORT).show();
                child_item_list.add("add");
                myExpendableListViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            initDateBase();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }



    //数据库初始化
    public void initDateBase(){
        dateBaseHelper = new DateBaseHelper(getActivity(),"result_history",null,1);
        db_write = dateBaseHelper.getWritableDatabase();
        db_read = dateBaseHelper.getReadableDatabase();
    }
    //ExpendListView适配器
    public class MyExpendableListViewAdapter extends BaseExpandableListAdapter{
        private static final String TAG = "ExpendListView";
        private Context context;
        @Override
        //获取分组的个数
        public int getGroupCount() {
            return group_list.size();
        }

        //获取子分组的个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return child_list.get(groupPosition).size();
        }

        //获取指定的分组数据
        @Override
        public Object getGroup(int groupPosition) {
            return group_list.get(groupPosition);
        }

        //获取指定的子分组数据
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return child_list.get(groupPosition).get(childPosition);
        }

        //获取指定分组的唯一ID
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        //获取子分组的唯一ID
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //分组和子选项是否持有稳定的ID, 底层数据的改变会不会影响到它们
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         *
         * 获取显示指定组的视图对象
         *
         * @param groupPosition 组位置
         * @param isExpanded 该组是展开状态还是伸缩状态
         * @param convertView 重用已有的视图对象
         * @param parent 返回的视图对象始终依附于的视图组
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_parent_item,parent,false);
                history_title = (TextView)convertView.findViewById(R.id.history_title);
                convertView.setTag(history_title);
            }else{
                history_title = (TextView)convertView.getTag();
            }
            history_title.setText(group_list.get(groupPosition));
            Log.d(TAG, "getGroupView: start");
            return convertView;
        }

        /**
         *
         * 获取一个视图对象，显示指定组中的指定子元素数据。
         *
         * @param groupPosition 组位置
         * @param childPosition 子元素位置
         * @param isLastChild 子元素是否处于组中的最后一个
         * @param convertView 重用已有的视图(View)对象
         * @param parent 返回的视图(View)对象始终依附于的视图组
         * @return
         * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View,
         *      android.view.ViewGroup)
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_child_item,parent,false);

                history_content = (TextView)convertView.findViewById(R.id.history_content);
                convertView.setTag(history_content);
            }else {
                history_content = (TextView) convertView.getTag();
            }
            history_content.setText(child_list.get(groupPosition).get(childPosition));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
    //将指定元素添加到指定数组（父级）
    public String[] groupAdd(String ele , String[] group_arr){
        String[] temp = new String[group_arr.length+1];
        if ("".equals(group_arr[0])) {
            group_arr[0] = ele;
        }else{
            for (int i = 0; i < temp.length - 1;i++){
                temp[i] = group_arr[i];
            }
            temp[temp.length-1] = ele;
            group_arr = temp;
        }
        return group_arr;
    }
}