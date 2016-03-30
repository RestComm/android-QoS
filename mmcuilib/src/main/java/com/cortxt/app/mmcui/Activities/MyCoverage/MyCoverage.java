package com.cortxt.app.mmcui.Activities.MyCoverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.cortxt.app.MMC.Activities.Dashboard;
import com.cortxt.app.mmcui.Activities.EventHistory;
import com.cortxt.app.mmcui.Activities.MMCActivity;
import com.cortxt.app.mmcui.Activities.MMCTrackedActivityOld;
import com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay.EventOverlayItem;
import com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay.EventsOverlay;
import com.cortxt.app.mmcui.Activities.CustomViews.DropDownMenuWindow;
import com.cortxt.app.mmcui.Activities.CustomViews.Fragments.EventHistoryFragment;
import com.cortxt.app.mmcui.Activities.ShareTask;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.Carrier;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;

public class MyCoverage extends MMCTrackedActivityOld {

	public static final String KEY_ZOOM_LEVEL = "ZOOM_LEVEL";
	public static final String KEY_LATITUDE_E6 = "LATITUDE_E6";
	public static final String KEY_LONGITUDE_E6 = "LONGITUDE_E6";
	public static final String KEY_COVERAGE_OVERLAY = "COVERAGE_OVERLAY";
	public static final int DEFAULT_ZOOM_LEVEL = 14;

	private MMCMapView mMapView;
	private MyLocationOverlay  mMyLocationOverlay;
	private CoverageOverlay mCoverageOverlay;
	private EventsOverlay mEventsOverlay;
	private ImageButton menuButton=null;
	private Button bottomShareButton=null;
	private CheckBox showOverlaysOption;
	private DropDownMenuWindow coverageMenu=null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.mycoverage, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
        MMCActivity.customizeTitleBar(this, view, R.string.dashboard_maps, R.string.dashcustom_maps);
		menuButton = (ImageButton) view.findViewById(R.id.actionbarMenuIcon);
		TextView mapTitle = (TextView) view.findViewById(R.id.actionbartitle);
		bottomShareButton = (Button) findViewById(R.id.shareButton);
		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, mapTitle, this);
		FontsUtil.applyFontToButton(MmcConstants.font_Regular, bottomShareButton, this);
		Bundle savedInstanceState = getIntent().getExtras();
			
		CoverageMapFragment mapFrag = (CoverageMapFragment) getSupportFragmentManager().findFragmentById(R.id.mycoverage_map_fragment);
		mMapView = ((MyCoverageMapActivity) mapFrag.getHostedActivity()).mMapView;
		mCoverageOverlay = new CoverageOverlay(this, mMapView, (ProgressBar) findViewById(R.id.mycoverage_busyIndicator), "");
		mMapView.setChangeListener(mCoverageOverlay);
		mMapView.setZoomLevelChangeListener(mCoverageOverlay);
		mMapView.getOverlays().add(mCoverageOverlay);
		
		if(savedInstanceState == null && getIntent().getExtras() != null) {
			savedInstanceState = getIntent().getExtras();
		}

		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
//		mMyLocationOverlay.setDrawAccuracyEnabled(true);
		mMapView.getOverlays().add(mMyLocationOverlay);
		
		Integer showCalls = this.getResources().getInteger(R.integer.COMPARE_CALLS);
		if (showCalls == -1) // allow calls decided by server
		{
			showCalls = 1;
			int hideCalls = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.HIDE_CALLS, 1);
			if (hideCalls == 1)
				showCalls = 0;
		}

		HashSet<Integer> eventsToDisplay = new HashSet<Integer>();
		if(showCalls == 1 && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("KEY_SETTINGS_MAP_FILTERS_DROPPEDCALL", true)) {
			eventsToDisplay.add(EventType.EVT_DROP.getIntValue());
			eventsToDisplay.add(EventType.EVT_CALLFAIL.getIntValue());
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("KEY_SETTINGS_MAP_FILTERS_LOSTVOICECOVERAGE", true)) {
			eventsToDisplay.add(EventType.COV_VOD_NO.getIntValue());
			eventsToDisplay.add(EventType.COV_VOD_YES.getIntValue());
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("KEY_SETTINGS_MAP_FILTERS_LOST3G", true)) {
			eventsToDisplay.add(EventType.COV_DATA_NO.getIntValue());
			eventsToDisplay.add(EventType.COV_3G_NO.getIntValue());
			eventsToDisplay.add(EventType.COV_4G_NO.getIntValue());
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("KEY_SETTINGS_MAP_FILTERS_SPEED", true)) {
			eventsToDisplay.add(EventType.MAN_SPEEDTEST.getIntValue());
			eventsToDisplay.add(EventType.VIDEO_TEST.getIntValue());
			eventsToDisplay.add(EventType.AUDIO_TEST.getIntValue());
			eventsToDisplay.add(EventType.YOUTUBE_TEST.getIntValue());
			eventsToDisplay.add(EventType.WEBPAGE_TEST.getIntValue());
		}

