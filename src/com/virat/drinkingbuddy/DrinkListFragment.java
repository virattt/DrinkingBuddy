package com.virat.drinkingbuddy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.virat.drinkingbuddy.dialogfragments.BACDialogFragment;
import com.virat.drinkingbuddy.dialogfragments.DoneDrinkingDialogFragment;
import com.virat.drinkingbuddy.dialogfragments.ProfileIncompleteFragment;
import com.virat.drinkingbuddy.models.DayLab;
import com.virat.drinkingbuddy.models.Drink;
import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.User;

public class DrinkListFragment extends ListFragment {

	public static final String EXTRA_DRINKLAB = "com.virat.drinkingbuddy.drinklab";
	public static final String EXTRA_DRINKS_ARRAY_SIZE = "com.virat.drinkingbuddy.drinks_array_size";
	public static final String EXTRA_USER_BAC = "com.virat.drinkingbuddy.user_bac";

	private static final String TAG = "DrinkListFragment";

	// Strings for dialog
	private static final String DIALOG_PROFILE = "profile";
	private static final String DIALOG_TERMS = "terms_and_conditions";
	private static final String DIALOG_DONE_DRINKING = "done_drinking";
	private static final String DIALOG_BAC = "dialog_bac";

	// Request IDs for Activity Result
	private static final int REQUEST_DRINKLAB = 0;
	private static final int REQUEST_TERMS = 1;
	private static final int REQUEST_DONE_DRINKING = 2;

	
	// Model level instance variables
	private ArrayList<Drink> mDrinks;
	private DrinkLab mDrinkLab;
	private UUID mDrinkLabId;
	private int mTotalCalories;
	private boolean mIsDrinking;

	private TextView mDrinksTextView; // drink count
	private TextView mTitleTextView; // title of drink
	private TextView mCaloriesTextView; // calories of drink
	private TextView mTimeTextView; // time of drink
	private TextView mTotalCaloriesTextView; // total calories
	private TextView mTotalDrinksTextView; // total drinks
	private TextView mTotalTimeTextView; // total drinking time
	private TextView mBacTextView; // BAC text view

	private Button mDoneDrinkingButton;
	private Button mBacButton;
	private Button mNewDrinkButton;

