package com.example.notificationpoc.tasks;

import java.util.List;

import org.acra.ACRA;

import com.example.notificationpoc.GCMIntentService;
import com.example.notificationpoc.MobileServiceClientSingletone;
import com.example.notificationpoc.entities.customer;
import com.example.notificationpoc.util.Constants;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class CustomerManager {
	private Context context;
	private MobileServiceClient mClient;
	
	public CustomerManager(Context ctx) {
		context = ctx;
		
		mClient = MobileServiceClientSingletone.get(context);
	}
	
	// ######## Application state changed events ########
	public void unfreeze() {
		context.registerReceiver(registerUserHandler, new IntentFilter(Constants.Events.REGISTER_USER));
	}
	
	public void freeze() {
		try {
			context.unregisterReceiver(registerUserHandler);
		} catch (Exception ex) {}
	}
	
	private final BroadcastReceiver registerUserHandler = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent) {
			        final String phoneNumberHash = intent.getExtras().getString("phone_number_hash");
			        final String registrationId = intent.getExtras().getString("registration_id");
			        
			        if (phoneNumberHash == null || phoneNumberHash.isEmpty()) {
			        	ACRA.getErrorReporter().handleException(new Throwable("PhoneNumberHash is null or empty. -> registerUserHandler"), true);
			        	return;
			        }
			        
			        if (registrationId == null || registrationId.isEmpty()) {
			        	ACRA.getErrorReporter().handleException(new Throwable("RegistrationId is null or empty. -> registerUserHandler"), true);
			        	return;
			        }
			        
			        final MobileServiceTable<customer> customerTable = mClient.getTable(customer.class);
			        
			        customerTable
			        	.where()
			        	.field("phone_number_hash").eq(phoneNumberHash)
			        	.and()
			        	.field("channel").eq(registrationId)
			        	.execute(new TableQueryCallback<customer>() {
						
						@Override
						public void onCompleted(List<customer> result, int count,
								Exception exception, ServiceFilterResponse response) {
							if (exception != null) {
								return;
							}
							
							if (result == null || result.isEmpty()) {
								customer c = new customer();
								c.phone_number_hash = phoneNumberHash;
								c.channel = registrationId;
								
								customerTable.insert(c, new TableOperationCallback<customer>() {
									@Override
									public void onCompleted(customer entity, Exception exception,
											ServiceFilterResponse response) {
										if (exception != null) {
										} else {
											GCMIntentService.setMyId(entity.id);
										}
									}
								});
							} else {
								GCMIntentService.setMyId(result.get(0).id);
							}
						}
					});
				}
			};
}
