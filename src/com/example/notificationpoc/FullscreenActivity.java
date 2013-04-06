package com.example.notificationpoc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.example.notificationpoc.entities.Message;
import com.example.notificationpoc.entities.MessageList;
import com.example.notificationpoc.entities.User;
import com.example.notificationpoc.entities.notifications;
import com.example.notificationpoc.tasks.DataPersisterTask;
import com.example.notificationpoc.util.Constants.MessageSendingState;
import com.example.notificationpoc.util.DateTime;
import com.microsoft.windowsazure.mobileservices.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;

@SuppressLint("NewApi")
public class FullscreenActivity extends Activity {
    private MobileServiceClient mClient;
    @com.google.gson.annotations.SerializedName("channel")
    private String mRegistrationId;
    public static final String SENDER_ID = "994169729957";
    public static final Integer MAX_MESSAGE_COUNT = 30;
    private static Integer LastLoadedMessageId = 0;
    
    private final UserIdentifier userIdentifier = new UserIdentifier();
    private final AlertDialogManager alert = new AlertDialogManager();
    private final DateTime dateUtil = new DateTime();
    private final DataPersisterTask dataPersister = new DataPersisterTask();
    
    private MessageList messages = new MessageList();
    private AlternatingItemsAdapter messagesAdapter;
    
    private EditText txtMessage;
    private Button btnSend;
    private ListView lstMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        
        InitGCM();
        InitMobileServiceClient();
        
        InitControls();
        InitDataBindings();
        InitEvents();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
        try {
        	if (messages.isEmpty()) {
        		MessageList recoveredMessages = dataPersister.RecoverMessages(this);
        		if (!recoveredMessages.isEmpty()) {
	        		messages.addAll(recoveredMessages);
	        		LastLoadedMessageId = messages.get(messages.size() - 1).Id;
	        		
	        		messagesAdapter.notifyDataSetChanged();
	        		ScrollListViewBottom(lstMessages);
        		}
        	}
			grabLastMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	MessageList persistableMessages = new MessageList();
    	for (int i = 0; i < messages.size(); i++) {
    		Message item = messages.get(i);
    		
    		if (item.Id != null) {
    			persistableMessages.add(item);
    		}
    	}
    	
    	dataPersister.PersistMessages(persistableMessages, this);
    	
    	unregisterReceiver(mRetryMessage);
    }

	private void grabLastMessages() throws Exception {
		StartLoadingUI();
		
		MobileServiceTable<notifications> notificationTable = mClient.getTable(notifications.class);
		
		notificationTable
			.where()
			.field("id")
			.gt(LastLoadedMessageId)
			.orderBy("id", QueryOrder.Descending)
			.top(MAX_MESSAGE_COUNT)
			.execute(new TableQueryCallback<notifications>() {
				@Override
				public void onCompleted(List<notifications> result, int count,
						Exception exception, ServiceFilterResponse response) {
					if (result != null) {
						MessageList existingMessages = new MessageList();
						
						int itemCount = result.size();
						
						for (Integer i = itemCount - 1; i >= 0; i--) {
							notifications notification = result.get(i);
							
							if (i == 0) {
								LastLoadedMessageId = notification.Id;
							}
							Message message = GetMessage(notification.Id, notification.text, notification.mRegistrationId, notification.time, notification.GUID);
							message.SendingState = MessageSendingState.DELIVERED;
							
							existingMessages.add(message);
						}
						
						messages.addAll(existingMessages);
						
						messagesAdapter.notifyDataSetChanged();
						StopLoadingUI();
					} else {
						StopLoadingUI();
						//alert.showAlertDialog(FullscreenActivity.this, "Retrieving messages...", "It was not possible to retrieve messages! Check your internet connection.", false);
					}
				}
		});
	}

