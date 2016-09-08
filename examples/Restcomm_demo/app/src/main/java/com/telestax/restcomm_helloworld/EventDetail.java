package com.telestax.restcomm_helloworld;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cortxt.app.utillib.Utils.ShareInviteTask;
import com.cortxt.app.utillib.Utils.TaskHelper;


public class EventDetail extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view  = inflater.inflate(R.layout.eventhistory, null, false);
		//ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_share, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_share) {
			String msg=getString(R.string.history_sharetitle);
			TaskHelper.execute(
					new ShareInviteTask(this, msg, msg, findViewById(R.id.eventhistoryContainer)));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
