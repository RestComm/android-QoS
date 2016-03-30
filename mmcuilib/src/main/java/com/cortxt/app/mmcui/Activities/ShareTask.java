package com.cortxt.app.mmcui.Activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.cortxt.app.mmcui.R;
import com.cortxt.app.mmcutility.Reporters.ReportManager;
import com.cortxt.app.mmcutility.Utils.Global;

public class ShareTask extends AsyncTask<Void, Void, Boolean> {

	private Context mContext;
	private String mTextToShare;
	private String mSubject;
	private View mViewToScreenshot;
	private Bitmap mScreenshot;
	private static boolean useTwitter = false;
	private boolean isFromEventDetail=false;
	
	public ShareTask(Context context, String textToShare, String subject, View viewToScreenshot) {
		mContext = context;
		mTextToShare = textToShare;
		mSubject = subject;
		mViewToScreenshot = viewToScreenshot;
	}
	
	@Override
	protected void onPreExecute() {
		if (mViewToScreenshot != null)
		{
			mViewToScreenshot.buildDrawingCache();
			mScreenshot = mViewToScreenshot.getDrawingCache();
		}
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
		String appname = Global.getAppName(mContext);
		String fileName = "/" +appname.replace(" ", "") + ".png";
		if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) {
			fileName = Environment.getExternalStorageDirectory().toString() + fileName;  
		}
		else {
			fileName = (mContext.getApplicationContext()).getCacheDir().toString()  + fileName; 
		}
		
		if(fileName != null) {
			FileOutputStream fos = null;
			try {
				if (mScreenshot == null || mScreenshot.isRecycled())
					return false;
//				fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/mmc.png");
				fos = new FileOutputStream(fileName);
				if (fos == null)
					return false;
				mScreenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
				
				fos.close();
			}
			catch (FileNotFoundException e) {
				return false;
			}
			catch (Exception e) {
				return false;
			}
			ReportManager reportManager = ReportManager.getInstance(mContext);
			HashMap<String, String> carrier = reportManager.getDevice().getCarrierProperties();
			try { 
				if(mContext instanceof MMCTrackedActivityOld) 
				{
					MMCTrackedActivityOld trackedActivity = (MMCTrackedActivityOld)mContext;
					trackedActivity.trackEvent ("ShareTask", trackedActivity.getLocalClassName(), mSubject, 0);
				}
//				else if (mContext instanceof MMCTrackedMapActivityOld) 
//				{
//					MMCTrackedMapActivityOld trackedActivity = (MMCTrackedMapActivityOld)mContext;
//					trackedActivity.trackEvent ("ShareTask", trackedActivity.getLocalClassName(), "", 0);
//				}
					
				String twitterHandle = reportManager.getTwitterHandle(carrier);
				if (twitterHandle == null || useTwitter == false)
					twitterHandle = carrier.get("carrier");
				if (twitterHandle == null)
					twitterHandle = "my carrier";
				
				if (mTextToShare != null)
				{
					mTextToShare = mTextToShare.replaceAll("@CARRIERHANDLE", twitterHandle);
					if (!appname.equals("MyMobileCoverage"))
						mTextToShare = mTextToShare.replaceAll("#MyMobileCoverage", appname);
				}
				if (mSubject != null)
				{
					mSubject = mSubject.replaceAll("@CARRIERHANDLE", twitterHandle);
					if (!appname.equals("MyMobileCoverage"))
						mSubject = mSubject.replaceAll("#MyMobileCoverage", appname);
				}
			}
			catch (Exception e) {
				try{
				String name = carrier.get("carrier");
				mTextToShare = mTextToShare.replaceAll("@CARRIERHANDLE", name);
				mSubject = mSubject.replaceAll("@CARRIERHANDLE", name);
				if (!appname.equals("MyMobileCoverage"))
					mTextToShare = mTextToShare.replaceAll("#MyMobileCoverage", appname);
				if (!appname.equals("MyMobileCoverage"))
					mSubject = mSubject.replaceAll("#MyMobileCoverage", appname);
				} catch (Exception e1) 
				{}
			}			
			
			return true; 
		}
		else { 
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean succeded) {
		if(succeded) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			Integer shareTwitter = mViewToScreenshot.getResources().getInteger(R.integer.SHARE_TWITTER);
			if (shareTwitter == 0)
				intent.setType("message/rfc822");
			else
			{
				//intent.setType("text/plain");
				intent.setType("image/png");
			}
			intent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
			intent.putExtra(Intent.EXTRA_TEXT, mTextToShare);
			String appname = Global.getAppName(mContext);
			String filename = "/" +appname.replace(" ", "") + ".png";
					
			if ((Environment.getExternalStorageState().toString()).equals(Environment.MEDIA_MOUNTED)) {				
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + filename));
			}
			else {
			   
//			    Bitmap bitmap = mScreenshot;
//			    String path = mContext.getApplicationContext().getCacheDir().toString() + "/mmc.png";
//			    
//			    Uri screenshotUri = Uri.parse(path);
//			    intent.setType("image/png");
//			    intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		
//				File f = new File(path);
//				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
			    
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + (mContext.getApplicationContext()).getCacheDir() + filename));

			}		
			mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_title)));
		}
		else {
			Toast.makeText(mContext, R.string.share_error, Toast.LENGTH_LONG).show();
		}
		if(isFromEventDetail){
			EventDetail.hidefacebookTwitterLayout();
		}
		
	} 
	
}
