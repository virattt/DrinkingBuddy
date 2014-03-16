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
import android.widget.Spinner;
import android.widget.TextView;

public class BACDialogFragment extends DialogFragment {
	private static final String EXTRA_CUSTOM_DRINKLAB = "com.virat.drinkingbuddy.custom_drink";
	
	private TextView mBACTextView;
	private DrinkLab mDrinkLab;
	private Spinner mSpinner;
	
	public static BACDialogFragment newInstance(DrinkLab d) {
		BACDialogFragment f = new BACDialogFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_CUSTOM_DRINKLAB, d);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		mDrinkLab = getArguments().getParcelable(EXTRA_CUSTOM_DRINKLAB);
	}
		
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.bac_dialog_fragment, null);
		mBACTextView = (TextView)v.findViewById(R.id.bac_textview);
		mBACTextView.setText(mDrinkLab.getBAC() + "");
		
		builder.setView(v)
		.setPositiveButton("Yes, I understand", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
		return builder.create();
	}

}
