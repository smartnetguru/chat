package com.example.notificationpoc.entities;

import java.util.Date;

import com.example.notificationpoc.util.Constants;
import com.example.notificationpoc.util.Constants.MessageSendingState;

public class Message {
	public String Text;
	public User User;
	public Date Time;
	
	public Constants.MessageSendingState SendingState = MessageSendingState.NONE;
	
	@Override
	public String toString() {
		return Text;
	}
}
