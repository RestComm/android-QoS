package com.cortxt.app.mmcui.Activities.CustomViews.Chart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.cortxt.app.mmcui.Activities.EventDetail;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.LiveBuffer;
import com.cortxt.app.mmcutility.Utils.TimeDataPoint;
import com.cortxt.app.mmcutility.Utils.TimeSeries;

public class Chart extends View {
	/*
	 * ==========================================================
	 * Start private variables
	 */
	
	/**
	 * Distance between the left edge of the chart and the left edge of
	 * the container.
	 */
	private static final int CHART_LEFT_PADDING = 12;
	/**
	 * Distance between the right edge of the chart and the right edge of
	 * the container.
	 */
	private static final int CHART_RIGHT_PADDING = 12;
	/**
	 * Distance between the top edge of the chart and the top edge of
	 * the container.
	 * The extra space between these 2 is used for the icons.
	 */
	private static final int CHART_TOP_PADDING = 6;//35;
	/**
	 * Distance between the bottom edge of the chart and the bottom edge of
	 * the container.
	 * The extra space between these 2 is used for the time-axis labels.
	 */
	private static final int CHART_BOTTOM_PADDING = 20;//16;
	/**
	 * This is the distance between the right edge of the charting area
	 * (after taking padding into account) and the y-axis.
	 */
	private static final int YAXIS_RIGHT_OFFSET = 3;
	/**
	 * This is the distance between the x axis and the time labels
	 */
	private static final int XAXIS_LABEL_YOFFSET = 14;
	/**
	 * This is the distance by which all the labels on the x axis are moved
	 * left.
	 */
	private static final int XAXIS_LABEL_XOFFSET = 8;
	/**
	 * The last label in the x axis is the "now" keyword. This is 
	 * a larger word than the rest and thus requires an additional offset
	 * to fit into the screen
	 */
	private static final int XAXIS_NOWLABEL_XOFFSET = 3;
	/**
	 * This is the offset that would be added to the y position of the
	 * event marker icons.
	 */
	private static final int EVENT_MARKER_YOFFSET = 0;
	/**
	 * The minimum height that the chart (excluding the paddings)
	 * can have.
	 */
	private static final int CHART_MIN_HEIGHT = 65;//67;
	/**
	 * The minimum width that the chart (excluding the paddings)
	 * can have.
	 */
	private static final int CHART_MIN_WIDTH = 200;
	/**
	 * The maximum height that the whole view can have
	 */
	private static final int CHART_MAX_HEIGHT = 140;
	/**
	 * The maximum width that the whole view can have
	 * if the view is in portrait mode.
	 */
	private static final int CHART_MAX_WIDTH_PORTRAIT = 267;
	/**
	 * The maximum width that the whole view can have
	 * if the view is in landscape mode.
	 */
	private static final int CHART_MAX_WIDTH_LANDSCAPE = 400;
	/**
	 * Stroke width of the x axis.
	 */
	private static final float XAXIS_STROKE_WIDTH = 1.67f;//2.67f;
	/**
	 * Stroke width of the Y-axis
	 */
	private static final float YAXIS_STROKE_WIDTH = 2.0f;
	/**
	 * Stroke width of the grid lines.
	 */
	private static final float GRID_LINE_STROKE_WIDTH = 1.3f;
	/**
	 * Stroke width of the time series on the chart.
	 */
	private static final float CHART_SERIES_STROKE_WIDTH = 1.3f;
	/**
	 * Stroke width for the lines that drop from the event markers to the x axis.
	 */
	private static final float MARKER_LINE_STROKE_WIDTH = 1.3f;
	/**
	 * Number of grid lines parallel to the x axis excluding the 
	 * x axis itself.
	 */
	private static final int XAXIS_GRID_LINE_COUNT = 8;
	/**
	 * This is the number of time labels on the x axis
	 */
	private static final int TIME_LABEL_COUNT = 6;
	/**
	 * This is the minimum value that the signalTrendChart can allow.
	 */
	public static final float SIGNAL_TREND_CHART_MIN_VALUE = -120.0f;
	/**
	 * This is the maximum value that the signalTrendChart can allow.
	 */
	public static final float SIGNAL_TREND_CHART_MAX_VALUE = -40.0f;
	
	private static final int MINOR_CELL_HEIGHT = 6;
	private static final int MAJOR_CELL_HEIGHT = 8;
	
