package com.virat.drinkingbuddy;

import android.support.v4.app.Fragment;


public class DayListActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new DayListFragment();
	}
	
}
