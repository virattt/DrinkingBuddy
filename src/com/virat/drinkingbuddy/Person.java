package com.virat.drinkingbuddy;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Person {
	private static final String FILENAME = "person.json";
	
	private static final String JSON_NAME = "name";
	private static final String JSON_GENDER = "gender";
	private static final String JSON_WEIGHT = "weight";
	private static final String JSON_IS_DRINKING = "is_drinking";
	
	private static Person sPerson;
	private Context mAppContext;
	private DrinkingBuddyJSONSerializer mSerializer;
	
	
	private String mName;
	private String mGender = "none";
	private String mWeight;
	private boolean mIsDrinking;
	
	public Person(Context appContext) {
		mAppContext = appContext;
		JSONObject json = new JSONObject();
		
		mSerializer = new DrinkingBuddyJSONSerializer(mAppContext, FILENAME);
		try {
			json = mSerializer.loadPerson();
			mName = json.getString(JSON_NAME);
			mGender = json.getString(JSON_GENDER);
			mWeight = json.getString(JSON_WEIGHT);
			mIsDrinking = json.getBoolean(JSON_IS_DRINKING);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		//json.put(JSON_PERSON, sPerson);
		json.put(JSON_NAME, mName);
		json.put(JSON_GENDER, mGender);
		json.put(JSON_WEIGHT, mWeight);
		json.put(JSON_IS_DRINKING, mIsDrinking);
		
		return json;
	}
	
	public static Person get(Context c) {
		if (sPerson == null) {
			sPerson = new Person(c.getApplicationContext());
		}
		return sPerson;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getGender() {
		return mGender;
	}

	public void setGender(String gender) {
		mGender = gender;
	}

	public String getWeight() {
		return mWeight;
	}

	public void setWeight(String weight) {
		mWeight = weight;
	}
	
	public boolean getIsDrinking() {
		return mIsDrinking;
	}
	
	public void setIsDrinking(boolean isDrinking) {
		mIsDrinking = isDrinking;
	}
	
	public boolean savePerson() {
		try {
			mSerializer.savePerson(sPerson);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
