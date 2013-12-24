package com.virat.drinkingbuddy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DrinkLab implements Parcelable {
	private static final String TAG = "DrinkLab";
	private static final String FILENAME = "drinks.json";
	
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_DATE = "date";
	private static final String JSON_DRINKS = "drinks";
	private static final String JSON_CALORIES = "calories";
	private static final String JSON_VOLUME = "volume";
	
	private ArrayList<Drink> mDrinks;
	private DrinkingBuddyJSONSerializer mSerializer;
	
	//private static DrinkLab sDrinkLab;
	private Context mAppContext;
	
	private String mTitle;
	private UUID mId;
	private Date mDate;
	private int mCalories;
	private int mTotalDrinks;
	private long mTotalTime;
	
	public DrinkLab(){
		mId = UUID.randomUUID();
		mDate = new Date();
		
		mDrinks = new ArrayList<Drink>();
	}
	
	public DrinkLab(JSONObject json) throws JSONException {
		JSONArray jsArray = new JSONArray();
		mDrinks = new ArrayList<Drink>();
		
		mId = UUID.fromString(json.getString(JSON_ID));
		if (json.has(JSON_TITLE)) {
			mTitle = json.getString(JSON_TITLE);
		}
		mDate = new Date(json.getLong(JSON_DATE));
		jsArray = json.getJSONArray(JSON_DRINKS);
		
		for (int i = 0; i < jsArray.length(); i++) {
			mDrinks.add(new Drink(jsArray.getJSONObject(i)));
		}
		mCalories = json.getInt(JSON_CALORIES);
	}
	
	/*public DrinkLab(Context appContext) {
		mAppContext = appContext;
		mSerializer = new DrinkingBuddyJSONSerializer(mAppContext, FILENAME);

		mId = UUID.randomUUID();
		mDate = new Date();
	
		//mDrinks = new ArrayList<Drink>();
		try {
			mDrinks = mSerializer.loadDrinks();
			Log.d(TAG, "Drinks loaded successfully");
		} catch (Exception e) {
			mDrinks = new ArrayList<Drink>();
			Log.e(TAG, "Drinks NOT loaded successfully", e);
		}
	}*/
	
	private DrinkLab(Parcel in) {
		this();
		
		in.readTypedList(mDrinks, Drink.CREATOR);
		mTitle = in.readString();
		mId = (UUID)in.readSerializable();
		mDate = (Date)in.readSerializable();
		mCalories = in.readInt();
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsArray = new JSONArray();
		
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_DATE, mDate.getTime());
		
		
		for (Drink d : mDrinks) {
			jsArray.put(d.toJSON());
		}
		
		json.put(JSON_DRINKS, jsArray);
		json.put(JSON_CALORIES, mCalories);
		
		return json;
	}
	
	public int describeContents() {
		// Ignore for now
		return this.hashCode();
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeTypedList(mDrinks);
		out.writeString(mTitle);
		out.writeSerializable(mId);
		out.writeSerializable(mDate);	
		out.writeInt(mCalories);
	}
	
	public static final Parcelable.Creator<DrinkLab> CREATOR = new Parcelable.Creator<DrinkLab>() {
		public DrinkLab createFromParcel(Parcel in) {
			return new DrinkLab(in);
		}
		
		public DrinkLab[] newArray(int size) {
			return new DrinkLab[size];
		}
	};
	
	public Date getDate() {
		return mDate;
	}
	
	public void setDate(Date date) {
		mDate = date;
	}
	
	public void addDrink(Drink d) {
		mDrinks.add(d);
	}
	
	public void deleteDrink(Drink d) {
		mDrinks.remove(d);
	}
	
	public int getDrinkCount() {
		return mDrinks.size();
	}
	
	public int getTotalVolume() {
		int volume = 0;
		
		for (Drink d : mDrinks) {
			volume += d.getVolume();
		}
		return volume;
	}
	
	public double getAverageAlcoholContent() {
		double alcohol_content = 0.0;
		double sum_of_alc_content = 0.0;
		
		for (Drink d : mDrinks) {
			sum_of_alc_content += d.getAlcoholContent();
			Log.d(TAG, "sum is " + sum_of_alc_content + " drink's alc content is " + d.getAlcoholContent());
		}
		
		alcohol_content = (sum_of_alc_content / mDrinks.size());
		
		return alcohol_content;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public UUID getId() {
		return mId;
	}
	
	public int getCalories() {
		mCalories = 0; // reset calorie count
		
		for (Drink d : mDrinks) {
			mCalories += d.getCalories();
		}
		return mCalories;
	}
	
	public int getTotalDrinks() {
		mTotalDrinks = mDrinks.size();
		
		return mTotalDrinks;
	}
	
	public String getTotalTime() {
		Date first_drink_time = mDrinks.get(0).getTime();
		Calendar c = Calendar.getInstance();
		Date time_right_now = new Date();
		
		
		long current_time_in_seconds = c.get(Calendar.HOUR) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
		
		long difference = time_right_now.getTime() - first_drink_time.getTime();

		Date drinking_duration = new Date(difference);
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
		
		long second = (difference / 1000) % 60;
		long minute = ((difference / (1000 * 60)) % 60) + 1;
		long hour = (difference / (1000 * 60 * 60)) % 24;

		String time = String.format("%02d:%02d:%02d", hour, minute, second);
		
		return time;
	}
	
	public String getDrinkingSessionTime() {
		if (mDrinks.size() == 0) {
			return "0:00";
		}
		
		Date first_drink_time = mDrinks.get(0).getTime();
		Date last_drink_time = mDrinks.get(mDrinks.size() -1).getTime();
		
		long difference = last_drink_time.getTime() - first_drink_time.getTime();

		Date drinking_duration = new Date(difference);
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
		
		//long second = (difference / 1000) % 60;
		
		long minute = ((difference / (1000 * 60)) % 60);
		// for cases when first_drink_time's minute value is greater
		// than the last_drink_time's minute value
		Log.d(TAG, "minute value is: " + minute);
		if (minute < 0) {
			minute = Math.abs(60 + minute);
		}
		long hour = ((difference / (1000*60*60)) % 24);
		Log.d(TAG, "hour value is: " + hour);
		
		if (hour < 0) {
			hour = Math.abs(23 + hour);
			Log.d(TAG, "hour value is: " + hour);
		}

		String time = String.format("%01d:%02d", hour, minute);
		
		return time;
	
	}
	
	// calculate the total # of hours of drinking for
	// the BAC calculator, which uses hours as its 
	// input for time.
	private long getHoursOfDrinking() {
		if (mDrinks.size() == 0) {
			return 0;
		}
		
		Date first_drink_time = mDrinks.get(0).getTime();
		Date time_right_now = new Date();
		// Date last_drink_time = mDrinks.get(mDrinks.size() -1).getTime();
		
		//long difference = last_drink_time.getTime() - first_drink_time.getTime();
		//long difference = time_right_now.getTime() - first_drink_time.getTime();
		
		long hour_now = (time_right_now.getTime() / (1000 * 60 * 60)); // I TOOK OFF MOD 24 ( % 24) TO SEE IF I CAN GET ABSOLUTE HOUR DURATION vs 24/hr based HOUR
		long hour_first_drink = (first_drink_time.getTime() / (1000 * 60 * 60)); // I TOOK OFF MOD 24 ( % 24) TO SEE IF I CAN GET ABSOLUTE HOUR DURATION vs 24/hr based HOUR
		Log.d(TAG, "Hour right now: " + hour_now);
		Log.d(TAG, "Hour first drink: " + hour_first_drink);
		
		
		long hour_difference = hour_now - hour_first_drink;
		Log.d(TAG, "Hour difference: " + hour_difference);
		
		if (hour_difference < 0) {
			hour_difference = Math.abs(23 + hour_difference);
			Log.d(TAG, "hour difference is: " + hour_difference);
		}
		
		/*
		long hour = (difference / (1000 * 60 * 60)) % 24;
		// for cases when first_drink_time's hour value is greater
		// than the last_drink_time's hour value
		if (hour < 0) {
			hour = Math.abs(23 + hour);
		}
		*/
		
		return hour_difference;
	}
	
	// calculate BAC using the Widmark formula 
	public String getBAC() {
		
		if (mDrinks.size() == 0) {
			return "0%";
		}
		
		// total hours of drinking
		long hours = getHoursOfDrinking();
		// int total_drinks = mDrinks.size();
		
		//int weight = Integer.parseInt(Person.get(mAppContext).getWeight());
		
		//int weight = Integer.parseInt(Person.get(mAppContext).getWeight());
		int weight = 180;
		
		int volume = getTotalVolume();
		Log.d(TAG, "volume is: " + volume);
		double average_alcohol_content = getAverageAlcoholContent();
		Log.d(TAG, "average alc content: " + average_alcohol_content);
		double alcohol_elimination_constant = 0.015;
		double gender_constant; 
		double bac_low = 0;
		String bac_low_string;
		
		
		// calculate the gender constant:
		// 0.55 for females and 0.68 for males
		
		if (Person.get(mAppContext).getGender().equals("female")) {
			gender_constant = 0.66;
		} else {
			gender_constant = 0.73;
		}
		
		Log.d(TAG, "Gender constant is: " + gender_constant);
		
		// get range for BAC, low and high
		bac_low = (((volume * average_alcohol_content) * (5.14)) / (weight * gender_constant)) - (0.015 * hours); 
		Log.d(TAG, "hours = " + hours);
		Log.d(TAG, "bac_low w/o abs value = " + bac_low);
		
		// convert rage into a String
		if (bac_low < 0) {
			bac_low_string = "0";
		} else {
			bac_low_string = String.format("%1$,.4f", bac_low);
		}
		//String bac_high_string = String.format("%1$,.3f", Math.abs(bac_high));
		
		return bac_low_string + "%";
	}
	
	public ArrayList<Drink> getDrinks() {
		return mDrinks;
	}
	
	public Drink getDrink(UUID id) {
		for (Drink d : mDrinks) {
			if (d.getId().equals(id))
				return d;
		}
		return null;
	}
	/*
	public boolean saveDrinks() {
		try {
			mSerializer.saveDrinkLab(mDrinks);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	*/
	public String toString() {
		return mTitle;
	}
	
	/** Static field used to regenerate object, individually or as arrays */
	public class MyDrinkLabCreator implements Parcelable.Creator<DrinkLab> {
		public DrinkLab createFromParcel(Parcel source) {
			return new DrinkLab(source);
		}
		
		public DrinkLab[] newArray(int size) {
			return new DrinkLab[size];
		}
	}
	
}