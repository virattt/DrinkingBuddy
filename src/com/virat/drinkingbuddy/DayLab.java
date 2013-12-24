package com.virat.drinkingbuddy;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

public class DayLab {

	private static final String TAG = "DrinkLab";
	private static final String FILENAME = "drinks.json";
	
	private ArrayList<DrinkLab> mDrinkDays;
	private DrinkingBuddyJSONSerializer mSerializer;
	
	private static DayLab sDayLab;
	private Context mAppContext;
	
	private DayLab(Context appContext) {
		mAppContext = appContext;

		mSerializer = new DrinkingBuddyJSONSerializer(mAppContext, FILENAME);
		
		//mDrinkDays = new ArrayList<DrinkLab>();
		
		try {
			mDrinkDays = mSerializer.loadDrinkLab();
			Log.d(TAG, "Drinks loaded successfully");
		} catch (Exception e) {
			mDrinkDays = new ArrayList<DrinkLab>();
			Log.e(TAG, "Drinks NOT loaded successfully", e);
		}
	}
	
	public static DayLab get(Context c) {
		if (sDayLab == null) {
			sDayLab = new DayLab(c.getApplicationContext());
		}
		return sDayLab;
	}
	
	public ArrayList<DrinkLab> getDrinkLabs() {
		return mDrinkDays;
	}

	public void addDrinkLab(DrinkLab d) {
		mDrinkDays.add(0, d);
	}
	
	public void deleteDrinkLab(DrinkLab d) {
		mDrinkDays.remove(d);
	}
	
	public DrinkLab getDrinkLab(UUID id) {
		for (DrinkLab d : mDrinkDays) {
			if (d.getId().equals(id))
				return d;
		}
		return null;
	}
	
	public boolean saveDrinkLab() {
		try {
			mSerializer.saveDrinkLab(mDrinkDays);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
