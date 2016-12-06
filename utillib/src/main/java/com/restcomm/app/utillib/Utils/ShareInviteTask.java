package com.restcomm.app.utillib.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.restcomm.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.R;
import com.restcomm.app.utillib.Reporters.LocalStorageReporter.LocalStorageReporter;
import com.restcomm.app.utillib.Reporters.ReportManager;
import com.restcomm.app.utillib.Reporters.WebReporter.WebReporter;
//import com.google.android.gms.appinvite.AppInviteInvitation;
//import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

 public class ShareInviteTask extends AsyncTask<Void, Void, Boolean> {

	private Context mContext;
	private String mTextToShare;
	private String mSubject, mSubject2;
	private String mEmailTo;
	private View mViewToScreenshot;
	private Bitmap mScreenshot;
	private static boolean useTwitter = false;
	public boolean emailOnly = false;
	private boolean isFromEventDetail=false;
	private boolean allowMynetwork=false, allowLinks=true;
	private int eventid = 0;
	private EventType eventType;
	private int ieventtype = 0;
	private int carrierID = 0;
	private String evtfloor = null;
	private String carriername = null, carrierhandle = null;
	private static final int REQUEST_INVITE = 0;

	private double fLat = 0;
	private double fLng = 0;
	private String eventAddress = null;
	private String strShortName = "";
	private String strEventdate = "";
	private Date dateEvent;
	private long realEventId = 0;
	private String filepath = null, filename = null;
	private String lastShortName = null;
	private boolean bInvite = false;//true;
	HashMap<String, String> event;
	private Uri mobileLink;


	AlertDialog alertDialog;

	public ShareInviteTask(Context context, String textToShare, String subject, View viewToScreenshot, EventType evttype) {
		eventType = evttype;
		init (context, textToShare, subject, viewToScreenshot);
	}

	 public ShareInviteTask(Context context, String textToShare, String subject, View viewToScreenshot, int evtid, Bitmap screenshot) {
		eventid = evtid;
		mScreenshot = screenshot;
		mViewToScreenshot = viewToScreenshot;
		init(context, textToShare, subject, viewToScreenshot);
	}

	 public ShareInviteTask(Context context, String textToShare, String subject, View viewToScreenshot, int evtid) {
		eventid = evtid;
		mViewToScreenshot = viewToScreenshot;
		init(context, textToShare, subject, viewToScreenshot);
	}
	 public ShareInviteTask(Context context, String textToShare, String subject, View viewToScreenshot) {
		init(context, textToShare, subject, viewToScreenshot);
	}
	private void init(Context context, String textToShare, String subject, View viewToScreenshot) {
		mContext = context;
		mTextToShare = textToShare;
		mSubject = subject;
		mViewToScreenshot = viewToScreenshot;
	}

	public void setEmailTo (String emailto)
	{
		mEmailTo = emailto;
	}
	
	@Override
	protected void onPreExecute() {

		{
			if (mViewToScreenshot != null && mScreenshot == null)
			{
				mViewToScreenshot.buildDrawingCache();
				mScreenshot = mViewToScreenshot.getDrawingCache();
			}

			try {

				ReportManager reportManager = ReportManager.getInstance(mContext);
				HashMap<String, String> carrier = reportManager.getDevice().getCarrierProperties();

				String twitterHandle = reportManager.getTwitterHandle(carrier);
				String carriername = carrier.get("carrier");
				if (carriername == null)
					carriername = "my carrier";
				if (twitterHandle == null || useTwitter == false)
					carrierhandle = carriername;

			} catch (Exception e) {
				try {

				} catch (Exception e1) {
				}
			}
		}
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) {
				filepath = /*"file://" + */ Environment.getExternalStorageDirectory().toString();
			}
			else {
				filepath = /*"file://" + */ mContext.getApplicationContext().getCacheDir().toString();
			}
			{
				ReportManager reportManager = ReportManager.getInstance(mContext.getApplicationContext());

				if (eventid == 0 && eventType != null)
				{
					eventid = reportManager.getLastEventOfType(eventType);

				}
				if (eventid > 0) {

					event = reportManager.getEventDetails(eventid);
					ieventtype = Integer.parseInt(event.get(ReportManager.EventKeys.TYPE));
					eventType = EventType.get(ieventtype);
					lastShortName = reportManager.getLastShortNameOfType(eventType);

					if (event.get(ReportManager.EventKeys.LONGITUDE) != null) {
						fLat = Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE));
						fLng = Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE));
					}

					long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
					dateEvent = new Date(timeStamp);
					realEventId = Long.parseLong(event.get(ReportManager.EventKeys.EVENTID));
					if (event.get(ReportManager.EventKeys.OPERATOR_ID).length() > 0)
						carrierID = Integer.parseInt(event.get(ReportManager.EventKeys.OPERATOR_ID));
					else
						carrierID = reportManager.getCurrentCarrier().ID;

					carrierhandle = carriername = event.get(ReportManager.EventKeys.CARRIER);

					int evtstr = eventType.getEventString();
					if (eventType == EventType.MAN_PLOTTING)
					{
						try {
							mSubject = mContext.getString(R.string.sharemessage_eventdetail_sampling);
							int ifloor = Integer.parseInt(event.get(ReportManager.EventKeys.RATING));
							evtfloor = "";
							if (ifloor == 0)
								evtfloor = " ground floor";
							else if (ifloor < 0)
								evtfloor += " underground level " + (-ifloor);
							else if (ifloor > 0)
								evtfloor += " floor " + (1 + ifloor);

							strShortName = "Sampling";
						}
						catch (Exception e) {}
					}
					else {
						mSubject = mContext.getString(R.string.sharemessage_eventdetail_event);
						strShortName = mContext.getString(evtstr) + " event";

					}
					if (lastShortName != null && lastShortName.length() > 0)
						strShortName = lastShortName;

					// replace tag for ADDRESS with geocode from the event location
					if (mSubject.indexOf ("@ADDRESS") >= 0)
					{
						if (fLng != 0) {
							Location location = new Location("");
							location.setLatitude(fLat);
							location.setLongitude(fLng);
							eventAddress = WebReporter.geocode(mContext, location);
							if (eventAddress == null || eventAddress.length() < 3)
								eventAddress = String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
						}
						else
							eventAddress = "(no location)";
					}
					mTextToShare = null;  // text message will be replaced with mSubject
				}
				else
					dateEvent = new Date();

				SimpleDateFormat sd = new SimpleDateFormat("MMM dd yy HH:mm");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateEvent);
				calendar.add(Calendar.DATE, -1);
				strEventdate = sd.format(calendar.getTime());


				// Check User for permission to View MyNetwork
				int allowMN = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PreferenceKeys.Miscellaneous.MYNETWORK_LINKS, 0);
				int allowBuildings = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PreferenceKeys.Miscellaneous.ALLOW_BUILDINGS, 0);
				if (allowMN == 1)
					allowMynetwork = true;
				if (allowBuildings == 2 && eventType == EventType.MAN_PLOTTING)
					allowLinks = false;


				return true;
			}
		} catch (Exception e)
		{}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean succeded) {
		if(succeded) {
			if (eventid > 0)
				askForNameToShare ();
			else
			{
				buildSubject ();
				sendShare();
			}

		}
		else {
			Toast.makeText(mContext, R.string.share_error, Toast.LENGTH_LONG).show();
		}

	}

	private String[] filePaths;
	private boolean prepareFiles ()
	{
		int files = 1;
		if (emailOnly)
			files = 2;
		filePaths = new String[files];
		String appname = Global.getAppName(mContext);
		filename = strShortName;
		if (strShortName.length() == 0)
			filename = appname;
		if (eventType == EventType.MAN_PLOTTING) {
			int ifloor = Integer.parseInt(event.get(ReportManager.EventKeys.RATING));
			filename += "_" + ifloor;
		}
		filename += "_" + strEventdate;
		filename = "/" + filename;//.replace(" ", "");
		filename = filepath + filename;

		if (filename != null) {
			FileOutputStream fos = null;
			try {
				if (mScreenshot == null || mScreenshot.isRecycled())
					return false;
				filePaths[0] = filename + ".png";
//				fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/mmc.png");
				fos = new FileOutputStream(filePaths[0]);
				if (fos == null)
					return false;
				mScreenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);

				fos.close();
			} catch (FileNotFoundException e) {
				return false;
			} catch (Exception e) {
				return false;
			}

		}

		return true;
	}
	private boolean prepareHtmlFile ()
	{
		try {
			filePaths[1] = filename + ".html";
			FileOutputStream fos = new FileOutputStream(filePaths[1]);
			if (fos == null)
				return false;
			fos.write(mTextToShare.getBytes(Charset.forName("UTF-8")));

			fos.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	private void sendShare () {
		Intent intent = null;

		try {
			ReportManager reportManager = ReportManager.getInstance(mContext.getApplicationContext());

			prepareFiles();
			if (emailOnly) {
				if (eventid > 0)
				{
					if (allowLinks) {
						mTextToShare += "\n" + mContext.getString(R.string.sharemessage_eventdetail_links);
						addShareLinks();
						mTextToShare = "<html><body>" + mTextToShare + "</body></html>";
						prepareHtmlFile();  // After share links are built, save this as an html file
					}
				}

				String shareEmail = PreferenceManager.getDefaultSharedPreferences(mContext).getString("KEY_SETTINGS_SHARETO_EMAIL", null);

				if (shareEmail != null && shareEmail.length() > 0 && shareEmail.indexOf("@") > 0)
					mEmailTo = shareEmail;
				if (mEmailTo != null)
					intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailTo});

				//int bGoogle = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);


				if (bInvite == true) {
//					intent = new AppInviteInvitation.IntentBuilder(mSubject)
//							.setMessage(mSubject)
//							.setDeepLink(mobileLink)
//							.setEmailSubject(mSubject)
//							.setEmailHtmlContent(mTextToShare)
//									//.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
//							//.setCallToActionText("Call To action")
//							.build();

					ArrayList<Uri> uris = new ArrayList<Uri>();
					//convert from paths to Android friendly Parcelable Uri's
					for (String file : filePaths) {
						if (file != null) {
							File fileIn = new File(file);
							Uri u = Uri.fromFile(fileIn);
							uris.add(u);
						}
					}
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				}
				else {
					intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
					intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mTextToShare));

					ArrayList<Uri> uris = new ArrayList<Uri>();
					//convert from paths to Android friendly Parcelable Uri's
					for (String file : filePaths) {
						if (file != null) {
							File fileIn = new File(file);
							Uri u = Uri.fromFile(fileIn);
							uris.add(u);
						}
					}
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				}
			} else {
				if (eventid > 0)
					mTextToShare += "\n" + mContext.getString(R.string.sharemessage_eventdetail_nolinks);
				intent = new Intent(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, mTextToShare);
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePaths[0]));
			}

			if (!bInvite)
			{
				Integer shareTwitter = mViewToScreenshot.getResources().getInteger(R.integer.SHARE_TWITTER);
				if (shareTwitter == 0 || emailOnly)
					intent.setType("message/rfc822");
				else {
					//intent.setType("text/plain");
					intent.setType("image/png");
				}

				intent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
			}

			//String filename = "/" + appname.replace(" ", "") + ".png";

			//		if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) {
			//			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + filename));
			//		}
			//		else {
			//			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + (mContext.getApplicationContext()).getCacheDir() + filename));
			//		}
			if (bInvite)
			{
				Activity activity = (Activity)mViewToScreenshot.getContext();//.getParent().getA

				activity.startActivityForResult(intent, 0);
			}
			else if (emailOnly)
				mContext.startActivity(intent);
			else
				mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_title)));

			reportManager.updateEventField(eventid, LocalStorageReporter.Events.KEY_SHORTNAME, strShortName);
			reportManager.updateEventField(eventid, LocalStorageReporter.Events.KEY_LONGNAME, mSubject);
		}
		catch (Exception e)
		{
			LoggerUtil.logToFile(LoggerUtil.Level.ERROR, "ShareTask", "sendShare", "exception", e);

		}
	}
	private void askForNameToShare ()
	{
		Activity activity = (Activity)mViewToScreenshot.getContext();//.getParent().getA
		//Ask if wants to sample more of the building
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		// ...Irrelevant code for customizing the buttons and title
		LayoutInflater inflater = activity.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.share_naming, null);
		dialogBuilder.setView(dialogView);

		String shortName = strShortName;

		String appname = Global.getAppName(mContext);
		buildSubject ();

		//		mainView.buildDrawingCache();
		//		final Bitmap mScreenshot = mainView.getDrawingCache();

		final EditText editName = (EditText)dialogView.findViewById(R.id.editName);
		editName.setText(mSubject);
		final EditText editShortName = (EditText)dialogView.findViewById(R.id.editShortName);
		editShortName.setText(shortName);
		ImageView screenView = (ImageView) dialogView.findViewById(R.id.imageScreen);
		screenView.setImageBitmap(mScreenshot);
		Button shareButton = (Button) dialogView.findViewById(R.id.buttonShare);
		Button cancelButton = (Button) dialogView.findViewById(R.id.buttonCancel);
		final CheckBox checkEmailLinks = (CheckBox)dialogView.findViewById(R.id.checkEmailLinks);
		//CheckedTextView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		checkEmailLinks.setChecked(true);
		alertDialog = dialogBuilder.create();

		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSubject = editName.getText ().toString();
				emailOnly = checkEmailLinks.isChecked();
				strShortName = editShortName.getText ().toString();
				sendShare ();
				alertDialog.dismiss();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		editShortName.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
				if(s.length() != 0)
				{
					strShortName = editShortName.getText ().toString();
					lastShortName = strShortName;
					buildSubject();
					editName.setText(mSubject);
				}
			}
		});

		alertDialog.show();
		alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				alertDialog = null;
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(alertDialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		alertDialog.getWindow().setAttributes(lp);
	}

	private void addShareLinks ()
	{
		//ReportManager reportManager = ReportManager.getInstance(mContext.getApplicationContext());

		if (eventid > 0) {

			//HashMap<String, String> event = reportManager.getEventDetails(eventid);

			SimpleDateFormat sd = new SimpleDateFormat("MMM dd yyyy");
			//Calendar calendar = new GregorianCalendar(dateEvent.getYear(), dateEvent.getMonth(), dateEvent.getDate());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateEvent);

			calendar.add(Calendar.DATE, -1);
			String startdate = sd.format(calendar.getTime());
			calendar.add(Calendar.DATE, 3);
			String enddate = sd.format(calendar.getTime());
			calendar.setTime(dateEvent);
			calendar.add(Calendar.MONTH, -3);
			String startdate2 = sd.format(calendar.getTime());

			String appurl = Global.getApiUrl(mContext);
			String servername = "app";
			if (appurl.indexOf("dev.") >= 0)
				servername = "devmynetwork";
			String link = "https://" + servername + ".mymobilecoverage.com/MyNetwork/simpleshare.html?userID=" + Global.getUserID(mContext) + "&type=" + ieventtype + "&carrierID=" + carrierID;
			link += "&id=" + realEventId + "&eventsmode=evt&limit=20000&startdate=" + startdate + "&enddate=" + enddate;
			link += "&filternames=all";
			link += "&zoom=14&lat=" + fLat + "&lng=" + fLng + "&expand=1&mob=1";
			link += "&apiKey=" + Global.getApiKey(mContext);
			if (bInvite == true) {
				String deepLink = buildDeepLink(link);
				mobileLink = Uri.parse(link);
			}


			//linkMN += "&evtname=Floor " + samplingEvent.getEventIndex() + " sampling";

			TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

			String linkEvent = Global.getApiUrl(mContext) + "/api/coveragedata?userid=" + Global.getUserID(mContext) + "&eventTypes=60&neighbors=1&json=0&samples=1&datadetail=1&evtMode=drivesmp&carriers=" + carrierID;
			linkEvent += "&start=" + startdate + "&stop=" + enddate + "&imsi=" + telephonyManager.getSubscriberId() + "&eventid=" + realEventId + "&download=1";
			linkEvent += "&apiKey=" + Global.getApiKey(mContext);
			//String linkCov = buildCoverageUrl(carrier, (int) samplingEvent.getEventIndex()); // PreferenceManager.getDefaultSharedPreferences(this).getString("LASTCOVERAGEURL", "");
			//linkCov += "&eventid=" + samplingEvent.getEventID() + "&start=" + startdate + "&stop=" + enddate;

			//mTextToShare += "<img id=\"MMClogo\" src=\"https://lb1.mymobilecoverage.com/img/mmcLOGO.png\">";

			String appname = Global.getAppName(mContext);
			if (bInvite)
				mTextToShare += "<br><br><a href='%%APPINVITE_LINK_PLACEHOLDER%%'>View in " + appname + "</a><br><br>";
			else {
				String deepLink = buildDeepLink(link);
				mTextToShare += "<br><br><a href='" + deepLink + "'>View in " + appname + "</a><br><br>";
			}
			mTextToShare += "<br><br><a href='" + link + "'>View in browser</a><br><br>";

			if (allowMynetwork) {
				String linkMN = "https://" + servername + ".mymobilecoverage.com/MyNetwork/mainindex2.html?userID=" + Global.getUserID(mContext) + "&type=" + ieventtype + "&carrierID=" + carrierID;
				linkMN += "&id=" + realEventId + "&eventsmode=evt&limit=20000&startdate=" + startdate2 + "&enddate=" + enddate;
				linkMN += "&filternames=all";
				linkMN += "&zoom=14&lat=" + fLat + "&lng=" + fLng + "&expand=1&mob=1";
				linkMN += "&apiKey=" + Global.getApiKey(mContext);

				mTextToShare += "<a href='" + urlencode(linkMN) + "'>View in MyNetwork (with login)</a><br><br>";
				mTextToShare += "<a href='" + urlencode(linkEvent) + "'>CSV Download</a><br><br>";
			}
			mTextToShare = mTextToShare.replace("\n", "<br>");

			//mTextToShare += "<a href='" + urlencode(linkCov) + "'>Coverage Image</a><br>";

		}

	}

	public Uri buildDeepLink(String deepLink, int minVersion) {
		// Get the unique appcode for this app.
		String appCode = mContext.getString(R.string.app_firebase_code);

		// Get this app's package name.
		String packageName = mContext.getPackageName();

		// Build the link with all required parameters
		Uri.Builder builder = new Uri.Builder()
				.scheme("https")
				.authority(appCode + ".app.goo.gl")
				.path("/")
				.appendQueryParameter("link", deepLink.toString())
				.appendQueryParameter("apn", packageName);


		// Minimum version is optional.
		if (minVersion > 0) {
			builder.appendQueryParameter("amv", Integer.toString(minVersion));
		}

		// Return the completed deep link.
		return builder.build();
	}

	public String buildDeepLink(String deepLink) {
		// Get the unique appcode for this app.
		String appCode = mContext.getString(R.string.app_firebase_code);

		// Get this app's package name.
		String packageName = mContext.getPackageName();
		String link = "https://" + appCode + ".app.goo.gl/?link=";
		deepLink = deepLink.replace("&", "%26");
		deepLink = deepLink.replace("=", "%3D");
		deepLink = deepLink.replace(" ", "%20");
		link += deepLink;
		link += "&apn=" + packageName;

		return link;

	}

	private String urlencode (String url)
	{
		url = url.replace (" ", "+");
		return url;
	}



	private void buildSubject ()
	{
		if (mSubject == null)
			mSubject = "";
		if (eventid > 0) {
			if (eventType == EventType.MAN_PLOTTING) {
				mSubject = mContext.getString(R.string.sharemessage_eventdetail_sampling);
				if (mSubject.indexOf("@FLOOR") >= 0)
					mSubject = mSubject.replaceAll("@FLOOR", evtfloor);

			} else {
				mSubject = mContext.getString(R.string.sharemessage_eventdetail_event);

			}
			if (lastShortName != null && lastShortName.length() > 0)
				strShortName = lastShortName;
			mSubject = String.format(mSubject, strShortName);

			// replace tag for ADDRESS with geocode from the event location
			if (mSubject.indexOf("@ADDRESS") >= 0) {
				mSubject = mSubject.replaceAll("@ADDRESS", eventAddress);
			}
			mTextToShare = null;  // text message will be replaced with mSubject

		}
		if (mSubject.indexOf("@DATETIME") >= 0) {
			mSubject = mSubject.replaceAll("@DATETIME", strEventdate);
		}

		String appname = Global.getAppName(mContext);

		mSubject = mSubject.replaceAll("@CARRIERHANDLE", carrierhandle);
		mSubject = mSubject.replaceAll("@CARRIERNAME", carriername);
		mSubject = mSubject.replaceAll("#MyMobileCoverage", appname);
		mSubject = mSubject.replaceAll("@LOGIN", Global.getLogin(mContext));


		int pos = mSubject.indexOf('/');
		if (pos > 0) {
			String sub = mSubject;
			mSubject = sub.substring(0, pos).trim();
			mSubject2 = sub.substring(pos + 1, sub.length()).trim();
		}

		if (mTextToShare == null)
			mTextToShare = mSubject + " " + mSubject2;
		mTextToShare = mTextToShare.replaceAll("@CARRIERHANDLE", carrierhandle);
		mTextToShare = mTextToShare.replaceAll("@CARRIERNAME", carriername);
		mTextToShare = mTextToShare.replaceAll("#MyMobileCoverage", appname);
		mTextToShare = mTextToShare.replaceAll("@LOGIN", Global.getLogin(mContext));
	}
}
