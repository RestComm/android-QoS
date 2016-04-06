package com.cortxt.app.uilib.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.cortxt.app.uilib.Activities.CustomViews.Fragments.ActiveTestsPager;
import com.cortxt.app.uilib.R;
import com.cortxt.app.uilib.utils.ScalingUtility;

import 	android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Created by bscheurman on 16-03-31.
 */

public class ActiveTestsActivity extends MMCTrackedActivityOld { //AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.active_test_tabs, null, false);
        ScalingUtility.getInstance(this).scaleView(view);
        setContentView(view);

        MMCActivity.customizeTitleBar(this, view, R.string.testscript_title, R.string.testscript_title);


        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ActiveTestsPager pagerAdapter =
                new ActiveTestsPager(getSupportFragmentManager(), ActiveTestsActivity.this);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
//        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//            TabLayout.Tab tab = tabLayout.getTabAt(i);
//            tab.setCustomView(pagerAdapter.getTabView(i));
//        }
    }

    public void backActionClicked(View button) {
        this.finish();
    }

}
