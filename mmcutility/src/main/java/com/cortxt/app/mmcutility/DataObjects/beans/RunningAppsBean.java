package com.cortxt.app.mmcutility.DataObjects.beans;

public class RunningAppsBean {
	private String appPkgName;
	private String appName;
	//private long currentTime;
	private long startedTime;
	private long activeTime;
	private double wifiSent, wifiSentLast;
	private double cellularSent, cellularSentLast;
	private double cellularReceived, cellularReceivedLast;
	private double wifiReceived, wifiReceivedLast;
	
	public String getAppPkgName() {
		return appPkgName;
	}
	public void setAppPkgName(String appPkgName) {
		this.appPkgName = appPkgName;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
//	public long getCurrentTime() {
//		return currentTime;
//	}
//	public void setCurrentTime(long currentTime) {
//		this.currentTime = currentTime;
//	}
	public long getStartedTime() {
		return startedTime;
	}
	public void setStartedTime(long startedTime) {
		this.startedTime = startedTime;
	}
	public long getActiveTime() {
		return activeTime;
	}
	public void setActiveTime(long activeTime) {
		this.activeTime = activeTime;
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
	
	
	public double getWifiSentLast() {
		return wifiSentLast;
	}

	public void setWifiSentLast(double wifiSentLast) {
		this.wifiSentLast = wifiSentLast;
	}

	public double getCellularSentLast() {
		return cellularSentLast;
	}

	public void setCellularSentLast(double cellularSentLast) {
		this.cellularSentLast = cellularSentLast;
	}
	
	public double getWifiReceivedLast() {
		return wifiReceivedLast;
	}

	public void setWifiReceivedLast(double wifiReceivedLast) {
		this.wifiReceivedLast = wifiReceivedLast;
	}

	public double getCellularReceivedLast() {
		return cellularReceivedLast;
	}

	public void setCellularReceivedLast(double cellularReceivedLast) {
		this.cellularReceivedLast = cellularReceivedLast;
	}

//    @Override
//    public String toString() {
//        return "RunningAppsBean{" +
//                "appPkgName='" + appPkgName + '\'' +
//                ", appName='" + appName + '\'' +
//                ", currentTime=" + currentTime +
//                ", startedTime=" + startedTime +
//                ", totalTime=" + totalTime +
//                '}';
//    }
//    
//    public String toString(char placeHolder) {
//        return "," + currentTime + "," + startedTime + "," + totalTime;                 
//    }
}
