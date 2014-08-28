package com.virat.drinkingbuddy.dialogfragments;

import com.virat.drinkingbuddy.R;
import com.virat.drinkingbuddy.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class DeleteDrinkDialogFragment extends DialogFragment {
	
	public static final String EXTRA_DELETE_DRINK = "com.virat.drinkingbuddy.delete_drink";
	
	private boolean mDeleteDrink;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_delete_drink, null);
		
		builder.setView(v)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDeleteDrink = true;
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
		i.putExtra(EXTRA_DELETE_DRINK, mDeleteDrink);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);

	}
}
