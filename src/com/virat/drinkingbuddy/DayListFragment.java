package com.virat.drinkingbuddy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.virat.drinkingbuddy.dialogfragments.TermsAndConditionsDialogFragment;
import com.virat.drinkingbuddy.dialogfragments.TodaySessionExistDialogFragment;
import com.virat.drinkingbuddy.models.DayLab;
import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.Person;

/*
 * This class displays the list of DrinkLabs (drinking sessions) 
 * and lets the user create new DrinkLab 
 */
public class DayListFragment extends ListFragment {

	private static final String DIALOG_TERMS = "terms_and_conditions";
	private static final String DIALOG_SESSION_EXISTS = "session_exists";

	public static final String TERMS_AND_CONDITIONS = "TermsAndConditions";

	private static final int REQUEST_TERMS = 0;
	private static final int REQUEST_SESSION_EXISTS = 1;

	// Array of DrinkLab(s)
	private ArrayList<DrinkLab> mDrinkDays;

	// UI components
	private TextView mDayTextView;
	private TextView mCaloriesTextView;
	private TextView mDateTextView;
	private TextView mSessionStatus;


	public static Context context;

	private boolean mTermsAgreed;

	private Button mStartDrinkingButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Customize the ActionBar
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		mDrinkDays = DayLab.get(getActivity()).getDrinkLabs();

		// Get and set the adapter
		DayAdapter adapter = new DayAdapter(mDrinkDays);
		setListAdapter(adapter);
		setHasOptionsMenu(true);

		// Set this class' context reference for system-wide notification creation
		DayListFragment.context = getActivity().getApplicationContext();

		// Set up Shared Prefs for Terms & Conditions
		SharedPreferences terms_settings = this.getActivity()
				.getSharedPreferences(TERMS_AND_CONDITIONS, 0);
		mTermsAgreed = terms_settings.getBoolean("terms agreed", false);

