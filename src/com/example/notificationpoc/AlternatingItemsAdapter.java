package com.example.notificationpoc;
import java.util.List;

import com.example.notificationpoc.entities.Message;
 
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class AlternatingItemsAdapter extends ArrayAdapter<Message> {
	public class ViewHolder {
		public TextView txtMessage;
		public TextView txtCreatedDateTime;
		public ImageView imgRetry;
		public View view;
	}
	
	private ViewHolder viewHolder;
	
    public AlternatingItemsAdapter(Context context, List<Message> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if (convertView == null) {
    		LayoutInflater inflater =(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		convertView = inflater.inflate(R.layout.message_layout, null);
    		
    		viewHolder = new ViewHolder();
    		viewHolder.view = convertView;
    		viewHolder.txtMessage = (TextView)convertView.findViewById(R.id.txtMessage);
    		viewHolder.txtCreatedDateTime = (TextView)convertView.findViewById(R.id.txtMessageCreatedTime);
    		viewHolder.imgRetry = (ImageView)convertView.findViewById(R.id.imgRetry);
    		
    		convertView.setTag(viewHolder);
    	} else {
    		viewHolder = (ViewHolder)convertView.getTag();
    	}
    	
        final Message currentMessage = this.getItem(position);
        
        String messageText;
        if (position > 0 && this.getItem(position - 1).User.Name.compareTo(currentMessage.User.Name) == 0) {
      	  	messageText = currentMessage.Text;
        } else {
        	messageText = currentMessage.toString();
        }
        
        viewHolder.txtMessage.setText(messageText);
        
        viewHolder.txtCreatedDateTime.setText(currentMessage.Time);
        viewHolder.imgRetry.setVisibility(View.GONE);
        viewHolder.view.setOnTouchListener(null);
        
        switch (currentMessage.SendingState) {
        case NONE:
        	break;
        case SENDING:
        	viewHolder.txtCreatedDateTime.setAlpha((float) 0.4);
        	break;
        case DELIVERED:
        	viewHolder.txtCreatedDateTime.setAlpha((float) 0.9);
        	break;
        case SENDING_FAILED:
        	viewHolder.imgRetry.setVisibility(View.VISIBLE);
        	//viewHolder.txtCreatedDateTime.setVisibility(View.GONE);
        	viewHolder.view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					retryMessage(getContext(), currentMessage.getGUID());
					return false;
				}
			});
        	break;
        }
        
        viewHolder.view.setBackgroundColor(currentMessage.User.FavoriteColor);
        
        return convertView;
    }
	
	private static void retryMessage(Context context, String GUID) {
        Intent intent = new Intent("com.example.notificationpoc.RETRY_MESSAGE");
        intent.putExtra("GUID", GUID);
        
        context.sendBroadcast(intent);
    }
}