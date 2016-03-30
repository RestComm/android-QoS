package com.cortxt.app.mmcui.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcutility.Utils.MmcConstants;


public class MMCActivity extends FragmentActivity {

    protected boolean isPortrait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isPhone = getResources().getBoolean(R.bool.isphone);
        boolean landscapeAllowed = getResources().getBoolean(R.bool.ALLOW_LANDSCAPE_FOR_TABLET);

		/* force inheriting activities to use portrait layout for phones and landscape layout for 
		 * 'large' and 'sw600dp' devices */
        if(isPhone || !landscapeAllowed) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isPortrait = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isPortrait = false;
        }
    }

    public static void customizeHeadings(Activity screen, View view, int[] fields) {
        if (fields == null)
            return;
        String headingColor = (screen.getResources().getString(R.string.HEADING_COLOR));
        headingColor = headingColor.length() > 0 ? headingColor : "999999";
        int lcolor = -1;
        if (headingColor.length() > 1)
            lcolor = Integer.parseInt(headingColor, 16) + (0xff000000);
        else
            return;
        int i;
        for (i = 0; i < fields.length; i++) {
            TextView headingText = (TextView) view.findViewById(fields[i]);
            if (headingText != null)
                headingText.setTextColor(lcolor);
        }
    }

    public static void customizeSimpleLabelsColor(View view, int[] fields, String headingColor) {
        if (headingColor == null || headingColor.length() == 0)
            return;
        if (fields == null)
            return;
        headingColor = headingColor.length() > 0 ? headingColor : "222222";
        int lcolor = -1;
        if (headingColor.length() > 1)
            lcolor = Integer.parseInt(headingColor, 16) + (0xff000000);
        else
            return;
        int i;
        for (i = 0; i < fields.length; i++) {
            TextView headingText = (TextView) view.findViewById(fields[i]);
            if (headingText != null)
                headingText.setTextColor(lcolor);
        }
    }

    public static void customizeTitleBar(Context screen, View view, int mmc_title, int custom_title) {
        int customBackground = (screen.getResources().getInteger(R.integer.CUSTOM_BACKGROUND));

        int customTitleBackground = (screen.getResources().getInteger(R.integer.CUSTOM_TITLE_BACKIMAGE));

        int customTitleLogo = (screen.getResources().getInteger(R.integer.CUSTOM_TITLELOGO));
        int customTitles = (screen.getResources().getInteger(R.integer.CUSTOM_TITLES));
        int customStartButton = (screen.getResources().getInteger(R.integer.CUSTOM_STARTBUTTON));
        String darkTitle = (screen.getResources().getString(R.string.TITLE_ISDARK));
        String screenColor = (screen.getResources().getString(R.string.SCREEN_COLOR));
        String customTitleBackColor = (screen.getResources().getString(R.string.TITLE_BACKCOLOR));
        String customTitleColor = (screen.getResources().getString(R.string.TITLE_COLOR));
        String titleLineColor = (screen.getResources().getString(R.string.TITLE_LINECOLOR));
        String dashLabelColor = (screen.getResources().getString(R.string.DASH_LABELCOLOR));
        screenColor = screenColor.length() > 0 ? screenColor : "dddddd";
        String customDashBackgroundColor = (screen.getResources().getString(R.string.CUSTOM_DASH_BACKGROUND_COLOR));
        String customBackgroundColor = (screen.getResources().getString(R.string.CUSTOM_BACKGROUND_COLOR));
        customTitleBackColor = customTitleBackColor.length() > 0 ? customTitleBackColor : "";
        customTitleColor = customTitleColor.length() > 0 ? customTitleColor : "cccccc";
        titleLineColor = titleLineColor.length() > 0 ? titleLineColor : "3399cc";
        dashLabelColor = dashLabelColor.length() > 0 ? dashLabelColor : "666666";

        RelativeLayout titleLayout = (RelativeLayout) view.findViewById(R.id.topactionbarLayout);
        RelativeLayout titleLine = (RelativeLayout) view.findViewById(R.id.topactionbarLine);
        double screenDensityScale = screen.getResources().getDisplayMetrics().density;

        int tbcolor = -1, ttcolor = -1, scolor = -1;
        if (screenColor.length() > 1) {
            RelativeLayout screenContainer = (RelativeLayout) view.findViewById(R.id.screen_container);
            scolor = Integer.parseInt(screenColor, 16) + (0xff000000);
            if (screenContainer != null)
                screenContainer.setBackgroundColor(scolor);
        }
        if (titleLineColor.length() > 1 && titleLine != null) {
            int tlcolor = Integer.parseInt(titleLineColor, 16) + (0xff000000);
            titleLine.setBackgroundColor(tlcolor);
        }

        TextView headerText = (TextView) view.findViewById(R.id.actionbartitle);
        if (customTitleColor.length() > 1) {
            ttcolor = Integer.parseInt(customTitleColor, 16) + (0xff000000);
            headerText.setTextColor(ttcolor);
        }
        boolean isLandscape = screen.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (headerText != null) {
            if (customTitles == 1)
                headerText.setText(custom_title);
            else
                headerText.setText(mmc_title);
            if(isLandscape) {
                headerText.setTextSize(24);
            } else if (screenDensityScale <= 1) {
                headerText.setTextSize(16);
            } else {
                headerText.setTextSize(20);
            }
            FontsUtil.applyFontToTextView(MmcConstants.font_Regular, headerText, screen);
        }

        if (customTitleLogo == 1) {
            ImageView topHeaderLogo = (ImageView) view.findViewById(R.id.actionBarLogo);
            topHeaderLogo.setBackgroundResource(R.drawable.action_bar_custom_logo);
        }
        View startButton = (View) view.findViewById(R.id.startButton);
        View shareButton = (View) view.findViewById(R.id.shareButton);

        if (customStartButton == 1) {
            if (startButton != null)
                startButton.setBackgroundResource(R.drawable.start_button_custom_selector);
            if (shareButton != null)
                shareButton.setBackgroundResource(R.drawable.start_button_custom_selector);
        }


        ImageButton actionbarMenuIcon = (ImageButton) view.findViewById(R.id.actionbarMenuIcon);
        ImageButton actionBarBackButton = (ImageButton) view.findViewById(R.id.actionBarBackButton);

        if (darkTitle.length() == 0)
            darkTitle = "1";

        // use the white action bar buttons for a dark background
        if (darkTitle.equals("1")) {
            // ImageButton actionBarBackButton
            // =(ImageButton)view.findViewById(R.id.actionBarBackButton);
            if (actionBarBackButton != null)
                actionBarBackButton.setImageResource(R.drawable.ic_action_back_icon_dark);

            View actionbarShareIcon = view.findViewById(R.id.actionbarShareIcon);
            if (actionbarShareIcon != null)
                actionbarShareIcon.setBackgroundResource(R.drawable.ic_action_share_icon_dark);

            Button actionbarHistoryIcon = (Button) view.findViewById(R.id.actionbarHistoryIcon);
            if (actionbarHistoryIcon != null)
                actionbarHistoryIcon.setBackgroundResource(R.drawable.ic_action_history_icon_dark);

            // Button actionbarMenuIcon =
            // (Button)view.findViewById(R.id.actionbarMenuIcon);
            if (actionbarMenuIcon != null)
                actionbarMenuIcon.setImageResource(R.drawable.ic_action_menu_icon_dark);

            Button actionbarLocationIcon = (Button) view.findViewById(R.id.actionbarLocationIcon);
            if (actionbarLocationIcon != null)
                actionbarLocationIcon.setBackgroundResource(R.drawable.ic_action_location_icon_dark);

        } else {
            if (actionBarBackButton != null)
                actionBarBackButton.setImageResource(R.drawable.ic_action_back_icon);
            if (actionbarMenuIcon != null)
                actionbarMenuIcon.setImageResource(R.drawable.ic_action_menu_icon);
        }

        if (customTitleBackColor.length() > 1) {
            tbcolor = Integer.parseInt(customTitleBackColor, 16) + (0xff000000);
            titleLayout.setBackgroundColor(tbcolor);
            if (actionBarBackButton != null)
                actionBarBackButton.setBackgroundColor(tbcolor);
            if (actionbarMenuIcon != null)
                actionbarMenuIcon.setBackgroundColor(tbcolor);
        } else if (customTitleBackground == 1) {
            titleLayout.setBackgroundResource(R.drawable.action_bar_custom_bg);
            if (actionBarBackButton != null)
                actionBarBackButton.setBackgroundResource(R.drawable.action_bar_custom_bg);
            if (actionbarMenuIcon != null)
                actionbarMenuIcon.setBackgroundResource(R.drawable.action_bar_custom_bg);
        }

    }
}