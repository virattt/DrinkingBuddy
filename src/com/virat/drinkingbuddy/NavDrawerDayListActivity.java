package com.virat.drinkingbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavDrawerDayListActivity extends Activity {
	private String[] mNavDrawerTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_layout);

		mNavDrawerTitles = getResources().getStringArray(
				R.array.nav_drawer_titles);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for this list
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mNavDrawerTitles));

		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle("drinkster");
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("drinkster");
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

	};

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {

			// Get String value of selected item
			String selected = parent.getItemAtPosition(position).toString();

			// Create a new empty fragment
			Fragment fragment = new EmptyFragment();
			Bundle args = new Bundle();

			// Add String value of selected item into EmptyFragment's Bundle
			args.putString(EmptyFragment.ARG_ITEM_TITLE, selected);
			fragment.setArguments(args);

			// Begin the fragment transaction
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(position, fragment)
					.commit();

			// selectItem(position);
		}
	}

	public static class EmptyFragment extends Fragment {
		public static final String ARG_ITEM_TITLE = "item_title";

		private String mItemTitle;
		private FragmentManager mFragmentManager;

		// Empty ctor
		public EmptyFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mItemTitle = getArguments().getString(ARG_ITEM_TITLE);

			if (mItemTitle.equals("Home")) {
				launchDayListFragment();
			} else if (mItemTitle.equals("Profile")) {
				launchUserFragment();
			}

		}

		public DayListFragment launchDayListFragment() {
			return new DayListFragment();
		}

		public UserFragment launchUserFragment() {
			return new UserFragment();
		}

	}
}
