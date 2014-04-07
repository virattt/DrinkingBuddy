package com.virat.drinkingbuddy;

import java.util.ArrayList;
import java.util.List;

import com.virat.drinkingbuddy.navdrawer.CustomDrawerAdapter;
import com.virat.drinkingbuddy.navdrawer.DrawerItem;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {
	
	public static final String ITEM_SELECTED = "item_selected";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	CustomDrawerAdapter mAdapter;

	List<DrawerItem> dataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initializing
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Add Drawer Item to dataList
		dataList.add(new DrawerItem("Home"));
		dataList.add(new DrawerItem("Profile"));
		dataList.add(new DrawerItem("Settings"));		
		dataList.add(new DrawerItem("Disclaimer"));

		mAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
				dataList);

		mDrawerList.setAdapter(mAdapter);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.drawer_sample, 
				R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
		      selectItem(0);
		}
		
		Bundle b = getIntent().getExtras();
		
		if (b != null) {
			int value = b.getInt(ITEM_SELECTED);
			selectItem(value);
		}	
	}

	public void selectItem(int position) {
		Fragment fragment = null;
		Bundle args = new Bundle();
		switch (position) {
		case 0:
			fragment = new DayListFragment();
			break;
		case 1:
			fragment = new UserFragment();
			break;
		case 2:
			fragment = new SettingsFragment();
			break;
		case 3:
			fragment = new DisclaimerFragment();
			break;
		default:
			break;
		}

		fragment.setArguments(args);
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}
}
