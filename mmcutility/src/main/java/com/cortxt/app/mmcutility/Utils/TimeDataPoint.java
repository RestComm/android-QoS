package com.cortxt.app.mmcutility.Utils;

import java.io.Serializable;
import java.util.Locale;

import android.text.format.Time;

/**
 * This class represents a single point in a TimeSeries instance.
 * It is a combination of a timestamp and a an arbitrary data-type.
 * @author Abhin
 *
 */
public class TimeDataPoint<E> implements Serializable{
	private E data, data2, data3;
	private long timestamp;
	
	public TimeDataPoint(E data, E data2, long timestamp){
		this.data = data;
		this.data2 = data2;
		this.timestamp = timestamp;
	}
	public TimeDataPoint(E data, E data2, E data3, long timestamp){
		this.data = data;
		this.data2 = data2;
		this.data3 = data3;
		this.timestamp = timestamp;
	}
	
	public TimeDataPoint(E data, Time timestamp){
		this.data = data;
		this.timestamp = timestamp.toMillis(true);
	}
	
	public E getData(){
		return this.data;
	}
	
	public E getData2(){
		return this.data2;
	}
	public E getData3(){
		return this.data3;
	}
	public long getTimestamp(){
		return this.timestamp;
	}
	public void setData(E data){
		this.data = data;
	}
	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	public void setTimestamp(Time timestamp){
		this.timestamp = timestamp.toMillis(true);
	}
	
	public String toString(){
		try{
		return String.format(Locale.US,"[data: %f; data2: %f; timestamp: %d]", data, data2, timestamp);
		} catch (Exception e) {}
		return "";
	}



	public void setData2(E _data2) {
		this.data2 = _data2;
	}
}
