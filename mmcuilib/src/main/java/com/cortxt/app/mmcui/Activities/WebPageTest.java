package com.cortxt.app.mmcui.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.DataObjects.EventType;
import com.cortxt.app.mmcutility.Utils.CommonIntentActionsOld;
import com.cortxt.app.mmcutility.Utils.CommonIntentBundleKeysOld;
import com.cortxt.app.mmcutility.Utils.TaskHelper;
import com.cortxt.com.mmcextension.EventTriggers.SpeedTestTrigger;
import com.cortxt.com.mmcextension.EventTriggers.VideoTestTrigger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class WebPageTest extends MMCActiveTest {

	private TextView textBufferProg, textStalls, textDuration;
	//private ProgressBar mPlayProgress;
    private WebView webView;
    private boolean bAnimating = false;
    private int prevPlayProgress = 0;

	private static final String TAG = WebPageTest.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view  = inflater.inflate(R.layout.vwebpage_test, null, false);
		this.setContentView(view);
        init ();
		//getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);

		MMCActivity.customizeTitleBar (this,view,R.string.eventtype_webpageTest, R.string.eventtype_webpageTest);

		textBufferProg = (TextView)view.findViewById(R.id.textBufferProg);
		textStalls = (TextView)view.findViewById(R.id.textStalls);
		textDuration = (TextView)view.findViewById(R.id.textDuration);
		mBufferProgress = (ProgressBar)view.findViewById(R.id.buffer_progress);

		if (trigger > 0) {
			findViewById(R.id.startButton).setVisibility(View.GONE);
			findViewById(R.id.TestInProgressButton).setVisibility(View.VISIBLE);
		}

        webView = (WebView)view.findViewById(R.id.webtest_webview);
		VideoTestTrigger.setWebView(webView);
        setShareText();
		eventType = EventType.WEBPAGE_TEST.getIntValue();

	}

    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

    @Override
    protected void startTest ()
    {
        Intent intent = new Intent(CommonIntentActionsOld.ACTIVE_TEST);
        intent.putExtra(CommonIntentBundleKeysOld.EXTRA_SPEED_TRIGGER, 0);
        intent.putExtra(CommonIntentBundleKeysOld.EXTRA_TEST_TYPE, EventType.WEBPAGE_TEST.getIntValue());
        prevPlayProgress = 0;
        bAnimating = false;
        VideoTestTrigger.setWebView(webView);
        webView.clearView();
        setProgress(0, 0, 0, 0, 0, 0, 0,0);
        sendBroadcast(intent);
    }

	@Override
	public void shareClicked(View button) {
		temporarilyDisableButton(button);


			float density = getResources().getDisplayMetrics().density;
			int width = getResources().getDisplayMetrics().widthPixels;

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					String message;
					int customSocialText = (getResources().getInteger(R.integer.CUSTOM_SOCIALTEXT));
					String subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtest:R.string.sharemessagesubject_speedtest);

					int activeconn = ActiveConnection ();
					if(activeconn == 10)
					{
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_wifi:R.string.sharemessage_speedtest_wifi);
						subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtestwifi:R.string.sharemessagesubject_speedtestwifi);
					}
					else if (activeconn == 11)
					{
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_wimax:R.string.sharemessage_speedtest_wimax);
						subject = getString((customSocialText == 1)?R.string.sharecustomsubject_speedtestwimax:R.string.sharemessagesubject_speedtestwimax);
					}
					else
						message = getString((customSocialText == 1)?R.string.sharecustom_speedtest_poorspeed:R.string.sharemessage_speedtest_poorspeed);
					TaskHelper.execute(
							new ShareTask(WebPageTest.this, message, subject, findViewById(R.id.webtest_container))); // .execute((Void[])null);
				}
			}, 1);

		}
	//}

	private void setShareText() {
		String carrier = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
		int activeconn = ActiveConnection ();
		if (activeconn == 10)
			carrier = "WiFi";
		else if (activeconn == 11)
			carrier = "WiMAX";
		else if (activeconn == 12)
			carrier = "Ethernet";
		String phone = "";
		if(android.os.Build.BRAND.length() > 0) {
			String brand = android.os.Build.BRAND.substring(0, 1).toUpperCase() + android.os.Build.BRAND.substring(1);
			phone = " - " + brand + " " + android.os.Build.MODEL;
		}
		long timeStamp = System.currentTimeMillis();
		DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		String dateTime = dateTimeFormat.format(new Date(timeStamp));

		String shareText = carrier + phone + "\n" + dateTime;
		//mShareText.setText(shareText);
	}

	/**
	 * Set download speed on gauge and rotate needle
	 * @param downSpeed download speed in bits/second
	 */
	private void setGaugeDownloadSpeed(int downSpeed) {
	}

    @Override
	protected void setProgress (int _bufferProgress, int _playProgress, int stallCount, int _stallTime, int _accessDelay, int _playDelay, int _duration, int _downloadTime)
	{
		float bufferProgress, playProgress, accessDelay, playDelay, stallTime, duration, downloadTime;
		bufferProgress = (int)(_bufferProgress / 100) / 10f;
		playProgress = (int)(_playProgress / 100) / 10f;
		accessDelay = (int)(_accessDelay / 100) / 10f;
		playDelay = (int)(_playDelay / 100) / 10f;
		stallTime = (int)(_stallTime / 100) / 10f;
		duration = (int)(_duration / 100) / 10f;
		downloadTime = (int)(_downloadTime / 100) / 10f;

		textBufferProg.setText(""+bufferProgress + "%");
		textStalls.setText("Access delay: " + accessDelay + "   " + "Load delay: " + playDelay);
		String durationLine = getString(R.string.activetest_buffertime) + ": " + downloadTime;
		textDuration.setText(durationLine);

		if (duration > 0)
		{
			int percentBuffer = _bufferProgress * 100 / _duration;
			int percentPlay = _playProgress * 100 / _duration;
			
			mBufferProgress.setProgress(percentBuffer);
		}
		
	}

    @Override
    protected void handleTestProgress (Intent intent)
    {

        if(intent.hasExtra(CommonIntentBundleKeysOld.EXTRA_BUFFER_PROGRESS)) {
            int bufferProgress = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_BUFFER_PROGRESS, 0);
            int playProgress = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_PLAY_PROGRESS, 0);
            int stallCount = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_STALLS, 0);
            int stallTime = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_STALL_TIME, 0);
			int downloadTime = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_DOWNLOAD_TIME, 0);
            int duration = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_DURATION, 0);
            int accessDelay = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_ACCESS_DELAY, 0);
            int playDelay = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_PLAY_DELAY, 0);
            int eventtype = intent.getIntExtra(CommonIntentBundleKeysOld.EXTRA_EVENTTYPE, 0);
            if (eventtype != EventType.WEBPAGE_TEST.getIntValue())
                return;
            setProgress(bufferProgress, playProgress, stallCount, stallTime, accessDelay, playDelay, duration, downloadTime);

        }
        else {


        }
    }

    @Override
    protected void handleTestComplete ()
    {
        //gifView.clear();
        mBufferProgress.setProgress(100);
        textBufferProg.setText(R.string.GenericText_Done);

    }

    @Override
    protected void handleTestError ()
    {
        //gifView.clear();
    }
//
//    public class PausableScaleAnimation extends ScaleAnimation {
//
//        private long mElapsedAtPause=0;
//        public boolean mPaused=false;
//
//        public PausableScaleAnimation(float fromX, float toX, float fromY, float toY, int pivotXType, float pivotXValue, int pivotYType,
//                                      float pivotYValue) {
//            super(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue);
//        }
//
//        @Override
//        public boolean getTransformation(long currentTime, Transformation outTransformation) {
//            if(mPaused && mElapsedAtPause==0) {
//                mElapsedAtPause=currentTime-getStartTime();
//            }
//            if(mPaused)
//                setStartTime(currentTime-mElapsedAtPause);
//            return super.getTransformation(currentTime, outTransformation);
//        }
//
//        public void pause() {
//            mElapsedAtPause=0;
//            mPaused=true;
//        }
//
//        public void resume() {
//            mPaused=false;
//        }
//    }

	
}
 