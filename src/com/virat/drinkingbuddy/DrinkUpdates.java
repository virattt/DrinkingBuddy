package com.virat.drinkingbuddy;

import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DrinkUpdates {
	public static final String EXTRA_DRINKLAB_ID = 
			"com.virat.drinkingbuddy.drinkupdates.drinklab_id";
	
	// Variables to start/stop updates
	private static AlarmManager mAlarmManager;
	private static Intent mDrinkUpdateIntent;
	private static PendingIntent mDrinkUpdatePendingIntent;
	
	/** Starts drink updates with data from given Intent */
	public static void startUpdates(Context context, Intent intent) {
		
		// Get DrinkLab ID from given intent	
		UUID drinkLabId = 
				(UUID) intent.getSerializableExtra(DrinkFragment.EXTRA_DRINKLAB_ID);
		
		// Create Intent and PendingIntent for AlarmManager
		mDrinkUpdateIntent = new Intent(context, DrinkUpdateReceiver.class);
		mDrinkUpdateIntent.putExtra(EXTRA_DRINKLAB_ID, drinkLabId);
		
		mDrinkUpdatePendingIntent = 
				PendingIntent.getBroadcast(context, 0, mDrinkUpdateIntent, 0);
		
		// Start the drink updates
		mAlarmManager = 
				(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		mAlarmManager.set(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_HOUR, 
				mDrinkUpdatePendingIntent);
	}
	
	/** Stops drink updates */
	public static void stopUpdates(Context context) {
		
		// Re-create Intent and PendingIntent for AlarmManager
		mDrinkUpdateIntent = new Intent(context, DrinkUpdateReceiver.class);	
		mDrinkUpdatePendingIntent = 
				PendingIntent.getBroadcast(context, 0, mDrinkUpdateIntent, 0);
				
		// Stop the drink updates
		mAlarmManager = 
				(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				
		mAlarmManager.cancel(mDrinkUpdatePendingIntent);
		mDrinkUpdatePendingIntent.cancel();
		
	}

}
