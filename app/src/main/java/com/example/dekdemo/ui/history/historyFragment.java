package com.example.dekdemo.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.example.dekdemo.MainActivity;
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
    private TextView history_title,history_content;
    private MainActivity mainActivity;
    private ExpandableListView expandableListView;
    private MyExpendableListViewAdapter myExpendableListViewAdapter= new MyExpendableListViewAdapter();
    public Handler historyHander = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 3 :
                    if (msg.obj != null){
                        refreshView(expandableListView);
                    }
            }
        }
    };
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
        group_list.add("20210416 ??????3");
        group_list.add("20210415 ??????2");
        group_list.add("20210414 ??????1");
//        child_item_list.add("12.1");
//        child_item_list.add("13.5");
        child_list.add(child_item_list);
        child_list.add(new ArrayList<String>());
        child_list.add(new ArrayList<String>());
        expandableListView.setAdapter(myExpendableListViewAdapter);
//        refreshView(expandableListView);
//        myExpendableListViewAdapter.notifyDataSetChanged();
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setHistoryHandler(historyHander);
    }

    //ExpendListView?????????
    public class MyExpendableListViewAdapter extends BaseExpandableListAdapter{
        private static final String TAG = "ExpendListView";
        private Context context;
        @Override
        //?????????????????????
        public int getGroupCount() {
            return group_list.size();
        }

        //????????????????????????
        @Override
        public int getChildrenCount(int groupPosition) {
            return child_list.get(groupPosition).size();
        }

        //???????????????????????????
        @Override
        public Object getGroup(int groupPosition) {
            return group_list.get(groupPosition);
        }

        //??????????????????????????????
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return child_list.get(groupPosition).get(childPosition);
        }

        //???????????????????????????ID
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        //????????????????????????ID
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //???????????????????????????????????????ID, ?????????????????????????????????????????????
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         *
         * ????????????????????????????????????
         *
         * @param groupPosition ?????????
         * @param isExpanded ???????????????????????????????????????
         * @param convertView ???????????????????????????
         * @param parent ????????????????????????????????????????????????
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
            myExpendableListViewAdapter.notifyDataSetChanged();
            return convertView;
        }

        /**
         *
         * ????????????????????????????????????????????????????????????????????????
         *
         * @param groupPosition ?????????
         * @param childPosition ???????????????
         * @param isLastChild ??????????????????????????????????????????
         * @param convertView ?????????????????????(View)??????
         * @param parent ???????????????(View)?????????????????????????????????
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
    //????????????????????????
    public void refreshView(ExpandableListView expandableListView){
        DateBaseHelper dateBaseHelper = new DateBaseHelper(getActivity(),"result_history",null,1);
        SQLiteDatabase db_read = dateBaseHelper.getReadableDatabase();
        Cursor cursor = db_read.rawQuery("select * from result_history",null);
        child_item_list.clear();
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            if (name.equals("20210416 ??????3")){
                child_item_list.add(value);
                myExpendableListViewAdapter.notifyDataSetChanged();
            }
        }
        cursor.close();
    }
}