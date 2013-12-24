package com.virat.drinkingbuddy;

import java.util.ArrayList;
import java.util.UUID;

import android.support.v4.app.Fragment;


public class DrinkActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		
		UUID drinkId = (UUID)getIntent().getSerializableExtra(DrinkFragment.EXTRA_DRINK_ID);
		UUID drinkLabId = (UUID)getIntent().getSerializableExtra(DrinkFragment.EXTRA_DRINKLAB_ID);
		
		//DrinkLab drinksArray = (DrinkLab)getIntent().getParcelableExtra(DrinkFragment.EXTRA_DRINKS_ARRAY);
		
		return DrinkFragment.newInstance(drinkId, drinkLabId);
	}

}
