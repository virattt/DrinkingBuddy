package com.virat.drinkingbuddy.models;

import java.text.DecimalFormat;
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
	private static final String JSON_DRINKING_DURATION = "drinking_duration";
	private static final String JSON_DATE = "date";
	private static final String JSON_DRINKS = "drinks";
	private static final String JSON_CALORIES = "calories";
	private static final String JSON_VOLUME = "volume";
	private static final String JSON_IS_DRINKING = "is_drinking";

	private ArrayList<Drink> mDrinks;
	private DrinkingBuddyJSONSerializer mSerializer;

	private Context mAppContext;

	private String mTitle;
	private String mDrinkingDuration;
	private UUID mId;
	private Date mDate;
	private int mCalories;
	private boolean mIsDrinking;

	private int mTotalDrinks;

	public DrinkLab() {
		mId = UUID.randomUUID();
		mDate = new Date();

		mDrinks = new ArrayList<Drink>();
		mIsDrinking = true;
		mDrinkingDuration = "0:00";
	}

	public DrinkLab(JSONObject json) throws JSONException {
		JSONArray jsArray = new JSONArray();
		mDrinks = new ArrayList<Drink>();

		mId = UUID.fromString(json.getString(JSON_ID));
		if (json.has(JSON_TITLE)) {
			mTitle = json.getString(JSON_TITLE);
		}
		if (json.has(JSON_DRINKING_DURATION)) {
			mDrinkingDuration = json.getString(JSON_DRINKING_DURATION);
		}
		mDate = new Date(json.getLong(JSON_DATE));
		jsArray = json.getJSONArray(JSON_DRINKS);

		for (int i = 0; i < jsArray.length(); i++) {
			mDrinks.add(new Drink(jsArray.getJSONObject(i)));
		}
		mCalories = json.getInt(JSON_CALORIES);
		mIsDrinking = json.getBoolean(JSON_IS_DRINKING);

	}

	private DrinkLab(Parcel in) {
		this();

		in.readTypedList(mDrinks, Drink.CREATOR);
		mTitle = in.readString();
		mDrinkingDuration = in.readString();
		mId = (UUID) in.readSerializable();
		mDate = (Date) in.readSerializable();
		mCalories = in.readInt();
		mIsDrinking = in.readByte() != 0;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsArray = new JSONArray();

		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_DRINKING_DURATION, mDrinkingDuration);
		json.put(JSON_DATE, mDate.getTime());

		for (Drink d : mDrinks) {
			jsArray.put(d.toJSON());
		}

		json.put(JSON_DRINKS, jsArray);
		json.put(JSON_CALORIES, mCalories);
		json.put(JSON_IS_DRINKING, mIsDrinking);

		return json;
	}

	public int describeContents() {
		// Ignore for now
		return this.hashCode();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeTypedList(mDrinks);
		out.writeString(mTitle);
		out.writeString(mDrinkingDuration);
		out.writeSerializable(mId);
		out.writeSerializable(mDate);
		out.writeInt(mCalories);
		out.writeByte((byte) (mIsDrinking ? 1 : 0));
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
		mDrinks.add(0, d);
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

	public double getSumAlcoholContent() {
		double alcohol_content = 0.0;

		for (Drink d : mDrinks) {
			alcohol_content += (d.getAlcoholContent() * d.getVolume());
		}

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

	public boolean getIsDrinking() {
		return mIsDrinking;
	}

	public void setIsDrinking(boolean isDrinking) {
		mIsDrinking = isDrinking;
	}

	public int getTotalDrinks() {
		mTotalDrinks = mDrinks.size();

		return mTotalDrinks;
	}

	public String getTotalTime() {
		int first_drink_index = 0;
		if (mDrinks.size() == 0) {
			return "0:00";
		} else {
			first_drink_index = mDrinks.size() - 1;
		}

		// Get the time of the first drink
		Date first_drink_time = mDrinks.get(first_drink_index).getTime();
		// Get the current time
		Date time_right_now = new Date();

		// Get the difference in TOTAL time between now and first drink
		long difference = time_right_now.getTime() - first_drink_time.getTime();
		// Get the difference in minutes between now and first drink
		double minute_difference = Math.floor((difference / (1000 * 60)) % 60);
		// Get the difference in hours between now and first drink
		double hour = Math.floor(((difference / (1000 * 60 * 60)) % 24));

		// For cases when first_drink_time's minute value is greater
		// than the current minutes value; i.e. :55 > :53
		if (minute_difference < 0) {
			minute_difference = Math.abs(60 + minute_difference);
		}

		String time = String.format("%01d:%02d", (int) hour,
				(int) minute_difference);
		mDrinkingDuration = time;
		return mDrinkingDuration;
	}

	public String getDrinkingDuration(DrinkLab drinkLab) {
		if (drinkLab.getIsDrinking()) {
			return getTotalTime();
		} else {
			return mDrinkingDuration;
		}
	}

	// calculate the total # of hours of drinking for
	// the BAC calculator, which uses hours as its
	// input for time.
	protected int getHoursOfDrinking() {
		int first_drink_index = 0;

		if (mDrinks.size() == 0) {
			return 0;
		} else {
			first_drink_index = mDrinks.size() - 1;
		}

		Date first_drink_time = mDrinks.get(first_drink_index).getTime();
		Date time_right_now = new Date();

		long hour_now = (time_right_now.getTime() / (1000 * 60 * 60)); // I TOOK
																		// OFF
																		// MOD
																		// 24 (
																		// % 24)
																		// TO
																		// SEE
																		// IF I
																		// CAN
																		// GET
																		// ABSOLUTE
																		// HOUR
																		// DURATION
																		// vs
																		// 24/hr
																		// based
																		// HOUR
		long hour_first_drink = (first_drink_time.getTime() / (1000 * 60 * 60)); // I
																					// TOOK
																					// OFF
																					// MOD
																					// 24
																					// (
																					// %
																					// 24)
																					// TO
																					// SEE
																					// IF
																					// I
																					// CAN
																					// GET
																					// ABSOLUTE
																					// HOUR
																					// DURATION
																					// vs
																					// 24/hr
																					// based
																					// HOUR

		double hour_difference = Math.floor(hour_now - hour_first_drink);

		if (hour_difference < 0) {
			hour_difference = Math.abs(23 + hour_difference);
		}

		return (int) hour_difference;
	}

	// calculate BAC using the Widmark formula
	public double getBAC() {

		if (mDrinks.size() == 0) {
			return 0.000;
		}

		// total hours of drinking
		long hours = getHoursOfDrinking();

		int weight = Integer.parseInt(Person.get(mAppContext).getWeight());

		double sum_alcohol_content = getSumAlcoholContent();
		double alcohol_elimination_constant = 0.015;
		double gender_constant;
		double bac_double = 0.000;
		double bac_double_formatted = 0.000;
		// calculate the gender constant:
		// 0.55 for females and 0.68 for males
		if (Person.get(mAppContext).getGender().equals("female")) {
			gender_constant = 0.66;
		} else {
			gender_constant = 0.73;
		}

		// get range for BAC, low and high
		bac_double = ((sum_alcohol_content * 5.14) / (weight * gender_constant))
				- (alcohol_elimination_constant * hours);
		bac_double_formatted = Math.round(bac_double * 1000) / 1000.0;

		// prevent getBAC from returning a negative value
		if (bac_double_formatted < 0) {
			return 0.000;
		} else {
			return bac_double_formatted;

		}
	}

	public String getImpairmentLevel() {
		double BAC = getBAC();

		if (BAC > .02 && BAC < .08) {
			return "Possibly impaired. Do NOT drive.";
		} else if (BAC >= .08) {
			return "Legally impaired. Do NOT drive.";
		} else {
			return "Not Impaired";
		}
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