//		OnItemGestureListener<EventOverlayItem> gestures = new OnItemGestureListener<EventOverlayItem>() {
//            @Override
//            public boolean onItemSingleTapUp(int index, EventOverlayItem item) {
//                //onTap(index);
//                return false;
//            }
//            @Override
//            public boolean onItemLongPress(int index, EventOverlayItem item) {
//                return false;
//            }
//        };
        ArrayList<EventOverlayItem> eventList = new ArrayList<EventOverlayItem>();
//        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		setEventsOverlay(new EventsOverlay(this, mMapView, eventsToDisplay, eventList));
		mMapView.setZoomLevelChangeListener(getEventsOverlay());
		// if (getEventsOverlay().size() > 0)
		// mMapView.getOverlays().add(getEventsOverlay());
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_COVERAGE_OVERLAY)) {
			int lastzoom = savedInstanceState.getInt(KEY_ZOOM_LEVEL);
			mCoverageOverlay.setLastCoverage(savedInstanceState);
		}

		setMapLocation(savedInstanceState);

		boolean bShowMapTip = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.Miscellaneous.SHOW_MAP_TIP, true);
		if (bShowMapTip == true) {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PreferenceKeys.Miscellaneous.SHOW_MAP_TIP, false).commit();
			String msg = this.getString(R.string.GenericText_MapTip);
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.show();
		}

		IntentFilter intentFilter = new IntentFilter(CommonIntentBundleKeysOld.ACTION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE);
		intentFilter.addAction(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE);
		registerReceiver(broadcastReceiver, intentFilter);
		
		centerOnLastKnownLocation();
		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("ShowCoverage", true).commit();
	}
	protected void saveInstanceState(Bundle outState) {

		if (mCoverageOverlay == null) // || mCoverageOverlay.getLastPng() ==
										// null)
			return;
		outState.putInt(KEY_ZOOM_LEVEL, mMapView.getZoomLevel());
		outState.putInt(KEY_LATITUDE_E6, mMapView.getMapCenter().getLatitudeE6());
		outState.putInt(KEY_LONGITUDE_E6, mMapView.getMapCenter().getLongitudeE6());

		if (mCoverageOverlay.getLastCoverage() != null) {
			outState.putParcelable(KEY_COVERAGE_OVERLAY, mCoverageOverlay.getLastCoverage());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMyLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onStop() {
		mCoverageOverlay.clear();
		mCoverageOverlay = null;
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMyLocationOverlay.enableMyLocation();
		// on resuming, update the chart using the DB to capture
		// data that was recorded when the activity was paused
		getEventsOverlay().buffer.updateActivityFromDB(3 * 3600 * 1000);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			MapMenuIconClicked(menuButton);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (coverageMenu != null && DropDownMenuWindow.isWindowAlreadyShowing) {
				coverageMenu.dismissWindow();
				return true;
			}
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Set the map location and zoom level from the savedInstanceState, or if
	 * none exists, the last known location
	 * 
	 * @param savedInstanceState
	 */
	private void setMapLocation(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ZOOM_LEVEL) && savedInstanceState.containsKey(KEY_LATITUDE_E6) && savedInstanceState.containsKey(KEY_LONGITUDE_E6)) {
			int zoomLevel = savedInstanceState.getInt(KEY_ZOOM_LEVEL);
			int latitudeE6 = savedInstanceState.getInt(KEY_LATITUDE_E6);
			int longitudeE6 = savedInstanceState.getInt(KEY_LONGITUDE_E6);

			mMapView.getController().setZoom(zoomLevel);
			mMapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
		} else {
			centerOnLastKnownLocation();
			mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
		}
	}

	/**
	 * Change map center to last known location
	 * 
	 * @return true if there was a last known location and the map was
	 *         successfully centered on it, false otherwise
	 */
	private boolean centerOnLastKnownLocation() {
		if (mMyLocationOverlay.getMyLocation() != null) {
			int latitudeE6 = mMyLocationOverlay.getMyLocation().getLatitudeE6();
			int longitudeE6 = mMyLocationOverlay.getMyLocation().getLongitudeE6();
			
			System.out.println(latitudeE6 + " " + longitudeE6);
			
			if(latitudeE6 == 0 && longitudeE6 == 0)
				return false;

			//mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
			mMapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
			return true;
		}
		else if(mMyLocationOverlay.getLastFix() != null) {
			int latitudeE6 = (int)(mMyLocationOverlay.getLastFix().getLatitude() * 1000000.0);
			int longitudeE6 = (int)(mMyLocationOverlay.getLastFix().getLongitude() * 1000000.0);
			System.out.println(latitudeE6 + " " + longitudeE6);
			if(latitudeE6 == 0 && longitudeE6 == 0)
				return false;
			
			//mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
			mMapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
			return true;
		} else {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(lastKnownLocation != null) {
				int latitudeE6 = (int)(lastKnownLocation.getLatitude() * 1000000.0);
				int longitudeE6 = (int)(lastKnownLocation.getLongitude() * 1000000.0);
				System.out.println(latitudeE6 + " " + longitudeE6);
				if(latitudeE6 == 0 && longitudeE6 == 0)
					return false;
				
				//mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
				mMapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
				return true;
			}
		}
		return false;
	}

	public void centerOnCurrentLocationClicked(View v) {
		boolean worked = centerOnLastKnownLocation();
		findViewById(R.id.shareButton).setVisibility(View.GONE);

		if (!worked) {
			Toast.makeText(this, R.string.mycoverage_unknownlocation, Toast.LENGTH_SHORT).show();
		}
	}

	public void topActionShareClicked(View v) {
		bottomShareButton.setVisibility(View.VISIBLE);
	}

	public void shareClicked(View v) {
		temporarilyDisableButton(bottomShareButton);
		final int customSocialText = getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT);
		final Integer shareTwitter = getResources().getInteger(R.integer.SHARE_TWITTER);
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				String msg = getString((customSocialText == 1)?R.string.sharecustom_mycoverage:R.string.sharemessage_mycoverage);
				String title = getString((customSocialText == 1)?R.string.sharecustomsubject_mycoverage:R.string.sharemessagesubject_mycoverage);
				Carrier carr = mCoverageOverlay.getCarrier();
				String twitterHandle = "my carrier";
				if (carr != null) {
					twitterHandle = carr.Twitter;
					if (twitterHandle == null || twitterHandle.length() < 3)
						twitterHandle = carr.Name;
					if (twitterHandle == null || twitterHandle.length() < 3)
						twitterHandle = "my carrier";
				}


				if (shareTwitter == 1) {
					if (msg != null)
						msg = msg.replaceAll("@CARRIERHANDLE", twitterHandle);
					if (title != null)
						title = title.replaceAll("@CARRIERHANDLE", twitterHandle);
				}
				TaskHelper.execute(
						new ShareTask(MyCoverage.this, msg, title, findViewById(R.id.mycoverage_container)));
			}
		},400);
	}

	public void overlaysClicked(View v) {

	}

	android.view.View.OnClickListener historyClicked = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showHistory(v);
			coverageMenu.dismissWindow();
		}
	};

	public void showHistory(View v) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(findViewById(R.id.fragment_container) == null) {
			// its portrait
			if(coverageMenu!=null){
				coverageMenu.dismissWindow();
			}
			Intent intent = new Intent(MyCoverage.this, EventHistory.class);
			startActivity(intent);
			return;
		}
		// its landscape
		Fragment f = getSupportFragmentManager().findFragmentByTag("eventHistory");
		if(f == null){
			f = new EventHistoryFragment();
			ft.add(R.id.fragment_container, f, "eventHistory").commit();
			v.setBackgroundColor(0xff3399cc);
		} else if(f.getView().getVisibility() == View.VISIBLE){
			// Toast.makeText(this, "Visible", Toast.LENGTH_SHORT).show();
			ft.hide(f).commit();
			v.setBackgroundColor(Color.TRANSPARENT);
		}else{
			// Toast.makeText(this, "Not Visible", Toast.LENGTH_SHORT).show();
			ft.show(f).commit();
			v.setBackgroundColor(0xff3399cc);
		}
	}

	android.view.View.OnClickListener overlaysClicked = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final String[] itemValues;
			String[] carrierNames = null;
			List<Carrier> operators = null;
			final Carrier currentCarrier;
			try {
				ReportManager reportManager = ReportManager.getInstance(getApplicationContext());
				operators = reportManager.getTopOperators(0, 0, 0, 0, 15, false);
				Collections.sort(operators);
				currentCarrier = reportManager.getCurrentCarrier();
			} catch (Exception e) {
				return;
			}
			if (operators != null) {
				itemValues = new String[operators.size()];
				carrierNames = new String[operators.size()];
				for (int i = 0; i < operators.size(); i++) {
					itemValues[i] = operators.get(i).OperatorId;
					carrierNames[i] = operators.get(i).Name;
                    for (int j = 0; j < i; j++) {
                        if (carrierNames[i].toLowerCase().equals(carrierNames[j].toLowerCase()))
                        {
                            if (operators.get(i).Tech.toLowerCase().equals("cdma"))
                                carrierNames[i] += " (cdma)";
                            if (operators.get(j).Tech.toLowerCase().equals("cdma"))
                                carrierNames[j] += " (cdma)";
                        }
                    }
				}
			} else {
				itemValues = new String[1];
				carrierNames = new String[1];
				if (currentCarrier != null) {
					itemValues[0] = currentCarrier.OperatorId;
					carrierNames[0] = currentCarrier.Name;
				} else {
					itemValues[0] = "";
					carrierNames[0] = "Unknown";
				}
			}

			new AlertDialog.Builder(MyCoverage.this).setTitle(R.string.mycoverage_selectCarrier).setItems(carrierNames, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (itemValues != null && mCoverageOverlay != null && which < itemValues.length) {
						mCoverageOverlay.setOperatorId(itemValues[which]);
					}
				}
			}).show();

			if (coverageMenu != null) {
				coverageMenu.dismissWindow();
			}
		}
	};
		
	android.view.View.OnClickListener showOverlays = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {			
			PreferenceManager.getDefaultSharedPreferences(MyCoverage.this).edit().putBoolean("ShowCoverage", showOverlaysOption.isChecked()).commit();			
			if(showOverlaysOption.isChecked() == false) {
//				mCoverageOverlay.setEnabled(false);	
				mMapView.invalidate();
			}
			if(showOverlaysOption.isChecked() == true) {
//				mCoverageOverlay.setEnabled(true);	//TODO: adapt to v1	
				mMapView.invalidate();
			}	
			if(coverageMenu!=null){
				coverageMenu.dismissWindow();
			}
		}	
	};
	
