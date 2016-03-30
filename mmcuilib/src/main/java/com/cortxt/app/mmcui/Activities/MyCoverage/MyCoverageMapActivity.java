package com.cortxt.app.mmcui.Activities.MyCoverage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.cortxt.app.mmcui.Activities.MMCTrackedMapActivityOld;
import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcui.utils.ScalingUtility;

public class MyCoverageMapActivity extends MMCTrackedMapActivityOld {

	public MMCMapView mMapView = null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.mycoverage_map_activity, null, false);
		ScalingUtility.getInstance(this).scaleView(view);
		setContentView(view);

		mMapView = (MMCMapView) findViewById(R.id.mycoverage_mapview);
	}
}
