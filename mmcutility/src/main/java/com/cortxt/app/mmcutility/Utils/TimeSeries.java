package com.cortxt.app.mmcutility.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a series that can be shown on the chart.
 * This type of series has to have 2 dimensions, of which one is
 * time and the other is a an arbitrary data-type. Each data point 
 * in this series is represented using the TimeDataPoint class.
 */
public class TimeSeries<E> implements Iterable<TimeDataPoint<E>>, Serializable{
	/**
	 * A generated serial UID for serialization
	 */
	private static final long serialVersionUID = 1004394507096858366L;
	public static final String TAG = TimeSeries.class.getSimpleName();
	public static final int DEFAULT_EXPIRY_TIMEOUT = 4*3600*1000; // 240000;	//default expiry timeout is 4 minutes
	/**
	 * The name of the series.
	 */
	private String name;
	/**
	 * The series itself
	 */
	private ArrayList<TimeDataPoint<E>> series;
	/**
	 * The largest value in y-axis that can be represented by this series.
	 */
	//private E maxValue;
	//private E minValue;
	/**
	 * This is the number of milliseconds that marks the timeout of each Time data point.
	 * If the time (in milliseconds) since the data point was added to the timeseries
	 * becomes greater than this timeout, then the data point is automatically deleted
	 * from the series.
	 */
	private int expiryTimeout;
	
	public TimeSeries(E minValue, E maxValue){
		this(5, "", minValue, maxValue);
	}
	public TimeSeries(String name, E minValue, E maxValue){
		this(5, name, minValue, maxValue);
	}
	public TimeSeries(int capacity, E minValue, E maxValue){
		this(capacity, "", minValue, maxValue);
	}
	public TimeSeries(int capacity, String name, E minValue, E maxValue){
		this(capacity, name, minValue, maxValue, DEFAULT_EXPIRY_TIMEOUT);
	}
	public TimeSeries(int capacity, String name, E minValue, E maxValue, int expiryTimeout){
		series = new ArrayList<TimeDataPoint<E>>(capacity);
		this.name = name;
		//this.minValue = minValue;
		//this.maxValue = maxValue;
		this.expiryTimeout = expiryTimeout;
	}
	
	public int getDataPointCount(){
		return series.size();
	}
	
	public void removeLast ()
	{
		if (series.size() > 0){
			try
			{
				series.remove(series.size()-1);
			}catch (Exception e){}
		}
		
	}
	
	/**
	 * This method adds a new data point to the internal list of data points.
	 * If the timestamp of the new data point matches that of an older point,
	 * then the older point is over-written.
	 * If not, then the point is added to the series in decreasing older of 
	 * timestamp.
	 * @param dataPoint
	 */
	public void addDataPoint(TimeDataPoint<E> dataPoint){
		/*
		 * We assume that at any point of time, the elements in the internal list
		 * are already in decreasing order of timestamp. Therefore, we start 
		 * comparing timestamps at the beginning of the list and when we find an
		 * element with a smaller timestamp than the one of dataPoint, we place 
		 * dataPoint before it.
		 */
		for (int counter = 0; counter < series.size(); counter++){
			TimeDataPoint<E> currentPoint = series.get(counter);
			
			//if timestamps match, then replace
			if (currentPoint.getTimestamp() == dataPoint.getTimestamp()){
				series.set(counter, dataPoint);
				return;
			}
			
			//if the timestamp of currentPoint is smaller (older) than that
			//of dataPoint, then insert at the current index
			if (currentPoint.getTimestamp() < dataPoint.getTimestamp()){
				series.add(counter, dataPoint);
				return;
			}
		}
		
		
		//if the code reaches here, then just add the data point at the end of the list
		series.add(dataPoint);
		
		
		/*
		 * NOTE: im leaving this code in comments in case the new code below introduced some bugs that i havent noticed
		//check if the oldest element in the arraylist has expired
		TimeDataPoint<E> oldestElement = series.get(series.size() - 1);
		if (oldestElement.getTimestamp() < System.currentTimeMillis() - expiryTimeout){
			MMCLogger.d(TAG, String.format("Deleting an expired data point from the series; timestamp: %d", oldestElement.getTimestamp()));
			//the following code just removes the the last element in the series
			series.remove(series.size() - 1);
		}
		*/
		
		
		//deleteExpiredElements();
	}
	
	public void deleteExpiredElements() {
		//delete elements that have expired from the end of series
		for(int i=series.size()-1; i>=0; i--) {
			TimeDataPoint<E> lastElement = series.get(i);
			if(lastElement.getTimestamp() < System.currentTimeMillis() - expiryTimeout && lastElement.getTimestamp() > 10000000) {
				series.remove(i);
			}
			else {
				break;
			}
		}
	}

	public Iterator<TimeDataPoint<E>> iterator() {
		return series.iterator();
	}
	
	public boolean isEmpty(){
		return series.isEmpty();
	}
	
	public TimeDataPoint get(int index){
		return series.get(index);
	}
	
	
	/**
	 * Gets the expiry timeout of the series.
	 * This is the number of milliseconds that marks the timeout of each Time data point.
	 * If the time (in milliseconds) since the data point was added to the timeseries
	 * becomes greater than this timeout, then the data point is automatically deleted
	 * from the series.	
	 * @return expiry timeout of the series
	 */
	public int getExpiryTimeout() {
		return expiryTimeout;
	}
	
	/**
	 * Sets the expiry timeout of the series.
	 * This is the number of milliseconds that marks the timeout of each Time data point.
	 * If the time (in milliseconds) since the data point was added to the timeseries
	 * becomes greater than this timeout, then the data point is automatically deleted
	 * from the series.	
	 * @param expiryTimeout expiry timeout of the series
	 */
	public void setExpiryTimeout(int expiryTimeout) {
		this.expiryTimeout = expiryTimeout;
	}
	
	
	@Override
	public String toString() {
		String string = "{ ";
		
		for(int i=0; i<series.size(); i++) {
			string += series.get(i).toString() + ", ";
		}
		
		string += " }";
		
		return string;
	}

    public String toShortString() {
        String string = "";
        for(int i=0; i<series.size(); i++) {
            string += series.get(i).getTimestamp() + ", " + series.get(i).getData() + ", ";
        }
        return string;
    }
	

}
