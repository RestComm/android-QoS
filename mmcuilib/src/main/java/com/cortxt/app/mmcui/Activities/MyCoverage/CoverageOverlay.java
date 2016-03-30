package com.cortxt.app.mmcui.Activities.MyCoverage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cortxt.app.mmcui.Activities.MMCMapActivity;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.DataObjects.Carrier;
import com.cortxt.app.mmcutility.DataObjects.MMCDevice;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.MMCLogger;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CoverageOverlay extends Overlay implements MMCMapView.OnChangeListener, MMCMapView.OnZoomLevelChangeListener {
	
	/**
	 * Minimum zoom level to show coverage at.
	 * If the user is zoomed out farther than this, coverage will not be requested
	 */
	private static final int ZOOM_LEVEL_THRESHOLD = 10;
	
	/**
	 * Time to wait after coverage is finished loading before loading coverage again if map has changed since
	 * last time coverage was requested (in milliseconds)
	 */
	private static final long RELOAD_DELAY = 500;
	
	private Context mContext;
	private MapView mMapView;
	private Carrier carrier;
	protected CoverageRequestTask mCoverageRequestTask;
	protected CoverageResponse mCoverageResponse;
	protected List<Carrier> carriers;
	/**
	 * Flag that indicates if map has changed since the last coverage request was started
	 */
	protected boolean mMapChanged;
	protected AlertDialog mErrorDialog;
	protected ProgressBar mBusyIndicator;
	private Handler mHandler = new Handler();
	private Timer coverageTimer = new Timer();
	public long lastLayout = 0;
	private Bitmap mCoverageImage = null;
	//private byte[] mPngBytes = null;
	private boolean bClosed = false;
	private static final String TAG = CoverageOverlay.class.getSimpleName();
	
	private GeoPoint lastKnownMapCenter;
	private int lastKnownLatitudeSpanE6, topopsLat;
	private int lastKnownLongitudeSpanE6, topopsLng;
	private float screenDensityScale;
	private float logoH = 50, logoW = 150, logoX = 15, logoY = 10;
	private Bitmap scaledLogo = null;
	private RectF logoRect;
	private GeoPoint mapCenterOSMFix = null;
	
	/**
	 * This is the area for which map coverage has been requested.
	 */
	private Rect haveCoverageForArea;
	/**
	 * This flag is set if the zoom level changes. This flag is specifically for the coverage overlay.
	 * NOTE: there are separate flags for coverage and events because they need to be unset separately
	 * after their request threads are started.
	 */
	private int zoomLevelChange;
	/**
	 * This flag is set if the map changes while a coverage request is being processed.
	 * While a request is being processed, we can't start a new request with the
	 * new center and lat/long spans, so we store them, and start a new request
	 * after the current request finishes.
	 */
	private int previousZoomLevel;
	private boolean coverageUpdateRequired;
	private Paint paint;
	private boolean topopStale = true;
	private CoverageTimerTask coverageTimerTask;
	private int floor = -999;
	private int previousFloor = -999;
	private String mapmethod = "";
	
	public CoverageOverlay(Context context, MapView mapView, ProgressBar busyIndicator, String mapMethod) {
		mContext = context;
		mMapView = mapView;
		mMapChanged = false;
		mBusyIndicator = busyIndicator;
		haveCoverageForArea = new Rect();
		screenDensityScale = context.getResources().getDisplayMetrics().density;
		paint = new Paint();
		logoH *= screenDensityScale;
		logoW *= screenDensityScale;
		logoX *= screenDensityScale;
		logoY *= screenDensityScale;
		carriers = ReportManager.getInstance(mContext.getApplicationContext()).getTopOperators (0, 0, 0, 0, 15, false);
		this.mapmethod = mapMethod;
	}
	
	public void setFloor(int floor) {
		previousFloor = this.floor;
		this.floor = floor;
		forceNewCoverage();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//super.draw (canvas, mapView, shadow);
		synchronized (this)
		{
			if(mCoverageResponse != null && mCoverageImage != null && !mCoverageImage.isRecycled()) {
				GeoPoint nw = new GeoPoint(mCoverageResponse.getNE().getLatitudeE6(), mCoverageResponse.getSW().getLongitudeE6());
				GeoPoint se = new GeoPoint(mCoverageResponse.getSW().getLatitudeE6(), mCoverageResponse.getNE().getLongitudeE6());
				
				Point topLeft = mapView.getProjection().toPixels(nw, null);
				Point bottomRight = mapView.getProjection().toPixels(se, null);
				
				Rect imageRect = new Rect(0, 0, mCoverageImage.getWidth(), mCoverageImage.getHeight());
				Rect mapRect = new Rect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
				
				canvas.drawBitmap(mCoverageImage, imageRect, mapRect, null);
				
				getLogo ();
				if (scaledLogo != null && logoRect != null && mapView.getResources().getInteger(R.integer.MAP_LOGO) == 1)
					canvas.drawBitmap(scaledLogo, null, logoRect, paint);
			}
		}
	}
	
	// render a scaled version of the carrier logo for the map
	private void getLogo ()
	{
		if (scaledLogo != null)
			return;
		// Draws the carrier logo for the top carrier in the current category
		if (carrier != null)
		{
			if (carrier.Logo == null)
			{
				String logoPath = mContext.getApplicationContext().getFilesDir() + carrier.Path;
				try
				{
					carrier.Logo = BitmapFactory.decodeFile(logoPath);
				}
				catch (Exception e)
				{
					MMCLogger.logToFile(MMCLogger.Level.ERROR, "CoverageOverlay", "getLogo", "error loading logo " + logoPath, e);
				}
			}
			if (carrier.Logo == null)
			{
				scaledLogo = null;
				return;
			}
			float boxH = logoH;
			float boxW = logoW;
			float w = (float)carrier.Logo.getWidth() * screenDensityScale / 1.5f;
			float h = (float)carrier.Logo.getHeight() * screenDensityScale / 1.5f;
			float drawW = 0.0f;
			float drawH = 0.0f;
			float boxRatio = boxW / boxH;
			float imgRatio = w / h;
			// Carrier logo may require some aspect ratio scaling because the online carrier logo images have different sizes and shapes
			if (imgRatio > boxRatio)
			{
				drawW = 8*boxW/10;
				if (Math.abs(drawW-w) < boxW/6)
					drawW = w;
				drawH = drawW/imgRatio;
			}
			else
			{
				drawH = 8*boxH/10;
				if (Math.abs(drawH-h) < boxH/6)
					drawH = h;
				drawW = drawH*imgRatio;
			}	
			scaledLogo = Bitmap.createScaledBitmap(carrier.Logo, (int)drawW, (int)drawH, true);
			//logoRect = new RectF(logoX+(boxW-drawW)/2, logoY+(boxH-drawH)/2, logoX+(boxW+drawW)/2, logoY + (boxH+drawH)/2); 
			logoRect = new RectF(logoX, logoY, logoX+drawW, logoY + drawH); 
			
		}
	}
	
	public void forceNewCoverage() {
		lastLayout = System.currentTimeMillis();
		haveCoverageForArea = new Rect();
		coverageTimerTask = new CoverageTimerTask(mapmethod);
		coverageTimer.schedule(coverageTimerTask, 1200);	
	}
	
	@Override
	public void onChange() {
		if(mMapView.getZoomLevel() > ZOOM_LEVEL_THRESHOLD) {
			lastLayout = System.currentTimeMillis();
			coverageTimerTask = new CoverageTimerTask(mapmethod);
			coverageTimer.schedule(coverageTimerTask, 1200);	
		}
		else if (previousZoomLevel > ZOOM_LEVEL_THRESHOLD)
		{
			Toast toast = Toast.makeText(mContext, R.string.mycoverage_zoomout, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			disposeImage();
			haveCoverageForArea = new Rect();
			coverageUpdateRequired = false;
			previousZoomLevel = mMapView.getZoomLevel();
			mMapChanged = false;
		}
//		
//		int threshold = 10;
//		int zoom = mMapView.getZoomLevel();
////		System.out.println("zoom " + zoom);
//		if(zoom <= 17 && zoom >= 14)
//			threshold = 100;
//		else if(zoom <= 17 && zoom >= 14)
//			threshold = 300;
//		else if(zoom <= 13 && zoom >= 10)
//			threshold = 1300;
//		else return;
//		
//		if(mapCenterOSMFix == null) {
//			mapCenterOSMFix = (GeoPoint) mMapView.getMapCenter();
//			if(mapCenterOSMFix.getLatitudeE6() == 0 && mapCenterOSMFix.getLongitudeE6() == 0)
//				mapCenterOSMFix = null;
//		}
//		else if(distanceTo((GeoPoint)mMapView.getMapCenter(), mapCenterOSMFix) > threshold) {
//			mMapView.getController().animateTo(mapCenterOSMFix);
////			System.out.println("animated " + threshold);
//		}
//		else {
//			mapCenterOSMFix = (GeoPoint) mMapView.getMapCenter();
//		}
		
		
	}
	
//	public float distanceTo(GeoPoint StartP, GeoPoint EndP) {
//		
//		if(EndP == null) 
//			return 0;
//		
//	    int Radius = 6371;//radius of earth in Km         
//	    double lat1 = StartP.getLatitudeE6()/1E6;
//	    double lat2 = EndP.getLatitudeE6()/1E6;
//	    double lon1 = StartP.getLongitudeE6()/1E6;
//	    double lon2 = EndP.getLongitudeE6()/1E6;
//	    double dLat = Math.toRadians(lat2-lat1);
//	    double dLon = Math.toRadians(lon2-lon1);
//	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//	    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//	    Math.sin(dLon/2) * Math.sin(dLon/2);
//	    double c = 2 * Math.asin(Math.sqrt(a));
//	    double valueResult= Radius*c;
//	    double km = valueResult/1;
//	    DecimalFormat newFormat = new DecimalFormat("####");
//	    float kmInDec =  Float.valueOf(newFormat.format(km));
////	    float meter = kmInDec%1000;
//	    System.out.println("kms: " + kmInDec);
//	    return kmInDec;
////	    return meter;
//	 }

	@Override
	public void onZoomLevelChange() {
		
		int change = mMapView.getZoomLevel() - previousZoomLevel;
		zoomLevelChange += change;
		
	}
	/**
	 * Deletes the native memory for the image 
	 */
	private void disposeImage() {
		if(mCoverageImage != null) {
			mCoverageImage.recycle();
			mCoverageImage = null;
		}
		System.gc();
	}
	/**
	 * Clears the image that shows coverage
	 */
	public void clear() {
		disposeImage();
		bClosed = true;
	}
	
	/**
	 * stop the network location manager after less than a second
	 */
	class CoverageTimerTask extends TimerTask {
		
		private String mapmethod = "";
		
		public CoverageTimerTask(String mapmethod) {
			this.mapmethod = mapmethod;
		}
		
		@Override
		public void run() {
			//MyCoverageMapActivity context = null;
			MMCMapActivity mappingContext = null;
			
//			try {
//				context = (MyCoverageMapActivity) mMapView.getContext();
//			} catch(Exception e) {
////				e.printStackTrace();
//			}
			
			try {
				mappingContext = (MMCMapActivity) mMapView.getContext();
			} catch(Exception e) {
//				e.printStackTrace();
			}
				
			if (carrier == null)
				carrier = ReportManager.getInstance(mContext.getApplicationContext()).getCurrentCarrier();
				
			if (lastLayout != 0 && lastLayout + 1000 <  System.currentTimeMillis())
     		{
//				if(context != null) {
//					 context.runOnUiThread(new Runnable() {
//						// @Override  
//		                 public void run() { 
//		                	// MMCMapActivityOld context = (MMCMapActivityOld) EventsMapViewOld.super.getContext();
//		                	 //if(mCoverageRequestTask == null || mCoverageRequestTask.getStatus() != AsyncTask.Status.RUNNING) {
//		         			//	mCoverageRequestTask = new CoverageRequestTask();
//		                	 { 
//		                		 runCode();
//		        			
//		                		 //mMapChanged = false;
//		                	 }
//		                	 //else {
//		                	 //	mMapChanged = true;
//		                	 //}
//		                	 lastLayout = 0;
//		                 }
//					 });
//				}
//				else 
				if(mappingContext != null) {
					mappingContext.runOnUiThread(new Runnable() {
						// @Override  
		                 public void run() { 
		                	 runCode();
		                	 lastLayout = 0;
		                 }
					 });
				}
     		}
         }
	}
			
	public void runCode() {
		String type = CoverageRequestWin.TYPE_RSSI;
		MMCDevice device = ReportManager.getInstance(mContext.getApplicationContext()).getDevice();
			
		//HashMap<String, String> carrierProperties = device.getCarrierProperties();
		
		GeoPoint center = (GeoPoint) mMapView.getMapCenter();
		int latSpanE6 = mMapView.getLatitudeSpan() * 2;
		int longSpanE6 = mMapView.getLongitudeSpan() * 2;
		
		int swLatE6 = round(center.getLatitudeE6() - latSpanE6/2, false);
		int swLongE6 = round(center.getLongitudeE6() - longSpanE6/2, false);
		int neLatE6 = round(center.getLatitudeE6() + latSpanE6/2, true);
		int neLongE6 = round(center.getLongitudeE6() + longSpanE6/2,true);
		
		
		int centerLatitudeE6, centerLongitudeE6, screenDlatE6, screenDlongE6;
		
		//while the map is being draw, latitude and longitude spans are invalid values, so we need to ensure they are valid before using them
		boolean isMapLatLongSpanValid = (mMapView.getLatitudeSpan() != 0 && mMapView.getLongitudeSpan() != 360000000);
		if(!isMapLatLongSpanValid && lastKnownMapCenter != null) {
			centerLatitudeE6 = lastKnownMapCenter.getLatitudeE6();
			centerLongitudeE6 = lastKnownMapCenter.getLongitudeE6();
			screenDlatE6 = lastKnownLatitudeSpanE6;
			screenDlongE6 = lastKnownLongitudeSpanE6;
		}
		else if (isMapLatLongSpanValid) {
			centerLatitudeE6 = mMapView.getMapCenter().getLatitudeE6();
			centerLongitudeE6 = mMapView.getMapCenter().getLongitudeE6();
			screenDlatE6 = mMapView.getLatitudeSpan();
			screenDlongE6 = mMapView.getLongitudeSpan();
		}
		else
			return;
		
		int screenTopLeftLatE6 = centerLatitudeE6 + screenDlatE6/2;
		int screenTopLeftLongE6 = centerLongitudeE6 - screenDlongE6/2;
		int screenBottomRightLatE6 = centerLatitudeE6 - screenDlatE6/2;
		int screenBottomRightLongE6 = centerLongitudeE6 + screenDlongE6/2;
		
		//rectangle that has the currently visible part of the screen (or last known part of screen, if current values are invalid)
		//boolean screenAreaIsCovered = haveCoverageForArea.contains(screenTopLeftLongE6, screenTopLeftLatE6, screenBottomRightLongE6, screenBottomRightLatE6);
		boolean screenAreaIsCovered = doesRectContain (haveCoverageForArea, screenTopLeftLongE6, screenTopLeftLatE6, screenBottomRightLongE6, screenBottomRightLatE6);
		int zoomLevelChange = mMapView.getZoomLevel() - previousZoomLevel;
		if(!screenAreaIsCovered || Math.abs(zoomLevelChange) >= 2 || (mapmethod.equals("survey") && previousFloor != floor)) {
			//start thread to request map coverage
			if(mCoverageRequestTask == null || mCoverageRequestTask.getStatus() == AsyncTask.Status.FINISHED) {
 				mCoverageRequestTask = new CoverageRequestTask();
 				previousFloor = floor;
			//if(coverageRequestTask == null || coverageRequestTask.getStatus() == AsyncTask.Status.FINISHED) {
				
				//int usedMegs = (int)(Debug.getNativeHeapAllocatedSize() / 1048576L);
				//String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				//Log.d(TAG,usedMegsString);
				
				int coveredAreaDlatE6 = screenDlatE6 * 2;
				int coveredAreaDlongE6 = screenDlongE6 * 2;
				
				final double bottomLeftLat = round(((double)centerLatitudeE6 - coveredAreaDlatE6/2.0) / 1000000.0, false);
				final double bottomLeftLong = round(((double)centerLongitudeE6 - coveredAreaDlongE6/2.0) / 1000000.0, false);
				final double topRightLat = round(((double)centerLatitudeE6 + coveredAreaDlatE6/2.0) / 1000000.0, true);
				final double topRightLong = round(((double)centerLongitudeE6 + coveredAreaDlongE6/2.0) / 1000000.0, true);
				haveCoverageForArea.set((int) (bottomLeftLong*1000000),
										(int) (topRightLat*1000000),
										(int) (topRightLong*1000000),
										(int) (bottomLeftLat*1000000));
				GeoPoint sw = new GeoPoint(haveCoverageForArea.bottom, haveCoverageForArea.left);
 				GeoPoint ne = new GeoPoint(haveCoverageForArea.top, haveCoverageForArea.right);
				//GeoPoint sw = new GeoPoint(swLatE6, swLongE6);
 				//GeoPoint ne = new GeoPoint(neLatE6, neLongE6);
 				
 				//CoverageRequest request = new CoverageRequest(mContext, type, carrierProperties, sw, ne);
 				CoverageRequestWin request = new CoverageRequestWin(mContext, type, carrier, sw, ne, mapmethod, floor);
 				//mCoverageRequestTask.execute(request);
 				TaskHelper.execute(mCoverageRequestTask, request);
 	
				coverageUpdateRequired = false;
				previousZoomLevel = mMapView.getZoomLevel();
				mMapChanged = false;
				zoomLevelChange = 0;
			}
			else {
				coverageUpdateRequired = true;
				lastKnownMapCenter = (GeoPoint) mMapView.getMapCenter();
				lastKnownLatitudeSpanE6 = mMapView.getLatitudeSpan();
				lastKnownLongitudeSpanE6 = mMapView.getLongitudeSpan();
				mMapChanged = true;
			}
			
		}
     }	
		
	/**
	 * @return last coverage shown on map, to be used for caching
	 */
	public Parcelable getLastCoverage() {
		return mCoverageResponse;
	}
	
	//public byte[] getLastPng ()
	//{
	//	return mPngBytes;
	//}
	
	public void setLastCoverage(Bundle savedInstanceState) {
		
		int lastzoom = savedInstanceState.getInt(MyCoverage.KEY_ZOOM_LEVEL);
		mCoverageResponse = (CoverageResponse) savedInstanceState.getParcelable(MyCoverage.KEY_COVERAGE_OVERLAY);
		if (mCoverageResponse.getOperatorId() != null && mCoverageResponse.getOperatorId().length() > 10)
		{
			setOperatorId (mCoverageResponse.getOperatorId ());
		}
		GeoPoint topright = mCoverageResponse.getNE();
		GeoPoint bottomleft = mCoverageResponse.getSW();
		// Prevent coverage from reloading by setting bounds and zoom
		haveCoverageForArea.set((int) (bottomleft.getLongitudeE6()),
								(int) (topright.getLatitudeE6()),
								(int) (topright.getLongitudeE6()),
								(int) (bottomleft.getLatitudeE6()));
		try
		{
			if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) {
				mCoverageImage = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/mmccoverage.png");
			}
			else {
				mCoverageImage = BitmapFactory.decodeFile((mContext.getApplicationContext()).getCacheDir().toString() + "/mmccoverage.png");
			}			
//			mCoverageImage = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/mmccoverage.png");
		} catch (OutOfMemoryError e) {}
		/*
		int pngSize = mCoverageResponse.getPngsize();
		 
		if (pngSize <= 0)
			return;
		mPngBytes = new byte[pngSize];
		FileInputStream fis;
		try
		{
			fis = new FileInputStream(Environment.getExternalStorageDirectory().toString() + "/mmccoverage.png");
			if (fis != null)
			{	
				fis.read(mPngBytes);
				fis.close();
			}
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
		}
		catch (Exception e) {
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		try
		{
			mCoverageImage = BitmapFactory.decodeByteArray(mPngBytes, 0, mPngBytes.length, options);
		}
		catch (OutOfMemoryError e)
		{
			MMCLogger.logToFile(Level.ERROR, TAG, "setLastCoverage", "OutOfMemoryError: " + e.toString());
		}
		catch (Exception e)
		{
			MMCLogger.logToFile(Level.ERROR, TAG, "setLastCoverage", "", e);
		}
		*/
			// set zoom level so coverage won't reload due to zoom change
		previousZoomLevel = lastzoom;
		mMapView.invalidate();
		// force to reload list of carriers next update
		topopsLat = 100;
	}
	
	/**
	 * Rounds a given coordinate to the appropriate number for the request to the server.
	 * This is needed because the server keeps tiles for specific latitude and longitude values, so the request needs to
	 * provide latitude and longitude values for the corners of the tiles, rather than arbitrary values.
	 * @param coordinate
	 * @param roundUp if true: round <code>value</code> up, if false: ronud down
	 * @return rounded coordinate value
	 */
	public static int round(int coordinate, boolean roundUp) {
		double value = coordinate/1000000.0;
		double roundedValue = value * 250.0;
		if (roundUp)
			roundedValue = Math.ceil(roundedValue);
		else
			roundedValue = Math.floor(roundedValue);
		roundedValue = roundedValue / 250.0;
		return (int)(roundedValue*1000000);
	}
	public static double round(double value, boolean roundUp) {
		double roundedValue = value * 250.0;
		if (roundUp)
			roundedValue = Math.ceil(roundedValue);
		else
			roundedValue = Math.floor(roundedValue);
		roundedValue = roundedValue / 250.0;
		return roundedValue;
	}
	public static boolean doesRectContain (Rect area, int left, int top, int right, int bottom)
	{
		if (left >= area.left && right <= area.right && top <= area.top && bottom >= area.bottom)
			return true;
		return false;
	}
	
	

	/**
	 * AsyncTask that gets the coverage image from server
	 * @author nasrullah
	 *
	 */
	class CoverageRequestTask extends AsyncTask<CoverageRequestWin, Void, CoverageResponse> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mBusyIndicator.setVisibility(View.VISIBLE);
		}

		@Override
		protected CoverageResponse doInBackground(CoverageRequestWin... request) {
			
			try {
				int lengthOfFile = 0;				
				String filename = null;
				String state = Environment.getExternalStorageState().toString();  //   removed
				String storage = null;
				
				String cache = (mContext.getApplicationContext()).getCacheDir().toString()  + "/mmccoverage.png";
				String externalFile = Environment.getExternalStorageDirectory().toString() + "/mmccoverage.png";
				
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					storage = externalFile;
					filename = storage;  //"/mnt/sdcard"
				}
				else {
					storage = cache;
					filename = storage; 
				}
				
				try {
			        File f = new File(filename);  //  /mnt/sdcard/mmccoverage.png if no sdcard /data/data/com.cortxt.MMC.app/cache/mmccoverage.png
			        int count;
		            URL url = request[0].getURL();
		            URLConnection connection = url.openConnection();
		            connection.connect();
		            lengthOfFile = connection.getContentLength();  
		            long total = 0;
		            InputStream input = new BufferedInputStream(url.openStream());  
		            OutputStream output = new FileOutputStream(f);  
		            byte data[] = new byte[1024];
		            while ((count = input.read(data)) != -1) {
		                total += count;
		                output.write(data, 0, count);
		            }
		            output.flush();
		            output.close();
		            input.close();
			        
			    } catch (Exception e) {
			    	return null;
			        //Log.e("Download Error: ", e.toString());
			    }
				
				// while in background, make sure we have a list of top 5 operators for the overlays menu
				// try to preload carrier logo
				new Thread(new Runnable() 
				{
					@Override
					public void run() { 
						try {
							GeoPoint center = mMapView.getMapCenter();
							// Load a list of top operators if we dont have a list, or if the location changed by > 1 degree
							if (topopStale == true || carriers == null || (topopsLat != 0 && (Math.abs(topopsLat - center.getLatitudeE6()) > 1000000 || Math.abs(topopsLng - center.getLongitudeE6()) > 1000000)))
							{
								carriers = ReportManager.getInstance(mContext.getApplicationContext()).getTopOperators ((double)center.getLatitudeE6()/1000000, (double)center.getLongitudeE6()/1000000, 10000, 0, 15, true);
								topopsLat = center.getLatitudeE6();
								topopsLng = center.getLongitudeE6();
								topopStale = false;
							}
							else if (topopsLat == 0) // keep track of the last topops position the first time (or when the list is reloaded)
							{
								topopsLat = center.getLatitudeE6();
								topopsLng = center.getLongitudeE6();
							}
						}
						catch (Exception e) {
							MMCLogger.logToFile(MMCLogger.Level.ERROR, TAG, "getTopOperators", "", e);
							//cannot do anything about this exception
						}
		            }}).start();

				//mPngBytes = response;
				//String s = response.toString();
				// do not create a bitmap after coverage has been cleared and map has been closed
				// map may be closed if coverage arrives late
				if (!bClosed)
				{
					synchronized (CoverageOverlay.this)
					{
						disposeImage();
						//Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
						mCoverageImage = BitmapFactory.decodeFile(storage);
						String opid = "";
						if (carrier != null)
							opid = carrier.OperatorId;
						return new CoverageResponse(lengthOfFile, request[0].getSW(), request[0].getNE(), opid);
					}
				}
			}
			catch (Exception e) {
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(CoverageResponse result) {
			mBusyIndicator.setVisibility(View.GONE);
			
			if(result != null) {
				mCoverageResponse = result;
				mMapView.invalidate();
				
				if(mMapChanged) {
					//load coverage image again
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							onChange();
						}
					}, RELOAD_DELAY);
				}
			}
			else if (!bClosed) {
				Toast toast = Toast.makeText(mContext, R.string.mycoverage_coverageerror_message, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				/*
				if(mErrorDialog == null || !mErrorDialog.isShowing()) {
					try
					{
						mErrorDialog = new AlertDialog.Builder(mContext)
								.setTitle(R.string.mycoverage_coverageerror_title)
								.setMessage(R.string.mycoverage_coverageerror_message)
								.setNeutralButton(R.string.mycoverage_coverageerror_button_ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										//do nothing, dialog will close on its own
									}
								})
								.show();
					}
					catch (Exception e)
					{
					}
				}
				*/
			}
		}
		
	}

	public Carrier getCarrier ()
	{
		return carrier;
	}
	public void setOperatorId(String opid) {
		
		try {
			//carriers = ReportManager.getInstance(mContext.getApplicationContext()).getTopOperators (0, 0, 0, 0, 5, false);
			if (carriers != null)
			{
				for (int i=0; i<carriers.size(); i++)
				{
					if (carriers.get(i).OperatorId.equals(opid))
					{
						carrier = carriers.get(i);
						break;
					}
				}
			}
			else
				carrier =  ReportManager.getInstance(mContext.getApplicationContext()).getCurrentCarrier();
			if (carrier != null)
			{
				previousZoomLevel = 1;
				scaledLogo = null;
				onChange();
				Carrier currentCarrier = ReportManager.getInstance(mContext.getApplicationContext()).getCurrentCarrier();
				if (currentCarrier != null && !currentCarrier.OperatorId.equals(opid))
					((MyCoverage)mContext).getEventsOverlay().show (false);
				else
					((MyCoverage)mContext).getEventsOverlay().show (true);
			}
		} catch (Exception e) {
			
		}
		
	}
}
class CoverageResponse implements Parcelable {
	//byte[] png; 
	private GeoPoint ne;
	private GeoPoint sw;
	private int pngSize = 0;
	private String operatorId;
	private int errorMessageResource;
	
