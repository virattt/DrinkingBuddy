package com.virat.drinkingbuddy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 * This class inflates the Terms & Conditions 
 */
public class DisclaimerFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate XML layout for this class
		View v = inflater.inflate(R.layout.terms_conditions_fragment, null);

		// Light gray background
		v.setBackgroundColor(Color.parseColor("#B1BDCD"));

		return v;
	}
}
