package com.example.notificationpoc.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.example.notificationpoc.tasks.DataPersisterTask;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneNumberManager {
	private Context context;
	private final DataPersisterTask persister;
	
	public PhoneNumberManager(Context ctx) {
		context = ctx;
		persister = new DataPersisterTask(ctx);
	}
	
	private String getPhoneNumberFromSim() {
		TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

		return tMgr.getLine1Number();
	}
	
	private String getPhoneNumberFromUI() {
		return "+491771745384";
	}
	
	private String getPersistedPhoneNumber() {
		return persister.recoverPhoneNumber();
	}
	
	public String getPhoneNumber() {
		String phoneNumber = getPersistedPhoneNumber();
		if (phoneNumber != null && !phoneNumber.isEmpty()) {
			return phoneNumber;
		}
		
		phoneNumber = getPhoneNumberFromSim();
		if (phoneNumber != null && !phoneNumber.isEmpty()) {
			persister.persistPhoneNumber(phoneNumber);
			
			return phoneNumber;
		}
		
		phoneNumber = getPhoneNumberFromUI();
		persister.persistPhoneNumber(phoneNumber);
		
		return phoneNumber;
	}
	
	public BigInteger getPhoneNumberHash() {
		String phoneNumber = getPhoneNumber();
		if (!phoneNumber.isEmpty()) {
			try {
				MessageDigest m = MessageDigest.getInstance("MD5");
				m.reset();
				m.update(phoneNumber.getBytes());
				byte[] digest = m.digest();
				BigInteger hash = new BigInteger(1,digest);
				
				return hash;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
