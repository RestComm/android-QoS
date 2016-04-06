package com.cortxt.app.uilib.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cortxt.app.uilib.R;
import com.cortxt.app.uilib.utils.ScalingUtility;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.Utils.TaskHelper;

public class SpeedTestHistory extends MMCActivity {

	private int eventtype = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent newintent = getIntent();
		String title = "";
		if (newintent.hasExtra("EVENTTYPE")) {
			eventtype = newintent.getIntExtra("EVENTTYPE", 0);
			EventType eventType = EventType.get(eventtype);
			title = eventType.getEventString(this) + " - " + getString(R.string.mystats_history);
		}
		View view  = getLayoutInflater().inflate(R.layout.speedtesthistory, null, false);
		view.findViewById(R.id.speedTestHistoryTitle).setVisibility(View.GONE);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
        customizeTitleBar(this,view,R.string.speedtesthistory_title, R.string.speedtesthistory_title);
		TextView headerText = (TextView) view.findViewById(R.id.actionbartitle);
		headerText.setText (title);
	}	

	public void backActionClicked(View button){
		this.finish();
	}
	
	public void shareClicked(View view) {
		String message = "Speed test history";
		TaskHelper.execute(
				new ShareTask(this, message, message, findViewById(R.id.speedTestHistoryContainer)));
	}

	public int getEventType ()
	{
		return eventtype;
	}
}
