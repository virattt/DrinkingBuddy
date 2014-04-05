package com.virat.drinkingbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class TermsAndConditionsDialogFragment extends DialogFragment {
	
	public static final String EXTRA_TERMS_AGREED = "com.virat.drinkingbuddy.terms_agreed";
	private boolean mTermsAgreed;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
	
		builder.setView(inflater.inflate(R.layout.dialog_terms_conditions, null))
		.setPositiveButton(R.string.agree_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mTermsAgreed = true;
				sendResult(Activity.RESULT_OK);
			}
		})
		.setNegativeButton(R.string.disagree_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mTermsAgreed = false;
				sendResult(Activity.RESULT_OK);
			}
		});
		
		return builder.create();
	}
	
	public void sendResult(int resultCode) {
		if (getTargetFragment() == null) 
			return;
		
		Intent i = new Intent();
		i.putExtra(EXTRA_TERMS_AGREED, mTermsAgreed);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);

	}

	@Override
	public void onPause() {
		if (mTermsAgreed == false) {
			// closing Entire Application
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		super.onPause();
	}
}
