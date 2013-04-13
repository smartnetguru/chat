package com.example.notificationpoc;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

import com.example.notificationpoc.util.ApplicationInfo;
import com.example.notificationpoc.util.Constants;
import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint("InlinedApi")
public class GCMIntentService extends GCMBaseIntentService {
	private static String sRegistrationId;
	private static Integer myId;
	private ApplicationInfo appInfo;
	
	private ApplicationInfo getAppInfo() {
		if (appInfo == null) {
			appInfo = new ApplicationInfo(this);
		}
		
		return appInfo;
	}
	
	public static Integer getMyId() {
		if (!setCalled) {
			ACRA.getErrorReporter().handleException(new Throwable("Calling getMyId without previously calling setMyId.", new Throwable()), true);
		}
		return myId;
	}
	
	private static boolean setCalled = false;
	public static void setMyId(Integer id) {
		if (id == null) {
			ACRA.getErrorReporter().handleException(new Throwable("setMyId called with null value.", new Throwable()), true);
		}
		
		setCalled = true;
		myId = id;
	}
	
	/*public static void setRegistrationId(Context ctx, String id) {
		Log.w("Assing channel", id, new Throwable());
		
		sRegistrationId = id;
		
		if (getMyId() == null)
			initRegisterUser(ctx, getPhoneNumberHash(ctx), id);
	}*/

	public static String getRegistrationId() {
	    return sRegistrationId;
	}

	public GCMIntentService(){
	    super(Constants.Application.SENDER_ID);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		String id = intent.getExtras().getString("id");
		String text = intent.getExtras().getString("text");
		String userId = intent.getExtras().getString("user_id");
		String time = intent.getExtras().getString("cr_time");
		
		boolean myOwnMessage = thisAppIsMessageOwner(userId);
		
		if (myOwnMessage || getAppInfo().isInForeground()) {
			initDisplayMessage(this, id, text, userId, time, myOwnMessage);
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

	private boolean thisAppIsMessageOwner(String userId) {
		return userId.compareTo(getMyId().toString()) == 0;
	}

	@Override
	protected void onRegistered(Context ctx, String registrationId) {
		sRegistrationId = registrationId;
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		
	}
	
	private static void initDisplayMessage(Context context, String id, String message, String userId, String time, boolean myOwnMessage) {
        Intent intent = new Intent(Constants.Events.DISPLAY_MESSAGE);
        intent.putExtra("id", id);
        intent.putExtra("text", message);
        intent.putExtra("user_id", userId);
        intent.putExtra("cr_time", time);
        intent.putExtra("myOwnMessage", myOwnMessage);
        
        context.sendBroadcast(intent);
    }
}
