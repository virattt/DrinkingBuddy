package com.virat.drinkingbuddy;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.virat.drinkingbuddy.dialogfragments.CustomBeerFragment;
import com.virat.drinkingbuddy.dialogfragments.CustomDrinkFragment;
import com.virat.drinkingbuddy.dialogfragments.CustomLiquorFragment;
import com.virat.drinkingbuddy.dialogfragments.CustomWineFragment;
import com.virat.drinkingbuddy.dialogfragments.DeleteDrinkDialogFragment;
import com.virat.drinkingbuddy.dialogfragments.DoneDrinkingDialogFragment;
import com.virat.drinkingbuddy.dialogfragments.ImageFragment;
import com.virat.drinkingbuddy.dialogfragments.TimePickerFragment;
import com.virat.drinkingbuddy.models.DayLab;
import com.virat.drinkingbuddy.models.Drink;
import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.Photo;
import com.virat.drinkingbuddy.models.PictureUtils;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DrinkFragment extends Fragment {

	//private static 	final String TAG = "DrinkFragment"; // for debugging
	
	public static final String EXTRA_DRINK_ID = "com.virat.drinkingbuddy.drinkfragment.drink_id";
	public static final String EXTRA_DRINKLAB_ID = "com.virat.drinkingbuddy.drinkfragment.drinklab_id";
	
	public static final String EXTRA_DRINKS_ARRAY = "com.virat.drinkingbuddy.drinks_array";
	public static final String EXTRA_DRINK = "com.virat.drinkingbuddy.drink";

	private static final String DIALOG_IMAGE = "image";
	private static final String DIALOG_TIME = "time";
	private static final String DIALOG_CUSTOM_DRINK = "custom_drink";
	private static final String DIALOG_CUSTOM_BEER = "custom_beer";
	private static final String DIALOG_CUSTOM_WINE = "custom_wine";
	private static final String DIALOG_CUSTOM_LIQUOR = "custom_liquor";
	private static final String DIALOG_DELETE_DRINK = "delete_drink";
	
	private static final int REQUEST_TIME = 0;
	private static final int REQUEST_PHOTO = 1;
	private static final int REQUEST_CUSTOM_DRINK = 2;
	private static final int REQUEST_DELETE_DRINK = 3;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	
	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	
	private Drink mDrink;
	private DrinkLab mDrinkLab;

	private Uri mFileUri;
	
	private LruCache<String, Bitmap> mMemoryCache; // memory cache
	
	private TextView mTitleField;
	private TextView mBeerTextView;
	private TextView mWineTextView;
	private TextView mLiquorTextView;
	
	private ImageButton mDeletePhotoButton;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	
	private Button mTimeButton;
	private Button mBeerButton;
	private Button mWineButton;
	private Button mLiquorButton;
	private Button mSaveButton;
	private Button mDeleteButton;
	
	// Variables for user notification preferences
	private SharedPreferences sharedPrefs;
	private boolean mNotificationPrefs;	
	
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
		
		sharedPrefs = getActivity().getSharedPreferences(SettingsFragment.SHARED_PREFS,
				Context.MODE_PRIVATE);
		mNotificationPrefs = sharedPrefs.getBoolean(SettingsFragment.NOTIFICATION_PREFS, true);

	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.drink_fragment, container, false);
		v.setBackgroundColor(getResources().getColor(R.color.gray));
		
		mTitleField = (TextView)v.findViewById(R.id.drink_titleTextView);
		mTitleField.setText(mDrink.getTitle());
		mTitleField.setHintTextColor(getResources().getColor(R.color.dodgerblue));
		mTitleField.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				CustomDrinkFragment dialog = CustomDrinkFragment.newInstance(mDrink);
				dialog.setTargetFragment(DrinkFragment.this, REQUEST_CUSTOM_DRINK);
				dialog.show(fm, DIALOG_CUSTOM_DRINK);				
			}
		});
		
		
		mTimeButton = (Button)v.findViewById(R.id.drink_timeButton);
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
	    

        mDeletePhotoButton = (ImageButton)v.findViewById(R.id.drink_photo_delete_button);
        mDeletePhotoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mDrink.getPhoto() == null)
					return;
				else {
					mPhotoView.setImageDrawable(v.getResources().getDrawable(R.drawable.logo_drink_fragment_pink));
					mDrink.setPhoto(null);
				}
				
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
        
        mBeerButton = (Button)v.findViewById(R.id.drink_beerButton);
        mBeerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpBeer();
			}
		});
        
        mBeerTextView = (TextView)v.findViewById(R.id.drink_beerTextViewButton);
        mBeerTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpBeer();
			}
		});

        mWineButton = (Button)v.findViewById(R.id.drink_wineButton);
        mWineButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpWine();
			}
		});
        
        mWineTextView = (TextView)v.findViewById(R.id.drink_wineTextViewButton);
        mWineTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpWine();
			}
		});
        
        mLiquorButton = (Button)v.findViewById(R.id.drink_liquorButton);
        mLiquorButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpLiquor();
			}
		});
        
        mLiquorTextView = (TextView)v.findViewById(R.id.drink_liquorTextViewsButton);
        mLiquorTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setUpLiquor();	
			}
		});
        
        mSaveButton = (Button)v.findViewById(R.id.drink_saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					
					// Start drink updates
					if (mNotificationPrefs) {
						
						// Create an Intent to start the Notification service
						Intent intent = createAlarmIntent();
						DrinkUpdates.startUpdates(DayListFragment.context, intent);
					}

					Toast.makeText(getActivity(), "Drink Saved!", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getActivity(), DrinkListActivity.class);
					intent.putExtra(EXTRA_DRINKLAB_ID, mDrinkLab.getId());
					NavUtils.navigateUpTo(getActivity(), intent);
				}
			}
		});
        
        mDeleteButton = (Button)v.findViewById(R.id.drink_deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity()
						.getSupportFragmentManager();
				DeleteDrinkDialogFragment dialog = new DeleteDrinkDialogFragment();
				dialog.setTargetFragment(DrinkFragment.this, REQUEST_DELETE_DRINK);
				dialog.show(fm, DIALOG_DELETE_DRINK);	
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
	
	/** Creates/re-creates Intent for starting/stopping alarm service */
	private Intent createAlarmIntent() {
		
		Intent intent = new Intent();
		intent.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID,
				mDrinkLab.getId());
		return intent;
	}
	
	private boolean isNewDrink() {
		if (mDrink.getCalories() == 0 
			&& mDrink.getAlcoholContent() == 0.00 
			&& mDrink.getVolume() == 0.00) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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
		} else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			String filename = data.getDataString();
			
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
			
			double custom_drink_volume = data.getDoubleExtra(CustomDrinkFragment.EXTRA_CUSTOM_DRINK_VOLUME, 0.00);
			mDrink.setVolume(custom_drink_volume);
		} else if (requestCode == REQUEST_DELETE_DRINK) {
			
			boolean delete_drink = 
					data.getBooleanExtra(DeleteDrinkDialogFragment.EXTRA_DELETE_DRINK, false);
			
			if (delete_drink) {
				// Delete the drink from the DrinkLab
				DayLab.get(getActivity()).getDrinkLab(mDrinkLab.getId())
				.deleteDrink(mDrink);
				
				// Cancel the alarm
				DrinkUpdates.stopUpdates(DayListFragment.context);
				
				// Navigate up to parent activity
				Toast.makeText(getActivity(), "Drink Deleted", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getActivity(), DrinkListActivity.class);
				intent.putExtra(EXTRA_DRINKLAB_ID, mDrinkLab.getId());
				NavUtils.navigateUpTo(getActivity(), intent);				
			}
			
		}
	}
	
	/** create a String Uri for saving an image */
	public String getPhotoDirString() {
	// Create a filename
				String filename = UUID.randomUUID().toString() + ".jpg";
				// Save the jpeg data to disk
				FileOutputStream os = null;
				boolean success = true;
				try {
					os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
					//os.write();
				} catch (Exception e) {
					success = false;
				} finally {
					try {
						if (os != null)
							os.close();
					} catch (Exception e) {
						success = false;
					}
				}
				return filename;
	}
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type, Context context){
		return Uri.fromFile(getOutputMediaFile(type, context));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type, Context context){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		String filename = UUID.randomUUID().toString() + ".jpg";
		File mediaStorageDir;


		mediaStorageDir = new File(context.getFilesDir(), "DrinksterPictures");
		mediaStorageDir.mkdirs();
		

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	        mediaFile.mkdirs();
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	    
	    //Log.d(TAG, "mediaFile: " + mediaFile);

	    return mediaFile;
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

			} else {
				BitmapWorkerTask task = new BitmapWorkerTask(getActivity(), mPhotoView);
				task.execute(path);
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
	
	private void setUpBeer() {
		// set Beer title 
		mTitleField.setText(mDrink.getTitle());
		
		// set Beer's default content
		mDrink.setCalories(150); // 105 calories in average beer
		mDrink.setAlcoholContent(.05); // avg alcohol content in beer is 4.5%
		mDrink.setVolume(12); // avg volume of beer is 12 fluid ounces
		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		CustomBeerFragment dialog = CustomBeerFragment.newInstance(mDrink);
		dialog.setTargetFragment(DrinkFragment.this, REQUEST_CUSTOM_DRINK);
		dialog.show(fm, DIALOG_CUSTOM_BEER);
	}
	
	private void setUpWine() {
		// set Wine title
		mTitleField.setText(mDrink.getTitle());
		
		// set Wine's default 
		mDrink.setCalories(125); // 125 calories in average wine glass
		mDrink.setAlcoholContent(.12); // avg alcohol content in beer is 12%
		mDrink.setVolume(5); // avg volume of wine is 5 fluid ounces
		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		CustomWineFragment dialog = CustomWineFragment.newInstance(mDrink);
		dialog.setTargetFragment(DrinkFragment.this, REQUEST_CUSTOM_DRINK);
		dialog.show(fm, DIALOG_CUSTOM_WINE);
	}
	
	private void setUpLiquor() {
		// set Wine title
		mTitleField.setText(mDrink.getTitle());
		
			// set Wine's default content
		mDrink.setCalories(97); // 97 calories in average liquor drink
		mDrink.setAlcoholContent(.40); // avg alcohol content in liquor is 40%
		mDrink.setVolume(1.50); // avg volume of liquor is 1.5 fluid ounces
		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		CustomLiquorFragment dialog = CustomLiquorFragment.newInstance(mDrink);
		dialog.setTargetFragment(DrinkFragment.this, REQUEST_CUSTOM_DRINK);
		dialog.show(fm, DIALOG_CUSTOM_LIQUOR);
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
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if (mFileUri != null) {
	        outState.putString("cameraImageUri", mFileUri.toString());
	    }
	}
}