	public static final float CHART_XAXIS_FONT_SIZE = 15.0f;
	/**
	 * Tag for debugging
	 */
	private static final String TAG = Chart.class.getSimpleName();
	/**
	 * Width of the chart
	 */
	private int chartWidth;
	/**
	 * Height of the chart
	 */
	private int chartHeight;
	/**
	 * This is the timespan (in seconds) that the chart is supposed to cover.
	 * By default, it is 4*60 seconds.
	 */
	private int chartTimespan;
	/*
	 * This is the number of seconds in the past to begin on the right side of the chart, typically 0s
	 */
	private int chartTime;
	
	/**
	 * This is the scale with which all lengths get multiplied by to make sure the charts work
	 * on all kinds of screen resolutions.
	 */
	private float screenDensityScale;

	//other variables
	Paint paint;	//This is the multi-purpose paint object used by all the methods.
					//Since this class does all of its drawing serially, it should
					//not cause any threading issues and can be used without any
					//form of locking.
	Time time = new Time(Time.getCurrentTimezone());		//This is a general time variable that will be constantly "setToNow"
															//and then copied to a long variable
	public LiveBuffer buffer = null;
	// Colors for cell handovers
	//							Black						Blue							Violet						Yellow							Cyan						Gray						Pink						White
	int[] colors = { Color.rgb(0x00, 0x00, 0x00), Color.rgb(0x80, 0x80, 0xFF), Color.rgb(0xC0, 0x00, 0xFF), Color.rgb(0xFF, 0xFF, 0x00), Color.rgb(0x00, 0xFF, 0xFF), Color.rgb(0xC0, 0xC0, 0xC0), Color.rgb(0xFF, 0xC0, 0xC0) , Color.rgb(0xFF, 0xFF, 0xFF)  };
	// Dark end of gradients for the 3 bars
	int[] colors2 = { Color.rgb(0x80, 0x80, 0x80), Color.rgb(0x00, 0x00, 0xFF), Color.rgb(0x80, 0x00, 0xC0), Color.rgb(0xC0, 0xC0, 0x00), Color.rgb(0x00, 0xC0, 0xC0), Color.rgb(0x80, 0x00, 0xD0), Color.rgb(0xC0, 0x80, 0x80) , Color.rgb(0xC0, 0xC0, 0xC0)  };
	// Bright gradients for sognal strength
	LinearGradient shader = null;
	// Faded gradients for unknown signal strength
	LinearGradient shaderfaded = null;
	
	private int fgColor = 0xffffffff;
	/*
	 * End private variables
	 * ==========================================================
	 * Start constructors
	 */
	public Chart(Context context) {
		super(context);
		
		init();
	}
	
	public Chart(Context context, AttributeSet attrs){
		super(context, attrs);
		
		init();
	}
	
	private void init(){
		screenDensityScale = getContext().getResources().getDisplayMetrics().density;
		paint = new Paint();
		chartTimespan = 5*60;	//by default, the chart covers 4*60 seconds of time
		
		int fgc = getResources().getInteger(R.integer.CUSTOM_NERD_CHART_FG);
		if(fgc>=0 && fgc<=0xffffff){
			fgColor = fgc + 0xff000000;
		}
		
		time.setToNow();
	}
	private void createShaders()
	{
		// Bright gradients for sognal strength
		shader = new LinearGradient(
				0, 
				CHART_TOP_PADDING * screenDensityScale, 
				0, 
				CHART_TOP_PADDING * screenDensityScale + chartHeight, 
				new int[]{ Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.rgb(100,255,0), Color.rgb(200,255,0), Color.rgb(255,200,0), Color.rgb(255,100,0), Color.RED }, 
				null, 
				Shader.TileMode.MIRROR
			);
		// Faded gradients for unknown sognal strength
		shaderfaded = new LinearGradient(
				0, 
				CHART_TOP_PADDING * screenDensityScale, 
				0, 
				CHART_TOP_PADDING * screenDensityScale + chartHeight, 
				new int[]{ Color.argb(160,0,255,0), Color.argb(160,0,255,0), Color.argb(160,0,255,0), Color.argb(160,0,255,0), Color.argb(160,100,255,0), Color.argb(160,200,255,0), Color.argb(160,255,200,0), Color.argb(192,255,100,0), Color.RED }, 
				null, 
				Shader.TileMode.MIRROR
			);
	}
	
