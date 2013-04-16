package com.android.gers.shopping.list.DB;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.android.gers.shopping.list.ShoppingList;

import Utils.DbFromXml;
import Utils.DbToXml;
import Utils.FileUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShoppingListDb extends SQLiteOpenHelper {

	private DbTableLists dbTableLists;
	private DbTableItems dbTableItems;
	
	private TestDBGenerator debugHelper;
	Context myContext;
	
	public ShoppingListDb(Context context) {
		super(context, ShoppingList.DB.NAME, null, ShoppingList.DB.DB_VERSION);

		this.myContext = context;
		dbTableLists = new DbTableLists();
		dbTableItems = new DbTableItems(DbTableLists.TABLE_NAME, DbTableLists.COL_ID);
		
		debugHelper = new TestDBGenerator(context, this);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createIfNotPresent(db, dbTableLists);
		createIfNotPresent(db, dbTableItems);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		recreate(db);
	}
	
	public void deleteDb() {
		close();
		myContext.deleteDatabase(ShoppingList.DB.NAME);
	}
	
	public void createTestDb() throws IOException {
		debugHelper.CreateTestDB();
	}
	
	public boolean exportDbAsXml(String fileName) {
		BufferedOutputStream outputStream;
		try {
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
				
			outputStream = FileUtils.OpenFileForWriting(fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		DbToXml dbToXml = new DbToXml(getReadableDatabase(), outputStream, ShoppingList.DB.DB_VERSION);
		
		dbToXml.Execute();
		
		return true;
	}
	
	private void recreate(SQLiteDatabase db) {
		dbTableItems.drop(db);
		dbTableLists.drop(db);
		
		onCreate(db);
	}
	
	public boolean importDbAsXml(String fileName) throws Exception {
		InputStream inputStream;
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				Log.e(ShoppingList.LOG_NAME, "file " + file + " does not exist");
			}
				
			inputStream = FileUtils.GetFileStream(fileName);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		DbFromXml dbFromXml = new DbFromXml(inputStream);
		HashMap<String, List<ContentValues>> tablesToRows = dbFromXml.Execute();
		
		if (tablesToRows.size() == 0) {
			throw new Exception("Can't import empty xml file");
		}
		
		SQLiteDatabase db = getWritableDatabase();

		recreate(db);
		for (Entry<String, List<ContentValues>> item : tablesToRows.entrySet()) {
			for (ContentValues row : item.getValue()) {
				if (item.getKey().equals(DbTableLists.TABLE_NAME)) {
					insertList(db, row);
				} else if (item.getKey().equals(DbTableItems.TABLE_NAME)) {
					insertItem(db, row);
				} else {
					throw new Exception("Unknown table: " + item.getKey());
				}
			}
		}
		return true;
	}
	
	public long insertList(SQLiteDatabase db, String listName) {
		return dbTableLists.insert(db, listName); 
	}
	
	public long insertList(SQLiteDatabase db, ContentValues list) {
		return dbTableLists.insert(db, list);
	}

	public Boolean updateList(SQLiteDatabase db, long id, ContentValues kvps) {
		return dbTableLists.update(db, id, kvps);
	}	
	
	
	public long insertItem(SQLiteDatabase db, ContentValues kvps) {
		return dbTableItems.insert(db, kvps);
	}
	
	public Boolean updateItem(SQLiteDatabase db, long id, ContentValues kvps) {
		return dbTableItems.update(db, id, kvps);
	}	
	
	public Boolean updateByListId(SQLiteDatabase db, long listId, ContentValues kvps) {
		return dbTableItems.updateByListId(db, listId, kvps);
	}

	public Cursor getAllLists(SQLiteDatabase db, Boolean noComplete) {
		return dbTableLists.getAllLists(db, noComplete);
	}
	
	public Cursor getListById(SQLiteDatabase db, long id) {
		return dbTableLists.getListById(db, id);		
	}
	
	public Cursor getItemsByListID(SQLiteDatabase db, long listId, Boolean includeComplete) {
		return dbTableItems.getItemsByListId(db, listId, includeComplete);
	}
	
	public Cursor getItemsByID(SQLiteDatabase db, long itemId) {
		return dbTableItems.getItemById(db, itemId);
	}

	public Cursor getShoppingListItemStats(SQLiteDatabase db) {
		return dbTableItems.getItemStatsByListId(db);
	}
	
	public Boolean deleteListById(SQLiteDatabase db, long id) {
		if (deleteItemsByListId(db, id)) {
			Log.d(ShoppingList.LOG_NAME, "calling deleteListById");
			return dbTableLists.deleteListById(db, id);	
		}
		return false;
	}

	public Boolean deleteItemsByListId(SQLiteDatabase db, long listId) {
		Log.d(ShoppingList.LOG_NAME, "deleteItemsByListId");
		return dbTableItems.deleteItemsByListId(db, listId);
	}
	
	public Boolean deleteItemById(SQLiteDatabase db, long id) {
		return dbTableItems.deleteItemsById(db, id);
	}
	
	private static void createIfNotPresent(SQLiteDatabase db, DbTableBase dbTable) {
		if (!dbTable.exists(db)) {
			Log.d(ShoppingList.LOG_NAME, "table " + dbTable.getName() + " doesn't exist, creating");
			dbTable.create(db);
		}
	}

}
