package com.cortxt.app.mmcui.Activities.CustomViews;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class LivestatusView extends View {
	private Paint mPaint;
	private Paint mPaint2;
//	public int c = 0;
	public int cc = 0;
	public int cc_voice = 0;
	public int pLength = 500;
	public int eventNumber_PhoneCall = 0;
	public int eventNumber_Voice = 0;
	public int[] eventArray;
	private int xLength;
	private int icon_boundary;
	private int icon_halflength = 10; //icon size 20x20
//	private int Green1 = -16759808;  //0xff004400
	private int Green2 = 0xff008800;
	private int Green3 = 0xff00cc00;
	private int xGap = 30;
	public float xLeft = 50;
	public float xRight;
	public float yTop = 30;
	public float yBottom;
	public float yMiddle;
//	private float maxC;
//	private float minC;
	public float[] pPosition;
	public float[] pPosition_raw;
	private boolean isBig = false;
	private int isDown = 0;
	public long timePoint=0;
	public long timeNow=0;
	public long[] eventTime_PhoneCall;
	public long[] eventTime_Voice;
	
	
	//use timepoint and timenow to put event icon

	public LivestatusView(Context context) {
		super(context);
	}
	
	public LivestatusView(Context context,AttributeSet attr)
	{
		super(context,attr);
		//TypedArray typeArray = context.obtainStyledAttributes(attr, R.styleable.LivestatusView);
		//isBig = typeArray.getBoolean(R.styleable.LivestatusView_isBig, false);
		//typeArray.recycle();
	}

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xRight = w-20;
		yBottom = h-30;
		yMiddle = (yBottom + yTop)/2;
		xLength = (int) (xRight-xLeft);
		icon_boundary = (int) (xRight-20);
		pPosition = new float[pLength];
		pPosition_raw = new float[pLength];
		eventTime_PhoneCall = new long[pLength];
		eventTime_Voice = new long[pLength];
		eventArray = new int[pLength];
//		for (int i = 0; i < pPosition.length; i++) {
//			pPosition[i] = yMiddle;
//		}
//		maxC = yBottom - yMiddle;
//		minC = yTop - yMiddle;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	// Generate a color from red to green for signal -120 to -40
    public static int signalColor (int iSignal)
    {
        int green = 5 * (iSignal + 120);
        if (green > 255)
            green = 255;
        
        int red = 250 - (120 + iSignal) * 5;
        if (red < 0)
            red = 0;
        
        int diff = green - red;
        if (diff < 0)
            diff = red-green;
        diff = (250-diff)/2;    
        green = green + diff;
        red = red + diff;   
        if (green > 255)
            green = 255;
        if (red > 255)
            red = 255;         
        
        green = (green / 8) * 8;        
        red = (red / 8) * 8;
        int iColor = red << 16;        
        iColor += green << 8;
        return iColor;
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Path path = new Path();
		mPaint = new Paint();
		mPaint2 = new Paint();  //for change trend color
		mPaint.setColor(Color.WHITE);
		canvas.drawRect(new Rect((int)xLeft, (int)yTop, (int)xRight, (int)yBottom), mPaint);
		mPaint.setColor(Color.BLACK);
		canvas.drawLine(xLeft-5, yTop, xRight, yTop, mPaint);
		canvas.drawLine(xLeft-5, yMiddle, xRight, yMiddle, mPaint);
		canvas.drawLine(xLeft-5, yBottom, xRight, yBottom, mPaint);
		canvas.drawLine(xLeft, yTop, xLeft, yBottom, mPaint);
		canvas.drawLine(xRight, yTop, xRight, yBottom, mPaint);
		if(isBig){
			mPaint.setTextSize(12);
			float xGapNumber = (xRight - xLeft)/xGap;
			for (int i = 0; i < xGapNumber; i++) {
				canvas.drawLine(xLeft+xGap*i, yBottom-7, xLeft+xGap*i, yBottom+4, mPaint);
				canvas.drawText(xGap*i+"s", xLeft+xGap*i, yBottom+20, mPaint);
			}
		}
		mPaint.setColor(Green2);
		mPaint.setTextSize(20);
		canvas.drawText("-40", 0, yTop+5, mPaint);
		canvas.drawText("-80", 0, yMiddle+5, mPaint);
		mPaint.setColor(Color.RED);
		canvas.drawText("-120", 0, yBottom+5, mPaint);
		
		mPaint.setColor(Green3);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(1);
		mPaint2.setColor(Green3);
		mPaint2.setStyle(Style.STROKE);
		mPaint2.setStrokeWidth(2);

//		MMCLogger.d("Leon", "pPosition value="+pPosition[0]);
//		for (int i = 0; i < pPosition.length; i++) {
//			pPosition[i] = pPosition[i]+yMiddle;
//		}
		
		path.moveTo(xRight, pPosition[pPosition.length-1]+yMiddle);
		for (int i = 1; i < xLength; i++) {
			float yValue = pPosition[xLength-1-i];
			float yValue_raw = pPosition_raw[xLength-1-i];
			int yValue_raw2 = (int)((yValue_raw*-1)-80);
			int iColor = signalColor(yValue_raw2) + 0xff000000;
			mPaint2.setColor(iColor); 
//			MMCLogger.d("Leon", "("+yValue+","+yValue2+","+iColor+")("+yValue3+","+yValue4+","+iColor2+")");
			path.lineTo(xRight-i, yValue+yMiddle);
			canvas.drawPath(path, mPaint2);
			path = new Path();
			path.moveTo(xRight-i, yValue+yMiddle);
		}
		
//		canvas.drawPath(path, mPaint);
		
		//draw event icon
		/*
		Bitmap icon_PhoneCall    = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_connect);
		Bitmap icon_Voice        = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_novoice);
		Bitmap icon_RegainVoice  = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_regainvoice);
		Bitmap icon_LowSignal    = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_lowsignal);
		Bitmap icon_RegainSignal = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_regainsignal);
		Bitmap icon_DroppedCall  = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_droppedcall);
		Bitmap icon_Lost3G       = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_lost3g);
		Bitmap icon_Regain3G     = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_regain3g);
		Bitmap icon_LostData     = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_novoice);
		Bitmap icon_RegainData   = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_regainvoice);
		Bitmap icon_CellChange   = BitmapFactory.decodeResource(this.getResources(),R.drawable.blank);
		Bitmap icon_Lost4G       = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_lost4g);
		Bitmap icon_TestSpeed    = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_update_speed_good);
		Bitmap icon_UpdateLoc    = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_update_speed_good);
		
		Bitmap iconArray[]={
				icon_PhoneCall  ,
				icon_Voice      ,icon_RegainVoice,
				icon_LowSignal  ,icon_RegainSignal,
				icon_DroppedCall,
				icon_Lost3G     ,icon_Regain3G,
				icon_LostData   ,icon_RegainData,
				icon_CellChange ,icon_Lost4G,
				icon_TestSpeed  ,icon_UpdateLoc
		};
		*/
		for (int i = 0; i < eventArray.length; i++) {
			for (int j = 0; j < 15; j++) {
				if(eventArray[eventArray.length-1-i]!=0){
					isDown++;
					if(xLeft+(eventArray.length-1-i)-icon_halflength <= icon_boundary){
						if(eventArray[(eventArray.length-1-i)] %2 ==1){
							if(isDown%2==0){
								if(j==10) {  //cell change
									mPaint.setStrokeWidth(2);
									mPaint.setColor(Color.BLACK);
									canvas.drawLine(xLeft+(eventArray.length-1-i)-5, yTop+2, xLeft+(eventArray.length-1-i)+5, yTop+2, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i)-5, yTop+2, xLeft+(eventArray.length-1-i), yTop+2+5, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+5, xLeft+(eventArray.length-1-i)+5, yTop+2, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+5, xLeft+(eventArray.length-1-i), yTop+2+10, mPaint);
//									canvas.drawBitmap(iconArray[j], xLeft+(eventArray.length-1-i)-5, yTop+2, mPaint);
//									mPaint.setColor(Color.LTGRAY);
//									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+10, xLeft+(eventArray.length-1-i), pPosition[(eventArray.length-1-i)], mPaint);
								}
								else {
									//draw event icon
									//canvas.drawBitmap(iconArray[j], xLeft+(eventArray.length-1-i)-icon_halflength, yTop-20, mPaint);
									//draw event gray line
									mPaint.setColor(Color.LTGRAY);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop-20+icon_halflength*2, xLeft+(eventArray.length-1-i), pPosition[(eventArray.length-1-i)]+yMiddle, mPaint);
								}
							}
							else {
								if(j==10) {  //cell change
									mPaint.setStrokeWidth(2);
									mPaint.setColor(Color.BLUE);
									canvas.drawLine(xLeft+(eventArray.length-1-i)-5, yTop+2, xLeft+(eventArray.length-1-i)+5, yTop+2, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i)-5, yTop+2, xLeft+(eventArray.length-1-i), yTop+2+5, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+5, xLeft+(eventArray.length-1-i)+5, yTop+2, mPaint);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+5, xLeft+(eventArray.length-1-i), yTop+2+10, mPaint);
//									canvas.drawBitmap(iconArray[j], xLeft+(eventArray.length-1-i)-5, yTop+2, mPaint);
//									mPaint.setColor(Color.LTGRAY);
//									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop+2+10, xLeft+(eventArray.length-1-i), pPosition[(eventArray.length-1-i)], mPaint);
								}
								else {
									//draw event icon
									//canvas.drawBitmap(iconArray[j], xLeft+(eventArray.length-1-i)-icon_halflength, yTop-30, mPaint);
									//draw event gray line
									mPaint.setColor(Color.LTGRAY);
									canvas.drawLine(xLeft+(eventArray.length-1-i), yTop-30+icon_halflength*2, xLeft+(eventArray.length-1-i), pPosition[(eventArray.length-1-i)]+yMiddle, mPaint);
								}
							}
							
						}
						eventArray[(eventArray.length-1-i)] = eventArray[(eventArray.length-1-i)] >> 1;
					}
					else {
						eventArray[(eventArray.length-1-i)]=0;
					}
					
				}
				else break;
				
			}
//			isDown=0;
		}
		
		
		isDown=0;

	}

	
}
