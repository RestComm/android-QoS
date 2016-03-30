package com.cortxt.app.mmcui.Activities.CustomViews;

import java.sql.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;

import com.cortxt.app.mmcui.Activities.NerdScreen;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcutility.DataObjects.PhoneState;
import com.cortxt.app.mmcutility.Utils.MmcConstants;

import android.util.DisplayMetrics;

public class NerdView extends View {

	/*
	 * ========================================================
	 * Start constants
	 */
	private static final int LEFT_PADDING = 15;
	private static final int NEIGHBOURS_HEIGHT=30;
	private static final int CENTER_SPACE=25;
	private static final int RIGHT_PADDING = 10;
	private static int TOP_PADDING = 24;
	private static final int BOTTOM_PADDING = 0;
	private static final float STROKE_WIDTH = 2.2f;
	public static  float FONT_SIZE = 14.0f;

	private static final String TAG = NerdView.class.getSimpleName();
	/*
	 * End constants
	 * ========================================================
	 * Start private variables
	 */

	private int myWidth = 0;
	private int myHeight = 0;
	private float screenDensityScale;

	//private Drawable background;
	private Context context;
	private int networkType = 0, networkTier = 0;
	private String lastData = "";
	private float backgroundHeight = 175;
	private float backgroundWidth;
	private RectF neighboursBg;
	private int[] neighbors = null;
	private String lteIdentity = null, lteIdentity2 = null;
	@SuppressWarnings("unchecked")
	public HashMap<String, String>[] nerdHash = new HashMap[2];
	private static String[][] keys = {{"Time", "Network", "Data", "LTE RSSI", "RSSI", "CDMA RSSI", "LTE RSRP", "LTE RSRQ", "LTE SNR", "LTE CQI", "EC/I0", "EC/N0", "RSCP", "SNR", "BER", "RRC", "ARFCN"},{"MCC", "MNC", "SID", "NID", "BID", "LAC", "RNC", "Cell ID", "PSC", "Lat", "Lng", "Tac", "Pci", "Ci", "Band"}};

	private Paint lightGrayPaint = new Paint();
	private Paint keyPaint = new Paint(), neighPaint = new Paint();
	private boolean showNeighbors = true;
	float yspace = 16;
	boolean isLandscape = false;

	/*
	 * End private variables
	 * ========================================================
	 * Start constructors (and their helper methods)
	 */
	public NerdView (Context context) {
		super(context);
		init(context);
	}

	public NerdView (Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
		processAttributeSet(attrs);
	}

	private void init(Context context) {
		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		screenDensityScale = getContext().getResources().getDisplayMetrics().density;
		nerdHash[0] = new HashMap<String, String> ();
		nerdHash[1] = new HashMap<String, String> ();
		neighboursBg=new  RectF();
		if (screenDensityScale < 1)
			backgroundHeight = 120;
		else if (screenDensityScale == 1)
			backgroundHeight = 150;
		this.context = context;
		
		showNeighbors = true;//(dm.heightPixels > dm.widthPixels);
		//if(!showNeighbors){
		if ((dm.heightPixels <= dm.widthPixels)){
			backgroundHeight = 110;
			FONT_SIZE = 11f;
			yspace = 12;
			//TOP_PADDING = 40;
		}
		isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if(isLandscape) {
			yspace = (FONT_SIZE = 19f);
			yspace = 22;
		}
		Typeface customFont = FontsUtil.getCustomFont(MmcConstants.font_Regular, context);
		keyPaint.setStyle(Style.FILL);
		keyPaint.setShader(null);
		keyPaint.setTypeface(customFont);
		keyPaint.setStrokeWidth(STROKE_WIDTH * screenDensityScale);
		keyPaint.setTextSize(FONT_SIZE * screenDensityScale);
		keyPaint.setAntiAlias(true);
		keyPaint.setColor(0xff666666);
		neighPaint.setColor(0xff333333);
		neighPaint.setStyle(Style.FILL);
		neighPaint.setShader(null);
		neighPaint.setTypeface(customFont);
		neighPaint.setStrokeWidth(STROKE_WIDTH * screenDensityScale);
		neighPaint.setTextSize(FONT_SIZE * screenDensityScale);
		neighPaint.setAntiAlias(true);
		
		String screenColor = (this.getResources().getString(R.string.SCREEN_COLOR));
		screenColor = screenColor.length() > 0 ? screenColor : "cccccc";
		int grayColor = Integer.parseInt(screenColor,16);
		grayColor = Math.max(0, grayColor - 0x101010);
		grayColor += (0xff000000);

		lightGrayPaint.setStyle(Style.FILL);
		lightGrayPaint.setShader(null);
		lightGrayPaint.setStrokeWidth(STROKE_WIDTH * screenDensityScale);
		lightGrayPaint.setAntiAlias(true);
		//lightGrayPaint.setColor(grayColor);
		
		int chartBg = getResources().getInteger(R.integer.CUSTOM_NERD_CHART_BG);
 		if (chartBg >= 0 && chartBg <= 0xffffff) {
 			lightGrayPaint.setColor(chartBg);
 		} else {
 			lightGrayPaint.setColor(grayColor);
 		}
	}

