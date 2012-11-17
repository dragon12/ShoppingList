package com.android.gers.shopping.list.DB;

import com.android.gers.shopping.list.ShoppingList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DbTableBase {
	private String tableName;
	
	public DbTableBase(String tableName) {
		this.tableName = tableName;
	}
	
	public String getName() {
		return tableName;
	}
	
	public boolean exists(SQLiteDatabase db) {
		Cursor results = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';", null);
		
		results.moveToFirst();
		Boolean retVal = !results.isAfterLast();
		results.close();
		
		return retVal;
	}
	
	public void drop(SQLiteDatabase db) {
		Log.i(ShoppingList.LOG_NAME, "Upgrading db, deleting all items in old table " + tableName + "!");
		db.execSQL("drop table if exists " + tableName);
	}
	
	public abstract void create(SQLiteDatabase db);
}
