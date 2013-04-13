package com.example.notificationpoc;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(sendReportsInDevMode=true, mailTo="ilozic@gmail.com", formKey = "")
public class MyApplication extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		ACRA.init(this);
	}
}
