package com.virat.drinkingbuddy;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	private static final String TAG = "SettingsFragment";

	protected static final String SHARED_PREFS = "shared_prefs";
	protected static final String NOTIFICATION_PREFS = "notifications_prefs";
	protected static final String SOUNDS_PREFS = "sounds_prefs";
	protected static final String VIBRATION_PREFS = "vibration_prefs";

	// Instance variables for drink updates
	private AlarmManager mAlarmManager;
	private Intent mDrinkUpdateReceiverIntent;
	private PendingIntent mDrinkUpdateReceiverPendingIntent;

	private TextView notificationsTextView;
	private TextView soundsTextView;
	private TextView vibrationTextView;

	private Switch mNotificationsSwitch;
	private Switch mSoundsSwitch;
	private Switch mVibrationSwitch;

	// Variables from shared preferences
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor editor;
	private boolean mNotificationPrefs;
	private boolean mSoundsPrefs;
	private boolean mVibrationPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		sharedPrefs = getActivity().getSharedPreferences(SHARED_PREFS,
				Context.MODE_PRIVATE);
		editor = sharedPrefs.edit();

		// get user's notification, sounds, vibration preferences
		mNotificationPrefs = sharedPrefs.getBoolean(NOTIFICATION_PREFS, true);
		mSoundsPrefs = sharedPrefs.getBoolean(SOUNDS_PREFS, true);
		mVibrationPrefs = sharedPrefs.getBoolean(VIBRATION_PREFS, true);

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		// save user's preferences to notifications
		editor.putBoolean(NOTIFICATION_PREFS, mNotificationPrefs);
		editor.putBoolean(SOUNDS_PREFS, mSoundsPrefs);
		editor.putBoolean(VIBRATION_PREFS, mVibrationPrefs);
		editor.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Create xml layout for this class
		View v = inflater.inflate(R.layout.settings_fragment, null);

		v.setBackgroundColor(Color.parseColor("#B1BDCD")); // light gray
															// background

		mNotificationsSwitch = (Switch) v
				.findViewById(R.id.notification_switch);

		mNotificationsSwitch.setChecked(mNotificationPrefs);
		mNotificationsSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// user wants notifications
							mNotificationPrefs = true;
						} else {
							mNotificationPrefs = false;

							// Get the AlarmManager Service
							mAlarmManager = (AlarmManager) getActivity()
									.getSystemService(Context.ALARM_SERVICE);

							// Create PendingIntent to start the
							// DrinkUpdateRecevier
							mDrinkUpdateReceiverIntent = new Intent(
									DayListFragment.context,
									DrinkUpdateReceiver.class);

							mDrinkUpdateReceiverPendingIntent = PendingIntent
									.getBroadcast(DayListFragment.context, // context
											0, // requestCode
											mDrinkUpdateReceiverIntent, // intent
											PendingIntent.FLAG_UPDATE_CURRENT);

							// cancel the alarm
							mAlarmManager
									.cancel(mDrinkUpdateReceiverPendingIntent);
							mDrinkUpdateReceiverPendingIntent.cancel();
						}
					}
				});

		mSoundsSwitch = (Switch) v.findViewById(R.id.sounds_switch);
		mSoundsSwitch.setChecked(mSoundsPrefs);
		mSoundsSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// user wants sound notifications
							mSoundsPrefs = true;
						} else {
							mSoundsPrefs = false;
						}
					}
				});

		mVibrationSwitch = (Switch) v.findViewById(R.id.vibration_switch);
		mVibrationSwitch.setChecked(mVibrationPrefs);
		mVibrationSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// user wants vibration on notifications
							mVibrationPrefs = true;
						} else {
							mVibrationPrefs = false;

						}
					}
				});

		return v;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