	public void setParent (Activity activity)
	{
		buffer = new LiveBuffer (activity, this);
	}

	/*
	 * End Constructors
	 * ===========================================================
	 * Start over-ridden methods (from view)
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		paint.setAntiAlias(true);
		if(!buffer.signalTimeSeries.isEmpty())
			drawTimeSeries(canvas, buffer.signalTimeSeries);
		
		drawEmptyChart(canvas);
		
		drawEventMarkers(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int calculatedWidth = measureWidth(widthMeasureSpec);
		int calculatedHeight = measureHeight(heightMeasureSpec);
		setMeasuredDimension(calculatedWidth, calculatedHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float height=getContext().getResources().getDisplayMetrics().heightPixels;
		if(height<800){
			h*=(height/800);
		}
		chartHeight = Math.max(h - (int)((CHART_TOP_PADDING + CHART_BOTTOM_PADDING) * screenDensityScale), (int)(CHART_MIN_HEIGHT * screenDensityScale));
		chartWidth = Math.max(w - (int)((CHART_LEFT_PADDING + CHART_RIGHT_PADDING) * screenDensityScale), (int)(CHART_MIN_WIDTH * screenDensityScale));
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	/*
	 * End over-ridden methods
	 * ========================================================
	 * Start helper methods
	 */
	
	/**
	 * This method draws the empty chart. This consists of 
	 * drawing the chart axes, the labels on the axes and the 
	 * background grid.
	 * @param canvas
	 */
	private void drawEmptyChart(Canvas canvas){
		//draw the time axis
		paint.setColor(Color.GRAY);
		//paint.setPathEffect(new DashPathEffect(new float[]{ 5.0f, 5.0f }, 0.0f));
		paint.setStrokeWidth(XAXIS_STROKE_WIDTH * screenDensityScale);
		paint.setStyle(Style.FILL);
		paint.setShader(null);
		canvas.drawLine(
				CHART_LEFT_PADDING * screenDensityScale, 
				chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale, 
				CHART_LEFT_PADDING * screenDensityScale + chartWidth, 
				chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale, 
				paint);
		
		//draw the grid lines parallel to the X-axis
		paint.setStrokeWidth(GRID_LINE_STROKE_WIDTH * screenDensityScale);
		float gridLineOffset = chartHeight / (XAXIS_GRID_LINE_COUNT );	//distance between 2 consecutive grid lines
		for(int counter = 0; counter < XAXIS_GRID_LINE_COUNT; counter++){
			canvas.drawLine(
					CHART_LEFT_PADDING * screenDensityScale, 
					CHART_TOP_PADDING * screenDensityScale + ((counter+1)*gridLineOffset), 
					CHART_LEFT_PADDING * screenDensityScale + chartWidth, 
					CHART_TOP_PADDING * screenDensityScale + ((counter+1)*gridLineOffset), 
					paint);
		}
		
		//draw the Y-axis
		paint.setStrokeWidth(YAXIS_STROKE_WIDTH * screenDensityScale);
		paint.setPathEffect(null);
		paint.setColor(getResources().getColor(R.color.solid_blue_MMC));
		canvas.drawLine(
				chartWidth + (CHART_LEFT_PADDING - YAXIS_RIGHT_OFFSET) * screenDensityScale, 
				CHART_TOP_PADDING * screenDensityScale, 
				chartWidth + (CHART_LEFT_PADDING - YAXIS_RIGHT_OFFSET) * screenDensityScale, 
				CHART_TOP_PADDING * screenDensityScale + chartHeight, 
				paint);
		
		//draw the labels on the time axis
		paint.setColor(fgColor);
		paint.setTextSize(CHART_XAXIS_FONT_SIZE * screenDensityScale);
		float distanceBtwTimeLabels = chartWidth / (TIME_LABEL_COUNT - 1);
		float timeLabelXCoord = (CHART_LEFT_PADDING - XAXIS_LABEL_XOFFSET) * screenDensityScale;
		float timeLabelYCoord = chartHeight + (CHART_TOP_PADDING + XAXIS_LABEL_YOFFSET) * screenDensityScale;
		int timeLabel = chartTimespan + getChartTime();
		for (int counter = 0; counter < TIME_LABEL_COUNT; counter ++){
			if (chartTimespan < 300)
				canvas.drawText(String.format("%ds", -timeLabel), timeLabelXCoord, timeLabelYCoord, paint);
			else
				canvas.drawText(String.format("%dm", -timeLabel/60), timeLabelXCoord, timeLabelYCoord, paint);
			timeLabel -= chartTimespan / (TIME_LABEL_COUNT - 1);
			timeLabelXCoord += distanceBtwTimeLabels;
		}
		//canvas.drawText("0", timeLabelXCoord - XAXIS_NOWLABEL_XOFFSET * screenDensityScale, timeLabelYCoord, paint);
	}
	