//	android.view.View.OnClickListener outdoorMapping = new android.view.View.OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			Intent intent = new Intent(MyCoverage.this, ManualMapping.class);
//			intent.putExtra("type", MmcConstants.OUTDOOR_SAMPLING);
//			startActivity(intent);
//			if(coverageMenu!=null){
//				coverageMenu.dismissWindow();
//			}
//		}
//	};

	public void MapBackActionClicked(View view) {
		this.finish();
	}
	
	public void MapMenuIconClicked(View view){
		if(DropDownMenuWindow.isWindowAlreadyShowing && coverageMenu!=null){
			coverageMenu.dismissWindow();
			return;
		}
	
		long currentTime=System.currentTimeMillis();
		if(currentTime-DropDownMenuWindow.lastWindowDismissedTime>200){
			bottomShareButton.setVisibility(View.GONE);

			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View menuOptionsView = inflater.inflate(R.layout.my_coverage_menu, null, false);
			ScalingUtility.getInstance(this).scaleView(menuOptionsView);

			TextView historyOption = (TextView) menuOptionsView.findViewById(R.id.HistoryOption);
			historyOption.setOnClickListener(historyClicked);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, historyOption, this);
			
			TextView overlaysOption=(TextView)menuOptionsView.findViewById(R.id.OverlaysOption);
			int carrierCovOnly = PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceKeys.Miscellaneous.CARRIER_COV_ONLY, 0);

			if (this.getResources().getInteger(R.integer.MAP_SWITCH_CARRIER) == 1 && carrierCovOnly != 1) {
				overlaysOption.setOnClickListener(overlaysClicked);
				FontsUtil.applyFontToTextView(MmcConstants.font_Regular, overlaysOption, this);
			}
			
			else
				overlaysOption.setVisibility(View.GONE);
					
			showOverlaysOption = (CheckBox)menuOptionsView.findViewById(R.id.OverlaysShowOption);
			View seperator3 = (View)menuOptionsView.findViewById(R.id.Separator3);
