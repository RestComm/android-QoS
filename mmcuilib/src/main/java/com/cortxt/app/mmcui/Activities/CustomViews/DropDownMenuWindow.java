package com.cortxt.app.mmcui.Activities.CustomViews;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.cortxt.app.mmcui.utils.ScalingUtility;
import com.cortxt.app.mmcutility.Utils.MMCLogger;

public class DropDownMenuWindow {

	private PopupWindow popWindow=null;
	int width=0;
	int offsetY=0;
    public static long  lastWindowDismissedTime=0;
    public static boolean isWindowAlreadyShowing=false;
    private static DropDownMenuWindow coverageMenu=null;
    
	@SuppressWarnings("deprecation")
	public  DropDownMenuWindow(View dropDownView,Context context,int offset,int winWidth){
		width= ScalingUtility.getInstance((Activity) context).reSizeWidth(winWidth);
		offsetY=ScalingUtility.getInstance((Activity)context).reSizeHeight(offset);

		popWindow=new PopupWindow(dropDownView, width, LayoutParams.WRAP_CONTENT);
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setOutsideTouchable(true);
		
		popWindow.setTouchInterceptor(PopUpToucIntercepter);
	}
	
	public void showCalculatorMenu(View anchor){	
		try{
			popWindow.showAsDropDown(anchor, 0, offsetY);
		} catch (Exception e){
			MMCLogger.logToFile(MMCLogger.Level.ERROR, "DropDownMenuWindow", "showCalculatorMenu", "exception", e);
		}
		isWindowAlreadyShowing=true;
	}
	
	public static void setCoverageMenu(DropDownMenuWindow newCoverageMenu){	
		coverageMenu = newCoverageMenu;
	}
	
	public static DropDownMenuWindow getCoverageMenu(){	
		return coverageMenu;
	}
	
	OnTouchListener PopUpToucIntercepter=new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_OUTSIDE ){
				lastWindowDismissedTime=System.currentTimeMillis();
				popWindow.dismiss();
				isWindowAlreadyShowing=false;
				return true;
			}
			return false;
		}
	};
	
	public void dismissWindow(){

		if(popWindow!=null){
			lastWindowDismissedTime=System.currentTimeMillis();
			popWindow.dismiss();
			isWindowAlreadyShowing=false;
		}
	}
}
