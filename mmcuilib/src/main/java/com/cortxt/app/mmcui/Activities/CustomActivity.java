package com.cortxt.app.mmcui.Activities;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;

/**
 * The base class for all activities that have the custom title bar
 * @author nasrullah
 *
 */
public abstract class CustomActivity extends MMCTrackedActivityOld {
	private TextView mTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		//TODO : fix setContentView parameter
		setContentView(new View(getApplicationContext()));
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		
		mTitle = (TextView) findViewById(R.id.textview_title);
	}
	
	/**
	 * Set the text of the title bar of the window
	 * @param whiteText the part of the title text that is white
	 * @param blueText The part of the title text that is blue
	 */
	protected void setTitle(String whiteText, String blueText) {
		SpannableString text = new SpannableString(whiteText + blueText);
		
		int white = getResources().getColor(R.color.white);
		int blue = getResources().getColor(R.color.title_blue);
		
		text.setSpan(new ForegroundColorSpan(white), 0, whiteText.length(), 0);
		text.setSpan(new ForegroundColorSpan(blue), whiteText.length(), text.length(), 0);
		
		mTitle.setText(text);
	}

}
