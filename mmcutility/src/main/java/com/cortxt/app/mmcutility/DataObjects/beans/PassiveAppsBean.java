package com.cortxt.app.mmcutility.DataObjects.beans;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class PassiveAppsBean {
	private int mAppUid;
	private String appPkgName;
	private long currentBytes;
	private long modifiedTime;
	private long startTime;
	private boolean firstEntry;
	private long oldBytes;
	private int id;
	private Context context;
	private String filename;
	private int importance;
	
	public PassiveAppsBean(Context newContext) {
		this.context = newContext;
	}
	
	public int getAppUid() {
		return mAppUid;
	}
	
	public void setAppUid(int appUid) {
		this.mAppUid = appUid;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public void setFileName(String newFileName) {
		this.filename = newFileName;
	}
	
	public void setImportance(int newImportance) {
		this.importance = newImportance;
	}
	
	public int getImportance() {
		return importance;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int newId) {
		this.id = newId;
	}
	
	public long getOldBytes() {
		return oldBytes;
	}
	
	public void setOldBytes(long newBytes) {
		this.oldBytes = newBytes;
	}
	
	public boolean getFirstEntry() {
		return firstEntry;
	}
	
	public void setFirstEntry(boolean newFirstStatus) {
		this.firstEntry = newFirstStatus;
	}
	
	public long getModifiedTime() {
		return modifiedTime;
	}
	
	public void setModifiedTime(long newTime) {
		this.modifiedTime = newTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(long newTime) {
		this.startTime = newTime;
	}

	public String getAppPkgName() {
		return appPkgName;
	}

	public void setAppPkgName(String appPkgName) {
		this.appPkgName = appPkgName;
	}

	public long getCurrentBytes() {
		return currentBytes;
	}

	public void setCurrentBytes(long received) {
		this.currentBytes = received;
	}	
	
	public String getAppName() {
		if(appPkgName.equals("Media"))
			return appPkgName;
		else
			return getNameFromPackage(appPkgName);
	}

    public String beanToString(long startTime) {     
    	startTime = startTime / 1000;
    	if(getId() == 3 && getCurrentBytes() > 0)
    		return "Media:" + (modifiedTime-startTime) + "," + currentBytes;
    	else if(getCurrentBytes() > 0)
    		return getNameFromPackage(appPkgName) + ":" + (modifiedTime-startTime) + "," + currentBytes;
    	else
    		return "";
    }
    
    public String initialBeanToString() {      
    	if(getId() == 3)
    		return "Media:" + startTime + "," + oldBytes;
    	else
    		return getNameFromPackage(appPkgName)  + " start:" + startTime + "," + oldBytes;
    }
    
    private String getNameFromPackage(String packageName) {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo = null;
					
		//convert package name to application name			
		try {	
			applicationInfo = packageManager.getApplicationInfo(packageName, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : packageName);
	}
    
    public String finalBeanToString() {      
    	if(getId() == 3)
    		return "Media:" ;
    	else
    		return getNameFromPackage(appPkgName);
    }
}
