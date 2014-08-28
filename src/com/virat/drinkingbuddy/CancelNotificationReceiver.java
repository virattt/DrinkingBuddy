package com.virat.drinkingbuddy;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * This class is a BroadcastReceiver that cancels
 * the existing drink updates and notification that are created
 * in the DrinkUpdateReceiver class.
 */
public class CancelNotificationReceiver extends BroadcastReceiver {
	private static final String TAG = "CancelNotificationReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// ID of DrinkUpdateReceiver notification
		int notificationId = intent.getIntExtra(
				DrinkUpdateService.MY_NOTIFICATION_ID_STRING, 1);
		cancelExistingAlarm(context);

		// Get NotificationManager and cancel existing notifs
		NotificationManager manager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		manager.cancel(notificationId);
		
		Log.d(TAG, "CancelNotificationReceiver onReceive called");
	}

	/** Re-creates the existing alarm and cancels it */
	private void cancelExistingAlarm(Context context) {
		Intent intent = new Intent();
		DrinkUpdateService.setServiceAlarm(context, false, intent);
	}
}
