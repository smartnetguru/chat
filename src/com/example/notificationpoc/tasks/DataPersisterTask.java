package com.example.notificationpoc.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.content.Context;

import com.example.notificationpoc.entities.MessageList;
import com.google.gson.Gson;

public class DataPersisterTask {
	private final String MESSAGES_FILE_NAME = "messages";
	
	public boolean PersistMessages(MessageList messagesToPersist, Context ctx) {
		if (messagesToPersist != null && !messagesToPersist.isEmpty()) {
			String jsonString = new Gson().toJson(messagesToPersist);
			try {
				// first delete not to keep duplicates
				ctx.deleteFile(MESSAGES_FILE_NAME);
				
				FileOutputStream stream = ctx.openFileOutput(MESSAGES_FILE_NAME, Context.MODE_PRIVATE);
				stream.write(jsonString.getBytes());
				stream.close();
				
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public MessageList RecoverMessages(Context ctx) {
		if (new File(MESSAGES_FILE_NAME).exists()) {
			return new MessageList();
		}
		
		try {			
			FileInputStream stream = ctx.openFileInput(MESSAGES_FILE_NAME);
			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader bReader = new BufferedReader(streamReader);
			
			StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = bReader.readLine()) != null) {
		        sb.append(line);
		    }
		    bReader.close();
		    streamReader.close();
		    stream.close();
		    
		    return new Gson().fromJson(sb.toString(), MessageList.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new MessageList();
	}
}
