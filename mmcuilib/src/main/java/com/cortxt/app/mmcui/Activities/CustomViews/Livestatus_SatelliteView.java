package com.cortxt.app.mmcui.Activities.CustomViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Livestatus_SatelliteView extends View {
	private Paint mPaint;
	public int cc = 0;
	private int BlueSatellite = 0xff007eb8;
	public float xLeft = 0;
	public float xRight = 0;
	public float yTop = 0;
	public float yBottom = 0;
	
	public Livestatus_SatelliteView(Context context) {
		super(context);
	}
	
	public Livestatus_SatelliteView(Context context,AttributeSet attr)
	{
		super(context,attr);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xRight = w;
		yBottom = h;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint = new Paint();
		mPaint.setColor(BlueSatellite);
//		MMCLogger.d("Leon", "sate cc:"+cc);
		for (int i = 0; i < cc; i++) {
			int sateBoundary = 15*(i+1);
			if (sateBoundary > xRight) {
				sateBoundary = (int) xRight;
			}
			canvas.drawRect(new Rect((int)(xLeft+5+(15*i)), (int)yTop+5, sateBoundary, (int)yBottom), mPaint);
		}
	}

	
}
