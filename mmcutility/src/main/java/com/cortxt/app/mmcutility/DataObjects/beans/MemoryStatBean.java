package com.cortxt.app.mmcutility.DataObjects.beans;

import android.graphics.drawable.Drawable;

public class MemoryStatBean {
	private String pid;
	private String packageName;
	private String appName;
	private Drawable icon;
	private String memoryUsage;

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

	public String getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(String memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

    @Override
    public String toString() {
        return "MemoryStatBean{" +
                "pid='" + pid + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", icon=" + icon +
                ", memoryUsage='" + memoryUsage + '\'' +
                '}';
    }
}
