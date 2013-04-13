package com.example.notificationpoc.tasks;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.notificationpoc.AlternatingItemsAdapter;
import com.example.notificationpoc.GCMIntentService;
import com.example.notificationpoc.MobileServiceClientSingletone;
import com.example.notificationpoc.entities.Message;
import com.example.notificationpoc.entities.MessageList;
import com.example.notificationpoc.entities.notifications;
import com.example.notificationpoc.util.Constants;
import com.example.notificationpoc.util.DateTime;
import com.example.notificationpoc.util.Constants.MessageSendingState;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceQuery;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.QueryOrder;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MessageManager {
	private final static Integer MAX_MESSAGE_COUNT = 50;
	
    private Context context;
    private Activity activity;
    
    private MobileServiceClient mClient;
	
    private final MessageList messages = new MessageList(MAX_MESSAGE_COUNT);
	private MessageList retryMessages = new MessageList();
	
	private final AlternatingItemsAdapter messagesAdapter;
	private final DataPersisterTask dataPersister;
	private final DateTime dateUtil = new DateTime();
	private Timer retryTimer;
	
	public MessageManager(Context ctx, Activity act) {
		this.context = ctx;
		this.activity = act;
		
		this.messagesAdapter = new AlternatingItemsAdapter(ctx, messages);
		this.dataPersister = new DataPersisterTask(ctx);
		
		mClient = MobileServiceClientSingletone.get(ctx);
	}
	
	private void InitRetryer() {
		retryTimer = new Timer();
		retryTimer.schedule(new TimerTask() {
			public void run()  {
				retryMessages();
			}
			}, 1000, 1000);
	}
	
	public void cleanup() {
		dataPersister.PersistMessages(new MessageList(), "messages");
		messages.clear();
		messagesAdapter.notifyDataSetChanged();
	}

	// ######## Initialization ########
	private void InitEventHandlers() {
		context.registerReceiver(displayMessageHandler, new IntentFilter(Constants.Events.DISPLAY_MESSAGE));
	}
	
	// ######## Application state changed events ########
	public void unfreeze() {
		MessageList recoveredMessages = dataPersister.RecoverMessages("messages");
		if (!recoveredMessages.isEmpty()) {
			messages.clear();
    		messages.addAll(recoveredMessages);
    		
    		messagesAdapter.notifyDataSetChanged();
		}
		
		MessageList recoveredRetryMessages = dataPersister.RecoverMessages("retrymessages");
		if (!recoveredRetryMessages.isEmpty()) {
			retryMessages.clear();
			retryMessages.addAll(recoveredRetryMessages);
		}
    	
		try {
			grabLastMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		InitEventHandlers();
		InitRetryer();
	}
	
	public void freeze() {
		retryTimer.cancel();
		try {
			context.unregisterReceiver(displayMessageHandler);
		} catch (Exception ex) {}
		
    	dataPersister.PersistMessages(messages, "messages");
    	dataPersister.PersistMessages(retryMessages, "retrymessages");
	}
	
	// ######## Other ########
	public void processMessage(String messageText) {
		Date currentDate = dateUtil.Now();
		
		Message message = new Message();
		message.Text = messageText;
		message.user_id = GCMIntentService.getMyId();
		message.Time = currentDate;
		
		message.SendingState = MessageSendingState.SENDING;
		AddToList(message);
		BroadcastMessage(message);
	}
	
	private void BroadcastMessage(final Message message) {
		try {
			MobileServiceTable<notifications> notificationTable = mClient.getTable(notifications.class);
			
			notifications n = new notifications();
			n.text = message.Text;
			n.time = dateUtil.GetDBDate(message.Time);
			n.userId = message.user_id;
			
			notificationTable.insert(n, new TableOperationCallback<notifications>() {
				@Override
				public void onCompleted(notifications entity, Exception exception,
						ServiceFilterResponse response) {
					if (exception != null) {
						if (!retryMessages.contains(message)) {
							retryMessages.add(message);
						}
					} else {
						if (retryMessages.contains(message)) {
							retryMessages.remove(message);
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private final BroadcastReceiver displayMessageHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Integer id = Integer.parseInt(intent.getExtras().getString("id"));
            String text = intent.getExtras().getString("text");
            Integer userId = Integer.parseInt(intent.getExtras().getString("user_id"));
            Date time = dateUtil.GetDBDate(intent.getExtras().getString("cr_time"));
            boolean myOwnMessage = intent.getExtras().getBoolean("myOwnMessage");
            
            Message message = ConstructMessage(id, text, userId, time);
            
            if (myOwnMessage) {
            	ConfirmMessage(message);
            } else {
            	message.SendingState = MessageSendingState.DELIVERED;
            	AddToList(message);
            }
        }
        
		private void ConfirmMessage(Message message) {
			Message messageToConfirm = AcquireMessageWithTime(message.Time);
			if (messageToConfirm != null) {
				messageToConfirm.Id = message.Id;
				messageToConfirm.SendingState = MessageSendingState.DELIVERED;
				messagesAdapter.notifyDataSetChanged();
			}
		}
    };
	
	public void grabLastMessages() throws Exception {
		context.sendBroadcast(new Intent(Constants.Events.START_LOADING));
		MobileServiceTable<notifications> notificationTable = mClient.getTable(notifications.class);
		
		MobileServiceQuery<TableQueryCallback<notifications>> query = notificationTable
			.where()
			.field("id")
			.gt(getLastDeliveredMessage())
			.orderBy("id", QueryOrder.Descending)
			.top(MAX_MESSAGE_COUNT);
		
		query.execute(new TableQueryCallback<notifications>() {
			@Override
			public void onCompleted(List<notifications> result, int count,
					Exception exception, ServiceFilterResponse response) {
				if (result != null) {
					int itemCount = result.size();
					
					for (Integer i = itemCount - 1; i >= 0; i--) {
						notifications notification = result.get(i);
						
						Message message = ConstructMessage(notification.id, notification.text, notification.userId, dateUtil.GetDBDate(notification.time));
						message.SendingState = MessageSendingState.DELIVERED;
						
						AddToList(message);
						messagesAdapter.notifyDataSetChanged();
					}
				}
				context.sendBroadcast(new Intent(Constants.Events.STOP_LOADING));
			}
		});
	}
	
	private void retryMessages() {
			if (retryMessages.size() == 0) {
				return;
			}
			
			for (Message item : retryMessages) {
				if (item.SendingState != MessageSendingState.RETRYING) {
					item.SendingState = MessageSendingState.RETRYING;
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							messagesAdapter.notifyDataSetChanged();
						}
					});
				}
				
				BroadcastMessage(item);
			}
	}

	private Message ConstructMessage(Integer id, String text, Integer userId, Date time) {
		Message message = new Message();
		message.Id = id;
		message.Text = text;
		message.user_id = userId;
		message.Time = time;
		
		return message;
	}
	
	private Message AcquireMessageWithTime(Date time) {
		for(Message item : messages) {
			if (item.Time.compareTo(time) == 0) {
				return item;
			}
		}
		return null;
	}
	
	private void AddToList(Message message) {		
		messages.add(message);
        messagesAdapter.notifyDataSetChanged();
	}
	
	private Integer getLastDeliveredMessage() {
		for (int i = messages.size() - 1; i >=0; i--) {
			Message item = messages.get(i);
			if (item.SendingState == MessageSendingState.DELIVERED) {
				return item.Id;
			}
		}
		
		return -1;
	}
	
	public AlternatingItemsAdapter getMessageAdapter() {
		return messagesAdapter;
	}
}
