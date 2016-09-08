package com.cortxt.app.corelib.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.cortxt.app.corelib.R;
import com.cortxt.app.utillib.DataObjects.EventType;
import com.cortxt.app.utillib.Utils.ScalingUtility;
import com.cortxt.app.utillib.Utils.ShareInviteTask;
import com.cortxt.app.utillib.Utils.TaskHelper;

import java.util.HashSet;


public class EventHistory extends FragmentActivity {
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
		View view  = inflater.inflate(R.layout.callhistory, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);
	}
	
	public void MapBackActionClicked(View v) {
		this.finish();
	}

	public void shareClicked(View view){
		String msg=getString(R.string.history_sharetitle);
		TaskHelper.execute(
				new ShareInviteTask(this, msg, msg, findViewById(R.id.eventhistoryContainer)));
	}
}
