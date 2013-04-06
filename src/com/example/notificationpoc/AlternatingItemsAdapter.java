package com.example.notificationpoc;
import java.util.List;

import com.example.notificationpoc.entities.Message;
import com.example.notificationpoc.util.Constants.MessageSendingState;
import com.example.notificationpoc.util.DateTime;
 
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class AlternatingItemsAdapter extends ArrayAdapter<Message> {
	public class ViewHolder {
		public TextView txtMessage;
		public TextView txtCreatedDateTime;
		public ImageView imgWaiting;
		public View view;
		
		public void setState(MessageSendingState state) {
	        switch (state) {
	        case SENDING:
	        	imgWaiting.setVisibility(View.GONE);
	        	txtCreatedDateTime.setVisibility(View.VISIBLE);
	        	break;
	        case DELIVERED:
	        	imgWaiting.setVisibility(View.GONE);
	        	txtCreatedDateTime.setVisibility(View.VISIBLE);
	        	break;
	        case RETRYING:
	        	imgWaiting.setVisibility(View.VISIBLE);
	        	txtCreatedDateTime.setVisibility(View.GONE);
	        	break;
	        }
		}
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
    		viewHolder.imgWaiting = (ImageView)convertView.findViewById(R.id.imgWaiting);
    		
    		convertView.setTag(viewHolder);
    	} else {
    		viewHolder = (ViewHolder)convertView.getTag();
    	}
    	
        final Message currentMessage = this.getItem(position);
        
        viewHolder.txtMessage.setText(currentMessage.toString());
        viewHolder.txtCreatedDateTime.setText(new DateTime().GetUIDate(currentMessage.Time));
        viewHolder.view.setOnTouchListener(null);
        viewHolder.setState(currentMessage.SendingState);
        viewHolder.view.setBackgroundColor(currentMessage.User.FavoriteColor);
        
        return convertView;
    }
}