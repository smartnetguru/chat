package com.example.notificationpoc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.support.v4.app.NotificationCompat;

import com.example.notificationpoc.util.ApplicationInfo;
import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint("InlinedApi")
public class GCMIntentService extends GCMBaseIntentService {
	private static String sRegistrationId;
	private final ApplicationInfo appInfo = new ApplicationInfo();
	
	public static void setRegistrationId(String id) {
		sRegistrationId = id;
	}

	public static String getRegistrationId() {
	    return sRegistrationId;
	}

	public GCMIntentService(){
	    super(FullscreenActivity.SENDER_ID);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Integer id = Integer.parseInt(intent.getExtras().getString("id"));
		String text = intent.getExtras().getString("text");
		String channel = intent.getExtras().getString("channel");
		String time = intent.getExtras().getString("cr_time");
		String GUID = intent.getExtras().getString("GUID");
		
		boolean myOwnMessage = ThisAppIsMessageOwner(channel);
		
		if (myOwnMessage || appInfo.isInForeground()) {
			displayMessage(this, text, channel, time, id, GUID, myOwnMessage);
		} else {
			Intent appIntent = new Intent(this, FullscreenActivity.class);
			appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
						.setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_SOUND)
			            .setSmallIcon(R.drawable.ic_launcher)
			            .setContentTitle("Message from <<User>>!")
			            .setContentIntent(pendingIntent)
			            .setPriority(Notification.PRIORITY_HIGH)
			            .setContentText(text);
			
			Notification notification = mBuilder.build();
			
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, notification);
		}
	}

	private boolean ThisAppIsMessageOwner(String channel) {
		return sRegistrationId.compareTo(channel) == 0;
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		sRegistrationId = arg1;
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
	}
	
	private static void displayMessage(Context context, String message, String channel, String time, Integer id, String GUID, boolean myOwnMessage) {
        Intent intent = new Intent("com.example.notificationpoc.DISPLAY_MESSAGE");
        intent.putExtra("id", id);
        intent.putExtra("text", message);
        intent.putExtra("channel", channel);
        intent.putExtra("cr_time", time);
        intent.putExtra("GUID", GUID);
        intent.putExtra("myOwnMessage", myOwnMessage);
        
        context.sendBroadcast(intent);
    }
	
	public Boolean isInForeground() {
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
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
