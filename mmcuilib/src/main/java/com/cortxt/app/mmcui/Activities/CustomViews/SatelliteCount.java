package com.cortxt.app.mmcui.Activities.CustomViews;


import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cortxt.app.mmcui.R;

public class SatelliteCount extends LinearLayout {
	private static final String TAG = SatelliteCount.class.getSimpleName();
	private static final int NUMBER_OF_GPS_TILES = 8;
	private static final int TILE_PADDING = 1;

	private int numberOfSatellites = 0;	//this is the number of satellites with SNR > 10
	private int numberOfSatellitesInFix = 0;	//this is the number of satellites we have a fix from

	private ArrayList<ImageView> satTiles;

	public SatelliteCount(Context context, AttributeSet attrs) {
		super(context, attrs);
		//TypedArray typedArr = context.obtainStyledAttributes(attrs, R.styleable.SatelliteCount);
        this.setOrientation(HORIZONTAL);
		this.setGravity(Gravity.LEFT);

		satTiles = new ArrayList<ImageView>(NUMBER_OF_GPS_TILES);
		for (int counter = 0; counter < NUMBER_OF_GPS_TILES; counter++){
			ImageView satTile = new ImageView(context);
			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 0, 0, 0);
			satTile.setLayoutParams(params);
			satTiles.add(counter, satTile);
			satTile.setImageResource(R.drawable.satellite_gray);
			//satTile.setPadding(TILE_PADDING, TILE_PADDING, TILE_PADDING, TILE_PADDING);
			addView(satTile);
		}
		updateSatelliteTileImages();
	}

	public void updateSatelliteCount(int numberOfSatellites, int numberOfSatellitesInFix){
		this.numberOfSatellites = numberOfSatellites;
		this.numberOfSatellitesInFix = numberOfSatellitesInFix;
		updateSatelliteTileImages();
	}

	private void updateSatelliteTileImages() {
		for (int counter = 0; counter < NUMBER_OF_GPS_TILES; counter++){
			if (counter < numberOfSatellitesInFix)
				satTiles.get(counter).setImageResource(R.drawable.satellite_green);
			else if (counter < numberOfSatellites)
				satTiles.get(counter).setImageResource(R.drawable.satellite_gray);
			else
				satTiles.get(counter).setImageResource(R.drawable.satellite_gray_dark);
		}
	}

}