	private int measureHeight (int heightMeasureSpec){
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);
		int measuredHeight = Math.max(specSize, (int)((CHART_MIN_HEIGHT + CHART_BOTTOM_PADDING + CHART_TOP_PADDING) * screenDensityScale));
		measuredHeight = Math.min(measuredHeight, (int)(CHART_MAX_HEIGHT * screenDensityScale));
		return measuredHeight;
	}
	
	private int measureHeight0(int heightMeasureSpec) {
        int result = 0;
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);
		
		if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            //get the size of the image and the paddings
        	result = (int) CHART_MAX_HEIGHT;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
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
        	result = (int) CHART_MAX_WIDTH_PORTRAIT;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
	}
	
	private int measureWidthO (int widthMeasureSpec){
		int measuredWidth = Math.max(widthMeasureSpec, (int)(CHART_MIN_WIDTH + CHART_LEFT_PADDING + CHART_RIGHT_PADDING));
		measuredWidth = Math.min(measuredWidth, (int)(CHART_MAX_WIDTH_PORTRAIT * screenDensityScale));
		return measuredWidth;
	}
	
	/**
	 * This method takes the points stored in the signalTimeSeries variable and
	 * draws a series on the canvas.
	 * @param canvas
	 * @param timeSeries
	 */
	private void drawTimeSeries(Canvas canvas, TimeSeries<Float> timeSeries){
		float xcoord = 0.0f;
		float ycoord = 0.0f;
		Path path = new Path();
		
		if (shader == null)
			createShaders ();
		//set paint parameters
		paint.setShader(shader);
		paint.setStrokeWidth(CHART_SERIES_STROKE_WIDTH * screenDensityScale);
		
		//make the initial translation to the starting point of the path (on the y axis).
		xcoord = chartWidth + (int)((CHART_LEFT_PADDING - YAXIS_RIGHT_OFFSET) * screenDensityScale);
		xcoord = Math.min(xcoord,getXCoordByTimestamp(System.currentTimeMillis()));
		float xcoord0 = xcoord;
		float ycoord0 = 0;
		Float data = (Float)timeSeries.get(0).getData();
		Float data2 = (Float)timeSeries.get(0).getData2();
		if ((data2 >= -140 && data2 <= -20) || data2 == -256)
			data = data2;
		if (data == 0)  // main screen turn off?
		{
			// need to find previous y coordinate of data or data2 by looking forward 1 or 2 in the list
			// so that gray area can be drawn
			if (timeSeries.getDataPointCount() > 1 && (Float)timeSeries.get(1).getData() != 0)
				if ((Float)timeSeries.get(1).getData2() <= -20 && (Float)timeSeries.get(1).getData2() >= -140)
					ycoord0 =  getYCoordByData((Float)timeSeries.get(1).getData2(), -40, -120);
				else
					ycoord0 =  getYCoordByData((Float)timeSeries.get(1).getData(), -40, -120);
			else if (timeSeries.getDataPointCount() > 2 && (Float)timeSeries.get(2).getData() != 0)
				if ((Float)timeSeries.get(2).getData2() <= -20 && (Float)timeSeries.get(2).getData2() >= -140)
					ycoord0 =  getYCoordByData((Float)timeSeries.get(2).getData2(), -40, -120);
				else
					ycoord0 =  getYCoordByData((Float)timeSeries.get(2).getData(), -40, -120);
		}
		ycoord = getYCoordByData((Float)data, -40, -120);
		path.moveTo(xcoord, ycoord);
		
		float startx = xcoord0;
		TimeDataPoint<Float> datapoint0 = null;
		int d=0;
		//draw the series according to the data points in timeSeries
		for (TimeDataPoint<Float> datapoint : timeSeries) {
			data = (Float)datapoint.getData();
			data2 = (Float)datapoint.getData2();
			if ((data2 >= -140 && data2 <= -20) || data2 == -256)
				data = data2;
			
			ycoord = getYCoordByData(data, -40, -120);
			
			if (getXCoordByTimestamp(datapoint.getTimestamp()+2000) < xcoord && data != 0)
			{
				xcoord = getXCoordByTimestamp(datapoint.getTimestamp()+2000);
				//path.lineTo(xcoord, ycoord);
				path.lineTo(xcoord0 - 2*screenDensityScale, ycoord);
			}
			
			xcoord = getXCoordByTimestamp(datapoint.getTimestamp());
			// Draw gray area where signal is unknown for > 30 seconds
			if (data == 0) // (datapoint0 != null && datapoint0.getTimestamp() - 60000 > datapoint.getTimestamp())
			{
				if (d == 0 || (d>0 && timeSeries.get(d-1).getTimestamp() - datapoint.getTimestamp() > 30000))
				{
					//close the path loop now
					path.lineTo(xcoord0, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
					path.lineTo(startx, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
					path.close();
					canvas.drawPath(path, paint);
					
					// draw gray polygon for signal unknown duration
					if (ycoord0 != 0)
					{
						path = new Path();
						path.moveTo(xcoord0, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
						path.lineTo(xcoord0, ycoord0);
						path.lineTo(xcoord,ycoord0);
						path.lineTo(xcoord, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
						
						path.close();
						paint.setShader(shaderfaded);
						canvas.drawPath(path, paint);
						
						paint.setShader(shader);
					}
					startx = xcoord;
					path = new Path();
					path.moveTo(xcoord, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
					if (ycoord0 != 0)
						path.lineTo(xcoord, ycoord0);
				}
			}
			if (xcoord > startx)
				xcoord = startx;
			if (data != 0)
			{
				path.lineTo(xcoord, ycoord);
				ycoord0 = ycoord;
			}
			xcoord0 = xcoord;
			datapoint0 = datapoint;
			d++;
		}
		
		//close the path loop now
		path.lineTo(xcoord, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale);
		path.lineTo(startx, chartHeight + (CHART_TOP_PADDING - XAXIS_STROKE_WIDTH) * screenDensityScale
		);
		path.close();
		
		//now draw the path onto the canvas
		canvas.drawPath(path, paint);
	}
	
	/**
	 * This method takes the events stored in the eventTimeSeries variable
	 * and draws the appropriate markers on the chart.
	 * @param canvas
	 */
	private void drawEventMarkers(Canvas canvas){
		//initialize paint parameters
		paint.setStrokeWidth(0);
		paint.setColor(fgColor);
		
		//declare the variables that will get re-used
		float xcoord = 0.0f, ycoord = 0.0f;
		Bitmap eventMarkerBitmap = null;
		
		//loop over eventTimeSeries and draw
		for (TimeDataPoint<EventType> eventDP : buffer.eventTimeSeries){
			//get the bitmap for the event
			if(eventDP.getData().getNerdImageResource() == 0){
				//this particular event doesn't have an image associated with it
				continue;
			}
			eventMarkerBitmap = BitmapFactory.decodeResource(
					getResources(), 
					eventDP.getData().getNerdImageResource()  //flag
			);
			if (eventMarkerBitmap == null)
				continue;
			//get the coordinates for the bitmap
			xcoord = getXCoordByTimestamp(eventDP.getTimestamp());
			ycoord = (CHART_TOP_PADDING + EVENT_MARKER_YOFFSET) * screenDensityScale;
			
			//draw the marker line onto the canvas -- vertical line from event icon down to x-axis 
			canvas.drawLine(xcoord, ycoord, xcoord, CHART_TOP_PADDING * screenDensityScale + chartHeight, paint);
			
			//adjust xcoord for the width of the icon and draw the icon onto the canvas
			xcoord -= (eventMarkerBitmap.getWidth() / 2);
			canvas.drawBitmap(eventMarkerBitmap, xcoord, ycoord, paint);
		}
		
		if (buffer.cellTimeSeries.getDataPointCount() == 0)
			return;
		int lastHigh = 0;
		int lastLow = 0;
		int[] cols = new int[buffer.cellTimeSeries.getDataPointCount()];
		int cells = buffer.cellTimeSeries.getDataPointCount();
		cols[cells-1] = 0;
		int colmax = 0;
		TimeDataPoint<Integer> cellDP = null;
		for (int c=cells-2; c>=0; c--)
		{
			cols[c] = -1;
			for (int d=cells-1; d> c; d--)
				if (buffer.cellTimeSeries.get(d).getData2().equals(buffer.cellTimeSeries.get(c).getData2()))
				{
					cols[c] = cols[d];
					break;
				}
			if (cols[c] == -1)
				cols[c] = ++colmax;
		}
		
		paint.setStrokeWidth(MARKER_LINE_STROKE_WIDTH * screenDensityScale);
		paint.setColor(Color.DKGRAY);
		//loop over cellTimeSeries and draw
		//for (TimeDataPoint<Integer> cellDP : buffer.cellTimeSeries)
		//for (int c=0; c<cells; c++)
		for (int c=cells-1; c>=0; c--) //draw cell handoffs
		{
			cellDP = buffer.cellTimeSeries.get(c);
			//get the bitmap for the event
			if(cellDP.getData() <= 0){
				//this particular event doesn't have an image associated with it
				continue;
			}
			
			//get the coordinates for the bitmap
			xcoord = getXCoordByTimestamp(cellDP.getTimestamp());
			ycoord = chartHeight + (CHART_TOP_PADDING - 2) * screenDensityScale;
			int sz = 0;  // size of marker, small=0 for cell changes, big=1 for LAC changes
			float[] s4 = {6f * screenDensityScale,8f * screenDensityScale}, s6 = {4f * screenDensityScale, 6f * screenDensityScale}, s2 = {3f * screenDensityScale, 4f * screenDensityScale};
			
			if ((int)cellDP.getData2() != lastLow || (int)cellDP.getData() != lastHigh)
			{
				if ((int)cellDP.getData() != lastHigh && lastHigh != 0)
					sz = 1;
				else
					sz = 0;
			
				paint.setColor(colors[cols[c]%8]);
				//draw the marker line onto the canvas
				for (int j=0; j<2; j++)
				{
					canvas.drawLine(xcoord+j, ycoord+j, xcoord+j, ycoord+j-s6[sz], paint);
					canvas.drawLine(xcoord+j, ycoord+j-s6[sz], xcoord+j - s2[sz], ycoord+j-s6[sz] - s4[sz], paint);
					canvas.drawLine(xcoord+j - s2[sz], ycoord+j-s6[sz] - s4[sz], xcoord+j + s2[sz], ycoord+j-s6[sz] - s4[sz], paint);
					canvas.drawLine(xcoord+j + s2[sz], ycoord+j-s6[sz] - s4[sz], xcoord+j, ycoord+j-s6[sz], paint);
					paint.setColor(colors2[cols[c]%8]);
				}
				
			}
			lastHigh = cellDP.getData();
			lastLow = cellDP.getData2();
			//n++;
		}
	}
	
	/**
	 * For charting, one routinely needs to find an appropriate x coordinate for 
	 * a given timestamp. This method does that conversion.
	 * It is assumed that the timestamp returns true using isTimestampPlottable.
	 * @param timestamp
	 * @return
	 */
	private float getXCoordByTimestamp(long timestamp){
		time.setToNow();
		long now = time.toMillis(true);
		
		long chartTimespanInMillis = chartTimespan * 1000;
		if (chartTimespanInMillis < 1000)
			chartTimespanInMillis = 1000;
		float xCoord = CHART_LEFT_PADDING * screenDensityScale + ((timestamp - now + chartTimespanInMillis + getChartTime()*1000)*chartWidth/(chartTimespanInMillis));
		xCoord -= (YAXIS_RIGHT_OFFSET * screenDensityScale);
		return xCoord;
	}
	
	/**
	 * For charting, one routinely needs to find an appropriate y coordinate for
	 * a given data value. This method does that conversion. 
	 * This conversion uses the maximum and minimum values permitted by the chart
	 * to do the conversion. These maximum and minimum values should be pre-scaled for density.
	 * @param data
	 * @return
	 */
	private float getYCoordByData(float data ,float maxVal, float minVal){
		float yCoord = CHART_TOP_PADDING * screenDensityScale + (maxVal - data)*chartHeight/(maxVal - minVal);
		if (data <= -120)
			data = data;
		return yCoord;
	}
	
	/**
	 * Not all timestamps stored in the timeSeries are plottable. If the timestamp of a 
	 * data point is outside of the chartTimespan, then this method returns false, otherwise
	 * it returns true.
	 * @param timestamp
	 * @return
	 */
	private boolean isTimestampPlottable(long timestamp){
		time.setToNow();
		long currentTime = time.toMillis(true);
		if (currentTime - (chartTimespan + getChartTime())*1000 < timestamp && timestamp < currentTime - (getChartTime())*1000)
			return true;
		else
			return false;
	}
	
	
	/**
	 * Returns the chart timespan expressed in milliseconds.
	 * @return
	 */
	public long getChartTimespan(){
		return this.chartTimespan * 1000;
	}
	
	
   // We can be in one of these 3 states
   static final int NONE = 0;
   static final int PRESS = 1;
   static final int DRAG = 2;
   static final int ZOOM = 3;
   int mode = NONE;

   // Remember some things for zooming
   PointF start = new PointF();
   PointF mid = new PointF();
   int startTimespan = chartTimespan;
   int startTime = getChartTime();
   float oldDist = 1f;
   /** Determine the space between the first two fingers */
   private float spacing(MotionEvent event) {
      float x = event.getX(0) - event.getX(1);
      float y = 0; // event.getY(0) - event.getY(1);
      return FloatMath.sqrt(x * x + y * y);
   }

   /** Calculate the mid point of the first two fingers */
   private void midPoint(PointF point, MotionEvent event) {
      float x = event.getX(0) + event.getX(1);
      float y = event.getY(0) + event.getY(1);
      point.set(x / 2, y / 2);
   }

   /** Show an event in the LogCat view, for debugging */
   private void dumpEvent(MotionEvent event) {
      String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
            "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append("event ACTION_").append(names[actionCode]);
      if (actionCode == MotionEvent.ACTION_POINTER_DOWN
            || actionCode == MotionEvent.ACTION_POINTER_UP) {
         sb.append("(pid ").append(
               action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
         sb.append(")");
      }
      sb.append("[");
      for (int i = 0; i < event.getPointerCount(); i++) {
         sb.append("#").append(i);
         sb.append("(pid ").append(event.getPointerId(i));
         sb.append(")=").append((int) event.getX(i));
         sb.append(",").append((int) event.getY(i));
         if (i + 1 < event.getPointerCount())
            sb.append(";");
      }
      sb.append("]");
      Log.d(TAG, sb.toString());
   }
   @Override
   public boolean onTouchEvent(MotionEvent event) {
	      // Dump touch event to log
	      dumpEvent(event);

	      // Handle touch events here...
	      switch (event.getAction() & MotionEvent.ACTION_MASK) {
	      case MotionEvent.ACTION_DOWN:
	         //savedMatrix.set(matrix);
	         start.set(event.getX(), event.getY());
	         startTime = getChartTime();
	         Log.d(TAG, "mode=DRAG");
	         mode = PRESS;
	         break;
	      case MotionEvent.ACTION_POINTER_DOWN:
	         oldDist = spacing(event);
	         startTimespan = chartTimespan;
	         startTime = getChartTime();
	         Log.d(TAG, "oldDist=" + oldDist);
	         if (oldDist > 10f) {
	            //savedMatrix.set(matrix);
	            midPoint(mid, event);
	            mode = ZOOM;
	            Log.d(TAG, "mode=ZOOM");
	         }
	         break;
	      case MotionEvent.ACTION_UP:
	    	  if (mode == DRAG)
	    	  {
	    		  float time = (startTime + (event.getX()-start.x) * (chartTimespan) / chartWidth);
	    		  if (time < 0)
	    			  time = 0;
	    		  setChartTime((int)Math.round(time/(chartTimespan/5)) * (chartTimespan/5));
	    		  buffer.updateActivityFromDB((getChartTime() + chartTimespan*3) * 1000);
	    	  }
	    	  if (Math.abs(start.x - event.getX()) < 10)  // small drag or simple press? check to popup event
	    	  {
	    		  start.y = start.y / screenDensityScale;
	    		  start.x = start.x / screenDensityScale;
	    		  float xcoord, ycoord, xdist, xdistmin = 20;
    			  
	    		  // hit test events
	    		  if (start.y > CHART_TOP_PADDING-10 && start.y < CHART_TOP_PADDING + 40)
	    		  {
	    			  //loop over eventTimeSeries and draw
	    			  TimeDataPoint<EventType> bestEvent = null;
	    			  for (TimeDataPoint<EventType> eventDP : buffer.eventTimeSeries){
	    				//get the coordinates for the event
	    				xcoord = getXCoordByTimestamp(eventDP.getTimestamp());
	    				xdist = Math.abs(xcoord-start.x*screenDensityScale);
	    				if (xdist < xdistmin)
	    				{
	    					bestEvent = eventDP;
	    					xdistmin = xdist;
	    				}
	    				else if (bestEvent != null)
	    					break;
	    			  }
	    			  if (bestEvent != null)
	    			  {
	    				  int eventId = ReportManager.getInstance(getContext()).getEventId(bestEvent.getTimestamp(), bestEvent.getData().getIntValue());
	    				  if (eventId != 0)
	    				  {
		    				  Intent intent = new Intent (buffer.getParent(), EventDetail.class);
		    				  intent.putExtra(EventDetail.EXTRA_EVENT_ID, eventId);
		    				  buffer.getParent().startActivity(intent);
	    				  }
	    				  else
	    				  {
	    					  String cstr = bestEvent.getData().getEventString(getContext());
		    				  Toast toast = Toast.makeText(buffer.getParent(), cstr, Toast.LENGTH_SHORT);
		    				  toast.show();
	    				  }
	    			  }
	    		  }
	    		  // hit test cell handoffs
	    		  else if (start.y > chartHeight / screenDensityScale + (CHART_TOP_PADDING - 10) && start.y < chartHeight / screenDensityScale + CHART_TOP_PADDING +12)
	    		  {
	    			  TimeDataPoint<Integer> bestCell = null;
	    			  for (TimeDataPoint<Integer> cellDP : buffer.cellTimeSeries)
	    			  {
	    				//get the coordinates for the cell marker
	    				xcoord = getXCoordByTimestamp(cellDP.getTimestamp());
	    				xdist = Math.abs(xcoord-start.x*screenDensityScale);
	    				if (xdist < xdistmin)
	    				{
	    					bestCell = cellDP;
	    					xdistmin = xdist;
	    				}
	    				else if (bestCell != null)
	    					break;
	    			  }
	    			  if (bestCell != null)
	    			  {   				  
	      				  String cstr = bestCell.getData() + "\n" +
	    						  (bestCell.getData2()) +"\n" + 
	    						  (bestCell.getData3()&0xFFFF);
	    				  Toast toast = Toast.makeText(buffer.getParent(), cstr, Toast.LENGTH_LONG);
	    				  toast.show();
	    			  }
	    		  }
	    	  }
	    	  mode = NONE;
		         Log.d(TAG, "mode=NONE");
		         break;
	      case MotionEvent.ACTION_POINTER_UP:
	    	  if (mode == ZOOM)
	    	  {
	    		  float newDist = spacing(event);
	    		  if (chartTimespan < startTimespan / 3 && startTimespan >= 300)
	    			  chartTimespan = (int)(startTimespan / 4);
	    		  else if (chartTimespan < startTimespan  && startTimespan >= 150)
	    			  chartTimespan = (int)(startTimespan / 2);
	    		  else if (chartTimespan > startTimespan * 3)
	    			  chartTimespan = (int)(startTimespan * 4);
	    		  else if (chartTimespan > startTimespan)
	    			  chartTimespan = (int)(startTimespan * 2);
	    		  
	    		  buffer.updateActivityFromDB((getChartTime() + chartTimespan*3) * 1000);
	    	  }
	    	  mode = NONE;
	         Log.d(TAG, "mode=NONE");
	         break;
	      case MotionEvent.ACTION_MOVE:
	    	 if (mode== PRESS  && Math.abs(start.x - event.getX()) > 20) //and moved by 10 pixels
	    		 mode = DRAG;
	    	 else if (mode == DRAG) {
	            // ...
	            //matrix.set(savedMatrix);
	            //matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
	        	 setChartTime((int) (startTime + (event.getX()-start.x) * (chartTimespan) / chartWidth));
	             invalidate ();
	         }
	         else if (mode == ZOOM) {
	            float newDist = spacing(event);
	            Log.d(TAG, "newDist=" + newDist);
	            chartTimespan = (int)(startTimespan * oldDist / newDist);
                invalidate ();
	         }
	         break;
	      }

	      return true; // indicate event was handled
	   }

public int getChartTime() {
	return chartTime;
}

public void setChartTime(int chartTime) {
	this.chartTime = chartTime;
}
	
	
}
