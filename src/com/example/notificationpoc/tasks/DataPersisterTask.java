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
	private Context context;
	
	public DataPersisterTask(Context ctx) {
		context = ctx;
	}
	
	public boolean PersistMessages(MessageList messagesToPersist, String name) {
		if (messagesToPersist != null && !messagesToPersist.isEmpty()) {
			String jsonString = new Gson().toJson(messagesToPersist);
			try {
				// first delete not to keep duplicates
				context.deleteFile(name);
				
				FileOutputStream stream = context.openFileOutput(name, Context.MODE_PRIVATE);
				stream.write(jsonString.getBytes());
				stream.close();
				
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public MessageList RecoverMessages(String name) {
		if (new File(name).exists()) {
			return new MessageList();
		}
		
		try {			
			FileInputStream stream = context.openFileInput(name);
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
			e.printStackTrace();
		}
		
		return new MessageList();
	}
}
