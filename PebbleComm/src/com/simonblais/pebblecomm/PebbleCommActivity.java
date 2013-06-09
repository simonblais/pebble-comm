package com.simonblais.pebblecomm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PebbleCommActivity extends Activity
{
    private PebbleCommReceiver pebbleReceiver = new PebbleCommReceiver();
    private PebbleKit.PebbleDataReceiver dataReceiver;

    private Handler mHandler;

    private final static UUID PEBBLE_APP_UUID = UUID.fromString("60113006-3F95-4AA8-8D82-0F28AB34C3E7");
    private final static int REQ_KEY = 0x0;
    private final static int NOTIF_KEY = 0x1;

    private final static int LIST_REQ = 0x0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHandler = new Handler();

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
    	// Receiver for media buttons
        IntentFilter receiverFilter = new IntentFilter();
    	receiverFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    	receiverFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
    	receiverFilter.addAction("com.getpebble.action.RECEIVE_DATA");

    	this.registerReceiver(pebbleReceiver, receiverFilter);

        super.onResume();

        // Receiver for any do task list request
        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                final int cmd = data.getInteger(REQ_KEY).intValue();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // All data received from the Pebble must be ACK'd, otherwise you'll hit time-outs in the
                        // watch-app which will cause the watch to feel "laggy" during periods of frequent
                        // communication.
                        PebbleKit.sendAckToPebble(context, transactionId);

                        switch (cmd) {
                            case LIST_REQ:
                                sendTaskListToPebble();
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        };
        PebbleKit.registerReceivedDataHandler(this, dataReceiver);
        //startWatchApp(getApplicationContext());
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

    public void sendTaskList(View view) {
        sendTaskListToPebble();
    }

    public void sendTaskListToPebble()
    {
        Context c = getApplicationContext();

        //startWatchApp(c);

        // Send message to pebble_any_do app
        PebbleDictionary data = new PebbleDictionary();
        data.addString( NOTIF_KEY, "List From Phone App");

        PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
    }

    public void startWatchApp(Context c)
    {
        // Start the app first
        PebbleKit.startAppOnPebble(c, PEBBLE_APP_UUID);
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