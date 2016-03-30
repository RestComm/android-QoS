package com.cortxt.app.mmcui.Activities.MyCoverage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import com.cortxt.app.mmcutility.Utils.MmcConstants;
import com.cortxt.app.mmcutility.Utils.PreferenceKeys;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * MapView that supports a change listener for panning and zooming
 * @author nasrullah
 *
 */
public class MMCMapView extends MapView implements OnGestureListener {
	
	protected GeoPoint mLastCenter; 
	protected int mLastZoomLevel;
	protected MMCMapView.OnChangeListener mChangeListener;
	protected MMCMapView.OnZoomLevelChangeListener mZoomLevelChangeListener;
	private long lastTouchTime = -1;
	private long startTime = 0;
	private float startX = 0, startY = 0;
	private float screenDensityScale = 0;
	private int mappingType = -1;
	//private ManualMapping manualMapping = null;
	private Context context;
	private CountDownTimer timer = null;
	private int count = 0;
	private GestureDetector gd;    
    private OnSingleTapListener singleTapListener;
    private OnDoubleTapListener doubleTapListener;
    private View.OnLongClickListener longClickListener;
	
	@SuppressLint("InlinedApi")
	public MMCMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		mLastCenter = (GeoPoint) getMapCenter();
		mLastZoomLevel = getZoomLevel()-2;
		this.setBuiltInZoomControls(true);
		screenDensityScale = context.getResources().getDisplayMetrics().density;	
		
		setupGestures();
		this.setSatellite(false);
		
		/*To fix bug: Path too large to be rendered into a texture 
		This is a problem with HW accelerated Paths - they have to be converted into Bitmaps to be uploaded as textures to OpenGL. 
		If the Path is too big, then the Bitmap is too big and it fails.*/
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)		
			setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
			//doens't appear to be necessary anymore with safeoverlays/drawsafe etc 
			//EDIT: overlays (compass, scale) are not drawn without this on
	}
		
	public void setChangeListener(MMCMapView.OnChangeListener mChangeListener) {
		this.mChangeListener = mChangeListener;
	}

	public void setZoomLevelChangeListener( MMCMapView.OnZoomLevelChangeListener mZoomLevelChangeListener) {
		this.mZoomLevelChangeListener = mZoomLevelChangeListener;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		boolean spanChanged = !getMapCenter().equals(mLastCenter);
		boolean zoomLevelChanged = getZoomLevel() != mLastZoomLevel;
		
		if (spanChanged || zoomLevelChanged) {
			if(mChangeListener != null) {
				mChangeListener.onChange();
			}
			
			if(zoomLevelChanged) {
				if(mZoomLevelChangeListener != null) {
					mZoomLevelChangeListener.onZoomLevelChange();
				}
			}
			
			mLastCenter = (GeoPoint) getMapCenter();
			mLastZoomLevel = getZoomLevel();
		}
	}
	
	/**
	 * Interface definition for callback to be invoked when the MapView is changed
	 * @author nasrullah
	 *
	 */
	public interface OnChangeListener {
		/**
		 * Called when the MapView is changed, when the user pans or zooms.
		 * NOTE: may be called back many times for each time the user pans or zooms
		 */
		public void onChange();
	}
	
	/**
	 * Interface definition for callback to be invoked when the MapView's zoom level is changed
	 * @author nasrullah
	 *
	 */
	public interface OnZoomLevelChangeListener {
		/**p
		 * Called when the MapView's zoom level is changed.
		 */
		public void onZoomLevelChange();
	}
   
/*	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {		
		
		System.out.println("onInterceptTouchEvent " + ev.getAction());
		
		if(mappingType != -1) {
			try {
				return super.onInterceptTouchEvent(ev);
			} catch (Exception e) {
				return false;
			}			
		}
		
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			startTime = System.currentTimeMillis();
			if (startTime - lastTouchTime < ViewConfiguration.getDoubleTapTimeout()) {
				// Double tap	     
//				System.out.println("zoom level " +getZoomLevel());
				if (getZoomLevel() >= 20) {
					//zoom out
					GeoPoint geoPoint = (GeoPoint) this.getMapCenter();
					this.getController().setZoom(14);
					this.getController().setCenter(geoPoint);
				}
				else {
					//zoom in
					GeoPoint geoPoint = (GeoPoint) this.getMapCenter();
					this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
					this.getController().setCenter(geoPoint);
				}
				lastTouchTime = -1;
			} else {
				// Too slow :)
				lastTouchTime = startTime;
			}
		}
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}	
	}	*/
    
