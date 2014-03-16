package com.virat.drinkingbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class DoneDrinkingDialogFragment extends DialogFragment {
	
	public static final String EXTRA_DONE_DRINKING = "com.virat.drinkingbuddy.done_drinking";
	
	private boolean mDoneDrinking;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_done_drinking, null);
		
		builder.setView(v)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDoneDrinking = true;
				sendResult(Activity.RESULT_OK);
				
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
		
		return builder.create();
	}
	
	public void sendResult(int resultCode) {
		if (getTargetFragment() == null) 
			return;
		
		Intent i = new Intent();
		i.putExtra(EXTRA_DONE_DRINKING, mDoneDrinking);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);

	}
}