	public static final Parcelable.Creator<CoverageResponse> CREATOR = new Parcelable.Creator<CoverageResponse>() {
		@Override
		public CoverageResponse createFromParcel(Parcel source) {
			return new CoverageResponse(source);
		}
		
		@Override
		public CoverageResponse[] newArray(int size) {
			return new CoverageResponse[size];
		}
	};
	
	public CoverageResponse(Parcel in) {
		//the first integer in the parcel indicates whether or not an image is present in the parcel
		if(in.readInt() == 1) {
			//this.image = Bitmap.CREATOR.createFromParcel(in);
			ne = new GeoPoint(in.readInt(), in.readInt());
			sw = new GeoPoint(in.readInt(), in.readInt());
			pngSize = in.readInt();
			operatorId = in.readString();
		}
	}
	
	//public CoverageResponse(Bitmap image, GeoPoint topRight, GeoPoint bottomLeft) {
	public CoverageResponse(int pngsize, GeoPoint sw, GeoPoint ne, String opid) {
		//this.image = image;
		this.ne = ne;
		this.sw = sw;
		this.pngSize = pngsize;
		this.operatorId = opid;
		this.errorMessageResource = -1;
	}
	
	public CoverageResponse(int errorMessageResource) {
		this.errorMessageResource = errorMessageResource;
	}

