package com.virat.drinkingbuddy;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class DrinkFragment extends Fragment {

	private static 	final String TAG = "DrinkFragment"; // for debugging
	
	public static final String EXTRA_DRINK_ID = "com.virat.drinkingbuddy.drink_id";
	public static final String EXTRA_DRINKLAB_ID = "com.virat.drinkingbuddy.drinklab_id";
	
	public static final String EXTRA_DRINKS_ARRAY = "com.virat.drinkingbuddy.drinks_array";
	public static final String EXTRA_DRINK = "com.virat.drinkingbuddy.drink";

	private static final String DIALOG_IMAGE = "image";
	private static final String DIALOG_TIME = "time";
	private static final String DIALOG_CUSTOM_DRINK = "custom_drink";
	
	private static final int REQUEST_TIME = 0;
	private static final int REQUEST_PHOTO = 1;
	private static final int REQUEST_CUSTOM_DRINK = 2;
	
	private Drink mDrink;
	private DrinkLab mDrinkLab;
	private EditText mTitleField;
	private Button mTimeButton;
	private ImageButton mPhotoButton;
	private ImageButton mDeletePhotoButton;
	private ImageView mPhotoView;
	private Button mBeerButton;
	private Button mWineButton;
	private Button mLiquorButton;
	private Button mCustomDrinkButton;
	private Button mSaveButton;
	
	private LruCache<String, Bitmap> mMemoryCache; // memory cache
	
	public static DrinkFragment newInstance(UUID drinkId, UUID drinkLabId) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_DRINK_ID, drinkId);
		args.putSerializable(EXTRA_DRINKLAB_ID, drinkLabId);
		
		DrinkFragment fragment = new DrinkFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.actionbar);
		
		getActivity().setTitle("My Drink");
		//setHasOptionsMenu(true);
		
		UUID drinkId = (UUID)getArguments().getSerializable(EXTRA_DRINK_ID);
		UUID drinkLabId = (UUID)getArguments().getSerializable(EXTRA_DRINKLAB_ID);
		mDrinkLab = DayLab.get(getActivity()).getDrinkLab(drinkLabId);
		
		mDrink = mDrinkLab.getDrink(drinkId);
		
		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
		
		// Use 1/8th of available memory for this memory cache.
		final int cacheSize = maxMemory / 8;
		
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
	            // number of items.
	            return bitmap.getByteCount() / 1024;
			}
		};
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.drink_fragment, container, false);
		v.setBackgroundColor(getResources().getColor(R.color.dodgerblue));
		
		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		*/
		
		mTitleField = (EditText)v.findViewById(R.id.drink_title);
		mTitleField.setText(mDrink.getTitle());
		mTitleField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mDrink.setTitle(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		mTimeButton = (Button)v.findViewById(R.id.drink_time);
		mTimeButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				TimePickerFragment dialog = new TimePickerFragment().newInstance(mDrink.getTime());
				dialog.setTargetFragment(DrinkFragment.this, REQUEST_TIME);
				dialog.show(fm, DIALOG_TIME);
			}
		});
		
		mPhotoView = (ImageView)v.findViewById(R.id.drink_imageView);
		mPhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Photo p = mDrink.getPhoto();
				if (p == null) 
					return;
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
				ImageFragment.newInstance(path).show(fm, DIALOG_IMAGE);
			}
		});
	    
		
		mPhotoButton = (ImageButton)v.findViewById(R.id.drink_photo_button);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DrinkCameraActivity.class);
                startActivityForResult(i, REQUEST_PHOTO);
            }
        });
        
        mDeletePhotoButton = (ImageButton)v.findViewById(R.id.drink_photo_delete_button);
        mDeletePhotoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mDrink.getPhoto() == null)
					return;
				else {
					mPhotoView.setImageDrawable(v.getResources().getDrawable(R.drawable.logo_drink_fragment));
					mDrink.setPhoto(null);
				}
				
			}
		});
        
        mBeerButton = (Button)v.findViewById(R.id.drink_beerButton);
        mBeerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// set Drink title
				mDrink.setTitle("Beer");
				mTitleField.setText(mDrink.getTitle());
				
				// set Drink's calories and alcohol content
				mDrink.setCalories(105); // 105 calories in average beer
				mDrink.setAlcoholContent(.05); // avg alcohol content in beer is 4.5%
				mDrink.setVolume(12); // avg volume of beer is 12 fluid ounces
				Log.d(TAG, "Calories in Beer is: " + mDrink.getCalories() + " Alc Content is: " + mDrink.getAlcoholContent());
				Log.d(TAG, "Volume is: " + mDrink.getVolume());
			}
		});

        mWineButton = (Button)v.findViewById(R.id.drink_wineButton);
        mWineButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// set Drink title
				mDrink.setTitle("Wine");
				mTitleField.setText(mDrink.getTitle());
				
				// set Drink's calories and alcohol content
				mDrink.setCalories(125); // 125 calories in average wine glass
				mDrink.setAlcoholContent(.12); // avg alcohol content in beer is 12%
				mDrink.setVolume(5); // avg volume of wine is 5 fluid ounces
				Log.d(TAG, "Calories in Wine is: " + mDrink.getCalories() + " Alc Content is: " + mDrink.getAlcoholContent());
				Log.d(TAG, "Volume is: " + mDrink.getVolume());
			}
		});
        
        mLiquorButton = (Button)v.findViewById(R.id.drink_liquorButton);
        mLiquorButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// set Drink title
				mDrink.setTitle("Liquor");
				mTitleField.setText(mDrink.getTitle());
				
				// set Drink's calories and alcohol content
				mDrink.setCalories(97); // 97 calories in average liquor drink
				mDrink.setAlcoholContent(.40); // avg alcohol content in liquor is 40%
				mDrink.setVolume(1.25); // avg volume of liquor is 1.5 fluid ounces
				Log.d(TAG, "Calories in Liquor is: " + mDrink.getCalories() + " Alc Content is: " + mDrink.getAlcoholContent());
				Log.d(TAG, "Volume is: " + mDrink.getVolume());

			}
		});
        
        mCustomDrinkButton = (Button)v.findViewById(R.id.drink_customDrinkButton);
        mCustomDrinkButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				CustomDrinkFragment dialog = new CustomDrinkFragment();
				dialog.setTargetFragment(DrinkFragment.this, REQUEST_CUSTOM_DRINK);
				dialog.show(fm, DIALOG_CUSTOM_DRINK);
			}
		});
        
        mSaveButton = (Button)v.findViewById(R.id.drink_saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					Toast.makeText(getActivity(), "Drink Saved!", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getActivity(), DrinkListActivity.class);
					intent.putExtra(EXTRA_DRINKLAB_ID, mDrinkLab.getId());
					NavUtils.navigateUpTo(getActivity(), intent);
				}
			}
		});
		
        
        // If camera is not available, disable camera functionality
        PackageManager pm = getActivity().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            mPhotoButton.setEnabled(false);
        }		
		
		return v; 
	}
	
	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					Intent intent = new Intent(getActivity(), DrinkListActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(EXTRA_DRINKLAB_ID, mDrinkLab.getId());
					NavUtils.navigateUpTo(getActivity(), intent);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	*/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_TIME) {
			Date time = (Date)data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
			mDrink.setTime(time);
		} else if (requestCode == REQUEST_PHOTO) {
			// Create a new Photo object and attach it to the drink
			String filename = data.getStringExtra(DrinkCameraFragment.EXTRA_PHOTO_FILENAME);
			
			if (filename != null) {
				Photo p = new Photo(filename);
				mDrink.setPhoto(p);
				showPhoto();
			}
		} else if (requestCode == REQUEST_CUSTOM_DRINK) {
			String custom_drink_name = data.getStringExtra(CustomDrinkFragment.EXTRA_CUSTOM_DRINK_NAME);
			mDrink.setTitle(custom_drink_name);
			mTitleField.setText(mDrink.getTitle());
			
			double custom_alcohol_content = data.getDoubleExtra(CustomDrinkFragment.EXTRA_CUSTOM_DRINK_ALCOHOL_CONTENT, 0.00);
			mDrink.setAlcoholContent(custom_alcohol_content);
			
			int custom_drink_calories = data.getIntExtra(CustomDrinkFragment.EXTRA_CUSTOM_DRINK_CALORIES, 0);
			mDrink.setCalories(custom_drink_calories);
			
			int custom_drink_volume = data.getIntExtra(CustomDrinkFragment.EXTRA_CUSTOM_DRINK_VOLUME, 0);
			mDrink.setVolume(custom_drink_volume);
			
			Log.d(TAG, "Custom Drink Name: " + mDrink.getTitle() + " Alc: " + mDrink.getAlcoholContent() + " Calories: " + mDrink.getCalories());
		}
	}
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
	
	// sets mPhotoView as long as the Photo's filename path
	// is not null.  Uses AsyncTask
	private void showPhoto() {
		// (Re)set the image button's image based on our photo
		Photo p = mDrink.getPhoto();
		//BitmapDrawable b = null;
		if (p != null) {
			String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
			
			final String imageKey = path;
			
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			
			if (bitmap != null) {
				mPhotoView.setImageBitmap(bitmap);
				Log.d(TAG, "DrinkFragment Bitmap is not null");

			} else {
				BitmapWorkerTask task = new BitmapWorkerTask(getActivity(), mPhotoView);
				task.execute(path);

				Log.d(TAG, "DrinkFragment Bitmap is NULL");
				//b = PictureUtils.getScaledDrawable(getActivity(), path);
			}
		}
		//mPhotoView.setImageDrawable(b);	
	}
	
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		protected String data;
		private Activity imageActivity;
		//private BitmapDrawable bitmap;
		
		public BitmapWorkerTask(Activity a, ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			imageActivity = a;
			
		}
		
		// Decode image in background
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			//bitmapDrawable = PictureUtils.getScaledDrawable(imageActivity, data);
			final Bitmap bitmap = PictureUtils.getScaledBitmap(imageActivity, data);
			addBitmapToMemoryCache(data, bitmap);
			
			return bitmap;
		}
		
		// Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap b) {
	    	if (isCancelled()) {
	    		b = null;
	    	}
	        if (imageViewReference != null && b != null) {
	            final ImageView imageView = imageViewReference.get();

	            if (imageView != null) {
	                imageView.setImageBitmap(b);
	            }
	        }
	    }
	}

	@Override
	public void onStart() {
		super.onStart();
		showPhoto();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		//PictureUtils.cleanImageView(mPhotoView);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();	
	}
}
