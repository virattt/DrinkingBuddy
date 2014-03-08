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
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CustomWineFragment extends DialogFragment {

	private static 	final String TAG = "CustomDrinkFragment"; // for debugging
	
	public static final String EXTRA_CUSTOM_DRINK_NAME = "com.virat.drinkingbuddy.custom_drink_name";
	public static final String EXTRA_CUSTOM_DRINK_ALCOHOL_CONTENT = "com.virat.drinkingbuddy.custom_drink_alcohol_content";
	public static final String EXTRA_CUSTOM_DRINK_CALORIES = "com.virat.drinkingbuddy.custom_drink_calories";
	public static final String EXTRA_CUSTOM_DRINK_VOLUME = "com.virat.drinkingbuddy.custom_drink_volume";
	
	private static final String EXTRA_CUSTOM_DRINK = "com.virat.drinkingbuddy.custom_drink";
	
	private String mCustomDrinkName;
	private double mCustomDrinkAlcoholContent;
	private int mCustomDrinkCalories;
	private double mCustomDrinkVolume;
	
	private EditText mDrinkName;
	private EditText mDrinkAlcoholContent;
	private EditText mDrinkCalories;
	private EditText mDrinkVolume;
	private TextView mDialogTitle;
	
	private Drink mDrink;
	private Spinner mSpinner;
	
	public static CustomWineFragment newInstance(Drink d) {
		CustomWineFragment f = new CustomWineFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_CUSTOM_DRINK, d);
		f.setArguments(args);
		
		return f;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mDrink = getArguments().getParcelable(EXTRA_CUSTOM_DRINK);
		
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_custom_drink, null);
		
		mDialogTitle = (TextView)v.findViewById(R.id.custom_drink_title_textview);
		mDialogTitle.setText("Wine");
		
		mSpinner = (Spinner)v.findViewById(R.id.custom_drink_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
		        R.array.custom_wine_array, R.layout.my_spinner_style);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(R.layout.my_spinner_dropdown_style);
		// Apply the adapter to the spinner
		mSpinner.setAdapter(adapter);
		mSpinner.setOnItemSelectedListener(new MyItemSelectedListener());
		
		

		mDrinkName = (EditText)v.findViewById(R.id.custom_drink_dialog_name);
		mDrinkName.setText(mDrink.getTitle());
		
		mDrinkAlcoholContent = (EditText)v.findViewById(R.id.custom_drink_dialog_alcohol_content);
		if (mDrink.getAlcoholContent() != 0.00) {
			mDrinkAlcoholContent.setText((mDrink.getAlcoholContent() * 100) + "");
		}
		
		mDrinkCalories = (EditText)v.findViewById(R.id.custom_drink_dialog_calories);
		if (mDrink.getCalories() != 0) {
			mDrinkCalories.setText(mDrink.getCalories() + "");
		}
		
		mDrinkVolume = (EditText)v.findViewById(R.id.custom_drink_dialog_volume);
		if (mDrink.getVolume() != 0.00) {
			mDrinkVolume.setText(mDrink.getVolume() + "");
		}
		
		
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog	 layout
		builder.setView(v)
		// Add action buttons
			.setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Dialog d = (Dialog) dialog;
					
					// set Drink's title
					mCustomDrinkName = mDrinkName.getText().toString();
					// set Drink's alcohol content
					if (mDrinkAlcoholContent.getText().toString() == null || mDrinkAlcoholContent.getText().toString().equals("")) {
                        mCustomDrinkAlcoholContent = 0.00;
					} else {
                        mCustomDrinkAlcoholContent = Double.parseDouble(mDrinkAlcoholContent.getText().toString()) / 100;
					}
					
					// set Drink's calories
					if (mDrinkCalories.getText().toString() == null || mDrinkCalories.getText().toString().equals("")) {
                        mCustomDrinkCalories = 0;
					} else {
                        mCustomDrinkCalories = Integer.parseInt(mDrinkCalories.getText().toString());
					}
					
					// set Drink's volume
					if (mDrinkVolume.getText().toString() == null || mDrinkVolume.getText().toString().equals("")) {
                        mCustomDrinkVolume = 0.00; 
					} else {
                        mCustomDrinkVolume = Double.parseDouble(mDrinkVolume.getText().toString());
					}
                	
					sendResult(Activity.RESULT_OK);
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CustomWineFragment.this.getDialog().cancel();
				}
			});
			
		return builder.create();
	}
	
	class MyItemSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String selected = parent.getItemAtPosition(pos).toString();
			if (selected.equals("Red Wine")) {
				setCustomDrink("Red Wine", "12.0", "125", "5.0");
			} else if (selected.equals("White Wine")) {
				setCustomDrink("White Wine", "11.0", "120", "5.0");
			}
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	// Convenience method to set the name, alc content,
	// calories, and volume of a custom drink
	private void setCustomDrink(String name, String alcoholContent, 
								String calories, String volume) {
		mDrinkName.setText(name);
		mDrinkAlcoholContent.setText(alcoholContent);
		mDrinkCalories.setText(calories); 
		mDrinkVolume.setText(volume);
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
