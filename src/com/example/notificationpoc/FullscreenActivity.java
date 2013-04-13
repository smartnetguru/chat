package com.example.notificationpoc;

import java.math.BigInteger;

import com.example.notificationpoc.tasks.CustomerManager;
import com.example.notificationpoc.tasks.MessageManager;
import com.example.notificationpoc.util.Constants;
import com.example.notificationpoc.util.PhoneNumberManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;

@SuppressLint("NewApi")
public class FullscreenActivity extends Activity {
    private MessageManager msgManager;
    private CustomerManager cstManager;
    private EditText txtMessage;
    private Button btnSend;
    private ListView lstMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        
        InitCustomerManager();
        InitMessageManager();
        
        InitControls();
        InitDataBindings();
        InitEventHandlers();
    }
    
    private void InitCustomerManager() {
		cstManager = new CustomerManager(this);
	}

	private void InitMessageManager() {
    	msgManager = new MessageManager(this, FullscreenActivity.this);
	}

	private void InitDataBindings() {
    	lstMessages.setAdapter(msgManager.getMessageAdapter());
	}

	@Override
    protected void onResume() {
    	super.onResume();
    	msgManager.unfreeze();
    	cstManager.unfreeze();
    	
    	InitGCM();
    	
    	ScrollListViewBottom(lstMessages);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	msgManager.freeze();
    	cstManager.freeze();
    	
    	try {
    		unregisterReceiver(startLoadingHandler);
    		unregisterReceiver(stopLoadingHandler);
    	} catch (Exception ex) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.addSubMenu("Cleanup");
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	boolean shouldCleanup = item.getTitle() == "Cleanup";
    	if (shouldCleanup) {
    		msgManager.cleanup();
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
	private void InitControls() {
		txtMessage = (EditText)findViewById(R.id.txtMessage);
		btnSend = (Button)findViewById(R.id.btnSend);
		
		try {
			lstMessages = ((ListView)findViewById(R.id.lstMessages));
			lstMessages.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		} catch (Exception e) {}
	}

	private void InitEventHandlers() {
		btnSend.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				String messageText = txtMessage.getText().toString().trim();
				
				if (!messageText.isEmpty() && action == MotionEvent.ACTION_UP)
				{	
					txtMessage.setText("");
					msgManager.processMessage(messageText);
				}
				
				return false;
			}
		});
		
		registerReceiver(startLoadingHandler, new IntentFilter(Constants.Events.START_LOADING));
		registerReceiver(stopLoadingHandler, new IntentFilter(Constants.Events.STOP_LOADING));
	}

	private void InitGCM() {
		GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        
        String mRegistrationId = GCMRegistrar.getRegistrationId(this);
        if (mRegistrationId.equals("")) {
            GCMRegistrar.register(this, Constants.Application.SENDER_ID);
        }
        
        Intent intent = new Intent(Constants.Events.REGISTER_USER);
        intent.putExtra("phone_number_hash", new PhoneNumberManager(this).getPhoneNumberHash().toString());
        intent.putExtra("registration_id", mRegistrationId);
        
        this.sendBroadcast(intent);
	}
	
	private void ScrollListViewBottom(final ListView list) {
		list.post(new Runnable(){
			  public void run() {
				  list.setSelection(list.getCount() - 1);
			  }});
	}
	
    private final BroadcastReceiver startLoadingHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	startLoadingUI();
        }
    };
    
    private final BroadcastReceiver stopLoadingHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	stopLoadingUI();
        }
    };
	
	private void startLoadingUI() {
		findViewById(R.id.progressBar).setVisibility(0);
	}
	
	private void stopLoadingUI() {
		findViewById(R.id.progressBar).setVisibility(4);
	}
}
