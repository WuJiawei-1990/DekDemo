package com.example.dekdemo.ui.startMeasure;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.dekdemo.R;
import com.example.dekdemo.fruitsMeasure.appleMeasure;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link startFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class startFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RelativeLayout test;
    public static Fragment mAppleMeasureFragment = new appleMeasure();
    private RelativeLayout appleMeasure;
    private LinearLayout backToStartFragment;
    public startFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment startFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static startFragment newInstance(String param1, String param2) {
        startFragment fragment = new startFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appleMeasure = getActivity().findViewById(R.id.appleLayout);
        appleMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAppleMeasureFragment.isVisible()){
                    FragmentManager childFragmentManager = getChildFragmentManager();
                    childFragmentManager.beginTransaction().add(R.id.measure_fragment_container , mAppleMeasureFragment).commit();
                }
            }
        });
    }
}
