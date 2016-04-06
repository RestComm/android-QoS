package com.cortxt.app.uilib.Activities.CustomViews.Fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.cortxt.app.uilib.R;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.Utils.PreferenceKeys;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bscheurman on 16-03-31.
 */

public class ActiveTestsPager extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Run Test", "Script Tests" };
    private Context context;

    List<Fragment> slidingViews = null;
    int[] ids = { R.layout.active_tests_run, R.layout.active_tests_script };
    private int count = 0;
    boolean isFullWidth = false;


    public ActiveTestsPager(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;

        slidingViews = new ArrayList<Fragment>();
        count = ids.length;

        ActiveTestsRunFragment f1 = new ActiveTestsRunFragment();
        Bundle b = new Bundle();
        //b.putInt("layoutId", R.layout.active_tests_run);
        f1.setPager (this);
        f1.setArguments(b);
        slidingViews.add(0, f1);

        ActiveTestsScriptFragment f2 = new ActiveTestsScriptFragment();
        b = new Bundle();
        //b.putInt("layoutId", R.layout.active_tests_run);
        f2.setArguments(b);
        f2.setPager (this);
        slidingViews.add(1, f2);

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        //return PageFragment.newInstance(position + 1);
        return slidingViews.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    public String[] initTestOptions (Spinner spinnerTest, Context context) {

        String[] testtypeValues;
        List<String> testtypes = new ArrayList<String> ();
        List<String> info = new ArrayList<String> ();

        if (PreferenceKeys.isEventPermitted(context, EventType.MAN_SPEEDTEST, 1)){
            info.add(context.getString(R.string.recordingdlg_speedtest));
            testtypes.add ("speed");
        }

        String videoUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.VIDEO_URL, null);
        if (!(videoUrl == null || videoUrl.length() == 0 || !PreferenceKeys.isEventPermitted(context, EventType.VIDEO_TEST, 1)))
        {
            info.add(context.getString(R.string.recordingdlg_videotest));
            testtypes.add ("video");
        }

        String youtubeId = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.YOUTUBE_VIDEOID, null);
        if (!(youtubeId == null || youtubeId.length() == 0 || !PreferenceKeys.isEventPermitted(context, EventType.YOUTUBE_TEST, 1)))
        {
            info.add(context.getString(R.string.recordingdlg_youtubetest));
            testtypes.add ("youtube");
        }

        String audioUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.AUDIO_URL, null);
        if (!(audioUrl == null || audioUrl.length() == 0 || !PreferenceKeys.isEventPermitted(context, EventType.AUDIO_TEST, 1)))
        {
            info.add(context.getString(R.string.recordingdlg_audiotest));
            testtypes.add ("audio");
        }

        String webUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.WEB_URL, null);
        if (!(webUrl == null || webUrl.length() == 0 || !PreferenceKeys.isEventPermitted(context, EventType.WEBPAGE_TEST, 1)))
        {
            info.add(context.getString(R.string.recordingdlg_webtest));
            testtypes.add ("web");
        }

        String voiceDial = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.Miscellaneous.VOICETEST_SERVICE, null);
        PackageManager pkMan = context.getPackageManager();
        int voiceCallPermissionValue = pkMan.checkPermission("android.permission.CALL_PHONE", context.getPackageName());

        if (!(voiceDial == null || voiceDial.length() == 0 || voiceCallPermissionValue != 0))
        {
            info.add(context.getString(R.string.recordingdlg_vqtest));
            testtypes.add ("vq");
        }

        if (PreferenceKeys.getSMSPermissionsAllowed(context, true) == true ){
            info.add(context.getString(R.string.recordingdlg_smstest));
            testtypes.add ("smstest");
        }

        int allowConnTest = PreferenceManager.getDefaultSharedPreferences(context).getInt(PreferenceKeys.Miscellaneous.AUTO_CONNECTION_TESTS, 1);
        if (allowConnTest == 1)
        {
            info.add(context.getString(R.string.recordingdlg_connecttest));
            testtypes.add ("latency");
        }

        int allowPingTest = 0; // PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.PING_TESTS, 1);
        if (allowPingTest == 1)
        {
            info.add(context.getString(R.string.recordingdlg_pingtest));
            testtypes.add ("ping");
        }

        //info = removeDuplications(info);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_item, info);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTest.setAdapter(null);
        spinnerTest.setAdapter(adapter);
        spinnerTest.setSelection(0);

        testtypeValues = testtypes.toArray(new String[0]);
        //testtypeValues = (String[])testtypes.toArray();
        return testtypeValues;
    }
}