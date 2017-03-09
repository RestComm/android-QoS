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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import org.restcomm.app.qoslib.R;

import org.restcomm.app.utillib.DataObjects.EventType;
import org.restcomm.app.utillib.Utils.ScalingUtility;
import org.restcomm.app.utillib.Utils.ShareInviteTask;
import org.restcomm.app.utillib.Utils.TaskHelper;

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
