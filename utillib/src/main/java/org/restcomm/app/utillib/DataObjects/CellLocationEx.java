/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.utillib.DataObjects;

import java.lang.reflect.Method;

import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.json.JSONObject;

/**
 * This object merely stores the cell Id along with the timestamp at which the cell id was recorded.
 * @author abhin
 */
public class CellLocationEx {
	private CellLocation cellLoc;
	private long cellIdTimestamp;
	private static Method getPscMethodPointer;
	private static final String TAG = CellLocationEx.class.getSimpleName();
	private int bsCode = -1;
	private String netType;
	
	public String getNetType() {
		return netType;
	}
	
	public CellLocation getCellLocation(){
		return cellLoc;
	}
	
	public int getBSLow(){
		if(cellLoc != null && cellLoc instanceof CdmaCellLocation) {
			return ((CdmaCellLocation) cellLoc).getBaseStationId();
		}
		else if(cellLoc != null && cellLoc instanceof GsmCellLocation) {
			return ((GsmCellLocation) cellLoc).getCid() & 0xffff;
		}
	
		return 0;
	}
	public int getBSMid(){
		if(cellLoc != null && cellLoc instanceof CdmaCellLocation) {
			return ((CdmaCellLocation) cellLoc).getNetworkId();
		}
		else if(cellLoc != null && cellLoc instanceof GsmCellLocation) {
			return ((GsmCellLocation) cellLoc).getCid() >> 16;
		}
	
		return 0;
	}
	public int getBSHigh(){
		if(cellLoc != null && cellLoc instanceof CdmaCellLocation) {
			return ((CdmaCellLocation) cellLoc).getSystemId();
		}
		else if(cellLoc != null && cellLoc instanceof GsmCellLocation) {
			return ((GsmCellLocation) cellLoc).getLac();
		}
	
		return 0;
	}
	public int getBSCode () {


		if(cellLoc != null && cellLoc instanceof CdmaCellLocation) {
			return 0;
		}
		else if(cellLoc != null && cellLoc instanceof GsmCellLocation) {
			bsCode = getPsc((GsmCellLocation) cellLoc); // ((GsmCellLocation) cellLoc).getLac();
		}

		try {
			if (bsCode <= 0) {
				JSONObject serviceMode = PhoneState.getServiceMode();
				if (serviceMode != null && serviceMode.getLong("time") + 20000 > System.currentTimeMillis()) {
					if (serviceMode.has("psc") && serviceMode.getString("psc").length() > 1) {
						int svc_psc = Integer.parseInt(serviceMode.getString("psc"), 10);
						if (svc_psc > 0) {
							bsCode = svc_psc;
						}
					}
					if (serviceMode.has("pci") && serviceMode.getString("pci").length() > 1) {
						int svc_psc = Integer.parseInt(serviceMode.getString("pci"), 10);
						if (svc_psc > 0) {
							bsCode = svc_psc;
						}
					}
				}
			}

		}
		catch (Exception e) {}

		return bsCode;

	}


	public void setNetType(String netType) {
		this.netType = netType;
	}	
	
	public void setCellLocation(CellLocation cellLoc){
		this.cellLoc = cellLoc;
	}

	public long getCellIdTimestamp() {
		return cellIdTimestamp;
	}
	public void setCellIdTimestamp(long cellIdTimestamp) {
		this.cellIdTimestamp = cellIdTimestamp;
	}	
	public CellLocationEx(CellLocation cellLoc, long cellIdTimestamp){
		this.cellIdTimestamp = cellIdTimestamp;
		this.cellLoc = cellLoc;
	}
	public CellLocationEx(CellLocation cellLoc){
		this.cellLoc = cellLoc;
		this.cellIdTimestamp = System.currentTimeMillis();
	}

	public String toString ()
	{
		String str = "";
		if (this.cellLoc != null)
		{
			return this.cellLoc.toString();
		}	
		return "null";
	}
	
	/**
	 * This method uses reflection to get the PSC of the network if the API level of the phone
	 * supports that method. This round-about method has to be used because the minimum SDK level
	 * of this application is 7 and the Primary Scrambling Code can only be acquired for API level 9
	 * onwards.
	 * @return
	 */
	public static int getPsc(GsmCellLocation gsmCellLocation){
		int returnValue = -1;
		try {
			getPscMethodPointer = GsmCellLocation.class.getMethod("getPsc", (Class[]) null);
		} catch (SecurityException e) {
			Log.d(TAG, "Not enough permissions to access Primary Scrambling Code");
		} catch (NoSuchMethodException e) {
			Log.d(TAG, "API version not high enough to access Primary Scrambling Code");
		}
		
		if (getPscMethodPointer != null){
			//now we're in business!
			try {
				returnValue = (Integer) getPscMethodPointer.invoke(gsmCellLocation, (Object[]) null);
			} catch (Exception e) {
				Log.d(TAG, "Could not get the Primary Scrambling Code", e);
			}
		}
		
		return returnValue;
	}
}
