package com.virat.drinkingbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class HomeFragment extends Fragment {
	
	private ImageButton mUserButton;
	private ImageButton mStartDrinkingButton;
	private ImageButton mDisclaimerButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.home_fragment, null);
		
		mUserButton = (ImageButton)v.findViewById(R.id.user_profile_imageButton);
		mUserButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), UserActivity.class);
				startActivity(i);
			}
		});
		
		mStartDrinkingButton = (ImageButton)v.findViewById(R.id.start_drinking_imageButton);
		mStartDrinkingButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), DayListActivity.class);
				startActivity(i);
			}
		});
		
		return v;
	}
}
