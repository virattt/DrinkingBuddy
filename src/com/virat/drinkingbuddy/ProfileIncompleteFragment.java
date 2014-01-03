package com.virat.drinkingbuddy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class ProfileIncompleteFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.profile_incomplete_title)
				.setPositiveButton(R.string.profile_incomplete_OK_button, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Launch UserFragment
						Intent i = new Intent(getActivity(), UserActivity.class);
						startActivity(i);
						
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ProfileIncompleteFragment.this.getDialog().cancel();
					}
				});
		
		return builder.create();
	}

}
