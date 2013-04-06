package com.example.notificationpoc.entities;

import java.util.ArrayList;

public class MessageList extends ArrayList<Message> {
	private Integer maxItemCount;
	private boolean limitedSize;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MessageList(Integer maxCount) {
		maxItemCount = maxCount;
		limitedSize = true;
	}
	
	public MessageList() {
		
	}

	@Override
	public void add(int index, Message object) {
		super.add(index, object);
		
		if (limitedSize && size() > maxItemCount) {
			remove(0);
		}
	}
}
