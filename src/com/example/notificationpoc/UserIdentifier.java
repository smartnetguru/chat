package com.example.notificationpoc;

import com.example.notificationpoc.entities.User;

public class UserIdentifier {
	private User Ivan;
	private User Lena;
	
	public UserIdentifier() {
		Lena = new User();
		Lena.Name = "Lena";
		Lena.FavoriteColor = 0xAA8C0281;
		Lena.Channel = ""; // not known, treat any other as Lena's
		
		Ivan = new User();
		Ivan.Name = "Ivan";
		Ivan.FavoriteColor = 0xBB020202;
		Ivan.Channel = "APA91bEzQIuyJFDp4pT8leRU2XRAzM28aIHhlH3zXk0NzjOkv96_FX787aOFDvUob2U0um91HTNGpgDekB-nCYf5uSwJ8o3hfSuUqh5ncR7Jgielmuokor-XSfZw86UNIHnSY-BZi4CpEEf6GbEpeoSAXy5aSuWDbg";
	}
	
	public User IdentifyUser(String channel) {
		if (Ivan.Channel.compareTo(channel) == 0) {
			return Ivan;
		}
		
		Lena.Channel = channel;
		return Lena;
	}
}
