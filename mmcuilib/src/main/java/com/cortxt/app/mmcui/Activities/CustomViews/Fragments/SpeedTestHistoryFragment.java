package com.cortxt.app.mmcui.Activities.CustomViews.Fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cortxt.app.mmcui.Activities.SpeedTestHistory;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.FontsUtil;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.TaskHelper;


// TODO: use Compatibility Package
@SuppressLint("NewApi")
public class SpeedTestHistoryFragment extends ListFragment {

	private TextView mEmptyMessage = null;
	private int eventtype = 24;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.frag_speedtesthistory, null, false);
		TextView titleText = (TextView) view.findViewById(R.id.speedTestHistoryTitle);
		TextView dateText = (TextView) view.findViewById(R.id.DateListHeading);
		TextView typeText = (TextView) view.findViewById(R.id.TypeListHeading);
		TextView latencyText = (TextView) view.findViewById(R.id.LatencyListHeading);
		TextView downloadtext = (TextView) view.findViewById(R.id.DownloadListHeading);
		TextView uploadText = (TextView) view.findViewById(R.id.uploadListHeading);
		mEmptyMessage = (TextView) view.findViewById(R.id.speedtesthistory_emptymessage);



		FontsUtil.applyFontToTextView(MmcConstants.font_Regular, titleText, getActivity());
		FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, dateText, getActivity());
		FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, typeText, getActivity());
		FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, latencyText, getActivity());
		FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, downloadtext, getActivity());
		FontsUtil.applyFontToTextView(MmcConstants.font_MEDIUM, uploadText, getActivity());

		try {
			eventtype = ((SpeedTestHistory) getActivity()).getEventType();
			if (eventtype != EventType.MAN_SPEEDTEST.getIntValue()) {
				uploadText.setText(R.string.activetest_stalls);

			}
		}catch (Exception e) {}
		updateHistory();

		return view;
	}

	public void updateHistory() {
		TaskHelper.execute(
				new AsyncTask<Void, Void, List<HashMap<String, String>>>() {
					@Override
					protected List<HashMap<String, String>> doInBackground(Void... params) {
						try {
							List<HashMap<String, Long>> rawResults = ReportManager.getInstance(getActivity()).getSpeedTestResults(eventtype, 0, Long.MAX_VALUE);
							List<HashMap<String, String>> results = getFormattedResults(rawResults);
							return results;
						} catch (Exception e) {
						} // in case window closed
						return null;
					}

					@Override
					protected void onPostExecute(List<HashMap<String, String>> results) {
						try {
							if (results != null && !results.isEmpty()) {
								SpeedHistoryAdapter resultsAdapter = new SpeedHistoryAdapter(results);
								setListAdapter(resultsAdapter);
								//					resultsAdapter.notifyDataSetChanged();
								mEmptyMessage.setVisibility(View.INVISIBLE);
							} else {
								mEmptyMessage.setVisibility(View.VISIBLE);
							}
						} catch (Exception e) {
						} // in case window closed
					}

				});
		//.execute((Void[]) null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	// TODO: on back finish the parent activity
	/*
	 * public void MapBackActionClicked(View view) { this.finish(); }
	 */

	private List<HashMap<String, String>> getFormattedResults(List<HashMap<String, Long>> rawResults) {
		List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>(rawResults.size());

		try{
		for (HashMap<String, Long> rawResult : rawResults) {
			HashMap<String, String> result = new HashMap<String, String>();

			long tier = rawResult.get(ReportManager.SpeedTestKeys.SPEEDTIER);
			result.put(ReportManager.SpeedTestKeys.SPEEDTIER, Long.toString(tier));

			long latency = rawResult.get(ReportManager.SpeedTestKeys.LATENCY);
			result.put(ReportManager.SpeedTestKeys.LATENCY, latency + " " + getString(R.string.speedtest_milliseconds));

			long downSpeed = rawResult.get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED);
			result.put(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED, getFormattedSpeed((int) downSpeed) + " " + getUnits((int) downSpeed));

			Long upSpeed = (rawResult.get(ReportManager.SpeedTestKeys.UPLOAD_SPEED));
			if (eventtype != EventType.MAN_SPEEDTEST.getIntValue())
				result.put(ReportManager.SpeedTestKeys.UPLOAD_SPEED, upSpeed.toString());
			else
				result.put(ReportManager.SpeedTestKeys.UPLOAD_SPEED, getFormattedSpeed((int)(long) upSpeed) + " " + getUnits((int)(long) upSpeed));

			long type = rawResult.get(ReportManager.EventKeys.TYPE);
			result.put(ReportManager.EventKeys.TYPE, Long.toString(type));

			long timeStamp = rawResult.get(ReportManager.SpeedTestKeys.TIMESTAMP);
			// long timeStamp =
			// Long.parseLong(rawResult.get(SpeedTestKeys.TIMESTAMP));
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
			DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
			Date d = new Date(timeStamp);
			String date = dateFormat.format(d);
			String time = timeFormat.format(d);
			result.put(ReportManager.SpeedTestKeys.TIMESTAMP, date + "\n" + time);

			results.add(result);
		}
		} catch (Exception e) {}
		return results;
	}

	/**
	 * Gets the appropriate units for the given speed (kbps or mbps)
	 * 
	 * @param speed
	 *            transfer speed in bits/second
	 * @return the appropriate units for the given speed
	 */
	private String getUnits(int speed) {
		if (speed >= 0) {
			return getString(R.string.speedtest_mbps);
		} else {
			return getString(R.string.speedtest_kbps);
		}
	}

	/**
	 * Gets the speed formatted for displaying, and with the units given by
	 *
	 * @param speed
	 *            transfer speed in bits/second
	 * @return the speed formatted for displaying
	 */
	private String getFormattedSpeed(int speed) {
		if (speed >= 0) {
			return String.format("%2.1f", (float) speed / 1000000.0f);
		} else {
			return String.format("%2.1f", (float) speed / 1000.0f);
		}
	}

	private class SpeedHistoryAdapter extends BaseAdapter {

		List<HashMap<String, String>> historyResults = null;

		public SpeedHistoryAdapter(List<HashMap<String, String>> results) {
			historyResults = results;
		}

		@Override
		public int getCount() {
			return historyResults.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.speedtesthistory_listitem, null, true);
				ScalingUtility.getInstance(getActivity()).scaleView(convertView);
			}
			TextView latencySpeed = (TextView) convertView.findViewById(R.id.speedtesthistory_latency);
			TextView downSpeed = (TextView) convertView.findViewById(R.id.speedtesthistory_downspeed);
			TextView upSpeed = (TextView) convertView.findViewById(R.id.speedtesthistory_upspeed);
			TextView speedType = (TextView) convertView.findViewById(R.id.speedtesthistory_type);
			TextView speedDate = (TextView) convertView.findViewById(R.id.speedtesthistory_date);
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, latencySpeed, getActivity());
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, downSpeed, getActivity());
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, upSpeed, getActivity());
			FontsUtil.applyFontToTextView(MmcConstants.font_Regular, speedDate, getActivity());

			String latency = historyResults.get(position).get(ReportManager.SpeedTestKeys.LATENCY);
			latencySpeed.setText("" + latency);
			String speed = historyResults.get(position).get(ReportManager.SpeedTestKeys.DOWNLOAD_SPEED);
			String type =  historyResults.get(position).get(ReportManager.EventKeys.TYPE);
			if (type.equals("54"))
			{
				downSpeed.setText(R.string.GenericText_Connect);
				if (latency.indexOf("-") >= 0)
				{
					upSpeed.setText(R.string.GenericText_Failed);
					if (latency.indexOf("-2") >= 0)
						downSpeed.setText(R.string.GenericText_Download);
					else if (latency.indexOf("-3") >= 0)
						downSpeed.setText(R.string.GenericText_Upload);
					else if (latency.indexOf("-5") >= 0)
						downSpeed.setText(R.string.GenericText_Test);
					else if (latency.indexOf("-4") >= 0)
						downSpeed.setText(R.string.speedtest_latency);
					latencySpeed.setText("");
				}
				else
					upSpeed.setText("Test");
			}
			else
			{
				if (speed.equals("0.0 Kb/s"))
					speed = "";
				downSpeed.setText("" + speed);
				speed = historyResults.get(position).get(ReportManager.SpeedTestKeys.UPLOAD_SPEED);
				if (speed.equals("0.0 Kb/s"))
					speed = "";
				upSpeed.setText("" + speed);
				if (type.equals("51")) // Passive speed test
				{
					latencySpeed.setText(R.string.GenericText_Passive);
				}
			}
			speedDate.setText("" + historyResults.get(position).get(ReportManager.SpeedTestKeys.TIMESTAMP));

			int tier = Integer.parseInt(historyResults.get(position).get(ReportManager.SpeedTestKeys.SPEEDTIER));
			switch (tier) {
			case 2:
				speedType.setText("2G");
				break;
			case 3:
				speedType.setText("3G");
				break;
			case 4:
				speedType.setText("3G");
				break;
			case 5:
				speedType.setText("LTE");
				break;
			case 10:
				speedType.setText("WiFi");
				break;
			case 11:
				speedType.setText("WiMax");
				break;
			}

			return convertView;
		}

	}
}
