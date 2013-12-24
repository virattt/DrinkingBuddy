package com.virat.drinkingbuddy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class DayListFragment extends ListFragment {
	
	private static final String TAG = "DayListFragment";
	
	private ArrayList<DrinkLab> mDrinkDays;
	private TextView mDayTextView;
	private TextView mCaloriesTextView;
	private TextView mDateTextView;
	
	private Button mStartDrinkingButton;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.actionbar); 
		
		mDrinkDays = DayLab.get(getActivity()).getDrinkLabs();
		
		DayAdapter adapter = new DayAdapter(mDrinkDays);		
		setListAdapter(adapter);
		setHasOptionsMenu(true);
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.list_item_all_day_list_frag, null);
		v.setBackgroundColor(getResources().getColor(R.color.dodgerblue));
		
		mStartDrinkingButton = (Button)v.findViewById(R.id.day_list_startDrinkingButton);
		mStartDrinkingButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DrinkLab drinkLab = new DrinkLab();				
				DayLab.get(getActivity()).addDrinkLab(drinkLab);
				Intent i = new Intent(getActivity(), DrinkListActivity.class);
				i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, drinkLab.getId());
				i.putExtra(DrinkListFragment.EXTRA_DRINKLAB, drinkLab);
				startActivityForResult(i, 0);
			}
		});
		
		ListView listView = (ListView)v.findViewById(android.R.id.list);
	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// use floating context menu on Froyo and Gingerbread
			registerForContextMenu(listView);
		}	else {
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
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.menu_item_delete_drink:
							DayAdapter adapter = (DayAdapter)getListAdapter();
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
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					// Required but not used in this implementation
				}
			});
		}
		
		return v;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.drink_list_item_context, menu);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
	 * Deletes a Drink using a floating context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		DayAdapter adapter = (DayAdapter)getListAdapter();
		DrinkLab drinkLab = adapter.getItem(position);
		
		switch (item.getItemId()) {
			case R.id.menu_item_delete_drink:
				//DrinkLab.get(getActivity()).deleteDrink(drink);
				DayLab.get(getActivity()).deleteDrinkLab(drinkLab);
				adapter.notifyDataSetChanged();
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		DrinkLab d = ((DayAdapter)getListAdapter()).getItem(position);
		
		// Start DrinkListActivity
		Intent i = new Intent(getActivity(), DrinkListActivity.class);
		i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, d.getId());
		i.putExtra(DrinkListFragment.EXTRA_DRINKLAB, d);
		Log.d(TAG, "UUID is: " + d.getId());
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
		switch(item.getItemId()) {
			case R.id.menu_item_user_profile:
				i = new Intent(getActivity(), UserActivity.class);
				startActivity(i);
				return true;
			/*case R.id.menu_item_new_drink_day:
				DrinkLab drinkLab = new DrinkLab();				
				DayLab.get(getActivity()).addDrinkLab(drinkLab);
				i = new Intent(getActivity(), DrinkListActivity.class);
				i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, drinkLab.getId());
				i.putExtra(DrinkListFragment.EXTRA_DRINKLAB, drinkLab);
				startActivityForResult(i, 0);				
				return true; */
			case android.R.id.home:
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					NavUtils.navigateUpFromSameTask(getActivity());
				}
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class DayAdapter extends ArrayAdapter<DrinkLab> {
	
		public DayAdapter(ArrayList<DrinkLab> drinkLabs) {
			super(getActivity(), 0, drinkLabs);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we aren't given a view, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.list_item_day, null);
			}
			
			// Configure the view for this DrinkLab
			DrinkLab d = getItem(position);
			
			
			
			TextView drinkCountTextView = 
					(TextView)convertView.findViewById(R.id.day_list_item_drinkCountTextView);
			
			// see if drink count is plural
			if (d.getDrinkCount() == 1) {
				drinkCountTextView.setText(d.getDrinkCount() + " Drink");
			} else {

				drinkCountTextView.setText(d.getDrinkCount() + " Drinks");
			}
			
			mDayTextView = (TextView)convertView.findViewById(R.id.day_list_item_dayTextView);
			mDayTextView.setText(formatDay(d.getDate()));
			
			mCaloriesTextView = (TextView)convertView.findViewById(R.id.day_list_item_caloriesTextView);
			mCaloriesTextView.setText("Calories: " + d.getCalories());
			
			mDateTextView = (TextView)convertView.findViewById(R.id.day_list_item_dateTextView);
			mDateTextView.setText(formatDate(d.getDate()));
			
			return convertView;
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		((DayAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	private String formatDay(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat("EEEE");
		return fmt.format(date);
	}
	
	private String formatDate(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat("MMM. d, yyyy");
		return fmt.format(date);
	}
}
