package com.virat.drinkingbuddy;

import java.util.UUID;

import android.support.v4.app.Fragment;

public class DrinkListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		UUID drinkLabId = (UUID)getIntent().getSerializableExtra(DrinkFragment.EXTRA_DRINKLAB_ID);
		DrinkLab drinkLab = (DrinkLab)getIntent().getParcelableExtra(DrinkListFragment.EXTRA_DRINKLAB);
		
		return DrinkListFragment.newInstance(drinkLabId, drinkLab);
	}

}
