/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.qoslib.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.restcomm.app.utillib.DataObjects.EventType;
import org.restcomm.app.utillib.Reporters.ReportManager;
import org.restcomm.app.utillib.Reporters.WebReporter.WebReporter;
import org.restcomm.app.utillib.Utils.Global;
import org.restcomm.app.utillib.Utils.LoggerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.restcomm.app.qoslib.R;

// TODO: use Compatibility Package
@SuppressLint("NewApi")
public class EventHistoryFragment extends ListFragment {
	private static final HashSet<Integer> EVENTS_TO_DISPLAY = new HashSet<Integer>();
	static {
		EVENTS_TO_DISPLAY.add(EventType.EVT_DROP.getIntValue());
		EVENTS_TO_DISPLAY.add(EventType.EVT_CALLFAIL.getIntValue());
	}

	// private ListView mListView;
	private TextView mEmptyMessage;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.frag_eventhistory, null, false);

		//TextView mapHistoryTitle = (TextView) view.findViewById(R.id.actionbartitle);

		Intent intent = this.getActivity().getIntent();
		boolean bFromStats = false;
		boolean bFromNerd = false;
		HashSet<Integer> eventsToDisplay = new HashSet<Integer>();

		int eventtype = 0;
		if (intent.hasExtra("eventtype"))
			eventtype = intent.getIntExtra("eventtype",0);
		int[] eventtypes;
		if (intent.hasExtra("eventtypes"))
		{
			eventtypes = intent.getIntArrayExtra("eventtypes");
			for (int i=0; i<eventtypes.length; i++)
				eventsToDisplay.add(eventtypes[i]);
		}

		mEmptyMessage = (TextView) getActivity().findViewById(R.id.eventhistory_emptymessage);

		if (eventtype > 0)
			eventsToDisplay.add(eventtype);

		//load lat/lons for locations before trying to geocode
		preGeocode(eventsToDisplay);
		
		return view;
	}

	public void parseAddress(String response,  List<HashMap<String, String>> events) {
		JSONObject json = null;
		
		if(response == null)
			return;
		
		try {
			json = new JSONObject(response);
			
			if(json.has("error")) {
				String error = json.getString("error");
			}
			else {
				json = json.getJSONObject("address");
				HashMap<String, String> event = events.get(0);
				String number = "";
				if(json.has("house_number"))
					number = json.getString("house_number");
				String road = json.getString("road");
				String suburb = "";
				//if(json.has("suburb"))
				//	suburb = ", " + json.getString("suburb");
				String address = number + " " + road + suburb;
				event.put(KEY_ADDRESS, address.trim());
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
			return;
		}
	} 
	
	public void parseAddresses(String response, List<HashMap<String, String>> events) {
		final String KEY_ADDRESS = "address";
		JSONObject json = null;
		JSONArray jsonArray = null;

		if(response == null)
			return;
		
		try {
			jsonArray = new JSONArray(response);
		} catch (JSONException e) {
			//Right now the server will give back a JSONObject if there is only 1 location, so if that happens here catch it 
			//This will be fixed soon so revisit this TODO 
			parseAddress(response, events);
			return;
		}
		try {
			for(int i = 0; i < jsonArray.length(); i++) {
				json = jsonArray.getJSONObject(i);
				HashMap<String, String> event = events.get(i);
				
				if(json.has("error")) {
					String error = json.getString("error");
				}
				else {
					json = json.getJSONObject("address");
					String number = "";
					if(json.has("house_number"))
						number = json.getString("house_number");
					String road = json.getString("road");
					String suburb = "";
					//if(json.has("suburb"))
					//	suburb = ", " + json.getString("suburb");
					String address = number + " " + road + suburb;
					event.put(KEY_ADDRESS, address.trim());
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
			return;
		}
/*
			@Override
			protected void onPostExecute(List<HashMap<String, String>> results) {
				try{
					if (mEmptyMessage == null) {
						if(getActivity() == null) {
							return;
						}
						mEmptyMessage = (TextView) getActivity().findViewById(R.id.eventhistory_emptymessage);
					}
					if (!results.isEmpty()) {
						EventListAdapter adapter = new EventListAdapter(EventHistoryFragment.this.getActivity(), results);
						// mListView.setAdapter(adapter);
						setListAdapter(adapter);
						mEmptyMessage.setVisibility(View.INVISIBLE);
						// ScalingUtility.getInstance(EventHistory.this).scaleView(mListView);
						// Toast.makeText(getActivity(), "not empty", Toast.LENGTH_SHORT).show();
					} else {
						mEmptyMessage.setVisibility(View.VISIBLE);
						// Toast.makeText(getActivity(), "empty", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e){} // in case window was closed
			}

		});
		//.execute((Void[]) null);
		
		return view;
*/
	}
	
	public void toggleHistory() {

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Toast.makeText(this.getActivity(), "Selected: " + position, Toast.LENGTH_SHORT).show();
//		v.setBackgroundColor(Color.WHITE);

		HashMap<String, String> event  = (HashMap<String,String>) v.getTag();
//		openEventShare (eventId);
		long eventId = Long.parseLong(event.get(ReportManager.EventKeys.EVENTID));
		int evtid = Integer.parseInt(event.get(ReportManager.EventKeys.ID));
		String name = event.get("name");

		Intent intent = new Intent(EventHistoryFragment.this.getActivity(), EventDetailWeb.class);
		intent.putExtra(EventDetailWeb.EXTRA_EVENT_ID, evtid);
		intent.putExtra("eventName", name);
		startActivity(intent);
	}



	// TODO: on back finish the parent activity
	/*
	 * public void MapBackActionClicked(View view) { this.finish(); }
	 */
	private static final String KEY_ADDRESS = "address";

	class EventListAdapter extends ArrayAdapter<HashMap<String, String>> {
		public EventListAdapter(Context context, List<HashMap<String, String>> objects) {
			super(context, R.layout.eventhistory_listitem, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			try {
				HashMap<String, String> event = getItem(position);

				if (row == null) {
					LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					row = inflater.inflate(R.layout.eventhistory_listitem, parent, false);
					//ScalingUtility.getInstance(EventHistoryFragment.this.getActivity()).scaleView(row);
				}

				EventType eventType = EventType.get(Integer.parseInt(event.get(ReportManager.EventKeys.TYPE)));
				TextView date = (TextView) row.findViewById(R.id.eventhistory_date);
				TextView time = (TextView) row.findViewById(R.id.eventhistory_time);
				TextView type = (TextView) row.findViewById(R.id.eventhistory_type);
				TextView loc = (TextView) row.findViewById(R.id.eventhistory_location);

//				FontsUtil.applyFontToTextView(Constants.font_Regular, type, EventHistoryFragment.this.getActivity());
//				FontsUtil.applyFontToTextView(Constants.font_Regular, time, EventHistoryFragment.this.getActivity());
//				FontsUtil.applyFontToTextView(Constants.font_Regular, date, EventHistoryFragment.this.getActivity());
//				FontsUtil.applyFontToTextView(Constants.font_Regular, loc, EventHistoryFragment.this.getActivity());

				int iconResource = eventType.getImageResource();
				int nameResource = eventType.getEventString();

				ImageView icon = (ImageView) row.findViewById(R.id.eventhistory_icon);
				if (iconResource != 0)
					icon.setImageResource(iconResource);

				String title = "";
				if (nameResource > 0)
					title = getContext().getString (nameResource);


				type.setText(title);

				long timeStamp = Long.parseLong(event.get(ReportManager.EventKeys.TIMESTAMP));
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
				DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
				Date d = new Date(timeStamp);
				date.setText(" / " + dateFormat.format(d));
				time.setText(timeFormat.format(d));

				if (event.containsKey(KEY_ADDRESS)) {
					loc.setText(event.get(KEY_ADDRESS));
				}
				int id = Integer.parseInt(event.get(ReportManager.EventKeys.ID));
				row.setTag(event);
			} catch (Exception e) {
				LoggerUtil.logToFile(LoggerUtil.Level.DEBUG, "EventHistory", "getView", "", e);
			}
			return row;
		}
	}

	public void preGeocode(final HashSet<Integer> eventsToDisplay) {
		new AsyncTask<Void, Void, List<HashMap<String, String>>>() {
			@Override
			protected List<HashMap<String, String>> doInBackground(Void... params) {
				List<HashMap<String, String>> results = ReportManager.getInstance(getActivity()).getEvents(eventsToDisplay);
				
				for (HashMap<String, String> event : results) {
					try {
						if (event == null) {
							continue;
						}
						if (!(event.containsKey(ReportManager.EventKeys.LATITUDE) && event.containsKey(ReportManager.EventKeys.LONGITUDE))) {
							event.put(KEY_ADDRESS, getActivity().getString(R.string.mycoverage_unknownlocation));
							continue;
						}
						Double lat = Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE));
						Double lon = Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE));
						String address = "@" + String.format("%.4f, %.4f", lat, lon);
						event.put(KEY_ADDRESS, address);
					}
					catch (Exception e){
						System.out.println(e);
					}
				}	
				
				return results;
			}

			@Override
			protected void onPostExecute(List<HashMap<String, String>> results) {
				if(getActivity() == null) {
					return;
				}
				
				if (mEmptyMessage == null) {
					
					mEmptyMessage = (TextView) getActivity().findViewById(R.id.eventhistory_emptymessage);
				}
				if (!results.isEmpty()) {
					EventListAdapter adapter = new EventListAdapter(EventHistoryFragment.this.getActivity(), results);
					setListAdapter(adapter);
					mEmptyMessage.setVisibility(View.INVISIBLE);
				}
				else {
					mEmptyMessage.setVisibility(View.VISIBLE);
				}
				
				geocode(eventsToDisplay);
			}

		}.execute((Void[]) null);
	}
	
	public void geocode(final HashSet<Integer> eventsToDisplay) {
		new AsyncTask<Void, Void, List<HashMap<String, String>>>() {
			@Override
			protected List<HashMap<String, String>> doInBackground(Void... params) {
				List<HashMap<String, String>> results = ReportManager.getInstance(getActivity()).getEvents(eventsToDisplay);
				
				String locations = "";
				for (HashMap<String, String> event : results) {
					try
					{
						if (event == null) {
							locations += "&location=0&location=0"; //This keeps order for parseAddresses()
							continue;
						}
						if (!(event.containsKey(ReportManager.EventKeys.LATITUDE) && event.containsKey(ReportManager.EventKeys.LONGITUDE))) {
							event.put(KEY_ADDRESS, getActivity().getString(R.string.mycoverage_unknownlocation));
							locations += "&location=0&location=0"; //This keeps order for parseAddresses()
							continue;
						}
						Double lat = Double.parseDouble(event.get(ReportManager.EventKeys.LATITUDE));
						Double lon = Double.parseDouble(event.get(ReportManager.EventKeys.LONGITUDE));
						locations += "&location=" + lat + "&location=" + lon;
						String address = String.format("%.4f, %.4f", lat, lon);
						event.put(KEY_ADDRESS, "@" + address);
					}
					catch (Exception e){
						System.out.println(e);
					}

				}	
				
				if(locations.equals(""))
					return null;
				
				try {
					String apiKey = Global.getApiKey(getActivity());
					String server = Global.getApiUrl(getActivity());
					String url = server + "/api/osm/location?apiKey=" + apiKey + locations;
					String response = WebReporter.getHttpURLResponse(url, false);

					parseAddresses(response, results);
				}
				catch (Exception e){
					System.out.println(e);
				}
				
				return results;
			}
			
			@Override
			protected void onPostExecute(List<HashMap<String, String>> results) {
				if(getActivity() == null || results == null) {
					return;
				}
				try{
				if (mEmptyMessage == null) {
					
					mEmptyMessage = (TextView) getActivity().findViewById(R.id.eventhistory_emptymessage);
				}
				if (!results.isEmpty() && EventHistoryFragment.this.getActivity() != null) {
					EventListAdapter adapter = new EventListAdapter(EventHistoryFragment.this.getActivity(), results);
					// mListView.setAdapter(adapter);
					setListAdapter(adapter);
					mEmptyMessage.setVisibility(View.INVISIBLE);

				} else {
					mEmptyMessage.setVisibility(View.VISIBLE);
				}
				} catch (Exception e) {} // in case window closed
			}

		}.execute((Void[]) null);
	}
}
