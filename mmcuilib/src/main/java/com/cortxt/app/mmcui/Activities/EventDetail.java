package com.cortxt.app.mmcui.Activities;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.LocalStorageReporter.LocalStorageReporter;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Reporters.WebReporter.WebReporter;
import com.cortxt.app.mmcutility.Utils.Global;
import com.cortxt.app.mmcutility.Utils.MMCException;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * Activity that shows details of an event. It must be passed an integer event id in the Intent, with the key
 * {@link EventDetail#EXTRA_EVENT_ID}
 * 
 * @author nasrullah
 * 
 */
public class EventDetail extends MMCTrackedMapActivityOld {
	public static final String EXTRA_EVENT_ID = "eventId";

	private static final int DEFAULT_ZOOM_LEVEL = 16;
	private static final int UNKNOWN_LOC_ZOOM_LEVEL = 5;

	private TextView mName;
	private TextView mTimeAndDate, mdropcallText;
	private TextView mLocation = null, mDetails = null;
	private static ImageView eventIcon, mapIcon;
	private static MapView mMapView;
	private static HashMap<String, String> mEvent;
	private AsyncTask<Void, Void, HashMap<String, String>> mGetEventInfoTask;
	private AsyncTask<Void, Void, Bitmap> mGetCarrierLogoTask;
	private AsyncTask<Void, Void, Long> mConfirmEventTask;
	private Handler mHandler = new Handler();
	private static RelativeLayout scalingLayout = null;
	private static LinearLayout facebookTwitterLayout = null;
	private static RelativeLayout callratingLayout = null;
	private static RelativeLayout bottomLayout = null;
	private Button dropButton = null;
	public static final String TAG = WebReporter.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.new_event_details, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);

		facebookTwitterLayout = (LinearLayout) findViewById(R.id.facebookLayout);
		callratingLayout = (RelativeLayout) findViewById(R.id.callRatinglayout);
		bottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout);
		scalingLayout = (RelativeLayout) findViewById(R.id.scallingWrapepr);

        MMCActivity.customizeTitleBar(this, view, R.string.dashboard_eventdetail, R.string.dashcustom_eventdetail);
		mName = (TextView) findViewById(R.id.event_name);
		mLocation = (TextView) findViewById(R.id.eventLocation);
		mDetails = (TextView) findViewById(R.id.eventDetails);
		eventIcon = (ImageView) findViewById(R.id.event_icon);
		mapIcon = (ImageView) findViewById(R.id.map_icon);
		mTimeAndDate = (TextView) findViewById(R.id.event_time);
		mMapView = (MapView) findViewById(R.id.eventdetail_mapview);
		mdropcallText = (TextView) findViewById(R.id.dropcallText);
		dropButton = (Button)findViewById(R.id.droppedCallButton);

		if (getIntent().hasExtra(EXTRA_EVENT_ID)) {
			final int eventId = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);

			/**
			 * This task gets the event info from the ReportManager, and looks up the address of the event location
			 */
			mGetEventInfoTask = new AsyncTask<Void, Void, HashMap<String, String>>() {
				private static final String KEY_ADDRESS = "address";

				@Override
				protected HashMap<String, String> doInBackground(Void... params) {
					HashMap<String, String> event = ReportManager.getInstance(getApplicationContext()).getEventDetails(eventId);
					mEvent = event;
					if (event == null)
						return null;
					// get address
					getAddress(event);

					return event;
				}

				private void showEventInfo() {
					long timeStamp = Long.parseLong(mEvent.get(ReportManager.EventKeys.TIMESTAMP));
					DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
					DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
					Date dateValue = new Date(timeStamp);
					mTimeAndDate.setText(timeFormat.format(dateValue) + " / " + dateFormat.format(dateValue));

					String phone = "";
					if (android.os.Build.BRAND.length() > 0) {
						String brand = android.os.Build.BRAND.substring(0, 1).toUpperCase() + android.os.Build.BRAND.substring(1);
						phone = "\n" + getString(R.string.eventdetail_phone) + " " + brand + " " + android.os.Build.MODEL;
					}


				}

				private void loadMap() {
					try {
						if (mEvent.containsKey(ReportManager.EventKeys.LATITUDE) && mEvent.containsKey(ReportManager.EventKeys.LONGITUDE)) {
							int latitudeE6 = (int) (Double.parseDouble(mEvent.get(ReportManager.EventKeys.LATITUDE)) * 1000000.0);
							int longitudeE6 = (int) (Double.parseDouble(mEvent.get(ReportManager.EventKeys.LONGITUDE)) * 1000000.0);
							mMapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
							mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
						} else {
							// coordinates not available -- so zoom out
							mMapView.getController().setZoom(UNKNOWN_LOC_ZOOM_LEVEL);
						}
						Integer itype = null;
						if (mEvent.get(ReportManager.EventKeys.TYPE) != null)
							itype = Integer.parseInt(mEvent.get(ReportManager.EventKeys.TYPE));
						if (itype == null)
							return;

						String details = "";

						final EventType eventType = EventType.get(itype);

						// Confirmation of dropped/failed calls, if necessary, displays options to confirm that
						// a call dropped or ended normally
						if (eventType == EventType.EVT_DROP || eventType == EventType.EVT_CALLFAIL || eventType == EventType.EVT_DISCONNECT || eventType == EventType.EVT_UNANSWERED) {
							int rating = Integer.parseInt(mEvent.get(ReportManager.EventKeys.RATING));
							// server can specify whether a confirmation can be invoked on a low rated
							// potentially-dropped call
							int allowConfirm = PreferenceManager.getDefaultSharedPreferences(EventDetail.this).getInt(PreferenceKeys.Miscellaneous.ALLOW_CONFIRMATION, 5);
							String noConfirm = (getResources().getString(R.string.NO_CONFIRMATION));
							if (noConfirm.equals("1"))
								allowConfirm = 100; // don't even allow confirmation buttons on drilled down
													// event
							// allowConfirm>=5 disables the confirmation because rating always <= 5
							// allowConfirm=1 hits the 'else' and invokes confirmation if rating >= 1 and <5
							// allowConfirm=3 hits the 'else' and invokes confirmation if rating >= 3 and <5
							if (rating >= allowConfirm && rating <= 4 && rating > 0) {
								facebookTwitterLayout.setVisibility(View.GONE);
								bottomLayout.setVisibility(View.VISIBLE);
								callratingLayout.setVisibility(View.VISIBLE);
								int customText = (getResources().getInteger(R.integer.CUSTOM_EVENTNAMES));
								if (eventType == EventType.EVT_CALLFAIL || eventType == EventType.EVT_UNANSWERED)
								{
									mdropcallText.setText((customText == 1)?R.string.failedcall_confirmation_customtext:R.string.failedcall_confirmation_text);
									dropButton.setText ((customText == 1)?R.string.Custom_Notification_call_failed:R.string.MMC_Notification_call_failed);
								}
								else
								{
									mdropcallText.setText((customText == 1)?R.string.dropcall_confirmation_customtext:R.string.dropcall_confirmation_text);
									dropButton.setText ((customText == 1)?R.string.Custom_Notification_call_dropped:R.string.MMC_Notification_call_dropped);
								}
							}
						}
						details = getEventDetails (EventDetail.this, mEvent);

						mDetails.setText (details);


						int markerResource = findDarkImage(eventType.getEventName());
						if (markerResource == -1) {
							markerResource = eventType.getImageResource();
						}
						int textResource = eventType.getEventString();

						if (textResource != 0 && markerResource != 0) {
							mName.setText(textResource);
							eventIcon.setImageResource(markerResource);
						}

						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								// Display icons for teh event, on the map and monochrome in panel
								showMapIcon(eventType.getMapImageResource(), true);
							}
						}, 100);
					} catch (Exception e) {
						MMCLogger.logToFile(MMCLogger.Level.DEBUG, "EventDetail", "onPostExecute", "", e);
					}
				}

				private void getAddress(final HashMap<String, String> event) {
					if (event.containsKey(ReportManager.EventKeys.LATITUDE) && event.containsKey(ReportManager.EventKeys.LONGITUDE)) {

						Runnable r = new Runnable() {

							@Override
							public void run() {
								String addressString = WebReporter.geocode(EventDetail.this, Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE)), Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE)));
								event.put(KEY_ADDRESS, addressString);
