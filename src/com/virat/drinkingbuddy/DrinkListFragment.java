package com.virat.drinkingbuddy;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.util.LruCache;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class DrinkListFragment extends ListFragment {
	
	public static final String EXTRA_DRINKLAB = "com.virat.drinkingbuddy.drinklab";
	private static final String TAG = "DrinkListFragment";
	
	private static final String DIALOG_PROFILE = "profile";
	private static final String DIALOG_TERMS = "terms_and_conditions";
	private static final String DIALOG_DONE_DRINKING = "done_drinking";
	private static final String DIALOG_BAC = "dialog_bac";
	
	private static final int REQUEST_DRINKLAB = 0;
	private static final int REQUEST_TERMS = 1;
	private static final int REQUEST_DONE_DRINKING = 2;
	
	private ArrayList<Drink> mDrinks;
	private DrinkLab mDrinkLab;
	private UUID mDrinkLabId;
	private int mTotalCalories;
	private boolean mIsDrinking;
	
	private TextView mDrinksTextView; // drink count
	private TextView mTitleTextView; // title of drink
	private TextView mCaloriesTextView; // calories of drink
	private TextView mTimeTextView; // time of drink
	private TextView mTotalCaloriesTextView; // total calories
	private TextView mTotalDrinksTextView; // total drinks
	private TextView mTotalTimeTextView; // total drinking time
	private TextView mBacTextView; // BAC text view
	
	private Button mBacButton;
	private Button mNewDrinkButton;
	private Button mDoneDrinkingButton;
	
	private BitmapWorkerTask mTask;
	private Bitmap mPlaceHolderDrawable;
	
	private LruCache<String, Bitmap> mMemoryCache; // memory cache
	
	public static DrinkListFragment newInstance (UUID drinkLabId, DrinkLab drinkLab) {
		Bundle args = new Bundle();
	    args.putSerializable(DrinkFragment.EXTRA_DRINKLAB_ID, drinkLabId);
	    args.putParcelable(EXTRA_DRINKLAB, drinkLab);
	    
	    DrinkListFragment fragment = new DrinkListFragment();
	    fragment.setArguments(args);
	    
	    return fragment;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 * The onCreate function
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.actionbar);
		actionBar.setDisplayShowHomeEnabled(true);
		
		UUID drinkLabId = (UUID)getArguments().getSerializable(DrinkFragment.EXTRA_DRINKLAB_ID);
		
		mDrinkLab = DayLab.get(getActivity()).getDrinkLab(drinkLabId);
		//mDrinkLab = (DrinkLab)getArguments().getParcelable(EXTRA_DRINKLAB);
		
		mDrinks = mDrinkLab.getDrinks();
		mTotalCalories = mDrinkLab.getCalories();
		mIsDrinking = mDrinkLab.getIsDrinking();
		
		setHasOptionsMenu(true);
		
		DrinkAdapter adapter = new DrinkAdapter(mDrinks);
		setListAdapter(adapter); 
		
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

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 * The onCreateView function
	 */
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.list_item_all_drink_list_frag, null);
		v.setBackgroundColor(Color.parseColor("#B1BDCD"));
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		
		
		ListView listView = (ListView)v.findViewById(android.R.id.list);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// use floating context menu on Froyo and Gingerbread
			registerForContextMenu(listView);
		}	else {
			// use contextual action bar on Honeycomb and higher
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Required. but not used in this implementation
					return false;
				} 
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// Required, but not used in this implementation				
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.drink_list_item_context, menu);
					return true;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.menu_item_delete_drink:
							DrinkAdapter adapter = (DrinkAdapter)getListAdapter();
							for (int i = adapter.getCount() - 1; i >= 0; i--) {
								if (getListView().isItemChecked(i)) {
									mDrinkLab.deleteDrink(adapter.getItem(i));
								}
							}
							mode.finish();
							adapter.notifyDataSetChanged();
							return true;
						default:
							return false;
					}
				}
				
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					// Required but not used in this implementation
				}
			});
		}
		
		mNewDrinkButton = (Button)v.findViewById(R.id.drink_list_newDrinkButton);
		
		if (!mDrinkLab.getIsDrinking()) {
			mNewDrinkButton.setClickable(false);
		} else {
			mNewDrinkButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Drink drink = new Drink();
					
					DayLab.get(getActivity()).getDrinkLab(mDrinkLab.getId()).addDrink(drink);
					
					Intent i = new Intent(getActivity(), DrinkActivity.class);
					i.putExtra(DrinkFragment.EXTRA_DRINK_ID, drink.getId());
					i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, mDrinkLab.getId());
	
					startActivityForResult(i, REQUEST_DRINKLAB);
				}
			});
		}
		// TextView for total drinks
		mTotalDrinksTextView = (TextView)v.findViewById(R.id.drink_list_drinkCountTextView); 
		//if (mDrinkLab.getTotalDrinks() != 0) {
			mTotalDrinksTextView.setText(mDrinkLab.getTotalDrinks() + "");
		//} else {
			//mTotalDrinksTextView.setText("0");
		//}
		// TextView for total calories
		mTotalCaloriesTextView = (TextView)v.findViewById(R.id.drink_list_totalCaloriesTextView);
		mTotalCaloriesTextView.setText(mDrinkLab.getCalories() + "");
		
		// TextView for total drinking time
		mTotalTimeTextView = (TextView)v.findViewById(R.id.drink_list_durationTextView);		
		mTotalTimeTextView.setText(mDrinkLab.getDrinkingDuration(mDrinkLab));
		
		// TextView for BAC
		//mBacTextView = (TextView)v.findViewById(R.id.drink_list_BACTextView);
		
		// Button for BAC
		mBacButton = (Button)v.findViewById(R.id.drink_list_BACButton);
		mBacButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (userProfileIncomplete()) {
					FragmentManager fm = getActivity().getSupportFragmentManager();
					ProfileIncompleteFragment dialog = new ProfileIncompleteFragment();
					dialog.show(fm, DIALOG_PROFILE);
				} else {
					FragmentManager fm = getActivity().getSupportFragmentManager();
					BACDialogFragment dialog = new BACDialogFragment().newInstance(mDrinkLab);
					dialog.show(fm, DIALOG_DONE_DRINKING);				
					
				}
			}
		});
		
		mDoneDrinkingButton = (Button)v.findViewById(R.id.drink_list_doneDrinkingButton);
		if (!mDrinkLab.getIsDrinking()) {
			mDoneDrinkingButton.setClickable(false);
		} else {
			mDoneDrinkingButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					FragmentManager fm = getActivity().getSupportFragmentManager();
					DoneDrinkingDialogFragment dialog = new DoneDrinkingDialogFragment();
					dialog.setTargetFragment(DrinkListFragment.this, REQUEST_DONE_DRINKING);
					dialog.show(fm, DIALOG_DONE_DRINKING);				
				}
			});
		}	
		return v;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 * The onCreateOptionsMenu - inflates the options menu at the top of the Activity
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_drink_list, menu);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
	 * onOptionsItemSelected - puts the users ActionBar choice on an Intent and starts
	 * an Activity that corresponds to the users choice
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				if (NavUtils.getParentActivityName(getActivity()) != null) {
					NavUtils.navigateUpFromSameTask(getActivity());
				}
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 * Handler for the lists in the LsitView when the user clicks a ListView View object
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Drink d = ((DrinkAdapter)getListAdapter()).getItem(position);
		
		// Start DrinkActivity
		Intent i = new Intent(getActivity(), DrinkActivity.class);
		i.putExtra(DrinkFragment.EXTRA_DRINK_ID, d.getId());
		i.putExtra(DrinkFragment.EXTRA_DRINKLAB_ID, mDrinkLab.getId());
		startActivityForResult(i, REQUEST_DRINKLAB);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != Activity.RESULT_OK) 
			return;
		
		if (requestCode == REQUEST_DRINKLAB) {
			mDrinkLab = data.getParcelableExtra(DrinkFragment.EXTRA_DRINKS_ARRAY);
		} else if (requestCode == REQUEST_DONE_DRINKING) {
			Person.get(getActivity()).setIsDrinking(false);
			Person.get(getActivity()).savePerson();
			mDrinkLab.setIsDrinking(false);
			mDoneDrinkingButton.setClickable(false);
			mNewDrinkButton.setClickable(false);
			Log.d(TAG, "Person isDrinking?" + Person.get(getActivity()).getIsDrinking());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 * Inflate the Context Menu for pre-Honeycomb devices
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.drink_list_item_context, menu);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
	 * Deletes a Drink using a floating context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		DrinkAdapter adapter = (DrinkAdapter)getListAdapter();
		Drink drink = adapter.getItem(position);
		
		switch (item.getItemId()) {
			case R.id.menu_item_delete_drink:
				//DrinkLab.get(getActivity()).deleteDrink(drink);
				mDrinkLab.deleteDrink(drink);
				adapter.notifyDataSetChanged();
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/*
	 * Nested ArrayAdapter class that serves as the adapter for the ListView in DrinkListFragment
	 */
	private class DrinkAdapter extends ArrayAdapter<Drink> {
		
		public DrinkAdapter(ArrayList<Drink> drinks) {
			super(getActivity(), 0, drinks);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if not given a View, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_drink, null);
			}
			
			// Configure the view for this Drink
			Drink d = getItem(position);
			
			mDrinksTextView = (TextView)convertView.findViewById(R.id.drink_list_item_drinksTextView);
			mDrinksTextView.setText(getPosition(d) + 1 + ""); // get the position for each drink
			
			mTitleTextView = (TextView)convertView.findViewById(R.id.drink_list_item_titleTextView);
			mTitleTextView.setText(d.getTitle());
			mCaloriesTextView = (TextView)convertView.findViewById(R.id.drink_list_item_caloriesTextView);
			mCaloriesTextView.setText(d.getCalories() + " Calories");
			mTimeTextView = (TextView)convertView.findViewById(R.id.drink_list_item_timeTextView);
			mTimeTextView.setText(formatTime(d.getTime()) + "");	
			
			return convertView;
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
	
	// Test handle concurrency
	private void showPhoto(Photo p, ImageView imageView) {
		// (Re)set the image button's image based on our photo
		if (p != null) {
			String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
			final String imageKey = path;
			
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			
			if (cancelPotentialWork(path, imageView)) {
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					final BitmapWorkerTask task = new BitmapWorkerTask(getActivity(), imageView);
					final AsyncDrawable asyncDrawable = 
							new AsyncDrawable(getResources(), null, task);
					imageView.setImageDrawable(asyncDrawable);
					task.execute(path);
				}
			}
		}
	}
	
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		protected String data;
		private Activity imageActivity;
		private BitmapDrawable bitmapDrawable;
		
		public BitmapWorkerTask(Activity a, ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			imageActivity = a;
			
		}
		
		// Decode image in background
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			Bitmap bitmap = getBitmapFromMemCache(data);
			
			if (bitmap == null) {
				bitmap = PictureUtils.getScaledThumbnail(imageActivity, data);
			}
			addBitmapToMemoryCache(data, bitmap);
			addBitmapToCache(data, bitmap);
			
			//bitmapDrawable = new BitmapDrawable(
			//						imageActivity.getResources(), 
				//					PictureUtils.getScaledThumbnail(imageActivity, data));
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
	            final BitmapWorkerTask bitmapWorkerTask = 
	            		getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(b);
	            }
	        }
	    }
	}
	
	static class AsyncDrawable extends BitmapDrawable {
		
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap,
	            BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference =
	            new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
	
	public void addBitmapToCache(String key, Bitmap bitmap) {
	    // Add to memory cache as before
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}
	// Creates a unique subdirectory of the designated app cache directory. Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(Context context, String uniqueName) {
	    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
	    // otherwise use internal cache dir
	    final String cachePath =
	            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
	                    !Utils.isExternalStorageRemovable() ? Utils.getExternalCacheDir(context).getPath() :
	                            context.getCacheDir().getPath();

	    return new File(cachePath + File.separator + uniqueName);
	}

	public static boolean cancelPotentialWork(String data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String bitmapData = bitmapWorkerTask.data;
	        if (bitmapData != data) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	 
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
	   if (imageView != null) {
	       final Drawable drawable = imageView.getDrawable();
	       if (drawable instanceof AsyncDrawable) {
	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	           return asyncDrawable.getBitmapWorkerTask();
	       }
	    }
	    return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		((DrinkAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		DayLab.get(getActivity()).saveDrinkLab();

		Log.d(TAG, "Person isDrinking?" + Person.get(getActivity()).getIsDrinking());
	}

	/*
	 * Formats the Time and returns it as a formatted String
	 */
	private String formatTime(Date time) {
		SimpleDateFormat fmt = new SimpleDateFormat("h:mm a");
		return fmt.format(time);
	}
	
	private boolean userProfileIncomplete() {
		if (Person.get(getActivity()).getWeight() == null || 
				Person.get(getActivity()).getWeight().equals("") ||
			Person.get(getActivity()).getGender().equals("none")) {
			return true;
		}
		return false;
	}
}
