package com.cortxt.app.mmcui.Activities.CustomViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LivestatusTimeCounter extends View {
	private Paint mPaint;
	public boolean showCounter = false;
	public int m = 0;
	public int s = 0;
	private float xLeft = 20;
	private float yTop = 12;
	
	public LivestatusTimeCounter(Context context) {
		super(context);
	}
	
	public LivestatusTimeCounter(Context context,AttributeSet attr)
	{
		super(context,attr);
	}

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(13);
//		MMCLogger.d("Leon", "LiveTimeCounter="+Boolean.toString(showCounter)+",("+m+","+s+")");
		if (showCounter) {
			canvas.drawText("("+m+":"+s+"s)", xLeft, yTop, mPaint);
		}

//		invalidate();  //unComment it for realtime, Comment it for Time Control
	}

	
}
