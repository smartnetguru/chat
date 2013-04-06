package com.example.notificationpoc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
	public String GetUIDate(Date dateTime) {
		if (DateTimeBeforeToday(dateTime)) {
			return new SimpleDateFormat(Constants.UI_DATE_TIME_FORMAT).format(dateTime).toString();
		} else {
			return new SimpleDateFormat(Constants.UI_TIME_ONLY_FORMAT).format(dateTime).toString();
		}
	}
	
	public String GetUIDate(String dateTime) {
		try {
			Date rawDate = new SimpleDateFormat(Constants.DB_DATE_TIME_FORMAT).parse(dateTime);
			return GetUIDate(rawDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String GetDBDate(Date dateTime) {
		return new SimpleDateFormat(Constants.DB_DATE_TIME_FORMAT).format(dateTime);
	}
	
	public Date Now() {
		return Calendar.getInstance().getTime();
	}
	
	private Boolean DateTimeBeforeToday(Date dateTime) {
		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.DAY_OF_YEAR, -1);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(dateTime);

		return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR));
	}
}
