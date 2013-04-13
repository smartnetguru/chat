package com.example.notificationpoc;

import android.content.Context;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

public class MobileServiceClientSingletone {
	private static MobileServiceClient mClient = null;
	
	public static MobileServiceClient get(Context ctx) {
		if (mClient == null) {
			try {
				mClient = new MobileServiceClient(
					      "https://notification.azure-mobile.net/",
					      "aJwQWBkTBHRaqoMHnrbapuLFaKaggY49",
					      ctx
					);
			} catch (Exception e) {
				e.printStackTrace();
				//alert.showAlertDialog(FullscreenActivity.this, "Initializing...", "It was not possible initialize connection! Please try again.", false);
			}
		}
		
		return mClient;
	}
}
