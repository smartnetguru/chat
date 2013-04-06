package com.example.notificationpoc.entities;

import com.example.notificationpoc.util.Constants;
import com.example.notificationpoc.util.Constants.MessageSendingState;

public class Message {
	public Integer Id;
	public String Text;
	public User User;
	public String Time;
	
	private String GUID;
	public String getGUID() {
		return GUID == null ? "" : GUID;
	}
	
	public void setGUID(String GUID) {
		this.GUID = GUID;
	}
	
	public Constants.MessageSendingState SendingState = MessageSendingState.NONE;
	
	@Override
	public String toString() {
		return User.Name + ": " + Text;
	}
}