		// Check if Terms have been seen
		if (mTermsAgreed == false) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			TermsAndConditionsDialogFragment dialog = new TermsAndConditionsDialogFragment();
			dialog.setTargetFragment(DayListFragment.this, REQUEST_TERMS);
			dialog.show(fm, DIALOG_TERMS);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		// Terms & Conditions result
		if (requestCode == REQUEST_TERMS) {
			
			// True = Terms accepted; False = Terms rejected
			mTermsAgreed = data.getBooleanExtra(
					TermsAndConditionsDialogFragment.EXTRA_TERMS_AGREED, false);
			
			// Get reference to SharedPrefs
			SharedPreferences terms_settings = this.getActivity()
					.getSharedPreferences(TERMS_AND_CONDITIONS, 0);
			
			// Editor object to make preference changes
			SharedPreferences.Editor editor = terms_settings.edit();

			if (mTermsAgreed == false) {
				// User didn't agree to the Terms
				editor.putBoolean("terms agreed", mTermsAgreed);
				editor.commit();

				// Force kill application
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				// User agreed to Terms, run App as usual
				editor.putBoolean("terms agreed", mTermsAgreed);
				editor.commit();
			}
		} else if (requestCode == REQUEST_SESSION_EXISTS) {
			// Receive user's choice to create 1+ sessions in same day
			boolean create_new_session = data
					.getBooleanExtra(TodaySessionExistDialogFragment.EXTRA_CREATE_ANOTHER_SESSION,
									false);

			// Cancel current alarm and create new session
			if (create_new_session) {
				cancelExistingAlarm();
				createNewDrinkLab();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.list_item_all_day_list_frag, null);
		v.setBackgroundColor(Color.parseColor("#B1BDCD")); // light gray

		mStartDrinkingButton = (Button) v
				.findViewById(R.id.day_list_startDrinkingButton);
		mStartDrinkingButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (todaySessionExists()) {
					FragmentManager fm = getActivity()
							.getSupportFragmentManager();
					TodaySessionExistDialogFragment dialog = new TodaySessionExistDialogFragment();
					dialog.setTargetFragment(DayListFragment.this,
							REQUEST_SESSION_EXISTS);
					dialog.show(fm, DIALOG_SESSION_EXISTS);
				} else {

					// Cancel existing alarm
					cancelExistingAlarm();

					createNewDrinkLab();
				}
			}
		});

		// ListView object
		ListView listView = (ListView) v.findViewById(android.R.id.list);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// use floating context menu on Froyo and Gingerbread
			registerForContextMenu(listView);
		} else {
			// use contextual action bar on Honeycomb and higher
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Required. but not used in this implementation
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// Required, but not used in this implementation
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.drink_list_item_context, menu);
					return true;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode,
						MenuItem item) {
					switch (item.getItemId()) {
					case R.id.menu_item_delete_drink:
						DayAdapter adapter = (DayAdapter) getListAdapter();
						DayLab dayLab = DayLab.get(getActivity());
						for (int i = adapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								dayLab.deleteDrinkLab(adapter.getItem(i));
							}
						}
						mode.finish();
						adapter.notifyDataSetChanged();
						return true;
					default:
						return false;
					}
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode,
						int position, long id, boolean checked) {
					// Required but not used in this implementation
				}
			});
		}

		return v;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.drink_list_item_context,
				menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int position = info.position;
		DayAdapter adapter = (DayAdapter) getListAdapter();
		DrinkLab drinkLab = adapter.getItem(position);

		switch (item.getItemId()) {
		case R.id.menu_item_delete_drink:
			// DrinkLab.get(getActivity()).deleteDrink(drink);
			DayLab.get(getActivity()).deleteDrinkLab(drinkLab);
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		DrinkLab d = ((DayAdapter) getListAdapter()).getItem(position);

		// Start DrinkListActivity
		Intent i = new Intent(getActivity(), DrinkListActivity.class);
		i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, d.getId());
		i.putExtra(DrinkListFragment.EXTRA_DRINKLAB, d);
		startActivity(i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_day_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
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

	/** Custom adapter that holds DrinkLabs for this ListFragment */
	private class DayAdapter extends ArrayAdapter<DrinkLab> {

		public DayAdapter(ArrayList<DrinkLab> drinkLabs) {
			super(getActivity(), 0, drinkLabs);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we aren't given a view, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.list_item_day, null);
			}

			// Configure the view for this DrinkLab
			DrinkLab d = getItem(position);

			TextView drinkCountTextView = (TextView) convertView
					.findViewById(R.id.day_list_item_drinkCountTextView);

			// Check for drink count plurality
			if (d.getDrinkCount() == 1) {
				drinkCountTextView.setText(d.getDrinkCount() + " Drink");
			} else {
				drinkCountTextView.setText(d.getDrinkCount() + " Drinks");
			}

			mDayTextView = (TextView) convertView.findViewById(R.id.day_list_item_dayTextView);
			
			// If current DrinkLab is today, then display "Today" instead of day of week
			if (isToday(d)) {
				mDayTextView.setText("Today");
			} else {
				mDayTextView.setText(formatDay(d.getDate()));
			}
			
			// Display DrinkLab calories
			mCaloriesTextView = (TextView) convertView.findViewById(R.id.day_list_item_caloriesTextView);
			mCaloriesTextView.setText(d.getCalories() + " Calories");

			// Display DrinkLab's date
			mDateTextView = (TextView) convertView.findViewById(R.id.day_list_item_dateTextView);
			mDateTextView.setText(formatDate(d.getDate()));

			mSessionStatus = (TextView) convertView.findViewById(R.id.day_list_item_session_status);

			// Display current DrinkLab's activity status (Drinking/Done Drinking)
			if (d.getIsDrinking()) {
				mSessionStatus.setText("Drinking");
				mSessionStatus.setTypeface(null, Typeface.BOLD);
			} else {
				mSessionStatus.setText("Done Drinking");
				mSessionStatus.setTypeface(null, Typeface.ITALIC);
			}

			return convertView;
		}
	}
	
	/** Checks if a DrinkLab for today already exists */
	private boolean todaySessionExists() {
		// Today's Date
		Date todayDate = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("M/d/yyyy");
		String todayDateStr = fmt.format(todayDate);

		for (DrinkLab drinkLab : DayLab.get(getActivity()).getDrinkLabs()) {
			// Get date of current drinkLab
			Date drinkLabDate = drinkLab.getDate();
			String drinkLabDateStr = fmt.format(drinkLabDate);

			// Check if drinkLab's date is today
			if (todayDateStr.equals(drinkLabDateStr)) {
				return true;
			}
		}
		return false;
	}
	/** Helper class that checks if a DrinkLab is for today */
	private boolean isToday(DrinkLab drinkLab) {
		
		// Today's date
		Date todayDate = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("M/d/yyyy");
		String todayDateStr = fmt.format(todayDate);
		String drinkLabDateStr = fmt.format(drinkLab.getDate());

		if (todayDateStr.equals(drinkLabDateStr))
			return true;

		return false;
	}

	/** Formats DrinkLab date to day of week */
	private String formatDay(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat("EEEE");
		return fmt.format(date);
	}
	
	/** Formats DrinkLab's date - i.e. April 6, 2014 */
	private String formatDate(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy");
		return fmt.format(date);
	}

	/** Static helper method that returns this class' context */
	public static Context getAppContext() {
		return DayListFragment.context;
	}

	/** Helper method that creates a new DrinkLab */
	private void createNewDrinkLab() {
		// Mark previous sessions as Done Drinking
		for (DrinkLab drinkLab : DayLab.get(getActivity()).getDrinkLabs()) {
			drinkLab.setIsDrinking(false);
		}

		// Create new DrinkLab
		DrinkLab drinkLab = new DrinkLab();
		DayLab.get(getActivity()).addDrinkLab(drinkLab);
		Intent i = new Intent(getActivity(), DrinkListActivity.class);
		i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, drinkLab.getId());
		i.putExtra(DrinkListFragment.EXTRA_DRINKLAB, drinkLab);

		// Person is now drinking
		Person.get(getActivity()).setIsDrinking(true);
		Person.get(getActivity()).savePerson();

		startActivityForResult(i, 0);
	}

	/** Helper method that cancels the existing alarm */
	private void cancelExistingAlarm() {
		
		// Cancel the alarm
		Intent intent = new Intent();
		DrinkUpdateService.setServiceAlarm(DayListFragment.context, false, intent);
	}


	@Override
	public void onResume() {
		super.onResume();
		((DayAdapter) getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}	
}