	private void processAttributeSet(AttributeSet attrs) {

	}

	/*
	 * End constructors
	 * ========================================================
	 * Start 'view' methods (helper methods included)
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//float yspace = 16;
		//if (screenDensityScale <= 1)
		//{
		//	yspace = 14;
		//	FONT_SIZE = 14.0f;
		//}

		int i, j;
		float labelX = 0;
		float lableValue=0;
		float labelY = 0;
		float yMax = 0;

		int d = 0;

		for (i=0; i<2; i++)
		{
			labelY = (TOP_PADDING) * screenDensityScale;
			d = 0;
			for (j=0; j<keys[i].length; j++)
			{
				String val = nerdHash[i].get(keys[i][j]);
				String key = keys[i][j] + ": ";
//				if(isLandscape) {
//					key = keys[i][j] + ":    ";
//				}
				if(keys[i][j].equalsIgnoreCase("Time")){
					NerdScreen.timeText.setText(val);
				}
				else if (val != null)
				{
					if (i==0)
						labelX = LEFT_PADDING * screenDensityScale;
					else
						labelX = CENTER_SPACE * screenDensityScale + (myWidth/2)+myWidth/12;

					lableValue=(labelX+myWidth/5);
					keyPaint.setTextAlign(Paint.Align.RIGHT);
					canvas.drawText(key, lableValue, labelY, keyPaint);
					keyPaint.setTextAlign(Paint.Align.LEFT);
					canvas.drawText(val,lableValue , labelY, keyPaint);
					labelY += (yspace) * screenDensityScale;

					if (labelY > yMax)
						yMax = labelY;
					if (labelY > myHeight)
						break;
				}
			}
		}

		if (showNeighbors) {
			drawNeighbors(canvas, yMax, yspace);
		}
	}
	private void drawNeighbors(Canvas canvas, float yMax, float yspace) {
		float newyMax=yMax+5*screenDensityScale;
		neighboursBg.set((float)0,(yMax-10*screenDensityScale), myWidth,(yMax+screenDensityScale*NEIGHBOURS_HEIGHT));
        //lteIdentity = "CI: 2323134 Tac: 2231";
        //lteIdentity2 = "eNodeB: 23293 Pci: 123";
		if (neighbors != null && lteIdentity == null && neighbors.length > 1 && neighbors[0] != 0)
		{  
			canvas.drawRect(neighboursBg, lightGrayPaint);
			canvas.drawText("Neighbors: ", LEFT_PADDING * screenDensityScale, newyMax, neighPaint);
			String name = "RSCP: ";
			int spacing = 15;
			if (networkTier < 3)
			{
				spacing = 20;
				name = "RSSI: ";
			}

			canvas.drawText(name, (LEFT_PADDING+15+spacing) * screenDensityScale, newyMax+yspace*screenDensityScale, neighPaint);

			for(int i=0; i<neighbors.length; i+=2)
			{
				if (i>=12 || (networkTier < 3 && i >= 10))
					break;

				canvas.drawText(Integer.toString(neighbors[i]), (LEFT_PADDING+72+spacing*i) * screenDensityScale, newyMax, neighPaint);
				canvas.drawText(Integer.toString(neighbors[i+1]), (LEFT_PADDING+72+spacing*i) * screenDensityScale, newyMax+screenDensityScale*yspace, neighPaint);
			}
		}
		else if (lteIdentity != null)
		{
			canvas.drawRect(neighboursBg, lightGrayPaint);
            if (isLandscape)
            {
                if (lteIdentity2 != null) {
                    lteIdentity = lteIdentity + " " + lteIdentity2;
                    lteIdentity2 = null;
                }
                newyMax += 3*screenDensityScale;
            }
			canvas.drawText(lteIdentity, LEFT_PADDING * screenDensityScale, newyMax, neighPaint);

            if (lteIdentity2 != null)
			    canvas.drawText(lteIdentity2, LEFT_PADDING * screenDensityScale, newyMax+screenDensityScale*yspace, neighPaint);
		}
	}

	private float rightAlignText(float startPosition,String key){
		float factor=(float) 7.0;
		int length=key.length();
		if(key.contains("Cell")){
			factor=(float) 5.9; 
		}
		return (float) (startPosition-(factor*length*screenDensityScale));

	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		if(isLandscape) {
//			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//			return;
//		}
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		myHeight = h - (int)((TOP_PADDING + BOTTOM_PADDING) * screenDensityScale);
		//myWidth = w - (int)((LEFT_PADDING + RIGHT_PADDING) * screenDensityScale);
		myWidth=w;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private int measureHeightO(int heightMeasureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			//get the size of the image and the paddings
			result = (int) (backgroundHeight* screenDensityScale);
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}

		return result;
	}
	private int measureHeight (int heightMeasureSpec){
		int measuredHeight = (int)(backgroundHeight* screenDensityScale);
		return measuredHeight;
	}
	
	private int measureWidth(int widthMeasureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			//get the size of the image
			result = (int) backgroundWidth;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	/*
	 * End view methods
	 * =======================================================================
	 * Start public methods
	 */
	public void setValue (int side, String name, String value){
		//basic validation
		if (value == null)
		{
			if (nerdHash[side].containsKey(name))
				nerdHash[side].remove(name);
			return;
		}
		if (side > 1)
			return;

		//update variables
		if (nerdHash[side].containsKey(name))
			nerdHash[side].put (name, value.toString());
		else
			nerdHash[side].put(name, value.toString());
		//force a redraw
		invalidate();
	}

