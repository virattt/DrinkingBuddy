package com.virat.drinkingbuddy;

import java.util.UUID;

import com.virat.drinkingbuddy.models.DayLab;
import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.Person;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

public class DrinkUpdateReceiver extends BroadcastReceiver {
	private static final int MY_NOTIFICATION_ID = 1;

	public static final String MY_NOTIFICATION_ID_STRING = "notification_id";

	// Variables for user preferences
	private SharedPreferences sharedPrefs;
	private boolean mSoundsPrefs;
	private boolean mVibrationPrefs;

	// Notification Action Elements
	private Intent mNotificationIntent;
	private Intent mNotificationCancelIntent;
	private PendingIntent mContentIntent;
	private PendingIntent mContentCancelIntent;

	// Notification Sound and Vibration on Arrival
	private Uri mSound = RingtoneManager
			.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

	private long[] mVibratePattern = { 0, 200, 200, 300 };

	@Override
	public void onReceive(Context context, Intent intent) {

		sharedPrefs = context.getSharedPreferences(
				SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
		mSoundsPrefs = sharedPrefs.getBoolean(SettingsFragment.SOUNDS_PREFS,
				true);
		mVibrationPrefs = sharedPrefs.getBoolean(
				SettingsFragment.VIBRATION_PREFS, true);

		// DrinkLab variables for the Notification's content intent
		UUID drinkLabId = (UUID) intent
				.getSerializableExtra(DrinkFragment.EXTRA_DRINKLAB_ID);
		DrinkLab drinkLab = (DrinkLab) intent
				.getParcelableExtra(DrinkListFragment.EXTRA_DRINKLAB);

		// Create an Intent for opening up app on notification click
		mNotificationIntent = new Intent(context, MainActivity.class);

		mContentIntent = PendingIntent.getActivity(context, 0,
				mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		// Create an Intent/PI for dismissing notifications
		mNotificationCancelIntent = new Intent(context,
				CancelNotificationReceiver.class);
		mNotificationCancelIntent.putExtra(MY_NOTIFICATION_ID_STRING,
				MY_NOTIFICATION_ID);
		mContentCancelIntent = PendingIntent.getBroadcast(context, 0,
				mNotificationCancelIntent, 0);

		if (DayLab.get(context).getDrinkLab(drinkLabId) != null) {
			int total_drinks = DayLab.get(context).getDrinkLab(drinkLabId)
					.getDrinkCount();
			double BAC;

			// Check if user has completed their profile,
			// if not, return 0.00 for BAC
			if (Person.get(context).userProfileIncomplete()) {
				BAC = 0.00;
			} else {
				BAC = DayLab.get(context).getDrinkLab(drinkLabId).getBAC();
			}

			String notificationTitle = "Drinkster";
			String notificationText = "Total Drinks: " + total_drinks
					+ "\n\nEstimate BAC: " + BAC;

			Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
			// Sets a title for the Inbox style big view
			inboxStyle.setBigContentTitle(notificationTitle);
			inboxStyle.addLine("Total Drinks: " + total_drinks);
			inboxStyle.addLine("Estimated BAC: " + BAC + "%");

			Notification.Builder notificationBuilder = new Notification.Builder(
					context)
					.setTicker(notificationText)
					.setSmallIcon(
							R.drawable.ic_stat_notification_logo_drink_fragment)
					.setAutoCancel(true)
					.setContentTitle(notificationTitle)
					.setContentText(notificationText)
					.setContentIntent(mContentIntent)
					.setStyle(inboxStyle)
					.addAction(R.drawable.ic_action_cancel, "Dismiss",
							mContentCancelIntent);

			if (mSoundsPrefs) {
				notificationBuilder.setSound(mSound);
			}

			if (mVibrationPrefs) {
				notificationBuilder.setVibrate(mVibratePattern);
			}

			// Pass the Notification to the NotificationManager:
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID,
					notificationBuilder.build());
		}
	}
}
