package com.cortxt.app.mmcui.Activities.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;

/**
 * 
 * @author abhin
 *
 */
public class SatelliteStatus extends LinearLayout {
	private static final String TAG = SatelliteStatus.class.getSimpleName();
	private TextView statusText;
	
	public SatelliteStatus(Context context) {
		super(context);
		init(context);
	}

	public SatelliteStatus(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		//set parameters for the linearLayout (this)
		this.setOrientation(HORIZONTAL);
		
		//insert gps image
		ImageView gpsImage = new ImageView(context);
		gpsImage.setImageResource(R.drawable.satellite_icon);
		addView(gpsImage);
		gpsImage.setPadding(0, 8, 8, 0);
		
		//insert the status text
		statusText = new TextView(context);
		statusText.setText(context.getString(R.string.LiveStatus_GPSOFF));	//TODO : Add gps off text here
		statusText.setTextColor(0xff666666);
		addView(statusText);		
	}
	
	public void setText(String text){
		this.statusText.setText(text);
	}
}