	public String getValue(int side, String name){
		if (nerdHash == null || side > 1)
			return null;
		return this.nerdHash[side].get(name);
	}

	/*
	 * Add final parameters to the nerd table and display it
	 */
	public void nerdOut (TelephonyManager telephonyManager){

		String carrier = telephonyManager.getNetworkOperatorName();
		String mcc = "0", mnc = "0";
		if (telephonyManager.getNetworkOperator() != null && telephonyManager.getNetworkOperator().length() >= 4)
		{
			mcc = telephonyManager.getNetworkOperator().substring(0, 3);
			mnc = telephonyManager.getNetworkOperator().substring(3);
		}
		networkType = telephonyManager.getNetworkType();
		networkTier = PhoneState.getNetworkGeneration(networkType);
		String nettype = PhoneState.getNetworkName(telephonyManager.getNetworkType());
		String data = PhoneState.getNetworkName (telephonyManager.getNetworkType()) + " ";
		int dataState = telephonyManager.getDataState();
		if (dataState == TelephonyManager.DATA_CONNECTED)
		{
			String activity = getActivityName(telephonyManager.getDataActivity());
			data += activity;
		}
		else if (telephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN)
		{
			String state = getStateName(telephonyManager.getDataState());
			data += state;
		}
		
		nerdHash[0].put("Data", data);
		//int serviceState = 
		//nerdHash[0].put("Network", carrier);
		nerdHash[0].put("Carrier", carrier);

		Date date = new Date(System.currentTimeMillis());
		final String dateStr = DateFormat.getDateFormat(this.getContext()).format(date);
		final String timeStr = dateStr + "  " + DateFormat.getTimeFormat(this.getContext()).format(date);

		nerdHash[0].put("Time", timeStr);
		
		nerdHash[1].put("MCC", mcc);
		nerdHash[1].put("MNC", mnc);
		//force a redraw
		invalidate();
	}

//	public String getStateName (int state)
//	{
//		switch (state)
//		{
//		case TelephonyManager.DATA_ACTIVITY_DORMANT:
//			return context.getString(R.string.LiveStatus_dormant); 
//		case TelephonyManager.DATA_ACTIVITY_IN:
//			return context.getString(R.string.LiveStatus_receiving);// "receiving"; 
//		case TelephonyManager.DATA_ACTIVITY_OUT:
//			return context.getString(R.string.LiveStatus_sending); // "sending";
//		case TelephonyManager.DATA_ACTIVITY_INOUT:
//			return context.getString(R.string.LiveStatus_sendrecv); // "send/receive"; 
//		case TelephonyManager.DATA_ACTIVITY_NONE:
//			return context.getString(R.string.LiveStatus_noactivty); // "no activity";
//		}
//		return context.getString(R.string.GenericText_Unknown);
//	}
	
