package com.example.notificationpoc.entities;

public class notifications {
	public int Id;
	public String GUID;
	
	@com.google.gson.annotations.SerializedName("channel")
	public String mRegistrationId;

	public String getRegistrationId() {
	    return mRegistrationId;
	}
	
	public final void setRegistrationId(String registrationId) {
	    mRegistrationId = registrationId;
	}
	
	public String text;
	public boolean complete;
	@com.google.gson.annotations.SerializedName("cr_time")
	public String time;
}