//
								String str = "";
								if (event.containsKey(KEY_ADDRESS)) {
									str = "@ " + event.get(KEY_ADDRESS);
								}
								final String addr = str;
//
								// we have an address now, so replace the latitude/longitude (initially set) with this address
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										mLocation.setText(addr);
									}
								});
//								try {
////									final String apiKey = PreferenceManager.getDefaultSharedPreferences(CompareNew.this).getString(PreferenceKeys.User.API_KEY, null);
////									if(apiKey == null)
////										return null;
//									double latitude = Double.parseDouble(event.get(EventKeys.LATITUDE));
//									double longitude = Double.parseDouble(event.get(EventKeys.LONGITUDE));
//									String apiKey = "16fc03de-9c41-4a17-ae4c-2987d2bb32dc";
//									String url = getString(R.string.MMC_STATIC_ASSET_URL) + "/api/osm/location?apiKey=" + apiKey + "&location=" + latitude + "&location=" + longitude;
//									String responseContents = WebReporter.getHttpURLResponse(url, false);
//									addressString = parseAddress(responseContents);
//									if (addressString == null)
//										return;
//
//								} catch(Exception e) {
//									return;
//								}
								
//								Geocoder geocoder = new Geocoder(EventDetail.this);
//								String addressString = "";
//
//								try {
//									List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(event.get(EventKeys.LATITUDE)), Double.parseDouble(event.get(EventKeys.LONGITUDE)), 1);
//
//									if (addresses != null && addresses.size() > 0) {
//										Address address = addresses.get(0);
//
//										for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//											addressString += address.getAddressLine(i) + " ";
//										}
//

