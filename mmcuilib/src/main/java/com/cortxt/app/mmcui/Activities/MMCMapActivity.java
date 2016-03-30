package com.cortxt.app.mmcui.Activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.cortxt.app.mmcui.R;
import com.google.android.maps.MapActivity;

public class MMCMapActivity extends MapActivity {

	protected boolean isPortrait = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean isPhone = getResources().getBoolean(R.bool.isphone);
		boolean landscapeAllowed = getResources().getBoolean(R.bool.ALLOW_LANDSCAPE_FOR_TABLET);

		/* force inheriting (map) activities to use portrait layout for phones and landscape layout for 
		 * 'large' and 'sw600dp' devices */
		if(isPhone || !landscapeAllowed) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			isPortrait = true;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			isPortrait = false;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