//			TextView outdoorMappingOption =(TextView)menuOptionsView.findViewById(R.id.OutdoorMappingOption);
//			View seperator4 = (View)menuOptionsView.findViewById(R.id.Separator4);
			String permission = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.User.USER_PERMISSION, "");
			if(permission.equals("manualmapping")) { //TODO: put this back
//			if(true) { //TODO: remove after testing
				//TODO: add turn on/off towers, dont even have towers showing yet
				//overlays show/hide
				boolean checked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ShowCoverage", true);
				showOverlaysOption.setChecked(checked);
				showOverlaysOption.setVisibility(View.VISIBLE);
				seperator3.setVisibility(View.VISIBLE);
				showOverlaysOption.setOnClickListener(showOverlays);	
			}
			
			coverageMenu=new DropDownMenuWindow(menuOptionsView,this,MmcConstants.MAP_MENU_OFFSET,MmcConstants.GENERAL_MENU_WINDOW_WIDTH);
			coverageMenu.showCalculatorMenu(view);	
			DropDownMenuWindow.setCoverageMenu(coverageMenu);
		}
	}

	//@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public EventsOverlay getEventsOverlay() {
		return mEventsOverlay;
	}

	public void setEventsOverlay(EventsOverlay mEventsOverlay) {
		this.mEventsOverlay = mEventsOverlay;
	}

	/**
	 * MMCService sends messages to this activity through intents. This class is
	 * responsible for receiving those intents and taking the appropriate action
	 * with them.
	 * 
	 * @author abhin
	 * 
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		Bundle extras;

		@Override
		public void onReceive(Context context, Intent intent) {
			extras = intent.getExtras();
			if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_LOCATION_UPDATE)) {
				manageLocationUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_SIGNAL_STRENGTH_UPDATE)) {
				manageSignalStrengthUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_EVENT_UPDATE)) {
				manageEventUpdate();
			} else if (intent.getAction().equals(CommonIntentBundleKeysOld.ACTION_CELL_UPDATE)) {
				manageCellUpdate();
			}
		}

		private void manageEventUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_EVENT)) {
					EventType event = (EventType) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_EVENT);
					getEventsOverlay().buffer.addEvent(event);
				}
			}
		}

		private void manageSignalStrengthUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_SIGNAL_STRENGTH_DBM)) {
					getEventsOverlay().buffer.addDataPoint(extras.getInt(CommonIntentBundleKeysOld.KEY_UPDATE_SIGNAL_STRENGTH_DBM));
				}
			}
		}

		private void manageCellUpdate() {
			if (extras != null) {
				if (extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_BS_LOW) && extras.containsKey(CommonIntentBundleKeysOld.KEY_UPDATE_BS_HIGH)) {
					Integer bsHigh = (Integer) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_HIGH);
					// Integer bsMid = (Integer)
					// extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_MID);
					Integer bsLow = (Integer) extras.getSerializable(CommonIntentBundleKeysOld.KEY_UPDATE_BS_LOW);
					getEventsOverlay().buffer.addCellID(bsHigh, bsLow);
				}
			}
		}

		private void manageLocationUpdate() {
			if (extras != null && extras.containsKey(CommonIntentBundleKeysOld.KEY_LOCATION_UPDATE)) {
				Location locUpdate = (Location) extras.getParcelable(CommonIntentBundleKeysOld.KEY_LOCATION_UPDATE);
				if (locUpdate != null)
					getEventsOverlay().buffer.addLocationPoint(locUpdate);
			}
		}
	};
					
	public void temporarilyDisableButton(final View button) {

		if (button == null)
			return;

		button.setEnabled(false);

		new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);  
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MyCoverage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setEnabled(true);
                    }
                });
            }
        }).start();
	}
}