	/*
	public Bitmap getImage() {
		if (bmimage == null && png != null)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			bmimage = BitmapFactory.decodeByteArray(png, 0, png.length, options);
			return bmimage;
		}
	}
	*/
	
	//public void setImage(Bitmap image) {
	//	this.image = image;
	//}

	public GeoPoint getNE() {
		return ne;
	}

	public void setNE(GeoPoint topRight) {
		this.ne = topRight;
	}

	public GeoPoint getSW() {
		return sw;
	}

	public void setSW(GeoPoint bottomLeft) {
		this.sw = bottomLeft;
	}

	public int getPngsize ()
	{
		return pngSize;
	}
	
	public String getOperatorId ()
	{
		return operatorId;
	}
	public int getErrorMessageResource() {
		return errorMessageResource;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//if(image != null && !image.isRecycled()) {
		if(ne != null) {
			//the first integer in the parcel indicates whether or not an image is present in the parcel
			dest.writeInt(1);
			//writeParcelable(image, image.describeContents());
			dest.writeInt(ne.getLatitudeE6());
			dest.writeInt(ne.getLongitudeE6());
			dest.writeInt(sw.getLatitudeE6());
			dest.writeInt(sw.getLongitudeE6());
			dest.writeInt(pngSize);
			dest.writeString(operatorId);
		}
		else {
			//the first integer in the parcel indicates whether or not an image is present in the parcel
			dest.writeInt(0);
		}
	}
}
/*
class CoverageResponse implements Parcelable {
	private Bitmap mImage;
	private GeoPoint mSW;
	private GeoPoint mNE;
	
	public static final Parcelable.Creator<CoverageResponse> CREATOR = new Parcelable.Creator<CoverageResponse>() {
		@Override
		public CoverageResponse createFromParcel(Parcel source) {
			return new CoverageResponse(source);
		}

		@Override
		public CoverageResponse[] newArray(int size) {
			return new CoverageResponse[size];
		}
	};
	
	public CoverageResponse(Bitmap coverageImage, GeoPoint sw, GeoPoint ne) {
		mImage = coverageImage;
		mSW = sw;
		mNE = ne;
	}
	
	public CoverageResponse(Parcel in) {
		//the first integer in the parcel indicates whether or not an image is present in the parcel
		if(in.readInt() == 1) {
			this.mImage = Bitmap.CREATOR.createFromParcel(in);

			mNE = new GeoPoint(in.readInt(), in.readInt());
			mSW = new GeoPoint(in.readInt(), in.readInt());
		}
	}

	public Bitmap getImage() {
		return mImage;
	}

	public GeoPoint getSW() {
		return mSW;
	}

	public GeoPoint getNE() {
		return mNE;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mImage != null && !mImage.isRecycled()) {
			//the first integer in the parcel indicates whether or not an image is present in the parcel
			dest.writeInt(1);
			dest.writeParcelable(mImage, mImage.describeContents());
			dest.writeInt(mNE.getLatitudeE6());
			dest.writeInt(mNE.getLongitudeE6());
			dest.writeInt(mSW.getLatitudeE6());
			dest.writeInt(mSW.getLongitudeE6());
		}
		else {
			//the first integer in the parcel indicates whether or not an image is present in the parcel
			dest.writeInt(0);
		}
	}
}
*/