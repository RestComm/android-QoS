package com.cortxt.app.mmcui.utils;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;

public class ScalingUtility {
	private final double standardWidth = 720;
	private final double standardHeight = 1184;
	private final double standardDensity = 2.0;
	private double runningDeviceDensity = 0;

	private double widthRatio = 0;
	private double heightRatio = 0;
	private double textSizeScalingFactor = 0;
	private double minScalingFactor = 0;
	private static ScalingUtility scalingUtility = null;
	Context context = null;
	int width = 0;
	int height = 0;
	boolean portrait = true;

	public static ScalingUtility getInstance(Context context) {
		if (scalingUtility == null && context != null) {
			scalingUtility = new ScalingUtility(context);
		}
		return scalingUtility;
	}

	protected ScalingUtility(Context ctxt) {
		context = ctxt;//activity.getApplicationContext();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();
		Log.e("Scaling", "Size: " + width + " : " + height);
		runningDeviceDensity = ctxt.getResources().getDisplayMetrics().density;

		widthRatio = portrait ? width / standardWidth : width/standardHeight ;
		heightRatio = portrait ?  height / standardHeight : heightRatio/standardWidth;
		minScalingFactor = Math.min(widthRatio, heightRatio);
		textSizeScalingFactor = minScalingFactor * (standardDensity / runningDeviceDensity);
	}

	public void scaleView(View view) {
		recursiveScaleView(view, 1);
	}
	
	public void scaleView(View view, float scale) {
		recursiveScaleView(view, scale);
	}

	

	public static String getIDName(View view, Class<?> clazz) 
	{

		try{
			Integer id = view.getId();
	        Field[] ids = clazz.getFields();
	        for (int i = 0; i < ids.length; i++) {
	            Object val = ids[i].get(null);
	            if (val != null && val instanceof Integer
	                    && ((Integer) val).intValue() == id.intValue()) {
	                return ids[i].getName();
	            }
	        }	       
		}
		catch (Exception e) {}
		return ""; 
	}
	
	private void recursiveScaleView(View childView, float scale) {

		if (childView.getLayoutParams() instanceof AbsListView.LayoutParams) {

			AbsListView.LayoutParams linearParams = (AbsListView.LayoutParams) childView.getLayoutParams();

			if (linearParams.width > 0)
				linearParams.width *= widthRatio;
			if (linearParams.height > 0)
				linearParams.height *= heightRatio;

			childView.setLayoutParams(linearParams);

		} else {


			boolean isBBQ = false; // MMCService.getPlatform() == 3;
			if(android.os.Build.BRAND.toLowerCase().contains("blackberry"))
				isBBQ = true;
			double yMult = scale, txtMult = 1.0;
			if (height <= width && childView.getParent() != null && childView.getParent() instanceof View)
			{
				txtMult = isBBQ ? 1.4 : 1;
				String parentid = getIDName((View)(childView.getParent()), R.id.class);
				String id = getIDName(childView, R.id.class);
				if (id.equals("topactionbarLayout") || parentid.equals("topactionbarLayout"))
					yMult = isBBQ ? 2.0 : 1.0;// 1.8;
			}
			MarginLayoutParams linearParams = (MarginLayoutParams) childView.getLayoutParams();
			String tag = (String) childView.getTag();
			if (linearParams != null) {
				Log.d("Width", "" + childView.getId() + " ==Height" + linearParams.height + "==Tag" + childView.getTag());
				if (linearParams.width != MarginLayoutParams.WRAP_CONTENT && linearParams.width != MarginLayoutParams.MATCH_PARENT && !(tag != null && tag.equalsIgnoreCase("constantwidth"))) {
					linearParams.width *= minScalingFactor * yMult;
				}

				if (linearParams.height != MarginLayoutParams.WRAP_CONTENT && linearParams.height != MarginLayoutParams.MATCH_PARENT && !(tag != null && tag.equalsIgnoreCase("constantheight"))) {
					linearParams.height *= minScalingFactor * yMult;
				}
				linearParams.leftMargin *= widthRatio;
				linearParams.rightMargin *= widthRatio;
				linearParams.topMargin *= heightRatio;
				childView.setLayoutParams(linearParams);

			}
			if (childView instanceof TextView) {
				float textSize = ((TextView) childView).getTextSize();
				textSize *= textSizeScalingFactor * txtMult;

				((TextView) childView).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			}
		}

		if (childView instanceof ViewGroup) {
			int childCount = ((ViewGroup) childView).getChildCount();

			for (int i = 0; i < childCount; i++) {
				recursiveScaleView(((ViewGroup) childView).getChildAt(i), scale);
			}
			return;
		}

	}

	public int getCurrentWidth() {
		return width;
	}

	public int getCurrentHeight() {
		return height;
	}

	public int reSizeWidth(int width) {
		return (int) (width * widthRatio);
	}

	public int reSizeHeight(int height) {
		return (int) (height * heightRatio);
	}

	public double getScaleFactor() {
		return minScalingFactor;
	}

	public static void invalidate() {
		scalingUtility = null;
	}
}