/*	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		
		System.out.println("onTouchEvent " + ev.getAction());
		
		if(manualMapping == null)	{
			try {
				return super.onInterceptTouchEvent(ev);
			} catch (Exception e) {
				return false;
			}	
		}
		
		if(ev.getAction() == MotionEvent.ACTION_UP && timer != null) {
			if(timer != null)
				timer.cancel();
			manualMapping.getMappingOverlay().removeTapAnimation();
			count = 0;
		}
		
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if(timer != null)
					timer.cancel();
			manualMapping.getMappingOverlay().removeTapAnimation();
			count = 0;
			return super.onTouchEvent(ev);
		}
		
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			startTime = System.currentTimeMillis();	
			startX = ev.getX();
			startY = ev.getY();			
		}
		
		if(ev.getAction() == MotionEvent.ACTION_DOWN && mappingType == MmcConstants.MANUAL_SEARCHING) {			
			if(Math.abs(startX-ev.getX()) < 20 && Math.abs(startY-ev.getY()) < 20) {
				drawCircle(ev, ++count);
							
				timer = new CountDownTimer(500, 100) {
					@Override
					public void onTick(long millisUntilFinished) {
						drawCircle(ev, ++count);
						System.out.println("count tap: " + count);
					}
	
					@Override
					public void onFinish() { 
						manualMapping.getMappingOverlay().removeTapAnimation();
						count = 0;
					}
					
				}.start();
			}		
		}
		
		if(ev.getAction() == MotionEvent.ACTION_UP && mappingType == MmcConstants.MANUAL_SEARCHING) {	
			//longClick	to choose a building
			if(System.currentTimeMillis() - startTime > 500 && Math.abs(startX-ev.getX()) < 20 && Math.abs(startY-ev.getY()) < 20) { 	
				GeoPoint geoPoint = (GeoPoint) this.getProjection().fromPixels((int)ev.getX(),(int)ev.getY());
				double latitude = (double) (geoPoint.getLatitudeE6()/1E6);
				double longitude = (double) (geoPoint.getLongitudeE6()/1E6);
			
				if(manualMapping.checkAccuracy(geoPoint)) {						
					if(manualMapping.getMappingOverlay().returnAsyncTaskStatus() != AsyncTask.Status.RUNNING)
						manualMapping.getMappingOverlay().requestPolygon(latitude, longitude);
					else
						Toast.makeText(context, context.getString(R.string.manualmapping_still_searching), Toast.LENGTH_SHORT).show();
				} 
				else {
					Toast.makeText(context, context.getString(R.string.manualmapping_outofrange_polygon), Toast.LENGTH_SHORT).show();
				}								
			}
		}
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}	
	}	*/

	private void setupGestures() {
        gd = new GestureDetector(this);  
       
        //set the on Double tap listener  
        gd.setOnDoubleTapListener(new OnDoubleTapListener() {
 
	        @Override
	        public boolean onSingleTapConfirmed(MotionEvent e) {
	            final MapController mc = MMCMapView.this.getController();
                if (singleTapListener != null) {
//                	System.out.println("onSingleTapConfirmed, event: " + e.getAction());
                	return singleTapListener.onSingleTap(e);
                } else {
                	return false;
                }
	        }
	 
	        @Override
	        public boolean onDoubleTap(MotionEvent e) {
	        	MMCMapView.this.getController().zoomInFixing((int) e.getX(), (int) e.getY());
//	        	System.out.println("onDoubleTap, event: " + e.getAction());
	        	return false;
	//        	if(doubleTapListener != null) {
	//        		return doubleTapListener.onDoubleTap(e);
	//        	}
	//        	else{
	//        		return false;
	//        	}
	        }
	 
	        @Override
	        public boolean onDoubleTapEvent(MotionEvent e) {
//	        	System.out.println("onDoubleTapEvent, event: " + e.getAction());
	        	return false;
	        }       
        });
    }
   
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
		return super.onTouchEvent(ev);

    }
       
    public void setOnSingleTapListener(OnSingleTapListener singleTapListener) {
    	this.singleTapListener = singleTapListener;
    }
    
    public void setOnDoubleTapListener(OnDoubleTapListener doubleTapListener) {
    	this.doubleTapListener = doubleTapListener;
    }
  
    public void setOnLongClickListener(View.OnLongClickListener longlistener) {
    	this.longClickListener = longlistener;
    }
    
    MotionEvent downEvent = null;
    private float downX = 0, downY = 0;
    @Override
    public boolean onDown(MotionEvent e) {
    	downX = e.getX();
    	downY = e.getY();
//    	System.out.println("onDown, event: " + e.getAction());
    	return false;
    }
 
    @Override
    public void onShowPress(MotionEvent e) {
//    	System.out.println("onShowPress, event: " + e.getAction());
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e) {

    	return false;
    }
 
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//    	System.out.println("onScroll, event: " + e1.getAction() + " event2: " + e2.getAction());
    	return false;
    }
 
    @Override
    public void onLongPress(MotionEvent e) {
//    	System.out.println("onLongPress, event: " + e.getAction());
    	
    }
 
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//    	System.out.println("onFling, event: " + e1.getAction() + " event2: " + e2.getAction());
	return false;
    }
    
    public interface OnSingleTapListener {
    	public boolean onSingleTap(MotionEvent e);
    }
}