	public static DrinkListFragment newInstance(UUID drinkLabId,
			DrinkLab drinkLab) {
		Bundle args = new Bundle();
		args.putSerializable(DrinkFragment.EXTRA_DRINKLAB_ID, drinkLabId);
		args.putParcelable(EXTRA_DRINKLAB, drinkLab);

		DrinkListFragment fragment = new DrinkListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up ActionBar
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		setHasOptionsMenu(true);

		UUID drinkLabId = (UUID) getArguments().getSerializable(
				DrinkFragment.EXTRA_DRINKLAB_ID);

		// Get current instance of DrinkLab using DayLab singleton
		mDrinkLab = DayLab.get(getActivity()).getDrinkLab(drinkLabId);

		// Create ArrayList of Drinks for DrinkAdapter use
		mDrinks = mDrinkLab.getDrinks();

		mTotalCalories = mDrinkLab.getCalories();
		mIsDrinking = mDrinkLab.getIsDrinking();

		// Create and set the list adapter
		DrinkAdapter adapter = new DrinkAdapter(mDrinks);
		setListAdapter(adapter);
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.list_item_all_drink, null);
		v.setBackgroundColor(Color.parseColor("#B1BDCD"));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}

		ListView listView = (ListView) v.findViewById(android.R.id.list);

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
						DrinkAdapter adapter = (DrinkAdapter) getListAdapter();
						for (int i = adapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								mDrinkLab.deleteDrink(adapter.getItem(i));
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

		mNewDrinkButton = (Button) v
				.findViewById(R.id.drink_list_newDrinkButton);

		if (!mDrinkLab.getIsDrinking()) {
			mNewDrinkButton.setClickable(false);
		} else {
			mNewDrinkButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Drink drink = new Drink();

					DayLab.get(getActivity()).getDrinkLab(mDrinkLab.getId())
							.addDrink(drink);

					Intent i = new Intent(getActivity(), DrinkActivity.class);
					i.putExtra(DrinkFragment.EXTRA_DRINK_ID, drink.getId());
					i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID,
							mDrinkLab.getId());

					startActivityForResult(i, REQUEST_DRINKLAB);
				}
			});
		}
		// TextView for total drinks
		mTotalDrinksTextView = (TextView) v
				.findViewById(R.id.drink_list_drinkCountTextView);

		mTotalDrinksTextView.setText(mDrinkLab.getTotalDrinks() + "");

		// TextView for total calories
		mTotalCaloriesTextView = (TextView) v
				.findViewById(R.id.drink_list_totalCaloriesTextView);
		mTotalCaloriesTextView.setText(mDrinkLab.getCalories() + "");

		// TextView for total drinking time
		mTotalTimeTextView = (TextView) v
				.findViewById(R.id.drink_list_durationTextView);
		mTotalTimeTextView.setText(mDrinkLab.getDrinkingDuration(mDrinkLab));

		// Button for Done Drinking
		mDoneDrinkingButton = (Button) v
				.findViewById(R.id.drink_list_doneDrinkingButton);
		if (!mDrinkLab.getIsDrinking()) {
			mDoneDrinkingButton.setClickable(false);
		} else {
			mDoneDrinkingButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					FragmentManager fm = getActivity()
							.getSupportFragmentManager();
					DoneDrinkingDialogFragment dialog = new DoneDrinkingDialogFragment();
					dialog.setTargetFragment(DrinkListFragment.this,
							REQUEST_DONE_DRINKING);
					dialog.show(fm, DIALOG_DONE_DRINKING);
				}
			});
		}
		
		// Button for BAC
		mBacButton = (Button) v.findViewById(R.id.drink_list_BACButton);
		mBacButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userProfileIncomplete()) {
					FragmentManager fm = getActivity()
							.getSupportFragmentManager();
					ProfileIncompleteFragment dialog = new ProfileIncompleteFragment();
					dialog.show(fm, DIALOG_PROFILE);
				} else {
					FragmentManager fm = getActivity()
							.getSupportFragmentManager();
					BACDialogFragment dialog = new BACDialogFragment()
							.newInstance(mDrinkLab);
					dialog.show(fm, DIALOG_DONE_DRINKING);

				}
			}
		});
		
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_drink_list, menu);
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Drink d = ((DrinkAdapter) getListAdapter()).getItem(position);

		// Start DrinkActivity
		Intent i = new Intent(getActivity(), DrinkActivity.class);
		i.putExtra(DrinkFragment.EXTRA_DRINK_ID, d.getId());
		i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, mDrinkLab.getId());
		startActivityForResult(i, REQUEST_DRINKLAB);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == REQUEST_DRINKLAB) {
			mDrinkLab = data.getParcelableExtra(DrinkFragment.EXTRA_DRINKS_ARRAY);
		} else if (requestCode == REQUEST_DONE_DRINKING) {
			User.get(getActivity()).setIsDrinking(false);
			User.get(getActivity()).savePerson();
			mDrinkLab.setIsDrinking(false);
			mDoneDrinkingButton.setClickable(false);
			mNewDrinkButton.setClickable(false);
			
			// Re-create Intent to stop the Notification service
			Intent intent = createAlarmIntent();
			DrinkUpdates.stopUpdates(DayListFragment.context);
		}
	}
	/** Creates/re-creates Intent for starting/stopping alarm service */
	private Intent createAlarmIntent() {
		
		Intent intent = new Intent();
		intent.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID,
				mDrinkLab.getId());
		return intent;
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
		DrinkAdapter adapter = (DrinkAdapter) getListAdapter();
		Drink drink = adapter.getItem(position);

		switch (item.getItemId()) {
		case R.id.menu_item_delete_drink:
			// DrinkLab.get(getActivity()).deleteDrink(drink);
			mDrinkLab.deleteDrink(drink);
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/** Serves as the adapter for the ListView in DrinkListFragment */
	private class DrinkAdapter extends ArrayAdapter<Drink> {

		public DrinkAdapter(ArrayList<Drink> drinks) {
			super(getActivity(), 0, drinks);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if not given a View, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.list_item_drink, null);
			}

			// Configure the view for this Drink
			Drink d = getItem(position);

			mDrinksTextView = (TextView) convertView
					.findViewById(R.id.drink_list_item_drinksTextView);
			mDrinksTextView.setText(getPosition(d) + 1 + ""); // get the
																// position for
																// each drink

			mTitleTextView = (TextView) convertView
					.findViewById(R.id.drink_list_item_titleTextView);
			mTitleTextView.setText(d.getTitle());
			mCaloriesTextView = (TextView) convertView
					.findViewById(R.id.drink_list_item_caloriesTextView);
			mCaloriesTextView.setText(d.getCalories() + " Calories");
			mTimeTextView = (TextView) convertView
					.findViewById(R.id.drink_list_item_timeTextView);
			mTimeTextView.setText(formatTime(d.getTime()) + "");

			return convertView;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		((DrinkAdapter) getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		DayLab.get(getActivity()).saveDrinkLab();
	}

	/** Formats the Time and returns it as a formatted String */
	private String formatTime(Date time) {
		SimpleDateFormat fmt = new SimpleDateFormat("h:mm a");
		return fmt.format(time);
	}

	private boolean userProfileIncomplete() {
		if (User.get(getActivity()).getWeight() == null
				|| User.get(getActivity()).getWeight().equals("")
				|| User.get(getActivity()).getGender().equals("none")) {
			return true;
		}
		return false;
	}
}
