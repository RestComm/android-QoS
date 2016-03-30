package com.cortxt.app.mmcui.Activities.MyCoverage;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.cortxt.app.mmcui.Activities.CustomViews.DropDownMenuWindow;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;

/**
 * ActivityGroup that holds {@link MyCoverage}
 * Needed because there is a bug in Android MapActivity and MapView that allows only one MapActivity
 * per application. The chosen way around that limitation is to put the {@link MyCoverage} activity
 * in an {@link ActivityGroup} that destroys the MyCoverage activity inside it in onPause and restarts it
 * in onResume
 * @author nasrullah
 *
 */
public class MyCoverageActivityGroup extends ActivityGroup {
	private static final String KEY_MYCOVERAGE_BUNDLE = "MYCOVERAGE_BUNDLE";
	
	private Bundle myCoverageSavedState;
	private IntentDispatcher intentDispatcher;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(new View(getApplicationContext()));
		
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		//TextView title = (TextView) findViewById(R.id.textview_title);
//		SpannableString titleText = new SpannableString(getString(R.string.mycoverage_title));
//		int white = getResources().getColor(R.color.white);
//		titleText.setSpan(new ForegroundColorSpan(white), 0, titleText.length(), 0);
//		title.setText(titleText);
		
		Intent intent = this.getIntent();
		if (intent.hasExtra("mapState"))
			myCoverageSavedState = intent.getBundleExtra("mapState");
		
		else if(savedInstanceState != null) {
			this.myCoverageSavedState = savedInstanceState.getBundle(KEY_MYCOVERAGE_BUNDLE);
		}
		intentDispatcher = new IntentDispatcher();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		removeMapView();		
	}	

	@Override
	protected void onResume() {
		super.onResume();
		addMapView();
	}
	
	@Override
	public void onBackPressed() {		
		if(DropDownMenuWindow.isWindowAlreadyShowing && DropDownMenuWindow.getCoverageMenu() != null) {
			try{
				DropDownMenuWindow.getCoverageMenu().dismissWindow();
				DropDownMenuWindow.setCoverageMenu(null);
			} catch (Exception e)
			{}
		}
		else
			super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveMapState ();
		outState.putBundle(KEY_MYCOVERAGE_BUNDLE, myCoverageSavedState);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		//inflate the menu from the xml
//		new MenuInflater(getApplication()).inflate(R.menu.map, menu);
//		
//		return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onMenuOpened(int featureId, Menu menu)
//	{
//		MenuItem toggleTrack = menu.findItem(R.id.Map_Menu_Track);
//		if (!MMCService.isInTracking())
//			toggleTrack.setTitle(R.string.GenericText_Track);
//		else
//			toggleTrack.setTitle(R.string.GenericText_StopTrack);
//		return super.onMenuOpened(featureId, menu);
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		
//		switch (item.getItemId())
//		{
//		case R.id.Map_Menu_History:
//			Intent intent = new Intent(this, EventHistory.class);
//			startActivity(intent);
//			break;
//		
//		case R.id.Map_Menu_Track:
//			return NerdScreen.handleStartTracking ((Activity)this, item);
//			/*if (!MMCService.isInTracking())
//			{
//				//final String[] items = new String[]{"5 minutes", "15 minutes", "30 minutes", "1 hour", "2 hours", "6 hours", "continuous"};
//				final int[] itemValues = new int[]{1, 3, 6, 12, 24, 72, 0};
//				
//				new AlertDialog.Builder(this)
//				.setTitle(R.string.LiveStatus_SelectTrackingDuration)
//				.setItems(R.array.LiveStatus_TrackingValues, new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						int numFiveMinutePeriodsToTrack = itemValues[which];
//						intentDispatcher.startTracking(numFiveMinutePeriodsToTrack);
//					}
//				})
//				.show();
//				item.setTitle(R.string.GenericText_StopTrack);
//			}
//			else
//			{
//				intentDispatcher.stopTracking();
//				item.setTitle(R.string.GenericText_Track);
//			}
//			return true;*/
//		}
//		
//		return super.onOptionsItemSelected(item);
//	}
	
	@Override
	public void finish () {	 
		saveMapState ();
		Intent intent = new Intent ();
		intent.putExtra("mapState", myCoverageSavedState);
		setResult (RESULT_OK, intent);
		super.finish ();
	}

	private void addMapView() {
		Intent intent = new Intent(this, MyCoverage.class);

		if(myCoverageSavedState != null) {
			intent.putExtras(myCoverageSavedState);
		}

		Window window = getLocalActivityManager().startActivity("MyCoverage", intent);
		setContentView(window.getDecorView());
	}
	
	private void saveMapState () {
		MyCoverage myCoverage = (MyCoverage) getCurrentActivity();
		myCoverageSavedState = new Bundle();
		if (myCoverage != null)
			myCoverage.saveInstanceState(myCoverageSavedState);		
	}

	private void removeMapView() {		
		setContentView(new FrameLayout(this));
		getLocalActivityManager().removeAllActivities();
	}
	
	/**
	 * This class dispatches the various intents on the parent class' behalf so that
	 * IPC seems simple to its methods.
	 * @author abhin
	 *
	 */
	class IntentDispatcher{
		public IntentDispatcher(){
		}
		
		/**
		 * Broadcasts an intent to start a tracking event.
		 * @param numFiveMinutePeriodsToTrack The number of five minute periods the tracking event should last for.
		 */
		public void startTracking(int numFiveMinutePeriodsToTrack) {
			Intent intent = new Intent(CommonIntentActionsOld.START_TRACKING_ACTION);
			intent.putExtra(CommonIntentActionsOld.TRACKING_NUM_5_MIN_PERIODS, numFiveMinutePeriodsToTrack);
			sendBroadcast(intent);
		}
		
		public void stopTracking() {
			Intent intent = new Intent(CommonIntentActionsOld.STOP_TRACKING_ACTION);
			sendBroadcast(intent);
		}
	}
}
