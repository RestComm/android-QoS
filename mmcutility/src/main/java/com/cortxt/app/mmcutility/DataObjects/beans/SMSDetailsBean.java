package com.cortxt.app.mmcutility.DataObjects.beans;

public class SMSDetailsBean {

	private int mSMSId;
	private int mSMSType;
	private String mSMSAddress;
	private String mSMSDate;
	
	public SMSDetailsBean() {
		mSMSId = -1;
		mSMSType = -1;
		mSMSAddress = null;
		mSMSDate = null;
	}
	
	public SMSDetailsBean(int sms_id, int sms_type, String sms_address, String sms_date){
		mSMSId = sms_id;
		mSMSType = sms_type;
		mSMSAddress = sms_address;
		mSMSDate = sms_date;
	}
	
	public int getSMSId(){
		return mSMSId;
	}

	public int getSMSType(){
		return mSMSType;
	}
	
	public String getSMSAddress(){
		return mSMSAddress;
	}
	
	public String getSMSDate(){
		return mSMSDate;
	}
	
	public void setSMSId(int sms_id){
		this.mSMSId = sms_id;
	}
	
	public void setSMSType(int sms_type){
		this.mSMSType = sms_type;
	}
	
	public void setSMSAddress(String sms_address){
		this.mSMSAddress = sms_address;
	}
	
	public void setSMSDate(String sms_date){
		this.mSMSDate = sms_date;
	}

    @Override
    public String toString() {
        return "SMSDetailsBean{" +
                "mSMSId=" + mSMSId +
                ", mSMSType=" + mSMSType +
                ", mSMSAddress='" + mSMSAddress + '\'' +
                ", mSMSDate='" + mSMSDate + '\'' +
                '}';
    }
}
