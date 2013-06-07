package com.simonblais.pebblecomm;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class PebbleCommActivity extends Activity
{
    private PebbleCommReceiver pebbleReceiver = new PebbleCommReceiver();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   
        
        Intent intent = getIntent();
        String action = intent.getAction();
        
        if (Intent.ACTION_SEND.equals(action) && intent.getType().equals("text/plain")) {
        	// Handle sharing text from other app by sending it to pebble
        	String body = intent.getStringExtra(Intent.EXTRA_TEXT);
        	        	
        	sendNotificationToPebble("Sent To Pebble", body);
        	
        	EditText text = (EditText) findViewById(R.id.editTextNotify);        	
        	text.setText(body);
        }
    }
    
    @Override
    public void onResume() {
    	IntentFilter receiverFilter = new IntentFilter();
    	receiverFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    	receiverFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
    	receiverFilter.addAction("com.getpebble.action.RECEIVE_DATA");
    	
    	this.registerReceiver(pebbleReceiver, receiverFilter);
    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	this.unregisterReceiver(pebbleReceiver);
    	super.onPause();
    }

    public void getTaskList(View view) {
        Cursor tasks = getContentResolver().query(TasksContract.TASKS_URI, null, null, null, null);

        EditText textField = (EditText) findViewById( R.id.taskList );
        textField.setText("");

        if ( tasks != null ) {

            int taskNameIndex = tasks.getColumnIndex( TasksContract.TasksColumns.TITLE );
            String currentText;

            while ( tasks.moveToNext() ) {
                currentText = tasks.getString(taskNameIndex);

                textField.append( currentText + "\n" );
            }
        }
    }

    /** Called when the send button is pressed */
    public void sendNotification(View view) {
    	EditText text = (EditText) findViewById(R.id.editTextNotify);
    	sendNotificationToPebble("Pebble Comm", text.getText().toString());
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