	public String getStateName (int state)
	{
		switch (state)
		{
		case TelephonyManager.DATA_CONNECTED:
			return "conn"; 
		case TelephonyManager.DATA_CONNECTING:
			return "connecting"; 
		case TelephonyManager.DATA_DISCONNECTED:
			return "disconnect"; 
		case TelephonyManager.DATA_SUSPENDED:
			return "suspended"; 
		}
		return "-";
	}
	public String getActivityName (int activity)
	{
		switch (activity)
		{
		case TelephonyManager.DATA_ACTIVITY_DORMANT:
			return context.getString(R.string.LiveStatus_dormant); 
		case TelephonyManager.DATA_ACTIVITY_IN:
			return context.getString(R.string.LiveStatus_receiving);
		case TelephonyManager.DATA_ACTIVITY_OUT:
			return context.getString(R.string.LiveStatus_sending);
		case TelephonyManager.DATA_ACTIVITY_INOUT:
			return context.getString(R.string.LiveStatus_sendrecv);
		case TelephonyManager.DATA_ACTIVITY_NONE:
			return context.getString(R.string.LiveStatus_noactivty);
		
		}
		return "U";
	}
	
	public String getServiceStateName (int state)
	{
		String name = "";
		if (state >= 10)
		{
			name = "roam ";
			if (state == 10)
				return name;
			else
				state = state - 10;
		}
		switch (state)
		{
		case ServiceState.STATE_OUT_OF_SERVICE:
			name += "(no svc)"; break;
		case ServiceState.STATE_EMERGENCY_ONLY:
			name += "(911 only)"; break;
		case PhoneState.SERVICE_STATE_AIRPLANE:
			name += "(airplane)"; break;
		case ServiceState.STATE_IN_SERVICE:
			name += "(in svc)"; break;
		case ServiceState.STATE_POWER_OFF:
			name += "(power off)"; break;
		}
		return name;
	}
	
	

	public void setNeighbors (int[] list)
	{
		if (list != null && list.length > 0 && list[0] != 0){
			lteIdentity = null;
			lteIdentity2 = null;
			neighbors = list;
			invalidate();
		}
		else
		{
			neighbors = null;
			invalidate();
		}
	}
	public void setLTEIdentity (String lte)
	{
		lteIdentity = lte;
		if (lte != null)
		{
			int p = lte.indexOf("eNB");
			if (p > 0)
			{
				lteIdentity = lte.substring(0, p);
				lteIdentity2 = lte.substring(p);
			}
		}
		//neighbors = null;
		invalidate();
	}


	public void update(TelephonyManager telephonyManager, String connectString) {
		// TODO Auto-generated method stub
		int dataactivity = telephonyManager.getDataActivity();
		int datastate = telephonyManager.getDataState();
		
		String carrier = telephonyManager.getNetworkOperatorName();
		
		String data = PhoneState.getNetworkName (telephonyManager.getNetworkType()) + " ";
		int dataState = telephonyManager.getDataState();
		if (dataState == TelephonyManager.DATA_CONNECTED)
		{
			String activity = getActivityName(telephonyManager.getDataActivity());
			data += activity;
		}
		else if (telephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN)
		{
			String state = getStateName(telephonyManager.getDataState());
			data += state;
		}
		if (connectString != null && connectString.length() > 0)
		{
			int serviceState = -1;
			String[] parts = connectString.split(",");
			String service = "";
			if (parts.length > 4)
			{
				try{
					serviceState = Integer.parseInt(parts[4]);
					service = getServiceStateName (serviceState);
					if (serviceState != ServiceState.STATE_IN_SERVICE)
						carrier += " " + service;
					else
					{
						if (carrier == null || carrier.length() == 0)
							carrier = service;
					}
					nerdHash[0].put("Network", carrier);
					this.postInvalidate();
				} 
				catch (Exception e) {}
			}
			
		}
		if (data != lastData)
		{
			nerdHash[0].put("Data", data);
			this.postInvalidate();
		}
		lastData = data;
		//dataactivity = data;
	}

	/*
	 * End public methods
	 * =======================================================================
	 * 
	 */
}
