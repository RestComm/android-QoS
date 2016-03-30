package com.cortxt.app.mmcui.Activities.MyCoverage.EventsOverlay;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cortxt.app.mmcui.Activities.EventDetail;
import com.cortxt.app.mmcui.Activities.MyCoverage.MMCMapView;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.LiveBuffer;
import com.cortxt.app.mmcutility.Utils.TimeDataPoint;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class EventsOverlay extends ItemizedOverlay<EventOverlayItem> implements MMCMapView.OnZoomLevelChangeListener {
	
	/**
	 * Stroke width of the ground track lines.
	 */
	private static final float TRACK_STROKE_WIDTH = 5f;
	
	private int text_offset_y;
	
	private Context mContext;
	private MapView mMapView;
	private static ArrayList<EventOverlayItem> mOverlayItemsList;
	private EventClusters eventClusters;
	private HashSet<Integer> mEventTypesToDisplay;
	public LiveBuffer buffer = null;
	private Paint paint;
	private float screenDensityScale;
	private boolean showOverlay = true;
	private boolean transit = false;
	private AsyncTask<Void, Void, List<HashMap<String, String>>> loadEventsTask = null;
	
	public static final String TAG = EventsOverlay.class.getSimpleName();
	
	public EventsOverlay(Context context, MapView mapView, HashSet<Integer> eventTypesToDisplay, ArrayList<EventOverlayItem> eventList) {
		super(ItemizedOverlay.boundCenter(context.getResources().getDrawable(R.drawable.mapicon_event_cluster)));
		mContext = context;
		mMapView = mapView;
		mEventTypesToDisplay = eventTypesToDisplay;
		mOverlayItemsList = eventList;
		

		eventClusters = new EventClusters(context, mapView);

		text_offset_y = (int)(5.0f * context.getResources().getDisplayMetrics().density);

		loadEvents();
		screenDensityScale = context.getResources().getDisplayMetrics().density;
		paint = new Paint();
		buffer = new LiveBuffer ((Activity)context, mapView);
	}
	
	@Override
	public void onZoomLevelChange() {
		regroupClusters();
	}

	
	@Override
	protected EventOverlayItem createItem(int i) {
		return mOverlayItemsList.get(i);
	}

	@Override
	public int size() {
		int count = mOverlayItemsList.size();
		return count;
	}
	
	public void setTransit(boolean transit) {
		this.transit = transit;
	}
	
	public void show (boolean bShow)
	{
		if (this.showOverlay == true && bShow == false)
		{
			Toast toast = Toast.makeText(mContext, R.string.mycoverage_hideevents, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		this.showOverlay = bShow;
		if (bShow)
			loadEvents();
		else
			eventClusters.clear();
	}
	private void loadEvents() {
		if (loadEventsTask != null) // || loadEventsTask.getStatus() == AsyncTask.Status.RUNNING)
			return;
		eventClusters.clear();

		loadEventsTask = new AsyncTask<Void, Void, List<HashMap<String, String>>>() {

			@Override
			protected List<HashMap<String, String>> doInBackground(Void... arg0) {
				List<HashMap<String, String>> events = ReportManager.getInstance(mContext).getEvents(mEventTypesToDisplay);
				return events;
			}
			
			@Override
			protected void onPostExecute(List<HashMap<String, String>> events) {
				for(HashMap<String, String> event : events) {
					if(event.containsKey(ReportManager.EventKeys.LATITUDE) &&
							event.containsKey(ReportManager.EventKeys.LONGITUDE)) {
						int eventId = Integer.parseInt(event.get(ReportManager.EventKeys.ID));
						int eventType = Integer.parseInt(event.get(ReportManager.EventKeys.TYPE));
						long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
						int latitudeE6 = (int) (Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE)) * 1000000.0);
						int longitudeE6 = (int) (Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE)) * 1000000.0);
						int markerResource = 0;
						int netchangeType = 0;
						//markerResource = EventInfo.MAP_EVENT_ICONS.get(eventType);
						markerResource = EventType.get(eventType).getMapImageResource();
						
						String title = "";

						String details = EventDetail.getEventDetails(mContext, event);
						
						if (markerResource != 0)
							eventClusters.addEventMarker(new EventMarker(eventId, latitudeE6, longitudeE6, timeStamp, eventType, markerResource, title, netchangeType, details));
//						if(Integer.parseInt(event.get(EventKeys.TYPE)) == EventType.MAN_TRANSIT.getIntValue()) {
//							MMCLogger.logToFile(MMCLogger.Level.DEBUG, TAG, "loadEvents", "Transit lat: " + latitudeE6 + ""
//									+ ", long: " + longitudeE6);
//						}
					}
				}
				
				mOverlayItemsList = eventClusters.getMarkersForOverlay();
				//setFocusedItem(-1);
				//setLastFocusedIndex(-1);
				populate();
				if (!mMapView.getOverlays().contains(EventsOverlay.this) ) // && mOverlayItemsList.size() > 0)
					mMapView.getOverlays().add(EventsOverlay.this);
				mMapView.invalidate();
				loadEventsTask = null;
				
			}
			
		}.execute((Void[])null);
	}


	public void regroupClusters() {
		eventClusters.regroupClusters();
		mOverlayItemsList = eventClusters.getMarkersForOverlay();
		//setFocusedItem (-1);
		//setLastFocusedIndex(-1);
		populate();
		mMapView.invalidate();
	}

	/**
	 * Deletes all markers that are displayed on the map or stored in memory.
	 */
	public void clear() {
		mOverlayItemsList.clear();
		eventClusters.clear();
		populate();
	}

	/**
	 * Draws the number of markers in the cluster (if it is greater than 1) on top of the icon.
	 */
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		
		if (!this.showOverlay)
			return;
		//draw numbers on icons that are clustered
		Projection projection = mapView.getProjection();
		paint.setAntiAlias(true);
		if(!transit)
		drawGroundTrack(canvas, projection);
		super.draw(canvas, mapView, false);

		paint.setColor(Color.rgb(0xFF, 0xFF, 0xFF));
		paint.setStrokeWidth(1 * screenDensityScale);
		paint.setStyle(Style.FILL);
		
		for(int i=0; i<mOverlayItemsList.size(); i++) {
			if(mOverlayItemsList.get(i).getClusterSize() > 1) {
				Point p = projection.toPixels(mOverlayItemsList.get(i).getPoint(), null);

				//Paint paint = new Paint();
				paint.setTextSize((int)(14.0f * mContext.getResources().getDisplayMetrics().scaledDensity));
				paint.setTextAlign(Paint.Align.CENTER);

				canvas.drawText(Integer.toString(mOverlayItemsList.get(i).getClusterSize()), p.x, p.y+text_offset_y, paint);
			}
		}
		if(buffer != null)
			drawCurrentEvents (canvas, projection);		
	}
	
	private void drawCurrentEvents (Canvas canvas, Projection projection)
	{
		if(buffer.locationTimeSeries == null)
			return;
		if (buffer.locationTimeSeries.getDataPointCount() < 2 || buffer.signalTimeSeries.getDataPointCount() <1)
			return;
		for (TimeDataPoint<EventType> eventDP : buffer.eventTimeSeries)
		{
			if (eventDP.getData2() == null)
			{
				int eventId = ReportManager.getInstance(buffer.getParent()).getEventId(eventDP.getTimestamp(), eventDP.getData().getIntValue());
				if (eventId > 0) 
				{
					eventDP.setData2(eventDP.getData());
					if (eventClusters.hasEvent (eventId))
						continue;
					
					HashMap<String, String> event = ReportManager.getInstance(mContext).getEventDetails(eventId);
					
					if(event.containsKey(ReportManager.EventKeys.LATITUDE) &&
							event.containsKey(ReportManager.EventKeys.LONGITUDE)) {
						int eventType = Integer.parseInt(event.get(ReportManager.EventKeys.TYPE));
						long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
						int latitudeE6 = (int) (Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE)) * 1000000.0);
						int longitudeE6 = (int) (Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE)) * 1000000.0);
						int markerResource = 0;
						int netchangeType = 0;
						//markerResource = EventInfo.MAP_EVENT_ICONS.get(eventType);
						markerResource = EventType.get(eventType).getMapImageResource();
						
						String title = "";
						String details = EventDetail.getEventDetails(mContext,event);
						
						if (markerResource != 0)
						{
							eventClusters.addEventMarker(new EventMarker(eventId, latitudeE6, longitudeE6, timeStamp, eventType, markerResource, title, netchangeType, details));
						}
					}					
				}
			}
		}
		/*
		//make the initial translation to the starting point of the path (on the y axis).
		double lat = (Double)buffer.locationTimeSeries.get(0).getData();
		double lng = (Double)buffer.locationTimeSeries.get(0).getData2();
		EventType evt = (EventType)buffer.eventTimeSeries.get(0).getData();
		int s = 0, i = 0, evts = buffer.eventTimeSeries.getDataPointCount();
		Point pp = null;
		//draw the series according to the data points in timeSeries
		for (TimeDataPoint<Double> datapoint : buffer.locationTimeSeries) {
			lat = (Double)datapoint.getData();
			lng = (Double)datapoint.getData2();
			long timestamp = datapoint.getTimestamp();
			for (i=s; i<evts; i++)
			{
				if (buffer.eventTimeSeries.get(i).getTimestamp() < timestamp)
				{
					evt = (EventType)buffer.eventTimeSeries.get(i).getData();
					s = i;
					break;
				}
			}
			Point p = projection.toPixels(new GeoPoint ((int)(lat*1000000), (int)(lng*1000000)), null);
			if (p.x < -400 || p.x > 1000 || p.y < -500 || p.y > 1500)
				continue;
			
			Bitmap eventMarkerBitmap = BitmapFactory.decodeResource(
					buffer.getParent().getResources(), evt.getMapImageResource());
			if (eventMarkerBitmap == null)
				continue;
			//adjust xcoord for the width of the icon and draw the icon onto the canvas
			p.x -= (eventMarkerBitmap.getWidth() / 2);
			p.y -= (eventMarkerBitmap.getHeight());
			canvas.drawBitmap(eventMarkerBitmap, p.x, p.y , paint);
		}
		*/
	}
	private void drawGroundTrack (Canvas canvas, Projection projection)
	{
		float xcoord = 0.0f;
		float ycoord = 0.0f;
		Path path = new Path();
//		path.onDrawCycleStart(canvas);
		paint.setStrokeWidth(TRACK_STROKE_WIDTH * screenDensityScale);
		paint.setStyle(Paint.Style.STROKE); 
		
		if (buffer.locationTimeSeries.getDataPointCount() < 2 || buffer.signalTimeSeries.getDataPointCount() <1)
			return;
		
		if (mContext.getResources().getBoolean(R.bool.GROUND_TRACK) == false) 
			return;
		//make the initial translation to the starting point of the path (on the y axis).
		double lat = (Double)buffer.locationTimeSeries.get(0).getData();
		double lng = (Double)buffer.locationTimeSeries.get(0).getData2();
		double lat0 = lat, lng0 = lng;
		int signal = (int)(float)(Float)buffer.signalTimeSeries.get(0).getData();
		int signal2 = (int)(float)(Float)buffer.signalTimeSeries.get(0).getData2();
		if ((signal2 >= -140 && signal2 <= -20) || signal2 == -256)
			signal = signal2;
		path.moveTo(xcoord, ycoord);
		double xcoord0 = xcoord;
		double startx = xcoord0;
		int s = 0, i = 0, signals = buffer.signalTimeSeries.getDataPointCount();
		int color = signalColor (signal,16);
		int prevcol = 0;
		Point pp = null;
		//draw the series according to the data points in timeSeries
		for (TimeDataPoint<Double> datapoint : buffer.locationTimeSeries) {
			lat = (Double)datapoint.getData();
			lng = (Double)datapoint.getData2();
			long timestamp = datapoint.getTimestamp();
			for (i=s; i<signals; i++)
			{
				if (buffer.signalTimeSeries.get(i).getTimestamp() < timestamp+2000)
				{
					signal = (int)(float)(Float)buffer.signalTimeSeries.get(i).getData();
					signal2 = (int)(float)(Float)buffer.signalTimeSeries.get(i).getData2();
					if ((signal2 >= -140 && signal2 <= -20) || signal2 == -256)
						signal = signal2;
					s = i;
					break;
				}
			}
			color = signalColor (signal,16);
			Point p = projection.toPixels(new GeoPoint ((int)(lat*1000000), (int)(lng*1000000)), null);
			if (pp != null && (Math.abs(lat - lat0) > 0.004 || Math.abs(lng - lng0) > 0.004))
				pp = null;
			
			if (p.x < -80 || p.x > mMapView.getWidth() + 80 || p.y < -80 || p.y > mMapView.getHeight() + 80)
			{
				pp = p;
				path.moveTo((float)pp.x, (float)pp.y);
				continue;
			}
				
			if (color != prevcol || prevcol == 0 || pp == null)
			{
				if (pp != null)
				{
					if (Math.abs(p.x - pp.x) < 30 && Math.abs(p.y - pp.y) < 30)
						path.lineTo((float)pp.x, (float)pp.y);
					//now draw the path onto the canvas
					paint.setColor(signalColor (signal,12));
					paint.setStrokeWidth(TRACK_STROKE_WIDTH * screenDensityScale);
					canvas.drawPath(path, paint);
					paint.setColor(signalColor (signal,16));
					paint.setStrokeWidth(3 * screenDensityScale);
					canvas.drawPath((Path) path, paint);
					path = new Path();
					path.moveTo((float)pp.x, (float)pp.y);
					path.lineTo((float)p.x, (float)p.y);
				}
				else
					path.moveTo((float)p.x, (float)p.y);
				//paint.setColor(color);
			}
			else
			{
				path.lineTo((float)p.x, (float)p.y);
			}
			prevcol = color;
			pp = p;
			lat0 = lat; lng0 = lng;
		}
		if (pp != null)
		{
			path.lineTo((float)pp.x, (float)pp.y);
			//now draw the path onto the canvas
			paint.setColor(signalColor (signal,12));
			paint.setStrokeWidth(TRACK_STROKE_WIDTH * screenDensityScale);
			canvas.drawPath(path, paint);
			paint.setColor(signalColor (signal,16));
			paint.setStrokeWidth(3 * screenDensityScale);
			canvas.drawPath(path, paint);
		}
	}

	public static int signalColor (int signal, int d16)
	{
		if (signal == 0)
			return Color.rgb(0, 0, 0);
		if (signal > -70)
			signal = -70;
		int green = 5 * (signal + 120);
		if (green > 255)
			green = 255;
		
		int red = 250 - 5 * (signal + 120);
		if (red < 0)
			red = 0;
		
		int diff = green - red;
		if (diff < 0)
			diff = red - green;
		diff = (250-diff)/2;
		green = green+diff;
		red = red + diff;
		if (green > 255)
			green = 255;
		if (red > 255)
			red = 255;
		green = ((green+1)/16)*d16;
		red = ((red+1)/16)*d16;
		return Color.rgb(red, green, 0);
	}
	
	@Override
	protected boolean onTap(int index) {
		EventMarkerCluster cluster = eventClusters.getClusters(index);
		if (cluster == null)
			return true;
		ArrayList<EventMarker> markers = cluster.getMarkers();
		if(markers.size() == 1) {
			EventMarker event = markers.get(0);
			Intent intent = new Intent(mContext, EventDetail.class);
			intent.putExtra(EventDetail.EXTRA_EVENT_ID, event.getEventId());
			mContext.startActivity(intent);
		}
		else {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View eventListLayout = inflater.inflate(R.layout.mycoverage_eventlist, null);
			
			ListView eventList = (ListView) eventListLayout.findViewById(R.id.mycoverage_eventlist);
			EventListAdapter adapter = new EventListAdapter(mContext, markers);
			eventList.setAdapter(adapter);
			
			eventList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int eventId = (Integer)view.getTag();
					Intent intent = new Intent(mContext, EventDetail.class);
					intent.putExtra(EventDetail.EXTRA_EVENT_ID, eventId);
					mContext.startActivity(intent);
				}
			});
			
			AlertDialog dialog = new AlertDialog.Builder(mContext)
			.setView(eventListLayout)
			.create();
			
			dialog.show();
		}
		return true;
	}
	
	/**
	 * ListAdapter for events list in popup that is displayed when a cluster of events is clicked.
	 * @author nasrullah
	 *
	 */
	class EventListAdapter extends ArrayAdapter<EventMarker> {

		public EventListAdapter(Context context, List<EventMarker> objects) {
			super(context, R.layout.eventhistory_listitem, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EventMarker event = getItem(position);
			
			View row = convertView;
			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.eventhistory_listitem, parent, false);
			}
			
			int eventType = event.getEventType();
			
			int iconResource = 0;
			iconResource = EventType.get(eventType).getImageResource();
			
			ImageView icon = (ImageView) row.findViewById(R.id.eventhistory_icon);
			icon.setImageResource(iconResource);
			
			TextView type = (TextView) row.findViewById(R.id.eventhistory_type);
			TextView details = (TextView) row.findViewById(R.id.eventhistory_location);
			int nameResource = 0;
			nameResource = EventType.get(eventType).getEventString();
			
			type.setText(nameResource);
			
			long timeStamp = event.getTimeStamp();
			
			TextView date = (TextView) row.findViewById(R.id.eventhistory_date);
			TextView time = (TextView) row.findViewById(R.id.eventhistory_time);
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
			DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
			Date d = new Date(timeStamp);
			date.setText(" " + dateFormat.format(d));
			time.setText(timeFormat.format(d));
			details.setText (event.getDetails());
			
			int id = event.getEventId();
			row.setTag(id);
			
			return row;
		}		
	}
}
