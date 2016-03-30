package com.cortxt.app.mmcui.Activities;

import java.util.HashSet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.TaskHelper;


public class EventHistory extends MMCTrackedActivityOld {
	private static final HashSet<Integer> EVENTS_TO_DISPLAY = new HashSet<Integer>();
	static {
		EVENTS_TO_DISPLAY.add(EventType.EVT_DROP.getIntValue());
		EVENTS_TO_DISPLAY.add(EventType.EVT_CALLFAIL.getIntValue());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* do we need to show this as a separate activity in landscape for some cases (eg Engineer Mode)? 
		 * If yes, leave it as is.. otherwise, if its only for portrait then do a return here for landscape devices
		*/
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view  = inflater.inflate(R.layout.eventhistory, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);

        MMCActivity.customizeTitleBar(this, view, R.string.dashboard_eventhistory, R.string.dashcustom_eventhistory);
	}
	
	public void MapBackActionClicked(View v) {
		this.finish();
	}

	public void shareClicked(View view){
		String msg=getString(R.string.history_sharetitle);
		TaskHelper.execute(
				new ShareTask(this, msg, msg, findViewById(R.id.eventhistoryContainer)));
	}
}
