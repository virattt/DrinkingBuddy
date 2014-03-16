package com.virat.drinkingbuddy;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class UserFragment extends Fragment {
	
	private EditText mPersonName;
	private EditText mPersonWeight;
	private RadioGroup mRadioGenderGroup;
	private RadioButton mRadioFemaleButton;
	private RadioButton mRadioMaleButton;
	private Button mDoneButton;
	private TextView mUserProfileIncompleteTextView;
	
	// private static final String TAG = "UserFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.actionbar);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.user_fragment, null);
		
		v.setBackgroundColor(Color.parseColor("#B1BDCD")); // light gray background
		
		mPersonName = (EditText)v.findViewById(R.id.user_name_editText);
		mPersonName.setText(Person.get(getActivity()).getName());
		mPersonName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Person.get(getActivity()).setName(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		
		});
		
		mPersonWeight = (EditText)v.findViewById(R.id.user_weight_editText);
		mPersonWeight.setText(Person.get(getActivity()).getWeight());
		mPersonWeight.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Person.get(getActivity()).setWeight(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		
		});
		
		mRadioFemaleButton = (RadioButton)v.findViewById(R.id.user_female_radioButton);
		mRadioMaleButton = (RadioButton)v.findViewById(R.id.user_male_radioButton);
		
		// Check if user has already specified their gender
		if (Person.get(getActivity()).getGender().equals("female")) {
			mRadioFemaleButton.setChecked(true);
		} else if (Person.get(getActivity()).getGender().equals("male")) {
			mRadioMaleButton.setChecked(true);
		}
		
		mRadioGenderGroup = (RadioGroup)v.findViewById(R.id.radioGender);
		mRadioGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// Check which radio button is clicked
				switch(checkedId) {
					case R.id.user_female_radioButton:
						Person.get(getActivity()).setGender("female"); // user is female
						break;
					case R.id.user_male_radioButton:
						Person.get(getActivity()).setGender("male"); // user is male
						break;
					default:
						break;
				}
			}
		});
		
		mUserProfileIncompleteTextView = (TextView)v.findViewById(R.id.user_profile_incomplete_textView);
		
		mDoneButton = (Button)v.findViewById(R.id.user_done_button);
		mDoneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// check if user has entered a valid weight
				if (weightIncomplete() || nameIncomplete()) {
					mUserProfileIncompleteTextView.setVisibility(View.VISIBLE);
					
				} else {
					mUserProfileIncompleteTextView.setVisibility(View.INVISIBLE);
					Person.get(getActivity()).savePerson();
					Toast.makeText(getActivity(), "Profile Saved!", Toast.LENGTH_SHORT).show();
					getActivity().finish();	
				}
			}
		});
		
		return v;
	}
	
	// Check if user has entered weight
	private boolean weightIncomplete() {
		if (Person.get(getActivity()).getWeight() == null || 
				Person.get(getActivity()).getWeight().equals("") ||
				Person.get(getActivity()).getWeight().equals("0")) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean nameIncomplete() {
		if (Person.get(getActivity()).getName() == null || 
				Person.get(getActivity()).getName().equals("")) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					NavUtils.navigateUpFromSameTask(getActivity());
				}
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Person.get(getActivity()).savePerson();
	}
	

}
