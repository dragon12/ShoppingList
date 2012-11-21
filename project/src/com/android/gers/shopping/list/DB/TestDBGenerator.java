package com.android.gers.shopping.list.DB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.android.gers.shopping.list.R;
import com.android.gers.shopping.list.ShoppingList;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TestDBGenerator {

	private ShoppingListDb dbHelper;
	private Context context;

	public TestDBGenerator(Context context, ShoppingListDb dbHelper) {
		this.dbHelper = dbHelper;
		this.context = context;
	}
	
	public void CreateTestDB() {
		dbHelper.deleteDb();
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
    	List<ContentValues> lists = parseCsv(context.getString(R.string.test_db_data_lists));
    	for (ContentValues kvps : lists) {
    		dbHelper.insertList(db, kvps);
    	}
    	
    	List<ContentValues> items = parseCsv(context.getString(R.string.test_db_data_items));
    	for (ContentValues kvps : items) {
    		dbHelper.insertItem(db, kvps);
    	}    	
	}

	private List<ContentValues> parseCsv(String file)
	{
		List<ContentValues> retVal = new ArrayList<ContentValues>();
		InputStream input = null;
		try {
			input = context.getAssets().open(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			Boolean initialised = false;
			List<String> headings = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] RowData = line.split(",");

				if (!initialised) {
					for (String heading : RowData) {
						headings.add(heading);
					}
					initialised = true;
				} else {
					ContentValues kvps = new ContentValues();
					for (int i = 0; i < RowData.length; ++i) {
						kvps.put(headings.get(i), RowData[i]);
					}
					retVal.add(kvps);
				}
			}
		}
		catch (IOException ex) {
			// handle exception
			Log.e(ShoppingList.LOG_NAME, "caught io exception: " + ex.toString());
		}
		finally {
			try {
				if (input != null) {
					input.close();
				}
			}
			catch (IOException e) {
				// handle exception
			}
		}
		return retVal;
	}
}
