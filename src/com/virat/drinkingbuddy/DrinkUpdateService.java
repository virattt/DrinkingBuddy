/*
 * Copyright (C) Drinkster
 * Developer: Virat Singh
 */

package com.virat.drinkingbuddy;

import java.util.UUID;

import com.virat.drinkingbuddy.models.DayLab;
import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.User;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

/**
 * Toggles the notification system on and off. Handles
 * a PendingIntent passed to its onHandleIntent class 
 * by the AlarmManager and creates a drink update Notification
 * with this PendingIntent.
 */

public class DrinkUpdateService extends IntentService {
	private static final String TAG = "DrinkUpdateService";
	
	private static final int MY_NOTIFICATION_ID = 1;

	public static final String MY_NOTIFICATION_ID_STRING = "notification_id";
	
	// Notification sound and vibration
	private Uri mSound = 
			RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

	private long[] mVibratePattern = { 0, 200, 200, 300 };
	

	public DrinkUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		sendNotification(intent);
	}
	
	/** Toggles on and off the AlarmManager */
	public static void setServiceAlarm(Context context, boolean isOn, Intent intent) {
		
		// Create copy of Intent for the Notification
		Intent i = new Intent(intent);
		i.setClass(context, DrinkUpdateService.class);
		PendingIntent pi = 
				PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = 
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (isOn) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, 
					System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_HOUR + AlarmManager.INTERVAL_FIFTEEN_MINUTES,  
					pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}
	
	/** Creates a new Notification and pushes it to the user */
	private void sendNotification(Intent intent) {
		// Get user's SharedPreferences on Notification vibration and sound
		SharedPreferences sharedPrefs = getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
		boolean soundsPrefs = sharedPrefs.getBoolean(SettingsFragment.SOUNDS_PREFS, true);
		boolean vibrationPrefs = sharedPrefs.getBoolean(SettingsFragment.VIBRATION_PREFS, true);
		
		// DrinkLab variables for the Notification's content intent
		UUID drinkLabId = 
				(UUID) intent.getSerializableExtra(DrinkFragment.EXTRA_DRINKLAB_ID);
		
		// Create an Intent and PendingIntent for launching app on notification click
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = 
				PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		// Create an Intent and PendingIntent for dismissing notifications
		Intent notificationCancelIntent = new Intent(DayListFragment.context, CancelNotificationReceiver.class);
		notificationCancelIntent.putExtra(MY_NOTIFICATION_ID_STRING, MY_NOTIFICATION_ID);
		PendingIntent contentCancelIntent = PendingIntent.getBroadcast(DayListFragment.context, 0, notificationCancelIntent, 0);

		if (DayLab.get(this).getDrinkLab(drinkLabId) != null) {
			int total_drinks = 
					DayLab.get(this).getDrinkLab(drinkLabId).getDrinkCount();
			
			double BAC;

			// Check if user has completed their profile, if not, return 0.00 for BAC
			if (User.get(this).userProfileIncomplete()) {
				BAC = 0.00;
			} else {
				BAC = DayLab.get(this).getDrinkLab(drinkLabId).getBAC();
			}

			String notificationTitle = "Drinkster";
			String notificationText = "Total Drinks: " + total_drinks
					+ "\n\nEstimate BAC: " + BAC;

			Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
			
			// Sets a title for the Inbox style big view
			inboxStyle.setBigContentTitle(notificationTitle);
			inboxStyle.addLine("Total Drinks: " + total_drinks);
			inboxStyle.addLine("Estimated BAC: " + BAC + "%");

			// Build the Notification
			Notification.Builder notificationBuilder = new Notification.Builder(
					this)
					.setTicker(notificationText)
					.setSmallIcon(R.drawable.ic_stat_notification_logo_drink_fragment)
					.setAutoCancel(true)
					.setContentTitle(notificationTitle)
					.setContentText(notificationText)
					.setContentIntent(contentIntent)
					.setStyle(inboxStyle);
					//.addAction(R.drawable.ic_action_cancel, "Dismiss Drink Updates",
						//	contentCancelIntent);

			// Check if user wants sound on Notification
			if (soundsPrefs) {
				notificationBuilder.setSound(mSound);
			}

			// Check if user wants vibration on Notification
			if (vibrationPrefs) {
				notificationBuilder.setVibrate(mVibratePattern);
			}

			// Pass the Notification to the NotificationManager:
			NotificationManager mNotificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
			}
		}
	}

