package com.example.notificationpoc.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApplicationInfo extends Activity {
	public Boolean isInForeground() {
		ActivityManager am = (ActivityManager)getSystemService(android.content.Context.ACTIVITY_SERVICE);
		// The first in the list of RunningTasks is always the foreground task.
		RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
		
		String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
		PackageManager pm = getPackageManager();
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
