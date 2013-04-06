package com.example.notificationpoc.util;

public class Constants {
	public class Application {
		public static final String SENDER_ID = "994169729957";
	}
	
	public class DateFormat {
		public final static String DB_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
		public final static String UI_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
		
		public final static String DATE_ONLY_FORMAT = "yyyy-MM-dd";
		public final static String UI_TIME_ONLY_FORMAT = "HH:mm";
	}
	
	public enum MessageSendingState {
		NONE,
		SENDING,
		DELIVERED,
		RETRYING
	}
	
	public class Events {
		public final static String START_LOADING = "com.example.notificationpoc.START_LOADING";
		public final static String STOP_LOADING = "com.example.notificationpoc.STOP_LOADING";
		public final static String DISPLAY_MESSAGE = "com.example.notificationpoc.DISPLAY_MESSAGE";
	}
}