	private void InitControls() {
		txtMessage = (EditText)findViewById(R.id.txtMessage);
		btnSend = (Button)findViewById(R.id.btnSend);
		
		lstMessages = ((ListView)findViewById(R.id.lstMessages));
		lstMessages.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	private void InitEvents() {
		btnSend.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				String messageText = txtMessage.getText().toString().trim();
				
				if (!messageText.isEmpty() && action == MotionEvent.ACTION_UP)
				{	
					// last resort, try one more time before sending message
					if (mRegistrationId.isEmpty()) {
						InitGCM();
					}
					
					if (mRegistrationId.isEmpty()) {
						alert.showAlertDialog(FullscreenActivity.this, "Initializing connection...", "It was not possible to initialize connection!", false);
					} else {
						txtMessage.setText("");
						
						User messageUser = userIdentifier.IdentifyUser(mRegistrationId);
						
						Date currentDate = dateUtil.Now();
						
						Message message = new Message();
						message.Text = messageText;
						message.User = messageUser;
						message.setGUID(UUID.randomUUID().toString());
						
						message.Time = dateUtil.GetDBDate(currentDate);
						BroadcastMessage(message);
						message.Time = dateUtil.GetUIDate(currentDate);
						AddMessage(message);
					}
				}
				
				return false;
			}
		});
		
		registerReceiver(mRetryMessage, new IntentFilter("com.example.notificationpoc.RETRY_MESSAGE"));
	}
	
	private void BroadcastMessage(final Message message) {
		try {
			message.SendingState = MessageSendingState.SENDING;
			messagesAdapter.notifyDataSetChanged();
			
			MobileServiceTable<notifications> notificationTable = mClient.getTable(notifications.class);
			
			notifications n = new notifications();
			n.complete = false;
			n.text = message.Text;
			n.time = message.Time.toString();
			n.GUID = message.getGUID();
			n.setRegistrationId(message.User.Channel);
			
			notificationTable.insert(n, new TableOperationCallback<notifications>() {
				@Override
				public void onCompleted(notifications entity, Exception exception,
						ServiceFilterResponse response) {
					if (exception != null) {
						//alert.showAlertDialog(FullscreenActivity.this, "Sending message...", "It was not possible to send message!", false);
						message.SendingState = MessageSendingState.SENDING_FAILED;
						messagesAdapter.notifyDataSetChanged();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			StopLoadingUI();
			alert.showAlertDialog(FullscreenActivity.this, "Sending message...", "It was not possible to send message!", false);
		}
	}

	private void InitDataBindings() {
		messagesAdapter = new AlternatingItemsAdapter(this, messages);
		
		lstMessages.setAdapter(messagesAdapter);
	}

	private void InitGCM() {
		GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        mRegistrationId = GCMRegistrar.getRegistrationId(this);
        if (mRegistrationId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        }
        GCMIntentService.setRegistrationId(mRegistrationId);
        registerReceiver(mHandleMessageReceiver, new IntentFilter("com.example.notificationpoc.DISPLAY_MESSAGE"));
	}

	private void InitMobileServiceClient() {
		try {
			mClient = new MobileServiceClient(
				      "https://notification.azure-mobile.net/",
				      "aJwQWBkTBHRaqoMHnrbapuLFaKaggY49",
				      this
				);
		} catch (Exception e) {
			e.printStackTrace();
			alert.showAlertDialog(FullscreenActivity.this, "Initializing...", "It was not possible initialize connection! Please try again.", false);
		}
	}
	
	private final BroadcastReceiver mRetryMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String GUID = intent.getExtras().getString("GUID");
			Message messageToRetry = FindMessageWithGUID(GUID);
			
			BroadcastMessage(messageToRetry);
		};
	};
	
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Integer id = intent.getExtras().getInt("id");
            String text = intent.getExtras().getString("text");
            String channel = intent.getExtras().getString("channel");
            String time = intent.getExtras().getString("cr_time");
            String GUID = intent.getExtras().getString("GUID");
            boolean myOwnMessage = intent.getExtras().getBoolean("myOwnMessage");
            
            Message message = GetMessage(id, text, channel, time, GUID);
            
            if (id > LastLoadedMessageId) {
            	LastLoadedMessageId = id;
            }
            
            if (myOwnMessage) {
            	ConfirmMessage(message);
            } else {
            	message.SendingState = MessageSendingState.DELIVERED;
            	AddMessage(message);
            }
        }
        
		private void ConfirmMessage(Message message) {
			Message messageToConfirm = FindMessageWithGUID(message.getGUID());
			if (messageToConfirm != null) {
				messageToConfirm.SendingState = MessageSendingState.DELIVERED;
				messagesAdapter.notifyDataSetChanged();
			}
		}
    };
    
	private Message FindMessageWithGUID(String GUID) {
		String itemGUID;
		for(Message item : messages) {
			itemGUID = item.getGUID();
			if (!itemGUID.isEmpty() && itemGUID.compareTo(GUID) == 0) {
				return item;
			}
		}
		return null;
	}
    
	private void AddMessage(Message message) {
		if (messages.size() > MAX_MESSAGE_COUNT) {
			messages.remove(0);
		}
		
		messages.add(message);
        messagesAdapter.notifyDataSetChanged();
	}
    
	private Message GetMessage(Integer id, String text, String channel, String time, String GUID) {
		String messageText = text;
		User messageUser = userIdentifier.IdentifyUser(channel);
		
		Message message = new Message();
		message.Id = id;
		message.Text = messageText;
		message.User = messageUser;
		message.Time = dateUtil.GetUIDate(time);
		message.setGUID(GUID);
		
		return message;
	}
	
	private void ScrollListViewBottom(final ListView list) {
		list.post(new Runnable(){
			  public void run() {
				  list.setSelection(list.getCount() - 1);
			  }});
	}
	
	private void StartLoadingUI() {
		findViewById(R.id.progressBar).setVisibility(0);
	}
	
	private void StopLoadingUI() {
		findViewById(R.id.progressBar).setVisibility(4);
	}
}
