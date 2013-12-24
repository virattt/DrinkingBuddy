package com.virat.drinkingbuddy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Drink implements Parcelable {
	
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_DATE = "date";
	private static final String JSON_PHOTO = "photo";
	private static final String JSON_CALORIES = "calories";
	private static final String JSON_VOLUME = "volume";
	private static final String JSON_ALCOHOL = "alcohol";
	
	private UUID mId;
	private String mTitle;
	private Date mDate;
	private Date mTime;
	private Photo mPhoto;
	private int mCalories = 100;
	private double mVolume = 12.0;
	private double mAlcoholContent = 0.06;
	
	public Drink() {
		//Generate unique identifier
		mId = UUID.randomUUID();
		mDate = new Date();
	}
	
	public Drink(JSONObject json) throws JSONException {
		mId = UUID.fromString(json.getString(JSON_ID));
		if (json.has(JSON_TITLE)) {
			mTitle = json.getString(JSON_TITLE);
		}
		mDate = new Date(json.getLong(JSON_DATE));
		if (json.has(JSON_PHOTO)) {
			mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
		}
		if (json.has(JSON_CALORIES)) {
			mCalories = json.getInt(JSON_CALORIES);
		}
		if (json.has(JSON_VOLUME)) {
			mVolume = json.getDouble(JSON_VOLUME);
		}
		if (json.has(JSON_ALCOHOL)) {
			mAlcoholContent = json.getDouble(JSON_ALCOHOL);
		}
		
	}
	
	/** Constructor from Parcel, reads back fields IN THE ORDER they were written */
	private Drink(Parcel in) {
		mId = (UUID)in.readSerializable();
		mTitle = in.readString();
		mDate = (Date)in.readSerializable();
		mTime = (Date)in.readSerializable();
		mCalories = in.readInt();
		mVolume = in.readDouble();
		mAlcoholContent = in.readDouble();
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_DATE, mDate.getTime());
		if (mPhoto != null) {
			json.put(JSON_PHOTO, mPhoto.toJSON());
		}
		json.put(JSON_CALORIES, mCalories);
		json.put(JSON_VOLUME, mVolume);
		json.put(JSON_ALCOHOL, mAlcoholContent);
		
		return json;
	}
	
	public int describeContents() {
		// Ignore for now
		return this.hashCode();
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeSerializable(mId);
		out.writeString(mTitle);
		out.writeSerializable(mDate);
		out.writeSerializable(mTime);
		out.writeInt(mCalories);
		out.writeDouble(mCalories);
		out.writeDouble(mAlcoholContent);
	}
	
	public static final Parcelable.Creator<Drink> CREATOR = new Parcelable.Creator<Drink>() {
		public Drink createFromParcel(Parcel in) {
			return new Drink(in);
		}
		
		public Drink[] newArray(int size) {
			return new Drink[size];
		}
	};
	
	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public UUID getId() {
		return mId;
	}
	
	public void setId(UUID id) {
		mId = id;
	}
	
	public String toString() {
		return mTitle;
	}
	
	public Date getDate() {
		return mDate;
	}
	
	public void setDate(Date date) {
		mDate = date;
	}
	
	public Date getTime() {
		return mDate;
	}
	
	public void setTime(Date time) {
		mDate = time;
	}

	public Photo getPhoto() {
		return mPhoto;
	}
	
	public void setPhoto(Photo p) {
		mPhoto = p;
	}
	
	public void setCalories(int calories) {
		mCalories = calories;
	}
	
	public int getCalories() {
		return mCalories;
	}
	
	public void setVolume(double volume) {
		mVolume = volume;
	}
	
	public double getVolume() {
		return mVolume;
	}
	
	public void setAlcoholContent(double alcoholContent) {
		mAlcoholContent = alcoholContent;
	}
	
	public double getAlcoholContent() {
		return mAlcoholContent;
	}
	
	/** Static field used to regenerate object, individually or as arrays */
	public class MyCreator implements Parcelable.Creator<Drink> {
		public Drink createFromParcel(Parcel source) {
			return new Drink(source);
		}
		
		public Drink[] newArray(int size) {
			return new Drink[size];
		}
	}
	
}
