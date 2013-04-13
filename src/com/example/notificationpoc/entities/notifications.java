package com.example.notificationpoc.entities;

public class notifications {
	public int id;
	@com.google.gson.annotations.SerializedName("user_id")
	public int userId;
	
	public String text;
	@com.google.gson.annotations.SerializedName("cr_time")
	public String time;
}
