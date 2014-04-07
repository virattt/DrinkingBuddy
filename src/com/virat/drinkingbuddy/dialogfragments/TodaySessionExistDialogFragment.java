package com.virat.drinkingbuddy.dialogfragments;

import com.virat.drinkingbuddy.R;
import com.virat.drinkingbuddy.R.layout;
import com.virat.drinkingbuddy.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class TodaySessionExistDialogFragment extends DialogFragment {

	public static final String EXTRA_CREATE_ANOTHER_SESSION = "com.virat.drinkingbuddy.create_another_session";
	
	private boolean mCreateAnotherSession;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		builder.setView(inflater.inflate(R.layout.dialog_today_session_exists, null))
		.setPositiveButton(R.string.yes_string, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCreateAnotherSession = true;
				sendResult(Activity.RESULT_OK);
			}
		})
		.setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		
		return builder.create();
	}
	
	public void sendResult(int resultCode) {
		if (getTargetFragment() == null) 
			return;
		
		Intent i = new Intent();
		i.putExtra(EXTRA_CREATE_ANOTHER_SESSION, mCreateAnotherSession);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);

	}

}