//									}
//								} catch (IOException e) {
//									// cannot do anything if we cannot get address -- lats/longs are already set in the location text view
//								}
							}
						};
						new Thread(r).start();
						try{
						String address = String.format("%.4f, %.4f", Double.valueOf(event.get(ReportManager.EventKeys.LATITUDE)), Double.valueOf(event.get(ReportManager.EventKeys.LONGITUDE)));
						event.put(KEY_ADDRESS, address);
						}catch (Exception e){}
					} else {
						String unknown;
						unknown = getString(R.string.mycoverage_unknownlocation);
						event.put(KEY_ADDRESS, unknown);
					}
				}

				@Override
				protected void onPostExecute(final HashMap<String, String> event) {

					// set event details
					showEventInfo();

					// show map and event icon on map
					loadMap();

					if (event == null)
						return;
					long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
					DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
					DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
					Date dateValue = new Date(timeStamp);
					mTimeAndDate.setText(timeFormat.format(dateValue) + " / " + dateFormat.format(dateValue));

					String address = "";
					if (event.containsKey(KEY_ADDRESS)) {
						address = "@ " + event.get(KEY_ADDRESS);
					}
					mLocation.setText(address);
					String phone = "";
					if (android.os.Build.BRAND.length() > 0) {
						String brand = android.os.Build.BRAND.substring(0, 1).toUpperCase() + android.os.Build.BRAND.substring(1);
						phone = "\n" + getString(R.string.eventdetail_phone) + " " + brand + " " + android.os.Build.MODEL;
					}
				}

			};
			TaskHelper.execute(mGetEventInfoTask);

			/**
			 * This task gets the carrier logo
			 */
			// mGetCarrierLogoTask = new AsyncTask<Void, Void, Bitmap>() {
			// @Override
			// protected Bitmap doInBackground(Void... params) {
			// try {
			// HashMap<String, String> carrier = ((MMCApplication)
			// getApplicationContext()).getDevice().getCarrierProperties();
			// return ReportManager.getInstance(getApplicationContext()).getCarrierLogo(carrier);
			// } catch (MMCException e) {
			// return null;
			// }
			// }
			//
			// @Override
			// protected void onPostExecute(Bitmap result) {
			// if(result != null) {
			// //mCarrierLogo.setImageBitmap(result);
			// }
			// }
			// }.execute((Void[])null);

		} else {
			onBackPressed();
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mGetEventInfoTask != null) {
			mGetEventInfoTask.cancel(true);
		}

		if (mGetCarrierLogoTask != null) {
			mGetCarrierLogoTask.cancel(true);
		}

		if (mConfirmEventTask != null) {
			mConfirmEventTask.cancel(true);
		}
	}

	public static void hidefacebookTwitterLayout() {
		bottomLayout.setVisibility(View.GONE);
		facebookTwitterLayout.setVisibility(View.GONE);
		showMapIcon(0, false);
	}
