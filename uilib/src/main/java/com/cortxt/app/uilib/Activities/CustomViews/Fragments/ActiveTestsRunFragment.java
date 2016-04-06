package com.cortxt.app.uilib.Activities.CustomViews.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.cortxt.app.uilib.R;

/**
 * Created by bscheurman on 16-03-31.
 */

public class ActiveTestsRunFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    Spinner spinnerTest = null;
    ActiveTestsPager parent = null;
    private int mPage;

//    public static ActiveTestsRunFragment newInstance(int page) {
//        Bundle args = new Bundle();
//        args.putInt(ARG_PAGE, page);
//        ActiveTestsRunFragment fragment = new ActiveTestsRunFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    public void setPager (ActiveTestsPager pager)
    {
        parent = pager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.active_tests_run, container, false);
        //TextView textView = (TextView) view;
        //textView.setText("Fragment #" + mPage);
        Bundle b = getArguments();
        //if(b != null && b.containsKey("highlightItem") && !b.getBoolean("highlightItem")) {
        //    ScalingUtility.getInstance(getActivity()).scaleView(view);
        //}
        initViews(view);
        return view;
    }

    public void startClicked(View button) {

        this.getActivity().finish();
    }

    private void initViews(View v) {
        spinnerTest = (Spinner) v.findViewById(R.id.activeTestSpinner);
        parent.initTestOptions (spinnerTest, this.getActivity());
    }
}
