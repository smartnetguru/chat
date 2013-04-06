package com.example.notificationpoc;

import com.example.notificationpoc.tasks.MessageManager;
import com.example.notificationpoc.util.Constants;

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
    private MessageManager msgManager;
    
    private EditText txtMessage;
    private Button btnSend;
    private ListView lstMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        
        InitGCM();
        InitMessageManager();
        
        InitControls();
        InitDataBindings();
        InitEventHandlers();
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
    	ScrollListViewBottom(lstMessages);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	msgManager.freeze();
    }

	private void InitControls() {
		txtMessage = (EditText)findViewById(R.id.txtMessage);
		btnSend = (Button)findViewById(R.id.btnSend);
		
		lstMessages = ((ListView)findViewById(R.id.lstMessages));
		lstMessages.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
        GCMIntentService.setRegistrationId(mRegistrationId);
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
