package com.cortxt.app.mmcutility.DataObjects.beans;

import android.graphics.drawable.Drawable;

public class CPUStatBean {
	private String pid;
	private String packageName;
	private String appName;
	private Drawable icon;
	private String cpuUsage;

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(String cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

    @Override
    public String toString() {
        return "CPUStatBean{" +
                "pid='" + pid + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", icon=" + icon +
                ", cpuUsage='" + cpuUsage + '\'' +
                '}';
    }
}
