package com.example.notificationpoc.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApplicationInfo {
	private Context context;
	
	public ApplicationInfo(Context ctx) {
		this.context = ctx;
	}
	
	public Boolean isInForeground() {
		ActivityManager am = (ActivityManager)context.getSystemService(android.content.Context.ACTIVITY_SERVICE);
		// The first in the list of RunningTasks is always the foreground task.
		RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
		
		String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
		PackageManager pm = context.getPackageManager();
		PackageInfo foregroundAppPackageInfo;
		try {
			foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
			String foregroundPackageName = foregroundAppPackageInfo.packageName;
			
			return foregroundPackageName.compareTo("com.example.notificationpoc") == 0;
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
