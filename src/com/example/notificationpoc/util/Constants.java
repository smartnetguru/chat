package com.example.notificationpoc.util;

public class Constants {
	public final static String DB_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String UI_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
	
	public final static String DATE_ONLY_FORMAT = "yyyy-MM-dd";
	public final static String UI_TIME_ONLY_FORMAT = "HH:mm";
	
	public enum MessageSendingState {
		NONE,
		SENDING_FAILED,
		SENDING,
		DELIVERED
	}
}
