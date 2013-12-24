package com.virat.drinkingbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

public class CustomDrinkFragment extends DialogFragment {

	private static 	final String TAG = "CustomDrinkFragment"; // for debugging
	
	public static final String EXTRA_CUSTOM_DRINK_NAME = "com.virat.drinkingbuddy.custom_drink_name";
	public static final String EXTRA_CUSTOM_DRINK_ALCOHOL_CONTENT = "com.virat.drinkingbuddy.custom_drink_alcohol_content";
	public static final String EXTRA_CUSTOM_DRINK_CALORIES = "com.virat.drinkingbuddy.custom_drink_calories";
	public static final String EXTRA_CUSTOM_DRINK_VOLUME = "com.virat.drinkingbuddy.custom_drink_volume";
	
	private String mCustomDrinkName;
	private double mCustomDrinkAlcoholContent;
	private int mCustomDrinkCalories;
	private int mCustomDrinkVolume;
	
	private EditText mDrinkName;
	private EditText mDrinkAlcoholContent;
	private EditText mDrinkCalories;
	private EditText mDrinkVolume;
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog	 layout
		builder.setView(inflater.inflate(R.layout.dialog_custom_drink, null))
		// Add action buttons
			.setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Dialog d = (Dialog) dialog;
					
					// set Drink's title
					mDrinkName = (EditText)d.findViewById(R.id.custom_drink_dialog_name);
					mCustomDrinkName = mDrinkName.getText().toString();
					
					// set Drink's alcohol content
					mDrinkAlcoholContent = (EditText)d.findViewById(R.id.custom_drink_dialog_alcohol_content);
					if (mDrinkAlcoholContent.getText().toString() == null || mDrinkAlcoholContent.getText().toString().equals("")) {
						mCustomDrinkAlcoholContent = .05;
						Log.d(TAG, "getText() is empty");
					} else {

						Log.d(TAG, "getText() is NOT empty");
						mCustomDrinkAlcoholContent = Double.parseDouble(mDrinkAlcoholContent.getText().toString()) / 100;
					}
					
					// set Drink's calories
					mDrinkCalories = (EditText)d.findViewById(R.id.custom_drink_dialog_calories);
					if (mDrinkCalories.getText().toString() == null || mDrinkCalories.getText().toString().equals("")) {
						mCustomDrinkCalories = 100;
					} else {
						mCustomDrinkCalories = Integer.parseInt(mDrinkCalories.getText().toString());
					}
					
					// set Drink's volume
					mDrinkVolume = (EditText)d.findViewById(R.id.custom_drink_dialog_volume);
					if (mDrinkVolume.getText().toString() == null || mDrinkVolume.getText().toString().equals("")) {
						mCustomDrinkVolume = 12; 
					} else {
						mCustomDrinkVolume = Integer.parseInt(mDrinkVolume.getText().toString());
					}
					
					sendResult(Activity.RESULT_OK);
					
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CustomDrinkFragment.this.getDialog().cancel();
					
				}
			});
			
		
		
		return builder.create();
	}
	
	private void sendResult(int resultCode) {
		if (getTargetFragment() == null) 
			return;
		
		Intent i = new Intent();
		i.putExtra(EXTRA_CUSTOM_DRINK_NAME, mCustomDrinkName);
		i.putExtra(EXTRA_CUSTOM_DRINK_ALCOHOL_CONTENT, mCustomDrinkAlcoholContent);
		i.putExtra(EXTRA_CUSTOM_DRINK_CALORIES, mCustomDrinkCalories);
		i.putExtra(EXTRA_CUSTOM_DRINK_VOLUME, mCustomDrinkVolume);
		
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
	}

}
