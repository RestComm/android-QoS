package com.cortxt.app.mmcui.Activities;

import android.os.Bundle;
import android.view.Menu;

//import com.google.android.apps.analytics.easytracking.EasyTracker;
//import com.google.android.maps.MapActivity;s

public class MMCTrackedMapActivityOld extends MMCMapActivity {

private boolean mShouldTrack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		try
		{
			EasyTracker.getTracker().setContext(this);
			boolean collectPreferences = PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean(PreferenceKeys.Miscellaneous.MISC_OPT_COLLECT_USAGE_DATA, true);
			boolean enable = collectPreferences && !MMCLogger.isDebuggable();
			setTrackingEnabled(enable);
		}
		catch (Exception e) {}
		*/
	}

	protected void onStart() {
		super.onStart();
		//if(mShouldTrack)
		//	EasyTracker.getTracker().trackActivityStart(this);
	}

	/**
	 * This method is deprecated in Android 3.0 (Honeycomb) and later, but
	 * GoogleAnalytics support goes back to Android 1.5 and therefore cannot use
	 * the Fragment API.
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		Object o = super.onRetainNonConfigurationInstance();
		//EasyTracker.getTracker().trackActivityRetainNonConfigurationInstance();
		return o;
	}

	@Override
	protected void onStop() {
		super.onStop();
		//if(mShouldTrack)
		//	EasyTracker.getTracker().trackActivityStop(this);
	}

	/*
	protected void setTrackingEnabled(boolean shouldTrack){
		mShouldTrack = shouldTrack;
	}
	
	protected void trackPageView(String page){
		if(mShouldTrack) {
			EasyTracker.getTracker().trackPageView(page);
		}
	}
	*/
	public void trackEvent(String category, String action, String label, int value){
		//if(mShouldTrack) {
		//	EasyTracker.getTracker().trackEvent(category, action, label, value);
		//}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu)
	{
		return super.onMenuOpened(featureId, menu);
	}

}
