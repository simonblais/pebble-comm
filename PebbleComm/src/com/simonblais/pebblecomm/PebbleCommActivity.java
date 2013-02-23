package com.simonblais.pebblecomm;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class PebbleCommActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    
    /** Called when the send button is pressed */
    public void sendNotification(View view) {
    	sendNotificationToPebble("Test Title", "Test Body");
    }
    
    
    
    /** Utility method to send an alert notification to Pebble */
    public void sendNotificationToPebble(String title, String body) {
    	
    	final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
    	
    	final Map<String, Object> data = new HashMap<String, Object>();
    	
    	data.put("title", title);
    	data.put("body", body);
    	
    	final JSONObject jsonData = new JSONObject(data);
    	final String notificationData = new JSONArray().put(jsonData).toString();
    	
    	i.putExtra("messageType", "PEBBLE_ALERT");
    	i.putExtra("sender", "PebbleCommunicator");
    	i.putExtra("notificationData", notificationData);
    	
    	Log.d( "PebbleComm", "Sending notification to Pebble" + notificationData);
    	sendBroadcast(i);
    }
}
