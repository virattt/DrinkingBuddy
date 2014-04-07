package com.virat.drinkingbuddy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.virat.drinkingbuddy.models.DrinkLab;
import com.virat.drinkingbuddy.models.Person;

import android.content.Context;

public class DrinkingBuddyJSONSerializer {
	
	private Context mContext;
	private String mFilename;
	
	public DrinkingBuddyJSONSerializer(Context c, String f) {
		mContext = c;
		mFilename = f;
	}
	
	public void savePerson(Person person) throws JSONException, IOException {
		// Build an array with JSON
		JSONArray array = new JSONArray();
		array.put(person.toJSON());
		
		// Write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
	}
	
	public void saveDrinkLab(ArrayList<DrinkLab> drinkLabs) throws JSONException, IOException {
		// Build an array with JSON
		JSONArray array = new JSONArray();
		for (DrinkLab d : drinkLabs) {
			array.put(d.toJSON());
		}
		
		// Write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	

	public JSONObject loadPerson() throws IOException, JSONException {
		JSONObject json = new JSONObject();
		BufferedReader reader = null;
		try {
			// Open and read the file into a StringBuilder
			InputStream in = mContext.openFileInput(mFilename);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Line breaks are omitted and irrelevant
				jsonString.append(line);
			}
			// Parse the JSON using JSONTokener
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			for (int i = 0; i < array.length(); i++) {
				json = array.getJSONObject(i);
			}
		} catch (FileNotFoundException e) {
			// Ignore this one; it happens when starting fresh
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return json;
	}
	
	public ArrayList<DrinkLab> loadDrinkLab() throws IOException, JSONException {
		ArrayList<DrinkLab> drinkLab = new ArrayList<DrinkLab>();
		BufferedReader reader = null;
		try {
			// Open and read the file into a StringBuilder
			InputStream in = mContext.openFileInput(mFilename);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Line breaks are omitted and irrelevant
				jsonString.append(line);
			}
			// Parse the JSON using JSONTokener
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			
			//Build the array of drink from JSONObjects
			for (int i = 0; i < array.length(); i++) {
				drinkLab.add(new DrinkLab(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// Ignore this one; it happens when starting fresh
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		return drinkLab;
	}
}