//	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void backActionClicked(View button) {
		this.finish();
	}

	private void confirmDropEvent(boolean bConfirm) {
		final int evtID = Integer.parseInt(mEvent.get(ReportManager.EventKeys.ID));
		final EventType evtType = EventType.get(Integer.parseInt(mEvent.get(ReportManager.EventKeys.TYPE)));

		final EventType newEvtType;
		final int rating;
		final long ltime = Long.parseLong(mEvent.get(ReportManager.EventKeys.TIMESTAMP));
		if (bConfirm)
			rating = 6;
		else
			rating = 0;

		// may change the event type if user says wrong assumption was made
		if (evtType == EventType.EVT_DISCONNECT && bConfirm)
			newEvtType = EventType.EVT_DROP;
		else if (evtType == EventType.EVT_UNANSWERED && bConfirm)
			newEvtType = EventType.EVT_CALLFAIL;
		else if (evtType == EventType.EVT_CALLFAIL && !bConfirm)
			newEvtType = EventType.EVT_UNANSWERED;
		else if (evtType == EventType.EVT_DROP && !bConfirm)
			newEvtType = EventType.EVT_DISCONNECT;
		else
			newEvtType = evtType;
		final int iUserID = Global.getUserID(this);
		final ReportManager reportManager = ReportManager.getInstance(getApplicationContext());
		reportManager.updateEventField(evtID, LocalStorageReporter.Events.KEY_TIER, Integer.toString(rating));
		if (newEvtType != evtType) {
			reportManager.updateEventField(evtID, LocalStorageReporter.Events.KEY_TYPE, Integer.toString(newEvtType.getIntValue()));
		}
		Toast toast = Toast.makeText(EventDetail.this, R.string.thankyou_confirmation_text, Toast.LENGTH_SHORT);
		toast.show();
		callratingLayout.setVisibility(View.GONE);

		/**
		 * This task gets the a random fact/statistic from the server
		 */
		mConfirmEventTask = new AsyncTask<Void, Void, Long>() {
			@Override
			protected Long doInBackground(Void... params) {
				Long result = 0l;
				try {
					result = reportManager.confirmEvent(ltime, evtType, newEvtType, rating, iUserID);
				} catch (MMCException e) {
					MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "ConfirmEventTask", "exeption", e);
					return 0l;
				}
				return result;
			}

			@Override
			protected void onPostExecute(Long lresult) {

				int markerResource = findDarkImage(newEvtType.getEventName());
				if (markerResource == -1) {
					markerResource = newEvtType.getImageResource();
				}
				int textResource = newEvtType.getEventString();
				showMapIcon(newEvtType.getMapImageResource(), true);

				try {
					if (mName != null && eventIcon != null && textResource != 0 && markerResource != 0) {
						mName.setText(textResource);
						eventIcon.setImageResource(markerResource);
					}
				} catch (Exception e) {
					MMCLogger.logToFile(MMCLogger.Level.DEBUG, "EventDetail", "onPostExecute", "", e);
				}

			}
		};
		TaskHelper.execute(mConfirmEventTask);

	}

	public void confirmDropped(View button) {
		confirmDropEvent(true);
	}

	public void confirmNormal(View button) {
		confirmDropEvent(false);
	}

	private int findDarkImage(String eventName) {
		if (eventName.equalsIgnoreCase("DROPPED") || eventName.equalsIgnoreCase("TT_DROPPED")) {
			return R.drawable.ic_stat_dropped_call_icon;
		} else if (eventName.equalsIgnoreCase("FAILED") || eventName.equalsIgnoreCase("TT_FAILED")) {
			return R.drawable.ic_stat_dropped_call_icon;
		} else if (eventName.equalsIgnoreCase("3G NO") || eventName.equalsIgnoreCase("TT_DATA")) {
			return R.drawable.ic_stat_no_3g_icon;
		} else if (eventName.equalsIgnoreCase("DATA NO")) {
			return R.drawable.ic_stat_no_2g_icon;
		} else if (eventName.equalsIgnoreCase("UPDATE") || eventName.equalsIgnoreCase("TRACK") || eventName.equalsIgnoreCase("TRAVEL-CHECK")) {
			return R.drawable.ic_stat_update;
		} else if (eventName.equalsIgnoreCase("4G NO")) {
			return R.drawable.ic_stat_no_lte_icon;
		} else if (eventName.equalsIgnoreCase("VOD YES")) {
			return R.drawable.ic_stat_service;
		} else if (eventName.equalsIgnoreCase("3G YES")) {
			return R.drawable.ic_stat_eventicon_gain3g;
		} else if (eventName.equalsIgnoreCase("DATA YES")) {
			return R.drawable.ic_stat_eventicon_gain2g;
		} else if (eventName.equalsIgnoreCase("4G YES")) {
			return R.drawable.ic_stat_eventicon_gainlte;
		} else if (eventName.equalsIgnoreCase("DISCONNECT")) {
			return R.drawable.ic_stat_disconnect_call_icon;
		} else if (eventName.equalsIgnoreCase("CONNECT")) {
			return R.drawable.ic_stat_eventicon_connectcall;
		} else if (eventName.equalsIgnoreCase("SPEED")) {
			return R.drawable.ic_stat_speed_test;
		} else if (eventName.equalsIgnoreCase("VOD NO") || eventName.equalsIgnoreCase("TT_SERVICE")) {
			return R.drawable.ic_stat_no_service;
		}
		return -1;
	}

	// static because it can be called from the ShareTask to reposition the icon after share panel hides
	private static void showMapIcon(int imageResource, boolean animate) {
		if (!mEvent.containsKey(ReportManager.EventKeys.LATITUDE)) {
			mapIcon.setVisibility(View.GONE);
		}
		ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) mapIcon.getLayoutParams();
		int height = mMapView.getHeight();
		int h = mapIcon.getHeight();
		margins.topMargin = height / 2 - h;
		mapIcon.setLayoutParams(margins);
		if (imageResource != 0)
			mapIcon.setImageResource(imageResource);
		if (animate) {

			TranslateAnimation animation = new TranslateAnimation(0, 0, (height / -2), 0);
			animation.setDuration(350);
			mapIcon.startAnimation(animation);
		} else {
			scalingLayout.requestLayout();
		}

	}

	public static String getEventDetails (Context context, HashMap<String, String> event)
	{
		int eventType = Integer.parseInt(event.get(ReportManager.EventKeys.TYPE));
		String details = "";

		if (eventType == EventType.APP_MONITORING.getIntValue() || eventType == EventType.MAN_SPEEDTEST.getIntValue()) {

			if (event.get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED) != null) {
				details += context.getString(R.string.speedtest_download_speed) + ":";
				int download = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED));
				double downloadSpeed = ((double) (download / 10000)) / 100.0;
				details += " " + downloadSpeed + " Mb/s  ";
			}

			if (event.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED) != null) {
				details += context.getString(R.string.speedtest_upload_speed) + ":";
				int upload = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED));
				double uploadSpeed = ((double) (upload / 10000)) / 100.0;
				details += " " + uploadSpeed + " Mb/s \n";
			}

			if (event.get(ReportManager.SpeedTestKeys.LATENCY) != null) {
				details += context.getString(R.string.speedtest_latency) + ":";
				int latency = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.LATENCY));
				details += " " + latency + " ms  ";
				details += getTier(event);
			}
		}
		else if (eventType == EventType.VIDEO_TEST.getIntValue() || eventType == EventType.YOUTUBE_TEST.getIntValue() ||
				eventType == EventType.AUDIO_TEST.getIntValue() ||  eventType == EventType.WEBPAGE_TEST.getIntValue())
		{
			if (event.get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED) != null) {
				details += context.getString(R.string.speedtest_download_speed) + ":";
				int download = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED));
				double downloadSpeed = ((double) (download / 10000)) / 100.0;
				details += " " + downloadSpeed + " Mb/s  ";
			}

			if (event.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED) != null) {
				details += context.getString(R.string.activetest_stalls) + ":";
				int stalls = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED));
				details += " " + stalls + "\n";
			}

			if (event.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED) != null) {
				details += context.getString(R.string.activetest_playdelay) + ":";
				int latency = Integer.parseInt(event.get(ReportManager.SpeedTestKeys.LATENCY));
				double sec = ((double) (latency / 10)) / 100.0;
				details += " " + sec + " sec  ";
				details += getTier(event);
			}
		}
		return details;
	}

	public static String getTier(HashMap<String, String> event) {
		int tier = Integer.parseInt(event.get(ReportManager.EventKeys.RATING));
		// Return hard-coded strings because these are universal
		if (tier == 1 || tier == 2)
			return "2G";
		else if (tier == 3 || tier == 4)
			return "3G";
		else if (tier == 5)
			return "LTE";
		else if (tier == 10)
			return "WiFi";
		else if (tier == 11)
			return "WiMax";
		return "";
	}

	public void shareClicked(View button) {
		boolean hideFbTwt = getResources().getBoolean(R.bool.HIDE_FB_TWTR);
		if (hideFbTwt) {
			facebookTwitterLayout.setVisibility(View.GONE);
			bottomLayout.setVisibility(View.GONE);
		} else {
			facebookTwitterLayout.setVisibility(View.VISIBLE);
			bottomLayout.setVisibility(View.VISIBLE);
		}
		callratingLayout.setVisibility(View.GONE);

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				String message;
				String subject;
				if (mEvent == null)
					return;

				int customSocialText = (getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT));

				String twittertext = "", facebooktext = "";
				if (customSocialText == 1) {
					twittertext = getString(R.string.customTwittertext);
					facebooktext = getString(R.string.customFacebooktext);
				} else {
					twittertext = getString(R.string.mmcTwittertext);
					facebooktext = getString(R.string.mmcFacebooktext);
				}
				ImageView twitterLogo = (ImageView) findViewById(R.id.twitterLogo);
				ImageView facebookLogo = (ImageView) findViewById(R.id.facebookLogo);
				TextView twitterTextview = (TextView) findViewById(R.id.twittertext);
				TextView facebookTextview = (TextView) findViewById(R.id.facebooktext);
				if (twitterTextview != null && twittertext.length() > 1)
					twitterTextview.setText(twittertext);
				else if (twitterLogo != null)
					twitterLogo.setVisibility(View.GONE);
				if (facebookTextview != null && facebooktext.length() > 1)
					facebookTextview.setText(facebooktext);
				else if (facebookLogo != null)
					facebookLogo.setVisibility(View.GONE);

				// int eventType = Integer.parseInt(mEvent.get(EventKeys.TYPE));
				EventType eventType = EventType.get(Integer.parseInt(mEvent.get(ReportManager.EventKeys.TYPE)));
				int markerResource = eventType.getImageResource();
				
				if (eventType == EventType.EVT_DROP) {
					message = getString((customSocialText == 1)?R.string.sharecustom_eventdetail_droppedcall:R.string.sharemessage_eventdetail_droppedcall);
					subject = getString((customSocialText == 1)?R.string.sharecustomsubject_eventdetail_droppedcall:R.string.sharemessagesubject_eventdetail_droppedcall);
				} else if (eventType == EventType.EVT_CALLFAIL) {
					message = getString((customSocialText == 1)?R.string.sharecustom_eventdetail_failedcall:R.string.sharemessage_eventdetail_failedcall);
					subject = getString((customSocialText == 1)?R.string.sharecustomsubject_eventdetail_failedcall:R.string.sharemessagesubject_eventdetail_failedcall);
				} else if (eventType != null) {
					int evtstr = eventType.getEventString();

					message = getString((customSocialText == 1)?R.string.sharecustom_eventdetail_event:R.string.sharemessage_eventdetail_event);
					subject = getString((customSocialText == 1)?R.string.sharecustomsubject_eventdetail_event:R.string.sharemessagesubject_eventdetail_event);
					String strEvent = getString(evtstr);
					message = String.format(message, strEvent);
					subject = String.format(subject, strEvent);

				} else {
					message = "";
					subject = "";
				}
				showMapIcon(0, false);
				TaskHelper.execute(
						new ShareTask(EventDetail.this, message, subject, findViewById(R.id.screen_container)));
			}
		}, 500);
	}
}
