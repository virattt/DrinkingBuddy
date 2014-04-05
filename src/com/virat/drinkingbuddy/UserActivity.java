package com.virat.drinkingbuddy;

import android.support.v4.app.Fragment;


public class UserActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new UserFragment();
	}
}
