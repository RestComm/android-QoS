package com.cortxt.app.mmcutility.DataObjects.beans;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;


public class DataStatsBean {
	private int mAppUid;
	private String appPkgName;
	//private long currentRX, wifiRX, cellRX;
	//private long currentTX, wifiTX, cellTX;
	
	private double sent;
	private double wifiSent;
	private double cellularSent;
	private double received;
	private double wifiReceived;
	private double cellularReceived;
	private double existedSent;
	private double existedReceived;
	
	private long modifiedTime;
	private long startTime;
	private boolean firstEntry;
	private long oldBytes;
	private int id;
	private Context context;
	private String filename;
	private int importance;
	private Drawable icon;
	
	public DataStatsBean(Context newContext) {
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

	public double getSent() {
		return sent;
	}

	public void setSent(double sent) {
		this.sent = sent;
	}
	
	// Combine this bean read from TrafficStats with last bean read from the database
	// return true if bytes were added
	public boolean addToBean(DataStatsBean beanDB, boolean bWifi) {
		double newBytes = getReceived() - beanDB.getReceived();
		
		boolean bReceived = true, bSent = true;
		if (newBytes > 0)
		{
			if (newBytes > 0 && bWifi)
				setWifiReceived (beanDB.wifiReceived + newBytes);
			else if (newBytes > 0)
				setCellularReceived (beanDB.cellularReceived + newBytes);
			else
				bReceived = false;
		}
		
		newBytes = getSent() - beanDB.getSent();
		if (newBytes > 0)
		{
			if (newBytes > 0 && bWifi)
				setWifiSent (beanDB.wifiSent + newBytes);
			else if (newBytes > 0)
				setCellularSent (beanDB.cellularSent + newBytes);
			else
				bSent = false;
		}
		
		return bSent || bReceived;
	}
	
	public void addReceived(double newbytes, DataStatsBean dataBean, boolean bWifi) {
		if (bWifi)
			this.wifiReceived = this.wifiReceived + newbytes;
		else
			this.cellularReceived = this.cellularReceived + newbytes;
	}

	public double getWifiSent() {
		return wifiSent;
	}

	public void setWifiSent(double wifiSent) {
		this.wifiSent = wifiSent;
	}

	public double getCellularSent() {
		return cellularSent;
	}

	public void setCellularSent(double cellularSent) {
		this.cellularSent = cellularSent;
	}

	public double getReceived() {
		return received;
	}

	public void setReceived(double received) {
		this.received = received;
	}	

	public double getWifiReceived() {
		return wifiReceived;
	}
	
	public void setWifiReceived(double wifiReceived) {
		this.wifiReceived = wifiReceived;
	}

	public double getCellularReceived() {
		return cellularReceived;
	}

	public void setCellularReceived(double cellularReceived) {
		this.cellularReceived = cellularReceived;
	}
	
	
	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}	
	
	public String getAppName() {
		if(appPkgName.equals("Media"))
			return appPkgName;
		else
			return getNameFromPackage(appPkgName);
	}

    public String beanToString(long startTime) {     
    	startTime = startTime / 1000;
    	if(getId() == 3 && getReceived() > 0)
    		return "Media:" + (modifiedTime-startTime) + "," + received;
    	else if(getReceived() > 0)
    		return getNameFromPackage(appPkgName) + ":" + (modifiedTime-startTime) + "," + received;
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
