package com.virat.drinkingbuddy;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * This class is a BroadcastReceiver that cancels
 * the existing drink updates and notification that are created
 * in the DrinkUpdateReceiver class.
 */
public class CancelNotificationReceiver extends BroadcastReceiver {

	// Instance variables for drink updates
	private AlarmManager mAlarmManager;
	private Intent mDrinkUpdateReceiverIntent;
	private PendingIntent mDrinkUpdateReceiverPendingIntent;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ID of DrinkUpdateReceiver notification
		int notificationId = intent.getIntExtra(
				DrinkUpdateReceiver.MY_NOTIFICATION_ID_STRING, 1);
		cancelExistingAlarm(context);

		// Get NotificationManager and cancel existing notifs
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId);
	}

	/** Re-creates the existing alarm and cancels it */
	private void cancelExistingAlarm(Context context) {
		// Get the AlarmManager Service
		mAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// PendingIntent for drink updates
		mDrinkUpdateReceiverIntent = new Intent(DayListFragment.context,
				DrinkUpdateReceiver.class);

		mDrinkUpdateReceiverPendingIntent = PendingIntent.getBroadcast(
				DayListFragment.context, // context
				0, // requestCode
				mDrinkUpdateReceiverIntent, // intent
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Cancel the alarm
		mAlarmManager.cancel(mDrinkUpdateReceiverPendingIntent);
		mDrinkUpdateReceiverPendingIntent.cancel();

	}
}
