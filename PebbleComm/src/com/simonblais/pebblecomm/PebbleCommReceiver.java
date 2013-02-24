package com.simonblais.pebblecomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PebbleCommReceiver extends BroadcastReceiver {

	/** Method to receive broadcasts*/
	@Override
	public void onReceive(Context context, Intent inboundIntent) { 
		Toast.makeText(context, "Received Broadcast", Toast.LENGTH_SHORT).show();
		
		// Stop the broadcast from getting further
		abortBroadcast();
	}

}
